import { useState, useEffect } from 'react';
import { Settings, Plus, Edit, Trash2, Eye, RefreshCw } from 'lucide-react';
import DataGrid from '../components/DataGrid';

const ConfigList = () => {
    const [configs, setConfigs] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch('/api/system/config/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setConfigs(data.data || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch configs:", err);
                setLoading(false);
            });
    }, []);

    const columns = [
        { key: 'configId', header: 'ID', sortable: true, align: 'center' },
        {
            key: 'configName',
            header: 'Name',
            sortable: true,
            render: (value, row) => (
                <div>
                    <div style={{ fontWeight: 600 }}>{value}</div>
                    <div style={{ fontSize: '11px', color: 'var(--text-muted)', fontFamily: 'monospace' }}>{row.configKey}</div>
                </div>
            )
        },
        {
            key: 'configValue',
            header: 'Value',
            sortable: true,
            render: (value) => (
                <span className="badge-outline" style={{ fontSize: '12px' }}>{value}</span>
            )
        },
        {
            key: 'configType',
            header: 'Type',
            sortable: true,
            align: 'center',
            render: (value) => (
                <span style={{
                    padding: '4px 10px',
                    borderRadius: '12px',
                    fontSize: '11px',
                    fontWeight: 600,
                    background: value === 'Y' ? '#10b98120' : '#3b82f620',
                    color: value === 'Y' ? '#10b981' : '#3b82f6'
                }}>
                    {value === 'Y' ? 'Yes' : 'No'}
                </span>
            )
        },
        {
            key: 'remark',
            header: 'Remark',
            sortable: false,
            render: (value) => <span style={{ color: 'var(--text-muted)' }}>{value || '-'}</span>
        }
    ];

    const actions = [
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
                        background: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <Settings size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Config Management</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Manage system configuration parameters
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
                    Add Config
                </button>
            </div>

            <DataGrid
                data={configs}
                columns={columns}
                actions={actions}
                loading={loading}
                searchable={true}
                sortable={true}
                filterable={true}
                selectable={true}
                pagination={true}
                pageSize={10}
                emptyMessage="No configurations found."
            />
        </div>
    );
};

export default ConfigList;
