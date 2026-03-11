import { useState, useRef, useEffect } from 'react';
import { Bell as BellIcon, LogOut as LogOutIcon, Palette as PaletteIcon, Check } from 'lucide-react';

const themes = [
    { id: 'light', label: 'Light', dot: '#3b82f6' },
    { id: 'dark', label: 'Dark', dot: '#334155' },
    { id: 'midnight', label: 'Midnight', dot: '#818cf8' },
    { id: 'emerald', label: 'Emerald', dot: '#059669' },
    { id: 'rose', label: 'Rose', dot: '#e11d48' },
];

const Topbar = () => {
    const [themeOpen, setThemeOpen] = useState(false);
    const [currentTheme, setCurrentTheme] = useState(() => localStorage.getItem('bms-theme') || 'light');
    const pickerRef = useRef(null);

    useEffect(() => {
        document.documentElement.setAttribute('data-theme', currentTheme);
        localStorage.setItem('bms-theme', currentTheme);
    }, [currentTheme]);

    // Load saved theme on mount
    useEffect(() => {
        const saved = localStorage.getItem('bms-theme');
        if (saved) {
            document.documentElement.setAttribute('data-theme', saved);
            setCurrentTheme(saved);
        }
    }, []);

    // Close picker on outside click
    useEffect(() => {
        const handler = (e) => {
            if (pickerRef.current && !pickerRef.current.contains(e.target)) setThemeOpen(false);
        };
        document.addEventListener('mousedown', handler);
        return () => document.removeEventListener('mousedown', handler);
    }, []);

    const handleLogout = async () => {
        await fetch('/api/logout', { method: 'POST' });
        window.location.href = '/login';
    };

    const iconBtn = { background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', display: 'flex', alignItems: 'center', padding: '4px', borderRadius: '4px', transition: 'color 0.15s' };

    return (
        <header className="topbar" style={{ margin: '8px 12px 0', borderRadius: '6px', borderBottom: 'none', background: 'var(--glass-bg)', border: '1px solid var(--glass-border)' }}>
            <h3 style={{ margin: 0, fontSize: '12px', fontWeight: 500, color: 'var(--text-muted)' }}>
                Welcome to <span style={{ color: 'var(--text-primary)', fontWeight: 600 }}>Vantage Admin</span>
            </h3>

            <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                {/* Theme Picker */}
                <div className="theme-picker" ref={pickerRef}>
                    <button onClick={() => setThemeOpen(!themeOpen)} style={iconBtn} title="Change theme">
                        <PaletteIcon size={15} />
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

                <button style={iconBtn} title="Notifications">
                    <BellIcon size={15} />
                </button>

                <div style={{ width: '1px', height: '16px', background: 'var(--border-color)', margin: '0 4px' }} />

                <button onClick={handleLogout} style={{ ...iconBtn, color: '#ef4444', gap: '4px', fontSize: '12px' }} title="Logout">
                    <LogOutIcon size={14} /> <span>Logout</span>
                </button>
            </div>
        </header>
    );
};

export default Topbar;
