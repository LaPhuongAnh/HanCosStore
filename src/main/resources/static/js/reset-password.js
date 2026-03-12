const form = document.getElementById('resetForm');
const alertBox = document.getElementById('alertBox');

form.addEventListener('submit', async (e) => {
    e.preventDefault();
    alertBox.classList.add('d-none');

    const token = document.getElementById('token').value;
    const newPassword = form.querySelector('input[name="newPassword"]').value;

    try {
        const res = await fetch('/reset-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ token, newPassword })
        });

        if (!res.ok) {
            const data = await res.json().catch(() => ({}));
            throw new Error(data.message || 'Token không hợp lệ hoặc đã hết hạn.');
        }

        const data = await res.json();
        alertBox.className = 'alert alert-success';
        alertBox.textContent = data.message || 'Đặt lại mật khẩu thành công.';
        alertBox.classList.remove('d-none');
    } catch (err) {
        alertBox.className = 'alert alert-danger';
        alertBox.textContent = err.message || 'Có lỗi xảy ra. Vui lòng thử lại.';
        alertBox.classList.remove('d-none');
    }
});
