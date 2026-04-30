import { useState, useRef } from 'react';
import { X, RefreshCw, ChevronLeft, ChevronRight } from 'lucide-react';

const TabBar = ({ tabs, activeTab, onTabClick, onTabClose, onRefresh }) => {
    const tabsRef = useRef(null);
    const [isRefreshing, setIsRefreshing] = useState(false);

    const scrollLeft = () => {
        if (tabsRef.current) tabsRef.current.scrollBy({ left: -200, behavior: 'smooth' });
    };

    const scrollRight = () => {
        if (tabsRef.current) tabsRef.current.scrollBy({ left: 200, behavior: 'smooth' });
    };

    const handleRefresh = () => {
        if (isRefreshing) return;
        setIsRefreshing(true);
        onRefresh(activeTab);
        setTimeout(() => setIsRefreshing(false), 800);
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
                <ChevronLeft size={14} />
            </button>

            {/* Tabs Container */}
            <div
                ref={tabsRef}
                style={{
                    flex: 1,
                    display: 'flex',
                    overflowX: 'auto',
                    overflowY: 'hidden',
                    scrollbarWidth: 'none',
                    msOverflowStyle: 'none',
                    scrollBehavior: 'smooth'
                }}
                className="hide-scrollbar"
            >
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
                <ChevronRight size={14} />
            </button>

            {/* Refresh Current Tab */}
            <button
                onClick={handleRefresh}
                disabled={isRefreshing}
                style={{
                    padding: '8px',
                    background: 'transparent',
                    border: 'none',
                    cursor: isRefreshing ? 'default' : 'pointer',
                    color: isRefreshing ? 'var(--primary-color)' : 'var(--text-muted)',
                    display: 'flex',
                    alignItems: 'center',
                    borderLeft: '1px solid var(--border-color)',
                    opacity: isRefreshing ? 0.7 : 1
                }}
                title="Refresh"
            >
                <RefreshCw
                    size={14}
                    style={{
                        animation: isRefreshing ? 'spin 0.8s linear infinite' : 'none'
                    }}
                />
            </button>
        </div>
    );
};

export default TabBar;
