import { useState, useEffect } from 'react';
import { 
    Mail, Plus, Edit, Trash2, Eye, Copy, Check, Save, X,
    RefreshCw, Code, Palette, AlertCircle, CheckCircle
} from 'lucide-react';
import Modal from '../components/Modal';

const EmailTemplateManager = () => {
    const [templates, setTemplates] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isPreviewOpen, setIsPreviewOpen] = useState(false);
    const [modalMode, setModalMode] = useState('add');
    const [currentTemplate, setCurrentTemplate] = useState(null);
    const [selectedTemplate, setSelectedTemplate] = useState(null);
    const [copiedId, setCopiedId] = useState(null);
    const [activeTab, setActiveTab] = useState('html');
    const [previewContent, setPreviewContent] = useState({ subject: '', body: '' });
    const [previewLoading, setPreviewLoading] = useState(false);
    const [datasources, setDatasources] = useState([]);
    const [submitting, setSubmitting] = useState(false);

    const [formData, setFormData] = useState({
        templateName: '',
        templateType: 'JOB_FAILURE',
        emailSubject: '',
        emailBody: '',
        isDefault: false,
        isActive: true,
        remark: '',
        datasourceKey: '',
        querySql: '',
        includeDataTable: false
    });

    const templateTypes = [
        { value: 'JOB_FAILURE', label: 'Job Failure', icon: '❌', color: '#f5576c' },
        { value: 'JOB_SUCCESS', label: 'Job Success', icon: '✅', color: '#11998e' },
        { value: 'JOB_RECOVERY', label: 'Job Recovery', icon: '🔄', color: '#4facfe' }
    ];

    const variableHelp = [
        { var: '${appName}', description: 'Application name' },
        { var: '${jobId}', description: 'Job ID' },
        { var: '${jobName}', description: 'Job name' },
        { var: '${jobGroup}', description: 'Job group' },
        { var: '${invokeTarget}', description: 'Invoke target method' },
        { var: '${cronExpression}', description: 'Cron expression' },
        { var: '${executionTime}', description: 'Execution start time' },
        { var: '${duration}', description: 'Execution duration (ms)' },
        { var: '${retryCount}', description: 'Retry count' },
        { var: '${status}', description: 'Execution status' },
        { var: '${message}', description: 'Result message' },
        { var: '${exceptionInfo}', description: 'Exception stack trace' },
        { var: '${timestamp}', description: 'Current timestamp' },
        { var: '${reportName}', description: 'Report name' },
        { var: '${reportFormat}', description: 'Report format (CSV, etc.)' },
        { var: '${totalRows}', description: 'Total data rows' },
        { var: '${dataTable}', description: 'HTML table from SQL query (if enabled)' }
    ];

    useEffect(() => {
        fetchTemplates();
        fetchDatasources();
    }, []);

    const fetchDatasources = () => {
        fetch('/api/system/datasource/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setDatasources((data.data || []).filter(d => d.status === '0'));
                }
            })
            .catch(err => console.error("Failed to fetch datasources:", err));
    };

    const fetchTemplates = () => {
        setLoading(true);
        fetch('/api/system/email-template/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setTemplates(data.data || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch templates:", err);
                setLoading(false);
            });
    };

    const handleAddClick = () => {
        setModalMode('add');
        setCurrentTemplate(null);
        setFormData({
            templateName: '',
            templateType: 'JOB_FAILURE',
            emailSubject: '',
            emailBody: '',
            isDefault: false,
            isActive: true,
            remark: ''
        });
        setIsModalOpen(true);
    };

    const handleEditClick = (template) => {
        setModalMode('edit');
        setCurrentTemplate(template);
        setFormData({
            templateName: template.templateName || '',
            templateType: template.templateType || 'JOB_FAILURE',
            emailSubject: template.emailSubject || '',
            emailBody: template.emailBody || '',
            isDefault: template.isDefault || false,
            isActive: template.isActive || true,
            remark: template.remark || '',
            datasourceKey: template.datasourceKey || '',
            querySql: template.querySql || '',
            includeDataTable: template.includeDataTable || false
        });
        setIsModalOpen(true);
    };

    const handlePreviewClick = () => {
        setPreviewLoading(true);
        fetch('/api/system/email-template/preview', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                emailSubject: formData.emailSubject,
                emailBody: formData.emailBody,
                datasourceKey: formData.datasourceKey,
                querySql: formData.querySql,
                includeDataTable: formData.includeDataTable
            })
        })
        .then(res => res.json())
        .then(data => {
            setPreviewLoading(false);
            if (data.code === 200) {
                setPreviewContent({ subject: data.subject || '', body: data.body || '' });
                setIsPreviewOpen(true);
            } else {
                alert(data.msg || 'Preview failed');
            }
        })
        .catch(err => {
            setPreviewLoading(false);
            console.error("Preview failed:", err);
            alert('Preview failed');
        });
    };

    const handleDeleteClick = (template) => {
        if (window.confirm(`Are you sure you want to delete template "${template.templateName}"?`)) {
            fetch(`/api/system/email-template/${template.templateId}`, {
                method: 'DELETE'
            })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    fetchTemplates();
                } else {
                    alert(data.msg || 'Failed to delete template');
                }
            })
            .catch(err => {
                console.error("Failed to delete template:", err);
                alert('Failed to delete template');
            });
        }
    };

    const handleSetDefault = (template) => {
        fetch(`/api/system/email-template/${template.templateId}/set-default?templateType=${template.templateType}`, {
            method: 'PUT'
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                fetchTemplates();
            } else {
                alert(data.msg || 'Failed to set default');
            }
        });
    };

    const handleToggleActive = (template) => {
        fetch(`/api/system/email-template/${template.templateId}/toggle-active`, {
            method: 'PUT'
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                fetchTemplates();
            } else {
                alert(data.msg || 'Failed to update status');
            }
        });
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleCheckboxChange = (e) => {
        const { name, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: checked
        }));
    };

    const handleSubmit = () => {
        if (!formData.templateName || !formData.emailSubject || !formData.emailBody) {
            alert('Please fill in all required fields');
            return;
        }

        setSubmitting(true);

        const url = modalMode === 'add' ? '/api/system/email-template' : '/api/system/email-template';
        const method = modalMode === 'add' ? 'POST' : 'PUT';
        const body = {
            ...formData,
            templateId: modalMode === 'edit' ? currentTemplate.templateId : null
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
                fetchTemplates();
            } else {
                alert(data.msg || `Failed to ${modalMode} template`);
            }
        })
        .catch(err => {
            setSubmitting(false);
            console.error(`Failed to ${modalMode} template:`, err);
            alert(`Failed to ${modalMode} template`);
        });
    };

    const copyToClipboard = (text, id) => {
        navigator.clipboard.writeText(text);
        setCopiedId(id);
        setTimeout(() => setCopiedId(null), 2000);
    };

    const insertVariable = (variable) => {
        setFormData(prev => ({
            ...prev,
            emailBody: prev.emailBody + variable
        }));
    };

    const getTypeColor = (type) => {
        const typeInfo = templateTypes.find(t => t.value === type);
        return typeInfo?.color || '#666';
    };

    const getTypeIcon = (type) => {
        const typeInfo = templateTypes.find(t => t.value === type);
        return typeInfo?.icon || '📧';
    };

    return (
        <div style={{
            height: 'calc(100vh - 70px)',
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
                        <Mail size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Email Templates</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Manage email notification templates for job events
                        </p>
                    </div>
                </div>
                <button
                    className="btn btn-primary"
                    onClick={handleAddClick}
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        padding: '10px 16px',
                        borderRadius: '8px',
                        fontWeight: 600
                    }}
                >
                    <Plus size={18} />
                    New Template
                </button>
            </div>

            {/* Template Cards */}
            <div style={{ 
                display: 'grid', 
                gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', 
                gap: '16px' 
            }}>
                {templates.map((template, idx) => (
                    <div
                        key={idx}
                        style={{
                            border: `1px solid ${template.isDefault ? 'var(--primary)' : 'var(--border-color)'}`,
                            borderRadius: '12px',
                            padding: '20px',
                            background: template.isDefault ? 'var(--primary)10' : 'var(--bg-secondary)',
                            transition: 'all 0.2s',
                            position: 'relative'
                        }}
                    >
                        {template.isDefault && (
                            <div style={{
                                position: 'absolute',
                                top: '12px',
                                right: '12px',
                                padding: '4px 10px',
                                background: 'var(--primary)',
                                color: 'white',
                                borderRadius: '12px',
                                fontSize: '11px',
                                fontWeight: 600
                            }}>
                                DEFAULT
                            </div>
                        )}

                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '12px' }}>
                            <div style={{
                                width: '40px',
                                height: '40px',
                                borderRadius: '10px',
                                background: getTypeColor(template.templateType) + '20',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                fontSize: '20px'
                            }}>
                                {getTypeIcon(template.templateType)}
                            </div>
                            <div>
                                <h3 style={{ margin: 0, fontSize: '15px', fontWeight: 600 }}>{template.templateName}</h3>
                                <span style={{ 
                                    fontSize: '11px', 
                                    color: getTypeColor(template.templateType),
                                    background: getTypeColor(template.templateType) + '20',
                                    padding: '2px 8px',
                                    borderRadius: '4px',
                                    fontWeight: 600
                                }}>
                                    {template.templateType}
                                </span>
                            </div>
                        </div>

                        <p style={{ 
                            fontSize: '12px', 
                            color: 'var(--text-secondary)', 
                            margin: '12px 0',
                            lineHeight: '1.5',
                            maxHeight: '60px',
                            overflow: 'hidden'
                        }}>
                            {template.emailSubject}
                        </p>

                        <div style={{ display: 'flex', gap: '8px', marginBottom: '12px' }}>
                            <span style={{
                                padding: '4px 8px',
                                borderRadius: '6px',
                                fontSize: '11px',
                                background: template.isActive ? '#11998e20' : '#f5576c20',
                                color: template.isActive ? '#11998e' : '#f5576c',
                                fontWeight: 600
                            }}>
                                {template.isActive ? 'Active' : 'Inactive'}
                            </span>
                        </div>

                        <div style={{ 
                            display: 'flex', 
                            gap: '8px',
                            paddingTop: '12px',
                            borderTop: '1px solid var(--border-color)'
                        }}>
                            <button
                                onClick={() => handlePreviewClick(template)}
                                className="btn btn-secondary"
                                style={{ flex: 1, padding: '8px', fontSize: '12px' }}
                            >
                                <Eye size={14} style={{ marginRight: '4px' }} />
                                Preview
                            </button>
                            <button
                                onClick={() => handleEditClick(template)}
                                className="btn btn-secondary"
                                style={{ flex: 1, padding: '8px', fontSize: '12px' }}
                            >
                                <Edit size={14} style={{ marginRight: '4px' }} />
                                Edit
                            </button>
                            <button
                                onClick={() => handleDeleteClick(template)}
                                className="btn btn-secondary"
                                style={{ 
                                    flex: 1, 
                                    padding: '8px', 
                                    fontSize: '12px',
                                    color: 'var(--danger)',
                                    borderColor: 'var(--danger)'
                                }}
                            >
                                <Trash2 size={14} style={{ marginRight: '4px' }} />
                                Delete
                            </button>
                        </div>

                        <div style={{ 
                            display: 'flex', 
                            gap: '8px', 
                            marginTop: '8px',
                            justifyContent: 'center'
                        }}>
                            {!template.isDefault && (
                                <button
                                    onClick={() => handleSetDefault(template)}
                                    style={{
                                        flex: 1,
                                        padding: '6px',
                                        borderRadius: '6px',
                                        border: '1px solid var(--border-color)',
                                        background: 'transparent',
                                        color: 'var(--text-primary)',
                                        cursor: 'pointer',
                                        fontSize: '11px',
                                        fontWeight: 500
                                    }}
                                >
                                    Set as Default
                                </button>
                            )}
                            <button
                                onClick={() => handleToggleActive(template)}
                                style={{
                                    flex: 1,
                                    padding: '6px',
                                    borderRadius: '6px',
                                    border: '1px solid var(--border-color)',
                                    background: 'transparent',
                                    color: 'var(--text-primary)',
                                    cursor: 'pointer',
                                    fontSize: '11px',
                                    fontWeight: 500
                                }}
                            >
                                {template.isActive ? 'Deactivate' : 'Activate'}
                            </button>
                        </div>
                    </div>
                ))}
            </div>

            {templates.length === 0 && !loading && (
                <div style={{ 
                    textAlign: 'center', 
                    padding: '60px 20px',
                    color: 'var(--text-muted)'
                }}>
                    <Mail size={48} style={{ margin: '0 auto 16px', opacity: 0.3 }} />
                    <p style={{ fontSize: '14px' }}>No email templates found</p>
                    <p style={{ fontSize: '12px', marginTop: '8px' }}>Create your first email template to get started</p>
                </div>
            )}

            {/* Add/Edit Modal */}
            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title={modalMode === 'add' ? 'Create Template' : 'Edit Template'}
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
                            className="btn btn-secondary"
                            onClick={handlePreviewClick}
                            disabled={submitting || previewLoading}
                            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                        >
                            {previewLoading ? (
                                <div style={{
                                    width: '12px',
                                    height: '12px',
                                    border: '2px solid var(--text-primary)',
                                    borderBottomColor: 'transparent',
                                    borderRadius: '50%',
                                    animation: 'spin 1s linear infinite'
                                }} />
                            ) : (
                                <Eye size={16} />
                            )}
                            Preview
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
                            <Save size={16} />
                            {modalMode === 'add' ? 'Create' : 'Save'}
                        </button>
                    </>
                }
            >
                <div style={{ maxHeight: '75vh', overflowY: 'auto', paddingRight: '8px' }}>
                    {/* Basic Info */}
                    <div style={{ marginBottom: '20px' }}>
                        <div className="form-row">
                            <FormInput
                                label="Template Name *"
                                name="templateName"
                                value={formData.templateName}
                                onChange={handleInputChange}
                                placeholder="e.g., Job Failure Alert"
                                disabled={false}
                            />
                            <div className="form-group">
                                <label className="form-label">Template Type *</label>
                                <select
                                    name="templateType"
                                    value={formData.templateType}
                                    onChange={handleInputChange}
                                    className="form-input"
                                    disabled={modalMode === 'edit'}
                                >
                                    {templateTypes.map(type => (
                                        <option key={type.value} value={type.value}>
                                            {type.icon} {type.label}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <FormInput
                            label="Email Subject *"
                            name="emailSubject"
                            value={formData.emailSubject}
                            onChange={handleInputChange}
                            placeholder="[${appName}] Job Failed: ${jobName}"
                            disabled={false}
                        />
                        <small className="form-help">Use variables like ${'{}'}jobName, ${'{}'}appName</small>
                    </div>

                    {/* Data Table Query */}
                    <div style={{ marginBottom: '20px', padding: '16px', background: 'var(--bg-secondary)', borderRadius: '8px' }}>
                        <h4 style={{ margin: '0 0 12px', fontSize: '13px', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <Code size={14} /> Data Table (Optional)
                        </h4>
                        <div className="form-row">
                            <div className="form-group">
                                <label className="form-label">Datasource</label>
                                <select
                                    name="datasourceKey"
                                    value={formData.datasourceKey}
                                    onChange={handleInputChange}
                                    className="form-input"
                                    disabled={false}
                                >
                                    <option value="">-- None --</option>
                                    {datasources.map(ds => (
                                        <option key={ds.datasourceKey} value={ds.datasourceKey}>
                                            {ds.datasourceName} ({ds.datasourceKey})
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <div className="form-group" style={{ display: 'flex', alignItems: 'flex-end', paddingBottom: '8px' }}>
                                <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '13px' }}>
                                    <input
                                        type="checkbox"
                                        name="includeDataTable"
                                        checked={formData.includeDataTable}
                                        onChange={handleCheckboxChange}
                                        style={{ width: '16px', height: '16px' }}
                                    />
                                    <span>Include data table in email</span>
                                </label>
                            </div>
                        </div>
                        {formData.includeDataTable && (
                            <div className="form-group">
                                <label className="form-label">SQL Query</label>
                                <textarea
                                    name="querySql"
                                    value={formData.querySql}
                                    onChange={handleInputChange}
                                    placeholder="SELECT * FROM sys_user WHERE status = '0'"
                                    rows={4}
                                    className="form-input"
                                    style={{ fontFamily: 'monospace', fontSize: '12px', resize: 'vertical' }}
                                    disabled={false}
                                />
                                <small className="form-help">Query results will be rendered as an HTML table at {'${dataTable}'} placeholder</small>
                            </div>
                        )}
                    </div>

                    {/* Email Body */}
                    <div style={{ marginBottom: '20px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                            <label className="form-label" style={{ margin: 0 }}>Email Body (HTML) *</label>
                            <div style={{ display: 'flex', gap: '8px' }}>
                                <button
                                    type="button"
                                    onClick={() => setActiveTab('html')}
                                    style={{
                                        padding: '4px 10px',
                                        borderRadius: '6px',
                                        border: 'none',
                                        background: activeTab === 'html' ? 'var(--primary)' : 'var(--bg-tertiary)',
                                        color: activeTab === 'html' ? 'white' : 'var(--text-primary)',
                                        cursor: 'pointer',
                                        fontSize: '11px',
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '4px'
                                    }}
                                >
                                    <Code size={12} /> HTML
                                </button>
                                <button
                                    type="button"
                                    onClick={() => setActiveTab('preview')}
                                    style={{
                                        padding: '4px 10px',
                                        borderRadius: '6px',
                                        border: 'none',
                                        background: activeTab === 'preview' ? 'var(--primary)' : 'var(--bg-tertiary)',
                                        color: activeTab === 'preview' ? 'white' : 'var(--text-primary)',
                                        cursor: 'pointer',
                                        fontSize: '11px',
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '4px'
                                    }}
                                >
                                    <Palette size={12} /> Preview
                                </button>
                            </div>
                        </div>

                        {activeTab === 'html' ? (
                            <>
                                <textarea
                                    name="emailBody"
                                    value={formData.emailBody}
                                    onChange={handleInputChange}
                                    placeholder="Enter HTML email template..."
                                    rows={15}
                                    className="form-input"
                                    style={{ 
                                        fontFamily: 'monospace', 
                                        fontSize: '12px',
                                        resize: 'vertical'
                                    }}
                                />
                                {/* Variable Quick Insert */}
                                <div style={{ marginTop: '12px' }}>
                                    <label className="form-label" style={{ fontSize: '12px' }}>Quick Insert Variables</label>
                                    <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                                        {variableHelp.slice(0, 7).map((item, idx) => (
                                            <button
                                                key={idx}
                                                type="button"
                                                onClick={() => insertVariable(item.var)}
                                                style={{
                                                    padding: '4px 8px',
                                                    borderRadius: '6px',
                                                    border: '1px solid var(--border-color)',
                                                    background: 'var(--bg-tertiary)',
                                                    color: 'var(--text-primary)',
                                                    cursor: 'pointer',
                                                    fontSize: '10px',
                                                    fontFamily: 'monospace'
                                                }}
                                            >
                                                {item.var}
                                            </button>
                                        ))}
                                    </div>
                                </div>
                            </>
                        ) : (
                            <div style={{
                                padding: '16px',
                                background: 'var(--bg-secondary)',
                                borderRadius: '8px',
                                minHeight: '300px',
                                border: '1px solid var(--border-color)'
                            }}>
                                <div dangerouslySetInnerHTML={{ __html: formData.emailBody || '<p style="color: var(--text-muted)">Enter HTML to preview</p>' }} />
                            </div>
                        )}
                    </div>

                    {/* Options */}
                    <div style={{ 
                        display: 'grid', 
                        gridTemplateColumns: 'repeat(2, 1fr)', 
                        gap: '12px',
                        padding: '16px',
                        background: 'var(--bg-secondary)',
                        borderRadius: '8px',
                        marginBottom: '20px'
                    }}>
                        <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                            <input
                                type="checkbox"
                                name="isDefault"
                                checked={formData.isDefault}
                                onChange={handleInputChange}
                                style={{ width: '16px', height: '16px' }}
                            />
                            <span style={{ fontSize: '13px' }}>Set as default for this type</span>
                        </label>
                        <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                            <input
                                type="checkbox"
                                name="isActive"
                                checked={formData.isActive}
                                onChange={handleInputChange}
                                style={{ width: '16px', height: '16px' }}
                            />
                            <span style={{ fontSize: '13px' }}>Active</span>
                        </label>
                    </div>

                    <FormInput
                        label="Remark"
                        name="remark"
                        value={formData.remark}
                        onChange={handleInputChange}
                        placeholder="Optional notes about this template"
                        disabled={false}
                    />

                    {/* Variable Help */}
                    <div style={{ 
                        marginTop: '20px', 
                        padding: '16px', 
                        background: 'var(--bg-tertiary)', 
                        borderRadius: '8px'
                    }}>
                        <h4 style={{ margin: '0 0 12px', fontSize: '13px', fontWeight: 600 }}>Available Variables</h4>
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '8px' }}>
                            {variableHelp.map((item, idx) => (
                                <div 
                                    key={idx}
                                    onClick={() => insertVariable(item.var)}
                                    style={{
                                        padding: '8px 10px',
                                        background: 'var(--bg-secondary)',
                                        borderRadius: '6px',
                                        cursor: 'pointer',
                                        transition: 'all 0.2s'
                                    }}
                                    onMouseEnter={(e) => {
                                        e.currentTarget.style.background = 'var(--primary)';
                                        e.currentTarget.style.color = 'white';
                                    }}
                                    onMouseLeave={(e) => {
                                        e.currentTarget.style.background = 'var(--bg-secondary)';
                                        e.currentTarget.style.color = 'var(--text-primary)';
                                    }}
                                >
                                    <code style={{ fontSize: '11px', display: 'block', marginBottom: '2px' }}>{item.var}</code>
                                    <span style={{ fontSize: '10px', opacity: 0.7 }}>{item.description}</span>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </Modal>

            {/* Preview Modal */}
            <Modal
                isOpen={isPreviewOpen}
                onClose={() => setIsPreviewOpen(false)}
                title="Live Email Preview"
                size="large"
            >
                {previewContent.subject && (
                    <div>
                        <div style={{
                            padding: '12px',
                            background: 'var(--bg-secondary)',
                            borderRadius: '8px',
                            marginBottom: '16px'
                        }}>
                            <strong style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Subject:</strong>
                            <div style={{ marginTop: '4px', fontSize: '14px' }}>
                                {previewContent.subject}
                            </div>
                        </div>
                        <div style={{
                            padding: '16px',
                            border: '1px solid var(--border-color)',
                            borderRadius: '8px',
                            background: 'white',
                            color: '#333',
                            maxHeight: '60vh',
                            overflow: 'auto'
                        }}>
                            <div dangerouslySetInnerHTML={{ __html: previewContent.body }} />
                        </div>
                    </div>
                )}
            </Modal>
        </div>
    );
};

// Simple FormInput component for this page
const FormInput = ({ label, name, value, onChange, placeholder, disabled }) => (
    <div className="form-group">
        <label className="form-label">{label}</label>
        <input
            type="text"
            name={name}
            value={value}
            onChange={onChange}
            placeholder={placeholder}
            className="form-input"
            disabled={disabled}
        />
    </div>
);

export default EmailTemplateManager;
