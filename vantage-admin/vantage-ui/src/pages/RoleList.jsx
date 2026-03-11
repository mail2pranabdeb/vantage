import { useState, useEffect } from 'react';
import { Shield, Plus, Edit, Trash2, Eye, RefreshCw } from 'lucide-react';
import DataGrid from '../components/DataGrid';

const RoleList = () => {
    const [roles, setRoles] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch('/api/system/role/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setRoles(data.data || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch roles:", err);
                setLoading(false);
            });
    }, []);

    const columns = [
        {
            key: 'roleId',
            header: 'Role ID',
            sortable: true
        },
        {
            key: 'roleName',
            header: 'Role Name',
            sortable: true,
            render: (value, row) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <div style={{
                        width: '32px',
                        height: '32px',
                        borderRadius: '8px',
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <Shield size={16} />
                    </div>
                    <div>
                        <div style={{ fontWeight: 600 }}>{value}</div>
                        <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{row.roleKey}</div>
                    </div>
                </div>
            )
        },
        {
            key: 'roleKey',
            header: 'Role Key',
            sortable: true
        },
        {
            key: 'roleSort',
            header: 'Sort',
            sortable: true,
            align: 'center'
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
                <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                    {value ? new Date(value).toLocaleDateString() : '-'}
                </span>
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
                        <Shield size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Role Management</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Manage user roles and permissions
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
                    Add Role
                </button>
            </div>

            <DataGrid
                data={roles}
                columns={columns}
                actions={actions}
                loading={loading}
                searchable={true}
                sortable={true}
                filterable={true}
                selectable={true}
                pagination={true}
                pageSize={10}
                emptyMessage="No roles found. Create your first role to get started."
            />
        </div>
    );
};

export default RoleList;
