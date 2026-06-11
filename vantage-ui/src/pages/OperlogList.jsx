import { useState, useEffect } from 'react';
import { FileText, Trash2, RefreshCw, CheckCircle, XCircle, Activity, X, AlertCircle, Clock, User, Globe, MapPin, Download } from 'lucide-react';
import DataGrid from '../components/DataGrid';

const OperlogDetailModal = ({ log, onClose }) => {
    if (!log) return null;

    const businessTypes = ['Other', 'Insert', 'Update', 'Delete', 'Grant', 'Export', 'Import', 'Force', 'Gen', 'Clean'];
    
    const parseJson = (str) => {
        if (!str) return null;
        try { return JSON.parse(str); } catch { return null; }
    };

    const oldValues = parseJson(log.oldValues);
    const newValues = parseJson(log.newValues);
    const changedFields = log.changedFields ? log.changedFields.split(',').filter(f => f) : [];

    return (
        <div style={{
            position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
            background: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center',
            justifyContent: 'center', zIndex: 1000
        }} onClick={onClose}>
            <div style={{
                background: 'var(--bg-secondary)', borderRadius: '16px', padding: '24px',
                width: '90%', maxWidth: '800px', maxHeight: '85vh', overflow: 'auto',
                boxShadow: '0 20px 60px rgba(0,0,0,0.3)', position: 'relative'
            }} onClick={e => e.stopPropagation()}>
                <button onClick={onClose} style={{
                    position: 'absolute', top: '16px', right: '16px', background: 'var(--bg-secondary)',
                    border: 'none', borderRadius: '8px', width: '36px', height: '36px',
                    display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', color: 'var(--text-primary)'
                }}>
                    <X size={18} />
                </button>

                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px' }}>
                    <div style={{
                        width: '40px', height: '40px', borderRadius: '10px',
                        background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
                        display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white'
                    }}>
                        <FileText size={20} />
                    </div>
                    <div>
                        <h3 style={{ margin: 0, fontSize: '18px', fontWeight: 700 }}>Operation Details</h3>
                        <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-muted)' }}>{log.title}</p>
                    </div>
                </div>

                <div style={{
                    display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
                    gap: '16px', marginBottom: '24px'
                }}>
                    <div style={{ background: 'var(--bg-secondary)', padding: '12px', borderRadius: '10px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px', color: 'var(--text-muted)', fontSize: '12px' }}>
                            <Activity size={14} /> Type
                        </div>
                        <div style={{ fontWeight: 600 }}>{businessTypes[log.businessType] || 'Other'}</div>
                    </div>
                    <div style={{ background: 'var(--bg-secondary)', padding: '12px', borderRadius: '10px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px', color: 'var(--text-muted)', fontSize: '12px' }}>
                            <CheckCircle size={14} /> Status
                        </div>
                        <div style={{ fontWeight: 600, color: log.status === 0 ? 'var(--success)' : 'var(--danger)' }}>
                            {log.status === 0 ? 'Success' : 'Failed'}
                        </div>
                    </div>
                    <div style={{ background: 'var(--bg-secondary)', padding: '12px', borderRadius: '10px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px', color: 'var(--text-muted)', fontSize: '12px' }}>
                            <User size={14} /> Operator
                        </div>
                        <div style={{ fontWeight: 600 }}>{log.operName || '-'}</div>
                    </div>
                    <div style={{ background: 'var(--bg-secondary)', padding: '12px', borderRadius: '10px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px', color: 'var(--text-muted)', fontSize: '12px' }}>
                            <Clock size={14} /> Time
                        </div>
                        <div style={{ fontWeight: 600 }}>{log.operTime ? new Date(log.operTime).toLocaleString() : '-'}</div>
                    </div>
                    <div style={{ background: 'var(--bg-secondary)', padding: '12px', borderRadius: '10px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px', color: 'var(--text-muted)', fontSize: '12px' }}>
                            <Globe size={14} /> IP
                        </div>
                        <div style={{ fontWeight: 600, fontFamily: 'monospace' }}>{log.operIp || '-'}</div>
                    </div>
                    <div style={{ background: 'var(--bg-secondary)', padding: '12px', borderRadius: '10px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px', color: 'var(--text-muted)', fontSize: '12px' }}>
                            <MapPin size={14} /> Location
                        </div>
                        <div style={{ fontWeight: 600 }}>{log.operLocation || '-'}</div>
                    </div>
                </div>

                <div style={{ marginBottom: '20px' }}>
                    <h4 style={{ fontSize: '14px', fontWeight: 600, marginBottom: '8px', color: 'var(--text-muted)' }}>Method</h4>
                    <div style={{ background: 'var(--bg-secondary)', padding: '12px', borderRadius: '10px', fontFamily: 'monospace', fontSize: '13px' }}>
                        {log.method || '-'}
                    </div>
                </div>

                <div style={{ marginBottom: '20px' }}>
                    <h4 style={{ fontSize: '14px', fontWeight: 600, marginBottom: '8px', color: 'var(--text-muted)' }}>URL</h4>
                    <div style={{ background: 'var(--bg-secondary)', padding: '12px', borderRadius: '10px', fontFamily: 'monospace', fontSize: '13px' }}>
                        {log.operUrl || '-'}
                    </div>
                </div>

                {log.costTime && (
                    <div style={{ marginBottom: '20px' }}>
                        <h4 style={{ fontSize: '14px', fontWeight: 600, marginBottom: '8px', color: 'var(--text-muted)' }}>Execution Time</h4>
                        <div style={{ background: 'var(--bg-secondary)', padding: '12px', borderRadius: '10px', fontSize: '13px' }}>
                            {log.costTime} ms
                        </div>
                    </div>
                )}

                {log.operParam && (
                    <div style={{ marginBottom: '20px' }}>
                        <h4 style={{ fontSize: '14px', fontWeight: 600, marginBottom: '8px', color: 'var(--text-muted)' }}>Request Parameters</h4>
                        <div style={{ background: 'var(--bg-secondary)', padding: '12px', borderRadius: '10px', fontFamily: 'monospace', fontSize: '13px', maxHeight: '150px', overflow: 'auto' }}>
                            {log.operParam}
                        </div>
                    </div>
                )}

                {log.jsonResult && (
                    <div style={{ marginBottom: '20px' }}>
                        <h4 style={{ fontSize: '14px', fontWeight: 600, marginBottom: '8px', color: 'var(--text-muted)' }}>Response</h4>
                        <div style={{ background: 'var(--bg-secondary)', padding: '12px', borderRadius: '10px', fontFamily: 'monospace', fontSize: '13px', maxHeight: '150px', overflow: 'auto' }}>
                            {log.jsonResult}
                        </div>
                    </div>
                )}

                {log.errorMsg && (
                    <div style={{ marginBottom: '20px' }}>
                        <h4 style={{ fontSize: '14px', fontWeight: 600, marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--danger)' }}>
                            <AlertCircle size={16} /> Error Message
                        </h4>
                        <div style={{ background: 'rgba(239, 68, 68, 0.1)', padding: '12px', borderRadius: '10px', fontFamily: 'monospace', fontSize: '13px', border: '1px solid rgba(239, 68, 68, 0.2)', maxHeight: '150px', overflow: 'auto' }}>
                            {log.errorMsg}
                        </div>
                    </div>
                )}

                {(oldValues || newValues) && (
                    <div style={{ marginBottom: '20px' }}>
                        <h4 style={{ fontSize: '14px', fontWeight: 600, marginBottom: '8px', color: 'var(--text-muted)' }}>
                            Audit Trail (Before / After)
                        </h4>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                            <div>
                                <h5 style={{ fontSize: '12px', color: 'var(--text-muted)', marginBottom: '6px' }}>Before</h5>
                                <div style={{ background: 'var(--bg-secondary)', padding: '12px', borderRadius: '10px', fontFamily: 'monospace', fontSize: '12px', maxHeight: '200px', overflow: 'auto', whiteSpace: 'pre-wrap' }}>
                                    {oldValues ? JSON.stringify(oldValues, null, 2) : '-'}
                                </div>
                            </div>
                            <div>
                                <h5 style={{ fontSize: '12px', color: 'var(--text-muted)', marginBottom: '6px' }}>After</h5>
                                <div style={{ background: 'var(--bg-secondary)', padding: '12px', borderRadius: '10px', fontFamily: 'monospace', fontSize: '12px', maxHeight: '200px', overflow: 'auto', whiteSpace: 'pre-wrap' }}>
                                    {newValues ? JSON.stringify(newValues, null, 2) : '-'}
                                </div>
                            </div>
                        </div>
                        {changedFields.length > 0 && (
                            <div style={{ marginTop: '12px' }}>
                                <h5 style={{ fontSize: '12px', color: 'var(--text-muted)', marginBottom: '6px' }}>Changed Fields</h5>
                                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                                    {changedFields.map((field, idx) => (
                                        <span key={idx} style={{
                                            padding: '4px 10px', borderRadius: '8px', fontSize: '11px', fontWeight: 600,
                                            background: '#f59e0b20', color: '#f59e0b'
                                        }}>
                                            {field}
                                        </span>
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

const OperlogList = () => {
    const [list, setList] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedLog, setSelectedLog] = useState(null);

    const fetchLogs = () => {
        setLoading(true);
        fetch('/api/system/operlog/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setList(data.data?.rows || data.data || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch operation logs:", err);
                setLoading(false);
            });
    };

    useEffect(() => {
        fetchLogs();
    }, []);

    const handleDelete = (row) => {
        if (!confirm(`Delete operation log #${row.operId}?`)) return;
        fetch(`/api/system/operlog`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify([row.operId])
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                setList(prev => prev.filter(log => log.operId !== row.operId));
            } else {
                alert(data.msg || 'Delete failed');
            }
        })
        .catch(err => {
            console.error('Delete failed:', err);
            alert('Delete failed');
        });
    };

    const handleClean = () => {
        if (!confirm('Clear ALL operation logs? This cannot be undone.')) return;
        fetch('/api/system/operlog/clean', { method: 'DELETE' })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                setList([]);
            } else {
                alert(data.msg || 'Clean failed');
            }
        })
        .catch(err => {
            console.error('Clean failed:', err);
            alert('Clean failed');
        });
    };

    const handleRefresh = () => {
        fetchLogs();
    };

    const columns = [
        { key: 'operId', header: 'ID', sortable: true, align: 'center' },
        {
            key: 'title',
            header: 'Operation',
            sortable: true,
            render: (value, row) => (
                <div>
                    <div style={{ fontWeight: 600 }}>{value}</div>
                    <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{row.method || '-'}</div>
                </div>
            )
        },
        {
            key: 'businessType',
            header: 'Type',
            sortable: true,
            align: 'center',
            render: (value) => {
                const types = ['Other', 'Insert', 'Update', 'Delete', 'Grant', 'Export', 'Import', 'Force', 'Gen', 'Clean'];
                return (
                    <span style={{
                        padding: '4px 10px',
                        borderRadius: '12px',
                        fontSize: '11px',
                        fontWeight: 600,
                        background: '#3b82f620',
                        color: '#3b82f6'
                    }}>
                        {types[value] || 'Other'}
                    </span>
                );
            }
        },
        {
            key: 'operName',
            header: 'Operator',
            sortable: true,
            render: (value) => <span style={{ fontWeight: 500 }}>{value || '-'}</span>
        },
        {
            key: 'operIp',
            header: 'IP',
            sortable: true,
            render: (value) => <span className="badge-outline" style={{ fontSize: '11px', fontFamily: 'monospace' }}>{value}</span>
        },
        {
            key: 'status',
            header: 'Status',
            sortable: true,
            render: (value) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    {value === 0 ? <CheckCircle size={16} className="text-success" /> : <XCircle size={16} className="text-danger" />}
                    <span style={{ fontWeight: 500 }}>{value === 0 ? 'Success' : 'Failed'}</span>
                </div>
            )
        },
        {
            key: 'operTime',
            header: 'Time',
            sortable: true,
            render: (value) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Activity size={14} style={{ opacity: 0.5 }} />
                    <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>{value ? new Date(value).toLocaleString() : '-'}</span>
                </div>
            )
        }
    ];

    const actions = [
        { label: 'View', icon: FileText, onClick: (row) => setSelectedLog(row) },
        { label: 'Delete', icon: Trash2, danger: true, onClick: handleDelete }
    ];

    const handleExport = (format) => {
        const columns = [
            { key: 'operId', label: 'ID' },
            { key: 'title', label: 'Title' },
            { key: 'method', label: 'Method' },
            { key: 'operName', label: 'Operator' },
            { key: 'operIp', label: 'IP' },
            { key: 'status', label: 'Status' },
            { key: 'operTime', label: 'Time' },
        ];
        const ext = format.toLowerCase();
        fetch('/api/system/export?format=' + format + '&filename=operlogs', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ columns, rows: list })
        })
        .then(res => res.blob())
        .then(blob => {
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a'); a.href = url; a.download = 'operlogs.' + ext;
            document.body.appendChild(a); a.click(); a.remove();
            URL.revokeObjectURL(url);
        })
        .catch(err => console.error('Export failed:', err));
    };

    const toolbarActions = [
        { label: 'PDF', icon: Download, onClick: () => handleExport('PDF') },
        { label: 'CSV', icon: Download, onClick: () => handleExport('CSV') },
        { label: 'Clean', icon: Trash2, onClick: handleClean },
        { label: 'Refresh', icon: RefreshCw, onClick: handleRefresh }
    ];

    return (
        <div className="page-container">
            <div className="page-header">
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{
                        width: '40px',
                        height: '40px',
                        borderRadius: '10px',
                        background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <FileText size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Operation Log</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Track and audit system operations
                        </p>
                    </div>
                </div>
                <div style={{ display: 'flex', gap: '8px' }}>
                    {toolbarActions.map((action, idx) => (
                        <button key={idx} className="btn btn-secondary" style={{
                            display: 'flex', alignItems: 'center', gap: '6px', padding: '10px 16px'
                        }} onClick={action.onClick}>
                            <action.icon size={16} /> {action.label}
                        </button>
                    ))}
                </div>
            </div>

            <DataGrid
                data={list}
                columns={columns}
                actions={actions}
                loading={loading}
                searchable={true}
                sortable={true}
                filterable={true}
                selectable={true}
                pagination={true}
                pageSize={15}
                emptyMessage="No operation logs found."
            />

            {selectedLog && (
                <OperlogDetailModal log={selectedLog} onClose={() => setSelectedLog(null)} />
            )}
        </div>
    );
};

export default OperlogList;
