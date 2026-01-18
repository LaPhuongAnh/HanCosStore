package com.example.demodatn2.service;

import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.entity.*;
import com.example.demodatn2.repository.*;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final GioHangRepository gioHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final BienTheSanPhamRepository bienTheSanPhamRepository;
    private final MaGiamGiaRepository maGiamGiaRepository;
    private final LichSuSuDungMaGiamGiaRepository lichSuSuDungMaGiamGiaRepository;
    @Getter
    private final VoucherService voucherService;

    @Transactional(readOnly = true)
    public List<DonHang> getAllOrders() {
        return donHangRepository.findAllByOrderByNgayDatDesc();
    }

    @Transactional(readOnly = true)
    public List<DonHang> searchOrders(String keyword, String status) {
        if (keyword == null || keyword.trim().isEmpty()) {
            if (status != null && !status.isEmpty() && !status.equals("ALL")) {
                return getOrdersByStatus(status);
            }
            return getAllOrders();
        }

        keyword = keyword.trim();
        if (status != null && !status.isEmpty() && !status.equals("ALL")) {
            return donHangRepository.searchWithStatus(keyword, status);
        }
        return donHangRepository.search(keyword);
    }

    @Transactional(readOnly = true)
    public List<DonHang> getOrdersByStatus(String status) {
        return donHangRepository.findByTrangThaiOrderByNgayDatDesc(status);
    }

    @Transactional(readOnly = true)
    public DonHang getOrderById(Integer id) {
        return donHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<ChiTietDonHang> getOrderItems(Integer orderId) {
        DonHang donHang = getOrderById(orderId);
        return chiTietDonHangRepository.findByDonHang(donHang);
    }

    @Transactional(readOnly = true)
    public List<DonHang> getOrdersByAccount(TaiKhoan taiKhoan) {
        return donHangRepository.findByTaiKhoanOrderByNgayDatDesc(taiKhoan);
    }

    @Transactional
    public void updateOrderStatus(Integer orderId, String newStatus) {
        DonHang donHang = getOrderById(orderId);
        
        // Không được chỉnh sửa đơn đã hoàn thành (DELIVERED) hoặc đã hủy (CANCELLED)
        if ("DELIVERED".equals(donHang.getTrangThai()) || "CANCELLED".equals(donHang.getTrangThai())) {
            throw new RuntimeException("Không thể cập nhật trạng thái cho đơn hàng đã hoàn thành hoặc đã hủy.");
        }
        
        // Nếu chuyển sang trạng thái CANCELLED từ updateOrderStatus (thường bởi admin)
        if ("CANCELLED".equals(newStatus)) {
            restoreStock(donHang);
        }
        
        donHang.setTrangThai(newStatus);
        donHang.setNgayCapNhat(Instant.now());
        donHangRepository.save(donHang);
    }

    @Transactional
    public void cancelOrder(Integer orderId, String reason, boolean isAdmin) {
        DonHang donHang = getOrderById(orderId);
        String currentStatus = donHang.getTrangThai();

        if ("CANCELLED".equals(currentStatus)) {
            throw new RuntimeException("Đơn hàng đã được hủy trước đó.");
        }
        if ("DELIVERED".equals(currentStatus)) {
            throw new RuntimeException("Không thể hủy đơn hàng đã giao thành công.");
        }

        if (!isAdmin) {
            // Khách hàng chỉ được hủy khi PENDING hoặc CONFIRMED
            if (!"PENDING".equals(currentStatus) && !"CONFIRMED".equals(currentStatus)) {
                throw new RuntimeException("Bạn không thể hủy đơn hàng ở trạng thái: " + currentStatus);
            }
        }
        // Admin được hủy mọi trạng thái trừ DELIVERED (đã check ở trên)

        donHang.setTrangThai("CANCELLED");
        donHang.setLyDoHuy(reason);
        donHang.setNgayCapNhat(Instant.now());
        
        restoreStock(donHang);
        
        donHangRepository.save(donHang);
    }

    private void restoreStock(DonHang donHang) {
        List<ChiTietDonHang> items = chiTietDonHangRepository.findByDonHang(donHang);
        for (ChiTietDonHang item : items) {
            BienTheSanPham bt = item.getBienTheSanPham();
            if (bt != null) {
                bt.setSoLuongTon(bt.getSoLuongTon() + item.getSoLuong());
                bienTheSanPhamRepository.save(bt);
            }
        }
    }

    @Transactional
    public void updateOrderAddress(Integer orderId, String hoTen, String soDienThoai, String diaChi) {
        DonHang donHang = getOrderById(orderId);
        String status = donHang.getTrangThai();

        if (!"PENDING".equals(status) && !"CONFIRMED".equals(status)) {
            throw new RuntimeException("Không thể thay đổi địa chỉ cho đơn hàng ở trạng thái: " + status);
        }

        donHang.setHoTenNhan(hoTen);
        donHang.setSoDienThoaiNhan(soDienThoai);
        donHang.setDiaChiNhan(diaChi);
        donHang.setNgayCapNhat(Instant.now());

        donHangRepository.save(donHang);
    }

    @Transactional
    public DonHang createOrder(String hoTen, String soDienThoai, String email, String diaChi, String ghiChu, String paymentMethod, HttpSession session) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        TaiKhoan taiKhoan = null;
        if (loginUser != null) {
            taiKhoan = taiKhoanRepository.findById(loginUser.getId()).orElse(null);
        }

        String sessionId = session.getId();
        GioHang gioHang = loginUser != null ? 
                gioHangRepository.findByTaiKhoan(taiKhoan).orElseThrow(() -> new RuntimeException("Giỏ hàng trống")) :
                gioHangRepository.findBySessionId(sessionId).orElseThrow(() -> new RuntimeException("Giỏ hàng trống"));

        if (gioHang.getChiTiets().isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        DonHang donHang = new DonHang();
        donHang.setMaDonHang("DH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        donHang.setTaiKhoan(taiKhoan);
        donHang.setHoTenNhan(hoTen);
        donHang.setSoDienThoaiNhan(soDienThoai);
        donHang.setEmailNhan(email);
        donHang.setDiaChiNhan(diaChi);
        donHang.setGhiChu(ghiChu);
        donHang.setPhuongThucThanhToan(paymentMethod);
        
        // Nếu chọn VietQR thì để trạng thái là PENDING_PAYMENT hoặc PENDING
        if ("VIETQR".equals(paymentMethod)) {
            donHang.setTrangThai("PENDING"); // Hoặc một trạng thái khác tùy quy trình
        } else {
            donHang.setTrangThai("PENDING");
        }
        
        donHang.setNgayDat(Instant.now());
        donHang.setPhiVanChuyen(BigDecimal.ZERO); // Mặc định freeship
        
        BigDecimal tamTinh = BigDecimal.ZERO;
        for (ChiTietGioHang item : gioHang.getChiTiets()) {
            tamTinh = tamTinh.add(item.getDonGia().multiply(new BigDecimal(item.getSoLuong())));
        }
        donHang.setTamTinh(tamTinh);

        // Áp dụng voucher từ session nếu có
        BigDecimal giamGiaAmount = BigDecimal.ZERO;
        MaGiamGia appliedVoucher = null;
        String voucherCode = (String) session.getAttribute("APPLIED_VOUCHER_CODE");
        
        if (voucherCode != null) {
            var voucherOpt = voucherService.validateVoucher(voucherCode, tamTinh);
            if (voucherOpt.isPresent()) {
                appliedVoucher = voucherOpt.get();
                giamGiaAmount = voucherService.calculateDiscount(appliedVoucher, tamTinh);
            }
        }
        
        donHang.setGiamGia(giamGiaAmount);
        donHang.setMaGiamGia(appliedVoucher);
        donHang.setTongTien(tamTinh.subtract(giamGiaAmount).add(donHang.getPhiVanChuyen()));

        donHang = donHangRepository.save(donHang);

        // Nếu có voucher, lưu lịch sử sử dụng và cập nhật số lượng
        if (appliedVoucher != null) {
            appliedVoucher.setSoLuongDaDung(appliedVoucher.getSoLuongDaDung() + 1);
            maGiamGiaRepository.save(appliedVoucher);
            
            LichSuSuDungMaGiamGia lichSu = new LichSuSuDungMaGiamGia();
            lichSu.setDonHang(donHang);
            lichSu.setTaiKhoan(taiKhoan);
            lichSu.setMaGiamGia(appliedVoucher);
            lichSu.setThoiGianSuDung(Instant.now());
            lichSuSuDungMaGiamGiaRepository.save(lichSu);
        }

        for (ChiTietGioHang item : gioHang.getChiTiets()) {
            BienTheSanPham bt = item.getBienTheSanPham();
            
            // Kiểm tra tồn kho lần cuối
            if (bt.getSoLuongTon() < item.getSoLuong()) {
                throw new RuntimeException("Sản phẩm " + bt.getSanPham().getTen() + " không đủ số lượng trong kho");
            }

            ChiTietDonHang ctdh = new ChiTietDonHang();
            ctdh.setDonHang(donHang);
            ctdh.setBienTheSanPham(bt);
            ctdh.setTenSanPham(bt.getSanPham().getTen());
            ctdh.setMauSac(bt.getMauSac());
            ctdh.setKichCo(bt.getKichCo());
            ctdh.setSoLuong(item.getSoLuong());
            ctdh.setDonGia(item.getDonGia());
            ctdh.setThanhTien(item.getDonGia().multiply(new BigDecimal(item.getSoLuong())));
            
            chiTietDonHangRepository.save(ctdh);

            // Trừ tồn kho
            bt.setSoLuongTon(bt.getSoLuongTon() - item.getSoLuong());
            bienTheSanPhamRepository.save(bt);
        }
        
        // Xóa giỏ hàng sau khi đặt thành công
        gioHangRepository.delete(gioHang);

        // Xóa thông tin voucher khỏi session
        session.removeAttribute("APPLIED_VOUCHER_CODE");
        session.removeAttribute("DISCOUNT_AMOUNT");

        return donHang;
    }
}
