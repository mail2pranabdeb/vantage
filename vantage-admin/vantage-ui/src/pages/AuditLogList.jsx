import { useState, useEffect } from 'react';
import { FileText, Eye, Search, RefreshCw, Filter } from 'lucide-react';
import { useToast } from '../components/Toast';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';

const AuditLogList = () => {
    const { addToast } = useToast();
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [total, setTotal] = useState(0);
    const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);
    const [selectedLog, setSelectedLog] = useState(null);
    const [filters, setFilters] = useState({
        tableName: '',
        operator: '',
        module: '',
        startTime: '',
        endTime: ''
    });
    const [pagination, setPagination] = useState({
        pageNum: 1,
        pageSize: 20
    });

    useEffect(() => {
        fetchLogs();
    }, [pagination]);

    const fetchLogs = () => {
        setLoading(true);
        const params = new URLSearchParams({
            pageNum: pagination.pageNum.toString(),
            pageSize: pagination.pageSize.toString()
        });

        if (filters.tableName) params.append('tableName', filters.tableName);
        if (filters.operator) params.append('operator', filters.operator);
        if (filters.module) params.append('module', filters.module);
        if (filters.startTime) params.append('startTime', filters.startTime);
        if (filters.endTime) params.append('endTime', filters.endTime);

        fetch(`/api/system/audit/list?${params}`)
            .then(res => res.json())
            .then(data => {
                setLoading(false);
                if (data.code === 200) {
                    setLogs(data.rows || []);
                    setTotal(data.total || 0);
                } else {
                    addToast('error', data.msg || 'Failed to load audit logs', 4000);
                }
            })
            .catch(err => {
                console.error("Failed to fetch audit logs:", err);
                setLoading(false);
                addToast('error', 'Failed to load audit logs', 5000);
            });
    };

    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({ ...prev, [name]: value }));
    };

    const handleSearch = () => {
        setPagination(prev => ({ ...prev, pageNum: 1 }));
        fetchLogs();
    };

    const handleReset = () => {
        setFilters({
            tableName: '',
            operator: '',
            module: '',
            startTime: '',
            endTime: ''
        });
        setPagination({ pageNum: 1, pageSize: 20 });
    };

    const handleViewDetails = (row) => {
        setSelectedLog(row);
        setIsDetailsModalOpen(true);
    };

    const parseJSON = (jsonString) => {
        try {
            return JSON.parse(jsonString);
        } catch (e) {
            return jsonString;
        }
    };

    const columns = [
        { key: 'operationTime', header: 'Timestamp', sortable: true },
        { key: 'tableName', header: 'Table', sortable: true },
        { key: 'recordId', header: 'Record ID' },
        { 
            key: 'operationType', 
            header: 'Operation',
            render: (value) => (
                <span className={`badge ${
                    value === 'INSERT' ? 'badge-success' : 
                    value === 'UPDATE' ? 'badge-warning' : 'badge-danger'
                }`} style={{ 
                    padding: '4px 8px', 
                    borderRadius: '4px', 
                    fontSize: '11px',
                    background: value === 'INSERT' ? '#dcfce7' : value === 'UPDATE' ? '#fef3c7' : '#fee2e2',
                    color: value === 'INSERT' ? '#166534' : value === 'UPDATE' ? '#92400e' : '#991b1b'
                }}>
                    {value}
                </span>
            )
        },
        { key: 'operator', header: 'User', sortable: true },
        { key: 'operatorIp', header: 'IP Address' },
        { key: 'module', header: 'Module' },
        {
            key: 'actions',
            header: 'Actions',
            maxWidth: '100px',
            render: (value, row) => (
                <button
                    className="btn btn-secondary"
                    onClick={() => handleViewDetails(row)}
                    style={{ padding: '4px 8px', fontSize: '11px' }}
                    title="View Details"
                >
                    <Eye size={14} />
                </button>
            )
        }
    ];

    return (
        <div style={{
            height: 'calc(100vh - 50px)',
            overflow: 'auto',
            padding: '8px'
        }}>
            <div className="page-header" style={{ marginBottom: '12px' }}>
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
                        <FileText size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '14px', fontWeight: 700, margin: 0 }}>Audit Trail</h2>
                        <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Track all system changes and operations
                        </p>
                    </div>
                </div>
                <button
                    className="btn btn-secondary"
                    onClick={fetchLogs}
                    style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                >
                    <RefreshCw size={16} />
                    Refresh
                </button>
            </div>

            {/* Filters */}
            <div style={{
                background: 'var(--bg-secondary)',
                padding: '16px',
                borderRadius: '8px',
                marginBottom: '12px',
                border: '1px solid var(--border-color)'
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px' }}>
                    <Filter size={16} style={{ color: 'var(--text-secondary)' }} />
                    <h3 style={{ fontSize: '13px', fontWeight: 600, margin: 0 }}>Filters</h3>
                </div>
                <div className="form-row" style={{ marginBottom: '12px' }}>
                    <FormInput
                        label="Table Name"
                        name="tableName"
                        value={filters.tableName}
                        onChange={handleFilterChange}
                        placeholder="e.g., sys_user"
                    />
                    <FormInput
                        label="Operator"
                        name="operator"
                        value={filters.operator}
                        onChange={handleFilterChange}
                        placeholder="e.g., admin"
                    />
                    <FormInput
                        label="Module"
                        name="module"
                        value={filters.module}
                        onChange={handleFilterChange}
                        placeholder="e.g., User Management"
                    />
                </div>
                <div className="form-row">
                    <div className="form-group">
                        <label className="form-label">Start Time</label>
                        <input
                            type="datetime-local"
                            name="startTime"
                            value={filters.startTime}
                            onChange={handleFilterChange}
                            className="form-input"
                        />
                    </div>
                    <div className="form-group">
                        <label className="form-label">End Time</label>
                        <input
                            type="datetime-local"
                            name="endTime"
                            value={filters.endTime}
                            onChange={handleFilterChange}
                            className="form-input"
                        />
                    </div>
                    <div className="form-group" style={{ display: 'flex', alignItems: 'flex-end', gap: '8px' }}>
                        <button
                            className="btn btn-primary"
                            onClick={handleSearch}
                            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                        >
                            <Search size={16} />
                            Search
                        </button>
                        <button
                            className="btn btn-secondary"
                            onClick={handleReset}
                        >
                            Reset
                        </button>
                    </div>
                </div>
            </div>

            {/* Data Grid */}
            <div style={{
                background: 'var(--bg-secondary)',
                borderRadius: '8px',
                border: '1px solid var(--border-color)',
                overflow: 'hidden'
            }}>
                {loading ? (
                    <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                        Loading audit logs...
                    </div>
                ) : logs.length === 0 ? (
                    <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                        <FileText size={48} style={{ margin: '0 auto 16px', opacity: 0.3 }} />
                        <p>No audit logs found</p>
                    </div>
                ) : (
                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead>
                            <tr style={{ background: 'var(--bg-tertiary)', borderBottom: '2px solid var(--border-color)' }}>
                                {columns.map(col => (
                                    <th key={col.key} style={{ 
                                        padding: '10px', 
                                        textAlign: 'left',
                                        maxWidth: col.maxWidth
                                    }}>
                                        {col.header}
                                    </th>
                                ))}
                            </tr>
                        </thead>
                        <tbody>
                            {logs.map((row, idx) => (
                                <tr key={row.auditId || idx} style={{ 
                                    borderBottom: '1px solid var(--border-color)',
                                    background: idx % 2 === 0 ? 'var(--bg-secondary)' : 'var(--bg-tertiary)'
                                }}>
                                    {columns.map(col => (
                                        <td key={col.key} style={{ padding: '10px', maxWidth: col.maxWidth }}>
                                            {col.render ? col.render(row[col.key], row) : (
                                                <span style={{ fontSize: '13px' }}>{row[col.key]}</span>
                                            )}
                                        </td>
                                    ))}
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}

                {/* Pagination */}
                {logs.length > 0 && (
                    <div style={{
                        padding: '12px',
                        borderTop: '1px solid var(--border-color)',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center'
                    }}>
                        <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                            Total: {total} records
                        </div>
                        <div style={{ display: 'flex', gap: '4px' }}>
                            <button
                                className="btn btn-secondary"
                                onClick={() => setPagination(prev => ({ ...prev, pageNum: prev.pageNum - 1 }))}
                                disabled={pagination.pageNum === 1}
                                style={{ padding: '6px 12px', fontSize: '12px' }}
                            >
                                Previous
                            </button>
                            <span style={{ padding: '6px 12px', fontSize: '12px' }}>
                                Page {pagination.pageNum}
                            </span>
                            <button
                                className="btn btn-secondary"
                                onClick={() => setPagination(prev => ({ ...prev, pageNum: prev.pageNum + 1 }))}
                                disabled={pagination.pageNum * pagination.pageSize >= total}
                                style={{ padding: '6px 12px', fontSize: '12px' }}
                            >
                                Next
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* Details Modal */}
            <Modal
                isOpen={isDetailsModalOpen}
                onClose={() => setIsDetailsModalOpen(false)}
                title="Audit Log Details"
                size="large"
                footer={
                    <button
                        className="btn btn-secondary"
                        onClick={() => setIsDetailsModalOpen(false)}
                    >
                        Close
                    </button>
                }
            >
                {selectedLog && (
                    <div style={{ maxHeight: '60vh', overflowY: 'auto' }}>
                        <div style={{ marginBottom: '20px' }}>
                            <h3 style={{ fontSize: '14px', fontWeight: 600, marginBottom: '12px' }}>Basic Information</h3>
                            <div className="form-row">
                                <div className="form-group">
                                    <label className="form-label">Operation Type</label>
                                    <div>{selectedLog.operationType}</div>
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Table</label>
                                    <div>{selectedLog.tableName}</div>
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Record ID</label>
                                    <div>{selectedLog.recordId}</div>
                                </div>
                            </div>
                            <div className="form-row">
                                <div className="form-group">
                                    <label className="form-label">Operator</label>
                                    <div>{selectedLog.operator}</div>
                                </div>
                                <div className="form-group">
                                    <label className="form-label">IP Address</label>
                                    <div>{selectedLog.operatorIp}</div>
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Timestamp</label>
                                    <div>{selectedLog.operationTime}</div>
                                </div>
                            </div>
                            {selectedLog.module && (
                                <div className="form-group">
                                    <label className="form-label">Module</label>
                                    <div>{selectedLog.module}</div>
                                </div>
                            )}
                            {selectedLog.changedFields && (
                                <div className="form-group">
                                    <label className="form-label">Changed Fields</label>
                                    <div style={{ fontFamily: 'monospace', fontSize: '12px' }}>
                                        {selectedLog.changedFields.split(',').join(', ')}
                                    </div>
                                </div>
                            )}
                        </div>

                        {selectedLog.oldValues && (
                            <div style={{ marginBottom: '20px' }}>
                                <h3 style={{ fontSize: '14px', fontWeight: 600, marginBottom: '8px' }}>Before (Old Values)</h3>
                                <pre style={{
                                    background: 'var(--bg-tertiary)',
                                    padding: '12px',
                                    borderRadius: '6px',
                                    fontFamily: 'monospace',
                                    fontSize: '11px',
                                    overflow: 'auto',
                                    maxHeight: '200px'
                                }}>
                                    {JSON.stringify(parseJSON(selectedLog.oldValues), null, 2)}
                                </pre>
                            </div>
                        )}

                        {selectedLog.newValues && (
                            <div>
                                <h3 style={{ fontSize: '14px', fontWeight: 600, marginBottom: '8px' }}>After (New Values)</h3>
                                <pre style={{
                                    background: 'var(--bg-tertiary)',
                                    padding: '12px',
                                    borderRadius: '6px',
                                    fontFamily: 'monospace',
                                    fontSize: '11px',
                                    overflow: 'auto',
                                    maxHeight: '200px'
                                }}>
                                    {JSON.stringify(parseJSON(selectedLog.newValues), null, 2)}
                                </pre>
                            </div>
                        )}
                    </div>
                )}
            </Modal>
        </div>
    );
};

export default AuditLogList;
