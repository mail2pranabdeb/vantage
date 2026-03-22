import { useState, useEffect } from 'react';
import { Code, Download, RefreshCw, Search, Eye, Table } from 'lucide-react';
import DataGrid from '../components/DataGrid';

const GenList = () => {
    const [tables, setTables] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch('/api/tool/gen/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setTables(data.data || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch tables:", err);
                setLoading(false);
            });
    }, []);

    const columns = [
        { key: 'tableId', header: 'ID', sortable: true, align: 'center' },
        {
            key: 'tableName',
            header: 'Table Name',
            sortable: true,
            render: (value) => (
                <span style={{ fontWeight: 600, fontFamily: 'monospace', fontSize: '12px' }}>{value}</span>
            )
        },
        {
            key: 'tableComment',
            header: 'Comment',
            sortable: true,
            render: (value) => <span style={{ color: 'var(--text-secondary)' }}>{value || '-'}</span>
        },
        {
            key: 'className',
            header: 'Entity Class',
            sortable: true,
            render: (value) => (
                <span className="badge-outline" style={{ fontSize: '11px', fontFamily: 'monospace' }}>{value}</span>
            )
        },
        {
            key: 'tplCategory',
            header: 'Template',
            sortable: true,
            render: (value) => {
                const colors = { 'crud': '#3b82f6', 'tree': '#10b981', 'sub': '#f59e0b' };
                return (
                    <span style={{
                        padding: '4px 10px',
                        borderRadius: '12px',
                        fontSize: '11px',
                        fontWeight: 600,
                        background: `${colors[value] || '#6b7280'}20`,
                        color: colors[value] || '#6b7280'
                    }}>
                        {(value || 'crud').toUpperCase()}
                    </span>
                );
            }
        }
    ];

    const actions = [
        { label: 'Preview', icon: Eye, onClick: (row) => console.log('Preview:', row) },
        { label: 'Download', icon: Download, onClick: (row) => console.log('Download:', row) },
        { label: 'Sync', icon: RefreshCw, onClick: (row) => console.log('Sync:', row) }
    ];

    const toolbarActions = [
        {
            label: 'Import',
            icon: Table,
            onClick: () => console.log('Import table')
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
                        background: 'linear-gradient(135deg, #12c2e9 0%, #c471ed 100%, #f64f59 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <Code size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Code Generator</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Generate CRUD code from database tables
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
                data={tables}
                columns={columns}
                actions={actions}
                loading={loading}
                searchable={true}
                sortable={true}
                filterable={true}
                selectable={true}
                pagination={true}
                pageSize={10}
                emptyMessage="No tables found. Import a table to generate code."
            />
        </div>
    );
};

export default GenList;
