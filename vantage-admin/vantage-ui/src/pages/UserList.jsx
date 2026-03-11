import { useState, useEffect } from 'react';
import { User, Mail, Phone, ShieldCheck, Clock, Plus, Edit, Trash2, Eye, RefreshCw } from 'lucide-react';
import DataGrid from '../components/DataGrid';

const formatDate = (dateValue) => {
    if (!dateValue) return 'N/A';
    try {
        return new Date(dateValue).toLocaleString();
    } catch {
        return 'N/A';
    }
};

const UserList = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch('/api/system/user/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setUsers(data.data || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch users:", err);
                setLoading(false);
            });
    }, []);

    const columns = [
        {
            key: 'userId',
            header: 'User ID',
            sortable: true
        },
        {
            key: 'loginName',
            header: 'Login Name',
            sortable: true,
            render: (value) => (
                <span className="badge-outline" style={{ fontWeight: 600 }}>
                    {value}
                </span>
            )
        },
        {
            key: 'userName',
            header: 'User Name',
            sortable: true
        },
        {
            key: 'email',
            header: 'Email',
            sortable: true,
            render: (value) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Mail size={14} style={{ opacity: 0.5 }} />
                    {value}
                </div>
            )
        },
        {
            key: 'phonenumber',
            header: 'Phone',
            sortable: true,
            render: (value) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Phone size={14} style={{ opacity: 0.5 }} />
                    {value || '-'}
                </div>
            )
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
        },
        {
            key: 'createTime',
            header: 'Create Time',
            sortable: true,
            render: (value) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Clock size={14} style={{ opacity: 0.5 }} />
                    {formatDate(value)}
                </div>
            )
        }
    ];

    const actions = [
        {
            label: 'View',
            icon: Eye,
            onClick: (row) => console.log('View:', row)
        },
        {
            label: 'Edit',
            icon: Edit,
            onClick: (row) => console.log('Edit:', row)
        },
        {
            label: 'Delete',
            icon: Trash2,
            danger: true,
            onClick: (row) => console.log('Delete:', row)
        }
    ];

    const toolbarActions = [
        {
            label: 'Refresh',
            icon: RefreshCw,
            onClick: () => {
                setLoading(true);
                setTimeout(() => setLoading(false), 500);
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
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <User size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>User Management</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Manage system users and their permissions
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
                    Add User
                </button>
            </div>

            <DataGrid
                data={users}
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
                emptyMessage="No users found. Add your first user to get started."
                onSelectionChange={(selected) => console.log('Selected:', selected)}
                onRowClick={(row) => console.log('Row clicked:', row)}
            />
        </div>
    );
};

export default UserList;
