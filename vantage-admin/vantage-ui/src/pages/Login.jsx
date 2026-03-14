import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Shield, Lock, User } from 'lucide-react';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const formData = new URLSearchParams();
            formData.append('username', username);
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
            minHeight: '100vh',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            background: 'linear-gradient(135deg, var(--bg-primary) 0%, rgba(59, 130, 246, 0.1) 100%)'
        }}>
            <div className="glass-panel animate-fade-in" style={{ width: '100%', maxWidth: '420px', padding: '48px', margin: '20px' }}>

                <div style={{ textAlign: 'center', marginBottom: '32px' }}>
                    <div style={{ display: 'inline-flex', padding: '16px', backgroundColor: 'rgba(59, 130, 246, 0.1)', borderRadius: '16px', color: 'var(--primary-color)', marginBottom: '16px' }}>
                        <Shield size={32} />
                    </div>
                    <h1 style={{ margin: 0, fontSize: '28px' }}>Login to BMS</h1>
                    <p style={{ color: 'var(--text-muted)', marginTop: '8px' }}>Open Source Management System</p>
                </div>

                <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                    <div style={{ position: 'relative' }}>
                        <User size={18} style={{ position: 'absolute', top: '50%', left: '16px', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                        <input
                            type="text"
                            name="username"
                            className="input"
                            placeholder="Username (e.g. admin)"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                            value="admin"
                            style={{ paddingLeft: '44px' }}
                        />
                    </div>

                    <div style={{ position: 'relative' }}>
                        <Lock size={18} style={{ position: 'absolute', top: '50%', left: '16px', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                        <input
                            type="password"
                            name="password"
                            className="input"
                            placeholder="Password (e.g. 123456)"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            value="123456"
                            style={{ paddingLeft: '44px' }}
                        />
                    </div>

                    <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '14px', marginTop: '8px' }} disabled={loading}>
                        {loading ? 'Authenticating...' : 'Sign In'}
                    </button>
                </form>

            </div>
        </div>
    );
};

export default Login;
