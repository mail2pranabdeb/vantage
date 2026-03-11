import { useState, useEffect } from 'react';
import { Clock, Plus, Play, Pause, RefreshCw, Trash2, Eye, Edit } from 'lucide-react';
import DataGrid from '../components/DataGrid';

const JobList = () => {
    const [jobs, setJobs] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
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
    }, []);

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
        { label: 'Run', icon: Play, onClick: (row) => console.log('Run:', row) },
        { label: 'Pause', icon: Pause, onClick: (row) => console.log('Pause:', row) },
        { label: 'Edit', icon: Edit, onClick: (row) => console.log('Edit:', row) },
        { label: 'Delete', icon: Trash2, danger: true, onClick: (row) => console.log('Delete:', row) }
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
                <button className="btn btn-primary" style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    padding: '10px 16px',
                    borderRadius: '8px',
                    fontWeight: 600
                }}>
                    <Plus size={18} />
                    Add Job
                </button>
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
                pageSize={10}
                emptyMessage="No scheduled jobs found."
            />
        </div>
    );
};

export default JobList;
