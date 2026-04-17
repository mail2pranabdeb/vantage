import React, { useState, useEffect, useRef } from 'react';
import { Search, Command, Zap, Users, Shield, Calendar, Settings, Clock, Layout, X } from 'lucide-react';

const CommandPalette = ({ onNavigate }) => {
    const [isOpen, setIsOpen] = useState(false);
    const [query, setQuery] = useState('');
    const [selectedIndex, setSelectedIndex] = useState(0);
    const inputRef = useRef(null);

    const commands = [
        { id: 'dashboard', title: 'Go to Dashboard', icon: Layout, category: 'Navigation', url: '/dashboard' },
        { id: 'system:user:list', title: 'Manage Users', icon: Users, category: 'System', url: '/system/user' },
        { id: 'system:role:list', title: 'Role Permissions', icon: Shield, category: 'System', url: '/system/role' },
        { id: 'system:job:list', title: 'Scheduled Jobs', icon: Calendar, category: 'Quartz', url: '/system/job' },
        { id: 'system:jobLog:list', title: 'Job Execution Logs', icon: Clock, category: 'Quartz', url: '/system/jobLog' },
        { id: 'system:config:list', title: 'System Configuration', icon: Settings, category: 'System', url: '/system/config' },
        { id: 'system:job:logs', title: 'Real-time Log Stream', icon: Zap, category: 'Monitoring', url: '/system/live-logs' },
    ];

    const filteredCommands = query === '' 
        ? commands 
        : commands.filter(cmd => 
            cmd.title.toLowerCase().includes(query.toLowerCase()) || 
            cmd.category.toLowerCase().includes(query.toLowerCase())
        );

    useEffect(() => {
        const handleKeyDown = (e) => {
            if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
                e.preventDefault();
                setIsOpen(true);
            }
            if (e.key === 'Escape') {
                setIsOpen(false);
            }
        };

        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, []);

    useEffect(() => {
        if (isOpen && inputRef.current) {
            inputRef.current.focus();
        }
    }, [isOpen]);

    const handleSelect = (cmd) => {
        onNavigate({
            id: cmd.id,
            title: cmd.title.replace('Go to ', '').replace('Manage ', ''),
            url: cmd.url,
            icon: '⚡'
        });
        setIsOpen(false);
        setQuery('');
        setSelectedIndex(0);
    };

    const handleKeyDown = (e) => {
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            setSelectedIndex(prev => (prev + 1) % filteredCommands.length);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            setSelectedIndex(prev => (prev - 1 + filteredCommands.length) % filteredCommands.length);
        } else if (e.key === 'Enter') {
            if (filteredCommands[selectedIndex]) {
                handleSelect(filteredCommands[selectedIndex]);
            }
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
                        placeholder="Search commands, pages, or settings... (Ctrl+K)"
                        value={query}
                        onChange={e => { setQuery(e.target.value); setSelectedIndex(0); }}
                        onKeyDown={handleKeyDown}
                        style={{
                            flex: 1, border: 'none', background: 'transparent',
                            color: 'var(--text-primary)', fontSize: '16px', outline: 'none'
                        }}
                    />
                    <div style={{ 
                        display: 'flex', alignItems: 'center', gap: '4px',
                        padding: '4px 8px', borderRadius: '4px', background: 'var(--bg-tertiary)',
                        fontSize: '10px', color: 'var(--text-muted)', fontWeight: 700
                    }}>
                        ESC
                    </div>
                </div>

                <div style={{ maxHeight: '400px', overflowY: 'auto', padding: '8px' }}>
                    {filteredCommands.length > 0 ? (
                        <>
                            {filteredCommands.map((cmd, index) => (
                                <div
                                    key={cmd.id}
                                    onClick={() => handleSelect(cmd)}
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
                                        background: index === selectedIndex ? 'var(--primary-color)' : 'var(--bg-tertiary)',
                                        color: index === selectedIndex ? 'white' : 'var(--text-secondary)',
                                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                                        transition: 'all 0.2s ease'
                                    }}>
                                        <cmd.icon size={16} />
                                    </div>
                                    <div style={{ flex: 1 }}>
                                        <p style={{ margin: 0, fontSize: '14px', fontWeight: 600, color: 'var(--text-primary)' }}>{cmd.title}</p>
                                        <p style={{ margin: 0, fontSize: '11px', color: 'var(--text-muted)' }}>{cmd.category}</p>
                                    </div>
                                    {index === selectedIndex && (
                                        <Command size={14} style={{ color: 'var(--primary-color)' }} />
                                    )}
                                </div>
                            ))}
                        </>
                    ) : (
                        <div style={{ padding: '32px', textAlign: 'center', color: 'var(--text-muted)' }}>
                            <p style={{ margin: 0, fontSize: '14px' }}>No results found for "{query}"</p>
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
