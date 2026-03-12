package com.example.demodatn2.service;

import com.example.demodatn2.dto.DanhMucDTO;
import com.example.demodatn2.entity.DanhMuc;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
// Contract cho nghiệp vụ danh mục: CRUD, thống kê và tìm kiếm có phân trang.
public interface    DanhMucService {
    List<DanhMuc> getAll();
    List<DanhMucDTO> getAllDTOs();
    List<DanhMuc> getActive();
    List<DanhMuc> getParents();
    Optional<DanhMuc> getById(Integer id);
    Optional<DanhMuc> getByMa(String ma);
    List<DanhMuc> getByParentId(Integer parentId);
    DanhMuc save(DanhMuc danhMuc);
    void saveDTO(DanhMucDTO dto);
    void deleteById(Integer id);
 Page<DanhMuc> findByDanhMucChaIsNull(int page, int size);
    long countAll();
    long countParents();
    long countChildren();
    long countActive();
    Page<DanhMuc> searchParents(String keyword, int page, int size);
}
