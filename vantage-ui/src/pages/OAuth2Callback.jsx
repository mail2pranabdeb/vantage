import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { setTokens } from '../services/api';

const OAuth2Callback = () => {
    const navigate = useNavigate();

    useEffect(() => {
        const hash = window.location.hash;
        if (hash.startsWith('#token=')) {
            const params = new URLSearchParams(hash.substring(1));
            const token = params.get('token');
            const refresh = params.get('refresh');
            if (token && refresh) {
                setTokens(token, refresh);
                window.location.hash = '';
                navigate('/dashboard', { replace: true });
            } else {
                navigate('/login', { replace: true });
            }
        } else {
            navigate('/login', { replace: true });
        }
    }, [navigate]);

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
