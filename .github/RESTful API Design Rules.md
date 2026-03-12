2.1 REST Principles

URL dùng danh từ

/api/v1/tasks
/api/v1/tasks/{id}


Không dùng động từ trong URL

2.2 HTTP Method Rules
Method	Ý nghĩa
GET	Lấy dữ liệu
POST	Tạo mới
PUT	Cập nhật toàn bộ
PATCH	Cập nhật một phần
DELETE	Xóa
2.3 API Versioning

BẮT BUỘC version trong URL

/api/v1/...

2.4 Response Standard (Bắt buộc)

Thành công:

{
  "success": true,
  "data": {},
  "message": "Xử lý thành công"
}


Lỗi:

{
  "success": false,
  "errorCode": "TASK_NOT_FOUND",
  "message": "Không tìm thấy công việc"
}

2.5 HTTP Status Code

200: Thành công

201: Tạo mới

400: Dữ liệu không hợp lệ

401 / 403: Auth / Permission

404: Không tìm thấy

500: Lỗi hệ thống

Ghi chú: Đã đọc.