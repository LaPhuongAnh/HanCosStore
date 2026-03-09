1.1 Kiến trúc bắt buộc

Tuân thủ mô hình:

Controller → Service → Repository


Không viết business logic trong Controller

Mỗi layer chỉ đúng vai trò của nó

Áp dụng SOLID principles

1.2 Coding Convention

Java:

Class: PascalCase

Method/Variable: camelCase

Tên phải phản ánh đúng nghiệp vụ Task Management

Luôn thêm comment tiếng Việt cho:

Logic phức tạp

Nghiệp vụ quan trọng

1.3 Service Layer

Mọi nghiệp vụ xử lý phải nằm trong Service

Dùng @Transactional cho:

Tạo / cập nhật / xóa task

Không gọi Repository trực tiếp từ Controller

1.4 Exception & Validation

Dùng @ControllerAdvice cho Global Exception

Không dùng Exception chung chung

Tạo exception theo nghiệp vụ:

TaskNotFoundException

InvalidTaskStatusException

Validation:

Dùng @Valid, @NotNull, @NotBlank

Không validate thủ công bằng if-else trong controller

1.5 Database – SQL Server

Mapping bằng JPA/Hibernate

Không dùng native query nếu không thật sự cần

Không thay đổi:

Tên bảng

Tên cột

Transaction phải đảm bảo rollback khi lỗi

Ghi chú: Đã đọc.