import { useState, useEffect } from 'react';
import { Menu, Plus, Edit, Trash2, Eye, Folder } from 'lucide-react';
import DataGrid from '../components/DataGrid';

const MenuList = () => {
    const [menus, setMenus] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch('/api/system/menu/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setMenus(data.data || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch menus:", err);
                setLoading(false);
            });
    }, []);

    const columns = [
        {
            key: 'menuId',
            header: 'ID',
            sortable: true,
            align: 'center'
        },
        {
            key: 'menuName',
            header: 'Menu Name',
            sortable: true,
            render: (value, row) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span style={{
                        fontSize: '16px',
                        opacity: 0.7
                    }}>{row.icon || '📄'}</span>
                    <span style={{ fontWeight: 600 }}>{value}</span>
                </div>
            )
        },
        {
            key: 'menuType',
            header: 'Type',
            sortable: true,
            align: 'center',
            render: (value) => {
                const types = { 'M': 'Directory', 'C': 'Menu', 'F': 'Button' };
                const colors = { 'M': '#3b82f6', 'C': '#10b981', 'F': '#f59e0b' };
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
            key: 'url',
            header: 'URL',
            sortable: false,
            render: (value) => (
                <span style={{ fontSize: '12px', color: 'var(--text-muted)', fontFamily: 'monospace' }}>
                    {value || '-'}
                </span>
            )
        },
        {
            key: 'perms',
            header: 'Permission',
            sortable: true,
            render: (value) => (
                <span className="badge-outline" style={{ fontSize: '11px' }}>
                    {value || '-'}
                </span>
            )
        },
        {
            key: 'visible',
            header: 'Visible',
            sortable: true,
            align: 'center',
            render: (value) => (
                <span className={`status-pill ${value === '0' ? 'active' : 'inactive'}`}>
                    {value === '0' ? 'Yes' : 'No'}
                </span>
            )
        },
        {
            key: 'orderNum',
            header: 'Sort',
            sortable: true,
            align: 'center'
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
                        background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <Menu size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Menu Management</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Manage system menus and navigation
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
                    Add Menu
                </button>
            </div>

            <DataGrid
                data={menus}
                columns={columns}
                actions={actions}
                loading={loading}
                searchable={true}
                sortable={true}
                filterable={true}
                selectable={true}
                pagination={true}
                pageSize={15}
                emptyMessage="No menus found."
            />
        </div>
    );
};

export default MenuList;
