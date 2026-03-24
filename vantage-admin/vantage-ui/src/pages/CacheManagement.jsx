import { useState, useEffect } from 'react';
import { Database, RefreshCw, Trash2, Eye, Server } from 'lucide-react';
import DataGrid from '../components/DataGrid';
import Modal from '../components/Modal';

const CacheManagement = () => {
    const [caches, setCaches] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedCache, setSelectedCache] = useState(null);
    const [isStatsModalOpen, setIsStatsModalOpen] = useState(false);
    const [cacheStats, setCacheStats] = useState(null);

    useEffect(() => {
        fetchCaches();
    }, []);

    const fetchCaches = () => {
        setLoading(true);
        fetch('/api/system/cache/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setCaches(data.data || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch caches:", err);
                setLoading(false);
            });
    };

    const handleClearCache = (cacheName) => {
        if (window.confirm(`Clear cache "${cacheName}"?`)) {
            fetch(`/api/system/cache/clear/${cacheName}`, { method: 'POST' })
                .then(res => res.json())
                .then(data => {
                    if (data.code === 200) {
                        alert(`Cache "${cacheName}" cleared successfully`);
                        fetchCaches();
                    } else {
                        alert(data.msg || 'Failed to clear cache');
                    }
                });
        }
    };

    const handleClearAll = () => {
        if (window.confirm('Clear ALL caches? This may temporarily affect application performance.')) {
            fetch('/api/system/cache/clear-all', { method: 'POST' })
                .then(res => res.json())
                .then(data => {
                    if (data.code === 200) {
                        alert('All caches cleared successfully');
                        fetchCaches();
                    } else {
                        alert(data.msg || 'Failed to clear all caches');
                    }
                });
        }
    };

    const handleViewStats = (cacheName) => {
        fetch(`/api/system/cache/stats/${cacheName}`)
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setCacheStats(data.data);
                    setIsStatsModalOpen(true);
                }
            });
    };

    const columns = [
        { key: 'cacheName', header: 'Cache Name', sortable: true },
        { 
            key: 'type', 
            header: 'Type',
            render: (value) => (
                <span style={{ 
                    padding: '4px 8px', 
                    borderRadius: '4px', 
                    fontSize: '11px',
                    background: 'var(--bg-tertiary)',
                    fontFamily: 'monospace'
                }}>
                    {value || 'N/A'}
                </span>
            )
        },
        {
            key: 'actions',
            header: 'Actions',
            align: 'center',
            render: (_, row) => (
                <div style={{ display: 'flex', gap: '4px', justifyContent: 'center' }}>
                    <button
                        onClick={() => handleViewStats(row.cacheName)}
                        className="btn btn-secondary"
                        style={{ padding: '4px 8px', fontSize: '11px' }}
                        title="View Stats"
                    >
                        <Eye size={12} />
                    </button>
                    <button
                        onClick={() => handleClearCache(row.cacheName)}
                        className="btn btn-secondary"
                        style={{ padding: '4px 8px', fontSize: '11px', color: 'var(--warning)' }}
                        title="Clear Cache"
                    >
                        <Trash2 size={12} />
                    </button>
                </div>
            )
        }
    ];

    const toolbarActions = [
        {
            label: 'Refresh',
            icon: RefreshCw,
            onClick: fetchCaches
        },
        {
            label: 'Clear All',
            icon: Trash2,
            onClick: handleClearAll,
            danger: true
        }
    ];

    return (
        <div className="page-container">
            <div className="page-header">
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{
                        width: '40px',
                        height: '40px',
                        borderRadius: '10px',
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <Server size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '13px', fontWeight: 700, margin: 0 }}>Cache Management</h2>
                        <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Monitor and manage application caches
                        </p>
                    </div>
                </div>
            </div>

            {/* Cache Summary Cards */}
            <div style={{ 
                display: 'grid', 
                gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', 
                gap: '12px',
                marginBottom: '20px'
            }}>
                <div style={{ 
                    padding: '16px', 
                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', 
                    borderRadius: '12px',
                    color: 'white'
                }}>
                    <div style={{ fontSize: '11px', opacity: 0.9 }}>Total Caches</div>
                    <div style={{ fontSize: '28px', fontWeight: 700 }}>{caches.length}</div>
                </div>
                <div style={{ 
                    padding: '16px', 
                    background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', 
                    borderRadius: '12px',
                    color: 'white'
                }}>
                    <div style={{ fontSize: '11px', opacity: 0.9 }}>System Caches</div>
                    <div style={{ fontSize: '28px', fontWeight: 700 }}>
                        {caches.filter(c => c.cacheName.includes('menu') || c.cacheName.includes('dict')).length}
                    </div>
                </div>
            </div>

            <DataGrid
                data={caches}
                columns={columns}
                toolbarActions={toolbarActions}
                loading={loading}
                searchable={false}
                sortable={true}
                pagination={false}
                emptyMessage="No caches found."
            />

            {/* Cache Stats Modal */}
            <Modal
                isOpen={isStatsModalOpen}
                onClose={() => setIsStatsModalOpen(false)}
                title="Cache Statistics"
                size="small"
            >
                {cacheStats && (
                    <div style={{ padding: '12px' }}>
                        <div style={{ marginBottom: '16px' }}>
                            <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '4px' }}>Cache Name</div>
                            <div style={{ fontSize: '14px', fontWeight: 600 }}>{cacheStats.cacheName}</div>
                        </div>
                        
                        <div style={{ marginBottom: '16px' }}>
                            <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '4px' }}>Cache Type</div>
                            <div style={{ fontSize: '13px', fontFamily: 'monospace', background: 'var(--bg-tertiary)', padding: '8px', borderRadius: '6px' }}>
                                {cacheStats.type}
                            </div>
                        </div>

                        <div style={{ marginBottom: '16px' }}>
                            <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '4px' }}>Native Cache</div>
                            <div style={{ fontSize: '13px', fontFamily: 'monospace', background: 'var(--bg-tertiary)', padding: '8px', borderRadius: '6px' }}>
                                {cacheStats.nativeCache}
                            </div>
                        </div>

                        {cacheStats.estimatedSize !== undefined && (
                            <div>
                                <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '4px' }}>Estimated Size</div>
                                <div style={{ fontSize: '24px', fontWeight: 700, color: 'var(--primary)' }}>
                                    {cacheStats.estimatedSize.toLocaleString()}
                                </div>
                            </div>
                        )}
                    </div>
                )}
            </Modal>
        </div>
    );
};

export default CacheManagement;
