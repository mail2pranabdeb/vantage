import { useState, useEffect } from 'react';
import { Settings, Mail, Building, Bell, Save, RefreshCw } from 'lucide-react';
import FormInput from '../components/FormInput';
import { useToast } from '../components/Toast';

const SystemSettings = () => {
    const { addToast } = useToast();
    const [loading, setLoading] = useState(false);
    const [activeTab, setActiveTab] = useState('basic');
    const [configs, setConfigs] = useState({
        // Basic Settings
        'system.name': 'Vantage Admin',
        'system.logo': '',
        'system.favicon': '',
        'system.copyright': '© 2024 Vantage. All rights reserved.',
        
        // Email Settings
        'mail.host': '',
        'mail.port': '587',
        'mail.username': '',
        'mail.password': '',
        'mail.fromEmail': '',
        'mail.fromName': 'Vantage Admin',
        'mail.enableAuth': 'true',
        'mail.enableTls': 'true',
        
        // Notification Settings
        'notification.job.failure': 'true',
        'notification.job.success': 'false',
        'notification.login.failure': 'true',
        'notification.admin.email': ''
    });

    useEffect(() => {
        fetchConfigs();
    }, []);

    const fetchConfigs = () => {
        setLoading(true);
        fetch('/api/system/config/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    const configMap = {};
                    (data.data || []).forEach(config => {
                        configMap[config.configKey] = config.configValue;
                    });
                    setConfigs(prev => ({ ...prev, ...configMap }));
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch configs:", err);
                setLoading(false);
            });
    };

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setConfigs(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? (checked ? 'true' : 'false') : value
        }));
    };

    const handleSave = (category) => {
        const configsToSave = [];
        
        // Convert configs to array format for API
        Object.entries(configs).forEach(([key, value]) => {
            if (key.startsWith(category === 'basic' ? 'system.' : 
                               category === 'email' ? 'mail.' : 'notification.')) {
                configsToSave.push({
                    configKey: key,
                    configValue: value,
                    configType: 'Y'
                });
            }
        });

        fetch('/api/system/config/batch', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(configsToSave)
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                addToast('success', data.msg || `${category.charAt(0).toUpperCase() + category.slice(1)} settings saved!`, 3000);
            } else {
                addToast('error', data.msg || 'Failed to save settings', 4000);
            }
        })
        .catch(err => {
            console.error("Failed to save configs:", err);
            addToast('error', 'Failed to save settings', 5000);
        });
    };

    const handleTestConnection = () => {
        const testEmail = prompt('Enter email address to receive test email:');
        if (!testEmail) return;

        setLoading(true);
        fetch('/api/system/config/test-email', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                to: testEmail,
                host: configs['mail.host'],
                port: configs['mail.port'],
                username: configs['mail.username'],
                password: configs['mail.password'],
                enableAuth: configs['mail.enableAuth'],
                enableTls: configs['mail.enableTls']
            })
        })
        .then(res => res.json())
        .then(data => {
            setLoading(false);
            if (data.code === 200) {
                addToast('success', data.msg, 5000);
            } else {
                addToast('error', data.msg || 'Failed to send test email', 5000);
            }
        })
        .catch(err => {
            setLoading(false);
            console.error("Failed to send test email:", err);
            addToast('error', 'Failed to send test email', 5000);
        });
    };

    const tabs = [
        { id: 'basic', label: 'Basic', icon: Building },
        { id: 'email', label: 'Email', icon: Mail },
        { id: 'notification', label: 'Notifications', icon: Bell }
    ];

    return (
        <div style={{
            height: 'calc(100vh - 70px)',
            overflow: 'auto',
            padding: '8px',
            maxWidth: '1400px',
            margin: '0 auto'
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
                        <Settings size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>System Settings</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Configure system-wide settings
                        </p>
                    </div>
                </div>
            </div>

            {/* Tabs */}
            <div style={{ display: 'flex', gap: '8px', marginBottom: '20px', borderBottom: '2px solid var(--border-color)', paddingBottom: '8px' }}>
                {tabs.map(tab => {
                    const Icon = tab.icon;
                    return (
                        <button
                            key={tab.id}
                            onClick={() => setActiveTab(tab.id)}
                            style={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: '8px',
                                padding: '10px 20px',
                                background: activeTab === tab.id ? 'var(--primary)' : 'transparent',
                                color: activeTab === tab.id ? 'white' : 'var(--text-secondary)',
                                border: 'none',
                                borderRadius: '8px 8px 0 0',
                                cursor: 'pointer',
                                fontSize: '14px',
                                fontWeight: activeTab === tab.id ? 600 : 500,
                                transition: 'all 0.2s'
                            }}
                        >
                            <Icon size={18} />
                            {tab.label}
                        </button>
                    );
                })}
            </div>

            {/* Basic Settings Tab */}
            {activeTab === 'basic' && (
                <div style={{ maxWidth: '800px' }}>
                    <div style={{ padding: '20px', background: 'var(--bg-secondary)', borderRadius: '12px', marginBottom: '20px' }}>
                        <h3 style={{ fontSize: '16px', fontWeight: 600, margin: '0 0 16px' }}>Basic Information</h3>
                        
                        <div className="form-row">
                            <FormInput
                                label="System Name"
                                name="system.name"
                                value={configs['system.name']}
                                onChange={handleInputChange}
                                placeholder="Vantage Admin"
                                disabled={loading}
                            />
                            <FormInput
                                label="Copyright Text"
                                name="system.copyright"
                                value={configs['system.copyright']}
                                onChange={handleInputChange}
                                placeholder="© 2024 Vantage. All rights reserved."
                                disabled={loading}
                            />
                        </div>

                        <div className="form-row">
                            <FormInput
                                label="Logo URL"
                                name="system.logo"
                                value={configs['system.logo']}
                                onChange={handleInputChange}
                                placeholder="/assets/logo.png"
                                disabled={loading}
                            />
                            <FormInput
                                label="Favicon URL"
                                name="system.favicon"
                                value={configs['system.favicon']}
                                onChange={handleInputChange}
                                placeholder="/assets/favicon.ico"
                                disabled={loading}
                            />
                        </div>

                        {configs['system.logo'] && (
                            <div style={{ marginTop: '16px', padding: '16px', background: 'var(--bg-tertiary)', borderRadius: '8px' }}>
                                <label className="form-label">Logo Preview</label>
                                <img src={configs['system.logo']} alt="Logo Preview" style={{ maxHeight: '80px', borderRadius: '8px' }} />
                            </div>
                        )}

                        <div style={{ marginTop: '20px', display: 'flex', justifyContent: 'flex-end' }}>
                            <button
                                className="btn btn-primary"
                                onClick={() => handleSave('basic')}
                                disabled={loading}
                                style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
                            >
                                <Save size={18} />
                                Save Basic Settings
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Email Settings Tab */}
            {activeTab === 'email' && (
                <div style={{ maxWidth: '800px' }}>
                    <div style={{ padding: '20px', background: 'var(--bg-secondary)', borderRadius: '12px', marginBottom: '20px' }}>
                        <h3 style={{ fontSize: '16px', fontWeight: 600, margin: '0 0 16px' }}>SMTP Configuration</h3>
                        
                        <div className="form-row">
                            <FormInput
                                label="SMTP Host *"
                                name="mail.host"
                                value={configs['mail.host']}
                                onChange={handleInputChange}
                                placeholder="smtp.gmail.com"
                                disabled={loading}
                            />
                            <FormInput
                                label="SMTP Port *"
                                name="mail.port"
                                type="number"
                                value={configs['mail.port']}
                                onChange={handleInputChange}
                                placeholder="587"
                                disabled={loading}
                            />
                        </div>

                        <div className="form-row">
                            <FormInput
                                label="Username"
                                name="mail.username"
                                value={configs['mail.username']}
                                onChange={handleInputChange}
                                placeholder="your-email@gmail.com"
                                disabled={loading}
                            />
                            <FormInput
                                label="Password"
                                name="mail.password"
                                type="password"
                                value={configs['mail.password']}
                                onChange={handleInputChange}
                                placeholder="••••••••"
                                disabled={loading}
                            />
                        </div>

                        <div className="form-row">
                            <FormInput
                                label="From Email *"
                                name="mail.fromEmail"
                                value={configs['mail.fromEmail']}
                                onChange={handleInputChange}
                                placeholder="noreply@yourcompany.com"
                                disabled={loading}
                            />
                            <FormInput
                                label="From Name"
                                name="mail.fromName"
                                value={configs['mail.fromName']}
                                onChange={handleInputChange}
                                placeholder="Vantage Admin"
                                disabled={loading}
                            />
                        </div>

                        <div style={{ display: 'flex', gap: '16px', marginTop: '16px' }}>
                            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                                <input
                                    type="checkbox"
                                    name="mail.enableAuth"
                                    checked={configs['mail.enableAuth'] === 'true'}
                                    onChange={handleInputChange}
                                    style={{ width: '16px', height: '16px' }}
                                />
                                <span style={{ fontSize: '13px' }}>Enable Authentication</span>
                            </label>
                            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                                <input
                                    type="checkbox"
                                    name="mail.enableTls"
                                    checked={configs['mail.enableTls'] === 'true'}
                                    onChange={handleInputChange}
                                    style={{ width: '16px', height: '16px' }}
                                />
                                <span style={{ fontSize: '13px' }}>Enable TLS/SSL</span>
                            </label>
                        </div>

                        <div style={{ marginTop: '20px', display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
                            <button
                                className="btn btn-secondary"
                                onClick={handleTestConnection}
                                disabled={loading}
                                style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
                            >
                                <RefreshCw size={18} />
                                Test Connection
                            </button>
                            <button
                                className="btn btn-primary"
                                onClick={() => handleSave('email')}
                                disabled={loading}
                                style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
                            >
                                <Save size={18} />
                                Save Email Settings
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Notification Settings Tab */}
            {activeTab === 'notification' && (
                <div style={{ maxWidth: '800px' }}>
                    <div style={{ padding: '20px', background: 'var(--bg-secondary)', borderRadius: '12px', marginBottom: '20px' }}>
                        <h3 style={{ fontSize: '16px', fontWeight: 600, margin: '0 0 16px' }}>Notification Preferences</h3>
                        
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                            <label style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer', padding: '12px', background: 'var(--bg-tertiary)', borderRadius: '8px' }}>
                                <input
                                    type="checkbox"
                                    name="notification.job.failure"
                                    checked={configs['notification.job.failure'] === 'true'}
                                    onChange={handleInputChange}
                                    style={{ width: '18px', height: '18px' }}
                                />
                                <div>
                                    <div style={{ fontWeight: 600, fontSize: '14px' }}>Job Failure Notifications</div>
                                    <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Send email when a scheduled job fails</div>
                                </div>
                            </label>

                            <label style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer', padding: '12px', background: 'var(--bg-tertiary)', borderRadius: '8px' }}>
                                <input
                                    type="checkbox"
                                    name="notification.job.success"
                                    checked={configs['notification.job.success'] === 'true'}
                                    onChange={handleInputChange}
                                    style={{ width: '18px', height: '18px' }}
                                />
                                <div>
                                    <div style={{ fontWeight: 600, fontSize: '14px' }}>Job Success Notifications</div>
                                    <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Send email when a scheduled job completes successfully</div>
                                </div>
                            </label>

                            <label style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer', padding: '12px', background: 'var(--bg-tertiary)', borderRadius: '8px' }}>
                                <input
                                    type="checkbox"
                                    name="notification.login.failure"
                                    checked={configs['notification.login.failure'] === 'true'}
                                    onChange={handleInputChange}
                                    style={{ width: '18px', height: '18px' }}
                                />
                                <div>
                                    <div style={{ fontWeight: 600, fontSize: '14px' }}>Login Failure Alerts</div>
                                    <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Send email on multiple failed login attempts</div>
                                </div>
                            </label>
                        </div>

                        <div style={{ marginTop: '20px' }}>
                            <FormInput
                                label="Admin Notification Email"
                                name="notification.admin.email"
                                value={configs['notification.admin.email']}
                                onChange={handleInputChange}
                                placeholder="admin@yourcompany.com"
                                disabled={loading}
                            />
                            <small className="form-help">Email address to receive system notifications</small>
                        </div>

                        <div style={{ marginTop: '20px', display: 'flex', justifyContent: 'flex-end' }}>
                            <button
                                className="btn btn-primary"
                                onClick={() => handleSave('notification')}
                                disabled={loading}
                                style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
                            >
                                <Save size={18} />
                                Save Notification Settings
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default SystemSettings;
