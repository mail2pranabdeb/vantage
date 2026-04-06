import { useState, useEffect } from 'react';
import { FileText, Plus, Edit, Trash2, RefreshCw, Play, Download, Calendar, Clock, Mail } from 'lucide-react';
import { useToast } from '../components/Toast';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';

const ReportList = () => {
    const { addToast } = useToast();
    const [reports, setReports] = useState([]);
    const [templates, setTemplates] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isTemplateModalOpen, setIsTemplateModalOpen] = useState(false);
    const [isScheduleModalOpen, setIsScheduleModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('add');
    const [currentReport, setCurrentReport] = useState(null);
    const [selectedTemplate, setSelectedTemplate] = useState(null);
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
        remark: '',
        scheduleEnabled: false,
        scheduleCron: '',
        emailEnabled: false,
        emailRecipients: '',
        emailSubject: ''
    });
    const [scheduleData, setScheduleData] = useState({
        cronExpression: '',
        recipients: '',
        ccEmails: '',
        subject: '',
        body: '',
        format: 'EXCEL'
    });
    const [submitting, setSubmitting] = useState(false);

    const cronPresets = [
        { label: 'Every Hour', value: '0 0 * * * ?' },
        { label: 'Every 6 Hours', value: '0 0 0/6 * * ?' },
        { label: 'Daily at 9 AM', value: '0 0 9 * * ?' },
        { label: 'Daily at 6 PM', value: '0 0 18 * * ?' },
        { label: 'Weekdays at 9 AM', value: '0 0 9 ? * MON-FRI' },
        { label: 'Weekly Sunday', value: '0 0 12 ? * SUN' },
        { label: 'Monthly 1st at 9 AM', value: '0 0 9 1 * ?' }
    ];

    useEffect(() => {
        fetchReports();
        fetchTemplates();
    }, []);

    const fetchReports = () => {
        setLoading(true);
        fetch('/api/system/report/list')
            .then(res => res.json())
            .then(data => {
                setLoading(false);
                if (data.code === 200) {
                    setReports(data.data || []);
                } else {
                    addToast('error', data.msg || 'Failed to load reports', 4000);
                }
            })
            .catch(() => {
                setLoading(false);
                addToast('error', 'Failed to load reports', 5000);
            });
    };

    const fetchTemplates = () => {
        fetch('/api/system/report/templates')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) setTemplates(data.data || []);
            });
    };

    const handleAddClick = () => {
        setModalMode('add');
        setCurrentReport(null);
        setSelectedTemplate(null);
        setFormData({
            reportName: '', reportKey: '', reportType: 'SQL',
            datasourceKey: 'master', sqlContent: '', paramsConfig: '',
            columnsConfig: '', outputFormat: 'EXCEL', status: '0', remark: ''
        });
        setIsModalOpen(true);
    };

    const handleCreateFromTemplate = () => {
        setIsTemplateModalOpen(true);
    };

    const handleTemplateSelect = (template) => {
        setSelectedTemplate(template);
        setFormData(prev => ({
            ...prev,
            reportName: template.templateName + ' Report',
            reportKey: template.templateKey + '_report',
            datasourceKey: template.datasourceKey,
            sqlContent: template.sqlContent || '',
            columnsConfig: template.columnsConfig || '',
            paramsConfig: template.filtersConfig || '',
            outputFormat: template.outputFormat || 'EXCEL'
        }));
    };

    const handleCreateFromTemplateSubmit = () => {
        if (!selectedTemplate) {
            addToast('error', 'Please select a template', 3000);
            return;
        }
        if (!formData.reportName || !formData.reportKey) {
            addToast('error', 'Report name and key are required', 3000);
            return;
        }

        setSubmitting(true);
        fetch('/api/system/report/from-template', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                templateId: selectedTemplate.templateId,
                reportName: formData.reportName,
                reportKey: formData.reportKey,
                scheduleCron: formData.scheduleCron || '',
                emailRecipients: formData.emailRecipients || '',
                emailSubject: formData.emailSubject || ''
            })
        })
        .then(res => res.json())
        .then(data => {
            setSubmitting(false);
            if (data.code === 200) {
                setIsTemplateModalOpen(false);
                addToast('success', 'Report created from template successfully!', 3000);
                fetchReports();
            } else {
                addToast('error', data.msg || 'Failed to create report', 5000);
            }
        })
        .catch(() => {
            setSubmitting(false);
            addToast('error', 'Failed to create report', 5000);
        });
    };

    const handleScheduleClick = (row) => {
        setCurrentReport(row);
        setScheduleData({
            cronExpression: row.scheduleCron || '',
            recipients: row.emailRecipients || '',
            ccEmails: '',
            subject: row.emailSubject || row.reportName,
            body: 'Please find the attached report.',
            format: row.outputFormat || 'EXCEL'
        });
        setIsScheduleModalOpen(true);
    };

    const handleScheduleSubmit = () => {
        if (!scheduleData.cronExpression || !scheduleData.recipients) {
            addToast('error', 'Cron expression and recipients are required', 3000);
            return;
        }

        setSubmitting(true);
        fetch(`/api/system/report/schedule/${currentReport.reportId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(scheduleData)
        })
        .then(res => res.json())
        .then(data => {
            setSubmitting(false);
            if (data.code === 200) {
                setIsScheduleModalOpen(false);
                addToast('success', `Report scheduled! Next run: ${data.data?.nextFireTime || 'N/A'}`, 5000);
                fetchReports();
            } else {
                addToast('error', data.msg || 'Failed to schedule report', 5000);
            }
        })
        .catch(() => {
            setSubmitting(false);
            addToast('error', 'Failed to schedule report', 5000);
        });
    };

    const handleUnscheduleClick = (row) => {
        if (!confirm('Unschedule this report?')) return;
        fetch(`/api/system/report/unschedule/${row.reportId}`, { method: 'DELETE' })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    addToast('success', 'Report unscheduled', 3000);
                    fetchReports();
                } else {
                    addToast('error', data.msg || 'Failed', 5000);
                }
            });
    };

    const handleEditClick = (row) => {
        setModalMode('edit');
        setCurrentReport(row);
        setFormData({
            reportName: row.reportName || '', reportKey: row.reportKey || '',
            reportType: row.reportType || 'SQL', datasourceKey: row.datasourceKey || 'master',
            sqlContent: row.sqlContent || '', paramsConfig: row.paramsConfig || '',
            columnsConfig: row.columnsConfig || '', outputFormat: row.outputFormat || 'EXCEL',
            status: row.status || '0', remark: row.remark || '',
            scheduleEnabled: row.scheduleEnabled || false, scheduleCron: row.scheduleCron || '',
            emailEnabled: row.emailEnabled || false, emailRecipients: row.emailRecipients || '',
            emailSubject: row.emailSubject || ''
        });
        setIsModalOpen(true);
    };

    const handleDeleteClick = (row) => {
        if (window.confirm(`Delete report "${row.reportName}"?`)) {
            fetch(`/api/system/report/${row.reportId}`, { method: 'DELETE' })
                .then(res => res.json())
                .then(data => {
                    if (data.code === 200) {
                        addToast('success', `Report deleted`, 3000);
                        fetchReports();
                    } else {
                        addToast('error', data.msg || 'Failed', 5000);
                    }
                });
        }
    };

    const handleExecuteClick = (row) => {
        fetch(`/api/system/report/execute/${row.reportId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({})
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                addToast('success', `${data.data?.length || 0} rows returned`, 5000);
                if (window.confirm('Download results as Excel?')) {
                    window.open(`/api/system/report/download/${row.reportId}?format=EXCEL`, '_blank');
                }
            } else {
                addToast('error', data.msg || 'Failed', 5000);
            }
        });
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = () => {
        if (!formData.reportName || !formData.reportKey || !formData.sqlContent) {
            addToast('error', 'Report name, key, and SQL are required', 3000);
            return;
        }
        setSubmitting(true);
        const url = '/api/system/report';
        const method = modalMode === 'add' ? 'POST' : 'PUT';
        const body = { ...formData, reportId: modalMode === 'edit' ? currentReport.reportId : null };

        fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) })
            .then(res => res.json())
            .then(data => {
                setSubmitting(false);
                if (data.code === 200) {
                    setIsModalOpen(false);
                    addToast('success', `Report ${modalMode === 'add' ? 'added' : 'updated'}`, 3000);
                    fetchReports();
                } else {
                    addToast('error', data.msg || 'Failed', 5000);
                }
            })
            .catch(() => {
                setSubmitting(false);
                addToast('error', 'Failed', 5000);
            });
    };

    return (
        <div style={{ height: 'calc(100vh - 50px)', overflow: 'auto', padding: '8px' }}>
            <div className="page-header" style={{ marginBottom: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{
                        width: '40px', height: '40px', borderRadius: '10px',
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white'
                    }}>
                        <FileText size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '14px', fontWeight: 700, margin: 0 }}>Report Management</h2>
                        <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Define, execute, and schedule reports
                        </p>
                    </div>
                </div>
                <div style={{ display: 'flex', gap: '8px' }}>
                    <button className="btn btn-secondary" onClick={handleCreateFromTemplate} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <FileText size={16} /> From Template
                    </button>
                    <button className="btn btn-primary" onClick={handleAddClick} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <Plus size={16} /> Add Report
                    </button>
                    <button className="btn btn-secondary" onClick={fetchReports} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <RefreshCw size={16} /> Refresh
                    </button>
                </div>
            </div>

            {/* Reports Table */}
            <div style={{ background: 'var(--bg-secondary)', borderRadius: '8px', border: '1px solid var(--border-color)', overflow: 'hidden' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                        <tr style={{ background: 'var(--bg-tertiary)', borderBottom: '2px solid var(--border-color)' }}>
                            <th style={{ padding: '10px', textAlign: 'left' }}>Report Name</th>
                            <th style={{ padding: '10px', textAlign: 'center' }}>Format</th>
                            <th style={{ padding: '10px', textAlign: 'center' }}>Schedule</th>
                            <th style={{ padding: '10px', textAlign: 'center' }}>Status</th>
                            <th style={{ padding: '10px', textAlign: 'center', width: '320px' }}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {loading ? (
                            <tr><td colSpan={5} style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>Loading...</td></tr>
                        ) : reports.length === 0 ? (
                            <tr><td colSpan={5} style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>No reports found</td></tr>
                        ) : (
                            reports.map(report => (
                                <tr key={report.reportId} style={{ borderBottom: '1px solid var(--border-color)' }}>
                                    <td style={{ padding: '10px' }}>
                                        <div style={{ fontWeight: 600 }}>{report.reportName}</div>
                                        <div style={{ fontSize: '11px', color: 'var(--text-muted)', fontFamily: 'monospace' }}>{report.reportKey}</div>
                                    </td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}>
                                        <span style={{ padding: '4px 10px', borderRadius: '12px', fontSize: '11px', fontWeight: 600, background: '#8b5cf620', color: '#8b5cf6' }}>{report.outputFormat}</span>
                                    </td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}>
                                        {report.scheduleEnabled ? (
                                            <span style={{ fontSize: '11px', color: 'var(--success)' }}>
                                                <Clock size={12} style={{ display: 'inline', marginRight: '4px' }} />
                                                {report.scheduleCron}
                                            </span>
                                        ) : (
                                            <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Not scheduled</span>
                                        )}
                                    </td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}>
                                        <span className={`status-pill ${report.status === '0' ? 'active' : 'inactive'}`}>
                                            {report.status === '0' ? 'Active' : 'Disabled'}
                                        </span>
                                    </td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}>
                                        <div style={{ display: 'flex', gap: '4px', justifyContent: 'center' }}>
                                            <button onClick={() => handleExecuteClick(report)} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '11px' }} title="Execute"><Play size={14} /></button>
                                            <button onClick={() => handleScheduleClick(report)} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '11px' }} title="Schedule"><Calendar size={14} /></button>
                                            <button onClick={() => window.open(`/api/system/report/download/${report.reportId}?format=EXCEL`, '_blank')} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '11px' }} title="Download"><Download size={14} /></button>
                                            <button onClick={() => handleEditClick(report)} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '11px' }} title="Edit"><Edit size={14} /></button>
                                            {report.scheduleEnabled && (
                                                <button onClick={() => handleUnscheduleClick(report)} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '11px', color: '#f59e0b', borderColor: '#f59e0b' }} title="Unschedule"><Clock size={14} /></button>
                                            )}
                                            <button onClick={() => handleDeleteClick(report)} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '11px', color: 'var(--danger)', borderColor: 'var(--danger)' }} title="Delete"><Trash2 size={14} /></button>
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {/* Add/Edit Modal */}
            <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={modalMode === 'add' ? 'Add Report' : 'Edit Report'} size="large"
                footer={<>
                    <button className="btn btn-secondary" onClick={() => setIsModalOpen(false)} disabled={submitting}>Cancel</button>
                    <button className="btn btn-primary" onClick={handleSubmit} disabled={submitting}>{modalMode === 'add' ? 'Create' : 'Save'}</button>
                </>}>
                <div style={{ maxHeight: '70vh', overflowY: 'auto', paddingRight: '8px' }}>
                    <div className="form-row">
                        <FormInput label="Report Name *" name="reportName" value={formData.reportName} onChange={handleInputChange} />
                        <FormInput label="Report Key *" name="reportKey" value={formData.reportKey} onChange={handleInputChange} disabled={modalMode === 'edit'} />
                    </div>
                    <div className="form-row">
                        <div className="form-group">
                            <label className="form-label">Output Format</label>
                            <select name="outputFormat" value={formData.outputFormat} onChange={handleInputChange} className="form-input">
                                <option value="EXCEL">Excel</option><option value="CSV">CSV</option><option value="HTML">HTML</option><option value="JSON">JSON</option>
                            </select>
                        </div>
                        <div className="form-group">
                            <label className="form-label">Status</label>
                            <select name="status" value={formData.status} onChange={handleInputChange} className="form-input">
                                <option value="0">Active</option><option value="1">Disabled</option>
                            </select>
                        </div>
                    </div>
                    <div className="form-group">
                        <label className="form-label">SQL Content *</label>
                        <textarea name="sqlContent" value={formData.sqlContent} onChange={handleInputChange} rows={10} className="form-input" style={{ fontFamily: 'monospace', fontSize: '12px' }} />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Remark</label>
                        <textarea name="remark" value={formData.remark} onChange={handleInputChange} rows={2} className="form-input" />
                    </div>
                </div>
            </Modal>

            {/* Create from Template Modal */}
            <Modal isOpen={isTemplateModalOpen} onClose={() => setIsTemplateModalOpen(false)} title="Create Report from Template" size="large"
                footer={<>
                    <button className="btn btn-secondary" onClick={() => setIsTemplateModalOpen(false)} disabled={submitting}>Cancel</button>
                    <button className="btn btn-primary" onClick={handleCreateFromTemplateSubmit} disabled={submitting || !selectedTemplate}>Create Report</button>
                </>}>
                <div style={{ maxHeight: '70vh', overflowY: 'auto' }}>
                    <div style={{ marginBottom: '16px' }}>
                        <h4 style={{ marginBottom: '12px' }}>Step 1: Select a Template</h4>
                        {templates.length === 0 ? (
                            <p style={{ color: 'var(--text-muted)' }}>No templates available. Create one in Report Designer first.</p>
                        ) : (
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '12px' }}>
                                {templates.map(t => (
                                    <div key={t.templateId} onClick={() => handleTemplateSelect(t)}
                                        style={{ padding: '12px', borderRadius: '8px', cursor: 'pointer', border: selectedTemplate?.templateId === t.templateId ? '2px solid var(--primary-color)' : '1px solid var(--border-color)', background: selectedTemplate?.templateId === t.templateId ? 'var(--primary-soft)' : 'var(--bg-tertiary)' }}>
                                        <div style={{ fontWeight: 600 }}>{t.templateName}</div>
                                        <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{t.templateKey}</div>
                                        {t.description && <div style={{ fontSize: '12px', marginTop: '8px' }}>{t.description}</div>}
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                    {selectedTemplate && (
                        <>
                            <h4 style={{ marginBottom: '12px' }}>Step 2: Configure Report</h4>
                            <div className="form-row">
                                <FormInput label="Report Name *" name="reportName" value={formData.reportName} onChange={handleInputChange} />
                                <FormInput label="Report Key *" name="reportKey" value={formData.reportKey} onChange={handleInputChange} />
                            </div>
                            <div className="form-row">
                                <div className="form-group">
                                    <label className="form-label">Schedule (Optional)</label>
                                    <input name="scheduleCron" value={formData.scheduleCron} onChange={handleInputChange} className="form-input" placeholder="e.g., 0 0 9 * * ? (daily 9 AM)" />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Email Recipients (Optional)</label>
                                    <input name="emailRecipients" value={formData.emailRecipients} onChange={handleInputChange} className="form-input" placeholder="user@email.com,user2@email.com" />
                                </div>
                            </div>
                            <div className="form-group">
                                <label className="form-label">Email Subject (Optional)</label>
                                <input name="emailSubject" value={formData.emailSubject} onChange={handleInputChange} className="form-input" placeholder="Default: report name" />
                            </div>
                        </>
                    )}
                </div>
            </Modal>

            {/* Schedule Modal */}
            <Modal isOpen={isScheduleModalOpen} onClose={() => setIsScheduleModalOpen(false)} title={`Schedule: ${currentReport?.reportName || ''}`} size="medium"
                footer={<>
                    <button className="btn btn-secondary" onClick={() => setIsScheduleModalOpen(false)} disabled={submitting}>Cancel</button>
                    <button className="btn btn-primary" onClick={handleScheduleSubmit} disabled={submitting}>
                        <Calendar size={16} /> Schedule
                    </button>
                </>}>
                <div style={{ maxHeight: '70vh', overflowY: 'auto' }}>
                    <div className="form-group">
                        <label className="form-label">Cron Expression *</label>
                        <input value={scheduleData.cronExpression} onChange={e => setScheduleData(p => ({ ...p, cronExpression: e.target.value }))} className="form-input" placeholder="e.g., 0 0 9 * * ?" />
                        <small className="form-help">Use presets below or enter custom cron</small>
                    </div>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', marginBottom: '16px' }}>
                        {cronPresets.map(p => (
                            <button key={p.value} onClick={() => setScheduleData(prev => ({ ...prev, cronExpression: p.value }))}
                                className="btn btn-secondary" style={{ padding: '6px 10px', fontSize: '11px' }}>{p.label}</button>
                        ))}
                    </div>
                    <div className="form-group">
                        <label className="form-label">Email Recipients *</label>
                        <input value={scheduleData.recipients} onChange={e => setScheduleData(p => ({ ...p, recipients: e.target.value }))} className="form-input" placeholder="user@email.com,user2@email.com" />
                    </div>
                    <div className="form-group">
                        <label className="form-label">CC Emails</label>
                        <input value={scheduleData.ccEmails} onChange={e => setScheduleData(p => ({ ...p, ccEmails: e.target.value }))} className="form-input" placeholder="Optional" />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Subject</label>
                        <input value={scheduleData.subject} onChange={e => setScheduleData(p => ({ ...p, subject: e.target.value }))} className="form-input" />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Email Body</label>
                        <textarea value={scheduleData.body} onChange={e => setScheduleData(p => ({ ...p, body: e.target.value }))} rows={3} className="form-input" />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Format</label>
                        <select value={scheduleData.format} onChange={e => setScheduleData(p => ({ ...p, format: e.target.value }))} className="form-input">
                            <option value="EXCEL">Excel</option><option value="CSV">CSV</option><option value="HTML">HTML</option><option value="JSON">JSON</option>
                        </select>
                    </div>
                </div>
            </Modal>
        </div>
    );
};

export default ReportList;
