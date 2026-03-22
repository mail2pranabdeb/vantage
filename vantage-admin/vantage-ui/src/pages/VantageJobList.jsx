import { useState, useEffect } from 'react';
import { Clock, Plus, Edit, Trash2, Play, Square, RefreshCw, Zap } from 'lucide-react';
import DataGrid from '../components/DataGrid';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';
import { useToast } from '../components/Toast';

const VantageJobList = () => {
    const { addToast } = useToast();
    const [jobs, setJobs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('add');
    const [currentJob, setCurrentJob] = useState(null);
    const [formData, setFormData] = useState({
        jobGroup: 1,
        jobCron: '',
        jobDesc: '',
        author: '',
        alarmEmail: '',
        executorHandler: '',
        executorParam: '',
        executorRouteStrategy: 'FIRST',
        executorBlockStrategy: 'SERIAL_EXECUTION',
        executorTimeout: 0,
        executorFailRetryCount: 0,
        glueType: 'BEAN',
        triggerStatus: 0
    });
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        fetchJobs();
    }, []);

    const fetchJobs = () => {
        setLoading(true);
        fetch('/vantage/job/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setJobs(data.rows || []);
                    addToast('success', `Loaded ${data.rows?.length || 0} job(s)`, 2000);
                } else {
                    addToast('error', data.msg || 'Failed to load jobs', 4000);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch jobs:", err);
                addToast('error', 'Failed to load jobs', 5000);
                setLoading(false);
            });
    };

    const handleAddClick = () => {
        setModalMode('add');
        setCurrentJob(null);
        setFormData({
            jobGroup: 1,
            jobCron: '',
            jobDesc: '',
            author: '',
            alarmEmail: '',
            executorHandler: '',
            executorParam: '',
            executorRouteStrategy: 'FIRST',
            executorBlockStrategy: 'SERIAL_EXECUTION',
            executorTimeout: 0,
            executorFailRetryCount: 0,
            glueType: 'BEAN',
            triggerStatus: 0
        });
        setIsModalOpen(true);
    };

    const handleEditClick = (row) => {
        setModalMode('edit');
        setCurrentJob(row);
        setFormData({
            jobGroup: row.jobGroup || 1,
            jobCron: row.jobCron || '',
            jobDesc: row.jobDesc || '',
            author: row.author || '',
            alarmEmail: row.alarmEmail || '',
            executorHandler: row.executorHandler || '',
            executorParam: row.executorParam || '',
            executorRouteStrategy: row.executorRouteStrategy || 'FIRST',
            executorBlockStrategy: row.executorBlockStrategy || 'SERIAL_EXECUTION',
            executorTimeout: row.executorTimeout || 0,
            executorFailRetryCount: row.executorFailRetryCount || 0,
            glueType: row.glueType || 'BEAN',
            triggerStatus: row.triggerStatus || 0
        });
        setIsModalOpen(true);
    };

    const handleDeleteClick = (row) => {
        if (window.confirm(`Are you sure you want to delete job "${row.jobDesc}"?`)) {
            fetch(`/vantage/job/${row.id}`, { method: 'DELETE' })
                .then(res => res.json())
                .then(data => {
                    if (data.code === 200) {
                        addToast('success', `Job "${row.jobDesc}" deleted successfully`, 3000);
                        fetchJobs();
                    } else {
                        addToast('error', data.msg || 'Failed to delete job', 5000);
                    }
                })
                .catch(err => {
                    console.error("Failed to delete job:", err);
                    addToast('error', 'Failed to delete job', 5000);
                });
        }
    };

    const handleStartStop = (row, action) => {
        const url = action === 'start' ? '/vantage/job/start' : '/vantage/job/stop';
        fetch(url, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id: row.id, triggerStatus: action === 'start' ? 1 : 0 })
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                addToast('success', `Job ${action === 'start' ? 'started' : 'stopped'} successfully`, 3000);
                fetchJobs();
            } else {
                addToast('error', data.msg || `Failed to ${action} job`, 5000);
            }
        })
        .catch(err => {
            console.error(`Failed to ${action} job:`, err);
            addToast('error', `Failed to ${action} job`, 5000);
        });
    };

    const handleRun = (row) => {
        fetch('/vantage/job/run', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id: row.id })
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                addToast('success', 'Job triggered successfully', 3000);
            } else {
                addToast('error', data.msg || 'Failed to trigger job', 5000);
            }
        })
        .catch(err => {
            console.error("Failed to trigger job:", err);
            addToast('error', 'Failed to trigger job', 5000);
        });
    };

    const handleSubmit = () => {
        setSubmitting(true);
        const url = modalMode === 'add' ? '/vantage/job' : '/vantage/job';
        const method = modalMode === 'add' ? 'POST' : 'PUT';
        const body = modalMode === 'add' 
            ? formData 
            : { ...formData, id: currentJob.id };

        fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        })
        .then(res => res.json())
        .then(data => {
            setSubmitting(false);
            if (data.code === 200) {
                addToast('success', `Job ${modalMode === 'add' ? 'created' : 'updated'} successfully`, 3000);
                setIsModalOpen(false);
                fetchJobs();
            } else {
                addToast('error', data.msg || `Failed to ${modalMode} job`, 5000);
            }
        })
        .catch(err => {
            setSubmitting(false);
            console.error(`Failed to ${modalMode} job:`, err);
            addToast('error', `Failed to ${modalMode} job`, 5000);
        });
    };

    const columns = [
        { key: 'id', header: 'ID', sortable: true, align: 'center', width: 60 },
        {
            key: 'jobDesc',
            header: 'Job Description',
            sortable: true,
            render: (value) => <span style={{ fontWeight: 600 }}>{value || '-'}</span>
        },
        {
            key: 'executorHandler',
            header: 'Executor Handler',
            sortable: true,
            render: (value) => <span className="badge-outline">{value || '-'}</span>
        },
        {
            key: 'jobCron',
            header: 'Cron Expression',
            sortable: true,
            render: (value) => <span style={{ fontFamily: 'monospace', fontSize: '11px' }}>{value || '-'}</span>
        },
        {
            key: 'triggerStatus',
            header: 'Status',
            sortable: true,
            align: 'center',
            render: (value) => (
                <span className={`status-pill ${value === 1 ? 'active' : 'inactive'}`}>
                    {value === 1 ? 'Running' : 'Stopped'}
                </span>
            )
        },
        { key: 'author', header: 'Author', sortable: true, align: 'center' },
        {
            key: 'actions',
            header: 'Actions',
            align: 'center',
            render: (value, row) => (
                <div style={{ display: 'flex', gap: '4px', justifyContent: 'center' }}>
                    <button className="btn-icon" title="Start" onClick={() => handleStartStop(row, 'start')}>
                        <Play size={14} style={{ color: '#10b981' }} />
                    </button>
                    <button className="btn-icon" title="Stop" onClick={() => handleStartStop(row, 'stop')}>
                        <Square size={14} style={{ color: '#ef4444' }} />
                    </button>
                    <button className="btn-icon" title="Run Once" onClick={() => handleRun(row)}>
                        <Zap size={14} style={{ color: '#f59e0b' }} />
                    </button>
                    <button className="btn-icon" title="Edit" onClick={() => handleEditClick(row)}>
                        <Edit size={14} />
                    </button>
                    <button className="btn-icon btn-icon-danger" title="Delete" onClick={() => handleDeleteClick(row)}>
                        <Trash2 size={14} />
                    </button>
                </div>
            )
        }
    ];

    const actions = [
        { label: 'Edit', icon: Edit, onClick: handleEditClick },
        { label: 'Delete', icon: Trash2, danger: true, onClick: handleDeleteClick }
    ];

    const toolbarActions = [
        {
            label: 'Add Job',
            icon: Plus,
            primary: true,
            onClick: handleAddClick
        },
        {
            label: 'Refresh',
            icon: RefreshCw,
            onClick: fetchJobs
        }
    ];

    return (
        <div className="page-container">
            <div className="page-header">
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <div style={{
                        width: '32px',
                        height: '32px',
                        borderRadius: '8px',
                        background: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <Clock size={16} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '16px', fontWeight: 700, margin: 0 }}>VantageJob Management</h2>
                        <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '2px 0 0' }}>
                            Distributed task scheduling and monitoring
                        </p>
                    </div>
                </div>
                <div style={{ display: 'flex', gap: '8px' }}>
                    {toolbarActions.map((action, idx) => (
                        <button 
                            key={idx} 
                            className={`btn ${action.primary ? 'btn-primary' : 'btn-secondary'}`}
                            style={{ display: 'flex', alignItems: 'center', gap: '6px', padding: '10px 16px' }}
                            onClick={action.onClick}
                        >
                            <action.icon size={16} /> {action.label}
                        </button>
                    ))}
                </div>
            </div>

            <DataGrid
                data={jobs}
                columns={columns}
                actions={actions}
                loading={loading}
                searchable={true}
                sortable={true}
                filterable={true}
                selectable={true}
                pagination={true}
                pageSize={15}
                emptyMessage="No jobs found. Click 'Add Job' to create one."
            />

            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title={`${modalMode === 'add' ? 'Add' : 'Edit'} VantageJob`}
                size="lg"
            >
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px' }}>
                    <FormInput
                        label="Job Description"
                        name="jobDesc"
                        value={formData.jobDesc}
                        onChange={(e) => setFormData({ ...formData, jobDesc: e.target.value })}
                        placeholder="Enter job description"
                        required
                    />
                    <FormInput
                        label="Cron Expression"
                        name="jobCron"
                        value={formData.jobCron}
                        onChange={(e) => setFormData({ ...formData, jobCron: e.target.value })}
                        placeholder="0 0/1 * * * ?"
                        required
                    />
                    <FormInput
                        label="Executor Handler"
                        name="executorHandler"
                        value={formData.executorHandler}
                        onChange={(e) => setFormData({ ...formData, executorHandler: e.target.value })}
                        placeholder="demoJobHandler"
                        required
                    />
                    <FormInput
                        label="Author"
                        name="author"
                        value={formData.author}
                        onChange={(e) => setFormData({ ...formData, author: e.target.value })}
                        placeholder="Enter author name"
                    />
                    <FormInput
                        label="Alarm Email"
                        name="alarmEmail"
                        value={formData.alarmEmail}
                        onChange={(e) => setFormData({ ...formData, alarmEmail: e.target.value })}
                        placeholder="admin@example.com"
                    />
                    <FormInput
                        label="Executor Parameters"
                        name="executorParam"
                        value={formData.executorParam}
                        onChange={(e) => setFormData({ ...formData, executorParam: e.target.value })}
                        placeholder="Optional parameters"
                    />
                    <FormInput
                        label="Route Strategy"
                        name="executorRouteStrategy"
                        type="select"
                        value={formData.executorRouteStrategy}
                        onChange={(e) => setFormData({ ...formData, executorRouteStrategy: e.target.value })}
                        options={[
                            { value: 'FIRST', label: 'First' },
                            { value: 'LAST', label: 'Last' },
                            { value: 'ROUND', label: 'Round Robin' },
                            { value: 'RANDOM', label: 'Random' },
                            { value: 'CONSISTENT_HASH', label: 'Consistent Hash' },
                            { value: 'LEAST_FREQUENTLY_USED', label: 'LFU' },
                            { value: 'LEAST_RECENTLY_USED', label: 'LRU' },
                            { value: 'FAILOVER', label: 'Failover' },
                            { value: 'BUSYOVER', label: 'Busyover' }
                        ]}
                    />
                    <FormInput
                        label="Block Strategy"
                        name="executorBlockStrategy"
                        type="select"
                        value={formData.executorBlockStrategy}
                        onChange={(e) => setFormData({ ...formData, executorBlockStrategy: e.target.value })}
                        options={[
                            { value: 'SERIAL_EXECUTION', label: 'Serial' },
                            { value: 'DISCARD_LATER', label: 'Discard Later' },
                            { value: 'COVER_EARLY', label: 'Cover Early' }
                        ]}
                    />
                    <FormInput
                        label="Timeout (seconds)"
                        name="executorTimeout"
                        type="number"
                        value={formData.executorTimeout}
                        onChange={(e) => setFormData({ ...formData, executorTimeout: parseInt(e.target.value) })}
                        placeholder="0"
                    />
                    <FormInput
                        label="Retry Count"
                        name="executorFailRetryCount"
                        type="number"
                        value={formData.executorFailRetryCount}
                        onChange={(e) => setFormData({ ...formData, executorFailRetryCount: parseInt(e.target.value) })}
                        placeholder="0"
                    />
                </div>

                <div style={{ display: 'flex', gap: '12px', marginTop: '24px', justifyContent: 'flex-end' }}>
                    <button className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
                        Cancel
                    </button>
                    <button 
                        className="btn btn-primary" 
                        onClick={handleSubmit}
                        disabled={submitting}
                    >
                        {submitting ? 'Saving...' : (modalMode === 'add' ? 'Add Job' : 'Save Changes')}
                    </button>
                </div>
            </Modal>
        </div>
    );
};

export default VantageJobList;
