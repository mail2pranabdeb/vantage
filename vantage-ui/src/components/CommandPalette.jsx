import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Search, Command, Zap, Users, Shield, Calendar, Settings, Clock, Layout, X, FileText, UserCheck, Globe } from 'lucide-react';

const NAV_COMMANDS = [
    { id: 'dashboard', title: 'Go to Dashboard', icon: Layout, category: 'Navigation', url: '/dashboard' },
    { id: 'system:user:list', title: 'Manage Users', icon: Users, category: 'System', url: '/system/user' },
    { id: 'system:role:list', title: 'Role Permissions', icon: Shield, category: 'System', url: '/system/role' },
    { id: 'system:job:list', title: 'Scheduled Jobs', icon: Calendar, category: 'Quartz', url: '/system/job' },
    { id: 'system:jobLog:list', title: 'Job Execution Logs', icon: Clock, category: 'Quartz', url: '/system/jobLog' },
    { id: 'system:config:list', title: 'System Configuration', icon: Settings, category: 'System', url: '/system/config' },
    { id: 'system:job:logs', title: 'Real-time Log Stream', icon: Zap, category: 'Monitoring', url: '/system/live-logs' },
];

const TYPE_ICONS = {
    User: Users,
    Role: Shield,
    Menu: Layout,
    Config: Settings,
    Notice: FileText,
    Job: Calendar,
};

