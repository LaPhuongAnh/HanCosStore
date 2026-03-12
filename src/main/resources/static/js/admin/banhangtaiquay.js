/* ══════════════════════════════════════════
   STATE
   ══════════════════════════════════════════ */
let cart = [];
let selectedCustomer = null;
let appliedVoucher = null;
let paymentMethod = 'cash';
let currentCategory = 'all';
let customerMode = 'guest';

/* ══════════════════════════════════════════
   FORMAT
   ══════════════════════════════════════════ */
function fmt(n) {
    return Number(n || 0).toLocaleString('vi-VN') + '₫';
}

/* ══════════════════════════════════════════
   FILTER SẢN PHẨM (show/hide DOM)
   ══════════════════════════════════════════ */
function filterCategory(catId, el) {
    currentCategory = catId;
    document.querySelectorAll('.pos-cat-link, .pos-cat-sub').forEach(b => b.classList.remove('active'));
    el.classList.add('active');
    const parentItem = el.closest('.pos-cat-item');
    if (parentItem) {
        const parentLink = parentItem.querySelector('.pos-cat-link');
        if (parentLink && parentLink !== el) parentLink.classList.add('active');
    }
    filterProducts();
}

function filterProducts() {
    const keyword = document.getElementById('searchProduct').value.trim().toLowerCase();
    const cards = document.querySelectorAll('.pos-product-card');
    let visible = 0;

    cards.forEach(card => {
        const catId = parseInt(card.dataset.cat) || 0;
        const parentCatId = parseInt(card.dataset.pcat) || 0;
        const name = card.dataset.name || '';
        const code = card.dataset.code || '';

        const matchCat = currentCategory === 'all' || catId == currentCategory || parentCatId == currentCategory;
        const matchSearch = !keyword || name.includes(keyword) || code.includes(keyword);

        if (matchCat && matchSearch) {
            card.style.display = '';
            visible++;
        } else {
            card.style.display = 'none';
        }
    });

    document.getElementById('noProductMsg').style.display = visible === 0 ? '' : 'none';
}

let searchTimer = null;
document.getElementById('searchProduct').addEventListener('input', function () {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(filterProducts, 300);
});

/* ══════════════════════════════════════════
   VARIANT MODAL (đọc data từ DOM — Thymeleaf render)
   ══════════════════════════════════════════ */
let modalCard = null;
let modalSelectedColor = null;
let modalSelectedSize = null;
let modalSelectedVariant = null;

function getVariantsFromCard(card) {
    return Array.from(card.querySelectorAll('.vd')).map(el => ({
        id: parseInt(el.dataset.id),
        mauSac: el.dataset.color,
        kichCo: el.dataset.size,
        gia: parseFloat(el.dataset.price),
        soLuongTon: parseInt(el.dataset.stock)
    }));
}

function openVariantModal(productId) {
    const card = document.querySelector('.pos-product-card[data-id="' + productId + '"]');
    if (!card) return;
    modalCard = card;

    const variants = getVariantsFromCard(card);
    if (variants.length === 0) return;

    document.getElementById('modalProductName').textContent = card.querySelector('.prod-name').textContent;

    const imgEl = card.querySelector('.prod-img-wrap img');
    const modalImg = document.getElementById('modalProductImg');
    if (imgEl) { modalImg.src = imgEl.src; modalImg.style.display = ''; }
    else { modalImg.style.display = 'none'; }

    const colors = [...new Set(variants.map(v => v.mauSac))];
    const sizes = [...new Set(variants.map(v => v.kichCo))];

    document.getElementById('modalColors').innerHTML = colors.map(c =>
        `<button class="variant-color-btn" onclick="selectColor('${c}',this)">${c}</button>`
    ).join('');
    document.getElementById('modalSizes').innerHTML = sizes.map(s =>
        `<button class="variant-size-btn" onclick="selectSize('${s}',this)">${s}</button>`
    ).join('');

    modalSelectedColor = null;
    modalSelectedSize = null;
    modalSelectedVariant = null;
    document.getElementById('modalQty').value = 1;
    document.getElementById('modalPrice').textContent = fmt(Math.min(...variants.map(v => v.gia)));
    document.getElementById('modalStock').textContent = '';

    if (colors.length === 1) {
        modalSelectedColor = colors[0];
        document.querySelector('.variant-color-btn').classList.add('active');
    }
    if (sizes.length === 1) {
        modalSelectedSize = sizes[0];
        document.querySelector('.variant-size-btn').classList.add('active');
    }
    updateModalVariant();

    new bootstrap.Modal(document.getElementById('variantModal')).show();
}

