package com.example.demodatn2.service;

import com.example.demodatn2.entity.MaGiamGia;
import com.example.demodatn2.repository.MaGiamGiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final MaGiamGiaRepository voucherRepository;

    public List<MaGiamGia> getAll() {
        return voucherRepository.findAll();
    }

    public Optional<MaGiamGia> getById(Integer id) {
        return voucherRepository.findById(id);
    }

    @Transactional
    public MaGiamGia save(MaGiamGia voucher) {
        normalizeVoucher(voucher);
        validateVoucherData(voucher);

        if (voucher.getSoLuongDaDung() == null) {
            voucher.setSoLuongDaDung(0);
        }
        return voucherRepository.save(voucher);
    }

    @Transactional
    public void delete(Integer id) {
        voucherRepository.deleteById(id);
    }

    public List<MaGiamGia> getAvailableVouchers() {
        return voucherRepository.findAvailableVouchers();
    }

    public Optional<MaGiamGia> validateVoucher(String code, BigDecimal orderAmount) {
        if (code == null || orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        String normalizedCode = code.trim().toUpperCase();
        if (normalizedCode.isEmpty()) {
            return Optional.empty();
        }

        Optional<MaGiamGia> voucherOpt = voucherRepository.findValidVoucher(normalizedCode);
        if (voucherOpt.isEmpty()) {
            return Optional.empty();
        }

        MaGiamGia voucher = voucherOpt.get();

        if (voucher.getSoLuongToiDa() != null) {
            int daDung = voucher.getSoLuongDaDung() == null ? 0 : voucher.getSoLuongDaDung();
            if (daDung >= voucher.getSoLuongToiDa()) {
                return Optional.empty();
            }
        }

        if (voucher.getDonToiThieu() != null && orderAmount.compareTo(voucher.getDonToiThieu()) < 0) {
            return Optional.empty();
        }

        if (voucher.getGiaTri() == null || voucher.getGiaTri().compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        return Optional.of(voucher);
    }

    public BigDecimal calculateDiscount(MaGiamGia voucher, BigDecimal orderAmount) {
        if (voucher == null || orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;

        if ("PERCENT".equals(voucher.getLoai())) {
            BigDecimal percent = voucher.getGiaTri();
            if (percent == null || percent.compareTo(BigDecimal.ZERO) <= 0) {
                return BigDecimal.ZERO;
            }
            if (percent.compareTo(ONE_HUNDRED) > 0) {
                percent = ONE_HUNDRED;
            }
            discount = orderAmount.multiply(percent.movePointLeft(2));
            if (voucher.getGiaTriToiDa() != null && discount.compareTo(voucher.getGiaTriToiDa()) > 0) {
                discount = voucher.getGiaTriToiDa();
            }
        } else if ("FIXED".equals(voucher.getLoai())) {
            discount = voucher.getGiaTri() != null ? voucher.getGiaTri() : BigDecimal.ZERO;
        }

        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            discount = BigDecimal.ZERO;
        }

        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return discount;
    }

    private void normalizeVoucher(MaGiamGia voucher) {
        if (voucher.getMa() != null) {
            voucher.setMa(voucher.getMa().trim().toUpperCase());
        }
        if (voucher.getLoai() != null) {
            voucher.setLoai(voucher.getLoai().trim().toUpperCase());
        }
        if (voucher.getTrangThai() == null || voucher.getTrangThai().trim().isEmpty()) {
            voucher.setTrangThai("ACTIVE");
        } else {
            voucher.setTrangThai(voucher.getTrangThai().trim().toUpperCase());
        }
    }

    private void validateVoucherData(MaGiamGia voucher) {
        if (voucher.getMa() == null || voucher.getMa().isEmpty()) {
            throw new IllegalArgumentException("Ma voucher khong duoc de trong.");
        }
        if (voucher.getLoai() == null || (!"PERCENT".equals(voucher.getLoai()) && !"FIXED".equals(voucher.getLoai()))) {
            throw new IllegalArgumentException("Loai voucher phai la PERCENT hoac FIXED.");
        }
        if (voucher.getGiaTri() == null || voucher.getGiaTri().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Gia tri giam phai lon hon 0.");
        }
        if ("PERCENT".equals(voucher.getLoai()) && voucher.getGiaTri().compareTo(ONE_HUNDRED) > 0) {
            throw new IllegalArgumentException("Voucher phan tram khong duoc vuot qua 100%.");
        }
        if (voucher.getGiaTriToiDa() != null && voucher.getGiaTriToiDa().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Gia tri giam toi da khong hop le.");
        }
        if (voucher.getDonToiThieu() != null && voucher.getDonToiThieu().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Don toi thieu khong hop le.");
        }
        if (voucher.getSoLuongDaDung() != null && voucher.getSoLuongDaDung() < 0) {
            throw new IllegalArgumentException("So luong da dung khong hop le.");
        }
        if (voucher.getSoLuongToiDa() != null && voucher.getSoLuongToiDa() < 0) {
            throw new IllegalArgumentException("So luong toi da khong hop le.");
        }
        if (voucher.getSoLuongToiDa() != null && voucher.getSoLuongDaDung() != null
                && voucher.getSoLuongDaDung() > voucher.getSoLuongToiDa()) {
            throw new IllegalArgumentException("So luong da dung khong duoc lon hon so luong toi da.");
        }
        if (voucher.getBatDauLuc() == null || voucher.getKetThucLuc() == null) {
            throw new IllegalArgumentException("Thoi gian bat dau/ket thuc khong duoc de trong.");
        }
        if (!voucher.getKetThucLuc().isAfter(voucher.getBatDauLuc())) {
            throw new IllegalArgumentException("Thoi gian ket thuc phai sau thoi gian bat dau.");
        }
    }
}
