import { useState, useEffect } from 'react';
import { FileText, Plus, Edit, Trash2, Play, Download, Calendar, Eye, Code, RefreshCw, Clock, Mail } from 'lucide-react';
import { useToast } from '../components/Toast';
import Modal from '../components/Modal';

/**
 * Unified Report Management Page
 * - Lists all report templates
 * - Create/Edit templates in Report Designer
 * - Execute, preview, export reports
 * - Schedule reports as Quartz jobs with email delivery
 */
const ReportManagement = () => {
    const { addToast } = useToast();
    const [templates, setTemplates] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isScheduleModalOpen, setIsScheduleModalOpen] = useState(false);
    const [currentTemplate, setCurrentTemplate] = useState(null);
    const [scheduleData, setScheduleData] = useState({
        cronExpression: '',
        recipients: '',
        ccEmails: '',
        subject: '',
        body: 'Please find the attached report.',
        format: 'EXCEL'
    });
    const [submitting, setSubmitting] = useState(false);

    const cronPresets = [
        { label: 'Every Hour', value: '0 0 * * * ?' },
        { label: 'Every 6 Hours', value: '0 0 0/6 * * ?' },
        { label: 'Daily at 9 AM', value: '0 0 9 * * ?' },
        { label: 'Daily at 6 PM', value: '0 0 18 * * ?' },
        { label: 'Weekdays 9 AM', value: '0 0 9 ? * MON-FRI' },
        { label: 'Weekly Sunday', value: '0 0 12 ? * SUN' },
        { label: 'Monthly 1st', value: '0 0 9 1 * ?' }
    ];

    useEffect(() => {
        fetchTemplates();
    }, []);

    const fetchTemplates = () => {
        setLoading(true);
        fetch('/api/system/report-designer/templates')
            .then(res => res.json())
            .then(data => {
                setLoading(false);
                if (data.code === 200) {
                    setTemplates(data.data || []);
                } else {
                    addToast('error', data.msg || 'Failed to load reports', 4000);
                }
            })
            .catch(() => {
                setLoading(false);
                addToast('error', 'Failed to load reports', 5000);
            });
    };

    // Open template in Report Designer
    const handleEditInDesigner = (row) => {
        // Use window.open to navigate to designer with template ID
        window.open(`/system/report-designer?templateId=${row.templateId}`, '_blank');
    };

    // Create new template - navigate to designer
    const handleCreateNew = () => {
        window.open('/system/report-designer', '_blank');
    };

    // Delete template
    const handleDelete = (row) => {
        if (!confirm(`Delete report template "${row.templateName}"?`)) return;
        fetch(`/api/system/report-designer/templates/${row.templateId}`, { method: 'DELETE' })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    addToast('success', 'Report deleted', 3000);
                    fetchTemplates();
                } else {
                    addToast('error', data.msg || 'Failed', 5000);
                }
            });
    };

    // Execute report
    const handleExecute = (row) => {
        fetch(`/api/system/report-designer/execute/${row.templateId}?params={}`, { method: 'POST' })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    addToast('success', `${data.data?.length || 0} rows returned`, 5000);
                    if (window.confirm('Download results as Excel?')) {
                        window.open(`/api/system/report-designer/export/${row.templateId}?format=${row.outputFormat || 'EXCEL'}`, '_blank');
                    }
                } else {
                    addToast('error', data.msg || 'Execution failed', 5000);
                }
            })
            .catch(() => addToast('error', 'Execution failed', 5000));
    };

    // Export report
    const handleExport = (row, format) => {
        window.open(`/api/system/report-designer/export/${row.templateId}?params={}&format=${format}`, '_blank');
    };

    // Open schedule modal
    const handleScheduleClick = (row) => {
        setCurrentTemplate(row);
        setScheduleData({
            cronExpression: '',
            recipients: '',
            ccEmails: '',
            subject: row.templateName + ' Report',
            body: 'Please find the attached report.',
            format: row.outputFormat || 'EXCEL'
        });
        setIsScheduleModalOpen(true);
    };

    // Schedule report as Quartz job
    const handleScheduleSubmit = () => {
        if (!scheduleData.cronExpression || !scheduleData.recipients) {
            addToast('error', 'Cron expression and email recipients are required', 3000);
            return;
        }
        setSubmitting(true);
        fetch(`/api/system/report/schedule/${currentTemplate.templateId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                ...scheduleData,
                templateId: currentTemplate.templateId,
                reportName: currentTemplate.templateName
            })
        })
        .then(res => res.json())
        .then(data => {
            setSubmitting(false);
            if (data.code === 200) {
                setIsScheduleModalOpen(false);
                addToast('success', `Scheduled! Next run: ${data.data?.nextFireTime || 'N/A'}`, 6000);
                fetchTemplates();
            } else {
                addToast('error', data.msg || 'Failed to schedule', 5000);
            }
        })
        .catch(() => {
            setSubmitting(false);
            addToast('error', 'Failed to schedule', 5000);
        });
    };

    return (
        <div style={{ height: 'calc(100vh - 50px)', overflow: 'auto', padding: '8px' }}>
            {/* Header */}
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
                            Design, execute, and schedule reports
                        </p>
                    </div>
                </div>
                <div style={{ display: 'flex', gap: '8px' }}>
                    <button className="btn btn-primary" onClick={handleCreateNew} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <Plus size={16} /> New Report
                    </button>
                    <button className="btn btn-secondary" onClick={fetchTemplates} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
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
                            <th style={{ padding: '10px', textAlign: 'center' }}>Datasource</th>
                            <th style={{ padding: '10px', textAlign: 'center' }}>Format</th>
                            <th style={{ padding: '10px', textAlign: 'center' }}>Schedule</th>
                            <th style={{ padding: '10px', textAlign: 'center', width: '360px' }}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {loading ? (
                            <tr><td colSpan={5} style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>Loading...</td></tr>
                        ) : templates.length === 0 ? (
                            <tr>
                                <td colSpan={5} style={{ padding: '60px', textAlign: 'center' }}>
                                    <FileText size={48} style={{ color: 'var(--text-muted)', opacity: 0.3, marginBottom: '16px' }} />
                                    <p style={{ color: 'var(--text-muted)', fontSize: '14px' }}>No reports found</p>
                                    <p style={{ color: 'var(--text-muted)', fontSize: '12px' }}>Click "New Report" to design one from scratch</p>
                                </td>
                            </tr>
                        ) : (
                            templates.map(t => (
                                <tr key={t.templateId} style={{ borderBottom: '1px solid var(--border-color)' }}>
                                    <td style={{ padding: '10px' }}>
                                        <div style={{ fontWeight: 600 }}>{t.templateName}</div>
                                        <div style={{ fontSize: '11px', color: 'var(--text-muted)', fontFamily: 'monospace' }}>{t.templateKey}</div>
                                        {t.description && <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginTop: '4px' }}>{t.description}</div>}
                                    </td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}>
                                        <span style={{ padding: '4px 10px', borderRadius: '12px', fontSize: '11px', fontWeight: 600, background: '#3b82f620', color: '#3b82f6' }}>{t.datasourceKey}</span>
                                    </td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}>
                                        <span style={{ padding: '4px 10px', borderRadius: '12px', fontSize: '11px', fontWeight: 600, background: '#8b5cf620', color: '#8b5cf6' }}>{t.outputFormat}</span>
                                    </td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}>
                                        <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Not scheduled</span>
                                    </td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}>
                                        <div style={{ display: 'flex', gap: '4px', justifyContent: 'center' }}>
                                            <button onClick={() => handleEditInDesigner(t)} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '11px' }} title="Edit in Designer"><Code size={14} /></button>
                                            <button onClick={() => handleExecute(t)} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '11px' }} title="Execute"><Play size={14} /></button>
                                            <button onClick={() => handleScheduleClick(t)} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '11px' }} title="Schedule"><Calendar size={14} /></button>
                                            <button onClick={() => handleExport(t, t.outputFormat || 'EXCEL')} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '11px' }} title="Export"><Download size={14} /></button>
                                            <button onClick={() => handleDelete(t)} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '11px', color: 'var(--danger)', borderColor: 'var(--danger)' }} title="Delete"><Trash2 size={14} /></button>
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {/* Schedule Modal */}
            <Modal isOpen={isScheduleModalOpen} onClose={() => setIsScheduleModalOpen(false)} title={`Schedule: ${currentTemplate?.templateName || ''}`} size="medium"
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
                        <label className="form-label">Attachment Format</label>
                        <select value={scheduleData.format} onChange={e => setScheduleData(p => ({ ...p, format: e.target.value }))} className="form-input">
                            <option value="EXCEL">Excel (.xls)</option><option value="CSV">CSV</option><option value="HTML">HTML</option><option value="JSON">JSON</option>
                        </select>
                    </div>
                </div>
            </Modal>
        </div>
    );
};

export default ReportManagement;