function selectColor(color, el) {
    document.querySelectorAll('.variant-color-btn').forEach(b => b.classList.remove('active'));
    el.classList.add('active');
    modalSelectedColor = color;

    const variants = getVariantsFromCard(modalCard);
    const availableSizes = new Set(variants.filter(v => v.mauSac === color).map(v => v.kichCo));
    document.querySelectorAll('.variant-size-btn').forEach(btn => {
        const size = btn.textContent;
        if (availableSizes.has(size)) {
            btn.disabled = false;
            btn.style.opacity = '1';
        } else {
            btn.disabled = true;
            btn.style.opacity = '0.4';
            if (modalSelectedSize === size) {
                modalSelectedSize = null;
                btn.classList.remove('active');
            }
        }
    });

    const colorImgEl = modalCard.querySelector('.vcid[data-color="' + color + '"]');
    if (colorImgEl) document.getElementById('modalProductImg').src = colorImgEl.dataset.url;

    updateModalVariant();
}

function selectSize(size, el) {
    document.querySelectorAll('.variant-size-btn').forEach(b => b.classList.remove('active'));
    el.classList.add('active');
    modalSelectedSize = size;
    updateModalVariant();
}

function updateModalVariant() {
    if (modalSelectedColor && modalSelectedSize) {
        const variants = getVariantsFromCard(modalCard);
        const v = variants.find(b => b.mauSac === modalSelectedColor && b.kichCo === modalSelectedSize);
        modalSelectedVariant = v || null;
        if (v) {
            document.getElementById('modalPrice').textContent = fmt(v.gia);
            document.getElementById('modalStock').textContent = 'Tồn kho: ' + v.soLuongTon;
            const qtyEl = document.getElementById('modalQty');
            if (parseInt(qtyEl.value) > v.soLuongTon) qtyEl.value = v.soLuongTon > 0 ? v.soLuongTon : 1;
        }
    } else {
        modalSelectedVariant = null;
    }
}

function modalQtyChange(delta) {
    const qtyEl = document.getElementById('modalQty');
    let val = parseInt(qtyEl.value) + delta;
    if (val < 1) val = 1;
    if (modalSelectedVariant && val > modalSelectedVariant.soLuongTon) {
        showToast('Số lượng vượt quá tồn kho!', 'error');
        return;
    }
    qtyEl.value = val;
}

function modalQtyInput(el) {
    let val = parseInt(el.value) || 1;
    if (val < 1) val = 1;
    if (modalSelectedVariant && val > modalSelectedVariant.soLuongTon) {
        showToast('Số lượng vượt quá tồn kho! (Tồn: ' + modalSelectedVariant.soLuongTon + ')', 'error');
        val = modalSelectedVariant.soLuongTon;
    }
    el.value = val;
}

function addToCartFromModal() {
    if (!modalSelectedVariant) {
        showToast('Vui lòng chọn màu sắc và kích cỡ', 'error');
        return;
    }

    const variant = modalSelectedVariant;
    const qty = parseInt(document.getElementById('modalQty').value) || 1;

    const existingItem = cart.find(c => c.variantId === variant.id);
    const currentInCart = existingItem ? existingItem.qty : 0;
    if (currentInCart + qty > variant.soLuongTon) {
        showToast('Sản phẩm không đủ số lượng! (Tồn kho: ' + variant.soLuongTon + ', trong giỏ: ' + currentInCart + ')', 'error');
        return;
    }

    const imgEl = modalCard.querySelector('.prod-img-wrap img');
    const img = imgEl ? imgEl.src : null;

    if (existingItem) {
        existingItem.qty += qty;
    } else {
        cart.push({
            key: variant.id,
            variantId: variant.id,
            productName: modalCard.querySelector('.prod-name').textContent,
            color: variant.mauSac,
            size: variant.kichCo,
            price: variant.gia,
            qty: qty,
            stock: variant.soLuongTon,
            img: img
        });
    }

    bootstrap.Modal.getInstance(document.getElementById('variantModal')).hide();
    renderCart();
    recalc();
    showToast('Đã thêm vào giỏ hàng', 'success');
}

