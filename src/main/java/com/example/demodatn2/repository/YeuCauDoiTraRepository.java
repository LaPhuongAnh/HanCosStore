package com.example.demodatn2.repository;

import com.example.demodatn2.entity.YeuCauDoiTra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface YeuCauDoiTraRepository extends JpaRepository<YeuCauDoiTra, Integer> {
    Optional<YeuCauDoiTra> findByDonHangId(Integer donHangId);
    List<YeuCauDoiTra> findAllByOrderByNgayTaoDesc();
}
