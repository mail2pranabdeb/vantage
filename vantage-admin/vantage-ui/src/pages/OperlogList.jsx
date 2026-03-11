import { useState, useEffect } from 'react';
import { FileText, Trash2, RefreshCw, CheckCircle, XCircle, Activity } from 'lucide-react';
import DataGrid from '../components/DataGrid';

const OperlogList = () => {
    const [list, setList] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch('/api/system/operlog/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setList(data.data || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch operation logs:", err);
                setLoading(false);
            });
    }, []);

    const columns = [
        { key: 'operId', header: 'ID', sortable: true, align: 'center' },
        {
            key: 'title',
            header: 'Operation',
            sortable: true,
            render: (value, row) => (
                <div>
                    <div style={{ fontWeight: 600 }}>{value}</div>
                    <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{row.method || '-'}</div>
                </div>
            )
        },
        {
            key: 'businessType',
            header: 'Type',
            sortable: true,
            align: 'center',
            render: (value) => {
                const types = ['Other', 'Insert', 'Update', 'Delete', 'Grant', 'Export', 'Import', 'Force', 'Gen', 'Clean'];
                return (
                    <span style={{
                        padding: '4px 10px',
                        borderRadius: '12px',
                        fontSize: '11px',
                        fontWeight: 600,
                        background: '#3b82f620',
                        color: '#3b82f6'
                    }}>
                        {types[value] || 'Other'}
                    </span>
                );
            }
        },
        {
            key: 'operName',
            header: 'Operator',
            sortable: true,
            render: (value) => <span style={{ fontWeight: 500 }}>{value || '-'}</span>
        },
        {
            key: 'operIp',
            header: 'IP',
            sortable: true,
            render: (value) => <span className="badge-outline" style={{ fontSize: '11px', fontFamily: 'monospace' }}>{value}</span>
        },
        {
            key: 'status',
            header: 'Status',
            sortable: true,
            render: (value) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    {value === 0 ? <CheckCircle size={16} className="text-success" /> : <XCircle size={16} className="text-danger" />}
                    <span style={{ fontWeight: 500 }}>{value === 0 ? 'Success' : 'Failed'}</span>
                </div>
            )
        },
        {
            key: 'operTime',
            header: 'Time',
            sortable: true,
            render: (value) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Activity size={14} style={{ opacity: 0.5 }} />
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
        { label: 'Clean', icon: Trash2, onClick: () => { if(confirm('Clear all operation logs?')) console.log('Clean all'); } },
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
                        background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <FileText size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Operation Log</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Track and audit system operations
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
                data={list}
                columns={columns}
                actions={actions}
                loading={loading}
                searchable={true}
                sortable={true}
                filterable={true}
                selectable={true}
                pagination={true}
                pageSize={15}
                emptyMessage="No operation logs found."
            />
        </div>
    );
};

export default OperlogList;
