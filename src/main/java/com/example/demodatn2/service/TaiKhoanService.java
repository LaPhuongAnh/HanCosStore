package com.example.demodatn2.service;

import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.entity.TaiKhoan;
import com.example.demodatn2.entity.VaiTro;
import com.example.demodatn2.repository.TaiKhoanRepository;
import com.example.demodatn2.repository.VaiTroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaiKhoanService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;

    @Transactional(readOnly = true)
    public List<TaiKhoanDTO> getAllTaiKhoans() {
        return searchTaiKhoans(null, null);
    }

    @Transactional(readOnly = true)
    public List<TaiKhoanDTO> searchTaiKhoans(String keyword, String trangThai) {
        List<TaiKhoan> users = taiKhoanRepository.search(
                (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null,
                (trangThai != null && !trangThai.trim().isEmpty()) ? trangThai.trim() : null
        );
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaiKhoanDTO getTaiKhoanById(Integer id) {
        TaiKhoan taiKhoan = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với ID: " + id));
        return convertToDTO(taiKhoan);
    }

    @Transactional
    public void updateTaiKhoan(Integer id, TaiKhoanDTO dto) {
        TaiKhoan taiKhoan = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với ID: " + id));
        
        taiKhoan.setHoTen(dto.getHoTen());
        taiKhoan.setEmail(dto.getEmail());
        taiKhoan.setSoDienThoai(dto.getSoDienThoai());
        taiKhoan.setTrangThai(dto.getTrangThai());

        // Cập nhật vai trò
        if (dto.getVaiTroIds() != null) {
            List<VaiTro> roles = vaiTroRepository.findAllById(dto.getVaiTroIds());
            taiKhoan.setVaiTros(new HashSet<>(roles));
        } else {
            taiKhoan.setVaiTros(new HashSet<>());
        }
        
        taiKhoanRepository.save(taiKhoan);
    }

    @Transactional
    public void deleteTaiKhoan(Integer id) {
        TaiKhoan taiKhoan = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với ID: " + id));
        // Xóa mềm bằng cách set trạng thái DELETED hoặc xóa hẳn tùy yêu cầu. 
        // Ở đây tôi sẽ xóa hẳn vì admin có quyền xóa.
        taiKhoanRepository.delete(taiKhoan);
    }

    private TaiKhoanDTO convertToDTO(TaiKhoan taiKhoan) {
        return TaiKhoanDTO.builder()
                .id(taiKhoan.getId())
                .tenDangNhap(taiKhoan.getTenDangNhap())
                .hoTen(taiKhoan.getHoTen())
                .email(taiKhoan.getEmail())
                .soDienThoai(taiKhoan.getSoDienThoai())
                .trangThai(taiKhoan.getTrangThai())
                .ngayTao(taiKhoan.getNgayTao())
                .vaiTroIds(taiKhoan.getVaiTros().stream().map(VaiTro::getId).collect(Collectors.toList()))
                .build();
    }
}
