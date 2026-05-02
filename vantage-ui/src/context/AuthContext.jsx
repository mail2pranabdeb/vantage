import { createContext, useContext, useState, useEffect } from 'react';
import { getAccessToken, setTokens, clearTokens } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const token = getAccessToken();
        if (token) {
            fetch('/api/me', {
                headers: { 'Authorization': `Bearer ${token}` }
            })
                .then(res => res.json())
                .then(data => {
                    if (data.code === 200) {
                        setUser(data.data);
                    } else {
                        clearTokens();
                    }
                })
                .catch(() => clearTokens())
                .finally(() => setLoading(false));
        } else {
            setLoading(false);
        }
    }, []);

    const login = async (username, password) => {
        const res = await fetch('/api/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        const data = await res.json();
        if (data.code === 200) {
            setTokens(data.data.token, data.data.refreshToken);
            setUser({ username: data.data.username });
            return { success: true };
        }
        return { success: false, message: data.msg || 'Invalid credentials' };
    };

    const logout = async () => {
        try {
            await fetch('/api/logout', { method: 'POST' });
        } catch { }
        clearTokens();
        setUser(null);
        window.location.href = '/login';
    };

    const value = { user, loading, login, logout };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error('useAuth must be used within AuthProvider');
    return ctx;
}
