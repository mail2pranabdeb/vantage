import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Shield, Lock, User, ArrowRight } from 'lucide-react';

// TEMPORARY TESTING - Auto-fill credentials
const TEST_USERNAME = 'admin';
const TEST_PASSWORD = '123456';

const Login = () => {
    const [username, setUsername] = useState(TEST_USERNAME);
    const [password, setPassword] = useState(TEST_PASSWORD);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const formData = new URLSearchParams();
            formData.append('username', username || 'admin');
            formData.append('password', password);

            const response = await fetch('/api/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: formData
            });

            if (response.ok) {
                navigate('/dashboard');
            } else {
                alert('Invalid credentials');
            }
        } catch (error) {
            console.error('Login error', error);
            alert('An error occurred during login');
        } finally {
            setLoading(false);
        }
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
            {/* Animated Mesh Background */}
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

                    <button 
                        type="submit" 
                        disabled={loading}
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
                        {loading ? 'AUTHENTICATING...' : (
                            <>
                                SIGN IN <ArrowRight size={18} />
                            </>
                        )}
                    </button>
                </form>

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
