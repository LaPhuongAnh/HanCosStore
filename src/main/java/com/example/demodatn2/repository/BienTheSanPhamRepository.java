package com.example.demodatn2.repository;

import com.example.demodatn2.entity.BienTheSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BienTheSanPhamRepository extends JpaRepository<BienTheSanPham, Integer> {
    List<BienTheSanPham> findBySanPham_Id(Integer SanPhamId);

    List<BienTheSanPham> findBySanPham_IdAndMauSac(Integer idSanPham, String mauSac);

    Optional<BienTheSanPham> findByMaSKU(String maSKU);

    Optional<BienTheSanPham> findBySanPham_IdAndMauSacAndKichCo(Integer idSanPham, String mauSac, String kichCo);

    interface PriceRange{
        BigDecimal getMinGia();
        BigDecimal getMaxGia();
    }

    @Query("""
        select min(v.gia) as minGia, max(v.gia) as maxGia
        from BienTheSanPham v
        where v.sanPham.id = ?1
          and (v.trangThai is null or lower(v.trangThai) = 'active')
    """)
    PriceRange findPriceRange(Integer sanPhamId);
    @Query("""
        select distinct v.mauSac
        from BienTheSanPham v
        where v.sanPham.id = ?1
          and (v.trangThai is null or lower(v.trangThai) = 'active')
        order by v.mauSac
    """)
    List<String> findDistinctMauSac(Integer sanPhamId);

        @Query("""
                select distinct v.kichCo
                from BienTheSanPham v
                where v.sanPham.id = ?1
                    and (v.trangThai is null or lower(v.trangThai) = 'active')
                order by v.kichCo
        """)
        List<String> findDistinctKichCo(Integer sanPhamId);

}
