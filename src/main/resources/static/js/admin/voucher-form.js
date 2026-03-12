function toggleGiaTriToiDa() {
        const loai = document.getElementById('loaiGiam').value;
        const input = document.getElementById('giaTriToiDa');
        const star = document.getElementById('giaTriToiDaStar');
        if (loai === 'PERCENT') {
            input.required = true;
            star.style.display = 'inline';
        } else {
            input.required = false;
            star.style.display = 'none';
        }
    }
    document.addEventListener('DOMContentLoaded', toggleGiaTriToiDa);

    document.querySelector('form').addEventListener('submit', function(e) {
        const giaTriToiDa = parseFloat(document.getElementById('giaTriToiDa').value);
        const donToiThieu = parseFloat(document.getElementById('donToiThieu').value);
        if (giaTriToiDa && donToiThieu && giaTriToiDa > donToiThieu * 0.3) {
            e.preventDefault();
            alert('Giá trị giảm tối đa không được vượt quá 30% đơn tối thiểu (' + Math.floor(donToiThieu * 0.3) + '₫).');
        }
    });