/* ══════════════════════════════════════════
   CART
   ══════════════════════════════════════════ */
function renderCart() {
    const wrap = document.getElementById('cartItems');
    document.getElementById('cartCount').textContent = cart.length;

    if (cart.length === 0) {
        wrap.innerHTML = '<div class="cart-empty"><i class="fas fa-shopping-basket"></i><p>Chưa có sản phẩm nào</p></div>';
        return;
    }

    wrap.innerHTML = cart.map((item, idx) => `
        <div class="cart-item">
            ${item.img ? `<img class="cart-item-img" src="${item.img}" alt="">` : '<div class="cart-item-img d-flex align-items-center justify-content-center"><i class="fas fa-image text-muted"></i></div>'}
            <div class="cart-item-info">
                <div class="cart-item-name" title="${item.productName}">${item.productName}</div>
                <div class="cart-item-variant">${item.color} / ${item.size}</div>
                <div class="cart-item-price">${fmt(item.price)}</div>
                <div class="qty-control">
                    <button onclick="changeQty(${idx},-1)">−</button>
                    <input type="number" class="qty-val" value="${item.qty}" min="1" onchange="changeQtyDirect(${idx}, this.value)">
                    <button onclick="changeQty(${idx},1)">+</button>
                </div>
                <div class="cart-item-subtotal mt-1">= ${fmt(item.price * item.qty)}</div>
            </div>
            <i class="fas fa-times cart-item-remove" onclick="removeItem(${idx})" title="Xóa"></i>
        </div>
    `).join('');
}

function changeQty(idx, delta) {
    const item = cart[idx];
    if (!item) return;
    const newQty = item.qty + delta;
    if (newQty < 1) {
        removeItem(idx);
        return;
    }
    if (newQty > item.stock) {
        showToast('Sản phẩm không đủ số lượng! Tồn kho: ' + item.stock, 'error');
        return;
    }
    item.qty = newQty;
    renderCart();
    recalc();
}

function changeQtyDirect(idx, value) {
    const item = cart[idx];
    if (!item) return;
    let newQty = parseInt(value) || 1;
    if (newQty < 1) newQty = 1;
    if (newQty > item.stock) {
        showToast('Sản phẩm không đủ số lượng! Tồn kho: ' + item.stock, 'error');
        newQty = item.stock;
    }
    item.qty = newQty;
    renderCart();
    recalc();
}

function removeItem(idx) {
    cart.splice(idx, 1);
    renderCart();
    recalc();
}

function clearCart() {
    cart = [];
    renderCart();
    recalc();
}

/* ══════════════════════════════════════════
   CUSTOMER MODE
   ══════════════════════════════════════════ */
