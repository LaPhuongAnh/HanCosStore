function deleteProduct(id) {
    Swal.fire({
        title: 'Xác nhận xóa?',
        text: "Sản phẩm sẽ bị đánh dấu là đã xóa và không hiển thị trên cửa hàng!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Xóa ngay',
        cancelButtonText: 'Hủy'
    }).then((result) => {
        if (result.isConfirmed) {
            fetch('/api/san-pham/' + id, {
                method: 'DELETE',
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            }).then(res => {
                if (res.ok) {
                    Swal.fire('Đã xóa!', 'Sản phẩm đã được xóa thành công.', 'success')
                        .then(() => location.reload());
                } else {
                    Swal.fire('Lỗi!', 'Không thể xóa sản phẩm này.', 'error');
                }
            });
        }
    });
}
