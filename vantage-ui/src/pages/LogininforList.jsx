import { useState, useEffect } from 'react';
import { ShieldAlert, Trash2, RefreshCw, CheckCircle, XCircle, MapPin, Monitor } from 'lucide-react';
import DataGrid from '../components/DataGrid';
import { useToast } from '../components/Toast';

const LogininforList = () => {
    const { addToast } = useToast();
    const [list, setList] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        console.log('[LogininforList] Fetching login records...');
        fetch('/api/system/logininfor/list')
            .then(res => res.json())
            .then(data => {
                console.log('[LogininforList] API Response:', data);
                setLoading(false);
                if (data.code === 200) {
                    const dataList = data.data?.rows || data.data || [];
                    setList(dataList);
                    if (dataList.length === 0) {
                        console.log('[Toast] Showing info toast: No records');
                        addToast('info', 'No login records found. Login to see records here.', 4000);
                    } else {
                        console.log('[Toast] Showing success toast:', dataList.length, 'records');

                    }
                } else {
                    console.log('[Toast] Showing error toast:', data.msg);
                    addToast('error', data.msg || 'Failed to load login records', 5000);
                }
            })
            .catch(err => {
                console.error("[LogininforList] Fetch error:", err);
                setLoading(false);
                addToast('error', 'Network error. Please try again.', 5000);
            });
    }, []);

    const columns = [
        { key: 'infoId', header: 'ID', sortable: true, align: 'center' },
        {
            key: 'loginName',
            header: 'Login Name',
            sortable: true,
            render: (value) => <span style={{ fontWeight: 600 }}>{value}</span>
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
            key: 'ipaddr',
            header: 'IP Address',
            sortable: true,
            render: (value) => <span className="badge-outline" style={{ fontSize: '11px', fontFamily: 'monospace' }}>{value}</span>
        },
        {
            key: 'loginLocation',
            header: 'Location',
            sortable: true,
            render: (value) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <MapPin size={14} style={{ opacity: 0.5 }} />
                    {value || 'Unknown'}
                </div>
            )
        },
        {
            key: 'browser',
            header: 'Browser',
            sortable: true,
            render: (value) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Monitor size={14} style={{ opacity: 0.5 }} />
                    {value || '-'}
                </div>
            )
        },
        {
            key: 'loginTime',
            header: 'Login Time',
            sortable: true,
            render: (value) => <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>{value ? new Date(value).toLocaleString() : '-'}</span>
        }
    ];

    const actions = [
        {
            label: 'Delete',
            icon: Trash2,
            danger: true,
            onClick: (row) => {
                addToast('warning', `Delete record for ${row.loginName}?`, 3000);
            }
        }
    ];

    const toolbarActions = [
        {
            label: 'Clean',
            icon: Trash2,
            onClick: () => {
                if(confirm('Clear all login logs?')) {
                    addToast('info', 'Clean feature coming soon...', 3000);
                }
            }
        },
        {
            label: 'Refresh',
            icon: RefreshCw,
            onClick: () => {
                setLoading(true);
                fetch('/api/system/logininfor/list')
                    .then(res => res.json())
                    .then(data => {
                        if (data.code === 200) {
                            setList(data.data?.rows || data.data || []);
                            addToast('success', 'Refreshed successfully', 2000);
                        }
                        setLoading(false);
                    })
                    .catch(() => {
                        addToast('error', 'Failed to refresh', 3000);
                        setLoading(false);
                    });
            }
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
                        background: 'linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <ShieldAlert size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Login Info</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Monitor user login activities and access logs
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
                emptyMessage="No login records found."
            />
        </div>
    );
};

export default LogininforList;
