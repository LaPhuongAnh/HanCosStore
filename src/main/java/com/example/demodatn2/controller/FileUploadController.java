package com.example.demodatn2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private final String UPLOAD_DIR = "src/main/resources/static/images/products/";

    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 1. Lấy tên file gốc (ví dụ: ao-khoac.jpg)
            String originalFilename = file.getOriginalFilename();
            // Xử lý an toàn: Thay khoảng trắng bằng gạch dưới (tránh lỗi URL)
            if (originalFilename != null) {
                originalFilename = originalFilename.replaceAll("\\s+", "_");
            }
            
            Path filePath = uploadPath.resolve(originalFilename);

            // 2. KIỂM TRA: Nếu file tên này đã tồn tại
            if (Files.exists(filePath)) {
                // A. So sánh xem có phải là 1 ảnh không (Dựa vào kích thước & nội dung)
                if (isSameContent(file, filePath)) {
                    // Nếu đúng là ảnh đó rồi -> Trả về đường dẫn luôn, KHÔNG LƯU MỚI
                    return ResponseEntity.ok("/images/products/" + originalFilename);
                } 
                
                // B. Nếu trùng tên nhưng nội dung khác (ảnh mới) -> Đổi tên nhẹ để tránh ghi đè
                // Ví dụ: ao-khoac.jpg -> ao-khoac_1715482.jpg
                String newName = renameFile(originalFilename);
                filePath = uploadPath.resolve(newName);
                originalFilename = newName; // Cập nhật tên mới để trả về
            }

            // 3. Lưu file
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            return ResponseEntity.ok("/images/products/" + originalFilename);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to upload: " + e.getMessage());
        }
    }

    // Hàm phụ: Kiểm tra xem file mới up lên có giống hệt file cũ không
    private boolean isSameContent(MultipartFile file, Path existingFilePath) throws IOException {
        // Cách 1: So sánh kích thước file trước (Nhanh)
        if (file.getSize() != Files.size(existingFilePath)) {
            return false; // Khác kích thước -> Chắc chắn là ảnh khác
        }
        
        // Cách 2: So sánh mã Hash MD5 (Chính xác 100%)
        String newFileHash = DigestUtils.md5DigestAsHex(file.getInputStream());
        String existingFileHash;
        try (InputStream is = Files.newInputStream(existingFilePath)) {
            existingFileHash = DigestUtils.md5DigestAsHex(is);
        }
        
        return newFileHash.equals(existingFileHash);
    }

    // Hàm phụ: Tạo tên mới nếu bị trùng (thêm timestamp)
    private String renameFile(String originalName) {
        String name = originalName;
        String ext = "";
        int dotIndex = originalName.lastIndexOf(".");
        if (dotIndex != -1) {
            name = originalName.substring(0, dotIndex);
            ext = originalName.substring(dotIndex);
        }
        // Thêm thời gian hiện tại vào sau tên để đảm bảo không trùng
        return name + "_" + System.currentTimeMillis() + ext;
    }
}