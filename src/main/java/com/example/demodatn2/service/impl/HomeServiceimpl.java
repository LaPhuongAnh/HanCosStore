package com.example.demodatn2.service.impl;

import com.example.demodatn2.dto.HomeProductVM;
import com.example.demodatn2.dto.ProductDetailVM;
import com.example.demodatn2.entity.DanhMuc;
import com.example.demodatn2.entity.HinhAnhMauSac;
import com.example.demodatn2.entity.HinhAnhSanPham;
import com.example.demodatn2.entity.SanPham;
import com.example.demodatn2.repository.BienTheSanPhamRepository;
import com.example.demodatn2.repository.DanhMucRepository;
import com.example.demodatn2.repository.HinhAnhSanPhamRepository;
import com.example.demodatn2.repository.SanPhamRepository;
import com.example.demodatn2.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeServiceimpl implements HomeService {
    private final SanPhamRepository sanPhamRepository;
    private final HinhAnhSanPhamRepository hinhAnhSanPhamRepository;
    private final BienTheSanPhamRepository bienTheSanPhamRepository;
    private final DanhMucRepository danhMucRepository;

    @Override
    public List<HomeProductVM> getHomeProducts(Integer danhMucId, String keyword) {
        List<SanPham> products;

        if (keyword != null && !keyword.trim().isEmpty()) {
            products = sanPhamRepository.searchActive(keyword.trim());
        } else if (danhMucId != null) {
            Optional<DanhMuc> dmOpt = danhMucRepository.findById(danhMucId);
            if (dmOpt.isPresent()) {
                DanhMuc dm = dmOpt.get();
                // Nếu là danh mục cha, lấy tất cả con
                if (dm.getDanhMucCha() == null) {
                    List<Integer> ids = new ArrayList<>();
                    ids.add(dm.getId());
                    if (dm.getDanhMucCon() != null) {
                        for (DanhMuc con : dm.getDanhMucCon()) {
                            ids.add(con.getId());
                        }
                    }
                    products = sanPhamRepository.findActiveByDanhMucIds(ids);
                } else {
                    products = sanPhamRepository.findActiveByDanhMucId(danhMucId);
                }
            } else {
                products = sanPhamRepository.findActiveForListing();
            }
        } else {
            products = sanPhamRepository.findActiveForListing();
        }

        return products.stream().map(sp -> {
           String anhChinh=hinhAnhSanPhamRepository
                   .findFirstBySanPham_IdOrderByLaAnhChinhDescThuTuAscIdAsc(sp.getId())
                   .map(HinhAnhSanPham::getDuongDanAnh)
                   .orElse(null);
           //khoanggia
           BienTheSanPhamRepository.PriceRange range=bienTheSanPhamRepository.findPriceRange(sp.getId());
           //mausac
           List<String> mauSac=bienTheSanPhamRepository.findDistinctMauSac(sp.getId());
           List<String> kichCos=bienTheSanPhamRepository.findDistinctKichCo(sp.getId());
           return HomeProductVM.builder()
                   .id(sp.getId())
                   .ten(sp.getTen())
                   .anhChinh(anhChinh)
                   .giaMin(range != null ? range.getMinGia() : null)
                   .giaMax(range != null ? range.getMaxGia() : null)
                   .mauSacs(mauSac)
               .kichCos(kichCos)
                   .maDanhMuc(sp.getDanhMuc() != null ? sp.getDanhMuc().getMa() : null)
                   .idDanhMuc(sp.getDanhMuc() != null ? sp.getDanhMuc().getId() : null)
                   .danhMuc(sp.getDanhMuc())
                   .build();

       }).toList();
    }

    @Override
    public ProductDetailVM getProductDetail(Integer id) {
        // Fetch SanPham with each collection separately to avoid MultipleBagFetchException
        SanPham sp = sanPhamRepository.findDetailWithBienTheById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // Trigger loading of other collections (already fetched via EntityGraph in these calls)
        sanPhamRepository.findDetailWithHinhAnhMauSacById(id);
        sanPhamRepository.findDetailWithHinhAnhSanPhamById(id);

        List<String> gallery = sp.getHinhAnhSanPhams().stream()
                .sorted((a, b) -> {
                    if (a.getLaAnhChinh() && !b.getLaAnhChinh()) return -1;
                    if (!a.getLaAnhChinh() && b.getLaAnhChinh()) return 1;
                    return a.getThuTu().compareTo(b.getThuTu());
                })
                .map(HinhAnhSanPham::getDuongDanAnh)
                .collect(Collectors.toList());

        Map<String, String> hinhAnhTheoMau = sp.getHinhAnhMauSacs().stream()
                .collect(Collectors.toMap(
                        HinhAnhMauSac::getMauSac,
                        HinhAnhMauSac::getDuongDanAnh,
                        (existing, replacement) -> existing
                ));

        List<ProductDetailVM.BienTheVM> bienThes = sp.getBienThes().stream()
                .filter(bt -> "ACTIVE".equals(bt.getTrangThai()))
                .map(bt -> ProductDetailVM.BienTheVM.builder()
                        .id(bt.getId())
                        .maSKU(bt.getMaSKU())
                        .mauSac(bt.getMauSac())
                        .kichCo(bt.getKichCo())
                        .gia(bt.getGia())
                        .giaGoc(bt.getGiaGoc())
                        .soLuongTon(bt.getSoLuongTon())
                        .build())
                .collect(Collectors.toList());

        return ProductDetailVM.builder()
                .id(sp.getId())
                .maSanPham(sp.getMaSanPham())
                .ten(sp.getTen())
                .moTaNgan(sp.getMoTaNgan())
                .moTa(sp.getMoTa())
                .chatLieu(sp.getChatLieu())
                .gioiTinh(sp.getGioiTinh())
                .danhMuc(sp.getDanhMuc())
                .hinhAnhGallery(gallery)
                .hinhAnhTheoMau(hinhAnhTheoMau)
                .bienThes(bienThes)
                .build();
    }

}
