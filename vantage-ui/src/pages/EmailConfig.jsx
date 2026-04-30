import { useState, useEffect } from 'react';
import { Mail, Save, RefreshCw, Send, Check, X } from 'lucide-react';
import FormInput from '../components/FormInput';
import Modal from '../components/Modal';

const EmailConfig = () => {
    const [config, setConfig] = useState({
        host: '',
        port: '587',
        username: '',
        password: '',
        fromEmail: '',
        fromName: 'Vantage Admin',
        enableAuth: true,
        enableTls: true
    });
    const [loading, setLoading] = useState(false);
    const [testing, setTesting] = useState(false);
    const [testEmail, setTestEmail] = useState('');
    const [isTestModalOpen, setIsTestModalOpen] = useState(false);

    useEffect(() => {
        fetchConfig();
    }, []);

    const fetchConfig = () => {
        setLoading(true);
        fetch('/api/system/email-config')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200 && data.data) {
                    setConfig(data.data);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch email config:", err);
                setLoading(false);
            });
    };

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setConfig(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSave = () => {
        fetch('/api/system/email-config', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(config)
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                alert('Email configuration saved successfully!');
            } else {
                alert(data.msg || 'Failed to save configuration');
            }
        })
        .catch(err => {
            console.error("Failed to save config:", err);
            alert('Failed to save configuration');
        });
    };

    const handleTest = () => {
        setTestEmail('');
        setIsTestModalOpen(true);
    };

    const sendTestEmail = () => {
        setTesting(true);
        fetch('/api/system/email-config/test', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ to: testEmail })
        })
        .then(res => res.json())
        .then(data => {
            setTesting(false);
            if (data.code === 200) {
                alert('Test email sent successfully! Check your inbox.');
                setIsTestModalOpen(false);
            } else {
                alert(data.msg || 'Failed to send test email');
            }
        })
        .catch(err => {
            setTesting(false);
            console.error("Failed to send test email:", err);
            alert('Failed to send test email');
        });
    };

    return (
        <div className="page-container">
            <div className="page-header">
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
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Email Configuration</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Configure SMTP settings for email notifications
                        </p>
                    </div>
                </div>
                <div style={{ display: 'flex', gap: '8px' }}>
                    <button
                        className="btn btn-secondary"
                        onClick={handleTest}
                        disabled={loading}
                        style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
                    >
                        <Send size={18} />
                        Test Email
                    </button>
                    <button
                        className="btn btn-primary"
                        onClick={handleSave}
                        disabled={loading}
                        style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
                    >
                        <Save size={18} />
                        Save Configuration
                    </button>
                </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '20px' }}>
                {/* SMTP Settings */}
                <div style={{ padding: '20px', background: 'var(--bg-secondary)', borderRadius: '12px' }}>
                    <h3 style={{ fontSize: '16px', fontWeight: 600, margin: '0 0 16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <Mail size={18} /> SMTP Server Settings
                    </h3>
                    
                    <div className="form-row">
                        <FormInput
                            label="SMTP Host *"
                            name="host"
                            value={config.host}
                            onChange={handleInputChange}
                            placeholder="smtp.gmail.com"
                            disabled={loading}
                        />
                        <FormInput
                            label="SMTP Port *"
                            name="port"
                            type="number"
                            value={config.port}
                            onChange={handleInputChange}
                            placeholder="587"
                            disabled={loading}
                        />
                    </div>

                    <div className="form-row">
                        <FormInput
                            label="Username"
                            name="username"
                            value={config.username}
                            onChange={handleInputChange}
                            placeholder="your-email@gmail.com"
                            disabled={loading}
                        />
                        <FormInput
                            label="Password"
                            name="password"
                            type="password"
                            value={config.password}
                            onChange={handleInputChange}
                            placeholder="••••••••"
                            disabled={loading}
                        />
                    </div>

                    <div style={{ display: 'flex', gap: '16px', marginTop: '16px' }}>
                        <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                            <input
                                type="checkbox"
                                name="enableAuth"
                                checked={config.enableAuth}
                                onChange={handleInputChange}
                                style={{ width: '16px', height: '16px' }}
                            />
                            <span style={{ fontSize: '13px' }}>Enable Authentication</span>
                        </label>
                        <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                            <input
                                type="checkbox"
                                name="enableTls"
                                checked={config.enableTls}
                                onChange={handleInputChange}
                                style={{ width: '16px', height: '16px' }}
                            />
                            <span style={{ fontSize: '13px' }}>Enable TLS/SSL</span>
                        </label>
                    </div>
                </div>

                {/* Sender Settings */}
                <div style={{ padding: '20px', background: 'var(--bg-secondary)', borderRadius: '12px' }}>
                    <h3 style={{ fontSize: '16px', fontWeight: 600, margin: '0 0 16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <Send size={18} /> Sender Settings
                    </h3>
                    
                    <FormInput
                        label="From Email *"
                        name="fromEmail"
                        value={config.fromEmail}
                        onChange={handleInputChange}
                        placeholder="noreply@yourcompany.com"
                        disabled={loading}
                    />
                    
                    <FormInput
                        label="From Name"
                        name="fromName"
                        value={config.fromName}
                        onChange={handleInputChange}
                        placeholder="Vantage Admin"
                        disabled={loading}
                    />

                    <div style={{ 
                        marginTop: '20px', 
                        padding: '16px', 
                        background: 'var(--bg-tertiary)', 
                        borderRadius: '8px' 
                    }}>
                        <h4 style={{ margin: '0 0 12px', fontSize: '14px', fontWeight: 600 }}>Common SMTP Settings</h4>
                        <div style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.8' }}>
                            <div><strong>Gmail:</strong> smtp.gmail.com : 587 (TLS)</div>
                            <div><strong>Outlook:</strong> smtp.office365.com : 587 (STARTTLS)</div>
                            <div><strong>Yahoo:</strong> smtp.mail.yahoo.com : 465 (SSL)</div>
                            <div><strong>SendGrid:</strong> smtp.sendgrid.net : 587</div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Test Email Modal */}
            <Modal
                isOpen={isTestModalOpen}
                onClose={() => setIsTestModalOpen(false)}
                title="Send Test Email"
                size="small"
                footer={
                    <>
                        <button
                            className="btn btn-secondary"
                            onClick={() => setIsTestModalOpen(false)}
                            disabled={testing}
                        >
                            Cancel
                        </button>
                        <button
                            className="btn btn-primary"
                            onClick={sendTestEmail}
                            disabled={testing}
                            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                        >
                            {testing && (
                                <div style={{
                                    width: '12px',
                                    height: '12px',
                                    border: '2px solid white',
                                    borderBottomColor: 'transparent',
                                    borderRadius: '50%',
                                    animation: 'spin 1s linear infinite'
                                }} />
                            )}
                            <Send size={16} />
                            Send Test
                        </button>
                    </>
                }
            >
                <div className="form-group">
                    <label className="form-label">Recipient Email</label>
                    <FormInput
                        name="testEmail"
                        value={testEmail}
                        onChange={(e) => setTestEmail(e.target.value)}
                        placeholder="recipient@example.com"
                        disabled={testing}
                    />
                    <small className="form-help">
                        A test email will be sent to verify your SMTP configuration
                    </small>
                </div>
            </Modal>
        </div>
    );
};

export default EmailConfig;
