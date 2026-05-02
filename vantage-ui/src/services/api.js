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

async function refreshToken() {
    const rt = getRefreshToken();
    if (!rt) throw new Error('No refresh token');
    const _res = await fetch('/api/login/refresh', {
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
    window.fetch = async function(url, options = {}) {
        if (typeof url === 'string' && url.startsWith('/api')) {
            const token = getAccessToken();
            if (token) {
                options.headers = {
                    'Authorization': `Bearer ${token}`,
                    ...(options.headers || {})
                };
            }

            let res = await originalFetch(url, options);

            if (res.status === 401 && !options._retry) {
                if (isRefreshing) {
                    const newToken = await new Promise(resolve => addRefreshSubscriber(resolve));
                    options.headers['Authorization'] = `Bearer ${newToken}`;
                    res = await originalFetch(url, options);
                } else {
                    isRefreshing = true;
                    try {
                        const newToken = await refreshToken();
                        onTokenRefreshed(newToken);
                        options.headers = {
                            ...options.headers,
                            'Authorization': `Bearer ${newToken}`
                        };
                        options._retry = true;
                        res = await originalFetch(url, options);
                    } catch {
                        clearTokens();
                        window.location.href = '/login';
                        throw new Error('Session expired');
                    } finally {
                        isRefreshing = false;
                    }
                }
            }
        }
        return res;
    };
}

export { getAccessToken, setTokens, clearTokens, setupAuthInterceptor };
