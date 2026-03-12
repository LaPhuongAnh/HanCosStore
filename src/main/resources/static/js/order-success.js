document.addEventListener('DOMContentLoaded', function() {
    const bankCode = 'MB';
    fetch('https://api.vietqr.io/v2/banks')
        .then(res => res.json())
        .then(data => {
            if (data.code === '00') {
                const bank = data.data.find(b => b.code === bankCode);
                if (bank) {
                    const bankNameEl = document.getElementById('shop-bank-name');
                    const bankLogoEl = document.getElementById('shop-bank-logo');

                    bankNameEl.innerText = bank.name + ' (' + bank.shortName + ')';
                    bankLogoEl.src = bank.logo;
                    bankLogoEl.style.display = 'inline-block';
                }
            }
        })
        .catch(err => console.error('Error fetching banks:', err));
});
