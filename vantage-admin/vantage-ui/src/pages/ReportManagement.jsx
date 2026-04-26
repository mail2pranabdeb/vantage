import { useState, useEffect } from 'react';
import { FileText, Plus, Play, Download, Code, RefreshCw, Clock, Trash2, Calendar, Copy } from 'lucide-react';
import { useToast } from '../components/Toast';
import Modal from '../components/Modal';

/**
 * Unified Report Management Page
 * - Lists all report templates with versions
 * - Create/Edit templates in Report Designer
 * - Execute, preview, export reports
 * - View version history
 * 
 * Note: Scheduling is done via the Job module by creating a "Report Execution" job
 */
const ReportManagement = () => {
    const { addToast } = useToast();
    const [templates, setTemplates] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isVersionModalOpen, setIsVersionModalOpen] = useState(false);
    const [isScheduleHelpOpen, setIsScheduleHelpOpen] = useState(false);
    const [currentTemplate, setCurrentTemplate] = useState(null);
    const [templateVersions, setTemplateVersions] = useState([]);
    const [scheduleCode, setScheduleCode] = useState('');
    const [scheduleEmail, setScheduleEmail] = useState('');
    const [executeParams, setExecuteParams] = useState('{}');
    const [isParamModalOpen, setIsParamModalOpen] = useState(false);
    const [pendingExecuteReport, setPendingExecuteReport] = useState(null);
    const [paramModalAction, setParamModalAction] = useState('execute');

    useEffect(() => {
        fetchTemplates();
    }, []);

    const fetchTemplates = () => {
        setLoading(true);
        fetch('/api/system/report-designer/templates?allVersions=true')
            .then(res => res.json())
            .then(data => {
                setLoading(false);
                if (data.code === 200) setTemplates(data.data || []);
                else addToast('error', data.msg || 'Failed to load reports', 4000);
            })
            .catch(() => { setLoading(false); addToast('error', 'Failed to load reports', 5000); });
    };

    // Navigate to Report Designer with template loaded
    const openDesigner = (pageConfig) => {
        window.dispatchEvent(new CustomEvent('navigate-to-page', { detail: { pageConfig } }));
    };

    const handleEditInDesigner = (row) => {
        openDesigner({
            id: `report-designer-${row.templateId}`,
            title: `Edit: ${row.templateName}`,
            url: `/system/report-designer?templateId=${row.templateId}`,
            icon: '📝'
        });
    };

    const handleCreateNew = () => {
        openDesigner({
            id: 'report-designer-new',
            title: 'New Report',
            url: '/system/report-designer',
            icon: '📝'
        });
    };

    const handleDelete = (row) => {
        if (!confirm(`Delete report template "${row.templateName}"?`)) return;
        fetch(`/api/system/report-designer/templates/${row.templateId}`, { method: 'DELETE' })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) { addToast('success', 'Report deleted', 3000); fetchTemplates(); }
                else addToast('error', data.msg || 'Failed', 5000);
            });
    };

