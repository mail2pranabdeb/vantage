const TOKEN_KEY = 'jwt_token';
const REFRESH_KEY = 'jwt_refresh';

function getAccessToken() {
    return sessionStorage.getItem(TOKEN_KEY);
}

function getRefreshToken() {
    return sessionStorage.getItem(REFRESH_KEY);
}

function setTokens(token, refreshToken) {
    sessionStorage.setItem(TOKEN_KEY, token);
    sessionStorage.setItem(REFRESH_KEY, refreshToken);
}

function clearTokens() {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(REFRESH_KEY);
}

let isRefreshing = false;
let refreshSubscribers = [];

function onTokenRefreshed(token) {
    refreshSubscribers.forEach(cb => cb(token));
    refreshSubscribers = [];
}

function addRefreshSubscriber(cb) {
    refreshSubscribers.push(cb);
}

async function doRefreshToken() {
    const rt = getRefreshToken();
    if (!rt) throw new Error('No refresh token');
    const _res = await window._originalFetch('/api/login/refresh', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: rt })
    });
    if (!_res.ok) throw new Error('Refresh failed');
    const data = await _res.json();
    if (data.code === 200) {
        setTokens(data.data.token, data.data.refreshToken);
        return data.data.token;
    }
    throw new Error('Refresh failed');
}

function setupAuthInterceptor() {
    const originalFetch = window.fetch;
    window._originalFetch = originalFetch;

    window.fetch = async function(url, options = {}) {
        if (typeof url !== 'string' || !url.startsWith('/api')) {
            return originalFetch(url, options);
        }

        const token = getAccessToken();
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        let res = await originalFetch(url, { ...options, headers });

        if (res.status === 401 && !options._retry) {
            if (isRefreshing) {
                const newToken = await new Promise(resolve => addRefreshSubscriber(resolve));
                headers['Authorization'] = `Bearer ${newToken}`;
                res = await originalFetch(url, { ...options, headers });
            } else {
                isRefreshing = true;
                try {
                    const newToken = await doRefreshToken();
                    onTokenRefreshed(newToken);
                    headers['Authorization'] = `Bearer ${newToken}`;
                    res = await originalFetch(url, { ...options, headers, _retry: true });
                } catch {
                    clearTokens();
                    window.location.href = '/login';
                    throw new Error('Session expired');
                } finally {
                    isRefreshing = false;
                }
            }
        }

        return res;
    };
}

export { getAccessToken, setTokens, clearTokens, setupAuthInterceptor };
