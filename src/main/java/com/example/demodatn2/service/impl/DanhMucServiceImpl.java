package com.example.demodatn2.service.impl;

import com.example.demodatn2.dto.DanhMucDTO;
import com.example.demodatn2.entity.DanhMuc;
import com.example.demodatn2.repository.DanhMucRepository;
import com.example.demodatn2.service.DanhMucService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
@Service
@RequiredArgsConstructor
// Triển khai nghiệp vụ danh mục: thao tác CRUD, map DTO và truy vấn danh mục cha/con.
public class DanhMucServiceImpl implements DanhMucService {

    private final DanhMucRepository danhMucRepository;

    @Override
    public List<DanhMuc> getAll() {
        return danhMucRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DanhMucDTO> getAllDTOs() {
        List<DanhMuc> all = danhMucRepository.findAll();
        // Lọc ra các danh mục cha
        return all.stream()
                .filter(dm -> dm.getDanhMucCha() == null)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DanhMucDTO convertToDTO(DanhMuc dm) {
        return DanhMucDTO.builder()
                .id(dm.getId())
                .ma(dm.getMa())
                .ten(dm.getTen())
                .trangThai(dm.getTrangThai())
                .ngayTao(dm.getNgayTao())
                .danhMucChaId(dm.getDanhMucCha() != null ? dm.getDanhMucCha().getId() : null)
                .tenDanhMucCha(dm.getDanhMucCha() != null ? dm.getDanhMucCha().getTen() : null)
                .danhMucCon(dm.getDanhMucCon() != null ? 
                        dm.getDanhMucCon().stream().map(this::convertToDTO).collect(Collectors.toList()) : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DanhMuc> getActive() {
        List<DanhMuc> categories = danhMucRepository.findByTrangThai("ACTIVE");
        // Force load children for each category to avoid LazyInitializationException in view
        categories.forEach(dm -> dm.getDanhMucCon().size());
        return categories;
    }

    @Override
    public List<DanhMuc> getParents() {
        return danhMucRepository.findByDanhMucChaIsNullAndTrangThai("ACTIVE");
    }

    @Override
    public Optional<DanhMuc> getById(Integer id) {
        return danhMucRepository.findById(id);
    }

    @Override
    public Optional<DanhMuc> getByMa(String ma) {
        return danhMucRepository.findByMa(ma);
    }

    @Override
    public List<DanhMuc> getByParentId(Integer parentId) {
        return danhMucRepository.findByDanhMucCha_Id(parentId);
    }

    @Override
    @Transactional
    public DanhMuc save(DanhMuc danhMuc) {
        if (danhMuc.getId() == null) {
            danhMuc.setNgayTao(LocalDateTime.now());
        }
        return danhMucRepository.save(danhMuc);
    }

    @Override
    @Transactional
    public void saveDTO(DanhMucDTO dto) {
        DanhMuc danhMuc;
        if (dto.getId() != null) {
            danhMuc = danhMucRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại: " + dto.getId()));
        } else {
            danhMuc = new DanhMuc();
            danhMuc.setNgayTao(LocalDateTime.now());
        }

        danhMuc.setMa(dto.getMa());
        danhMuc.setTen(dto.getTen());
        danhMuc.setTrangThai(dto.getTrangThai() != null ? dto.getTrangThai() : "ACTIVE");

        if (dto.getDanhMucChaId() != null) {
            DanhMuc cha = danhMucRepository.findById(dto.getDanhMucChaId())
                    .orElseThrow(() -> new RuntimeException("Danh mục cha không tồn tại: " + dto.getDanhMucChaId()));
            danhMuc.setDanhMucCha(cha);
        } else {
            danhMuc.setDanhMucCha(null);
        }

        danhMucRepository.save(danhMuc);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        danhMucRepository.deleteById(id);
    }
    @Override
@Transactional(readOnly = true)
public Page<DanhMuc> findByDanhMucChaIsNull(int page, int size) {

    Pageable pageable = PageRequest.of(page, size);

    Page<DanhMuc> categoryPage = danhMucRepository.findByDanhMucChaIsNull(pageable);

    // load danh mục con
    categoryPage.getContent().forEach(dm -> dm.getDanhMucCon().size());

    return categoryPage;
}

    @Override
    public long countAll() { return danhMucRepository.count(); }

    @Override
    public long countParents() { return danhMucRepository.countByDanhMucChaIsNull(); }

    @Override
    public long countChildren() { return danhMucRepository.countByDanhMucChaIsNotNull(); }

    @Override
    public long countActive() { return danhMucRepository.countByTrangThai("ACTIVE"); }

    @Override
    @Transactional(readOnly = true)
    public Page<DanhMuc> searchParents(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DanhMuc> categoryPage = danhMucRepository.searchParents(keyword, pageable);
        categoryPage.getContent().forEach(dm -> dm.getDanhMucCon().size());
        return categoryPage;
    }
}