function switchCustMode(mode, btn) {
    customerMode = mode;
    // toggle tab buttons
    document.querySelectorAll('.cust-mode-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    // toggle panels
    document.getElementById('custPanelGuest').style.display = mode === 'guest' ? '' : 'none';
    document.getElementById('custPanelExisting').style.display = mode === 'existing' ? '' : 'none';
    document.getElementById('custPanelNew').style.display = mode === 'new' ? '' : 'none';
    // reset state when switching
    if (mode !== 'existing') clearCustomer();
    if (mode !== 'new') {
        document.getElementById('custName').value = '';
        document.getElementById('custPhone').value = '';
    }
}

/* ══════════════════════════════════════════
   CUSTOMER COMBOBOX (Thymeleaf render, JS filter DOM)
   ══════════════════════════════════════════ */
let custDropdownOpen = false;

function toggleCustDropdown() {
    custDropdownOpen ? closeCustDropdown() : openCustDropdown();
}

function openCustDropdown() {
    custDropdownOpen = true;
    document.getElementById('custComboDropdown').classList.add('show');
    document.getElementById('custComboSearch').value = '';
    filterCustList('');
    setTimeout(() => document.getElementById('custComboSearch').focus(), 50);
}

function closeCustDropdown() {
    custDropdownOpen = false;
    document.getElementById('custComboDropdown').classList.remove('show');
}

function filterCustList(keyword) {
    const kw = keyword.toLowerCase();
    const items = document.querySelectorAll('#custComboList .cust-combobox-item');
    let visible = 0;
    items.forEach(item => {
        const name = item.dataset.name || '';
        const phone = (item.dataset.phone || '').toLowerCase();
        const email = item.dataset.email || '';
        const match = !kw || name.includes(kw) || phone.includes(kw) || email.includes(kw);
        item.style.display = match ? '' : 'none';
        if (match) visible++;
        if (selectedCustomer && parseInt(item.dataset.id) === selectedCustomer.id) {
            item.classList.add('selected');
        } else {
            item.classList.remove('selected');
        }
    });
    document.getElementById('custNoResult').style.display = visible === 0 ? '' : 'none';
}

// Search filter inside combobox
(function() {
    const searchInput = document.getElementById('custComboSearch');
    searchInput.addEventListener('input', function() {
        filterCustList(this.value.trim());
    });
})();

// Close dropdown on outside click
document.addEventListener('click', function(e) {
    if (custDropdownOpen && !document.getElementById('custCombobox').contains(e.target)) {
        closeCustDropdown();
    }
});

function pickCustomer(el) {
    selectedCustomer = {
        id: parseInt(el.dataset.id),
        hoTen: el.dataset.hoten,
        soDienThoai: el.dataset.phone
    };
    document.getElementById('custComboText').textContent = selectedCustomer.hoTen + ' — ' + (selectedCustomer.soDienThoai || '');
    document.getElementById('custComboText').classList.add('has-value');
    document.getElementById('custComboClear').style.display = '';
    closeCustDropdown();
}

function clearCustomer() {
    selectedCustomer = null;
    document.getElementById('custComboText').textContent = '-- Chọn khách hàng --';
    document.getElementById('custComboText').classList.remove('has-value');
    document.getElementById('custComboClear').style.display = 'none';
}

/* ══════════════════════════════════════════
   VOUCHER
   ══════════════════════════════════════════ */
async function applyVoucher() {
    const code = document.getElementById('voucherCode').value.trim();
    if (!code) { showToast('Vui lòng nhập mã giảm giá', 'error'); return; }

    const subtotal = cart.reduce((s, i) => s + i.price * i.qty, 0);
    if (subtotal === 0) { showToast('Giỏ hàng trống', 'error'); return; }

    try {
        const res = await fetch('/admin/pos/api/voucher/validate', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({code, amount: subtotal})
        });
        const data = await res.json();
        if (data.success) {
            appliedVoucher = {code: data.code, discount: Number(data.discount)};
            document.getElementById('voucherInputArea').style.display = 'none';
            document.getElementById('voucherApplied').style.display = '';
            document.getElementById('voucherLabel').textContent = data.code + ' (-' + fmt(data.discount) + ')';
            recalc();
            showToast('Áp dụng mã giảm giá thành công!', 'success');
        } else {
            showToast(data.message || 'Mã giảm giá không hợp lệ', 'error');
        }
    } catch (e) {
        showToast('Lỗi kiểm tra mã giảm giá', 'error');
    }
}

function removeVoucher() {
    appliedVoucher = null;
    document.getElementById('voucherInputArea').style.display = '';
    document.getElementById('voucherApplied').style.display = 'none';
    document.getElementById('voucherCode').value = '';
    recalc();
}

/* ══════════════════════════════════════════
   RECALC
   ══════════════════════════════════════════ */
function recalc() {
    const subtotal = cart.reduce((s, i) => s + i.price * i.qty, 0);
    const discount = appliedVoucher ? appliedVoucher.discount : 0;
    const total = Math.max(0, subtotal - discount);

    document.getElementById('subtotal').textContent = fmt(subtotal);
    if (discount > 0) {
        document.getElementById('discountRow').style.display = '';
        document.getElementById('discountAmount').textContent = '-' + fmt(discount);
    } else {
        document.getElementById('discountRow').style.display = 'none';
    }
    document.getElementById('totalAmount').textContent = fmt(total);

    // Enable/disable checkout
    document.getElementById('btnCheckout').disabled = cart.length === 0;

    if (paymentMethod === 'transfer') updateTransferQR();
    calcChange();
}

/* ══════════════════════════════════════════
   PAYMENT
   ══════════════════════════════════════════ */
