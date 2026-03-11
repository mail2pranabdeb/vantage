import { useState, useEffect } from 'react';
import { Bell, Plus, Edit, Trash2, Eye, RefreshCw } from 'lucide-react';
import DataGrid from '../components/DataGrid';

const NoticeList = () => {
    const [notices, setNotices] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch('/api/system/notice/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setNotices(data.data || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch notices:", err);
                setLoading(false);
            });
    }, []);

    const columns = [
        { key: 'noticeId', header: 'ID', sortable: true, align: 'center' },
        {
            key: 'noticeTitle',
            header: 'Title',
            sortable: true,
            render: (value, row) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Bell size={16} style={{ opacity: 0.5 }} />
                    <span style={{ fontWeight: 600 }}>{value}</span>
                </div>
            )
        },
        {
            key: 'noticeType',
            header: 'Type',
            sortable: true,
            render: (value) => {
                const types = { '1': 'Notification', '2': 'Announcement' };
                const colors = { '1': '#3b82f6', '2': '#f59e0b' };
                return (
                    <span style={{
                        padding: '4px 10px',
                        borderRadius: '12px',
                        fontSize: '11px',
                        fontWeight: 600,
                        background: `${colors[value]}20`,
                        color: colors[value]
                    }}>
                        {types[value] || value}
                    </span>
                );
            }
        },
        {
            key: 'status',
            header: 'Status',
            sortable: true,
            render: (value) => (
                <span className={`status-pill ${value === '0' ? 'active' : 'inactive'}`}>
                    {value === '0' ? 'Normal' : 'Disabled'}
                </span>
            )
        }
    ];

    const actions = [
        { label: 'View', icon: Eye, onClick: (row) => console.log('View:', row) },
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
                        background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <Bell size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Notice Management</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Manage system notices and announcements
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
                    Add Notice
                </button>
            </div>

            <DataGrid
                data={notices}
                columns={columns}
                actions={actions}
                loading={loading}
                searchable={true}
                sortable={true}
                filterable={true}
                selectable={true}
                pagination={true}
                pageSize={10}
                emptyMessage="No notices found."
            />
        </div>
    );
};

export default NoticeList;
