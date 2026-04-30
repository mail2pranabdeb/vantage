import { useState, useEffect } from 'react';
import { Code, Play, Save, Plus, Edit, Trash2, Terminal, RefreshCw } from 'lucide-react';
import { useToast } from '../components/Toast';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';

const ScriptJobList = () => {
    const { addToast } = useToast();
    const [jobs, setJobs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isEditorOpen, setIsEditorOpen] = useState(false);
    const [isRunning, setIsRunning] = useState(false);
    const [consoleOutput, setConsoleOutput] = useState([]);
    const [modalMode, setModalMode] = useState('add');
    const [currentJob, setCurrentJob] = useState(null);
    const [formData, setFormData] = useState({
        jobName: '',
        jobGroup: 'DEFAULT',
        invokeTarget: '',
        cronExpression: '',
        scriptType: 'javascript',
        scriptContent: '',
        status: '0',
        remark: ''
    });

    const scriptTypes = [
        { value: 'sql', label: 'SQL Script' },
        { value: 'javascript', label: 'JavaScript (Requires Nashorn)' },
        { value: 'groovy', label: 'Groovy (Requires Groovy JSR-223)' },
        { value: 'python', label: 'Python (Requires Jython)' }
    ];

    useEffect(() => {
        fetchJobs();
    }, []);

    const fetchJobs = () => {
        setLoading(true);
        fetch('/api/system/job/list')
            .then(res => res.json())
            .then(data => {
                setLoading(false);
                if (data.code === 200) {
                    // Filter for script jobs (jobs with script content)
                    const scriptJobs = (data.data || []).filter(job => job.scriptContent);
                    setJobs(scriptJobs);
                }
            })
            .catch(err => {
                console.error("Failed to fetch jobs:", err);
                setLoading(false);
            });
    };

    const handleAddClick = () => {
        setModalMode('add');
        setCurrentJob(null);
        setFormData({
            jobName: '',
            jobGroup: 'DEFAULT',
            invokeTarget: 'scriptExecutor.execute',
            cronExpression: '',
            scriptType: 'javascript',
            scriptContent: '// Write your script here\nconsole.log("Hello World!");\n',
            status: '0',
            remark: ''
        });
        setConsoleOutput([]);
        setIsEditorOpen(true);
    };

    const handleEditClick = (row) => {
        setModalMode('edit');
        setCurrentJob(row);
        setFormData({
            jobName: row.jobName || '',
            jobGroup: row.jobGroup || 'DEFAULT',
            invokeTarget: row.invokeTarget || '',
            cronExpression: row.cronExpression || '',
            scriptType: row.scriptType || 'javascript',
            scriptContent: row.scriptContent || '',
            status: row.status || '0',
            remark: row.remark || ''
        });
        setConsoleOutput([]);
        setIsEditorOpen(true);
    };

    const handleDeleteClick = (row) => {
        if (window.confirm(`Delete script job "${row.jobName}"?`)) {
            fetch(`/api/system/job/${row.jobId}`, { method: 'DELETE' })
                .then(res => res.json())
                .then(data => {
                    if (data.code === 200) {
                        addToast('success', 'Job deleted successfully', 3000);
                        fetchJobs();
                    } else {
                        addToast('error', data.msg || 'Failed to delete job', 5000);
                    }
                });
        }
    };

    const handleRunScript = () => {
        setIsRunning(true);
        setConsoleOutput([{ type: 'info', message: 'Running script...' }]);
        
        fetch('/api/system/scriptJob/run', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                scriptType: formData.scriptType,
                scriptContent: formData.scriptContent
            })
        })
        .then(res => res.json())
        .then(data => {
            setIsRunning(false);
            if (data.code === 200) {
                const output = data.data || [];
                setConsoleOutput(output.map(line => ({ type: line.type || 'output', message: line.message || line })));
                addToast('success', 'Script executed successfully', 3000);
            } else {
                setConsoleOutput([{ type: 'error', message: data.msg || 'Script execution failed' }]);
                addToast('error', data.msg || 'Script execution failed', 5000);
            }
        })
        .catch(err => {
            setIsRunning(false);
            console.error("Failed to run script:", err);
            setConsoleOutput([{ type: 'error', message: 'Script execution failed: ' + err.message }]);
            addToast('error', 'Script execution failed', 5000);
        });
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleScriptChange = (value) => {
        setFormData(prev => ({
            ...prev,
            scriptContent: value
        }));
    };

    const handleSubmit = () => {
        if (!formData.jobName || !formData.scriptContent) {
            addToast('error', 'Job name and script content are required', 3000);
            return;
        }

        const url = modalMode === 'add' ? '/api/system/job' : '/api/system/job';
        const method = modalMode === 'add' ? 'POST' : 'PUT';
        const body = { 
            ...formData, 
            jobId: modalMode === 'edit' ? currentJob.jobId : null
        };

        fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                setIsEditorOpen(false);
                addToast('success', data.msg || `Job ${modalMode === 'add' ? 'created' : 'updated'} successfully`, 3000);
                fetchJobs();
            } else {
                addToast('error', data.msg || `Failed to ${modalMode} job`, 5000);
            }
        });
    };

    const columns = [
        { key: 'jobName', header: 'Job Name', sortable: true },
        { key: 'jobGroup', header: 'Group', maxWidth: '100px' },
        { 
            key: 'scriptType', 
            header: 'Script Type',
            maxWidth: '120px',
            render: (value) => (
                <span className="badge badge-outline" style={{ 
                    background: value === 'javascript' ? '#fef3c7' : value === 'sql' ? '#dbeafe' : '#f3e8ff',
                    padding: '4px 8px',
                    borderRadius: '4px',
                    fontSize: '11px'
                }}>
                    {value}
                </span>
            )
        },
        { key: 'cronExpression', header: 'Cron Expression', maxWidth: '150px' },
        { 
            key: 'status', 
            header: 'Status',
            maxWidth: '80px',
            render: (value) => (
                <span className={`status-pill ${value === '0' ? 'active' : 'inactive'}`}>
                    {value === '0' ? 'Active' : 'Paused'}
                </span>
            )
        },
        {
            key: 'actions',
            header: 'Actions',
            maxWidth: '200px',
            actions: [
                { icon: Play, label: 'Run', onClick: handleEditClick, className: 'btn-primary' },
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
                        <Code size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '14px', fontWeight: 700, margin: 0 }}>Script Jobs (GLUE)</h2>
                        <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Write and execute scripts directly from UI
                        </p>
                    </div>
                </div>
                <button
                    className="btn btn-primary"
                    onClick={handleAddClick}
                    style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                >
                    <Plus size={16} />
                    New Script Job
                </button>
            </div>

            {/* Jobs Grid */}
            <div style={{
                background: 'var(--bg-secondary)',
                borderRadius: '8px',
                border: '1px solid var(--border-color)',
                overflow: 'hidden'
            }}>
                {loading ? (
                    <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                        Loading script jobs...
                    </div>
                ) : jobs.length === 0 ? (
                    <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                        <Code size={48} style={{ margin: '0 auto 16px', opacity: 0.3 }} />
                        <p>No script jobs configured</p>
                        <p style={{ fontSize: '12px' }}>Click "New Script Job" to create one</p>
                    </div>
                ) : (
                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead>
                            <tr style={{ background: 'var(--bg-tertiary)', borderBottom: '2px solid var(--border-color)' }}>
                                {columns.map(col => (
                                    <th key={col.key} style={{ 
                                        padding: '10px', 
                                        textAlign: 'left',
                                        maxWidth: col.maxWidth
                                    }}>
                                        {col.header}
                                    </th>
                                ))}
                            </tr>
                        </thead>
                        <tbody>
                            {jobs.map((row, idx) => (
                                <tr key={row.jobId} style={{ 
                                    borderBottom: '1px solid var(--border-color)',
                                    background: idx % 2 === 0 ? 'var(--bg-secondary)' : 'var(--bg-tertiary)'
                                }}>
                                    {columns.map(col => (
                                        <td key={col.key} style={{ padding: '10px', maxWidth: col.maxWidth }}>
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

            {/* Script Editor Modal */}
            <Modal
                isOpen={isEditorOpen}
                onClose={() => setIsEditorOpen(false)}
                title={modalMode === 'add' ? 'New Script Job' : 'Edit Script Job'}
                size="xlarge"
                footer={
                    <>
                        <button
                            className="btn btn-secondary"
                            onClick={() => setIsEditorOpen(false)}
                            disabled={isRunning}
                        >
                            Cancel
                        </button>
                        <button
                            className="btn btn-primary"
                            onClick={handleRunScript}
                            disabled={isRunning || modalMode === 'edit'}
                            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                        >
                            {isRunning ? (
                                <>
                                    <div style={{
                                        width: '12px',
                                        height: '12px',
                                        border: '2px solid white',
                                        borderBottomColor: 'transparent',
                                        borderRadius: '50%',
                                        animation: 'spin 1s linear infinite'
                                    }} />
                                    Running...
                                </>
                            ) : (
                                <>
                                    <Play size={16} />
                                    Run Script
                                </>
                            )}
                        </button>
                        <button
                            className="btn btn-secondary"
                            onClick={handleSubmit}
                            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                        >
                            <Save size={16} />
                            {modalMode === 'add' ? 'Create' : 'Save'}
                        </button>
                    </>
                }
            >
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', maxHeight: '70vh' }}>
                    {/* Left Panel - Editor */}
                    <div style={{ display: 'flex', flexDirection: 'column' }}>
                        <h3 style={{ fontSize: '13px', fontWeight: 600, margin: '0 0 12px' }}>Script Editor</h3>
                        
                        <div className="form-row">
                            <FormInput
                                label="Job Name *"
                                name="jobName"
                                value={formData.jobName}
                                onChange={handleInputChange}
                                placeholder="e.g., Data Cleanup Script"
                            />
                            <div className="form-group">
                                <label className="form-label">Script Type</label>
                                <select
                                    name="scriptType"
                                    value={formData.scriptType}
                                    onChange={handleInputChange}
                                    className="form-input"
                                >
                                    {scriptTypes.map(type => (
                                        <option key={type.value} value={type.value}>{type.label}</option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <div className="form-row">
                            <FormInput
                                label="Cron Expression"
                                name="cronExpression"
                                value={formData.cronExpression}
                                onChange={handleInputChange}
                                placeholder="0 0 2 * * ? (empty for manual execution)"
                            />
                            <div className="form-group">
                                <label className="form-label">Status</label>
                                <select
                                    name="status"
                                    value={formData.status}
                                    onChange={handleInputChange}
                                    className="form-input"
                                >
                                    <option value="0">Active</option>
                                    <option value="1">Paused</option>
                                </select>
                            </div>
                        </div>

                        <div className="form-group" style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                            <label className="form-label">Script Content *</label>
                            <textarea
                                name="scriptContent"
                                value={formData.scriptContent}
                                onChange={(e) => handleScriptChange(e.target.value)}
                                placeholder="// Write your script here"
                                rows={20}
                                className="form-input"
                                style={{ 
                                    fontFamily: 'Consolas, Monaco, monospace', 
                                    fontSize: '12px',
                                    flex: 1,
                                    resize: 'none'
                                }}
                            />
                            <small className="form-help">
                                {formData.scriptType === 'javascript' && 'JavaScript (Nashorn) - Can access Java classes'}
                                {formData.scriptType === 'sql' && 'SQL Script - Will execute against default datasource'}
                            </small>
                        </div>
                    </div>

                    {/* Right Panel - Console Output */}
                    <div style={{ display: 'flex', flexDirection: 'column' }}>
                        <h3 style={{ fontSize: '13px', fontWeight: 600, margin: '0 0 12px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <Terminal size={16} />
                            Console Output
                        </h3>
                        
                        <div style={{
                            flex: 1,
                            background: '#1e1e1e',
                            borderRadius: '6px',
                            padding: '12px',
                            fontFamily: 'Consolas, Monaco, monospace',
                            fontSize: '12px',
                            overflowY: 'auto',
                            color: '#d4d4d4',
                            minHeight: '400px'
                        }}>
                            {consoleOutput.length === 0 ? (
                                <div style={{ color: '#6a6a6a', fontStyle: 'italic' }}>
                                    Click "Run Script" to see output...
                                </div>
                            ) : (
                                consoleOutput.map((line, idx) => (
                                    <div key={idx} style={{ 
                                        marginBottom: '4px',
                                        color: line.type === 'error' ? '#f48771' : 
                                               line.type === 'warn' ? '#dcdcaa' : 
                                               line.type === 'info' ? '#569cd6' : '#d4d4d4'
                                    }}>
                                        <span style={{ opacity: 0.5, marginRight: '8px' }}>
                                            [{new Date().toLocaleTimeString()}]
                                        </span>
                                        {line.message}
                                    </div>
                                ))
                            )}
                        </div>
                    </div>
                </div>
            </Modal>
        </div>
    );
};

export default ScriptJobList;
