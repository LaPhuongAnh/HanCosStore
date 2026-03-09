3.1 Cấu hình hệ thống

Sử dụng:

application.yml

Profiles: dev, test, prod

Không hard-code:

Password

Connection string

Secret key

3.2 Logging

Dùng SLF4J / Logback

Log phải:

Rõ ràng

Bằng tiếng Việt

Log level:

INFO: nghiệp vụ

ERROR: exception

3.3 Build & CI/CD

Project phải:

Build được bằng Maven

Không phụ thuộc môi trường local

Không commit:

target/

File config nhạy cảm

4️⃣ Quy tắc khi Copilot sinh code

Không sinh code nếu:

Vi phạm Detailed Design

Nếu thiếu thông tin:

Giả định hợp lý

Ghi chú rõ trong comment

Ưu tiên:

Dễ đọc > Code phức tạp

Bảo trì > Tối ưu sớm

Ghi chú: Đã đọc.