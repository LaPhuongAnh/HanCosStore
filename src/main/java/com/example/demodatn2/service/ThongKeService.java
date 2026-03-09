package com.example.demodatn2.service;

import com.example.demodatn2.dto.DoanhThuDTO;
import com.example.demodatn2.entity.DonHang;
import com.example.demodatn2.repository.ChiTietDonHangRepository;
import com.example.demodatn2.repository.DonHangRepository;
import com.example.demodatn2.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ThongKeService {

    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    public DoanhThuDTO getDoanhThuTongHop() {
        BigDecimal tongDoanhThu = donHangRepository.sumTongDoanhThu();
        Long soDonHang = donHangRepository.countDonHangThanhCong();
        Long soSPDaBan = chiTietDonHangRepository.sumSoLuongDaBan();
        Long soKhachHang = taiKhoanRepository.countCustomers();

        // Lấy doanh thu hôm nay
        Instant dauNgay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        BigDecimal doanhThuHomNay = donHangRepository.sumDoanhThuTuNgay(dauNgay);
        Long soDonHomNay = donHangRepository.countDonHangTuNgay(dauNgay);

        return DoanhThuDTO.builder()
                .tongDoanhThu(tongDoanhThu != null ? tongDoanhThu : BigDecimal.ZERO)
                .soDonHang(soDonHang != null ? soDonHang : 0L)
                .soSanPhamDaBan(soSPDaBan != null ? soSPDaBan : 0L)
                .doanhThuHomNay(doanhThuHomNay != null ? doanhThuHomNay : BigDecimal.ZERO)
                .soDonHomNay(soDonHomNay != null ? soDonHomNay : 0L)
            .soKhachHang(soKhachHang != null ? soKhachHang : 0L)
                .build();
    }

    public List<Map<String, Object>> getDoanhThuTheoNgay(Instant tuNgay, Instant denNgay) {
        List<Object[]> results = donHangRepository.getDoanhThuTheoNgay(tuNgay, denNgay);
        List<Map<String, Object>> data = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("ngay", row[0].toString());
            map.put("doanhThu", row[1]);
            data.add(map);
        }
        return data;
    }

    public Map<String, Object> getDoanhThuTrongKhoang(Instant tuNgay, Instant denNgay) {
        BigDecimal doanhThuThucTe = donHangRepository.sumTongTienByTrangThaiInRange(
            List.of("COMPLETED"), tuNgay, denNgay);
        BigDecimal doanhThuTamTinh = donHangRepository.sumTongTienByTrangThaiInRange(
            List.of("PENDING", "PROCESSING", "SHIPPED", "DELIVERED"), tuNgay, denNgay);
        BigDecimal doanhThuThatThoat = donHangRepository.sumTongTienByTrangThaiInRange(
            List.of("CANCELLED"), tuNgay, denNgay);
        Long soDon = donHangRepository.countDonHangTrongKhoang(tuNgay, denNgay);

        Map<String, Object> stats = new HashMap<>();
        stats.put("doanhThuThucTe", doanhThuThucTe != null ? doanhThuThucTe : BigDecimal.ZERO);
//        stats.put("doanhThuTamTinh", doanhThuTamTinh != null ? doanhThuTamTinh : BigDecimal.ZERO);
//        stats.put("doanhThuThatThoat", doanhThuThatThoat != null ? doanhThuThatThoat : BigDecimal.ZERO);
        stats.put("soDon", soDon != null ? soDon : 0L);
        return stats;
    }

    public byte[] exportDoanhThuToExcel(Instant tu, Instant den) throws IOException {
        List<DonHang> orders = donHangRepository.findAllDeliveredInRange(tu, den);
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Báo cáo doanh thu");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Mã đơn hàng", "Ngày đặt", "Khách hàng", "Số điện thoại", "Phương thức thanh toán", "Tổng tiền"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Date Format Style
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));

            // Money Format Style
            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0\"₫\""));

            // Create Data Rows
            int rowIdx = 1;
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());
            BigDecimal totalRevenue = BigDecimal.ZERO;

            for (DonHang order : orders) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(order.getMaDonHang());
                
                Cell dateCell = row.createCell(1);
                dateCell.setCellValue(java.util.Date.from(order.getNgayDat()));
                dateCell.setCellStyle(dateStyle);
                
                row.createCell(2).setCellValue(order.getHoTenNhan());
                row.createCell(3).setCellValue(order.getSoDienThoaiNhan());
                row.createCell(4).setCellValue(order.getPhuongThucThanhToan());
                
                Cell amountCell = row.createCell(5);
                amountCell.setCellValue(order.getTongTien().doubleValue());
                amountCell.setCellStyle(moneyStyle);
                
                totalRevenue = totalRevenue.add(order.getTongTien());
            }

            // Total Row
            Row totalRow = sheet.createRow(rowIdx);
            Cell labelCell = totalRow.createCell(4);
            labelCell.setCellValue("TỔNG CỘNG:");
            labelCell.setCellStyle(headerStyle);
            
            Cell totalCell = totalRow.createCell(5);
            totalCell.setCellValue(totalRevenue.doubleValue());
            totalCell.setCellStyle(moneyStyle);

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
