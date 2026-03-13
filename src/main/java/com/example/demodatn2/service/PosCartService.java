package com.example.demodatn2.service;

import com.example.demodatn2.dto.PosCartItemDTO;
import com.example.demodatn2.dto.PosInvoiceSummaryDTO;
import com.example.demodatn2.entity.BienTheSanPham;
import com.example.demodatn2.entity.HinhAnhMauSac;
import com.example.demodatn2.entity.HinhAnhSanPham;
import com.example.demodatn2.entity.SanPham;
import com.example.demodatn2.repository.BienTheSanPhamRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PosCartService {

    private static final String SESSION_CARTS   = "POS_CARTS";
    private static final String SESSION_ACTIVE  = "POS_ACTIVE_INVOICE";
    private static final String SESSION_COUNTER = "POS_INVOICE_COUNTER";

    private final BienTheSanPhamRepository bienTheSanPhamRepository;

    @Transactional(readOnly = true)
    public List<PosCartItemDTO> getCart(HttpSession session) {
        return new ArrayList<>(getMutableCart(session));
    }

    @Transactional(readOnly = true)
    public List<PosCartItemDTO> addItem(HttpSession session, Integer variantId, Integer qty) {
        if (variantId == null) {
            throw new RuntimeException("variantId không hợp lệ");
        }
        int quantity = qty == null ? 1 : qty;
        if (quantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        BienTheSanPham variant = bienTheSanPhamRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại: " + variantId));

        if (variant.getSoLuongTon() == null || variant.getSoLuongTon() <= 0) {
            throw new RuntimeException("Sản phẩm đã hết hàng");
        }

        List<PosCartItemDTO> cart = getMutableCart(session);
        PosCartItemDTO existing = cart.stream()
                .filter(item -> Objects.equals(item.getVariantId(), variantId))
                .findFirst()
                .orElse(null);

        int currentQty = existing != null && existing.getQty() != null ? existing.getQty() : 0;
        int nextQty = currentQty + quantity;
        if (nextQty > variant.getSoLuongTon()) {
            throw new RuntimeException("Số lượng vượt quá tồn kho");
        }

        if (existing != null) {
            existing.setQty(nextQty);
            existing.setStock(variant.getSoLuongTon());
            existing.setPrice(variant.getGia());
        } else {
            cart.add(toPosCartItem(variant, quantity));
        }

        return new ArrayList<>(cart);
    }

    @Transactional(readOnly = true)
    public List<PosCartItemDTO> updateQty(HttpSession session, Integer variantId, Integer qty) {
        if (variantId == null) {
            throw new RuntimeException("variantId không hợp lệ");
        }
        if (qty == null || qty <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        BienTheSanPham variant = bienTheSanPhamRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại: " + variantId));

        if (qty > variant.getSoLuongTon()) {
            throw new RuntimeException("Số lượng vượt quá tồn kho");
        }

        List<PosCartItemDTO> cart = getMutableCart(session);
        PosCartItemDTO existing = cart.stream()
                .filter(item -> Objects.equals(item.getVariantId(), variantId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Sản phẩm chưa có trong giỏ"));

        existing.setQty(qty);
        existing.setStock(variant.getSoLuongTon());
        existing.setPrice(variant.getGia());
        return new ArrayList<>(cart);
    }

    public List<PosCartItemDTO> removeItem(HttpSession session, Integer variantId) {
        List<PosCartItemDTO> cart = getMutableCart(session);
        cart.removeIf(item -> Objects.equals(item.getVariantId(), variantId));
        return new ArrayList<>(cart);
    }

    public void clear(HttpSession session) {
        getMutableCart(session).clear();
    }

    // ─── Invoice management ───────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, List<PosCartItemDTO>> getAllCarts(HttpSession session) {
        Object raw = session.getAttribute(SESSION_CARTS);
        if (raw instanceof LinkedHashMap) {
            return (LinkedHashMap<String, List<PosCartItemDTO>>) raw;
        }
        LinkedHashMap<String, List<PosCartItemDTO>> carts = new LinkedHashMap<>();
        session.setAttribute(SESSION_CARTS, carts);
        return carts;
    }

    public String getActiveInvoiceId(HttpSession session) {
        String activeId = (String) session.getAttribute(SESSION_ACTIVE);
        if (activeId == null || !getAllCarts(session).containsKey(activeId)) {
            activeId = createInvoice(session);
        }
        return activeId;
    }

    public String createInvoice(HttpSession session) {
        LinkedHashMap<String, List<PosCartItemDTO>> carts = getAllCarts(session);
        Integer counter = (Integer) session.getAttribute(SESSION_COUNTER);
        if (counter == null) counter = 0;
        counter++;
        session.setAttribute(SESSION_COUNTER, counter);
        String invoiceId = "HD" + counter;
        carts.put(invoiceId, new ArrayList<>());
        session.setAttribute(SESSION_ACTIVE, invoiceId);
        return invoiceId;
    }

    public String switchInvoice(HttpSession session, String invoiceId) {
        if (!getAllCarts(session).containsKey(invoiceId)) {
            throw new RuntimeException("Hóa đơn không tồn tại: " + invoiceId);
        }
        session.setAttribute(SESSION_ACTIVE, invoiceId);
        return invoiceId;
    }

    public String deleteInvoice(HttpSession session, String invoiceId) {
        LinkedHashMap<String, List<PosCartItemDTO>> carts = getAllCarts(session);
        carts.remove(invoiceId);
        String activeId = (String) session.getAttribute(SESSION_ACTIVE);
        if (invoiceId.equals(activeId)) {
            if (!carts.isEmpty()) {
                activeId = carts.keySet().iterator().next();
            } else {
                activeId = createInvoice(session);
            }
            session.setAttribute(SESSION_ACTIVE, activeId);
        }
        return (String) session.getAttribute(SESSION_ACTIVE);
    }

    public List<PosInvoiceSummaryDTO> listInvoices(HttpSession session) {
        String activeId = getActiveInvoiceId(session);
        LinkedHashMap<String, List<PosCartItemDTO>> carts = getAllCarts(session);
        List<PosInvoiceSummaryDTO> result = new ArrayList<>();
        int index = 1;
        for (Map.Entry<String, List<PosCartItemDTO>> entry : carts.entrySet()) {
            List<PosCartItemDTO> items = entry.getValue();
            int itemCount = items.stream().mapToInt(i -> i.getQty() != null ? i.getQty() : 0).sum();
            double total = items.stream().mapToDouble(i -> {
                double price = i.getPrice() != null ? i.getPrice().doubleValue() : 0;
                int qty = i.getQty() != null ? i.getQty() : 0;
                return price * qty;
            }).sum();
            result.add(new PosInvoiceSummaryDTO(
                    entry.getKey(), "HĐ " + index, itemCount, total, entry.getKey().equals(activeId)));
            index++;
        }
        return result;
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private List<PosCartItemDTO> getMutableCart(HttpSession session) {
        String activeId = getActiveInvoiceId(session);
        return getAllCarts(session).computeIfAbsent(activeId, k -> new ArrayList<>());
    }

    private PosCartItemDTO toPosCartItem(BienTheSanPham variant, int qty) {
        return PosCartItemDTO.builder()
                .variantId(variant.getId())
                .productName(variant.getSanPham().getTen())
                .color(variant.getMauSac())
                .size(variant.getKichCo())
                .price(variant.getGia())
                .qty(qty)
                .stock(variant.getSoLuongTon())
                .img(resolveImage(variant.getSanPham(), variant.getMauSac()))
                .build();
    }

    private String resolveImage(SanPham sanPham, String color) {
        List<HinhAnhMauSac> colorImages = sanPham.getHinhAnhMauSacs();
        if (colorImages != null) {
            String byColor = colorImages.stream()
                    .filter(img -> img.getMauSac() != null && img.getMauSac().equalsIgnoreCase(color))
                    .map(HinhAnhMauSac::getDuongDanAnh)
                    .filter(path -> path != null && !path.isBlank())
                    .findFirst()
                    .orElse(null);
            if (byColor != null) {
                return byColor;
            }
        }

        List<HinhAnhSanPham> productImages = sanPham.getHinhAnhSanPhams();
        if (productImages == null || productImages.isEmpty()) {
            return null;
        }

        return productImages.stream()
                .sorted(Comparator
                        .comparing((HinhAnhSanPham img) -> !Boolean.TRUE.equals(img.getLaAnhChinh()))
                        .thenComparing(HinhAnhSanPham::getThuTu, Comparator.nullsLast(Integer::compareTo)))
                .map(HinhAnhSanPham::getDuongDanAnh)
                .filter(path -> path != null && !path.isBlank())
                .findFirst()
                .orElse(null);
    }
}
