function cancelOrder() {
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
            document.getElementById('cancelReason').value = reason;
            document.getElementById('cancelOrderForm').submit();
        }
    });
}

function requestReturn() {
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
            document.getElementById('returnReason').value = reason;
            document.getElementById('returnOrderForm').submit();
        }
    });
}

let locationData = [];
const editAddressModal = new bootstrap.Modal(document.getElementById('editAddressModal'));

function showEditAddressModal() {
    if (locationData.length === 0) {
        fetch('/data/dvhcvn.json')
            .then(response => response.json())
            .then(res => {
                locationData = res.data;
                initLocationDropdowns();
                editAddressModal.show();
            })
            .catch(error => console.error('Lỗi tải dữ liệu địa chính:', error));
    } else {
        editAddressModal.show();
    }
}

function initLocationDropdowns() {
    const provinceSelect = document.getElementById('province');
    const districtSelect = document.getElementById('district');
    const wardSelect = document.getElementById('ward');
    const detailAddressInput = document.getElementById('detailAddress');

    provinceSelect.innerHTML = '<option value="">Chọn Tỉnh/Thành</option>';
    locationData.forEach(p => {
        provinceSelect.add(new Option(p.name, p.level1_id));
    });

    const headRow = document.querySelector('.head-row');
    const currentAddress = headRow ? (headRow.dataset.currentAddress || '') : '';

    if (currentAddress) {
        const parts = currentAddress.split(',').map(s => s.trim());

        if (parts.length >= 4) {
            const detail = parts.slice(0, parts.length - 3).join(', ');
            const wardName = parts[parts.length - 3];
            const districtName = parts[parts.length - 2];
            const provinceName = parts[parts.length - 1];

            detailAddressInput.value = detail;

            const prov = locationData.find(p => p.name === provinceName);
            if (prov) {
                provinceSelect.value = prov.level1_id;
                updateDistricts(prov.level1_id);

                setTimeout(() => {
                    const dist = prov.level2s.find(d => d.name === districtName);
                    if (dist) {
                        districtSelect.value = dist.level2_id;
                        updateWards(prov.level1_id, dist.level2_id);

                        setTimeout(() => {
                            const ward = dist.level3s.find(w => w.name === wardName);
                            if (ward) {
                                wardSelect.value = ward.level3_id;
                            }
                        }, 100);
                    }
                }, 100);
            }
        } else {
            detailAddressInput.value = currentAddress;
        }
    }

    provinceSelect.addEventListener('change', function() {
        updateDistricts(this.value);
        wardSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
        wardSelect.disabled = true;
    });

    districtSelect.addEventListener('change', function() {
        updateWards(provinceSelect.value, this.value);
    });
}

function updateDistricts(provinceId) {
    const districtSelect = document.getElementById('district');
    districtSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';

    if (!provinceId) {
        districtSelect.disabled = true;
        return;
    }

    const province = locationData.find(p => p.level1_id === provinceId);
    if (province && province.level2s) {
        province.level2s.forEach(d => {
            districtSelect.add(new Option(d.name, d.level2_id));
        });
        districtSelect.disabled = false;
    } else {
        districtSelect.disabled = true;
    }
}

function updateWards(provinceId, districtId) {
    const wardSelect = document.getElementById('ward');
    wardSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';

    if (!provinceId || !districtId) {
        wardSelect.disabled = true;
        return;
    }

    const province = locationData.find(p => p.level1_id === provinceId);
    if (province && province.level2s) {
        const district = province.level2s.find(d => d.level2_id === districtId);
        if (district && district.level3s) {
            district.level3s.forEach(w => {
                wardSelect.add(new Option(w.name, w.level3_id));
            });
            wardSelect.disabled = false;
        } else {
            wardSelect.disabled = true;
        }
    } else {
        wardSelect.disabled = true;
    }
}

function saveAddress() {
    const hoTen = document.getElementById('editHoTen').value;
    const soDienThoai = document.getElementById('editSoDienThoai').value;
    const provinceSelect = document.getElementById('province');
    const districtSelect = document.getElementById('district');
    const wardSelect = document.getElementById('ward');
    const detail = document.getElementById('detailAddress').value;

    if (!hoTen || !soDienThoai || !provinceSelect.value || !districtSelect.value || !wardSelect.value || !detail) {
        Swal.fire('Lỗi', 'Vui lòng điền đầy đủ thông tin giao hàng!', 'warning');
        return;
    }

    const provinceName = provinceSelect.options[provinceSelect.selectedIndex].text;
    const districtName = districtSelect.options[districtSelect.selectedIndex].text;
    const wardName = wardSelect.options[wardSelect.selectedIndex].text;

    const fullAddress = `${detail}, ${wardName}, ${districtName}, ${provinceName}`;

    document.getElementById('fullAddress').value = fullAddress;
    document.getElementById('editAddressForm').submit();
}
