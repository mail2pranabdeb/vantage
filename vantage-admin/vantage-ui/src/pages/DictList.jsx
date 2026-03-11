import { useState, useEffect } from 'react';
import { Database, Plus, Edit, Trash2, Eye } from 'lucide-react';
import DataGrid from '../components/DataGrid';

const DictList = () => {
    const [dicts, setDicts] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch('/api/system/dict/type/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setDicts(data.data || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch dicts:", err);
                setLoading(false);
            });
    }, []);

    const columns = [
        { key: 'dictId', header: 'ID', sortable: true, align: 'center' },
        {
            key: 'dictName',
            header: 'Dict Name',
            sortable: true,
            render: (value, row) => (
                <div>
                    <div style={{ fontWeight: 600 }}>{value}</div>
                    <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{row.dictType}</div>
                </div>
            )
        },
        {
            key: 'dictType',
            header: 'Dict Type',
            sortable: true,
            render: (value) => (
                <span className="badge-outline" style={{ fontSize: '11px', fontFamily: 'monospace' }}>{value}</span>
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
            key: 'remark',
            header: 'Remark',
            sortable: false,
            render: (value) => <span style={{ color: 'var(--text-muted)' }}>{value || '-'}</span>
        },
        {
            key: 'createTime',
            header: 'Create Time',
            sortable: true,
            render: (value) => <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>{value ? new Date(value).toLocaleDateString() : '-'}</span>
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
                        background: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: '#333'
                    }}>
                        <Database size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Dict Management</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Manage system dictionaries and lookup values
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
                    Add Dict
                </button>
            </div>

            <DataGrid
                data={dicts}
                columns={columns}
                actions={actions}
                loading={loading}
                searchable={true}
                sortable={true}
                filterable={true}
                selectable={true}
                pagination={true}
                pageSize={10}
                emptyMessage="No dictionaries found."
            />
        </div>
    );
};

export default DictList;
