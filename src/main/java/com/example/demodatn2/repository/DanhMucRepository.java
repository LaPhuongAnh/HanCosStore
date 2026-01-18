package com.example.demodatn2.repository;

import com.example.demodatn2.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DanhMucRepository extends JpaRepository<DanhMuc, Integer> {
    Optional<DanhMuc> findByMa(String ma);
     List<DanhMuc> findByDanhMucCha_Id(Integer parentId);

     List<DanhMuc> findByTrangThai(String trangThai);

    // ✅ lấy tất cả danh mục CHA (cha = null)
    List<DanhMuc> findByDanhMucChaIsNullAndTrangThai(String trangThai);

    // ✅ lấy tất cả danh mục CON theo mã cha
    List<DanhMuc> findByDanhMucCha_MaAndTrangThai(String maCha, String trangThai);

    // (tuỳ chọn) lấy tất cả danh mục CON theo tên cha
    List<DanhMuc> findByDanhMucCha_TenAndTrangThai(String tenCha, String trangThai);
    List<DanhMuc> findByDanhMucCha_TenIgnoreCaseAndTrangThai(String parentTen, String trangThai);

    // Lấy con theo MÃ cha

}
