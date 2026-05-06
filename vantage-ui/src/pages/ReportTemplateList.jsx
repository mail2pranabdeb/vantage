import { useState, useEffect } from 'react';
import { FileText, Edit, Trash2, Play, Download, Plus, RefreshCw, Eye } from 'lucide-react';
import DataGrid from '../components/DataGrid';

const ReportTemplateList = () => {
    const [list, setList] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [modalMode, setModalMode] = useState('add');
    const [formData, setFormData] = useState({
        templateId: null,
        templateName: '',
        templateKey: '',
        description: '',
        datasourceKey: '',
        reportMode: 'SQL',
        sqlContent: '',
        outputFormat: 'EXCEL',
        status: '0'
    });
    const [datasources, setDatasources] = useState([]);

    useEffect(() => {
        fetchTemplates();
        fetchDatasources();
    }, []);

    const fetchTemplates = () => {
        setLoading(true);
        fetch('/api/system/report-designer/templates')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) setList(data.data || []);
                setLoading(false);
            })
            .catch(() => setLoading(false));
    };

    const fetchDatasources = () => {
        fetch('/api/system/datasource/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) setDatasources(data.data || []);
            });
    };

    const handleAdd = () => {
        setModalMode('add');
        setFormData({
            templateId: null,
            templateName: '',
            templateKey: '',
            description: '',
            datasourceKey: '',
            reportMode: 'SQL',
            sqlContent: '',
            outputFormat: 'EXCEL',
            status: '0'
        });
        setShowModal(true);
    };

    const handleEdit = (row) => {
        setModalMode('edit');
        setFormData(row);
        setShowModal(true);
    };

    const handleDelete = (row) => {
        if (!confirm(`Delete template "${row.templateName}"?`)) return;
        fetch(`/api/system/report-designer/templates/${row.templateId}`, { method: 'DELETE' })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    fetchTemplates();
                } else {
                    alert(data.msg || 'Delete failed');
                }
            });
    };

    const handleSave = () => {
        const url = modalMode === 'edit' ? `/api/system/report-designer/templates/${formData.templateId}` : '/api/system/report-designer/templates';
        const method = modalMode === 'edit' ? 'PUT' : 'POST';
        fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                setShowModal(false);
                fetchTemplates();
            } else {
                alert(data.msg || 'Save failed');
            }
        });
    };

    const handleExecute = (row) => {
        fetch(`/api/system/report-designer/execute/${row.templateId}?params={}`, { method: 'POST' })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    const rows = Array.isArray(data.data) ? data.data : (data.data?.data || []);
                    alert(`Report executed successfully. ${rows.length} rows returned.`);
                } else {
                    alert(data.msg || 'Execution failed');
                }
            });
    };

    const handleExport = (row, format) => {
        window.open(`/api/system/report-designer/export/${row.templateId}?params={}&format=${format}`, '_blank');
    };

    const columns = [
        { key: 'templateId', header: 'ID', align: 'center', sortable: true },
        {
            key: 'templateName',
            header: 'Template Name',
            sortable: true,
            render: (value, row) => (
                <div>
                    <div style={{ fontWeight: 600 }}>{value}</div>
                    <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{row.templateKey}</div>
                </div>
            )
        },
        { key: 'datasourceKey', header: 'Datasource', align: 'center', sortable: true },
        {
            key: 'reportMode',
            header: 'Mode',
            align: 'center',
            render: (value) => (
                <span style={{
                    padding: '4px 10px', borderRadius: '12px', fontSize: '11px', fontWeight: 600,
                    background: value === 'SQL' ? '#3b82f620' : '#10b98120',
                    color: value === 'SQL' ? '#3b82f6' : '#10b981'
                }}>
                    {value}
                </span>
            )
        },
        {
            key: 'outputFormat',
            header: 'Format',
            align: 'center',
            render: (value) => (
                <span style={{
                    padding: '4px 10px', borderRadius: '12px', fontSize: '11px', fontWeight: 600,
                    background: '#8b5cf620', color: '#8b5cf6'
                }}>
                    {value}
                </span>
            )
        },
        { key: 'createTime', header: 'Created', align: 'center', sortable: true, render: (v) => v ? new Date(v).toLocaleDateString() : '-' }
    ];

    const actions = [
        { label: 'View', icon: Eye, onClick: (row) => window.open(`/system/report-designer?id=${row.templateId}`, '_blank') },
        { label: 'Execute', icon: Play, onClick: handleExecute },
        { label: 'Export', icon: Download, onClick: (row) => handleExport(row, row.outputFormat) },
        { label: 'Edit', icon: Edit, onClick: handleEdit },
        { label: 'Delete', icon: Trash2, danger: true, onClick: handleDelete }
    ];

    const toolbarActions = [
        { label: 'Add Template', icon: Plus, onClick: handleAdd },
        { label: 'Refresh', icon: RefreshCw, onClick: fetchTemplates }
    ];

    return (
        <div className="page-container">
            <div className="page-header">
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{
                        width: '40px', height: '40px', borderRadius: '10px',
                        background: 'linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%)',
                        display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white'
                    }}>
                        <FileText size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Report Templates</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Manage saved report templates
                        </p>
                    </div>
                </div>
            </div>

            <DataGrid
                data={list}
                columns={columns}
                actions={actions}
                loading={loading}
                searchable={true}
                sortable={true}
                pagination={true}
                pageSize={10}
                emptyMessage="No report templates found."
                toolbarActions={toolbarActions}
            />

            {/* Modal */}
            {showModal && (
                <div style={{
                    position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
                    background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center',
                    justifyContent: 'center', zIndex: 1000
                }} onClick={() => setShowModal(false)}>
                    <div style={{
                        background: 'var(--bg-secondary)', borderRadius: '12px', padding: '24px',
                        width: '90%', maxWidth: '600px', maxHeight: '85vh', overflow: 'auto'
                    }} onClick={e => e.stopPropagation()}>
                        <h3 style={{ marginBottom: '20px' }}>{modalMode === 'add' ? 'Add' : 'Edit'} Template</h3>
                        
                        <div className="form-group">
                            <label className="form-label">Template Name</label>
                            <input className="form-input" value={formData.templateName}
                                onChange={e => setFormData(p => ({ ...p, templateName: e.target.value }))}
                                placeholder="e.g., Monthly Sales Report" />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Template Key</label>
                            <input className="form-input" value={formData.templateKey}
                                onChange={e => setFormData(p => ({ ...p, templateKey: e.target.value }))}
                                placeholder="e.g., monthly_sales_report" />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Datasource</label>
                            <select className="form-input" value={formData.datasourceKey}
                                onChange={e => setFormData(p => ({ ...p, datasourceKey: e.target.value }))}>
                                <option value="">-- Select --</option>
                                {datasources.map(ds => (
                                    <option key={ds.datasourceKey} value={ds.datasourceKey}>{ds.datasourceName}</option>
                                ))}
                            </select>
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label className="form-label">Report Mode</label>
                                <select className="form-input" value={formData.reportMode}
                                    onChange={e => setFormData(p => ({ ...p, reportMode: e.target.value }))}>
                                    <option value="SQL">SQL Mode</option>
                                    <option value="VISUAL_BUILDER">Visual Builder</option>
                                    <option value="HYBRID">Hybrid</option>
                                </select>
                            </div>
                            <div className="form-group">
                                <label className="form-label">Output Format</label>
                                <select className="form-input" value={formData.outputFormat}
                                    onChange={e => setFormData(p => ({ ...p, outputFormat: e.target.value }))}>
                                    <option value="EXCEL">Excel</option>
                                    <option value="CSV">CSV</option>
                                    <option value="HTML">HTML</option>
                                    <option value="JSON">JSON</option>
                                </select>
                            </div>
                        </div>

                        <div className="form-group">
                            <label className="form-label">SQL Query</label>
                            <textarea className="form-input" rows="8" style={{ fontFamily: 'monospace', fontSize: '12px' }}
                                value={formData.sqlContent}
                                onChange={e => setFormData(p => ({ ...p, sqlContent: e.target.value }))}
                                placeholder="SELECT * FROM table WHERE condition = :param" />
                        </div>

                        <div className="form-group">
                            <label className="form-label">Description</label>
                            <input className="form-input" value={formData.description}
                                onChange={e => setFormData(p => ({ ...p, description: e.target.value }))}
                                placeholder="Brief description of this report" />
                        </div>

                        <div style={{ display: 'flex', gap: '8px', marginTop: '20px', justifyContent: 'flex-end' }}>
                            <button className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                            <button className="btn btn-primary" onClick={handleSave}>
                                {modalMode === 'add' ? 'Create' : 'Update'} Template
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ReportTemplateList;