/* ── Cấu hình ngân hàng ── */
const BANK_CODE  = 'MB';
const BANK_ACC   = '0766128057';
const BANK_OWNER = 'TRAN DUC HAI';

let bankInfo = null;

(async function loadBankInfo() {
    try {
        const res = await fetch('https://api.vietqr.io/v2/banks');
        const json = await res.json();
        if (json.data) {
            bankInfo = json.data.find(b => b.code === BANK_CODE || b.shortName === BANK_CODE);
            if (bankInfo) {
                document.getElementById('transferBankName').textContent = bankInfo.shortName + ' - ' + bankInfo.name;
                const logoEl = document.getElementById('transferBankLogo');
                if (bankInfo.logo) { logoEl.src = bankInfo.logo; logoEl.style.display = ''; }
            }
        }
    } catch (e) { /* fallback giữ nguyên text mặc định */ }
})();

function selectPayment(method, el) {
    paymentMethod = method;
    document.querySelectorAll('.pay-method-btn').forEach(b => b.classList.remove('active'));
    el.classList.add('active');

    const isCash = method === 'cash';
    document.getElementById('cashRow').style.display = isCash ? '' : 'none';
    document.getElementById('changeRow').style.display = 'none';
    document.getElementById('transferRow').style.display = isCash ? 'none' : '';

    if (!isCash) {
        updateTransferQR();
        openTransferModal();
    }
}

function openTransferModal() {
    updateTransferQR();
    new bootstrap.Modal(document.getElementById('transferModal')).show();
}

function updateTransferQR() {
    const subtotal = cart.reduce((s, i) => s + i.price * i.qty, 0);
    const discount = appliedVoucher ? appliedVoucher.discount : 0;
    const total = Math.max(0, subtotal - discount);

    document.getElementById('transferSTK').textContent = BANK_ACC;
    document.getElementById('transferName').textContent = BANK_OWNER;
    document.getElementById('transferAmount').textContent = fmt(total);

    const bankBin = bankInfo ? bankInfo.bin : BANK_CODE;
    const info = 'HANCOS ' + Date.now();
    const qrUrl = 'https://img.vietqr.io/image/' + bankBin + '-' + BANK_ACC + '-compact2.png?amount=' + total + '&addInfo=' + encodeURIComponent(info) + '&accountName=' + encodeURIComponent(BANK_OWNER);
    document.getElementById('transferQR').src = qrUrl;
}

function copySTK() {
    navigator.clipboard.writeText(BANK_ACC).then(() => showToast('Đã sao chép số tài khoản', 'success'));
}

function calcChange() {
    const subtotal = cart.reduce((s, i) => s + i.price * i.qty, 0);
    const discount = appliedVoucher ? appliedVoucher.discount : 0;
    const total = Math.max(0, subtotal - discount);
    const given = parseFloat(document.getElementById('cashGiven').value.replace(/[^0-9]/g, '')) || 0;

    if (given > 0 && paymentMethod === 'cash') {
        const change = given - total;
        document.getElementById('changeRow').style.display = '';
        document.getElementById('changeAmount').textContent = fmt(Math.max(0, change));
        document.getElementById('changeAmount').style.color = change >= 0 ? '#28a745' : '#dc3545';
    } else {
        document.getElementById('changeRow').style.display = 'none';
    }
}

/* ══════════════════════════════════════════
   CHECKOUT
   ══════════════════════════════════════════ */
