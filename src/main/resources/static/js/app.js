(function () {
    const createBtn = document.getElementById('create-chat-btn');
    const createError = document.getElementById('create-error');
    const createdChatCard = document.getElementById('created-chat-card');
    const createdChatCode = document.getElementById('created-chat-code');
    const createdChatLink = document.getElementById('created-chat-link');
    const createdChatQr = document.getElementById('created-chat-qr');
    const openChatBtn = document.getElementById('open-chat-btn');
    const joinForm = document.getElementById('join-form');
    const joinInput = document.getElementById('join-code-input');
    const joinError = document.getElementById('join-error');

    function showError(el, message) {
        el.textContent = message;
        el.classList.remove('d-none');
    }

    createBtn.addEventListener('click', async () => {
        createBtn.disabled = true;
        createError.classList.add('d-none');
        try {
            const response = await fetch('/api/chats', { method: 'POST' });
            if (!response.ok) {
                throw new Error('Failed to create chat (' + response.status + ')');
            }
            const chat = await response.json();
            const chatPath = '/chat/' + encodeURIComponent(chat.code);
            const chatUrl = new URL(chatPath, window.location.href).toString();
            const qrUrl = new URL('https://api.qrserver.com/v1/create-qr-code/');
            qrUrl.searchParams.set('size', '220x220');
            qrUrl.searchParams.set('data', chatUrl);

            createdChatCode.textContent = chat.code;
            createdChatLink.textContent = chatUrl;
            createdChatLink.href = chatUrl;
            createdChatQr.src = qrUrl.toString();
            openChatBtn.href = chatPath;
            createdChatCard.classList.remove('d-none');
            createdChatCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
            createBtn.disabled = false;
        } catch (err) {
            showError(createError, 'Could not create a chat. Please try again.');
            createBtn.disabled = false;
        }
    });

    joinForm.addEventListener('submit', (event) => {
        event.preventDefault();
        const code = joinInput.value.trim().toUpperCase();
        if (!code) {
            showError(joinError, 'Enter a chat code first.');
            return;
        }
        window.location.href = '/chat/' + encodeURIComponent(code);
    });

    joinInput.addEventListener('input', () => {
        joinInput.value = joinInput.value.toUpperCase();
    });
})();
