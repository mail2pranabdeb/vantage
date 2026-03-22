import { useState } from 'react';
import { X, RefreshCw } from 'lucide-react';

const TabBar = ({ tabs, activeTab, onTabClick, onTabClose, onRefresh }) => {
    const [scrollPosition, setScrollPosition] = useState(0);

    const scrollLeft = () => {
        setScrollPosition(Math.max(0, scrollPosition - 200));
    };

    const scrollRight = () => {
        setScrollPosition(scrollPosition + 200);
    };

    return (
        <div style={{
            display: 'flex',
            alignItems: 'center',
            background: 'var(--bg-secondary)',
            borderBottom: '1px solid var(--border-color)',
            height: '44px',
            overflow: 'hidden'
        }}>
            {/* Scroll Left Button */}
            <button
                onClick={scrollLeft}
                style={{
                    padding: '8px',
                    background: 'transparent',
                    border: 'none',
                    cursor: 'pointer',
                    color: 'var(--text-muted)',
                    display: 'flex',
                    alignItems: 'center',
                    borderRight: '1px solid var(--border-color)'
                }}
            >
                <RefreshCw size={14} style={{ transform: 'rotate(-90deg)' }} />
            </button>

            {/* Tabs Container */}
            <div style={{
                flex: 1,
                display: 'flex',
                overflow: 'hidden',
                transform: `translateX(-${scrollPosition}px)`,
                transition: 'transform 0.3s ease'
            }}>
                {tabs.map((tab, index) => (
                    <div
                        key={tab.id}
                        onClick={() => onTabClick(tab.id)}
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: '8px',
                            padding: '10px 20px',
                            background: activeTab === tab.id ? 'var(--bg-primary)' : 'var(--bg-secondary)',
                            borderBottom: activeTab === tab.id ? '2px solid var(--primary-color)' : '2px solid transparent',
                            borderRight: '1px solid var(--border-color)',
                            cursor: 'pointer',
                            fontSize: '13px',
                            fontWeight: activeTab === tab.id ? '600' : '400',
                            color: activeTab === tab.id ? 'var(--text-primary)' : 'var(--text-secondary)',
                            whiteSpace: 'nowrap',
                            transition: 'all 0.2s',
                            minWidth: '140px',
                            maxWidth: '200px'
                        }}
                    >
                        {tab.icon}
                        <span style={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis' }}>{tab.title}</span>
                        {/* Only show close button for closable tabs */}
                        {tab.closable !== false && (
                            <button
                                onClick={(e) => {
                                    e.stopPropagation();
                                    onTabClose(tab.id);
                                }}
                                style={{
                                    padding: '2px',
                                    background: 'transparent',
                                    border: 'none',
                                    cursor: 'pointer',
                                    color: 'var(--text-muted)',
                                    opacity: activeTab === tab.id ? 1 : 0,
                                    transition: 'opacity 0.2s',
                                    borderRadius: '4px',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center'
                                }}
                                onMouseEnter={(e) => {
                                    e.target.style.color = '#ef4444';
                                    e.target.style.background = 'rgba(239, 68, 68, 0.1)';
                                }}
                                onMouseLeave={(e) => {
                                    e.target.style.color = 'var(--text-muted)';
                                    e.target.style.background = 'transparent';
                                }}
                                title="Close tab"
                            >
                                <X size={12} />
                            </button>
                        )}
                    </div>
                ))}
            </div>

            {/* Scroll Right Button */}
            <button
                onClick={scrollRight}
                style={{
                    padding: '8px',
                    background: 'transparent',
                    border: 'none',
                    cursor: 'pointer',
                    color: 'var(--text-muted)',
                    display: 'flex',
                    alignItems: 'center',
                    borderLeft: '1px solid var(--border-color)'
                }}
            >
                <RefreshCw size={14} style={{ transform: 'rotate(90deg)' }} />
            </button>

            {/* Refresh Current Tab */}
            <button
                onClick={() => onRefresh(activeTab)}
                style={{
                    padding: '8px',
                    background: 'transparent',
                    border: 'none',
                    cursor: 'pointer',
                    color: 'var(--text-muted)',
                    display: 'flex',
                    alignItems: 'center',
                    borderLeft: '1px solid var(--border-color)'
                }}
                title="Refresh"
            >
                <RefreshCw size={14} />
            </button>
        </div>
    );
};

export default TabBar;
