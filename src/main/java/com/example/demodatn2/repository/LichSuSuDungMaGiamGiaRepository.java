package com.example.demodatn2.repository;

import com.example.demodatn2.entity.LichSuSuDungMaGiamGia;
import com.example.demodatn2.entity.TaiKhoan;
import com.example.demodatn2.entity.MaGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LichSuSuDungMaGiamGiaRepository extends JpaRepository<LichSuSuDungMaGiamGia, Integer> {
}
