import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Shield, Lock, User, ArrowRight } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const TEST_USERNAME = 'admin';
const TEST_PASSWORD = '123456';

const Login = () => {
    const [username, setUsername] = useState(TEST_USERNAME);
    const [password, setPassword] = useState(TEST_PASSWORD);
    const [error, setError] = useState('');
    const { login, user } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        if (params.get('oauth2_error')) {
            setError('Google sign in failed. Please try again.');
        }

        if (user) {
            navigate('/dashboard', { replace: true });
        }
    }, [user, navigate]);

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');

        const result = await login(username || 'admin', password);
        if (result.success) {
            navigate('/dashboard');
        } else {
            setError(result.message);
        }
    };

    const handleGoogleLogin = () => {
        const isDev = window.location.port === '5173';
        const baseUrl = isDev ? 'http://localhost:8080' : window.location.origin;
        window.location.href = `${baseUrl}/oauth2/authorization/google`;
    };

    return (
        <div style={{
            position: 'relative',
            minHeight: '100vh',
            width: '100%',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            background: '#0a0c10',
            overflow: 'hidden',
            fontFamily: "'Inter', sans-serif"
        }}>
            <div className="mesh-blob" style={{ top: '-10%', left: '-10%', width: '40vw', height: '40vw', background: 'rgba(59, 130, 246, 0.4)' }}></div>
            <div className="mesh-blob" style={{ bottom: '-10%', right: '-10%', width: '50vw', height: '50vw', background: 'rgba(139, 92, 246, 0.3)', animationDelay: '-5s' }}></div>
            <div className="mesh-blob" style={{ top: '20%', right: '10%', width: '30vw', height: '30vw', background: 'rgba(236, 72, 153, 0.2)', animationDelay: '-10s' }}></div>

            <div className="glass-panel animate-fade-in" style={{ 
                position: 'relative',
                zIndex: 1,
                width: '100%', 
                maxWidth: '400px', 
                padding: '40px', 
                margin: '20px',
                background: 'rgba(255, 255, 255, 0.03)',
                boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.5)',
                border: '1px solid rgba(255, 255, 255, 0.08)'
            }}>

                <div style={{ textAlign: 'center', marginBottom: '40px' }}>
                    <div style={{ 
                        display: 'inline-flex', 
                        padding: '12px', 
                        background: 'linear-gradient(135deg, rgba(59, 130, 246, 0.2), rgba(139, 92, 246, 0.2))', 
                        borderRadius: '12px', 
                        color: '#60a5fa', 
                        marginBottom: '20px',
                        border: '1px solid rgba(96, 165, 250, 0.3)'
                    }}>
                        <Shield size={28} />
                    </div>
                    <h1 style={{ margin: 0, fontSize: '26px', fontWeight: 800, color: 'white', letterSpacing: '-0.5px' }}>VANTAGE ADMIN</h1>
                    <p style={{ color: 'rgba(255, 255, 255, 0.5)', marginTop: '8px', fontSize: '13px' }}>Enterprise Control Interface</p>
                </div>

                <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                    <div style={{ position: 'relative' }}>
                        <span style={{ 
                            position: 'absolute', 
                            top: '50%', 
                            left: '16px', 
                            transform: 'translateY(-50%)', 
                            color: 'rgba(255, 255, 255, 0.4)',
                            display: 'flex',
                            alignItems: 'center',
                            zIndex: 2
                        }}>
                            <User size={18} />
                        </span>
                        <input
                            type="text"
                            placeholder="Username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                            className="glass-input"
                            style={{ 
                                width: '100%',
                                padding: '14px 16px 14px 48px',
                                borderRadius: '12px',
                                outline: 'none',
                                fontSize: '14px'
                            }}
                        />
                    </div>

                    <div style={{ position: 'relative' }}>
                        <span style={{ 
                            position: 'absolute', 
                            top: '50%', 
                            left: '16px', 
                            transform: 'translateY(-50%)', 
                            color: 'rgba(255, 255, 255, 0.4)',
                            display: 'flex',
                            alignItems: 'center',
                            zIndex: 2
                        }}>
                            <Lock size={18} />
                        </span>
                        <input
                            type="password"
                            placeholder="Password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            className="glass-input"
                            style={{ 
                                width: '100%',
                                padding: '14px 16px 14px 48px',
                                borderRadius: '12px',
                                outline: 'none',
                                fontSize: '14px'
                            }}
                        />
                    </div>

                    {error && (
                        <div style={{ color: '#ef4444', fontSize: '13px', textAlign: 'center', padding: '8px', background: 'rgba(239, 68, 68, 0.1)', borderRadius: '8px', border: '1px solid rgba(239, 68, 68, 0.2)' }}>
                            {error}
                        </div>
                    )}

                    <button 
                        type="submit" 
                        className="btn"
                        style={{ 
                            width: '100%', 
                            padding: '16px', 
                            marginTop: '8px',
                            background: 'linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%)',
                            color: 'white',
                            borderRadius: '12px',
                            fontWeight: 700,
                            letterSpacing: '0.5px',
                            boxShadow: '0 10px 15px -3px rgba(59, 130, 246, 0.3)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            gap: '10px',
                            cursor: 'pointer',
                            transition: 'all 0.3s ease',
                            border: '2px solid #fbbf24'
                        }}
                        onMouseEnter={(e) => {
                            e.currentTarget.style.transform = 'translateY(-2px)';
                            e.currentTarget.style.boxShadow = '0 15px 25px -5px rgba(59, 130, 246, 0.4)';
                        }}
                        onMouseLeave={(e) => {
                            e.currentTarget.style.transform = 'translateY(0)';
                            e.currentTarget.style.boxShadow = '0 10px 15px -3px rgba(59, 130, 246, 0.3)';
                        }}
                    >
                        SIGN IN <ArrowRight size={18} />
                    </button>
                </form>

                <div style={{ display: 'flex', alignItems: 'center', margin: '24px 0', gap: '12px' }}>
                    <div style={{ flex: 1, height: '1px', background: 'rgba(255, 255, 255, 0.1)' }}></div>
                    <span style={{ fontSize: '12px', color: 'rgba(255, 255, 255, 0.3)', whiteSpace: 'nowrap' }}>or continue with</span>
                    <div style={{ flex: 1, height: '1px', background: 'rgba(255, 255, 255, 0.1)' }}></div>
                </div>

                <button
                    onClick={handleGoogleLogin}
                    style={{ 
                        width: '100%',
                        padding: '14px',
                        background: 'rgba(255, 255, 255, 0.05)',
                        border: '1px solid rgba(255, 255, 255, 0.1)',
                        borderRadius: '12px',
                        color: 'white',
                        fontSize: '14px',
                        fontWeight: 600,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: '10px',
                        cursor: 'pointer',
                        transition: 'all 0.3s ease'
                    }}
                    onMouseEnter={(e) => {
                        e.currentTarget.style.background = 'rgba(255, 255, 255, 0.1)';
                    }}
                    onMouseLeave={(e) => {
                        e.currentTarget.style.background = 'rgba(255, 255, 255, 0.05)';
                    }}
                >
                    <svg width="18" height="18" viewBox="0 0 24 24">
                        <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z"/>
                        <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                        <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                        <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                    </svg>
                    Sign in with Google
                </button>

                <div style={{ marginTop: '32px', textAlign: 'center' }}>
                    <p style={{ fontSize: '12px', color: 'rgba(255, 255, 255, 0.3)', margin: 0 }}>
                        &copy; 2024 Vantage Systems. Professional Edition.
                    </p>
                </div>
            </div>
        </div>
    );
};

export default Login;