async function checkout() {
    if (cart.length === 0) { showToast('Giỏ hàng trống', 'error'); return; }

    let custName, custPhone, custId = null;

    if (customerMode === 'guest') {
        custName = 'Khách lẻ';
        custPhone = 'N/A';
    } else if (customerMode === 'existing') {
        if (!selectedCustomer) { showToast('Vui lòng chọn khách hàng', 'error'); return; }
        custId = selectedCustomer.id;
        custName = selectedCustomer.hoTen || selectedCustomer.tenDangNhap;
        custPhone = selectedCustomer.soDienThoai || 'N/A';
    } else { // 'new'
        custName = document.getElementById('custName').value.trim();
        custPhone = document.getElementById('custPhone').value.trim();
        if (!custName) { showToast('Vui lòng nhập tên khách hàng', 'error'); return; }
        if (!custPhone) { showToast('Vui lòng nhập số điện thoại', 'error'); return; }
    }

    // Validate payment
    if (paymentMethod === 'cash') {
        const subtotal = cart.reduce((s, i) => s + i.price * i.qty, 0);
        const discount = appliedVoucher ? appliedVoucher.discount : 0;
        const total = Math.max(0, subtotal - discount);
        const given = parseFloat(document.getElementById('cashGiven').value.replace(/[^0-9]/g, '')) || 0;
        if (given < total) {
            showToast('Tiền khách đưa chưa đủ!', 'error');
            return;
        }
    } else if (paymentMethod === 'transfer') {
        if (!document.getElementById('transferConfirm').checked) {
            showToast('Vui lòng xác nhận đã nhận được tiền chuyển khoản!', 'error');
            return;
        }
    }

    const payload = {
        customerId: custId,
        customerName: custName,
        customerPhone: custPhone,
        paymentMethod: paymentMethod,
        voucherCode: appliedVoucher ? appliedVoucher.code : null,
        note: document.getElementById('orderNote').value.trim() || null,
        items: cart.map(i => ({
            variantId: i.variantId,
            qty: i.qty,
            price: i.price
        }))
    };

    document.getElementById('btnCheckout').disabled = true;
    document.getElementById('btnCheckout').innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Đang xử lý...';

    try {
        const res = await fetch('/admin/pos/api/checkout', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            document.getElementById('successOrderCode').textContent = data.orderCode;
            document.getElementById('successTotal').textContent = fmt(data.total);
            document.getElementById('successOverlay').classList.add('show');
        } else {
            showToast(data.message || 'Thanh toán thất bại', 'error');
            document.getElementById('btnCheckout').disabled = false;
            document.getElementById('btnCheckout').innerHTML = '<i class="fas fa-check-circle me-2"></i>Thanh toán';
        }
    } catch (e) {
        showToast('Lỗi kết nối: ' + e.message, 'error');
        document.getElementById('btnCheckout').disabled = false;
        document.getElementById('btnCheckout').innerHTML = '<i class="fas fa-check-circle me-2"></i>Thanh toán';
    }
}

/* ══════════════════════════════════════════
   NEW ORDER / RESET
   ══════════════════════════════════════════ */
function newOrder() {
    cart = [];
    selectedCustomer = null;
    appliedVoucher = null;
    paymentMethod = 'cash';

    document.getElementById('successOverlay').classList.remove('show');
    // Reset customer to guest mode
    customerMode = 'guest';
    document.querySelectorAll('.cust-mode-btn').forEach(b => b.classList.remove('active'));
    document.querySelector('[data-mode="guest"]').classList.add('active');
    document.getElementById('custPanelGuest').style.display = '';
    document.getElementById('custPanelExisting').style.display = 'none';
    document.getElementById('custPanelNew').style.display = 'none';
    clearCustomer();
    document.getElementById('custName').value = '';
    document.getElementById('custPhone').value = '';
    document.getElementById('orderNote').value = '';
    document.getElementById('voucherCode').value = '';
    document.getElementById('voucherInputArea').style.display = '';
    document.getElementById('voucherApplied').style.display = 'none';
    document.getElementById('cashGiven').value = '';
    document.getElementById('changeRow').style.display = 'none';
    document.getElementById('cashRow').style.display = '';
    document.getElementById('transferRow').style.display = 'none';
    document.getElementById('transferConfirm').checked = false;
    document.getElementById('btnCheckout').disabled = true;
    document.getElementById('btnCheckout').innerHTML = '<i class="fas fa-check-circle me-2"></i>Thanh toán';

    document.querySelectorAll('.pay-method-btn').forEach(b => b.classList.remove('active'));
    document.querySelector('[data-method="cash"]').classList.add('active');

    renderCart();
    recalc();

    // Reload trang để lấy dữ liệu tồn kho mới từ server
    window.location.reload();
}

/* ══════════════════════════════════════════
   TOAST
   ══════════════════════════════════════════ */
function showToast(msg, type) {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = 'pos-toast ' + (type || 'success');
    toast.innerHTML = `<i class="fas fa-${type === 'error' ? 'exclamation-circle text-danger' : 'check-circle text-success'}"></i> ${msg}`;
    container.appendChild(toast);
    setTimeout(() => { toast.style.opacity = '0'; setTimeout(() => toast.remove(), 300); }, 3000);
}
