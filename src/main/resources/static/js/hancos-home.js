document.addEventListener("DOMContentLoaded", () => {
    // normalize: bỏ dấu + lower + trim
    const normalize = (s) =>
        (s ?? "")
            .toString()
            .trim()
            .toLowerCase()
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "");

    // Ánh xạ tên màu (VN/biến thể) -> class CSS
    const colorMap = new Map([
        ["den", "black"],
        ["trang", "white"],
        ["xam", "gray"],
        ["be", "beige"],
        ["beige", "beige"],
        ["nau", "brown"],
        ["xanh navy", "navy"],
        ["navy", "navy"],
        ["xanh", "navy"], // bạn muốn xanh chung hiển thị navy
        ["do", "red"],
        ["hong", "pink"],
        ["vang", "yellow"],
        ["xanh la", "green"],
        ["xanh duong", "blue"],
    ]);

    const getCssColorName = (name) => {
        const key = normalize(name);
        if (colorMap.has(key)) return colorMap.get(key);
        for (const [k, v] of colorMap.entries()) {
            if (key.startsWith(k)) return v;
        }
        return "gray";
    };

    // Update color swatches
    document.querySelectorAll(".color-swatch").forEach((swatch) => {
        const colorName = swatch.dataset.colorName || swatch.getAttribute("data-color-name") || "";
        const key = normalize(colorName);

        const cssColor = getCssColorName(colorName);

        // reset class về đúng 2 class cần thiết
        swatch.className = `color-swatch color-${cssColor}`;
    });

    // Filter tabs (hiện tại chỉ set active + log)
    const filterBtns = document.querySelectorAll(".filter-btn");
    filterBtns.forEach((btn) => {
        btn.addEventListener("click", () => {
            filterBtns.forEach((b) => b.classList.remove("active"));
            btn.classList.add("active");

            const filter = btn.getAttribute("data-filter");
            console.log("Filter by:", filter);
            // Thêm logic filter nếu cần
        });
    });

    const productCards = Array.from(document.querySelectorAll(".product-card"));
    const colorSelectEl = document.getElementById("colorFilterSelect");
    const sizeSelectEl = document.getElementById("sizeFilterSelect");
    const priceSelectEl = document.getElementById("priceFilterSelect");
    const resetBtn = document.getElementById("resetFilters");
    const filterEmptyState = document.getElementById("filterEmptyState");
    const paginationWrapEl = document.getElementById("homePaginationWrap");
    const paginationEl = document.getElementById("homePagination");
    const pageInfoEl = document.getElementById("homePageInfo");
    const pageSize = Math.max(parseInt(paginationEl?.dataset.pageSize || "8", 10), 1);
    let currentPage = 1;

    const splitValues = (value) =>
        (value || "")
            .split(",")
            .map((v) => v.trim())
            .filter(Boolean);

    const toNumber = (value) => {
        const num = parseFloat(value);
        return Number.isFinite(num) ? num : null;
    };

    const uniqueFromProducts = (attr) => {
        const set = new Map();
        productCards.forEach((card) => {
            splitValues(card.getAttribute(attr)).forEach((val) => {
                const key = normalize(val);
                if (!set.has(key)) {
                    set.set(key, val);
                }
            });
        });
        return Array.from(set.values());
    };

    const renderSelectOptions = (selectEl, values) => {
        if (!selectEl) return;
        selectEl.querySelectorAll("option:not(:first-child)").forEach((opt) => opt.remove());
        values.forEach((val) => {
            const option = document.createElement("option");
            option.value = val;
            option.textContent = val;
            selectEl.appendChild(option);
        });
    };

    const parsePriceRange = (value) => {
        if (!value) return { min: null, max: null };
        const [minStr, maxStr] = value.split("-");
        const min = minStr ? toNumber(minStr) : null;
        const max = maxStr ? toNumber(maxStr) : null;
        return { min, max };
    };

    const renderPagination = (visibleCards) => {
        if (!paginationEl || !paginationWrapEl) return;

        const totalItems = visibleCards.length;
        const totalPages = Math.max(Math.ceil(totalItems / pageSize), 1);
        if (currentPage > totalPages) currentPage = totalPages;

        paginationEl.innerHTML = "";

        if (totalItems === 0) {
            paginationWrapEl.classList.add("d-none");
            if (pageInfoEl) pageInfoEl.textContent = "";
            return;
        }

        paginationWrapEl.classList.toggle("d-none", totalPages <= 1);
        const pageCardsStart = (currentPage - 1) * pageSize;
        const pageCardsEnd = pageCardsStart + pageSize;

        productCards.forEach((card) => {
            card.style.display = "none";
        });
        visibleCards.slice(pageCardsStart, pageCardsEnd).forEach((card) => {
            card.style.display = "";
        });

        if (pageInfoEl) {
            pageInfoEl.textContent = `Trang ${currentPage}/${totalPages} - ${totalItems} san pham`;
        }

        if (totalPages <= 1) return;

        const createPageItem = (label, page, disabled = false, active = false) => {
            const li = document.createElement("li");
            li.className = `page-item${disabled ? " disabled" : ""}${active ? " active" : ""}`;

            const btn = document.createElement("button");
            btn.type = "button";
            btn.className = "page-link";
            btn.textContent = label;
            btn.disabled = disabled;
            btn.addEventListener("click", () => {
                currentPage = page;
                applyFilters(false);
                window.scrollTo({ top: document.getElementById("product-section")?.offsetTop - 90 || 0, behavior: "smooth" });
            });
            li.appendChild(btn);
            return li;
        };

        paginationEl.appendChild(createPageItem("«", Math.max(currentPage - 1, 1), currentPage === 1));
        for (let i = 1; i <= totalPages; i += 1) {
            paginationEl.appendChild(createPageItem(String(i), i, false, i === currentPage));
        }
        paginationEl.appendChild(createPageItem("»", Math.min(currentPage + 1, totalPages), currentPage === totalPages));
    };

    const applyFilters = (resetPage = true) => {
        if (productCards.length === 0) {
            if (filterEmptyState) filterEmptyState.classList.add("d-none");
            if (paginationWrapEl) paginationWrapEl.classList.add("d-none");
            return;
        }
        if (resetPage) currentPage = 1;

        const selectedColor = normalize(colorSelectEl?.value || "");
        const selectedSize = normalize(sizeSelectEl?.value || "");
        const { min: minPrice, max: maxPrice } = parsePriceRange(priceSelectEl?.value || "");
        const visibleCards = productCards.filter((card) => {
            const colors = splitValues(card.getAttribute("data-colors")).map(normalize);
            const sizes = splitValues(card.getAttribute("data-sizes")).map(normalize);
            const cardMin = toNumber(card.getAttribute("data-min-price")) ?? 0;
            const cardMax = toNumber(card.getAttribute("data-max-price")) ?? cardMin;

            const matchColor = !selectedColor || colors.includes(selectedColor);
            const matchSize = !selectedSize || sizes.includes(selectedSize);

            let matchPrice = true;
            if (minPrice !== null || maxPrice !== null) {
                const min = minPrice ?? 0;
                const max = maxPrice ?? Number.MAX_SAFE_INTEGER;
                matchPrice = cardMax >= min && cardMin <= max;
            }
            return matchColor && matchSize && matchPrice;
        });

        if (filterEmptyState) {
            filterEmptyState.classList.toggle("d-none", visibleCards.length > 0);
        }
        renderPagination(visibleCards);
    };

    const resetFilters = () => {
        if (colorSelectEl) colorSelectEl.value = "";
        if (sizeSelectEl) sizeSelectEl.value = "";
        if (priceSelectEl) priceSelectEl.value = "";
        applyFilters();
    };

    const initFilters = () => {
        const colors = uniqueFromProducts("data-colors").sort((a, b) => a.localeCompare(b, "vi"));
        const sizes = uniqueFromProducts("data-sizes").sort((a, b) => a.localeCompare(b, "vi"));
        renderSelectOptions(colorSelectEl, colors);
        renderSelectOptions(sizeSelectEl, sizes);

        colorSelectEl?.addEventListener("change", applyFilters);
        sizeSelectEl?.addEventListener("change", applyFilters);
        priceSelectEl?.addEventListener("change", applyFilters);
        resetBtn?.addEventListener("click", resetFilters);
        applyFilters();
    };

    initFilters();
})
;
document.addEventListener("click", (e) => {
    const link = e.target.closest(".dropdown-submenu > a");
    if (!link) return;

    if (window.matchMedia("(max-width: 992px)").matches) {
        e.preventDefault(); // chặn chuyển trang khi bấm cha trên mobile
        const submenu = link.nextElementSibling;
        if (submenu) submenu.classList.toggle("show");
    }
});
