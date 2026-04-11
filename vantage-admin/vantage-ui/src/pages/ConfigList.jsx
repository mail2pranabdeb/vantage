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
                        addToast('success', `Loaded ${data.data.length} config(s)`, 2000);
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
        { key: 'configId', header: 'ID', sortable: true, align: 'center' },
        {
            key: 'configName',
            header: 'Name',
            sortable: true,
            render: (value, row) => (
                <div>
                    <div style={{ fontWeight: 600 }}>{value}</div>
                    <div style={{ fontSize: '11px', color: 'var(--text-muted)', fontFamily: 'monospace' }}>{row.configKey}</div>
                </div>
            )
        },
        {
            key: 'configValue',
            header: 'Value',
            sortable: true,
            render: (value) => (
                <span className="badge-outline" style={{ fontSize: '12px' }}>{value}</span>
            )
        },
        {
            key: 'configType',
            header: 'Type',
            sortable: true,
            align: 'center',
            render: (value) => (
                <span style={{
                    padding: '4px 10px',
                    borderRadius: '12px',
                    fontSize: '11px',
                    fontWeight: 600,
                    background: value === 'Y' ? '#10b98120' : '#3b82f620',
                    color: value === 'Y' ? '#10b981' : '#3b82f6'
                }}>
                    {value === 'Y' ? 'Yes' : 'No'}
                </span>
            )
        },
        {
            key: 'remark',
            header: 'Remark',
            sortable: false,
            render: (value) => <span style={{ color: 'var(--text-muted)' }}>{value || '-'}</span>
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
            <div className="page-header">
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{
                        width: '40px',
                        height: '40px',
                        borderRadius: '10px',
                        background: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <Settings size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Config Management</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Manage system configuration parameters
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
                    Add Config
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
