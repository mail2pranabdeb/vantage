import { useState, useEffect, useRef } from 'react';
import { 
    Clock, Plus, Play, Pause, RefreshCw, Trash2, Eye, Edit, 
    BarChart3, Calendar, FileText, Download, Upload, Settings,
    Bell, Link, Timer, Globe, Copy, Check, X
} from 'lucide-react';
import DataGrid from '../components/DataGrid';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';
import CronBuilder from '../components/CronBuilder';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useToast } from '../components/Toast';

const JobList = () => {
    const toast = useToast();
    const [jobs, setJobs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isLogsModalOpen, setIsLogsModalOpen] = useState(false);
    const [isTemplatesModalOpen, setIsTemplatesModalOpen] = useState(false);
    const [isMetricsModalOpen, setIsMetricsModalOpen] = useState(false);
    const [isChainModalOpen, setIsChainModalOpen] = useState(false);
    const [chainJobs, setChainJobs] = useState([]);
    const [allJobsMap, setAllJobsMap] = useState({});
    const [modalMode, setModalMode] = useState('add');
    const [currentJob, setCurrentJob] = useState(null);
    const [jobLogs, setJobLogs] = useState([]);
    const [jobTemplates, setJobTemplates] = useState([]);
    const [emailTemplates, setEmailTemplates] = useState([]);
    const [metrics, setMetrics] = useState(null);
    const [selectedJobs, setSelectedJobs] = useState([]);
    const [showCronBuilder, setShowCronBuilder] = useState(false);
    const [copiedId, setCopiedId] = useState(null);
    const [submitting, setSubmitting] = useState(false);
    const [activeReports, setActiveReports] = useState([]);
    const [emailGroups, setEmailGroups] = useState([]);
    const [formData, setFormData] = useState({
        jobName: '',
        jobGroup: '',
        invokeTarget: '',
        jobType: 'BEAN',
        reportId: '',
        reportEmailGroup: [],
        cronExpression: '',
        misfirePolicy: '3',
        concurrent: '1',
        status: '0',
        maxRetryCount: 0,
        retryInterval: 60,
        timeoutSeconds: 3600,
        notifyOnFailure: false,
        notificationEmails: '',
        emailTemplateId: '',
        webhookUrl: '',
        dependentJobIds: '',
        timeZone: 'UTC',
        allowHoliday: true,
        remark: ''
    });

    const wsClientRef = useRef(null);

    // WebSocket connection for real-time updates
    useEffect(() => {
        const client = new Client({
            webSocketFactory: () => new SockJS('/ws-job'),
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('WebSocket connected');
                client.subscribe('/topic/job/updates', (message) => {
                    const event = JSON.parse(message.body);
                    console.log('Job event:', event);
                    fetchJobs(); // Refresh jobs on any event
                });
            },
            onDisconnect: () => {
                console.log('WebSocket disconnected');
            }
        });
        client.activate();
        wsClientRef.current = client;

        return () => {
            if (wsClientRef.current) {
                wsClientRef.current.deactivate();
            }
        };
    }, []);

    useEffect(() => {
        fetchJobs();
        fetchJobTemplates();
        fetchEmailTemplates();
        fetchMetrics();
        
        // Fetch active reports for dropdown
        fetch('/api/system/report-designer/active-templates')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) setActiveReports(data.data || []);
            });

        // Fetch email templates for dropdown
        fetch('/api/system/email-template/active')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    const templates = data.data || [];
                    setEmailTemplates(templates);
                    // Auto-select default template for new jobs
                    const defaultTpl = templates.find(t => t.isDefault) || templates[0];
                    if (defaultTpl) {
                        setFormData(prev => ({ ...prev, emailTemplateId: String(defaultTpl.templateId) }));
                    }
                }
            })
            .catch(err => console.error("Failed to fetch email templates:", err));

        // Fetch email groups from dictionary
        fetch('/api/system/dict/data/type/sys_report_email_group')
            .then(res => res.json())
            .then(data => {
                console.log("Email groups response:", data);
                if (data.code === 200) {
                    setEmailGroups(data.data || []);
                    console.log("Email groups set:", data.data);
                }
            })
            .catch(err => console.error("Failed to fetch email groups:", err));
    }, []);

    const fetchJobTemplates = () => {
        fetch('/api/system/job-template/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setJobTemplates(data.data || []);
                }
            })
            .catch(err => console.error("Failed to fetch job templates:", err));
    };

    const fetchEmailTemplates = () => {
        fetch('/api/system/email-template/active')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setEmailTemplates(data.data || []);
                }
            })
            .catch(err => console.error("Failed to fetch email templates:", err));
    };

    const fetchJobs = () => {
        setLoading(true);
        fetch('/api/system/job/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    const jobs = data.data || [];
                    setJobs(jobs);
                    // Build a map for quick lookup
                    const map = {};
                    jobs.forEach(j => { map[j.jobId] = j; });
                    setAllJobsMap(map);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch jobs:", err);
                setLoading(false);
            });
    };

    const fetchMetrics = () => {
        fetch('/api/system/job-dashboard/metrics')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setMetrics(data.data);
                }
            })
            .catch(err => console.error("Failed to fetch metrics:", err));
    };

    const fetchJobLogs = (jobId) => {
        fetch(`/api/system/job-log/job/${jobId}`)
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setJobLogs(data.data || []);
                    setIsLogsModalOpen(true);
                }
            })
            .catch(err => console.error("Failed to fetch job logs:", err));
    };

    const handleAddClick = () => {
        setModalMode('add');
        setCurrentJob(null);
        // Auto-select default email template if available
        const defaultTemplateId = emailTemplates.length > 0 ? String(emailTemplates[0].templateId) : '';
        setFormData({
            jobName: '',
            jobGroup: '',
            invokeTarget: '',
            jobType: 'BEAN',
            reportId: '',
            reportEmailGroup: [],
            emailTemplateId: defaultTemplateId,
            cronExpression: '',
            misfirePolicy: '3',
            concurrent: '1',
            status: '0',
            maxRetryCount: 0,
            retryInterval: 60,
            timeoutSeconds: 3600,
            notifyOnFailure: false,
            notificationEmails: '',
            webhookUrl: '',
            dependentJobIds: '',
            timeZone: 'UTC',
            allowHoliday: true,
            remark: ''
        });
        setShowCronBuilder(false);
        setIsModalOpen(true);
    };

    const handleEditClick = (row) => {
        console.log("Edit job row data:", row);
        // Parse email group - split comma-separated string to array for multi-select
        let emailGroupArray = [];
        if (row.reportEmailGroup) {
            emailGroupArray = row.reportEmailGroup.split(',').map(s => s.trim()).filter(s => s);
        }
        setFormData({
            jobName: row.jobName || '',
            jobGroup: row.jobGroup || '',
            invokeTarget: row.invokeTarget || '',
            jobType: row.jobType || 'BEAN',
            reportId: row.reportId || '',
            reportEmailGroup: emailGroupArray,
            cronExpression: row.cronExpression || '',
            misfirePolicy: String(row.misfirePolicy || '3'),
            concurrent: String(row.concurrent || '1'),
            status: row.status || '0',
            maxRetryCount: row.maxRetryCount || 0,
            retryInterval: row.retryInterval || 60,
            timeoutSeconds: row.timeoutSeconds || 3600,
            notifyOnFailure: row.notifyOnFailure || false,
            notificationEmails: row.notificationEmails || '',
            webhookUrl: row.webhookUrl || '',
            emailTemplateId: row.emailTemplateId || '',
            dependentJobIds: row.dependentJobIds || '',
            timeZone: row.timeZone || 'UTC',
            allowHoliday: row.allowHoliday !== undefined ? row.allowHoliday : true,
            remark: row.remark || ''
        });
        setCurrentJob(row);
        setModalMode('edit');
        setShowCronBuilder(false);
        setIsModalOpen(true);
    };

    const handleViewClick = (row) => {
        setModalMode('view');
        setCurrentJob(row);
        // Parse email group - split comma-separated string to array for multi-select
        let emailGroupArray = [];
        if (row.reportEmailGroup) {
            emailGroupArray = row.reportEmailGroup.split(',').map(s => s.trim()).filter(s => s);
        }
        setFormData({
            jobName: row.jobName || '',
            jobGroup: row.jobGroup || '',
            invokeTarget: row.invokeTarget || '',
            jobType: row.jobType || 'BEAN',
            reportId: row.reportId || '',
            reportEmailGroup: emailGroupArray,
            cronExpression: row.cronExpression || '',
            misfirePolicy: String(row.misfirePolicy || '3'),
            concurrent: String(row.concurrent || '1'),
            status: row.status || '0',
            maxRetryCount: row.maxRetryCount || 0,
            retryInterval: row.retryInterval || 60,
            timeoutSeconds: row.timeoutSeconds || 3600,
            notifyOnFailure: row.notifyOnFailure || false,
            notificationEmails: row.notificationEmails || '',
            webhookUrl: row.webhookUrl || '',
            emailTemplateId: row.emailTemplateId || '',
            dependentJobIds: row.dependentJobIds || '',
            timeZone: row.timeZone || 'UTC',
            allowHoliday: row.allowHoliday !== undefined ? row.allowHoliday : true,
            remark: row.remark || ''
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
                    const jobLogId = data.data;
                    toast.success(`Job executed successfully. Run ID: ${jobLogId}`);
                } else {
                    toast.error(data.msg || 'Failed to run job');
                }
            })
            .catch(err => {
                console.error("Failed to run job:", err);
                toast.error('Failed to run job');
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

    const handleBulkAction = (action) => {
        if (selectedJobs.length === 0) {
            alert('Please select at least one job');
            return;
        }
        
        const ids = selectedJobs.map(j => j.jobId);
        const confirmMsg = `Are you sure you want to ${action} ${selectedJobs.length} job(s)?`;
        
        if (!window.confirm(confirmMsg)) return;

        const url = `/api/system/job/batch/${action}`;
        fetch(url, {
            method: action === 'run' ? 'POST' : 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ids)
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                alert(data.msg || `Bulk ${action} completed`);
                fetchJobs();
                setSelectedJobs([]);
            } else {
                alert(data.msg || `Bulk ${action} failed`);
            }
        })
        .catch(err => {
            console.error(`Bulk ${action} failed:`, err);
            alert(`Bulk ${action} failed`);
        });
    };

    const handleExport = () => {
        const ids = selectedJobs.length > 0 ? selectedJobs.map(j => j.jobId) : null;
        const url = ids ? `/api/system/job/export?ids=${ids.join(',')}` : '/api/system/job/export';
        
        fetch(url)
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    const blob = new Blob([JSON.stringify(data.data, null, 2)], { type: 'application/json' });
                    const url = URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = url;
                    a.download = `jobs-export-${new Date().toISOString().split('T')[0]}.json`;
                    a.click();
                    URL.revokeObjectURL(url);
                }
            })
            .catch(err => console.error("Export failed:", err));
    };

    const handleImport = (event) => {
        const file = event.target.files[0];
        if (!file) return;

        const reader = new FileReader();
        reader.onload = (e) => {
            try {
                const jobs = JSON.parse(e.target.result);
                fetch('/api/system/job/import', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(jobs)
                })
                .then(res => res.json())
                .then(data => {
                    if (data.code === 200) {
                        alert(data.msg || 'Import completed');
                        fetchJobs();
                    } else {
                        alert(data.msg || 'Import failed');
                    }
                });
            } catch (err) {
                alert('Invalid JSON file');
            }
        };
        reader.readAsText(file);
        event.target.value = '';
    };

    const handleTemplateSelect = (templateName) => {
        fetch(`/api/system/job-template/create/${templateName}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                alert('Job created from template');
                fetchJobs();
                setIsTemplatesModalOpen(false);
            } else {
                alert(data.msg || 'Failed to create job from template');
            }
        });
    };

    const handleViewChainClick = (row) => {
        fetch(`/api/system/job/${row.jobId}/chain`)
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setChainJobs(data.data || []);
                    setIsChainModalOpen(true);
                }
            })
            .catch(err => console.error("Failed to fetch job chain:", err));
    };

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSubmit = () => {
        setSubmitting(true);

        // Validate email template for REPORT type
        if (formData.jobType === 'REPORT' && !formData.emailTemplateId) {
            alert('Please select an Email Template for Report jobs');
            setSubmitting(false);
            return;
        }

        const url = modalMode === 'add' ? '/api/system/job' : '/api/system/job';
        const method = modalMode === 'add' ? 'POST' : 'PUT';
        const body = {
            ...formData,
            jobId: modalMode === 'edit' ? currentJob.jobId : null,
            // Convert array back to comma-separated string for backend
            reportEmailGroup: Array.isArray(formData.reportEmailGroup)
                ? formData.reportEmailGroup.join(',')
                : formData.reportEmailGroup,
            misfirePolicy: parseInt(formData.misfirePolicy),
            concurrent: formData.concurrent === '1' ? '1' : '0',
            maxRetryCount: parseInt(formData.maxRetryCount),
            retryInterval: parseInt(formData.retryInterval),
            timeoutSeconds: parseInt(formData.timeoutSeconds),
            emailTemplateId: formData.emailTemplateId ? parseInt(formData.emailTemplateId) : null
        };
        console.log("Submitting job data:", body);

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
                fetchJobs();
                fetchMetrics();
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

    const copyToClipboard = (text, id) => {
        navigator.clipboard.writeText(text);
        setCopiedId(id);
        setTimeout(() => setCopiedId(null), 2000);
    };

    const columns = [
        { 
            key: 'jobId', 
            header: 'ID', 
            sortable: true, 
            align: 'center',
            width: '60px'
        },
        {
            key: 'jobName',
            header: 'Job Name',
            sortable: true,
            render: (value, row) => (
                <div>
                    <div style={{ fontWeight: 600 }}>{value}</div>
                    <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{row.jobGroup}</div>
                    {row.templateName && (
                        <span style={{ fontSize: '10px', color: 'var(--primary)', background: 'rgba(99, 102, 241, 0.1)', padding: '2px 6px', borderRadius: '4px', marginTop: '2px', display: 'inline-block' }}>
                            {row.templateName}
                        </span>
                    )}
                </div>
            )
        },
        {
            key: 'status',
            header: 'Status',
            sortable: true,
            align: 'center',
            render: (value) => (
                <span className={`status-pill ${value === '0' ? 'active' : 'inactive'}`} style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '6px',
                    padding: '4px 12px',
                    borderRadius: '12px',
                    fontSize: '11px',
                    fontWeight: 600
                }}>
                    <span style={{
                        width: '8px',
                        height: '8px',
                        borderRadius: '50%',
                        background: value === '0' ? '#11998e' : '#6b7280',
                        display: 'inline-block',
                        boxShadow: value === '0' ? '0 0 6px #11998e' : 'none',
                        animation: value === '0' ? 'pulse 2s infinite' : 'none'
                    }} />
                    {value === '0' ? 'Active' : 'Paused'}
                </span>
            )
        },
        {
            key: 'actions',
            header: 'Retry/Timeout',
            sortable: false,
            align: 'center',
            render: (_, row) => (
                <div style={{ display: 'flex', gap: '4px', justifyContent: 'center', fontSize: '10px' }}>
                    <span style={{ padding: '2px 6px', background: 'var(--bg-tertiary)', borderRadius: '4px' }}>
                        Retry: {row.maxRetryCount || 0}
                    </span>
                    <span style={{ padding: '2px 6px', background: 'var(--bg-tertiary)', borderRadius: '4px' }}>
                        {(row.timeoutSeconds || 3600) / 60}m
                    </span>
                </div>
            )
        }
    ];

    const actions = [
        { label: 'Run', icon: Play, onClick: handleRunClick },
        { label: 'Pause', icon: Pause, onClick: handlePauseClick },
        { label: 'Chain', icon: Link, onClick: handleViewChainClick },
        { label: 'Logs', icon: FileText, onClick: (row) => fetchJobLogs(row.jobId) },
        { label: 'Edit', icon: Edit, onClick: handleEditClick },
        { label: 'Delete', icon: Trash2, danger: true, onClick: handleDeleteClick }
    ];

    const toolbarActions = [
        { label: 'Refresh', icon: RefreshCw, onClick: fetchJobs },
        { label: 'Templates', icon: FileText, onClick: () => setIsTemplatesModalOpen(true) },
        { label: 'Metrics', icon: BarChart3, onClick: () => setIsMetricsModalOpen(true) },
        { label: 'Export', icon: Download, onClick: handleExport },
        { 
            label: 'Import', 
            icon: Upload, 
            onClick: () => document.getElementById('import-file').click() 
        }
    ];

    const bulkActions = [
        { label: 'Run Selected', icon: Play, onClick: () => handleBulkAction('run') },
        { label: 'Pause Selected', icon: Pause, onClick: () => handleBulkAction('pause') },
        { label: 'Resume Selected', icon: Play, onClick: () => handleBulkAction('resume') },
        { label: 'Delete Selected', icon: Trash2, onClick: () => handleBulkAction('delete') }
    ];

    return (
        <div className="page-container">
            <input 
                type="file" 
                id="import-file" 
                accept=".json" 
                style={{ display: 'none' }} 
                onChange={handleImport} 
            />

            {/* Metrics Summary */}
            {metrics && (
                <div style={{ 
                    display: 'grid', 
                    gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', 
                    gap: '12px', 
                    marginBottom: '20px' 
                }}>
                    <div style={{ padding: '16px', background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', borderRadius: '12px', color: 'white' }}>
                        <div style={{ fontSize: '11px', opacity: 0.9 }}>Total Jobs</div>
                        <div style={{ fontSize: '28px', fontWeight: 700 }}>{metrics.totalJobs}</div>
                    </div>
                    <div style={{ padding: '16px', background: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)', borderRadius: '12px', color: 'white' }}>
                        <div style={{ fontSize: '11px', opacity: 0.9 }}>Active</div>
                        <div style={{ fontSize: '28px', fontWeight: 700 }}>{metrics.activeJobs}</div>
                    </div>
                    <div style={{ padding: '16px', background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', borderRadius: '12px', color: 'white' }}>
                        <div style={{ fontSize: '11px', opacity: 0.9 }}>Paused</div>
                        <div style={{ fontSize: '28px', fontWeight: 700 }}>{metrics.pausedJobs}</div>
                    </div>
                    <div style={{ padding: '16px', background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', borderRadius: '12px', color: 'white' }}>
                        <div style={{ fontSize: '11px', opacity: 0.9 }}>Success Rate</div>
                        <div style={{ fontSize: '28px', fontWeight: 700 }}>{metrics.successRate?.toFixed(1)}%</div>
                    </div>
                </div>
            )}

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
                            Schedule and manage background tasks with advanced features
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
                bulkActions={bulkActions}
                loading={loading}
                searchable={true}
                sortable={true}
                filterable={true}
                selectable={true}
                pagination={true}
                pageSize={10}
                emptyMessage="No scheduled jobs found."
                onSelectionChange={setSelectedJobs}
            />

            {/* Add/Edit/View Modal */}
            <Modal
                key={currentJob?.jobId || 'new'}
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title={modalMode === 'add' ? 'Add Job' : modalMode === 'edit' ? 'Edit Job' : 'View Job'}
                size="large"
                compact={false}
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
                            {modalMode === 'add' ? 'Create' : 'Save'}
                        </button>
                    </>
                )}
            >
                <div style={{ maxHeight: '70vh', overflowY: 'auto', paddingRight: '8px' }}>
                    {/* Basic Settings */}
                    <div style={{ marginBottom: '20px' }}>
                        <h4 style={{ margin: '0 0 12px', fontSize: '14px', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <Settings size={16} /> Basic Settings
                        </h4>
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
                            <label className="form-label">Job Type</label>
                            <select 
                                name="jobType" 
                                value={formData.jobType} 
                                onChange={handleInputChange} 
                                className="form-input"
                                disabled={modalMode === 'view'}
                            >
                                <option value="BEAN">Standard (Invoke Target)</option>
                                <option value="REPORT">Report Execution</option>
                            </select>
                        </div>

                        {formData.jobType === 'REPORT' ? (
                            <>
                            <div className="form-group">
                                <label className="form-label">Select Active Report</label>
                                <select
                                    name="reportId"
                                    value={formData.reportId}
                                    onChange={handleInputChange}
                                    className="form-input"
                                    required
                                    disabled={modalMode === 'view'}
                                >
                                    <option value="">-- Select a Report --</option>
                                    {activeReports.map(r => (
                                        <option key={r.templateId} value={r.templateId}>
                                            {r.templateName} (v{r.version})
                                        </option>
                                    ))}
                                </select>
                                <small className="form-help">Only activated reports appear here</small>
                            </div>

                            <div className="form-group">
                                <label className="form-label">
                                    Email Template <span style={{ color: 'var(--danger)' }}>*</span>
                                </label>
                                <select
                                    name="emailTemplateId"
                                    value={formData.emailTemplateId}
                                    onChange={handleInputChange}
                                    className="form-input"
                                    required
                                    disabled={modalMode === 'view'}
                                >
                                    <option value="">-- Select Email Template --</option>
                                    {emailTemplates.map(t => (
                                        <option key={t.templateId} value={t.templateId}>
                                            {t.templateName} {t.isDefault ? '(Default)' : ''}
                                        </option>
                                    ))}
                                </select>
                                <small className="form-help">
                                    {emailTemplates.length === 0 ? (
                                        <span style={{ color: 'var(--danger)' }}>No templates found. </span>
                                    ) : ''}
                                    <a href="#/system/email-templates" onClick={(e) => {
                                        e.preventDefault();
                                        window.location.href = '/#/system/email-templates';
                                    }}>Manage Templates</a>
                                </small>
                            </div>
                            </>
                        ) : (
                            <div className="form-group">
                                <label className="form-label">Invoke Target</label>
                                <FormInput
                                    name="invokeTarget"
                                    value={formData.invokeTarget}
                                    onChange={handleInputChange}
                                    placeholder="e.g., beanName.method('args')"
                                    required={formData.jobType === 'BEAN'}
                                    disabled={modalMode === 'view'}
                                />
                                <small className="form-help">Method to invoke for this scheduled job</small>
                            </div>
                        )}

                        {formData.jobType === 'REPORT' && (
                            <div className="form-row">
                                <div className="form-group">
                                    <label className="form-label">Email Group (Optional)</label>
                                    <select
                                        name="reportEmailGroup"
                                        value={formData.reportEmailGroup}
                                        onChange={(e) => {
                                            const selected = Array.from(e.target.selectedOptions, option => option.value);
                                            setFormData(prev => ({ ...prev, reportEmailGroup: selected }));
                                        }}
                                        className="form-input"
                                        multiple
                                        disabled={modalMode === 'view'}
                                        style={{ minHeight: '80px' }}
                                    >
                                        {emailGroups.map(g => (
                                            <option key={g.dictCode} value={g.dictCode}>
                                                {g.dictLabel}
                                            </option>
                                        ))}
                                    </select>
                                    <small className="form-help">Hold Ctrl/Cmd to select multiple groups</small>
                                </div>

                                <div className="form-group">
                                    <label className="form-label">Additional Recipients</label>
                                    <FormInput
                                        name="notificationEmails"
                                        value={formData.notificationEmails}
                                        onChange={handleInputChange}
                                        placeholder="user@test.com,manager@test.com"
                                        disabled={modalMode === 'view'}
                                    />
                                </div>
                            </div>
                        )}

                        <div className="form-group">
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                                <label className="form-label" style={{ margin: 0 }}>Cron Expression</label>
                                {modalMode !== 'view' && (
                                    <button
                                        type="button"
                                        onClick={() => setShowCronBuilder(!showCronBuilder)}
                                        style={{ fontSize: '11px', color: 'var(--primary)', background: 'none', border: 'none', cursor: 'pointer' }}
                                    >
                                        {showCronBuilder ? 'Hide Builder' : 'Open Builder'}
                                    </button>
                                )}
                            </div>
                            <FormInput
                                name="cronExpression"
                                value={formData.cronExpression}
                                onChange={handleInputChange}
                                placeholder="e.g., 0/5 * * * * ?"
                                required
                                disabled={modalMode === 'view'}
                            />
                            {showCronBuilder && modalMode !== 'view' && (
                                <div style={{ marginTop: '12px' }}>
                                    <CronBuilder
                                        value={formData.cronExpression}
                                        onChange={(cron) => setFormData(prev => ({ ...prev, cronExpression: cron }))}
                                        disabled={modalMode === 'view'}
                                    />
                                </div>
                            )}
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
                            <div className="form-group">
                                <label className="form-label">Status</label>
                                <select
                                    name="status"
                                    value={formData.status}
                                    onChange={handleInputChange}
                                    className="form-input"
                                    disabled={modalMode === 'view'}
                                >
                                    <option value="0">Active</option>
                                    <option value="1">Paused</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    {/* Advanced Settings */}
                    <div style={{ marginBottom: '20px', padding: '16px', background: 'var(--bg-secondary)', borderRadius: '8px' }}>
                        <h4 style={{ margin: '0 0 12px', fontSize: '14px', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <Timer size={16} /> Retry & Timeout
                        </h4>
                        <div className="form-row">
                            <FormInput
                                label="Max Retry Count"
                                name="maxRetryCount"
                                type="number"
                                value={formData.maxRetryCount}
                                onChange={handleInputChange}
                                placeholder="0"
                                disabled={modalMode === 'view'}
                            />
                            <FormInput
                                label="Retry Interval (seconds)"
                                name="retryInterval"
                                type="number"
                                value={formData.retryInterval}
                                onChange={handleInputChange}
                                placeholder="60"
                                disabled={modalMode === 'view'}
                            />
                            <FormInput
                                label="Timeout (seconds)"
                                name="timeoutSeconds"
                                type="number"
                                value={formData.timeoutSeconds}
                                onChange={handleInputChange}
                                placeholder="3600"
                                disabled={modalMode === 'view'}
                            />
                        </div>
                    </div>

                    {/* Notification Settings */}
                    <div style={{ marginBottom: '20px', padding: '16px', background: 'var(--bg-secondary)', borderRadius: '8px' }}>
                        <h4 style={{ margin: '0 0 12px', fontSize: '14px', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <Bell size={16} /> Notifications
                        </h4>
                        <div className="form-group">
                            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                                <input
                                    type="checkbox"
                                    name="notifyOnFailure"
                                    checked={formData.notifyOnFailure}
                                    onChange={handleInputChange}
                                    disabled={modalMode === 'view'}
                                    style={{ width: '16px', height: '16px' }}
                                />
                                <span style={{ fontSize: '13px' }}>Enable failure notifications</span>
                            </label>
                        </div>
                        {formData.notifyOnFailure && (
                            <>
                                <div className="form-row">
                                    <FormInput
                                        label="Notification Emails (comma-separated)"
                                        name="notificationEmails"
                                        value={formData.notificationEmails}
                                        onChange={handleInputChange}
                                        placeholder="admin@example.com, user@example.com"
                                        disabled={modalMode === 'view'}
                                    />
                                </div>
                                <FormInput
                                    label="Webhook URL"
                                    name="webhookUrl"
                                    value={formData.webhookUrl}
                                    onChange={handleInputChange}
                                    placeholder="https://hooks.slack.com/..."
                                    disabled={modalMode === 'view'}
                                />
                            </>
                        )}
                    </div>

                    {/* Dependencies */}
                    <div style={{ marginBottom: '20px', padding: '16px', background: 'var(--bg-secondary)', borderRadius: '8px' }}>
                        <h4 style={{ margin: '0 0 12px', fontSize: '14px', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <Link size={16} /> Job Dependencies
                        </h4>
                        <div className="form-group">
                            <label className="form-label">Dependent Jobs (comma-separated IDs)</label>
                            <select
                                name="dependentJobIds"
                                value={formData.dependentJobIds ? formData.dependentJobIds.split(',').map(s => s.trim()).filter(s => s) : []}
                                onChange={(e) => {
                                    const selected = Array.from(e.target.selectedOptions, option => option.value);
                                    setFormData(prev => ({ ...prev, dependentJobIds: selected.join(',') }));
                                }}
                                className="form-input"
                                multiple
                                disabled={modalMode === 'view'}
                                style={{ minHeight: '80px' }}
                            >
                                {Object.values(allJobsMap).filter(j => j.jobId !== currentJob?.jobId).map(j => (
                                    <option key={j.jobId} value={String(j.jobId)}>
                                        {j.jobName} (ID: {j.jobId})
                                    </option>
                                ))}
                            </select>
                            <small className="form-help">Hold Ctrl/Cmd to select multiple. These jobs run after this job completes.</small>
                        </div>
                    </div>

                    {/* Additional Settings */}
                    <div className="form-row">
                        <FormInput
                            label="Time Zone"
                            name="timeZone"
                            value={formData.timeZone}
                            onChange={handleInputChange}
                            placeholder="UTC"
                            disabled={modalMode === 'view'}
                        />
                        <div className="form-group">
                            <label className="form-label">Allow Holiday Execution</label>
                            <select
                                name="allowHoliday"
                                value={formData.allowHoliday ? 'true' : 'false'}
                                onChange={(e) => setFormData(prev => ({ ...prev, allowHoliday: e.target.value === 'true' }))}
                                className="form-input"
                                disabled={modalMode === 'view'}
                            >
                                <option value="true">Yes</option>
                                <option value="false">No</option>
                            </select>
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="form-label">Remark</label>
                        <textarea
                            name="remark"
                            value={formData.remark}
                            onChange={handleInputChange}
                            placeholder="Optional notes about this job"
                            rows={3}
                            className="form-input"
                            disabled={modalMode === 'view'}
                            style={{ resize: 'vertical' }}
                        />
                    </div>
                </div>
            </Modal>

            {/* Job Logs Modal */}
            <Modal
                isOpen={isLogsModalOpen}
                onClose={() => setIsLogsModalOpen(false)}
                title="Job Execution Logs"
                size="large"
            >
                <div style={{ maxHeight: '70vh', overflowY: 'auto' }}>
                    {jobLogs.length === 0 ? (
                        <p style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '20px' }}>No execution logs found</p>
                    ) : (
                        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '12px' }}>
                            <thead>
                                <tr style={{ borderBottom: '2px solid var(--border-color)' }}>
                                    <th style={{ padding: '10px', textAlign: 'left' }}>Time</th>
                                    <th style={{ padding: '10px', textAlign: 'left' }}>Status</th>
                                    <th style={{ padding: '10px', textAlign: 'left' }}>Duration</th>
                                    <th style={{ padding: '10px', textAlign: 'left' }}>Retries</th>
                                    <th style={{ padding: '10px', textAlign: 'left' }}>Message</th>
                                </tr>
                            </thead>
                            <tbody>
                                {jobLogs.map((log, idx) => (
                                    <tr key={idx} style={{ borderBottom: '1px solid var(--border-color)' }}>
                                        <td style={{ padding: '10px' }}>{log.startTime?.replace('T', ' ')}</td>
                                        <td style={{ padding: '10px' }}>
                                            <span className={`status-pill ${log.status === '0' ? 'active' : 'inactive'}`}>
                                                {log.status === '0' ? 'Success' : 'Failed'}
                                            </span>
                                        </td>
                                        <td style={{ padding: '10px' }}>{log.executionDuration} ms</td>
                                        <td style={{ padding: '10px' }}>{log.retryCount || 0}</td>
                                        <td style={{ padding: '10px', maxWidth: '300px', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                            {log.jobMessage}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </Modal>

            {/* Templates Modal */}
            <Modal
                isOpen={isTemplatesModalOpen}
                onClose={() => setIsTemplatesModalOpen(false)}
                title="Job Templates"
                size="medium"
            >
                <div style={{ display: 'grid', gap: '12px' }}>
                    {jobTemplates.map((template, idx) => (
                        <div 
                            key={idx} 
                            style={{ 
                                padding: '16px', 
                                border: '1px solid var(--border-color)', 
                                borderRadius: '8px',
                                cursor: 'pointer',
                                transition: 'all 0.2s'
                            }}
                            onClick={() => handleTemplateSelect(template.name)}
                            onMouseEnter={(e) => {
                                e.currentTarget.style.borderColor = 'var(--primary)';
                                e.currentTarget.style.background = 'var(--bg-secondary)';
                            }}
                            onMouseLeave={(e) => {
                                e.currentTarget.style.borderColor = 'var(--border-color)';
                                e.currentTarget.style.background = 'transparent';
                            }}
                        >
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div>
                                    <h4 style={{ margin: '0 0 4px', fontSize: '14px' }}>{template.jobName}</h4>
                                    <p style={{ margin: 0, fontSize: '12px', color: 'var(--text-muted)' }}>{template.description}</p>
                                    <code style={{ fontSize: '11px', color: 'var(--primary)', fontFamily: 'monospace' }}>{template.cronExpression}</code>
                                </div>
                                <button className="btn btn-primary" style={{ padding: '6px 12px', fontSize: '12px' }}>
                                    Use Template
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            </Modal>

            {/* Metrics Modal */}
            <Modal
                isOpen={isMetricsModalOpen}
                onClose={() => setIsMetricsModalOpen(false)}
                title="Job Dashboard Metrics"
                size="medium"
            >
                {metrics ? (
                    <div style={{ display: 'grid', gap: '16px' }}>
                        <div style={{ padding: '16px', background: 'var(--bg-secondary)', borderRadius: '8px' }}>
                            <h4 style={{ margin: '0 0 12px', fontSize: '14px' }}>Execution Statistics (Last 30 Days)</h4>
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '12px' }}>
                                <div>
                                    <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Total Executions</div>
                                    <div style={{ fontSize: '24px', fontWeight: 700 }}>{metrics.totalExecutions}</div>
                                </div>
                                <div>
                                    <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Successful</div>
                                    <div style={{ fontSize: '24px', fontWeight: 700, color: 'var(--success)' }}>{metrics.successfulExecutions}</div>
                                </div>
                                <div>
                                    <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Failed</div>
                                    <div style={{ fontSize: '24px', fontWeight: 700, color: 'var(--danger)' }}>{metrics.failedExecutions}</div>
                                </div>
                                <div>
                                    <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Avg Duration</div>
                                    <div style={{ fontSize: '24px', fontWeight: 700 }}>{(metrics.avgExecutionDuration / 1000).toFixed(2)}s</div>
                                </div>
                            </div>
                        </div>
                        
                        {metrics.recentFailures && metrics.recentFailures.length > 0 && (
                            <div style={{ padding: '16px', background: 'var(--bg-secondary)', borderRadius: '8px' }}>
                                <h4 style={{ margin: '0 0 12px', fontSize: '14px' }}>Recent Failures</h4>
                                {metrics.recentFailures.map((failure, idx) => (
                                    <div key={idx} style={{ padding: '8px 0', borderBottom: idx < metrics.recentFailures.length - 1 ? '1px solid var(--border-color)' : 'none' }}>
                                        <div style={{ fontWeight: 600, fontSize: '13px' }}>{failure.jobName}</div>
                                        <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{failure.message}</div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                ) : (
                    <p style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '20px' }}>Loading metrics...</p>
                )}
            </Modal>

            {/* Job Chain Modal */}
            <Modal
                isOpen={isChainModalOpen}
                onClose={() => setIsChainModalOpen(false)}
                title="Job Dependency Chain"
                size="medium"
            >
                {chainJobs.length > 0 ? (
                    <div>
                        {chainJobs.map((job, idx) => (
                            <div key={idx} style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '8px' }}>
                                <div style={{
                                    width: '32px', height: '32px', borderRadius: '50%',
                                    background: idx === 0 ? 'var(--primary)' : 'var(--bg-tertiary)',
                                    color: idx === 0 ? 'white' : 'var(--text-primary)',
                                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                                    fontWeight: 600, fontSize: '12px', flexShrink: 0
                                }}>
                                    {idx + 1}
                                </div>
                                <div style={{ flex: 1 }}>
                                    <div style={{ fontWeight: 600, fontSize: '13px' }}>{job.jobName}</div>
                                    <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                                        ID: {job.jobId} | Group: {job.jobGroup} | Status: {job.status === '0' ? 'Active' : 'Paused'}
                                    </div>
                                </div>
                                {idx < chainJobs.length - 1 && (
                                    <div style={{ color: 'var(--primary)', fontSize: '18px' }}>→</div>
                                )}
                            </div>
                        ))}
                    </div>
                ) : (
                    <p style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '20px' }}>No dependencies configured.</p>
                )}
            </Modal>
        </div>
    );
};

export default JobList;
