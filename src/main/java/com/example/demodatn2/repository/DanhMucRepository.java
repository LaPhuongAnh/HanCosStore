package com.example.demodatn2.repository;

import com.example.demodatn2.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
   Page<DanhMuc> findByDanhMucChaIsNull(Pageable pageable);

    long countByDanhMucChaIsNull();
    long countByDanhMucChaIsNotNull();
    long countByTrangThai(String trangThai);

    @Query("SELECT DISTINCT d FROM DanhMuc d LEFT JOIN d.danhMucCon c " +
           "WHERE d.danhMucCha IS NULL AND " +
           "(LOWER(d.ten) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "OR LOWER(d.ma) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "OR LOWER(c.ten) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "OR LOWER(c.ma) LIKE LOWER(CONCAT('%', :kw, '%')))")
    Page<DanhMuc> searchParents(@Param("kw") String keyword, Pageable pageable);
}
