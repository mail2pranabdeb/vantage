import { useState, useEffect } from 'react';
import { FileText, Plus, Edit, Trash2, RefreshCw, Play, Download } from 'lucide-react';
import { useToast } from '../components/Toast';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';

const ReportList = () => {
    const { addToast } = useToast();
    const [reports, setReports] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('add');
    const [currentReport, setCurrentReport] = useState(null);
    const [formData, setFormData] = useState({
        reportName: '',
        reportKey: '',
        reportType: 'SQL',
        datasourceKey: 'master',
        sqlContent: '',
        paramsConfig: '',
        columnsConfig: '',
        outputFormat: 'EXCEL',
        status: '0',
        remark: ''
    });
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        fetchReports();
    }, []);

    const fetchReports = () => {
        setLoading(true);
        fetch('/api/system/report/list')
            .then(res => res.json())
            .then(data => {
                setLoading(false);
                if (data.code === 200) {
                    setReports(data.data || []);
                    addToast('success', `Loaded ${data.data.length} report(s)`, 2000);
                } else {
                    addToast('error', data.msg || 'Failed to load reports', 4000);
                }
            })
            .catch(err => {
                console.error("Failed to fetch reports:", err);
                setLoading(false);
                addToast('error', 'Failed to load reports', 5000);
            });
    };

    const handleAddClick = () => {
        setModalMode('add');
        setCurrentReport(null);
        setFormData({
            reportName: '',
            reportKey: '',
            reportType: 'SQL',
            datasourceKey: 'master',
            sqlContent: '',
            paramsConfig: '',
            columnsConfig: '',
            outputFormat: 'EXCEL',
            status: '0',
            remark: ''
        });
        setIsModalOpen(true);
    };

    const handleEditClick = (row) => {
        setModalMode('edit');
        setCurrentReport(row);
        setFormData({
            reportName: row.reportName || '',
            reportKey: row.reportKey || '',
            reportType: row.reportType || 'SQL',
            datasourceKey: row.datasourceKey || 'master',
            sqlContent: row.sqlContent || '',
            paramsConfig: row.paramsConfig || '',
            columnsConfig: row.columnsConfig || '',
            outputFormat: row.outputFormat || 'EXCEL',
            status: row.status || '0',
            remark: row.remark || ''
        });
        setIsModalOpen(true);
    };

    const handleDeleteClick = (row) => {
        if (window.confirm(`Are you sure you want to delete report "${row.reportName}"?`)) {
            fetch(`/api/system/report/${row.reportId}`, {
                method: 'DELETE'
            })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    addToast('success', `Report "${row.reportName}" deleted successfully`, 3000);
                    fetchReports();
                } else {
                    addToast('error', data.msg || 'Failed to delete report', 5000);
                }
            })
            .catch(err => {
                console.error("Failed to delete report:", err);
                addToast('error', 'Failed to delete report', 5000);
            });
        }
    };

    const handleExecuteClick = (row) => {
        addToast('info', 'Report execution - coming soon', 3000);
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = () => {
        if (!formData.reportName || !formData.reportKey || !formData.sqlContent) {
            addToast('error', 'Report name, key, and SQL content are required', 3000);
            return;
        }

        setSubmitting(true);

        const url = modalMode === 'add' ? '/api/system/report' : '/api/system/report';
        const method = modalMode === 'add' ? 'POST' : 'PUT';
        const body = { 
            ...formData, 
            reportId: modalMode === 'edit' ? currentReport.reportId : null
        };

        fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        })
        .then(res => res.json())
        .then(data => {
            setSubmitting(false);
            if (data.code === 200) {
                setIsModalOpen(false);
                addToast('success', data.msg || `Report ${modalMode === 'add' ? 'added' : 'updated'} successfully`, 3000);
                fetchReports();
            } else {
                addToast('error', data.msg || `Failed to ${modalMode} report`, 5000);
            }
        })
        .catch(err => {
            setSubmitting(false);
            console.error(`Failed to ${modalMode} report:`, err);
            addToast('error', `Failed to ${modalMode} report`, 5000);
        });
    };

    const toolbarActions = [
        {
            label: 'Refresh',
            icon: RefreshCw,
            onClick: fetchReports
        },
        {
            label: 'Add Report',
            icon: Plus,
            onClick: handleAddClick
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
                        <h2 style={{ fontSize: '14px', fontWeight: 700, margin: 0 }}>Report Management</h2>
                        <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Define and execute custom SQL reports
                        </p>
                    </div>
                </div>
                <div style={{ display: 'flex', gap: '8px' }}>
                    {toolbarActions.map((action, idx) => (
                        <button
                            key={idx}
                            className="btn btn-primary"
                            onClick={action.onClick}
                            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                        >
                            <action.icon size={16} />
                            {action.label}
                        </button>
                    ))}
                </div>
            </div>

            {/* Reports Table */}
            <div style={{
                background: 'var(--bg-secondary)',
                borderRadius: '8px',
                border: '1px solid var(--border-color)',
                overflow: 'hidden'
            }}>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                        <tr style={{ background: 'var(--bg-tertiary)', borderBottom: '2px solid var(--border-color)' }}>
                            <th style={{ padding: '10px', textAlign: 'left' }}>Report Name</th>
                            <th style={{ padding: '10px', textAlign: 'left' }}>Report Key</th>
                            <th style={{ padding: '10px', textAlign: 'center' }}>Type</th>
                            <th style={{ padding: '10px', textAlign: 'center' }}>Format</th>
                            <th style={{ padding: '10px', textAlign: 'center' }}>Status</th>
                            <th style={{ padding: '10px', textAlign: 'center', width: '280px' }}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {loading ? (
                            <tr>
                                <td colSpan={6} style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                                    Loading reports...
                                </td>
                            </tr>
                        ) : reports.length === 0 ? (
                            <tr>
                                <td colSpan={6} style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                                    No reports found. Click "Add Report" to create one.
                                </td>
                            </tr>
                        ) : (
                            reports.map(report => (
                                <tr key={report.reportId} style={{ borderBottom: '1px solid var(--border-color)' }}>
                                    <td style={{ padding: '10px', fontWeight: 500 }}>{report.reportName}</td>
                                    <td style={{ padding: '10px', fontFamily: 'monospace', fontSize: '12px' }}>{report.reportKey}</td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}>{report.reportType}</td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}>{report.outputFormat}</td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}>
                                        <span className={`status-pill ${report.status === '0' ? 'active' : 'inactive'}`}>
                                            {report.status === '0' ? 'Active' : 'Disabled'}
                                        </span>
                                    </td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}>
                                        <div style={{ display: 'flex', gap: '4px', justifyContent: 'center' }}>
                                            <button
                                                onClick={() => handleExecuteClick(report)}
                                                className="btn btn-secondary"
                                                style={{ padding: '4px 8px', fontSize: '11px' }}
                                                title="Execute"
                                            >
                                                <Play size={14} />
                                            </button>
                                            <button
                                                onClick={() => handleEditClick(report)}
                                                className="btn btn-secondary"
                                                style={{ padding: '4px 8px', fontSize: '11px' }}
                                                title="Edit"
                                            >
                                                <Edit size={14} />
                                            </button>
                                            <button
                                                onClick={() => handleDeleteClick(report)}
                                                className="btn btn-secondary"
                                                style={{ 
                                                    padding: '4px 8px', 
                                                    fontSize: '11px',
                                                    color: 'var(--danger)',
                                                    borderColor: 'var(--danger)'
                                                }}
                                                title="Delete"
                                            >
                                                <Trash2 size={14} />
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {/* Add/Edit Modal */}
            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title={modalMode === 'add' ? 'Add Report' : 'Edit Report'}
                size="large"
                footer={
                    <>
                        <button
                            className="btn btn-secondary"
                            onClick={() => setIsModalOpen(false)}
                            disabled={submitting}
                        >
                            Cancel
                        </button>
                        <button
                            className="btn btn-primary"
                            onClick={handleSubmit}
                            disabled={submitting}
                            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                        >
                            {submitting && (
                                <div style={{
                                    width: '12px',
                                    height: '12px',
                                    border: '2px solid white',
                                    borderBottomColor: 'transparent',
                                    borderRadius: '50%',
                                    animation: 'spin 1s linear infinite'
                                }} />
                            )}
                            {modalMode === 'add' ? 'Create' : 'Save'}
                        </button>
                    </>
                }
            >
                <div style={{ maxHeight: '70vh', overflowY: 'auto', paddingRight: '8px' }}>
                    <div className="form-row">
                        <FormInput
                            label="Report Name *"
                            name="reportName"
                            value={formData.reportName}
                            onChange={handleInputChange}
                            placeholder="e.g., User Activity Report"
                            disabled={submitting}
                        />
                        <FormInput
                            label="Report Key *"
                            name="reportKey"
                            value={formData.reportKey}
                            onChange={handleInputChange}
                            placeholder="e.g., user_activity"
                            disabled={submitting || modalMode === 'edit'}
                        />
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label className="form-label">Report Type</label>
                            <select
                                name="reportType"
                                value={formData.reportType}
                                onChange={handleInputChange}
                                className="form-input"
                                disabled={submitting}
                            >
                                <option value="SQL">SQL Query</option>
                                <option value="STORED_PROC">Stored Procedure</option>
                            </select>
                        </div>
                        <div className="form-group">
                            <label className="form-label">Output Format</label>
                            <select
                                name="outputFormat"
                                value={formData.outputFormat}
                                onChange={handleInputChange}
                                className="form-input"
                                disabled={submitting}
                            >
                                <option value="EXCEL">Excel</option>
                                <option value="PDF">PDF</option>
                                <option value="CSV">CSV</option>
                                <option value="HTML">HTML</option>
                            </select>
                        </div>
                        <div className="form-group">
                            <label className="form-label">Status</label>
                            <select
                                name="status"
                                value={formData.status}
                                onChange={handleInputChange}
                                className="form-input"
                                disabled={submitting}
                            >
                                <option value="0">Active</option>
                                <option value="1">Disabled</option>
                            </select>
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="form-label">SQL Content *</label>
                        <textarea
                            name="sqlContent"
                            value={formData.sqlContent}
                            onChange={handleInputChange}
                            placeholder="SELECT * FROM sys_user WHERE status = '0'"
                            rows={10}
                            className="form-input"
                            style={{ fontFamily: 'monospace', fontSize: '12px' }}
                            disabled={submitting}
                        />
                        <small className="form-help">Enter SQL query. Use :paramName for parameters</small>
                    </div>

                    <div className="form-row">
                        <FormInput
                            label="Parameters Config (JSON)"
                            name="paramsConfig"
                            value={formData.paramsConfig}
                            onChange={handleInputChange}
                            placeholder='[{"name":"status","type":"string","default":"0"}]'
                            disabled={submitting}
                        />
                        <FormInput
                            label="Columns Config (JSON)"
                            name="columnsConfig"
                            value={formData.columnsConfig}
                            onChange={handleInputChange}
                            placeholder='[{"field":"user_name","header":"User Name"}]'
                            disabled={submitting}
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label">Remark</label>
                        <textarea
                            name="remark"
                            value={formData.remark}
                            onChange={handleInputChange}
                            placeholder="Optional description"
                            rows={2}
                            className="form-input"
                            disabled={submitting}
                        />
                    </div>
                </div>
            </Modal>
        </div>
    );
};

export default ReportList;
