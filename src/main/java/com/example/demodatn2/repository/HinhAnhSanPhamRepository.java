package com.example.demodatn2.repository;

import com.example.demodatn2.entity.HinhAnhSanPham;
import com.example.demodatn2.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HinhAnhSanPhamRepository extends JpaRepository<HinhAnhSanPham, Integer> {
    List<HinhAnhSanPham> findBySanPham_IdOrderByLaAnhChinhDescThuTuAscIdAsc(Integer sanPhamId);
    Optional<HinhAnhSanPham> findFirstBySanPham_IdOrderByLaAnhChinhDescThuTuAscIdAsc(Integer sanPhamId);
    @Query("""
        select ha
        from HinhAnhSanPham ha
        where ha.sanPham.id = ?1 and ha.laAnhChinh = true
    """)
    Optional<HinhAnhSanPham> findPrimaryBySanPhamId(Integer sanPhamId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM HinhAnhSanPham h WHERE h.sanPham = ?1")
    void deleteBySanPham(SanPham sanPham);
}