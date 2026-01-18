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

    // Update color swatches
    document.querySelectorAll(".color-swatch").forEach((swatch) => {
        const colorName = swatch.dataset.colorName || swatch.getAttribute("data-color-name") || "";
        const key = normalize(colorName);

        let cssColor = "gray";
        if (colorMap.has(key)) {
            cssColor = colorMap.get(key);
        } else {
            // nếu key dài hơn (vd: "xanh navy dam"), thử match theo startsWith
            for (const [k, v] of colorMap.entries()) {
                if (key.startsWith(k)) {
                    cssColor = v;
                    break;
                }
            }
        }

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
