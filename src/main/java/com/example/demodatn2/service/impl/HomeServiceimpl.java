package com.example.demodatn2.service.impl;

import com.example.demodatn2.dto.HomeProductVM;
import com.example.demodatn2.dto.ProductDetailVM;
import com.example.demodatn2.entity.DanhMuc;
import com.example.demodatn2.entity.HinhAnhMauSac;
import com.example.demodatn2.entity.HinhAnhSanPham;
import com.example.demodatn2.entity.SanPham;
import com.example.demodatn2.repository.BienTheSanPhamRepository;
import com.example.demodatn2.repository.DanhMucRepository;
import com.example.demodatn2.repository.HinhAnhMauSacRepository;
import com.example.demodatn2.repository.HinhAnhSanPhamRepository;
import com.example.demodatn2.repository.SanPhamRepository;
import com.example.demodatn2.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// Triển khai dữ liệu trang chủ: lọc sản phẩm, phân trang và dựng chi tiết sản phẩm cho UI.
public class HomeServiceimpl implements HomeService {
    private final SanPhamRepository sanPhamRepository;
    private final HinhAnhSanPhamRepository hinhAnhSanPhamRepository;
    private final HinhAnhMauSacRepository hinhAnhMauSacRepository;
    private final BienTheSanPhamRepository bienTheSanPhamRepository;
    private final DanhMucRepository danhMucRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HomeProductVM> getHomeProducts(Integer danhMucId, String keyword) {
        List<SanPham> products;

        if (keyword != null && !keyword.trim().isEmpty()) {
            products = sanPhamRepository.searchActive(keyword.trim());
        } else if (danhMucId != null) {
            Optional<DanhMuc> dmOpt = danhMucRepository.findById(danhMucId);
            if (dmOpt.isPresent()) {
                DanhMuc dm = dmOpt.get();
                // Nếu là danh mục cha, lấy tất cả con
                if (dm.getDanhMucCha() == null) {
                    List<Integer> ids = new ArrayList<>();
                    ids.add(dm.getId());
                    if (dm.getDanhMucCon() != null) {
                        for (DanhMuc con : dm.getDanhMucCon()) {
                            ids.add(con.getId());
                        }
                    }
                    products = sanPhamRepository.findActiveByDanhMucIds(ids);
                } else {
                    products = sanPhamRepository.findActiveByDanhMucId(danhMucId);
                }
            } else {
                products = sanPhamRepository.findActiveForListing();
            }
        } else {
            products = sanPhamRepository.findActiveForListing();
        }

        return products.stream().map(sp -> {
           String anhChinh=hinhAnhSanPhamRepository
                   .findFirstBySanPham_IdOrderByLaAnhChinhDescThuTuAscIdAsc(sp.getId())
                   .map(HinhAnhSanPham::getDuongDanAnh)
                   .orElse(null);
           //khoanggia
           BienTheSanPhamRepository.PriceRange range=bienTheSanPhamRepository.findPriceRange(sp.getId());
           //mausac
           List<String> mauSac=bienTheSanPhamRepository.findDistinctMauSac(sp.getId());
           List<String> kichCos=bienTheSanPhamRepository.findDistinctKichCo(sp.getId());
           Map<String, String> hinhAnhTheoMau = hinhAnhMauSacRepository.findBySanPham_Id(sp.getId()).stream()
               .collect(Collectors.toMap(
                   h -> normalizeColorKey(h.getMauSac()),
                   HinhAnhMauSac::getDuongDanAnh,
                   (existing, replacement) -> existing
               ));
           List<HomeProductVM.BienTheNhanhVM> bienThes = sp.getBienThes().stream()
               .filter(bt -> bt.getTrangThai() == null || "ACTIVE".equalsIgnoreCase(bt.getTrangThai()))
               .filter(bt -> bt.getSoLuongTon() != null && bt.getSoLuongTon() > 0)
               .map(bt -> HomeProductVM.BienTheNhanhVM.builder()
                   .id(bt.getId())
                   .mauSac(bt.getMauSac())
                   .kichCo(bt.getKichCo())
                   .soLuongTon(bt.getSoLuongTon())
               .anhMauSac(hinhAnhTheoMau.getOrDefault(normalizeColorKey(bt.getMauSac()), anhChinh))
                   .build())
               .collect(Collectors.toList());
           return HomeProductVM.builder()
                   .id(sp.getId())
                   .ten(sp.getTen())
                   .anhChinh(anhChinh)
                   .giaMin(range != null ? range.getMinGia() : null)
                   .giaMax(range != null ? range.getMaxGia() : null)
                   .mauSacs(mauSac)
               .kichCos(kichCos)
                   .maDanhMuc(sp.getDanhMuc() != null ? sp.getDanhMuc().getMa() : null)
                   .idDanhMuc(sp.getDanhMuc() != null ? sp.getDanhMuc().getId() : null)
                   .danhMuc(sp.getDanhMuc())
               .bienThes(bienThes)
                   .build();

       }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HomeProductVM> getHomeProductsPage(Integer danhMucId, String keyword, Pageable pageable) {
        Page<SanPham> page;

        if (keyword != null && !keyword.trim().isEmpty()) {
            page = sanPhamRepository.searchActivePage(keyword.trim(), pageable);
        } else if (danhMucId != null) {
            Optional<DanhMuc> dmOpt = danhMucRepository.findById(danhMucId);
            if (dmOpt.isPresent()) {
                DanhMuc dm = dmOpt.get();
                if (dm.getDanhMucCha() == null) {
                    List<Integer> ids = new ArrayList<>();
                    ids.add(dm.getId());
                    if (dm.getDanhMucCon() != null) {
                        for (DanhMuc con : dm.getDanhMucCon()) {
                            ids.add(con.getId());
                        }
                    }
                    page = sanPhamRepository.findActiveByDanhMucIdsPage(ids, pageable);
                } else {
                    page = sanPhamRepository.findActiveByDanhMucIdPage(danhMucId, pageable);
                }
            } else {
                page = sanPhamRepository.findActiveForListingPage(pageable);
            }
        } else {
            page = sanPhamRepository.findActiveForListingPage(pageable);
        }

        return page.map(sp -> {
            String anhChinh = hinhAnhSanPhamRepository
                    .findFirstBySanPham_IdOrderByLaAnhChinhDescThuTuAscIdAsc(sp.getId())
                    .map(HinhAnhSanPham::getDuongDanAnh)
                    .orElse(null);
            BienTheSanPhamRepository.PriceRange range = bienTheSanPhamRepository.findPriceRange(sp.getId());
            List<String> mauSac = bienTheSanPhamRepository.findDistinctMauSac(sp.getId());
            List<String> kichCos = bienTheSanPhamRepository.findDistinctKichCo(sp.getId());
            Map<String, String> hinhAnhTheoMau = hinhAnhMauSacRepository.findBySanPham_Id(sp.getId()).stream()
                    .collect(Collectors.toMap(
                            h -> normalizeColorKey(h.getMauSac()),
                            HinhAnhMauSac::getDuongDanAnh,
                            (existing, replacement) -> existing
                    ));
                List<HomeProductVM.BienTheNhanhVM> bienThes = sp.getBienThes().stream()
                    .filter(bt -> bt.getTrangThai() == null || "ACTIVE".equalsIgnoreCase(bt.getTrangThai()))
                    .filter(bt -> bt.getSoLuongTon() != null && bt.getSoLuongTon() > 0)
                    .map(bt -> HomeProductVM.BienTheNhanhVM.builder()
                        .id(bt.getId())
                        .mauSac(bt.getMauSac())
                        .kichCo(bt.getKichCo())
                        .soLuongTon(bt.getSoLuongTon())
                        .anhMauSac(hinhAnhTheoMau.getOrDefault(normalizeColorKey(bt.getMauSac()), anhChinh))
                        .build())
                    .collect(Collectors.toList());
            return HomeProductVM.builder()
                    .id(sp.getId())
                    .ten(sp.getTen())
                    .anhChinh(anhChinh)
                    .giaMin(range != null ? range.getMinGia() : null)
                    .giaMax(range != null ? range.getMaxGia() : null)
                    .mauSacs(mauSac)
                    .kichCos(kichCos)
                    .maDanhMuc(sp.getDanhMuc() != null ? sp.getDanhMuc().getMa() : null)
                    .idDanhMuc(sp.getDanhMuc() != null ? sp.getDanhMuc().getId() : null)
                    .danhMuc(sp.getDanhMuc())
                    .bienThes(bienThes)
                    .build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailVM getProductDetail(Integer id) {
        // Fetch SanPham with each collection separately to avoid MultipleBagFetchException
        SanPham sp = sanPhamRepository.findDetailWithBienTheById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // Fetch other collections into their own entities, then copy into sp
        SanPham spWithMauSac = sanPhamRepository.findDetailWithHinhAnhMauSacById(id).orElse(sp);
        SanPham spWithGallery = sanPhamRepository.findDetailWithHinhAnhSanPhamById(id).orElse(sp);

        List<String> gallery = spWithGallery.getHinhAnhSanPhams().stream()
                .sorted((a, b) -> {
                    if (a.getLaAnhChinh() && !b.getLaAnhChinh()) return -1;
                    if (!a.getLaAnhChinh() && b.getLaAnhChinh()) return 1;
                    return a.getThuTu().compareTo(b.getThuTu());
                })
                .map(HinhAnhSanPham::getDuongDanAnh)
                .collect(Collectors.toList());

        Map<String, String> hinhAnhTheoMau = spWithMauSac.getHinhAnhMauSacs().stream()
                .collect(Collectors.toMap(
                        HinhAnhMauSac::getMauSac,
                        HinhAnhMauSac::getDuongDanAnh,
                        (existing, replacement) -> existing
                ));

        List<ProductDetailVM.BienTheVM> bienThes = sp.getBienThes().stream()
                .filter(bt -> "ACTIVE".equals(bt.getTrangThai()))
                .map(bt -> ProductDetailVM.BienTheVM.builder()
                        .id(bt.getId())
                        .maSKU(bt.getMaSKU())
                        .mauSac(bt.getMauSac())
                        .kichCo(bt.getKichCo())
                        .gia(bt.getGia())
                        .giaGoc(bt.getGiaGoc())
                        .soLuongTon(bt.getSoLuongTon())
                        .build())
                .collect(Collectors.toList());

        // Resolve danhMuc fields inside transaction (avoid lazy proxy outside session)
        Integer danhMucId = null, danhMucChaId = null;
        String danhMucTen = null, danhMucChaTen = null;
        if (sp.getDanhMuc() != null) {
            danhMucId = sp.getDanhMuc().getId();
            danhMucTen = sp.getDanhMuc().getTen();
            if (sp.getDanhMuc().getDanhMucCha() != null) {
                danhMucChaId = sp.getDanhMuc().getDanhMucCha().getId();
                danhMucChaTen = sp.getDanhMuc().getDanhMucCha().getTen();
            }
        }

        return ProductDetailVM.builder()
                .id(sp.getId())
                .maSanPham(sp.getMaSanPham())
                .ten(sp.getTen())
                .moTaNgan(sp.getMoTaNgan())
                .moTa(sp.getMoTa())
                .chatLieu(sp.getChatLieu())
                .gioiTinh(sp.getGioiTinh())
                .danhMucId(danhMucId)
                .danhMucTen(danhMucTen)
                .danhMucChaId(danhMucChaId)
                .danhMucChaTen(danhMucChaTen)
                .hinhAnhGallery(gallery)
                .hinhAnhTheoMau(hinhAnhTheoMau)
                .bienThes(bienThes)
                .build();
    }

    private String normalizeColorKey(String color) {
        if (color == null) {
            return "";
        }
        String normalized = Normalizer.normalize(color, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .trim()
            .toLowerCase()
            .replaceAll("\\s+", " ");
        return normalized;
    }

}
