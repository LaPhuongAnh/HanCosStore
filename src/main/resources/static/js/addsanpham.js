const API_URL = '/api';

const SIZE_LETTERS = ["XS","S","M","L","XL","XXL"];
const SIZE_PANTS = ["28","29","30","31","32","33","34","35","36","37","38"];
const SIZE_SHOES = ["35","36","37","38","39","40","41","42","43","44","45"];

function normalizeCategoryName(name) {
    return removeVietnameseTones((name || "").toLowerCase());
}

function getSelectedCategoryName() {
    const child = document.getElementById('danhMucId');
    const parent = document.getElementById('parentDanhMucId');

    if (child && child.value && child.selectedIndex >= 0) {
        return child.options[child.selectedIndex]?.text || "";
    }

    if (parent && parent.value && parent.selectedIndex >= 0) {
        return parent.options[parent.selectedIndex]?.text || "";
    }

    return "";
}

function getSizeOptions() {
    const name = normalizeCategoryName(getSelectedCategoryName());

    if (name.includes('giay') || name.includes('dep')) {
        return SIZE_SHOES;
    }

    if (name.includes('quan')) {
        return SIZE_PANTS;
    }

    if (name.includes('ao')) {
        return SIZE_LETTERS;
    }

    return SIZE_LETTERS;
}

function buildSizeOptionsHtml(selected = '') {
    const options = getSizeOptions();
    const optionHtml = options.map(size => `<option value="${size}" ${selected === size ? 'selected' : ''}>${size}</option>`).join('');
    return `<option value="">-- Chọn size --</option>${optionHtml}`;
}

function updateSizeSelect(selectEl) {
    if (!selectEl) return;
    const current = selectEl.value;
    selectEl.innerHTML = buildSizeOptionsHtml();
    if (getSizeOptions().includes(current)) {
        selectEl.value = current;
    }
}

function updateAllSizeSelects() {
    const variants = document.querySelectorAll('.variant-item');
    variants.forEach((item) => {
        const sizeEl = item.querySelector('select[name*=".kichCo"]');
        updateSizeSelect(sizeEl);
        const index = item.getAttribute('data-index');
        if (index !== null) {
            updateSKU(index);
        }
    });
}

async function loadChildren() {
    const parentId = document.getElementById('parentDanhMucId').value;
    const select = document.getElementById('danhMucId');

    // Xóa danh mục con cũ
    select.innerHTML = '<option value="">-- Chọn danh mục con --</option>';

    if (!parentId) {
        select.disabled = true;
        updateAllSizeSelects();
        return;
    }
    select.disabled = false;

    try {
        const response = await fetch(`${API_URL}/danh-muc/${parentId}/children`);
        const children = await response.json();

        children.forEach(dm => {
            const option = document.createElement('option');
            option.value = dm.id;
            option.textContent = dm.ten;
            select.appendChild(option);
        });
        updateAllSizeSelects();
    } catch (error) {
        console.error('Lỗi tải danh mục con:', error);
    }
}

