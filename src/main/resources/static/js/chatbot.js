(() => {
    const widget = document.getElementById('chatbot-widget');
    if (!widget) return;

    const toggle = document.getElementById('chatbotToggle');
    const panel = document.getElementById('chatbotPanel');
    const messagesEl = document.getElementById('chatbotMessages');
    const inputEl = document.getElementById('chatbotInput');
    const sendBtn = document.getElementById('chatbotSend');

    let greeted = false;

    const chips = Array.from(document.querySelectorAll('.chatbot-chip'));

    const escapeHtml = (text) => {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text.replace(/[&<>"']/g, (m) => map[m]);
    };

    const linkify = (text) => {
        let html = escapeHtml(text).replace(/\n/g, '<br>');
        const patterns = [
            /https?:\/\/[^\s]+/g,
            /\/(products\/\d+)/g,
            /\/(order\/my-orders\/\d+)/g,
            /\/(order\/my-orders)\b/g,
            /\/(chinh-sach-doi-tra)\b/g,
            /\/(login)\b/g
        ];

        patterns.forEach((pattern) => {
            html = html.replace(pattern, (match) => {
                const href = match.startsWith('http') ? match : match;
                return `<a href="${href}">${match}</a>`;
            });
        });

        return html;
    };

    const addMessage = (text, sender) => {
        const msg = document.createElement('div');
        msg.className = `chatbot-message ${sender}`;
        msg.innerHTML = linkify(text);
        messagesEl.appendChild(msg);
        messagesEl.scrollTop = messagesEl.scrollHeight;
        return msg;
    };

    const setTyping = () => addMessage('Đang trả lời...', 'bot');

    const sendMessage = async (text) => {
        const content = text.trim();
        if (!content) return;
        inputEl.value = '';
        addMessage(content, 'user');
        const typingEl = setTyping();

        try {
            const response = await fetch('/api/chatbot', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify({ message: content })
            });

            if (!response.ok) {
                throw new Error('Request failed');
            }

            const data = await response.json();
            typingEl.remove();
            addMessage(data.reply || 'Mình chưa nhận được phản hồi, bạn thử lại nhé.', 'bot');
        } catch (error) {
            typingEl.remove();
            addMessage('Có lỗi khi kết nối. Bạn thử lại sau nhé.', 'bot');
        }
    };

    toggle.addEventListener('click', () => {
        panel.classList.toggle('open');
        if (panel.classList.contains('open') && !greeted) {
            addMessage('Xin chào! Mình có thể tư vấn sản phẩm, hỗ trợ tìm đơn và chính sách đổi trả.', 'bot');
            greeted = true;
        }
    });

    sendBtn.addEventListener('click', () => sendMessage(inputEl.value));

    inputEl.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            sendMessage(inputEl.value);
        }
    });

    chips.forEach((chip) => {
        chip.addEventListener('click', () => sendMessage(chip.dataset.message || chip.textContent));
    });
})();
