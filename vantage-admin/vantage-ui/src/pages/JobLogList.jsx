import { useState, useEffect } from 'react';
import { FileText, Trash2, RefreshCw, CheckCircle, XCircle, Clock } from 'lucide-react';
import DataGrid from '../components/DataGrid';

const JobLogList = () => {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch('/api/system/job-log/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setLogs(data.data || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch job logs:", err);
                setLoading(false);
            });
    }, []);

    const columns = [
        { key: 'jobLogId', header: 'ID', sortable: true, align: 'center' },
        {
            key: 'jobName',
            header: 'Job Name',
            sortable: true,
            render: (value, row) => (
                <div>
                    <div style={{ fontWeight: 600 }}>{value}</div>
                    <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{row.jobGroup}</div>
                </div>
            )
        },
        {
            key: 'jobMessage',
            header: 'Message',
            sortable: false,
            render: (value) => <span style={{ color: 'var(--text-secondary)', fontSize: '12px' }}>{value}</span>
        },
        {
            key: 'status',
            header: 'Status',
            sortable: true,
            render: (value) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    {value === '0' ? <CheckCircle size={16} className="text-success" /> : <XCircle size={16} className="text-danger" />}
                    <span style={{ fontWeight: 500 }}>{value === '0' ? 'Success' : 'Failed'}</span>
                </div>
            )
        },
        {
            key: 'startTime',
            header: 'Start Time',
            sortable: true,
            render: (value) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Clock size={14} style={{ opacity: 0.5 }} />
                    <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>{value ? new Date(value).toLocaleString() : '-'}</span>
                </div>
            )
        }
    ];

    const actions = [
        { label: 'View', icon: FileText, onClick: (row) => console.log('View:', row) },
        { label: 'Delete', icon: Trash2, danger: true, onClick: (row) => console.log('Delete:', row) }
    ];

    const toolbarActions = [
        { label: 'Clean', icon: Trash2, onClick: () => { if(confirm('Clear all job logs?')) console.log('Clean all'); } },
        { label: 'Refresh', icon: RefreshCw, onClick: () => { setLoading(true); setTimeout(() => setLoading(false), 500); } }
    ];

    return (
        <div className="page-container">
            <div className="page-header">
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{
                        width: '40px',
                        height: '40px',
                        borderRadius: '10px',
                        background: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: '#333'
                    }}>
                        <FileText size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Job Log</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            View execution history of scheduled jobs
                        </p>
                    </div>
                </div>
                <div style={{ display: 'flex', gap: '8px' }}>
                    {toolbarActions.map((action, idx) => (
                        <button key={idx} className="btn btn-secondary" style={{
                            display: 'flex', alignItems: 'center', gap: '6px', padding: '10px 16px'
                        }} onClick={action.onClick}>
                            <action.icon size={16} /> {action.label}
                        </button>
                    ))}
                </div>
            </div>

            <DataGrid
                data={logs}
                columns={columns}
                actions={actions}
                loading={loading}
                searchable={true}
                sortable={true}
                filterable={true}
                selectable={true}
                pagination={true}
                pageSize={15}
                emptyMessage="No job execution logs found."
            />
        </div>
    );
};

export default JobLogList;
