import { useState, useEffect } from 'react';
import { Database, Plus, Edit, Trash2, RefreshCw, Play, Check, X } from 'lucide-react';
import { useToast } from '../components/Toast';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';

const DatasourceList = () => {
    const { addToast } = useToast();
    const [datasources, setDatasources] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isTestModalOpen, setIsTestModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('add');
    const [currentDatasource, setCurrentDatasource] = useState(null);
    const [testing, setTesting] = useState(false);
    const [testResult, setTestResult] = useState(null);
    const [formData, setFormData] = useState({
        datasourceName: '',
        datasourceKey: '',
        dbType: 'H2',
        url: '',
        username: '',
        password: '',
        driverClass: '',
        status: '0',
        remark: ''
    });

    const dbTypes = [
        { value: 'H2', label: 'H2 Database' },
        { value: 'MYSQL', label: 'MySQL' },
        { value: 'POSTGRESQL', label: 'PostgreSQL' },
        { value: 'ORACLE', label: 'Oracle' },
        { value: 'SQLSERVER', label: 'SQL Server' }
    ];

    useEffect(() => {
        fetchDatasources();
    }, []);

    const fetchDatasources = () => {
        setLoading(true);
        fetch('/api/system/datasource/list')
            .then(res => res.json())
            .then(data => {
                setLoading(false);
                if (data.code === 200) {
                    setDatasources(data.data || []);
                } else {
                    addToast('error', data.msg || 'Failed to load datasources', 4000);
                }
            })
            .catch(err => {
                console.error("Failed to fetch datasources:", err);
                setLoading(false);
                addToast('error', 'Failed to load datasources', 5000);
            });
    };

    const handleAddClick = () => {
        setModalMode('add');
        setCurrentDatasource(null);
        setFormData({
            datasourceName: '',
            datasourceKey: '',
            dbType: 'H2',
            url: '',
            username: '',
            password: '',
            driverClass: '',
            status: '0',
            remark: ''
        });
        setIsModalOpen(true);
    };

    const handleEditClick = (row) => {
        setModalMode('edit');
        setCurrentDatasource(row);
        setFormData({
            datasourceName: row.datasourceName || '',
            datasourceKey: row.datasourceKey || '',
            dbType: row.dbType || 'H2',
            url: row.url || '',
            username: row.username || '',
            password: row.password || '',
            driverClass: row.driverClass || '',
            status: row.status || '0',
            remark: row.remark || ''
        });
        setIsModalOpen(true);
    };

    const handleDeleteClick = (row) => {
        if (window.confirm(`Are you sure you want to delete datasource "${row.datasourceName}"?`)) {
            fetch(`/api/system/datasource/${row.datasourceId}`, {
                method: 'DELETE'
            })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    addToast('success', `Datasource "${row.datasourceName}" deleted successfully`, 3000);
                    fetchDatasources();
                } else {
                    addToast('error', data.msg || 'Failed to delete datasource', 5000);
                }
            })
            .catch(err => {
                console.error("Failed to delete datasource:", err);
                addToast('error', 'Failed to delete datasource', 5000);
            });
        }
    };

    const handleTestClick = (row) => {
        setCurrentDatasource(row);
        setTestResult(null);
        setIsTestModalOpen(true);
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));

        // Auto-fill driver class and URL when DB type changes
        if (name === 'dbType') {
            fetch(`/api/system/datasource/driver/${value}`)
                .then(res => res.json())
                .then(data => {
                    if (data.code === 200) {
                        setFormData(prev => ({
                            ...prev,
                            driverClass: data.data.driverClass,
                            url: data.data.urlPattern
                        }));
                    }
                });
        }
    };

    const handleSubmit = () => {
        if (!formData.datasourceName || !formData.datasourceKey || !formData.url) {
            addToast('error', 'Please fill in required fields', 3000);
            return;
        }

        const url = modalMode === 'add' ? '/api/system/datasource' : '/api/system/datasource';
        const method = modalMode === 'add' ? 'POST' : 'PUT';
        const body = { 
            ...formData, 
            datasourceId: modalMode === 'edit' ? currentDatasource.datasourceId : null
        };

        fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                setIsModalOpen(false);
                addToast('success', data.msg || `Datasource ${modalMode === 'add' ? 'added' : 'updated'} successfully`, 3000);
                fetchDatasources();
            } else {
                addToast('error', data.msg || `Failed to ${modalMode} datasource`, 5000);
            }
        })
        .catch(err => {
            console.error(`Failed to ${modalMode} datasource:`, err);
            addToast('error', `Failed to ${modalMode} datasource`, 5000);
        });
    };

    const handleTestConnection = () => {
        setTesting(true);
        fetch('/api/system/datasource/test', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(currentDatasource)
        })
        .then(res => res.json())
        .then(data => {
            setTesting(false);
            if (data.code === 200) {
                setTestResult(data.data);
            } else {
                setTestResult({ success: false, message: data.msg });
            }
        })
        .catch(err => {
            setTesting(false);
            console.error("Failed to test connection:", err);
            setTestResult({ success: false, message: 'Connection test failed' });
        });
    };

    const columns = [
        { key: 'datasourceName', header: 'Name', sortable: true },
        { key: 'datasourceKey', header: 'Key', sortable: true },
        { 
            key: 'dbType', 
            header: 'DB Type',
            render: (value) => (
                <span className="badge badge-outline" style={{ 
                    background: value === 'H2' ? '#e0f2fe' : value === 'MYSQL' ? '#fef3c7' : '#dbeafe',
                    padding: '4px 8px',
                    borderRadius: '4px',
                    fontSize: '11px'
                }}>
                    {value}
                </span>
            )
        },
        { key: 'url', header: 'URL', maxWidth: '300px', ellipsis: true },
        { 
            key: 'status', 
            header: 'Status',
            render: (value) => (
                <span className={`status-pill ${value === '0' ? 'active' : 'inactive'}`}>
                    {value === '0' ? 'Active' : 'Inactive'}
                </span>
            )
        },
        { 
            key: 'lastTestStatus', 
            header: 'Last Test',
            render: (value, row) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                    {value === '0' ? (
                        <Check size={14} style={{ color: 'var(--success)' }} />
                    ) : value === '1' ? (
                        <X size={14} style={{ color: 'var(--danger)' }} />
                    ) : (
                        <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Not tested</span>
                    )}
                    {row.lastTestTime && (
                        <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                            {new Date(row.lastTestTime).toLocaleString()}
                        </span>
                    )}
                </div>
            )
        },
        {
            key: 'actions',
            header: 'Actions',
            maxWidth: '280px',
            actions: [
                { icon: Play, label: 'Test', onClick: handleTestClick, className: 'btn-secondary' },
                { icon: Edit, label: 'Edit', onClick: handleEditClick, className: 'btn-secondary' },
                { icon: Trash2, label: 'Delete', onClick: handleDeleteClick, danger: true }
            ]
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
                        <Database size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '14px', fontWeight: 700, margin: 0 }}>Multi-Datasource</h2>
                        <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Manage multiple database connections
                        </p>
                    </div>
                </div>
                <button
                    className="btn btn-primary"
                    onClick={handleAddClick}
                    style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                >
                    <Plus size={16} />
                    Add Datasource
                </button>
            </div>

            {/* Datasources Grid */}
            <div style={{
                background: 'var(--bg-secondary)',
                borderRadius: '8px',
                border: '1px solid var(--border-color)',
                overflow: 'hidden'
            }}>
                {loading ? (
                    <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                        Loading datasources...
                    </div>
                ) : datasources.length === 0 ? (
                    <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                        <Database size={48} style={{ margin: '0 auto 16px', opacity: 0.3 }} />
                        <p>No datasources configured</p>
                        <p style={{ fontSize: '12px' }}>Click "Add Datasource" to create one</p>
                    </div>
                ) : (
                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead>
                            <tr style={{ background: 'var(--bg-tertiary)', borderBottom: '2px solid var(--border-color)' }}>
                                {columns.map(col => (
                                    <th key={col.key} style={{ 
                                        padding: '10px', 
                                        textAlign: 'left',
                                        maxWidth: col.maxWidth,
                                        overflow: 'hidden',
                                        textOverflow: 'ellipsis'
                                    }}>
                                        {col.header}
                                    </th>
                                ))}
                            </tr>
                        </thead>
                        <tbody>
                            {datasources.map((row, idx) => (
                                <tr key={row.datasourceId} style={{ 
                                    borderBottom: '1px solid var(--border-color)',
                                    background: idx % 2 === 0 ? 'var(--bg-secondary)' : 'var(--bg-tertiary)'
                                }}>
                                    {columns.map(col => (
                                        <td key={col.key} style={{ 
                                            padding: '10px',
                                            maxWidth: col.maxWidth,
                                            overflow: 'hidden',
                                            textOverflow: 'ellipsis'
                                        }}>
                                            {col.actions ? (
                                                <div style={{ display: 'flex', gap: '4px' }}>
                                                    {col.actions.map((action, aIdx) => (
                                                        <button
                                                            key={aIdx}
                                                            className={`btn ${action.className || ''}`}
                                                            onClick={() => action.onClick(row)}
                                                            style={{ padding: '4px 8px', fontSize: '11px' }}
                                                            title={action.label}
                                                        >
                                                            <action.icon size={14} />
                                                        </button>
                                                    ))}
                                                </div>
                                            ) : col.render ? (
                                                col.render(row[col.key], row)
                                            ) : (
                                                <span style={{ fontSize: '13px' }}>{row[col.key]}</span>
                                            )}
                                        </td>
                                    ))}
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>

            {/* Add/Edit Modal */}
            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title={modalMode === 'add' ? 'Add Datasource' : 'Edit Datasource'}
                size="large"
                footer={
                    <>
                        <button
                            className="btn btn-secondary"
                            onClick={() => setIsModalOpen(false)}
                        >
                            Cancel
                        </button>
                        <button
                            className="btn btn-primary"
                            onClick={handleSubmit}
                            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                        >
                            {modalMode === 'add' ? 'Create' : 'Save'}
                        </button>
                    </>
                }
            >
                <div style={{ maxHeight: '70vh', overflowY: 'auto', paddingRight: '8px' }}>
                    <div className="form-row">
                        <FormInput
                            label="Datasource Name *"
                            name="datasourceName"
                            value={formData.datasourceName}
                            onChange={handleInputChange}
                            placeholder="e.g., Production Database"
                        />
                        <FormInput
                            label="Datasource Key *"
                            name="datasourceKey"
                            value={formData.datasourceKey}
                            onChange={handleInputChange}
                            placeholder="e.g., prod_db"
                            disabled={modalMode === 'edit'}
                        />
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label className="form-label">Database Type *</label>
                            <select
                                name="dbType"
                                value={formData.dbType}
                                onChange={handleInputChange}
                                className="form-input"
                            >
                                {dbTypes.map(db => (
                                    <option key={db.value} value={db.value}>{db.label}</option>
                                ))}
                            </select>
                        </div>
                        <div className="form-group">
                            <label className="form-label">Status</label>
                            <select
                                name="status"
                                value={formData.status}
                                onChange={handleInputChange}
                                className="form-input"
                            >
                                <option value="0">Active</option>
                                <option value="1">Inactive</option>
                            </select>
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="form-label">JDBC URL *</label>
                        <input
                            type="text"
                            name="url"
                            value={formData.url}
                            onChange={handleInputChange}
                            placeholder="jdbc:h2:file:./data/vantage"
                            className="form-input"
                            style={{ fontFamily: 'monospace', fontSize: '12px' }}
                        />
                    </div>

                    <div className="form-row">
                        <FormInput
                            label="Username *"
                            name="username"
                            value={formData.username}
                            onChange={handleInputChange}
                            placeholder="Database username"
                        />
                        <FormInput
                            label="Password *"
                            name="password"
                            type="password"
                            value={formData.password}
                            onChange={handleInputChange}
                            placeholder="Database password"
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label">Driver Class</label>
                        <input
                            type="text"
                            name="driverClass"
                            value={formData.driverClass}
                            onChange={handleInputChange}
                            placeholder="e.g., org.h2.Driver"
                            className="form-input"
                            style={{ fontFamily: 'monospace', fontSize: '12px' }}
                            readOnly
                        />
                        <small className="form-help">Auto-filled based on database type</small>
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
                        />
                    </div>
                </div>
            </Modal>

            {/* Test Connection Modal */}
            <Modal
                isOpen={isTestModalOpen}
                onClose={() => setIsTestModalOpen(false)}
                title={`Test Connection: ${currentDatasource?.datasourceName || ''}`}
                size="medium"
                footer={
                    <>
                        <button
                            className="btn btn-secondary"
                            onClick={() => setIsTestModalOpen(false)}
                        >
                            Close
                        </button>
                        <button
                            className="btn btn-primary"
                            onClick={handleTestConnection}
                            disabled={testing}
                            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                        >
                            {testing ? (
                                <>
                                    <div style={{
                                        width: '12px',
                                        height: '12px',
                                        border: '2px solid white',
                                        borderBottomColor: 'transparent',
                                        borderRadius: '50%',
                                        animation: 'spin 1s linear infinite'
                                    }} />
                                    Testing...
                                </>
                            ) : (
                                <>
                                    <Play size={16} />
                                    Test Connection
                                </>
                            )}
                        </button>
                    </>
                }
            >
                <div style={{ padding: '20px', textAlign: 'center' }}>
                    {testResult ? (
                        <div style={{
                            padding: '20px',
                            borderRadius: '8px',
                            background: testResult.success ? 'rgba(16, 185, 129, 0.1)' : 'rgba(239, 68, 68, 0.1)',
                            border: `2px solid ${testResult.success ? 'var(--success)' : 'var(--danger)'}`
                        }}>
                            {testResult.success ? (
                                <Check size={48} style={{ color: 'var(--success)', margin: '0 auto 16px' }} />
                            ) : (
                                <X size={48} style={{ color: 'var(--danger)', margin: '0 auto 16px' }} />
                            )}
                            <h3 style={{ 
                                color: testResult.success ? 'var(--success)' : 'var(--danger)',
                                margin: '0 0 8px'
                            }}>
                                {testResult.success ? 'Connection Successful!' : 'Connection Failed'}
                            </h3>
                            <p style={{ margin: 0, color: 'var(--text-secondary)' }}>
                                {testResult.message}
                            </p>
                            {testResult.lastTestTime && (
                                <p style={{ 
                                    marginTop: '16px', 
                                    fontSize: '12px', 
                                    color: 'var(--text-muted)' 
                                }}>
                                    Tested: {new Date(testResult.lastTestTime).toLocaleString()}
                                </p>
                            )}
                        </div>
                    ) : (
                        <div style={{ color: 'var(--text-muted)' }}>
                            <Database size={48} style={{ margin: '0 auto 16px', opacity: 0.3 }} />
                            <p>Click "Test Connection" to verify database connectivity</p>
                            <p style={{ fontSize: '12px', marginTop: '8px' }}>
                                Testing: {currentDatasource?.url}
                            </p>
                        </div>
                    )}
                </div>
            </Modal>
        </div>
    );
};

export default DatasourceList;
