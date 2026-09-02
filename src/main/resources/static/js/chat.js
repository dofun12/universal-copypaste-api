(function () {
    const code = document.body.dataset.chatCode;
    const messagesEl = document.getElementById('messages');
    const emptyStateEl = document.getElementById('empty-state');
    const composer = document.getElementById('composer');
    const input = document.getElementById('message-input');
    const copyBtn = document.getElementById('copy-code-btn');
    const banner = document.getElementById('connection-banner');

    const renderedIds = new Set();

    function showBanner(message) {
        banner.textContent = message;
        banner.classList.remove('d-none');
    }

    function hideBanner() {
        banner.classList.add('d-none');
    }

    function formatTime(iso) {
        const date = new Date(iso);
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }

    function appendMessage(message) {
        if (renderedIds.has(message.id)) {
            return;
        }
        renderedIds.add(message.id);
        emptyStateEl.classList.add('d-none');

        const wrapper = document.createElement('div');
        wrapper.className = 'd-flex mb-2';

        const bubble = document.createElement('div');
        bubble.className = 'message-bubble bg-white border rounded-3 px-3 py-2 shadow-sm';
        bubble.innerHTML = message.renderedContent;

        const time = document.createElement('div');
        time.className = 'message-time text-muted mt-1';
        time.textContent = formatTime(message.createdAt);
        bubble.appendChild(time);

        wrapper.appendChild(bubble);
        messagesEl.appendChild(wrapper);
        messagesEl.scrollTop = messagesEl.scrollHeight;
    }

    async function loadInitialMessages() {
        try {
            const response = await fetch(`/api/chats/${encodeURIComponent(code)}/messages`);
            if (!response.ok) {
                throw new Error('not found');
            }
            const page = await response.json();
            const messages = page.content || [];
            messages.forEach(appendMessage);
        } catch (err) {
            showBanner('This chat code was not found or has expired.');
        }
    }

    async function sendMessage(content) {
        const response = await fetch(`/api/chats/${encodeURIComponent(code)}/messages`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content })
        });
        if (!response.ok) {
            throw new Error('Failed to send message');
        }
        // The server also broadcasts this message over WebSocket; if the socket
        // is connected we'll get a duplicate push, which appendMessage dedupes
        // by id. We still render immediately here for instant feedback.
        const message = await response.json();
        appendMessage(message);
    }

    function connectRealtime() {
        const socket = new SockJS('/ws');
        const client = new StompJs.Client({
            webSocketFactory: () => socket,
            reconnectDelay: 3000,
            onConnect: () => {
                hideBanner();
                client.subscribe(`/topic/chat/${code}`, (frame) => {
                    const message = JSON.parse(frame.body);
                    appendMessage(message);
                });
            },
            onWebSocketClose: () => showBanner('Reconnecting…'),
            onStompError: () => showBanner('Connection error, retrying…')
        });
        client.activate();
    }

    composer.addEventListener('submit', async (event) => {
        event.preventDefault();
        const content = input.value.trim();
        if (!content) {
            return;
        }
        input.value = '';
        try {
            await sendMessage(content);
        } catch (err) {
            showBanner('Could not send message. Please try again.');
        }
    });

    input.addEventListener('keydown', (event) => {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            composer.requestSubmit();
        }
    });

    copyBtn.addEventListener('click', async () => {
        try {
            await navigator.clipboard.writeText(code);
            copyBtn.textContent = 'Copied!';
            setTimeout(() => { copyBtn.textContent = 'Copy'; }, 1500);
        } catch (err) {
            // Clipboard API may be unavailable (e.g. insecure context); ignore.
        }
    });

    loadInitialMessages().then(connectRealtime);
})();
