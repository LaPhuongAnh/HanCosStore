function deleteVoucher(id) {
    Swal.fire({
        title: 'Xác nhận xóa?',
        text: "Voucher này sẽ bị xóa vĩnh viễn!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Xóa ngay',
        cancelButtonText: 'Hủy'
    }).then((result) => {
        if (result.isConfirmed) {
            fetch('/admin/vouchers/' + id, {
                method: 'DELETE',
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            }).then(res => res.text()).then(data => {
                if (data === 'SUCCESS') {
                    Swal.fire('Đã xóa!', 'Voucher đã được xóa thành công.', 'success')
                        .then(() => location.reload());
                } else {
                    Swal.fire('Lỗi!', 'Không thể xóa voucher: ' + data, 'error');
                }
            });
        }
    });
}
