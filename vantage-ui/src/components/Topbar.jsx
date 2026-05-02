import { useState, useRef, useEffect } from 'react';
import { LogOut as LogOutIcon, Palette as PaletteIcon, Check } from 'lucide-react';
import NotificationCenter from './NotificationCenter';
import { useAuth } from '../context/AuthContext';

const themes = [
    { id: 'sap', label: 'SAP GUI', dot: '#0a6ed1' },
    { id: 'light', label: 'Light', dot: '#3b82f6' },
    { id: 'dark', label: 'Dark', dot: '#334155' },
    { id: 'midnight', label: 'Midnight', dot: '#818cf8' },
    { id: 'emerald', label: 'Emerald', dot: '#059669' },
    { id: 'rose', label: 'Rose', dot: '#e11d48' },
];

const Topbar = () => {
    const { logout, user } = useAuth();
    const [themeOpen, setThemeOpen] = useState(false);
    const [currentTheme, setCurrentTheme] = useState(() => localStorage.getItem('bms-theme') || 'sap');
    const pickerRef = useRef(null);

    useEffect(() => {
        document.documentElement.setAttribute('data-theme', currentTheme);
        localStorage.setItem('bms-theme', currentTheme);
    }, [currentTheme]);

    useEffect(() => {
        const saved = localStorage.getItem('bms-theme');
        if (saved) {
            document.documentElement.setAttribute('data-theme', saved);
            setCurrentTheme(saved);
        }
    }, []);

    useEffect(() => {
        const handler = (e) => {
            if (pickerRef.current && !pickerRef.current.contains(e.target)) setThemeOpen(false);
        };
        document.addEventListener('mousedown', handler);
        return () => document.removeEventListener('mousedown', handler);
    }, []);

    const iconBtn = { background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', display: 'flex', alignItems: 'center', padding: '4px', borderRadius: '4px', transition: 'color 0.15s' };

    return (
        <header className="topbar" style={{ margin: '8px 12px 0', borderRadius: '6px', borderBottom: 'none', background: 'var(--glass-bg)', border: '1px solid var(--glass-border)' }}>
            <h3 style={{ margin: 0, fontSize: '11px', fontWeight: 500, color: 'var(--text-muted)' }}>
                Welcome <span style={{ color: 'var(--text-primary)', fontWeight: 600 }}>{user?.username || 'User'}</span>
            </h3>

            <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <div className="theme-picker" ref={pickerRef}>
                    <button onClick={() => setThemeOpen(!themeOpen)} style={iconBtn} title="Change theme">
                        <PaletteIcon size={14} />
                    </button>
                    {themeOpen && (
                        <div className="theme-picker-menu animate-fade-in">
                            <div style={{ padding: '4px 8px 6px', fontSize: '10px', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Theme</div>
                            {themes.map(t => (
                                <button key={t.id} className="theme-option" onClick={() => { setCurrentTheme(t.id); setThemeOpen(false); }}>
                                    <span className="theme-dot" style={{ background: t.dot, borderColor: currentTheme === t.id ? t.dot : 'var(--border-color)' }} />
                                    <span style={{ flex: 1 }}>{t.label}</span>
                                    {currentTheme === t.id && <Check size={12} style={{ color: t.dot }} />}
                                </button>
                            ))}
                        </div>
                    )}
                </div>

                <NotificationCenter />

                <div style={{ width: '1px', height: '16px', background: 'var(--border-color)', margin: '0 4px' }} />

                <button onClick={logout} style={{ ...iconBtn, color: '#ef4444', gap: '4px', fontSize: '11px' }} title="Logout">
                    <LogOutIcon size={13} /> <span>Logout</span>
                </button>
            </div>
        </header>
    );
};

export default Topbar;