async function loadDanhMuc() {
    try {
        const response = await fetch(`${API_URL}/danh-muc`);
        const danhMucs = await response.json();
        const select = document.getElementById('danhMucId');

        danhMucs.forEach(dm => {
            const option = document.createElement('option');
            option.value = dm.id;
            option.textContent = dm.ten;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Lỗi tải danh mục:', error);
    }
}

function addVariant() {
    const variantsList = document.getElementById('variantsList');
    const index = variantsList.children.length;

    const html = `
                <div class="variant-item" data-index="${index}">
                    <div class="item-header">
                        <span class="item-number"><i class="fas fa-tags"></i> Biến thể #${index + 1}</span>
                        <button type="button" class="btn btn-danger" onclick="removeVariant(${index})">
                            <i class="fas fa-trash"></i> Xóa
                        </button>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                          <label>Màu Sắc <span class="required">*</span></label>

  <select name="variants[${index}].mauSac"
          required
        onchange="updateSKU(this.closest('.variant-item').dataset.index)"
          class="form-control">
    <option value="">-- Chọn màu --</option>
    <option value="Đen">Đen</option>
    <option value="Trắng">Trắng</option>
    <option value="Đỏ">Đỏ</option>
    <option value="Xanh dương">Xanh dương</option>
    <option value="Xanh lá">Xanh lá</option>
    <option value="Vàng">Vàng</option>
    <option value="Cam">Cam</option>
    <option value="Tím">Tím</option>
    <option value="Hồng">Hồng</option>
    <option value="Nâu">Nâu</option>
    <option value="Xám">Xám</option>
    <option value="Be">Be</option>
  </select>
                        </div>
                        <div class="form-group">
                            <label>Kích Cỡ <span class="required">*</span></label>
                            <select name="variants[${index}].kichCo" required onchange="updateSKU(${index})">
                                ${buildSizeOptionsHtml('')}
                            </select>
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>Mã SKU <span class="required">*</span></label>
                            <input type="text" name="variants[${index}].maSKU" required placeholder="AO_PHAO_AKP_DEN_S">
                        </div>
                        <div class="form-group">
                            <label>Số Lượng Tồn <span class="required">*</span></label>
                            <input type="number" name="variants[${index}].soLuongTon" required min="0" placeholder="100">
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>Giá Bán <span class="required">*</span></label>
                            <input type="number" name="variants[${index}].gia" required min="0" placeholder="850000">
                        </div>
                        <div class="form-group">
                            <label>Giá Gốc</label>
                            <input type="number" name="variants[${index}].giaGoc" min="0" placeholder="1200000">
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>Khối Lượng (gram)</label>
                            <input type="number" name="variants[${index}].khoiLuongGram" min="0" placeholder="500">
                        </div>
                    </div>
                </div>
            `;

    variantsList.insertAdjacentHTML('beforeend', html);
}

function updateSKU(index) {
    const item = document.querySelector(`.variant-item[data-index="${index}"]`);
    if (!item) return;

    const maSP = document.getElementById('maSanPham').value.trim();

    // Màu là SELECT
    const mauSacEl = item.querySelector('select[name*=".mauSac"]');
    const kichCoEl = item.querySelector('select[name*=".kichCo"]');
    const skuEl    = item.querySelector('input[name*=".maSKU"]');

    if (!mauSacEl || !kichCoEl || !skuEl) return;

    const mauSac = mauSacEl.value;
    const kichCo = kichCoEl.value;

    if (maSP && mauSac && kichCo) {
        const sku = `${toSkuToken(maSP)}_${toSkuToken(mauSac)}_${toSkuToken(kichCo)}`;
        if (skuEl.value !== sku) {
            skuEl.value = sku;
        }
    }
}

function toSkuToken(s) {
    return removeVietnameseTones(s)
        .trim()
        .toUpperCase()
        .replace(/\s+/g, "_");
}

function removeVietnameseTones(str) {
    if (!str) return "";
    str = str.replace(/à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ|À|Á|Ạ|Ả|Ã|Â|Ầ|Ấ|Ậ|Ẩ|Ẫ|Ă|Ằ|Ắ|Ặ|Ẳ|Ẵ/g, "a");
    str = str.replace(/è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ|È|É|Ẹ|Ẻ|Ẽ|Ê|Ề|Ế|Ệ|Ể|Ễ/g, "e");
    str = str.replace(/ì|í|ị|ỉ|ĩ|Ì|Í|Ị|Ỉ|Ĩ/g, "i");
    str = str.replace(/ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ|Ò|Ó|Ọ|Ỏ|Õ|Ô|Ồ|Ố|Ộ|Ổ|Ỗ|Ơ|Ờ|Ớ|Ợ|Ở|Ỡ/g, "o");
    str = str.replace(/ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ|Ù|Ú|Ụ|Ủ|Ũ|Ư|Ừ|Ứ|Ự|Ử|Ữ/g, "u");
    str = str.replace(/ỳ|ý|ỵ|ỷ|ỹ|Ỳ|Ý|Ỵ|Ỷ|Ỹ/g, "y");
    str = str.replace(/đ|Đ/g, "d");
    return str;
}

function updateAllSKUs() {
    const variants = document.querySelectorAll('.variant-item');
    variants.forEach((v, index) => {
        updateSKU(v.getAttribute('data-index'));
    });
}

function syncColors() {
    const variantColors = Array.from(document.querySelectorAll('#variantsList select[name*=".mauSac"]'))
        .map(el => el.value.trim())
        .filter((value, index, self) => value !== "" && self.indexOf(value) === index);

    const colorImageSections = document.querySelectorAll('#colorImagesList .image-item');

    if (variantColors.length > 0 && colorImageSections.length === 0) {
        variantColors.forEach(color => addColorImageWithColor(color));
    }
}

function addColorImageWithColor(color) {
    const colorImagesList = document.getElementById('colorImagesList');
    const index = colorImagesList.children.length;

    const html = `
                <div class="image-item" data-index="${index}">
                    <div class="item-header">
                        <span class="item-number"><i class="fas fa-palette"></i> Hình màu #${index + 1}</span>
                        <button type="button" class="btn btn-danger" onclick="removeColorImage(${index})">
                            <i class="fas fa-trash"></i> Xóa
                        </button>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>Màu Sắc <span class="required">*</span></label>
                            <input type="text" name="colorImages[${index}].mauSac" required value="${color}" placeholder="Đen">
                        </div>
                       <div class="form-group">
  <label>Chọn Ảnh <span class="required">*</span></label>

  <input type="file"
         accept=".jpg,.jpeg,.png,.webp,.gif,.bmp,.tif,.tiff,.heic,.heif,image/*"
         class="file-upload"
         onchange="handleFileUpload(this, 'colorImages[${index}].duongDanAnh', 'preview_color_img_${index}')">

  <input type="hidden" name="colorImages[${index}].duongDanAnh" required>
  <img id="preview_color_img_${index}" class="preview-img" src="" alt="Preview">
</div>
                    </div>
                </div>
            `;
    colorImagesList.insertAdjacentHTML('beforeend', html);
}

function removeVariant(index) {
    const variant = document.querySelector(`#variantsList .variant-item[data-index="${index}"]`);
    if (variant) {
        variant.remove();
        updateVariantNumbers();
    }
}

function updateVariantNumbers() {
    const variants = document.querySelectorAll('#variantsList .variant-item');
    variants.forEach((variant, idx) => {
        variant.setAttribute('data-index', idx);
        variant.querySelector('.item-number').textContent = `Biến thể #${idx + 1}`;
    });
}

function getNextImageIndex() {
    const images = document.querySelectorAll('#imagesList .image-item');
    if (images.length === 0) return 0;

    let maxIndex = -1;
    images.forEach((item) => {
        const idx = parseInt(item.getAttribute('data-index'), 10);
        if (!Number.isNaN(idx) && idx > maxIndex) {
            maxIndex = idx;
        }
    });
    return maxIndex + 1;
}

function addImage() {
    const imagesList = document.getElementById('imagesList');
    const index = getNextImageIndex();

    const html = `
                <div class="image-item" data-index="${index}">
                    <div class="item-header">
                        <span class="item-number"><i class="fas fa-image"></i> Hình ảnh #${index + 1}</span>
                        <button type="button" class="btn btn-danger" onclick="removeImage(${index})">
                            <i class="fas fa-trash"></i> Xóa
                        </button>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>Chọn Ảnh <span class="required">*</span></label>
                            <input type="file" accept="image/*" class="file-upload"
                                onchange="handleFileUpload(this, 'images[${index}].duongDanAnh', 'preview_img_${index}')">
                            <input type="hidden" name="images[${index}].duongDanAnh" required>
                            <img id="preview_img_${index}" class="preview-img" src="" alt="Preview">
                        </div>
                        <div class="form-group">
                            <label>Thứ Tự</label>
                            <input type="number" name="images[${index}].thuTu" value="${index + 1}" min="1">
                        </div>
                    </div>
                    <div class="checkbox-group">
                        <input type="checkbox" name="images[${index}].laAnhChinh" id="primary_${index}" ${index === 0 ? 'checked' : ''} onchange="handlePrimaryChange(${index})">
                        <label for="primary_${index}">Đặt làm ảnh chính</label>
                    </div>
                </div>
            `;

    imagesList.insertAdjacentHTML('beforeend', html);
    return index;
}

async function handleFileUpload(fileInput, hiddenInputName, previewId) {
    const file = fileInput.files[0];
    if (!file) return;

    // Xem trước ngay
    const preview = document.getElementById(previewId);
    preview.src = URL.createObjectURL(file);
    preview.classList.add('show');

    // Tạo FormData
    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch(`${API_URL}/upload`, {
            method: 'POST',
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            body: formData
        });

        if (response.ok) {
            const relativePath = await response.text();
            document.querySelector(`input[name="${hiddenInputName}"]`).value = relativePath;
        } else {
            const error = await response.text();
            showAlert('error', `Lỗi upload: ${error}`);
        }
    } catch (error) {
        showAlert('error', `Lỗi kết nối upload: ${error.message}`);
    }
}

function removeImage(index) {
    const image = document.querySelector(`#imagesList .image-item[data-index="${index}"]`);
    if (image) image.remove();
}

function setFileToInput(fileInput, file) {
    const dt = new DataTransfer();
    dt.items.add(file);
    fileInput.files = dt.files;
}

function normalizePastedFile(file, seq) {
    const ext = (file.type && file.type.includes('/')) ? file.type.split('/')[1] : 'png';
    const safeExt = (ext || 'png').replace(/[^a-zA-Z0-9]/g, '').toLowerCase() || 'png';
    const fileName = `pasted_${Date.now()}_${seq}.${safeExt}`;
    return new File([file], fileName, { type: file.type || 'image/png' });
}

function handleImagePaste(event) {
    const items = event.clipboardData?.items || [];
    const imageFiles = [];

    for (const item of items) {
        if (item.kind !== 'file') continue;
        const file = item.getAsFile();
        if (file && file.type && file.type.startsWith('image/')) {
            imageFiles.push(file);
        }
    }

    if (imageFiles.length === 0) return;
    event.preventDefault();

    imageFiles.forEach((rawFile, i) => {
        const file = normalizePastedFile(rawFile, i + 1);
        const index = addImage();
        const input = document.querySelector(`#imagesList .image-item[data-index="${index}"] input.file-upload`);
        if (!input) return;

        setFileToInput(input, file);
        handleFileUpload(input, `images[${index}].duongDanAnh`, `preview_img_${index}`);
    });
}

function handlePrimaryChange(index) {
    const checkboxes = document.querySelectorAll('input[name*="laAnhChinh"]');
    checkboxes.forEach((cb, i) => {
        if (i !== index) cb.checked = false;
    });
}

function addColorImage() {
    const colorImagesList = document.getElementById('colorImagesList');
    const index = colorImagesList.children.length;

    const html = `
                <div class="image-item" data-index="${index}">
                    <div class="item-header">
                        <span class="item-number"><i class="fas fa-palette"></i> Hình màu #${index + 1}</span>
                        <button type="button" class="btn btn-danger" onclick="removeColorImage(${index})">
                            <i class="fas fa-trash"></i> Xóa
                        </button>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>Màu Sắc <span class="required">*</span></label>
                            <input type="text" name="colorImages[${index}].mauSac" required placeholder="Đen">
                        </div>
                        <div class="form-group">
                            <label>Chọn Ảnh <span class="required">*</span></label>
                            <input type="file" accept="image/*" class="file-upload"
                                onchange="handleFileUpload(this, 'colorImages[${index}].duongDanAnh', 'preview_color_img_${index}')">
                            <input type="hidden" name="colorImages[${index}].duongDanAnh" required>
                            <img id="preview_color_img_${index}" class="preview-img" src="" alt="Preview">
                        </div>
                    </div>
                </div>
            `;

    colorImagesList.insertAdjacentHTML('beforeend', html);
}

function removeColorImage(index) {
    const image = document.querySelectorAll('#colorImagesList .image-item')[index];
    if (image) image.remove();
}

function showAlert(type, message) {
    Swal.fire({
        icon: type,
        title: type === 'success' ? 'Thành công!' : 'Lỗi!',
        text: message,
        confirmButtonColor: '#8b4d4d'
    });
}

async function resetForm() {
    const result = await Swal.fire({
        title: 'Bạn có chắc chắn?',
        text: "Tất cả dữ liệu đã nhập sẽ bị xóa!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#8b4d4d',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
        document.getElementById('productForm').reset();
        document.getElementById('variantsList').innerHTML = '';
        document.getElementById('imagesList').innerHTML = '';
        document.getElementById('colorImagesList').innerHTML = '';

        addVariant();
        addImage();
        addColorImage();
    }
}

function collectFormData() {
    const formData = {
        maSanPham: document.getElementById('maSanPham').value.trim(),
        ten: document.getElementById('ten').value.trim(),
        moTaNgan: document.getElementById('moTaNgan').value.trim(),
        moTa: document.getElementById('moTa').value.trim(),
        chatLieu: document.getElementById('chatLieu').value.trim(),
        gioiTinh: document.getElementById('gioiTinh').value,
        danhMucId: document.getElementById('danhMucId').value ? parseInt(document.getElementById('danhMucId').value) : (document.getElementById('parentDanhMucId').value ? parseInt(document.getElementById('parentDanhMucId').value) : null),
        bienThes: [],
        hinhAnhSanPhams: [],
        hinhAnhMauSacs: []
    };

    const variants = document.querySelectorAll('#variantsList .variant-item');
    variants.forEach(variant => {
        const bienThe = {
            maSKU: variant.querySelector('input[name*=".maSKU"]').value.trim(),
            mauSac: variant.querySelector('select[name*=".mauSac"]').value.trim(),
            kichCo: variant.querySelector('select[name*=".kichCo"]').value,
            soLuongTon: parseInt(variant.querySelector('input[name*=".soLuongTon"]').value) || 0,
            gia: parseFloat(variant.querySelector('input[name*=".gia"]').value) || 0,
            giaGoc: variant.querySelector('input[name*=".giaGoc"]').value ? parseFloat(variant.querySelector('input[name*=".giaGoc"]').value) : null,
            khoiLuongGram: variant.querySelector('input[name*=".khoiLuongGram"]').value ? parseInt(variant.querySelector('input[name*=".khoiLuongGram"]').value) : null,
            trangThai: 'ACTIVE'
        };
        formData.bienThes.push(bienThe);
    });

    const images = document.querySelectorAll('#imagesList .image-item');
    images.forEach(image => {
        formData.hinhAnhSanPhams.push({
            duongDanAnh: image.querySelector('input[name*="duongDanAnh"]').value.trim(),
            laAnhChinh: image.querySelector('input[name*="laAnhChinh"]').checked,
            thuTu: parseInt(image.querySelector('input[name*="thuTu"]').value) || 1
        });
    });

    const colorImages = document.querySelectorAll('#colorImagesList .image-item');
    colorImages.forEach(image => {
        formData.hinhAnhMauSacs.push({
            mauSac: image.querySelector('input[name*="mauSac"]').value.trim(),
            duongDanAnh: image.querySelector('input[name*="duongDanAnh"]').value.trim()
        });
    });

    return formData;
}

document.getElementById('productForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const formData = collectFormData();

    if (formData.bienThes.length === 0) {
        showAlert('error', 'Vui lòng thêm ít nhất 1 biến thể!');
        return;
    }

    if (formData.hinhAnhSanPhams.length === 0) {
        showAlert('error', 'Vui lòng thêm ít nhất 1 hình ảnh!');
        return;
    }

    document.getElementById('loading').classList.add('show');

    try {
        const response = await fetch(`${API_URL}/san-pham`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify(formData)
        });

        document.getElementById('loading').classList.remove('show');

        if (response.ok) {
            const result = await response.json();
            showAlert('success', `Tạo sản phẩm thành công! Mã: ${result.maSanPham}`);

            setTimeout(() => resetForm(), 2000);
        } else {
            const error = await response.text();
            showAlert('error', `Lỗi: ${error}`);
        }
    } catch (error) {
        document.getElementById('loading').classList.remove('show');
        showAlert('error', `Lỗi kết nối: ${error.message}`);
    }
});

window.addEventListener('DOMContentLoaded', () => {
    // Khởi tạo form trống
    addVariant();
    addImage();
    // addColorImage(); // Không cần tạo sẵn, người dùng sẽ nhấn Sync hoặc thêm sau

    const parentDanhMucEl = document.getElementById('parentDanhMucId');
    const danhMucEl = document.getElementById('danhMucId');

    if (parentDanhMucEl) {
        parentDanhMucEl.addEventListener('change', () => setTimeout(updateAllSizeSelects, 0));
    }
    if (danhMucEl) {
        danhMucEl.addEventListener('change', updateAllSizeSelects);
    }

    const pasteArea = document.getElementById('imagePasteArea');
    if (pasteArea) {
        pasteArea.addEventListener('click', () => pasteArea.focus());
        pasteArea.addEventListener('paste', handleImagePaste);
    }
});
