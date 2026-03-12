const form = document.getElementById('forgotForm');
const alertBox = document.getElementById('alertBox');

form.addEventListener('submit', async (e) => {
    e.preventDefault();
    alertBox.classList.add('d-none');

    const formData = new FormData(form);
    const email = formData.get('email');

    try {
        const res = await fetch('/forgot-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email })
        });

        const data = await res.json();
        alertBox.className = 'alert alert-success';
        alertBox.textContent = data.message || 'Nếu email tồn tại, hệ thống đã gửi link đặt lại mật khẩu.';
        alertBox.classList.remove('d-none');
        form.reset();
    } catch (err) {
        alertBox.className = 'alert alert-danger';
        alertBox.textContent = 'Có lỗi xảy ra. Vui lòng thử lại.';
        alertBox.classList.remove('d-none');
    }
});
