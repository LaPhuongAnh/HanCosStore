function cancelOrder(orderId) {
    Swal.fire({
        title: 'Hủy đơn hàng',
        text: 'Vui lòng cho biết lý do hủy đơn hàng này:',
        input: 'text',
        inputPlaceholder: 'Nhập lý do tại đây...',
        showCancelButton: true,
        confirmButtonColor: '#dc3545',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Xác nhận hủy',
        cancelButtonText: 'Quay lại',
        inputValidator: (value) => {
            if (!value) {
                return 'Bạn cần nhập lý do hủy!';
            }
        }
    }).then((result) => {
        if (result.isConfirmed) {
            const reason = result.value;
            const formData = new FormData();
            formData.append('reason', reason);

            fetch('/order/cancel/' + orderId, {
                method: 'POST',
                body: formData,
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            }).then(res => res.text()).then(data => {
                if (data === 'SUCCESS') {
                    Swal.fire('Thành công', 'Đơn hàng của bạn đã được hủy.', 'success')
                        .then(() => location.reload());
                } else {
                    Swal.fire('Lỗi', data, 'error');
                }
            });
        }
    });
}

function requestReturn(orderId) {
    Swal.fire({
        title: 'Yêu cầu trả hàng',
        input: 'select',
        inputOptions: {
            'Sản phẩm lỗi / hư hỏng': 'Sản phẩm lỗi / hư hỏng',
            'Giao sai sản phẩm': 'Giao sai sản phẩm',
            'Sản phẩm không đúng mô tả': 'Sản phẩm không đúng mô tả',
            'Không vừa size': 'Không vừa size',
            'Khác': 'Khác'
        },
        inputPlaceholder: 'Chọn lý do trả hàng',
        showCancelButton: true,
        confirmButtonColor: '#fd7e14',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Gửi yêu cầu',
        cancelButtonText: 'Hủy',
        inputValidator: (value) => {
            if (!value) {
                return 'Vui lòng chọn lý do trả hàng!';
            }
        }
    }).then((result) => {
        if (result.isConfirmed) {
            const reason = result.value;
            const formData = new FormData();
            formData.append('reason', reason);

            fetch('/order/return/' + orderId, {
                method: 'POST',
                body: formData,
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            }).then(res => res.text()).then(data => {
                if (data === 'SUCCESS') {
                    Swal.fire('Thành công', 'Yêu cầu trả hàng đã được gửi. Vui lòng chờ admin xử lý.', 'success')
                        .then(() => location.reload());
                } else {
                    Swal.fire('Lỗi', data, 'error');
                }
            });
        }
    });
}
