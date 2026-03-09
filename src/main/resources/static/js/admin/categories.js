function deleteCategory(id) {
    Swal.fire({
        title: 'Xác nhận xóa?',
        text: "Nếu xóa danh mục cha, các danh mục con cũng sẽ bị ảnh hưởng! Bạn chắc chắn muốn xóa?",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Xóa ngay',
        cancelButtonText: 'Hủy'
    }).then((result) => {
        if (result.isConfirmed) {
            fetch('/api/danh-muc/' + id, {
                method: 'DELETE',
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            }).then(res => {
                if (res.ok) {
                    Swal.fire('Đã xóa!', 'Danh mục đã được xóa thành công.', 'success')
                        .then(() => location.reload());
                } else {
                    res.text().then(text => {
                        Swal.fire('Lỗi!', text || 'Không thể xóa danh mục này. Có thể danh mục đang chứa sản phẩm.', 'error');
                    });
                }
            });
        }
    });
}
