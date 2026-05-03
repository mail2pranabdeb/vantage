import { useEffect } from 'react';
import { setTokens } from '../services/api';

function extractCode() {
    const params = new URLSearchParams(window.location.search);
    return params.get('code');
}

const exchangeCode = extractCode();
if (exchangeCode) {
    console.log('[OAuth2Callback] Found exchange code, fetching tokens...');
}

const OAuth2Callback = () => {
    useEffect(() => {
        const code = new URLSearchParams(window.location.search).get('code');
        if (code) {
            fetch(`/api/oauth2/exchange?code=${code}`)
                .then(res => res.json())
                .then(data => {
                    if (data.code === 200 && data.token && data.refreshToken) {
                        setTokens(data.token, data.refreshToken);
                        window.location.href = '/dashboard';
                    } else {
                        console.error('[OAuth2Callback] Token exchange failed:', data);
                        window.location.href = '/login';
                    }
                })
                .catch(err => {
                    console.error('[OAuth2Callback] Token exchange error:', err);
                    window.location.href = '/login';
                });
        } else {
            const token = sessionStorage.getItem('jwt_token');
            if (token) {
                window.location.href = '/dashboard';
            } else {
                window.location.href = '/login';
            }
        }
    }, []);

    return (
        <div style={{
            minHeight: '100vh',
            width: '100%',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            background: '#0a0c10',
            color: 'white',
            fontFamily: "'Inter', sans-serif"
        }}>
            <div style={{ textAlign: 'center' }}>
                <div className="pulse-dot" style={{ color: 'var(--primary-color)', marginBottom: '16px' }}></div>
                <p style={{ color: 'rgba(255, 255, 255, 0.6)', fontSize: '14px' }}>Completing sign in...</p>
            </div>
        </div>
    );
};

export default OAuth2Callback;
