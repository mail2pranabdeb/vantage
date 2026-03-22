import { useState, useEffect } from 'react';
import { Clock, Plus, Play, Pause, RefreshCw, Trash2, Eye, Edit } from 'lucide-react';
import DataGrid from '../components/DataGrid';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';

const JobList = () => {
    const [jobs, setJobs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('add');
    const [currentJob, setCurrentJob] = useState(null);
    const [formData, setFormData] = useState({
        jobName: '',
        jobGroup: '',
        invokeTarget: '',
        cronExpression: '',
        misfirePolicy: '3',
        concurrent: '1',
        status: '0'
    });
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        fetchJobs();
    }, []);

    const fetchJobs = () => {
        setLoading(true);
        fetch('/api/system/job/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setJobs(data.data || []);
                }
                setLoading(false);
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
            jobGroup: '',
            invokeTarget: '',
            cronExpression: '',
            misfirePolicy: '3',
            concurrent: '1',
            status: '0'
        });
        setIsModalOpen(true);
    };

    const handleEditClick = (row) => {
        setModalMode('edit');
        setCurrentJob(row);
        setFormData({
            jobName: row.jobName || '',
            jobGroup: row.jobGroup || '',
            invokeTarget: row.invokeTarget || '',
            cronExpression: row.cronExpression || '',
            misfirePolicy: String(row.misfirePolicy || '3'),
            concurrent: String(row.concurrent || '1'),
            status: row.status || '0'
        });
        setIsModalOpen(true);
    };

    const handleViewClick = (row) => {
        setModalMode('view');
        setCurrentJob(row);
        setFormData({
            jobName: row.jobName || '',
            jobGroup: row.jobGroup || '',
            invokeTarget: row.invokeTarget || '',
            cronExpression: row.cronExpression || '',
            misfirePolicy: String(row.misfirePolicy || '3'),
            concurrent: String(row.concurrent || '1'),
            status: row.status || '0'
        });
        setIsModalOpen(true);
    };

    const handleDeleteClick = (row) => {
        if (window.confirm(`Are you sure you want to delete job "${row.jobName}"?`)) {
            fetch(`/api/system/job/${row.jobId}`, {
                method: 'DELETE'
            })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setJobs(jobs.filter(j => j.jobId !== row.jobId));
                } else {
                    alert(data.msg || 'Failed to delete job');
                }
            })
            .catch(err => {
                console.error("Failed to delete job:", err);
                alert('Failed to delete job');
            });
        }
    };

    const handleRunClick = (row) => {
        if (window.confirm(`Run job "${row.jobName}" now?`)) {
            fetch(`/api/system/job/run`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ jobId: row.jobId })
            })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    alert('Job executed successfully');
                } else {
                    alert(data.msg || 'Failed to run job');
                }
            })
            .catch(err => {
                console.error("Failed to run job:", err);
                alert('Failed to run job');
            });
        }
    };

    const handlePauseClick = (row) => {
        const newStatus = row.status === '0' ? '1' : '0';
        fetch(`/api/system/job/changeStatus`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ jobId: row.jobId, status: newStatus })
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                fetchJobs();
            } else {
                alert(data.msg || 'Failed to update job status');
            }
        })
        .catch(err => {
            console.error("Failed to update job status:", err);
            alert('Failed to update job status');
        });
    };

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
            ? '/api/system/job' 
            : '/api/system/job';
        
        const method = modalMode === 'add' ? 'POST' : 'PUT';
        const body = modalMode === 'add' 
            ? { ...formData, misfirePolicy: parseInt(formData.misfirePolicy), concurrent: formData.concurrent === '1' } 
            : { ...formData, jobId: currentJob.jobId, misfirePolicy: parseInt(formData.misfirePolicy), concurrent: formData.concurrent === '1' };

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
                fetchJobs();
            } else {
                alert(data.msg || `Failed to ${modalMode} job`);
            }
        })
        .catch(err => {
            setSubmitting(false);
            console.error(`Failed to ${modalMode} job:`, err);
            alert(`Failed to ${modalMode} job`);
        });
    };

    const columns = [
        { key: 'jobId', header: 'ID', sortable: true, align: 'center' },
        {
            key: 'jobName',
            header: 'Job Name',
            sortable: true,
            render: (value, row) => (
                <div>
                    <div style={{ fontWeight: 600 }}>{value}</div>
                    <div style={{ fontSize: '11px', color: 'var(--text-muted)', fontFamily: 'monospace' }}>{row.jobGroup}</div>
                </div>
            )
        },
        {
            key: 'invokeTarget',
            header: 'Target',
            sortable: false,
            render: (value) => (
                <span className="badge-outline" style={{ fontSize: '11px', fontFamily: 'monospace', maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    {value}
                </span>
            )
        },
        {
            key: 'cronExpression',
            header: 'Cron',
            sortable: true,
            render: (value) => (
                <span style={{
                    padding: '4px 8px',
                    borderRadius: '6px',
                    fontSize: '11px',
                    fontFamily: 'monospace',
                    background: 'var(--bg-tertiary)',
                    color: 'var(--text-secondary)'
                }}>
                    {value}
                </span>
            )
        },
        {
            key: 'status',
            header: 'Status',
            sortable: true,
            render: (value) => (
                <span className={`status-pill ${value === '0' ? 'active' : 'inactive'}`}>
                    {value === '0' ? 'Running' : 'Paused'}
                </span>
            )
        }
    ];

    const actions = [
        { label: 'Run', icon: Play, onClick: handleRunClick },
        { label: 'Pause', icon: Pause, onClick: handlePauseClick },
        { label: 'Edit', icon: Edit, onClick: handleEditClick },
        { label: 'Delete', icon: Trash2, danger: true, onClick: handleDeleteClick }
    ];

    const toolbarActions = [
        {
            label: 'Refresh',
            icon: RefreshCw,
            onClick: fetchJobs
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
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <Clock size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Job Management</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Schedule and manage background tasks
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
                    Add Job
                </button>
            </div>

            <DataGrid
                data={jobs}
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
                emptyMessage="No scheduled jobs found."
            />

            {/* Add/Edit Modal */}
            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title={modalMode === 'add' ? 'Add Job' : modalMode === 'edit' ? 'Edit Job' : 'View Job'}
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
                            label="Job Name"
                            name="jobName"
                            value={formData.jobName}
                            onChange={handleInputChange}
                            placeholder="Enter job name"
                            required
                            disabled={modalMode === 'view'}
                        />
                        <FormInput
                            label="Job Group"
                            name="jobGroup"
                            value={formData.jobGroup}
                            onChange={handleInputChange}
                            placeholder="e.g., system, user"
                            required
                            disabled={modalMode === 'view'}
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label">Invoke Target</label>
                        <FormInput
                            name="invokeTarget"
                            value={formData.invokeTarget}
                            onChange={handleInputChange}
                            placeholder="e.g., ryTask.ryMultipleParams('ry', 'test')"
                            required
                            disabled={modalMode === 'view'}
                        />
                        <small className="form-help">Method to invoke for this scheduled job</small>
                    </div>

                    <div className="form-row">
                        <FormInput
                            label="Cron Expression"
                            name="cronExpression"
                            value={formData.cronExpression}
                            onChange={handleInputChange}
                            placeholder="e.g., 0/5 * * * * ?"
                            required
                            disabled={modalMode === 'view'}
                        />
                        <div className="form-group">
                            <label className="form-label">Status</label>
                            <select
                                name="status"
                                value={formData.status}
                                onChange={handleInputChange}
                                className="form-input"
                                disabled={modalMode === 'view'}
                            >
                                <option value="0">Running</option>
                                <option value="1">Paused</option>
                            </select>
                        </div>
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label className="form-label">Misfire Policy</label>
                            <select
                                name="misfirePolicy"
                                value={formData.misfirePolicy}
                                onChange={handleInputChange}
                                className="form-input"
                                disabled={modalMode === 'view'}
                            >
                                <option value="1">Fire Now</option>
                                <option value="2">Do Nothing</option>
                                <option value="3">Fire Once</option>
                            </select>
                        </div>
                        <div className="form-group">
                            <label className="form-label">Concurrent</label>
                            <select
                                name="concurrent"
                                value={formData.concurrent}
                                onChange={handleInputChange}
                                className="form-input"
                                disabled={modalMode === 'view'}
                            >
                                <option value="1">Allow</option>
                                <option value="0">Disallow</option>
                            </select>
                        </div>
                    </div>
                </div>
            </Modal>
        </div>
    );
};

export default JobList;
