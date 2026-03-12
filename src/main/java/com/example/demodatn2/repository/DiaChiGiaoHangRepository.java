package com.example.demodatn2.repository;

import com.example.demodatn2.entity.DiaChiGiaoHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiaChiGiaoHangRepository extends JpaRepository<DiaChiGiaoHang, Integer> {
    List<DiaChiGiaoHang> findByTaiKhoanIdOrderByLaMacDinhDescNgayTaoDesc(Integer taiKhoanId);
}
