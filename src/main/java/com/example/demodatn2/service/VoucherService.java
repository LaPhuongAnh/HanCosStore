package com.example.demodatn2.service;

import com.example.demodatn2.entity.MaGiamGia;
import com.example.demodatn2.repository.MaGiamGiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final MaGiamGiaRepository voucherRepository;

    public List<MaGiamGia> getAll() {
        return voucherRepository.findAll();
    }

    public Optional<MaGiamGia> getById(Integer id) {
        return voucherRepository.findById(id);
    }

    @Transactional
    public MaGiamGia save(MaGiamGia voucher) {
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
        Optional<MaGiamGia> voucherOpt = voucherRepository.findValidVoucher(code);
        
        if (voucherOpt.isPresent()) {
            MaGiamGia voucher = voucherOpt.get();
            
            // Kiểm tra số lượng
            if (voucher.getSoLuongToiDa() != null && voucher.getSoLuongDaDung() >= voucher.getSoLuongToiDa()) {
                return Optional.empty();
            }
            
            // Kiểm tra giá trị đơn tối thiểu
            if (voucher.getDonToiThieu() != null && orderAmount.compareTo(voucher.getDonToiThieu()) < 0) {
                return Optional.empty();
            }
            
            return Optional.of(voucher);
        }
        
        return Optional.empty();
    }

    public BigDecimal calculateDiscount(MaGiamGia voucher, BigDecimal orderAmount) {
        BigDecimal discount = BigDecimal.ZERO;
        
        if ("PERCENT".equals(voucher.getLoai())) {
            discount = orderAmount.multiply(voucher.getGiaTri().divide(new BigDecimal(100)));
            if (voucher.getGiaTriToiDa() != null && discount.compareTo(voucher.getGiaTriToiDa()) > 0) {
                discount = voucher.getGiaTriToiDa();
            }
        } else if ("FIXED".equals(voucher.getLoai())) {
            discount = voucher.getGiaTri();
        }
        
        // Không để tiền giảm lớn hơn tiền đơn hàng
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }
        
        return discount;
    }
}
