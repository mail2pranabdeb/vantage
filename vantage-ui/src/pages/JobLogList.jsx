import { useState, useEffect } from 'react';
import { FileText, Trash2, RefreshCw, CheckCircle, XCircle, Clock, X, Download } from 'lucide-react';
import DataGrid from '../components/DataGrid';
import Modal from '../components/Modal';
import { useToast } from '../components/Toast';

const JobLogList = () => {
    const toast = useToast();
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isViewModalOpen, setIsViewModalOpen] = useState(false);
    const [selectedLog, setSelectedLog] = useState(null);

    useEffect(() => {
        fetchLogs();
    }, []);

    const fetchLogs = () => {
        setLoading(true);
        fetch('/api/system/job-log/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setLogs(data.data?.rows || data.data || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch job logs:", err);
                setLoading(false);
            });
    };

    const handleViewClick = (row) => {
        setSelectedLog(row);
        setIsViewModalOpen(true);
    };

    const handleDeleteClick = (row) => {
        if (window.confirm(`Delete log #${row.jobLogId} for "${row.jobName}"?`)) {
            fetch(`/api/system/job-log/${row.jobLogId}`, { method: 'DELETE' })
                .then(res => res.json())
                .then(data => {
                    if (data.code === 200) {
                        toast.success('Log deleted');
                        fetchLogs();
                    } else {
                        toast.error(data.msg || 'Failed to delete log');
                    }
                })
                .catch(err => {
                    console.error("Failed to delete log:", err);
                    toast.error('Failed to delete log');
                });
        }
    };

    const handleCleanLogs = () => {
        if (window.confirm('Clear all job logs? This cannot be undone.')) {
            fetch('/api/system/job-log/clean', { method: 'DELETE' })
                .then(res => res.json())
                .then(data => {
                    if (data.code === 200) {
                        toast.success('All logs cleared');
                        fetchLogs();
                    } else {
                        toast.error(data.msg || 'Failed to clear logs');
                    }
                })
                .catch(err => {
                    console.error("Failed to clear logs:", err);
                    toast.error('Failed to clear logs');
                });
        }
    };

    const columns = [
        { key: 'jobLogId', header: 'ID', sortable: true, align: 'center' },
        {
            key: 'jobName',
            header: 'Job Name',
            sortable: true,
            render: (value, row) => (
                <div>
                    <div style={{ fontWeight: 600 }}>{value}</div>
                    <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{row.jobGroup}</div>
                </div>
            )
        },
        {
            key: 'invokeTarget',
            header: 'Target',
            sortable: false,
            render: (value) => <span style={{ color: 'var(--text-secondary)', fontSize: '12px', fontFamily: 'monospace' }}>{value || '-'}</span>
        },
        {
            key: 'status',
            header: 'Status',
            sortable: true,
            render: (value) => {
                if (value === '2') {
                    return (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <Clock size={16} style={{ color: 'var(--warning)' }} />
                            <span style={{ fontWeight: 500, color: 'var(--warning)' }}>Running</span>
                        </div>
                    );
                }
                return (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        {value === '0' ? <CheckCircle size={16} className="text-success" /> : <XCircle size={16} className="text-danger" />}
                        <span style={{ fontWeight: 500 }}>{value === '0' ? 'Success' : 'Failed'}</span>
                    </div>
                );
            }
        },
        {
            key: 'startTime',
            header: 'Start Time',
            sortable: true,
            render: (value) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Clock size={14} style={{ opacity: 0.5 }} />
                    <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>{value ? new Date(value).toLocaleString() : '-'}</span>
                </div>
            )
        },
        {
            key: 'executionDuration',
            header: 'Duration',
            sortable: true,
            align: 'center',
            render: (value) => (
                <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                    {value ? `${value} ms` : '-'}
                </span>
            )
        }
    ];

    const actions = [
        { label: 'View', icon: FileText, onClick: handleViewClick },
        { label: 'Delete', icon: Trash2, danger: true, onClick: handleDeleteClick }
    ];

    const handleExport = (format) => {
        const columns = [
            { key: 'jobLogId', label: 'ID' },
            { key: 'jobName', label: 'Job Name' },
            { key: 'jobGroup', label: 'Job Group' },
            { key: 'status', label: 'Status' },
            { key: 'startTime', label: 'Time' },
        ];
        const ext = format.toLowerCase();
        fetch('/api/system/export?format=' + format + '&filename=joblogs', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ columns, rows: logs })
        })
        .then(res => res.blob())
        .then(blob => {
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a'); a.href = url; a.download = 'joblogs.' + ext;
            document.body.appendChild(a); a.click(); a.remove();
            URL.revokeObjectURL(url);
        })
        .catch(err => console.error('Export failed:', err));
    };

    const toolbarActions = [
        { label: 'Export PDF', icon: Download, onClick: () => handleExport('PDF') },
        { label: 'Export CSV', icon: Download, onClick: () => handleExport('CSV') },
        { label: 'Clean All', icon: Trash2, onClick: handleCleanLogs },
        { label: 'Refresh', icon: RefreshCw, onClick: fetchLogs }
    ];

    return (
        <div className="page-container">
            <div className="page-header">
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{
                        width: '40px',
                        height: '40px',
                        borderRadius: '10px',
                        background: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: '#333'
                    }}>
                        <FileText size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Job Log</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            View execution history of scheduled jobs
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
                data={logs}
                columns={columns}
                actions={actions}
                toolbarActions={toolbarActions}
                loading={loading}
                searchable={true}
                sortable={true}
                filterable={true}
                selectable={true}
                pagination={true}
                pageSize={15}
                emptyMessage="No job execution logs found."
            />

            {/* View Log Detail Modal */}
            <Modal
                isOpen={isViewModalOpen}
                onClose={() => setIsViewModalOpen(false)}
                title={`Job Log #${selectedLog?.jobLogId || ''}`}
                size="large"
            >
                {selectedLog && (
                    <div style={{ maxHeight: '60vh', overflowY: 'auto', paddingRight: '8px' }}>
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px', marginBottom: '20px' }}>
                            <div>
                                <label style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>Job Name</label>
                                <div style={{ fontSize: '14px', fontWeight: 500, marginTop: '4px' }}>{selectedLog.jobName}</div>
                            </div>
                            <div>
                                <label style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>Job Group</label>
                                <div style={{ fontSize: '14px', fontWeight: 500, marginTop: '4px' }}>{selectedLog.jobGroup}</div>
                            </div>
                            <div>
                                <label style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>Status</label>
                                <div style={{ marginTop: '4px' }}>
                                    {selectedLog.status === '0' ? (
                                        <span style={{ padding: '4px 10px', borderRadius: '12px', fontSize: '12px', fontWeight: 600, background: '#11998e20', color: '#11998e' }}>Success</span>
                                    ) : selectedLog.status === '2' ? (
                                        <span style={{ padding: '4px 10px', borderRadius: '12px', fontSize: '12px', fontWeight: 600, background: '#f59e0b20', color: '#f59e0b' }}>Running</span>
                                    ) : (
                                        <span style={{ padding: '4px 10px', borderRadius: '12px', fontSize: '12px', fontWeight: 600, background: '#f5576c20', color: '#f5576c' }}>Failed</span>
                                    )}
                                </div>
                            </div>
                            <div>
                                <label style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>Duration</label>
                                <div style={{ fontSize: '14px', fontFamily: 'monospace', marginTop: '4px' }}>
                                    {selectedLog.executionDuration ? `${selectedLog.executionDuration} ms` : '-'}
                                </div>
                            </div>
                            <div>
                                <label style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>Start Time</label>
                                <div style={{ fontSize: '13px', marginTop: '4px' }}>{selectedLog.startTime ? new Date(selectedLog.startTime).toLocaleString() : '-'}</div>
                            </div>
                            <div>
                                <label style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>End Time</label>
                                <div style={{ fontSize: '13px', marginTop: '4px' }}>{selectedLog.endTime ? new Date(selectedLog.endTime).toLocaleString() : '-'}</div>
                            </div>
                            <div>
                                <label style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>Retry Count</label>
                                <div style={{ fontSize: '14px', marginTop: '4px' }}>{selectedLog.retryCount || 0}</div>
                            </div>
                            <div>
                                <label style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>Invoke Target</label>
                                <div style={{ fontSize: '12px', fontFamily: 'monospace', marginTop: '4px', wordBreak: 'break-all' }}>{selectedLog.invokeTarget || '-'}</div>
                            </div>
                        </div>

                        {selectedLog.jobMessage && (
                            <div style={{ marginBottom: '16px' }}>
                                <label style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>Message</label>
                                <div style={{ fontSize: '13px', marginTop: '4px', padding: '12px', background: 'var(--bg-secondary)', borderRadius: '8px' }}>
                                    {selectedLog.jobMessage}
                                </div>
                            </div>
                        )}

                        {selectedLog.exceptionInfo && (
                            <div>
                                <label style={{ fontSize: '11px', color: 'var(--danger)', fontWeight: 600, textTransform: 'uppercase' }}>Exception</label>
                                <pre style={{
                                    marginTop: '4px',
                                    padding: '12px',
                                    background: '#f5576c10',
                                    border: '1px solid #f5576c40',
                                    borderRadius: '8px',
                                    fontFamily: 'monospace',
                                    fontSize: '11px',
                                    whiteSpace: 'pre-wrap',
                                    wordBreak: 'break-all',
                                    maxHeight: '300px',
                                    overflowY: 'auto',
                                    color: 'var(--danger)',
                                    margin: '4px 0 0 0'
                                }}>
{selectedLog.exceptionInfo}
                                </pre>
                            </div>
                        )}
                    </div>
                )}
            </Modal>
        </div>
    );
};

export default JobLogList;
