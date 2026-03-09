let myChart = null;

document.addEventListener('DOMContentLoaded', function() {
    // Mặc định load 30 ngày gần đây
    const today = new Date();
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(today.getDate() - 30);

    document.getElementById('tuNgay').value = thirtyDaysAgo.toISOString().split('T')[0];
    document.getElementById('denNgay').value = today.toISOString().split('T')[0];

    updateStatistics();
});

function resetFilter() {
    document.getElementById('tuNgay').value = '';
    document.getElementById('denNgay').value = '';
    updateStatistics();
}

function quickFilter(days) {
    const today = new Date();
    const pastDate = new Date();
    pastDate.setDate(today.getDate() - days);

    const formatYMD = (date) => {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    };

    document.getElementById('tuNgay').value = formatYMD(pastDate);
    document.getElementById('denNgay').value = formatYMD(today);
    updateStatistics();
}

function exportExcel() {
    const tuNgay = document.getElementById('tuNgay').value;
    const denNgay = document.getElementById('denNgay').value;

    let url = '/admin/thong-ke/export-excel';
    let params = [];
    if (tuNgay) params.push(`tuNgay=${tuNgay}`);
    if (denNgay) params.push(`denNgay=${denNgay}`);

    if (params.length > 0) {
        url += '?' + params.join('&');
    }

    window.location.href = url;
}

function updateStatistics() {
    const tuNgay = document.getElementById('tuNgay').value;
    const denNgay = document.getElementById('denNgay').value;

    let urlParams = new URLSearchParams();
    if (tuNgay) urlParams.append('tuNgay', tuNgay);
    if (denNgay) urlParams.append('denNgay', denNgay);

    const paramsStr = urlParams.toString();

    // Cập nhật nhãn biểu đồ
    if (tuNgay && denNgay) {
        document.getElementById('chartLabel').innerText = `Từ ${formatDateDisplay(tuNgay)} đến ${formatDateDisplay(denNgay)}`;
    } else if (tuNgay) {
        document.getElementById('chartLabel').innerText = `Từ ${formatDateDisplay(tuNgay)}`;
    } else if (denNgay) {
        document.getElementById('chartLabel').innerText = `Đến ${formatDateDisplay(denNgay)}`;
    } else {
        document.getElementById('chartLabel').innerText = "Tất cả thời gian";
    }

    // Tải dữ liệu tổng hợp
    fetch(`/admin/thong-ke/data/tong-hop?${paramsStr}`, {
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(res => res.json())
    .then(data => {
        document.getElementById('doanhThuThucTe').innerText = new Intl.NumberFormat('vi-VN').format(data.doanhThuThucTe) + '₫';
        document.getElementById('doanhThuTamTinh').innerText = new Intl.NumberFormat('vi-VN').format(data.doanhThuTamTinh) + '₫';
        document.getElementById('doanhThuThatThoat').innerText = new Intl.NumberFormat('vi-VN').format(data.doanhThuThatThoat) + '₫';
        document.getElementById('soDonTrongKy').innerText = data.soDon;
    });

    // Tải dữ liệu biểu đồ
    fetch(`/admin/thong-ke/data/doanh-thu-ngay?${paramsStr}`, {
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(response => response.json())
    .then(data => {
        const labels = data.map(item => formatDateDisplay(item.ngay));
        const values = data.map(item => item.doanhThu);

        if (myChart) {
            myChart.destroy();
        }

        const ctx = document.getElementById('doanhThuChart').getContext('2d');
        myChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Doanh thu (VNĐ)',
                    data: values,
                    borderColor: '#8b4d4d',
                    backgroundColor: 'rgba(139, 77, 77, 0.1)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.3
                }]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) {
                                return new Intl.NumberFormat('vi-VN').format(value) + '₫';
                            }
                        }
                    }
                },
                plugins: {
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return new Intl.NumberFormat('vi-VN').format(context.raw) + '₫';
                            }
                        }
                    }
                }
            }
        });
    });
}

function formatDateDisplay(dateStr) {
    if (!dateStr) return "";
    const parts = dateStr.split('-');
    if (parts.length === 3) {
        return `${parts[2]}/${parts[1]}/${parts[0]}`;
    }
    return dateStr;
}