const CommandPalette = ({ onNavigate }) => {
    const [isOpen, setIsOpen] = useState(false);
    const [query, setQuery] = useState('');
    const [selectedIndex, setSelectedIndex] = useState(0);
    const [searchResults, setSearchResults] = useState([]);
    const [loading, setLoading] = useState(false);
    const inputRef = useRef(null);
    const timerRef = useRef(null);

    useEffect(() => {
        const handleKeyDown = (e) => {
            if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
                e.preventDefault();
                setIsOpen(prev => !prev);
            }
            if (e.key === 'Escape') {
                setIsOpen(false);
            }
        };
        const handleToggle = () => setIsOpen(prev => !prev);
        window.addEventListener('keydown', handleKeyDown);
        window.addEventListener('opencode:toggle-search', handleToggle);
        return () => {
            window.removeEventListener('keydown', handleKeyDown);
            window.removeEventListener('opencode:toggle-search', handleToggle);
        };
    }, []);

    useEffect(() => {
        if (isOpen && inputRef.current) inputRef.current.focus();
    }, [isOpen]);

    useEffect(() => {
        if (timerRef.current) clearTimeout(timerRef.current);
        if (!query.trim()) { setSearchResults([]); setLoading(false); return; }
        setLoading(true);
        timerRef.current = setTimeout(() => {
            fetch(`/api/system/search?q=${encodeURIComponent(query.trim())}&maxPerType=5`)
                .then(res => res.json())
                .then(data => {
                    if (data.code === 200) setSearchResults(data.data || []);
                    else setSearchResults([]);
                })
                .catch(() => setSearchResults([]))
                .finally(() => setLoading(false));
        }, 250);
        return () => { if (timerRef.current) clearTimeout(timerRef.current); };
    }, [query]);

    const getFilteredCommands = useCallback(() => {
        if (!query.trim()) return { commands: NAV_COMMANDS, results: [] };
        const filtered = NAV_COMMANDS.filter(cmd =>
            cmd.title.toLowerCase().includes(query.toLowerCase()) ||
            cmd.category.toLowerCase().includes(query.toLowerCase())
        );
        return { commands: filtered, results: searchResults };
    }, [query, searchResults]);

    const allItems = getFilteredCommands();
    const combined = [
        ...allItems.commands.map(c => ({ ...c, _type: 'nav' })),
        ...allItems.results.map(r => ({ _type: 'record', ...r })),
    ];

    const handleSelect = (item) => {
        if (item._type === 'nav') {
            onNavigate({
                id: item.id,
                title: item.title.replace('Go to ', '').replace('Manage ', ''),
                url: item.url,
                icon: '⚡'
            });
        } else {
            onNavigate({
                id: `${item.type}:${item.id}`,
                title: item.label,
                url: item.url,
                icon: '🔍'
            });
        }
        setIsOpen(false);
        setQuery('');
        setSelectedIndex(0);
    };

    const handleKeyDown = (e) => {
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            setSelectedIndex(prev => (prev + 1) % (combined.length || 1));
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            setSelectedIndex(prev => (prev - 1 + (combined.length || 1)) % (combined.length || 1));
        } else if (e.key === 'Enter') {
            if (combined[selectedIndex]) handleSelect(combined[selectedIndex]);
        }
    };

    if (!isOpen) return null;

    return (
        <div style={{
            position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh',
            background: 'rgba(0, 0, 0, 0.4)', backdropFilter: 'blur(8px)',
            zIndex: 9999, display: 'flex', justifyContent: 'center', paddingTop: '15vh'
        }} onClick={() => setIsOpen(false)}>
            <div
                className="glass-panel animate-up"
                style={{
                    width: '100%', maxWidth: '600px', height: 'fit-content',
                    padding: 0, overflow: 'hidden', border: '1px solid var(--glass-border)',
                    boxShadow: '0 24px 80px rgba(0,0,0,0.4)',
                    background: 'var(--bg-secondary)'
                }}
                onClick={e => e.stopPropagation()}
            >
                <div style={{
                    display: 'flex', alignItems: 'center', padding: '16px',
                    borderBottom: '1px solid var(--border-color)', gap: '12px'
                }}>
                    <Search size={20} style={{ color: 'var(--text-muted)' }} />
                    <input
                        ref={inputRef}
                        type="text"
                        placeholder="Search commands, users, roles, menus... (Ctrl+K)"
                        value={query}
                        onChange={e => { setQuery(e.target.value); setSelectedIndex(0); }}
                        onKeyDown={handleKeyDown}
                        style={{
                            flex: 1, border: 'none', background: 'transparent',
                            color: 'var(--text-primary)', fontSize: '16px', outline: 'none'
                        }}
                    />
                    {loading && (
                        <div style={{ width: 16, height: 16, border: '2px solid var(--border-color)', borderTopColor: 'var(--primary-color)', borderRadius: '50%', animation: 'spin 0.6s linear infinite' }} />
                    )}
                    <div style={{
                        display: 'flex', alignItems: 'center', gap: '4px',
                        padding: '4px 8px', borderRadius: '4px', background: 'var(--bg-tertiary)',
                        fontSize: '10px', color: 'var(--text-muted)', fontWeight: 700
                    }}>
                        ESC
                    </div>
                </div>

                <div style={{ maxHeight: '400px', overflowY: 'auto', padding: '8px' }}>
                    {combined.length > 0 ? combined.map((item, index) => {
                        const Icon = item._type === 'nav' ? item.icon : (TYPE_ICONS[item.type] || Globe);
                        return (
                            <div
                                key={item._type === 'nav' ? item.id : `${item.type}:${item.id}`}
                                onClick={() => handleSelect(item)}
                                onMouseEnter={() => setSelectedIndex(index)}
                                style={{
                                    display: 'flex', alignItems: 'center', gap: '12px',
                                    padding: '12px 16px', borderRadius: '10px', cursor: 'pointer',
                                    background: index === selectedIndex ? 'var(--primary-soft)' : 'transparent',
                                    transition: 'all 0.2s ease'
                                }}
                            >
                                <div style={{
                                    width: '32px', height: '32px', borderRadius: '8px',
                                    background: index === selectedIndex ? 'var(--primary-color)' : (
                                        item._type === 'record' ? '#10b981' : 'var(--bg-tertiary)'
                                    ),
                                    color: index === selectedIndex ? 'white' : 'var(--text-secondary)',
                                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                                    transition: 'all 0.2s ease', fontSize: '16px'
                                }}>
                                    {item._type === 'nav' ? <Icon size={16} /> : (
                                        typeof Icon === 'function' ? <Icon size={16} /> : Icon
                                    )}
                                </div>
                                <div style={{ flex: 1 }}>
                                    <p style={{ margin: 0, fontSize: '14px', fontWeight: 600, color: 'var(--text-primary)' }}>
                                        {item._type === 'nav' ? item.title : item.label}
                                    </p>
                                    <p style={{ margin: 0, fontSize: '11px', color: 'var(--text-muted)' }}>
                                        {item._type === 'nav' ? item.category : `${item.type} · ${item.subtitle || item.url}`}
                                    </p>
                                </div>
                                {index === selectedIndex && (
                                    <Command size={14} style={{ color: 'var(--primary-color)' }} />
                                )}
                            </div>
                        );
                    }) : (
                        <div style={{ padding: '32px', textAlign: 'center', color: 'var(--text-muted)' }}>
                            <p style={{ margin: 0, fontSize: '14px' }}>
                                {query.trim() ? `No results found for "${query}"` : 'Type to search users, roles, menus, configs, notices, and jobs'}
                            </p>
                        </div>
                    )}
                </div>

                <div style={{
                    padding: '12px 16px', background: 'var(--bg-tertiary)',
                    borderTop: '1px solid var(--border-color)', display: 'flex', gap: '16px'
                }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '10px', color: 'var(--text-muted)' }}>
                        <span style={{ padding: '2px 4px', borderRadius: '3px', background: 'var(--bg-secondary)', border: '1px solid var(--border-color)' }}>↵</span>
                        <span>Select</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '10px', color: 'var(--text-muted)' }}>
                        <span style={{ padding: '2px 4px', borderRadius: '3px', background: 'var(--bg-secondary)', border: '1px solid var(--border-color)' }}>↑↓</span>
                        <span>Navigate</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CommandPalette;
