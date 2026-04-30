import { useState, useEffect } from 'react';
import { Settings, Plus, Edit, Trash2, Eye, RefreshCw, Mail, Send } from 'lucide-react';
import DataGrid from '../components/DataGrid';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';
import { useToast } from '../components/Toast';

const ConfigList = () => {
    const { addToast } = useToast();
    const [configs, setConfigs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isTestEmailOpen, setIsTestEmailOpen] = useState(false);
    const [testEmailTo, setTestEmailTo] = useState('');
    const [modalMode, setModalMode] = useState('add');
    const [currentConfig, setCurrentConfig] = useState(null);
    const [formData, setFormData] = useState({
        configName: '',
        configKey: '',
        configValue: '',
        configType: 'Y',
        remark: ''
    });
    const [submitting, setSubmitting] = useState(false);

    // SMTP config keys that should show the email test button
    const smtpKeys = ['mail.smtp.host', 'mail.smtp.port', 'mail.smtp.username', 'mail.smtp.password'];

    useEffect(() => {
        fetchConfigs();
    }, []);

    const fetchConfigs = () => {
        setLoading(true);
        fetch('/api/system/config/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setConfigs(data.data || []);
                    if (data.data && data.data.length > 0) {

                    }
                } else {
                    addToast('error', data.msg || 'Failed to load configs', 4000);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch configs:", err);
                setLoading(false);
                addToast('error', 'Failed to load configs. Please refresh.', 5000);
            });
    };

    const handleAddClick = () => {
        setModalMode('add');
        setCurrentConfig(null);
        setFormData({
            configName: '',
            configKey: '',
            configValue: '',
            configType: 'Y',
            remark: ''
        });
        setIsModalOpen(true);
    };

    const handleEditClick = (row) => {
        setModalMode('edit');
        setCurrentConfig(row);
        setFormData({
            configName: row.configName || '',
            configKey: row.configKey || '',
            configValue: row.configValue || '',
            configType: row.configType || 'Y',
            remark: row.remark || ''
        });
        setIsModalOpen(true);
    };

    const handleViewClick = (row) => {
        setModalMode('view');
        setCurrentConfig(row);
        setFormData({
            configName: row.configName || '',
            configKey: row.configKey || '',
            configValue: row.configValue || '',
            configType: row.configType || 'Y',
            remark: row.remark || ''
        });
        setIsModalOpen(true);
    };

    const handleDeleteClick = (row) => {
        if (window.confirm(`Are you sure you want to delete config "${row.configName}"?`)) {
            fetch(`/api/system/config/${row.configId}`, {
                method: 'DELETE'
            })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setConfigs(configs.filter(c => c.configId !== row.configId));
                    addToast('success', `Config "${row.configName}" deleted successfully`, 3000);
                } else {
                    addToast('error', data.msg || 'Failed to delete config', 5000);
                }
            })
            .catch(err => {
                console.error("Failed to delete config:", err);
                addToast('error', 'Failed to delete config', 5000);
            });
        }
    };

    const handleTestEmail = () => {
        setTestEmailTo('');
        setIsTestEmailOpen(true);
    };

    const sendTestEmail = () => {
        if (!testEmailTo) {
            addToast('error', 'Recipient email is required', 3000);
            return;
        }
        setSubmitting(true);
        fetch('/api/system/config/test-email', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ to: testEmailTo })
        })
        .then(res => res.json())
        .then(data => {
            setSubmitting(false);
            if (data.code === 200) {
                addToast('success', data.msg || 'Test email sent!', 5000);
                setIsTestEmailOpen(false);
            } else {
                addToast('error', data.msg || 'Failed to send test email', 5000);
            }
        })
        .catch(() => {
            setSubmitting(false);
            addToast('error', 'Failed to send test email', 5000);
        });
    };

    const hasSmtpConfig = configs.some(c => smtpKeys.includes(c.configKey) && c.configValue);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = () => {
        setSubmitting(true);
        
        const url = modalMode === 'add' 
            ? '/api/system/config' 
            : '/api/system/config';
        
        const method = modalMode === 'add' ? 'POST' : 'PUT';
        const body = modalMode === 'add' 
            ? { ...formData } 
            : { ...formData, configId: currentConfig.configId };

        fetch(url, {
            method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(body)
        })
        .then(res => res.json())
        .then(data => {
            setSubmitting(false);
            if (data.code === 200) {
                setIsModalOpen(false);
                addToast('success', `Config "${formData.configName}" ${modalMode === 'add' ? 'created' : 'updated'} successfully`, 3000);
                fetchConfigs();
            } else {
                addToast('error', data.msg || `Failed to ${modalMode} config`, 5000);
            }
        })
        .catch(err => {
            setSubmitting(false);
            console.error(`Failed to ${modalMode} config:`, err);
            addToast('error', `Failed to ${modalMode} config`, 5000);
        });
    };

    const columns = [
        { key: 'configId', header: 'ID', sortable: true, align: 'center', width: '60px' },
        {
            key: 'configName',
            header: 'Configuration Title',
            sortable: true,
            render: (value, row) => (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                    <div style={{ fontWeight: 600, fontSize: '13px' }}>{value}</div>
                    <div style={{ fontSize: '10px', color: 'var(--text-muted)', fontFamily: 'monospace', letterSpacing: '0.2px' }}>{row.configKey}</div>
                </div>
            )
        },
        {
            key: 'configValue',
            header: 'Value',
            sortable: true,
            width: '25%',
            render: (value) => (
                <code style={{ 
                    fontSize: '11px', 
                    padding: '2px 6px', 
                    background: 'var(--bg-tertiary)', 
                    border: '1px solid var(--border-color)',
                    borderRadius: '4px',
                    color: 'var(--primary-color)',
                    display: 'inline-block',
                    maxWidth: '300px',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap'
                }}>{value}</code>
            )
        },
        {
            key: 'configType',
            header: 'Built-in',
            sortable: true,
            align: 'center',
            width: '80px',
            render: (value) => (
                <span style={{
                    padding: '2px 8px',
                    borderRadius: '4px',
                    fontSize: '10px',
                    fontWeight: 700,
                    textTransform: 'uppercase',
                    background: value === 'Y' ? 'rgba(16, 185, 129, 0.1)' : 'rgba(100, 116, 139, 0.1)',
                    color: value === 'Y' ? '#10b981' : '#64748b'
                }}>
                    {value === 'Y' ? 'System' : 'Custom'}
                </span>
            )
        },
        {
            key: 'remark',
            header: 'Description',
            sortable: false,
            render: (value) => <span style={{ color: 'var(--text-muted)', fontSize: '11px' }}>{value || 'No description provided'}</span>
        }
    ];

    const actions = [
        { label: 'View', icon: Eye, onClick: handleViewClick },
        { label: 'Edit', icon: Edit, onClick: handleEditClick },
        { label: 'Delete', icon: Trash2, danger: true, onClick: handleDeleteClick }
    ];

    const toolbarActions = [
        {
            label: 'Refresh',
            icon: RefreshCw,
            onClick: fetchConfigs
        },
        {
            label: 'Test Email',
            icon: Send,
            onClick: handleTestEmail,
            disabled: !hasSmtpConfig
        }
    ];

    return (
        <div className="page-container">
            <div className="page-header" style={{ padding: '8px 16px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{
                        width: '32px',
                        height: '32px',
                        borderRadius: '8px',
                        background: 'var(--primary-soft)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'var(--primary-color)',
                        border: '1px solid var(--primary-soft)'
                    }}>
                        <Settings size={18} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '14px', fontWeight: 800, margin: 0 }}>Configuration Engine</h2>
                        <p style={{ fontSize: '10px !important', color: 'var(--text-muted)', margin: '2px 0 0' }}>
                            Advanced system parameters & environment variables
                        </p>
                    </div>
                </div>
                <button 
                    className="btn btn-primary" 
                    onClick={handleAddClick}
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '6px',
                        padding: '6px 12px',
                    }}
                >
                    <Plus size={14} />
                    Add Entry
                </button>
            </div>

            <DataGrid
                data={configs}
                columns={columns}
                actions={actions}
                toolbarActions={toolbarActions}
                loading={loading}
                searchable={true}
                sortable={true}
                filterable={true}
                selectable={true}
                pagination={true}
                pageSize={10}
                emptyMessage="No configurations found."
            />

            {/* Add/Edit Modal */}
            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title={modalMode === 'add' ? 'Add Config' : modalMode === 'edit' ? 'Edit Config' : 'View Config'}
                size="small"
                compact={true}
                footer={modalMode !== 'view' && (
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
                            style={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: '6px'
                            }}
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
                )}
            >
                <div style={{ display: 'flex', flexDirection: 'column' }}>
                    <div className="form-row">
                        <FormInput
                            label="Config Name"
                            name="configName"
                            value={formData.configName}
                            onChange={handleInputChange}
                            placeholder="Enter config name"
                            required
                            disabled={modalMode === 'view'}
                        />
                        <FormInput
                            label="Config Key"
                            name="configKey"
                            value={formData.configKey}
                            onChange={handleInputChange}
                            placeholder="e.g., sys.user.initPassword"
                            required
                            disabled={modalMode === 'view' || modalMode === 'edit'}
                        />
                    </div>

                    <div className="form-row">
                        <FormInput
                            label="Config Value"
                            name="configValue"
                            value={formData.configValue}
                            onChange={handleInputChange}
                            placeholder="Enter config value"
                            required
                            disabled={modalMode === 'view'}
                        />
                        <div className="form-group">
                            <label className="form-label">Is Built-in</label>
                            <select
                                name="configType"
                                value={formData.configType}
                                onChange={handleInputChange}
                                className="form-input"
                                disabled={modalMode === 'view'}
                            >
                                <option value="Y">Yes</option>
                                <option value="N">No</option>
                            </select>
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="form-label">Remark</label>
                        <textarea
                            name="remark"
                            value={formData.remark}
                            onChange={handleInputChange}
                            placeholder="Enter any remarks"
                            className="form-input"
                            rows={3}
                            disabled={modalMode === 'view'}
                        />
                    </div>
                </div>
            </Modal>

            {/* Test Email Modal */}
            <Modal
                isOpen={isTestEmailOpen}
                onClose={() => setIsTestEmailOpen(false)}
                title="Send Test Email"
                size="medium"
                footer={<>
                    <button className="btn btn-secondary" onClick={() => setIsTestEmailOpen(false)} disabled={submitting}>Cancel</button>
                    <button className="btn btn-primary" onClick={sendTestEmail} disabled={submitting} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        {submitting ? 'Sending...' : <><Send size={16} /> Send Test Email</>}
                    </button>
                </>}
            >
                <div style={{ maxHeight: '70vh', overflowY: 'auto' }}>
                    <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '16px' }}>
                        A test email will be sent using your SMTP configuration from <strong>sys_config</strong> table.
                        Make sure you have configured <code style={{ background: 'var(--bg-tertiary)', padding: '2px 6px', borderRadius: '4px' }}>mail.smtp.host</code>, <code style={{ background: 'var(--bg-tertiary)', padding: '2px 6px', borderRadius: '4px' }}>mail.smtp.port</code>, <code style={{ background: 'var(--bg-tertiary)', padding: '2px 6px', borderRadius: '4px' }}>mail.smtp.username</code>, and <code style={{ background: 'var(--bg-tertiary)', padding: '2px 6px', borderRadius: '4px' }}>mail.smtp.password</code>.
                    </p>
                    <div className="form-group">
                        <label className="form-label">Recipient Email *</label>
                        <input
                            className="form-input"
                            value={testEmailTo}
                            onChange={e => setTestEmailTo(e.target.value)}
                            placeholder="user@example.com"
                        />
                    </div>
                </div>
            </Modal>
        </div>
    );
};

export default ConfigList;
