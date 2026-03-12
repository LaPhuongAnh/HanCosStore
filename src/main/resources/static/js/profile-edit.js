let locationData = [];
let addrModal;
let locationReady;

document.addEventListener('DOMContentLoaded', function() {
    addrModal = new bootstrap.Modal(document.getElementById('addressModal'));

    locationReady = fetch('/data/dvhcvn.json')
        .then(r => r.json())
        .then(res => {
            locationData = res.data;
            const sel = document.getElementById('addrProvince');
            locationData.forEach(p => sel.add(new Option(p.name, p.name)));
        });

    document.getElementById('addrProvince').addEventListener('change', function() {
        const dSel = document.getElementById('addrDistrict');
        const wSel = document.getElementById('addrWard');
        dSel.innerHTML = '<option value="">Chọn</option>';
        wSel.innerHTML = '<option value="">Chọn</option>';
        wSel.disabled = true;
        if (this.value) {
            const prov = locationData.find(p => p.name === this.value);
            if (prov && prov.level2s) {
                prov.level2s.forEach(d => dSel.add(new Option(d.name, d.name)));
                dSel.disabled = false;
            }
        } else {
            dSel.disabled = true;
        }
    });

    document.getElementById('addrDistrict').addEventListener('change', function() {
        const wSel = document.getElementById('addrWard');
        wSel.innerHTML = '<option value="">Chọn</option>';
        if (this.value) {
            const prov = locationData.find(p => p.name === document.getElementById('addrProvince').value);
            const dist = prov.level2s.find(d => d.name === this.value);
            if (dist && dist.level3s) {
                dist.level3s.forEach(w => wSel.add(new Option(w.name, w.name)));
                wSel.disabled = false;
            }
        } else {
            wSel.disabled = true;
        }
    });
});

function openAddressModal() {
    document.getElementById('addressModalTitle').textContent = 'Thêm địa chỉ';
    document.getElementById('addrId').value = '';
    document.getElementById('addrHoTen').value = '';
    document.getElementById('addrSDT').value = '';
    document.getElementById('addrProvince').value = '';
    document.getElementById('addrProvince').dispatchEvent(new Event('change'));
    document.getElementById('addrDetail').value = '';
    document.getElementById('addrDefault').checked = false;
    addrModal.show();
}

function editAddressFromEl(el) {
    const d = el.dataset;
    editAddress(d.id, d.hoten, d.sdt, d.tinh, d.huyen, d.xa, d.chitiet, d.macdinh === 'true');
}

async function editAddress(id, hoTen, sdt, tinhThanh, quanHuyen, phuongXa, chiTiet, macDinh) {
    await locationReady;
    document.getElementById('addressModalTitle').textContent = 'Sửa địa chỉ';
    document.getElementById('addrId').value = id;
    document.getElementById('addrHoTen').value = hoTen;
    document.getElementById('addrSDT').value = sdt;
    document.getElementById('addrDetail').value = chiTiet;
    document.getElementById('addrDefault').checked = macDinh;

    const pSel = document.getElementById('addrProvince');
    const dSel = document.getElementById('addrDistrict');
    const wSel = document.getElementById('addrWard');

    dSel.innerHTML = '<option value="">Chọn</option>';
    wSel.innerHTML = '<option value="">Chọn</option>';
    dSel.disabled = true;
    wSel.disabled = true;

    const matchName = (full, short) => full === short || full.endsWith(short) || short.endsWith(full);

    const prov = locationData.find(p => matchName(p.name, tinhThanh));
    if (prov) {
        pSel.value = prov.name;
        prov.level2s.forEach(d => dSel.add(new Option(d.name, d.name)));
        dSel.disabled = false;

        const dist = prov.level2s.find(d => matchName(d.name, quanHuyen));
        if (dist) {
            dSel.value = dist.name;
            dist.level3s.forEach(w => wSel.add(new Option(w.name, w.name)));
            wSel.disabled = false;

            const ward = dist.level3s.find(w => matchName(w.name, phuongXa));
            if (ward) {
                wSel.value = ward.name;
            }
        }
    }

    addrModal.show();
}

function saveAddress() {
    const hoTen = document.getElementById('addrHoTen').value.trim();
    const sdt = document.getElementById('addrSDT').value.trim();
    const tinh = document.getElementById('addrProvince').value;
    const quan = document.getElementById('addrDistrict').value;
    const phuong = document.getElementById('addrWard').value;
    const chiTiet = document.getElementById('addrDetail').value.trim();

    if (!hoTen || !sdt || !tinh || !quan || !phuong || !chiTiet) {
        alert('Vui lòng điền đầy đủ thông tin');
        return;
    }

    const form = new FormData();
    const addrId = document.getElementById('addrId').value;
    if (addrId) {
        form.append('id', addrId);
    }
    form.append('hoTenNhan', hoTen);
    form.append('soDienThoaiNhan', sdt);
    form.append('tinhThanh', tinh);
    form.append('quanHuyen', quan);
    form.append('phuongXa', phuong);
    form.append('diaChiChiTiet', chiTiet);
    form.append('laMacDinh', document.getElementById('addrDefault').checked);

    fetch('/account/address/save', { method: 'POST', body: form })
        .then(r => r.json())
        .then(data => {
            if (data.success) {
                addrModal.hide();
                location.reload();
            } else {
                alert(data.message || 'Lỗi');
            }
        });
}

function deleteAddress(id) {
    if (!confirm('Xóa địa chỉ này?')) {
        return;
    }
    const form = new FormData();
    form.append('id', id);
    fetch('/account/address/delete', { method: 'POST', body: form })
        .then(r => r.json())
        .then(data => {
            if (data.success) {
                location.reload();
            } else {
                alert(data.message || 'Lỗi');
            }
        });
}
