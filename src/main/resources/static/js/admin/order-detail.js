function adminCancelOrder() {
    const orderId = document.body?.dataset?.orderId;
    if (!orderId) {
        return;
    }

    Swal.fire({
        title: 'Hủy đơn hàng (Admin)',
        text: 'Nhập lý do hủy đơn hàng này:',
        input: 'text',
        inputPlaceholder: 'Lý do hủy...',
        showCancelButton: true,
        confirmButtonColor: '#dc3545',
        confirmButtonText: 'Xác nhận hủy',
        cancelButtonText: 'Đóng',
        inputValidator: (value) => {
            if (!value) {
                return 'Vui lòng nhập lý do!';
            }
        }
    }).then((result) => {
        if (result.isConfirmed) {
            const reason = result.value;
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = `/admin/orders/${orderId}/cancel`;

            const reasonInput = document.createElement('input');
            reasonInput.type = 'hidden';
            reasonInput.name = 'reason';
            reasonInput.value = reason;

            form.appendChild(reasonInput);
            document.body.appendChild(form);
            form.submit();
        }
    });
}