const handleExecute = (row) => {
        setParamModalAction('execute');
        setPendingExecuteReport(row);
        setExecuteParams('{}');
        setIsParamModalOpen(true);
    };

    const handleExportWithParams = (row) => {
        setParamModalAction('export');
        setPendingExecuteReport(row);
        setExecuteParams('{}');
        setIsParamModalOpen(true);
    };

    const executeReport = (row, params) => {
        fetch(`/api/system/report-designer/execute/${row.templateId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: params
        })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    const rowCount = data.data?.length || 0;
                    if (rowCount > 0) {
                        addToast('success', `${rowCount} rows returned`, 3000);
                    } else if (rowCount === 0) {
                        addToast('info', 'No data returned', 5000);
                    }
                } else {
                    addToast('error', data.msg || 'Execution failed', 5000);
                }
            })
            .catch(() => addToast('error', 'Execution failed', 5000));
    };

    const handleDirectDownload = (url, filename) => {
        fetch(url)
            .then(res => res.blob())
            .then(blob => {
                const downloadUrl = window.URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = downloadUrl;
                link.download = filename;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
                window.URL.revokeObjectURL(downloadUrl);
            })
            .catch(() => {
                window.open(url, '_blank');
            });
    };

    const handleParamExecute = () => {
        setIsParamModalOpen(false);
        if (!pendingExecuteReport) return;
        
        if (paramModalAction === 'export') {
            const format = pendingExecuteReport.outputFormat || 'EXCEL';
            const params = executeParams || '{}';
            handleDirectDownload(`/api/system/report-designer/export/${pendingExecuteReport.templateId}?format=${format}&params=${encodeURIComponent(params)}`, `${pendingExecuteReport.templateName}.${format.toLowerCase()}`);
            addToast('success', 'Report exported successfully', 3000);
        } else {
            executeReport(pendingExecuteReport, executeParams);
        }
        setPendingExecuteReport(null);
    };

    const handleActivate = (row) => {
        if (!confirm(`Activate this report? This will set status to Active and increment version.`)) return;

        fetch(`/api/system/report-designer/templates/${row.templateId}/activate`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' }
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                addToast('success', data.msg || 'Report activated successfully', 3000);
                fetchTemplates();
            } else {
                addToast('error', data.msg || 'Failed to activate report', 5000);
            }
        })
        .catch(err => {
            console.error("Failed to activate report:", err);
            addToast('error', 'Failed to activate report', 5000);
        });
    };

    const handleExport = (row, format) => {
        const filename = `${row.templateName || 'report'}.${format.toLowerCase()}`;
        handleDirectDownload(`/api/system/report-designer/export/${row.templateId}?format=${format}`, filename);
    };

    const handleViewVersions = (row) => {
        setCurrentTemplate(row);
        fetch(`/api/system/report-designer/templates/${row.templateKey}/versions`)
            .then(res => res.json())
            .then(data => { if (data.code === 200) { setTemplateVersions(data.data || []); setIsVersionModalOpen(true); } });
    };

    const handleArchiveVersion = (version) => {
        if (!confirm(`Archive version ${version.version}?`)) return;
        fetch(`/api/system/report-designer/templates/${version.templateId}/archive`, { method: 'PUT' })
            .then(res => res.json())
            .then(data => { if (data.code === 200) { addToast('success', 'Version archived', 3000); fetchTemplates(); handleViewVersions(currentTemplate); } });
    };

    const handleActivateVersion = (version) => {
        fetch(`/api/system/report-designer/templates/${version.templateId}/activate`, { method: 'PUT' })
            .then(res => res.json())
            .then(data => { if (data.code === 200) { addToast('success', 'Version activated', 3000); fetchTemplates(); handleViewVersions(currentTemplate); } });
    };

    const handleScheduleHelp = (row) => {
        setCurrentTemplate(row);
        setScheduleEmail('');
        fetch(`/api/system/report-designer/templates/${row.templateKey}/versions`)
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setTemplateVersions(data.data || []);
                    const activeVersions = (data.data || []).filter(v => v.status === '0');
                    if (activeVersions.length > 0) {
                        const v = activeVersions[0];
                        // Use the template's saved output format instead of hardcoding EXCEL
                        const format = v.outputFormat || 'EXCEL';
                        setScheduleCode(`reportExecutionJob.execute(${v.templateId}, '${format}', ['your@email.com'], null, '${row.templateName}', 'Please find the attached report.', '{}')`);
                    }
                }
            });
        setIsScheduleHelpOpen(true);
    };

    const updateScheduleCode = (versionId, email) => {
        const version = templateVersions.find(v => v.templateId === versionId);
        if (version) {
            const format = version.outputFormat || 'EXCEL';
            setScheduleCode(`reportExecutionJob.execute(${version.templateId}, '${format}', ['${email || 'your@email.com'}'], null, '${currentTemplate.templateName}', 'Please find the attached report.', '{}')`);
        }
        setScheduleEmail(email);
    };

    const copyToClipboard = (text) => {
        navigator.clipboard.writeText(text).then(() => addToast('success', 'Copied to clipboard!', 2000));
    };

    return (
        <div style={{ height: 'calc(100vh - 50px)', overflow: 'auto', padding: '8px' }}>
            <div className="page-header" style={{ marginBottom: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{ width: '40px', height: '40px', borderRadius: '10px', background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white' }}><FileText size={20} /></div>
                    <div>
                        <h2 style={{ fontSize: '14px', fontWeight: 700, margin: 0 }}>Report Management</h2>
                        <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '4px 0 0' }}>Design, execute, and schedule reports</p>
                    </div>
                </div>
                <div style={{ display: 'flex', gap: '8px' }}>
                    <button className="btn btn-primary" onClick={handleCreateNew} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}><Plus size={16} /> New Report</button>
                    <button className="btn btn-secondary" onClick={fetchTemplates} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}><RefreshCw size={16} /> Refresh</button>
                </div>
            </div>

            <div style={{ background: 'var(--bg-secondary)', borderRadius: '8px', border: '1px solid var(--border-color)', overflow: 'hidden' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                        <tr style={{ background: 'var(--bg-tertiary)', borderBottom: '2px solid var(--border-color)' }}>
                            <th style={{ padding: '10px', textAlign: 'left' }}>Report Name</th>
                            <th style={{ padding: '10px', textAlign: 'left' }}>Report Code</th>
                            <th style={{ padding: '10px', textAlign: 'center' }}>Datasource</th>
                            <th style={{ padding: '10px', textAlign: 'center' }}>Format</th>
                            <th style={{ padding: '10px', textAlign: 'center' }}>Version</th>
                            <th style={{ padding: '10px', textAlign: 'center' }}>Used in Jobs</th>
                            <th style={{ padding: '10px', textAlign: 'center', width: '280px' }}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {loading ? (
                            <tr><td colSpan={7} style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>Loading...</td></tr>
                        ) : templates.length === 0 ? (
                            <tr><td colSpan={7} style={{ padding: '60px', textAlign: 'center' }}>
                                <FileText size={48} style={{ color: 'var(--text-muted)', opacity: 0.3, marginBottom: '16px' }} />
                                <p style={{ color: 'var(--text-muted)', fontSize: '14px' }}>No reports found</p>
                                <p style={{ color: 'var(--text-muted)', fontSize: '12px' }}>Click "New Report" to design one</p>
                            </td></tr>
                        ) : (
                            templates.map(t => (
                                <tr key={t.templateId} style={{ borderBottom: '1px solid var(--border-color)' }}>
                                    <td style={{ padding: '10px' }}>
                                        <div style={{ fontWeight: 600 }}>{t.templateName}</div>
                                    </td>
                                    <td style={{ padding: '10px', fontFamily: 'monospace', fontSize: '11px', color: 'var(--text-muted)' }}>{t.templateKey}</td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}><span style={{ padding: '4px 10px', borderRadius: '12px', fontSize: '11px', fontWeight: 600, background: '#3b82f620', color: '#3b82f6' }}>{t.datasourceKey}</span></td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}><span style={{ padding: '4px 10px', borderRadius: '12px', fontSize: '11px', fontWeight: 600, background: '#8b5cf620', color: '#8b5cf6' }}>{t.outputFormat}</span></td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}>
                                        <span style={{ padding: '4px 10px', borderRadius: '12px', fontSize: '11px', fontWeight: 600, background: t.status === '0' ? '#10b98120' : '#f59e0b20', color: t.status === '0' ? '#10b981' : '#f59e0b' }}>
                                            {t.status === '0' ? `Active v${t.version}` : `v${t.version}`}
                                        </span>
                                    </td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}>
                                        <span style={{ padding: '4px 8px', borderRadius: '8px', fontSize: '10px', background: '#6366f120', color: '#6366f1' }}>
                                            -
                                        </span>
                                    </td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}>
                                        <div style={{ display: 'flex', gap: '4px', justifyContent: 'center' }}>
                                            {t.status !== '0' && (
                                                <button onClick={() => handleActivate(t)} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '11px', color: '#10b981', borderColor: '#10b981' }} title="Activate Report">
                                                    <Play size={14} />
                                                </button>
                                            )}
                                            <button onClick={() => handleEditInDesigner(t)} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '11px' }} title="Edit in Designer"><Code size={14} /></button>
                                            <button onClick={() => handleExportWithParams(t)} className="btn btn-primary" style={{ padding: '4px 12px', fontSize: '11px' }} title="Execute and Export"><Play size={14} /> Export</button>
                                            <button onClick={() => handleScheduleHelp(t)} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '11px' }} title="Get Schedule Code"><Calendar size={14} /></button>
                                            <button onClick={() => handleViewVersions(t)} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '11px' }} title="Versions"><Clock size={14} /></button>
                                            <button onClick={() => handleDelete(t)} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '11px', color: 'var(--danger)', borderColor: 'var(--danger)' }} title="Delete"><Trash2 size={14} /></button>
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {/* Version History Modal */}
            <Modal isOpen={isVersionModalOpen} onClose={() => setIsVersionModalOpen(false)} title={`Versions: ${currentTemplate?.templateName || ''}`} size="medium">
                <div style={{ maxHeight: '70vh', overflowY: 'auto' }}>
                    <div style={{ padding: '12px', background: 'var(--bg-tertiary)', borderRadius: '8px', marginBottom: '16px', fontSize: '12px', color: 'var(--text-secondary)' }}>
                        <strong>Scheduling:</strong> To schedule this report, go to <strong>Job Scheduling → Add Job</strong> and select "Report Execution" as the job type. Choose the template and version you want to run.
                    </div>
                    {templateVersions.length === 0 ? (
                        <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '20px' }}>No versions found</p>
                    ) : (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                            {templateVersions.map(v => (
                                <div key={v.templateId} style={{ padding: '12px', borderRadius: '8px', border: `1px solid ${v.status === '0' ? 'var(--success)' : 'var(--border-color)'}`, background: v.status === '0' ? 'rgba(16, 185, 129, 0.05)' : 'var(--bg-tertiary)' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                            <span style={{ fontWeight: 700, fontSize: '14px' }}>v{v.version}</span>
                                            <span style={{ padding: '2px 8px', borderRadius: '8px', fontSize: '10px', fontWeight: 600, background: v.status === '0' ? '#10b98120' : v.status === '2' ? '#f59e0b20' : '#ef444420', color: v.status === '0' ? '#10b981' : v.status === '2' ? '#f59e0b' : '#ef4444' }}>{v.status === '0' ? 'Active' : v.status === '2' ? 'Archived' : 'Inactive'}</span>
                                        </div>
                                        <div style={{ display: 'flex', gap: '4px' }}>
                                            {v.status !== '0' && <button onClick={() => handleActivateVersion(v)} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '10px' }}>Activate</button>}
                                            {v.status === '0' && <button onClick={() => handleArchiveVersion(v)} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '10px', color: '#f59e0b', borderColor: '#f59e0b' }}>Archive</button>}
                                            <button onClick={() => handleEditInDesigner(v)} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '10px' }}><Code size={12} /></button>
                                        </div>
                                    </div>
                                    {v.changeLog && <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginBottom: '4px' }}>{v.changeLog}</div>}
                                    <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{v.updateTime ? `Updated: ${new Date(v.updateTime).toLocaleString()}` : `Created: ${new Date(v.createTime).toLocaleString()}`}</div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </Modal>

            {/* Schedule Help Modal */}
            <Modal isOpen={isScheduleHelpOpen} onClose={() => setIsScheduleHelpOpen(false)} title={`Schedule: ${currentTemplate?.templateName || ''}`} size="medium">
                <div style={{ maxHeight: '70vh', overflowY: 'auto' }}>
                    <div style={{ padding: '12px', background: 'var(--bg-tertiary)', borderRadius: '8px', marginBottom: '16px', fontSize: '12px' }}>
                        <strong>How to Schedule This Report:</strong><br/>
                        Configure this report to run automatically via the Job Scheduling module.
                    </div>

                    <div className="form-group">
                        <label className="form-label">Select Version</label>
                        <select className="form-input" value={templateVersions.find(v => scheduleCode.includes(String(v.templateId)))?.templateId || ''}
                            onChange={e => updateScheduleCode(parseInt(e.target.value), scheduleEmail)}>
                            {templateVersions.filter(v => v.status === '0').map(v => (
                                <option key={v.templateId} value={v.templateId}>v{v.version} - {v.changeLog || 'No notes'}</option>
                            ))}
                        </select>
                    </div>

                    <div className="form-group">
                        <label className="form-label">Email Recipients</label>
                        <input className="form-input" value={scheduleEmail} onChange={e => {
                            setScheduleEmail(e.target.value);
                            const version = templateVersions.find(v => scheduleCode.includes(String(v.templateId)));
                            if (version) setScheduleCode(`reportExecutionJob.execute(${version.templateId}, 'EXCEL', ['${e.target.value || 'your@email.com'}'], null, '${currentTemplate?.templateName || ''}', 'Please find the attached report.', '{}')`);
                        }} placeholder="user@email.com,user2@email.com" />
                    </div>

                    <div className="form-group">
                        <label className="form-label">Invoke Target (copy this)</label>
                        <div style={{ position: 'relative' }}>
                            <pre style={{ background: 'var(--bg-primary)', padding: '12px', borderRadius: '8px', fontSize: '11px', overflow: 'auto', maxHeight: '100px', fontFamily: 'monospace', border: '1px solid var(--border-color)' }}>{scheduleCode}</pre>
                            <button onClick={() => copyToClipboard(scheduleCode)} style={{ position: 'absolute', top: '8px', right: '8px', background: 'var(--primary-color)', color: '#fff', border: 'none', borderRadius: '6px', padding: '4px 8px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px' }}>
                                <Copy size={12} /> Copy
                            </button>
                        </div>
                    </div>

                    <div style={{ padding: '12px', background: 'rgba(59, 130, 246, 0.1)', borderRadius: '8px', border: '1px solid rgba(59, 130, 246, 0.2)', fontSize: '12px' }}>
                        <strong>Scheduling Steps:</strong>
                        <ol style={{ paddingLeft: '20px', marginTop: '8px', lineHeight: '1.8' }}>
                            <li>Copy the <strong>Invoke Target</strong> above</li>
                            <li>Go to <strong>Job Scheduling → Add Job</strong></li>
                            <li>Set Job Name (e.g., "{currentTemplate?.templateName} Report")</li>
                            <li>Set Job Group (e.g., "reports")</li>
                            <li>Paste the Invoke Target into the <strong>Invoke Target</strong> field</li>
                            <li>Set cron expression (e.g., <code style={{ background: 'var(--bg-primary)', padding: '2px 6px', borderRadius: '4px' }}>0 0 9 * * ?</code> for daily 9 AM)</li>
                            <li>Enable and save</li>
                        </ol>
                    </div>
                </div>
            </Modal>

            {/* Execute Parameters Modal */}
            <Modal isOpen={isParamModalOpen} onClose={() => setIsParamModalOpen(false)} title={paramModalAction === 'export' ? 'Export Report' : 'Execute Report'} size="medium">
                <div style={{ padding: '16px' }}>
                    <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginBottom: '16px' }}>
                        Enter parameter values for this report. Use JSON format.
                    </p>
                    <div className="form-group">
                        <label className="form-label">Parameters (JSON)</label>
                        <textarea 
                            className="form-input" 
                            rows={8}
                            value={executeParams}
                            onChange={e => setExecuteParams(e.target.value)}
                            placeholder='{"status": "0", "startDate": "2026-01-01"}'
                            style={{ fontFamily: 'monospace', fontSize: '12px' }}
                        />
                    </div>
                    <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end', marginTop: '16px' }}>
                        <button className="btn btn-secondary" onClick={() => setIsParamModalOpen(false)}>Cancel</button>
                        <button className="btn btn-primary" onClick={handleParamExecute}>{paramModalAction === 'export' ? 'Export' : 'Execute'}</button>
                    </div>
                </div>
            </Modal>
        </div>
    );
};

export default ReportManagement;
