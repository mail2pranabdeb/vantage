import { useState, useEffect, useMemo } from 'react';
import { Code, Download, Database, Table, Play, Check, X, Plus, Trash2, Save, Copy, Search } from 'lucide-react';
import { useToast } from '../components/Toast';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';

const COLUMN_TYPES = [
  { value: 'VARCHAR', label: 'VARCHAR' },
  { value: 'INTEGER', label: 'INTEGER' },
  { value: 'BIGINT', label: 'BIGINT' },
  { value: 'DECIMAL', label: 'DECIMAL' },
  { value: 'BOOLEAN', label: 'BOOLEAN' },
  { value: 'DATE', label: 'DATE' },
  { value: 'TIMESTAMP', label: 'TIMESTAMP' },
  { value: 'TEXT', label: 'TEXT' },
  { value: 'CLOB', label: 'CLOB' },
  { value: 'BLOB', label: 'BLOB' }
];

const GenList = () => {
    const { addToast } = useToast();
    const [tables, setTables] = useState([]);
    const [selectedTables, setSelectedTables] = useState([]);
    const [loading, setLoading] = useState(true);
    const [generating, setGenerating] = useState(false);

    const [datasources, setDatasources] = useState([]);
    const [selectedDs, setSelectedDs] = useState('master');
    const [searchQuery, setSearchQuery] = useState('');

    const [isCloneOpen, setIsCloneOpen] = useState(false);
    const [cloning, setCloning] = useState(false);
    const [cloneSource, setCloneSource] = useState(null);
    const [cloneConfig, setCloneConfig] = useState({ newTableName: '', newTableComment: '' });

    const [isPreviewOpen, setIsPreviewOpen] = useState(false);
    const [previewData, setPreviewData] = useState(null);
    const [previewLoading, setPreviewLoading] = useState(false);

    const filteredTables = useMemo(() => {
        if (!searchQuery.trim()) return tables;
        const q = searchQuery.toLowerCase();
        return tables.filter(t =>
            t.tableName.toLowerCase().includes(q) ||
            (t.tableComment && t.tableComment.toLowerCase().includes(q))
        );
    }, [tables, searchQuery]);

    const [isConfigOpen, setIsConfigOpen] = useState(false);
    const [config, setConfig] = useState({
        packageName: 'com.pd.modules',
        author: 'admin',
        moduleName: 'system',
        tablePrefix: 'sys_',
        generateMenu: true,
        generateEntity: true,
        generateRepository: true,
        generateService: true,
        generateController: true,
        generateUi: true
    });

    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [creating, setCreating] = useState(false);
    const [newTable, setNewTable] = useState({
        tableName: '',
        tableComment: '',
        datasourceKey: 'master',
        columns: [
            { columnName: 'name', columnType: 'VARCHAR', columnLength: 255, nullable: true, defaultValue: '', columnComment: '' }
        ]
    });

    useEffect(() => {
        fetchDatasources();
    }, []);

    useEffect(() => {
        fetchTables();
    }, [selectedDs]);

    const fetchDatasources = () => {
        fetch('/api/system/datasource/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    const dsList = data.data || [];
                    const options = [{ datasourceKey: 'master', datasourceName: 'Default (H2)' }, ...dsList];
                    setDatasources(options);
                }
            })
            .catch(() => {});
    };

    const fetchTables = () => {
        setLoading(true);
        fetch(`/api/tool/gen/db/tables?datasourceKey=${selectedDs}`)
            .then(res => res.json())
            .then(data => {
                setLoading(false);
                if (data.code === 200) {
                    setTables(data.data || []);
                } else {
                    addToast('error', data.msg || 'Failed to load tables', 4000);
                }
            })
            .catch(err => {
                console.error("Failed to fetch tables:", err);
                setLoading(false);
                addToast('error', 'Failed to load tables', 5000);
            });
    };

    const toggleTableSelection = (tableName) => {
        setSelectedTables(prev =>
            prev.includes(tableName)
                ? prev.filter(t => t !== tableName)
                : [...prev, tableName]
        );
    };

    const selectAll = () => {
        setSelectedTables(tables.map(t => t.tableName));
    };

    const deselectAll = () => {
        setSelectedTables([]);
    };

    const handleGenerate = () => {
        if (selectedTables.length === 0) {
            addToast('error', 'Please select at least one table', 3000);
            return;
        }

        setGenerating(true);
        setIsConfigOpen(false);

        const table = selectedTables[0];
        const tableInfo = tables.find(t => t.tableName === table);
        const params = new URLSearchParams({
            tableName: table,
            tableComment: tableInfo?.tableComment || '',
            datasourceKey: selectedDs,
            moduleName: config.moduleName,
            packageName: config.packageName,
            author: config.author
        });

        fetch(`/api/tool/gen/download?${params}`)
            .then(res => {
                const ct = res.headers.get('Content-Type') || '';
                if (ct.includes('text/plain')) {
                    return res.text().then(msg => { throw new Error(msg); });
                }
                const disposition = res.headers.get('Content-Disposition');
                const filename = disposition
                    ? disposition.split('filename=')[1]?.replace(/['"]/g, '') || 'module.zip'
                    : 'module.zip';
                return res.blob().then(blob => ({ blob, filename }));
            })
            .then(({ blob, filename }) => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = filename;
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                window.URL.revokeObjectURL(url);
                setGenerating(false);
                setSelectedTables([]);
                addToast('success', `Downloaded module for ${table}`, 3000);
            })
            .catch(err => {
                setGenerating(false);
                addToast('error', err.message, 5000);
            });
    };

    const handlePreview = (tableName, tableComment) => {
        setPreviewLoading(true);
        setIsPreviewOpen(true);
        const params = new URLSearchParams({
            tableName,
            tableComment: tableComment || '',
            moduleName: config.moduleName,
            packageName: config.packageName,
            author: config.author
        });
        fetch(`/api/tool/gen/preview-code?${params}`)
            .then(res => res.json())
            .then(data => {
                setPreviewLoading(false);
                if (data.code === 200) {
                    setPreviewData(data.data);
                } else {
                    setPreviewData(null);
                    addToast('error', data.msg || 'Preview failed', 5000);
                }
            })
            .catch(err => {
                setPreviewLoading(false);
                setPreviewData(null);
                addToast('error', 'Preview failed: ' + err.message, 5000);
            });
    };

    const addColumn = () => {
        setNewTable(prev => ({
            ...prev,
            columns: [...prev.columns, { columnName: '', columnType: 'VARCHAR', columnLength: 255, nullable: true, defaultValue: '', columnComment: '' }]
        }));
    };

    const removeColumn = (idx) => {
        setNewTable(prev => ({
            ...prev,
            columns: prev.columns.filter((_, i) => i !== idx)
        }));
    };

    const updateColumn = (idx, field, value) => {
        setNewTable(prev => {
            const cols = [...prev.columns];
            cols[idx] = { ...cols[idx], [field]: value };
            return { ...prev, columns: cols };
        });
    };

    const openCloneDialog = (table) => {
        setCloneSource(table);
        setCloneConfig({
            newTableName: table.tableName + '_CLONE',
            newTableComment: (table.tableComment || table.tableName) + ' (Cloned)'
        });
        setIsCloneOpen(true);
    };

    const handleCloneTable = () => {
        if (!cloneSource) return;
        if (!cloneConfig.newTableName.trim()) {
            addToast('error', 'Please enter a new table name', 3000);
            return;
        }
        setCloning(true);
        fetch('/api/tool/gen/db/clone-table', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                sourceTableName: cloneSource.tableName,
                newTableName: cloneConfig.newTableName.trim(),
                newTableComment: cloneConfig.newTableComment.trim(),
                datasourceKey: selectedDs
            })
        })
        .then(res => res.json())
        .then(data => {
            setCloning(false);
            if (data.code === 200) {
                addToast('success', data.msg || 'Table cloned', 3000);
                setIsCloneOpen(false);
                setCloneSource(null);
                fetchTables();
                // Auto-select and open generate dialog
                const newName = cloneConfig.newTableName.trim();
                setTimeout(() => {
                    setSelectedTables([newName]);
                    setIsConfigOpen(true);
                }, 500);
            } else {
                addToast('error', data.msg || 'Failed to clone table', 5000);
            }
        })
        .catch(err => {
            setCloning(false);
            addToast('error', 'Failed to clone table: ' + err.message, 5000);
        });
    };

    const handleCreateTable = () => {
        if (!newTable.tableName.trim()) {
            addToast('error', 'Please enter a table name', 3000);
            return;
        }
        if (newTable.columns.length === 0 || !newTable.columns.some(c => c.columnName.trim())) {
            addToast('error', 'Please add at least one column', 3000);
            return;
        }

        setCreating(true);
        const payload = { ...newTable, datasourceKey: selectedDs, columns: newTable.columns.filter(c => c.columnName.trim()) };

        fetch('/api/tool/gen/db/create-table', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(res => res.json())
        .then(data => {
            setCreating(false);
            if (data.code === 200) {
                addToast('success', data.msg || 'Table created', 3000);
                setIsCreateOpen(false);
                fetchTables();
            } else {
                addToast('error', data.msg || 'Failed to create table', 5000);
            }
        })
        .catch(err => {
            setCreating(false);
            addToast('error', 'Failed to create table: ' + err.message, 5000);
        });
    };

    return (
        <div style={{
            height: 'calc(100vh - 50px)',
            overflow: 'auto',
            padding: '8px'
        }}>
            <div className="page-header" style={{ marginBottom: '12px' }}>
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
                        <Code size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '14px', fontWeight: 700, margin: 0 }}>Code Generation</h2>
                        <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Generate CRUD code from database tables
                        </p>
                    </div>
                </div>
                <div style={{ display: 'flex', gap: '8px', alignItems: 'center', flexWrap: 'wrap' }}>
                    <select
                        value={selectedDs}
                        onChange={(e) => { setSelectedDs(e.target.value); setSelectedTables([]); }}
                        style={{
                            padding: '6px 10px',
                            fontSize: '12px',
                            borderRadius: '6px',
                            border: '1px solid var(--border-color)',
                            background: 'var(--bg-secondary)',
                            color: 'var(--text-primary)',
                            cursor: 'pointer'
                        }}
                    >
                        {datasources.map(ds => (
                            <option key={ds.datasourceKey} value={ds.datasourceKey}>
                                {ds.datasourceName} ({ds.datasourceKey})
                            </option>
                        ))}
                    </select>
                    <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                        <Search size={14} style={{ position: 'absolute', left: '8px', color: 'var(--text-muted)', pointerEvents: 'none' }} />
                        <input
                            type="text"
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            placeholder="Search tables..."
                            style={{
                                padding: '6px 10px 6px 28px',
                                fontSize: '12px',
                                borderRadius: '6px',
                                border: '1px solid var(--border-color)',
                                background: 'var(--bg-secondary)',
                                color: 'var(--text-primary)',
                                width: '180px',
                                outline: 'none'
                            }}
                        />
                        {searchQuery && (
                            <button
                                onClick={() => setSearchQuery('')}
                                style={{
                                    position: 'absolute', right: '4px',
                                    border: 'none', background: 'transparent',
                                    cursor: 'pointer', color: 'var(--text-muted)',
                                    padding: '2px', display: 'flex'
                                }}
                            >
                                <X size={12} />
                            </button>
                        )}
                    </div>
                    <button
                        className="btn btn-secondary"
                        onClick={() => {
                            setNewTable({
                                tableName: '',
                                tableComment: '',
                                datasourceKey: selectedDs,
                                columns: [{ columnName: 'name', columnType: 'VARCHAR', columnLength: 255, nullable: true, defaultValue: '', columnComment: '' }]
                            });
                            setIsCreateOpen(true);
                        }}
                        style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                    >
                        <Plus size={16} />
                        Create Table
                    </button>
                    <button
                        className="btn btn-secondary"
                        onClick={selectAll}
                        style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                    >
                        <Check size={16} />
                        Select All
                    </button>
                    <button
                        className="btn btn-secondary"
                        onClick={deselectAll}
                        style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                    >
                        <X size={16} />
                        Deselect All
                    </button>
                    <button
                        className="btn btn-primary"
                        onClick={() => setIsConfigOpen(true)}
                        disabled={selectedTables.length === 0 || generating}
                        style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                    >
                        <Play size={16} />
                        {generating ? 'Generating...' : `Generate (${selectedTables.length})`}
                    </button>
                </div>
            </div>

            {/* Tables Grid */}
            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
                gap: '12px'
            }}>
                {loading ? (
                    <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)', gridColumn: '1 / -1' }}>
                        Loading tables...
                    </div>
                ) : tables.length === 0 ? (
                    <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)', gridColumn: '1 / -1' }}>
                        <Database size={48} style={{ margin: '0 auto 16px', opacity: 0.3 }} />
                        <p>No tables found in database</p>
                        <button
                            className="btn btn-primary"
                            onClick={() => {
                                setNewTable({
                                    tableName: '',
                                    tableComment: '',
                                    datasourceKey: selectedDs,
                                    columns: [{ columnName: 'name', columnType: 'VARCHAR', columnLength: 255, nullable: true, defaultValue: '', columnComment: '' }]
                                });
                                setIsCreateOpen(true);
                            }}
                            style={{ marginTop: '12px', display: 'inline-flex', alignItems: 'center', gap: '6px' }}
                        >
                            <Plus size={16} />
                            Create Table
                        </button>
                    </div>
                ) : filteredTables.length === 0 ? (
                    <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)', gridColumn: '1 / -1' }}>
                        <Search size={48} style={{ margin: '0 auto 16px', opacity: 0.3 }} />
                        <p>No tables match "{searchQuery}"</p>
                    </div>
                ) : (
                    filteredTables.map(table => (
                        <div
                            key={table.tableName}
                            onClick={() => toggleTableSelection(table.tableName)}
                            style={{
                                padding: '12px',
                                background: selectedTables.includes(table.tableName)
                                    ? 'var(--primary-color)'
                                    : 'var(--bg-secondary)',
                                color: selectedTables.includes(table.tableName) ? 'white' : 'var(--text-primary)',
                                borderRadius: '8px',
                                border: '1px solid var(--border-color)',
                                cursor: 'pointer',
                                transition: 'all 0.2s',
                                position: 'relative'
                            }}
                            onMouseEnter={(e) => {
                                e.currentTarget.style.transform = 'translateY(-2px)';
                                e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)';
                            }}
                            onMouseLeave={(e) => {
                                e.currentTarget.style.transform = 'translateY(0)';
                                e.currentTarget.style.boxShadow = 'none';
                            }}
                        >
                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
                                <Table size={18} />
                                <span style={{ fontWeight: 600, fontSize: '13px' }}>{table.tableName}</span>
                            </div>
                            <div style={{ fontSize: '11px', opacity: 0.8 }}>
                                {table.tableComment || 'No description'}
                            </div>
                            <div style={{
                                position: 'absolute',
                                top: '8px',
                                right: '8px',
                                width: '20px',
                                height: '20px',
                                borderRadius: '50%',
                                background: selectedTables.includes(table.tableName) ? 'white' : 'var(--border-color)',
                                color: selectedTables.includes(table.tableName) ? 'var(--primary-color)' : 'transparent',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center'
                            }}>
                                <Check size={14} strokeWidth={3} />
                            </div>
                            <div style={{
                                position: 'absolute',
                                bottom: '8px',
                                right: '8px',
                                display: 'flex',
                                gap: '4px'
                            }}>
                                <button
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        setSelectedTables([table.tableName]);
                                        setIsConfigOpen(true);
                                    }}
                                    style={{
                                        padding: '4px 8px',
                                        fontSize: '11px',
                                        background: selectedTables.includes(table.tableName) ? 'rgba(255,255,255,0.15)' : 'rgba(39,174,96,0.1)',
                                        border: 'none',
                                        borderRadius: '4px',
                                        color: selectedTables.includes(table.tableName) ? 'white' : '#27ae60',
                                        cursor: 'pointer',
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '4px'
                                    }}
                                    title="Generate code for this table"
                                >
                                    <Play size={12} />
                                    Gen
                                </button>
                                <button
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        openCloneDialog(table);
                                    }}
                                    style={{
                                        padding: '4px 8px',
                                        fontSize: '11px',
                                        background: selectedTables.includes(table.tableName) ? 'rgba(255,255,255,0.15)' : 'rgba(102,126,234,0.1)',
                                        border: 'none',
                                        borderRadius: '4px',
                                        color: selectedTables.includes(table.tableName) ? 'white' : 'var(--primary-color)',
                                        cursor: 'pointer',
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '4px'
                                    }}
                                    title="Clone table"
                                >
                                    <Copy size={12} />
                                    Clone
                                </button>
                                <button
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        handlePreview(table.tableName, table.tableComment);
                                    }}
                                    style={{
                                        padding: '4px 8px',
                                        fontSize: '11px',
                                        background: 'rgba(255,255,255,0.2)',
                                        border: 'none',
                                        borderRadius: '4px',
                                        color: selectedTables.includes(table.tableName) ? 'white' : 'var(--text-secondary)',
                                        cursor: 'pointer',
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '4px'
                                    }}
                                >
                                    <Code size={12} />
                                    Preview
                                </button>
                            </div>
                        </div>
                    ))
                )}
            </div>

            {/* Configuration Modal */}
            <Modal
                isOpen={isConfigOpen}
                onClose={() => setIsConfigOpen(false)}
                title="Code Generation Configuration"
                size="medium"
                footer={
                    <>
                        <button
                            className="btn btn-secondary"
                            onClick={() => setIsConfigOpen(false)}
                            disabled={generating}
                        >
                            Cancel
                        </button>
                        <button
                            className="btn btn-primary"
                            onClick={handleGenerate}
                            disabled={generating}
                            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                        >
                            {generating && (
                                <div style={{
                                    width: '12px',
                                    height: '12px',
                                    border: '2px solid white',
                                    borderBottomColor: 'transparent',
                                    borderRadius: '50%',
                                    animation: 'spin 1s linear infinite'
                                }} />
                            )}
                            Generate Code
                        </button>
                    </>
                }
            >
                <div style={{ maxHeight: '60vh', overflowY: 'auto', paddingRight: '8px' }}>
                    <div className="form-row">
                        <FormInput
                            label="Package Name"
                            name="packageName"
                            value={config.packageName}
                            onChange={(e) => setConfig(prev => ({ ...prev, packageName: e.target.value }))}
                            placeholder="com.pd.modules"
                        />
                        <FormInput
                            label="Author"
                            name="author"
                            value={config.author}
                            onChange={(e) => setConfig(prev => ({ ...prev, author: e.target.value }))}
                            placeholder="admin"
                        />
                    </div>

                    <div className="form-row">
                        <FormInput
                            label="Module Name"
                            name="moduleName"
                            value={config.moduleName}
                            onChange={(e) => setConfig(prev => ({ ...prev, moduleName: e.target.value }))}
                            placeholder="system"
                        />
                        <FormInput
                            label="Table Prefix"
                            name="tablePrefix"
                            value={config.tablePrefix}
                            onChange={(e) => setConfig(prev => ({ ...prev, tablePrefix: e.target.value }))}
                            placeholder="sys_"
                        />
                    </div>

                    <div style={{ marginTop: '16px' }}>
                        <label className="form-label">Generate Components:</label>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
                            {[
                                { key: 'generateEntity', label: 'Entity' },
                                { key: 'generateRepository', label: 'Repository' },
                                { key: 'generateService', label: 'Service' },
                                { key: 'generateController', label: 'Controller' },
                                { key: 'generateUi', label: 'UI Components' },
                                { key: 'generateMenu', label: 'Menu Items' }
                            ].map(item => (
                                <label key={item.key} style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '8px',
                                    padding: '8px',
                                    background: 'var(--bg-tertiary)',
                                    borderRadius: '6px',
                                    cursor: 'pointer'
                                }}>
                                    <input
                                        type="checkbox"
                                        checked={config[item.key]}
                                        onChange={(e) => setConfig(prev => ({ ...prev, [item.key]: e.target.checked }))}
                                        style={{ width: '16px', height: '16px' }}
                                    />
                                    <span style={{ fontSize: '12px' }}>{item.label}</span>
                                </label>
                            ))}
                        </div>
                    </div>
                </div>
            </Modal>

            {/* Create Table Modal */}
            <Modal
                isOpen={isCreateOpen}
                onClose={() => setIsCreateOpen(false)}
                title="Create Database Table"
                size="large"
                footer={
                    <>
                        <button
                            className="btn btn-secondary"
                            onClick={() => setIsCreateOpen(false)}
                            disabled={creating}
                        >
                            Cancel
                        </button>
                        <button
                            className="btn btn-primary"
                            onClick={handleCreateTable}
                            disabled={creating}
                            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                        >
                            <Save size={16} />
                            {creating ? 'Creating...' : 'Create Table'}
                        </button>
                    </>
                }
            >
                <div style={{ maxHeight: '70vh', overflowY: 'auto', paddingRight: '8px' }}>
                    <div className="form-row">
                        <FormInput
                            label="Table Name"
                            name="tableName"
                            value={newTable.tableName}
                            onChange={(e) => setNewTable(prev => ({ ...prev, tableName: e.target.value }))}
                            placeholder="e.g. sys_products"
                        />
                        <FormInput
                            label="Table Comment"
                            name="tableComment"
                            value={newTable.tableComment}
                            onChange={(e) => setNewTable(prev => ({ ...prev, tableComment: e.target.value }))}
                            placeholder="e.g. Products table"
                        />
                    </div>

                    <div style={{ marginTop: '16px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
                            <label className="form-label" style={{ margin: 0 }}>Columns</label>
                            <button
                                className="btn btn-secondary"
                                onClick={addColumn}
                                style={{ display: 'flex', alignItems: 'center', gap: '4px', padding: '4px 10px', fontSize: '12px' }}
                            >
                                <Plus size={14} />
                                Add Column
                            </button>
                        </div>

                        {/* Header */}
                        <div style={{
                            display: 'grid',
                            gridTemplateColumns: '2fr 1.2fr 0.8fr 0.6fr 1.2fr 40px',
                            gap: '6px',
                            padding: '6px 8px',
                            fontSize: '11px',
                            fontWeight: 600,
                            color: 'var(--text-muted)',
                            borderBottom: '1px solid var(--border-color)',
                            marginBottom: '6px'
                        }}>
                            <span>Column Name</span>
                            <span>Type</span>
                            <span>Length</span>
                            <span>Nullable</span>
                            <span>Comment</span>
                            <span></span>
                        </div>

                        {newTable.columns.map((col, idx) => (
                            <div key={idx} style={{
                                display: 'grid',
                                gridTemplateColumns: '2fr 1.2fr 0.8fr 0.6fr 1.2fr 40px',
                                gap: '6px',
                                alignItems: 'center',
                                marginBottom: '6px'
                            }}>
                                <input
                                    type="text"
                                    value={col.columnName}
                                    onChange={(e) => updateColumn(idx, 'columnName', e.target.value)}
                                    placeholder="column_name"
                                    style={{
                                        padding: '6px 8px',
                                        fontSize: '12px',
                                        borderRadius: '4px',
                                        border: '1px solid var(--border-color)',
                                        background: 'var(--bg-primary)',
                                        color: 'var(--text-primary)'
                                    }}
                                />
                                <select
                                    value={col.columnType}
                                    onChange={(e) => updateColumn(idx, 'columnType', e.target.value)}
                                    style={{
                                        padding: '6px 8px',
                                        fontSize: '12px',
                                        borderRadius: '4px',
                                        border: '1px solid var(--border-color)',
                                        background: 'var(--bg-primary)',
                                        color: 'var(--text-primary)'
                                    }}
                                >
                                    {COLUMN_TYPES.map(t => (
                                        <option key={t.value} value={t.value}>{t.label}</option>
                                    ))}
                                </select>
                                <input
                                    type="number"
                                    value={col.columnLength}
                                    onChange={(e) => updateColumn(idx, 'columnLength', parseInt(e.target.value) || 255)}
                                    min="1"
                                    max="4000"
                                    style={{
                                        padding: '6px 8px',
                                        fontSize: '12px',
                                        borderRadius: '4px',
                                        border: '1px solid var(--border-color)',
                                        background: 'var(--bg-primary)',
                                        color: 'var(--text-primary)',
                                        width: '100%'
                                    }}
                                />
                                <label style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer' }}>
                                    <input
                                        type="checkbox"
                                        checked={col.nullable}
                                        onChange={(e) => updateColumn(idx, 'nullable', e.target.checked)}
                                        style={{ width: '16px', height: '16px' }}
                                    />
                                </label>
                                <input
                                    type="text"
                                    value={col.columnComment}
                                    onChange={(e) => updateColumn(idx, 'columnComment', e.target.value)}
                                    placeholder="Comment"
                                    style={{
                                        padding: '6px 8px',
                                        fontSize: '12px',
                                        borderRadius: '4px',
                                        border: '1px solid var(--border-color)',
                                        background: 'var(--bg-primary)',
                                        color: 'var(--text-primary)'
                                    }}
                                />
                                <button
                                    onClick={() => removeColumn(idx)}
                                    disabled={newTable.columns.length <= 1}
                                    style={{
                                        padding: '4px',
                                        border: 'none',
                                        borderRadius: '4px',
                                        background: 'transparent',
                                        color: '#e74c3c',
                                        cursor: newTable.columns.length <= 1 ? 'not-allowed' : 'pointer',
                                        opacity: newTable.columns.length <= 1 ? 0.3 : 1,
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center'
                                    }}
                                >
                                    <Trash2 size={14} />
                                </button>
                            </div>
                        ))}
                    </div>
                </div>
            </Modal>
            {/* Clone Table Modal */}
            <Modal
                isOpen={isCloneOpen}
                onClose={() => { setIsCloneOpen(false); setCloneSource(null); }}
                title="Clone Table"
                size="small"
                footer={
                    <>
                        <button
                            className="btn btn-secondary"
                            onClick={() => { setIsCloneOpen(false); setCloneSource(null); }}
                            disabled={cloning}
                        >
                            Cancel
                        </button>
                        <button
                            className="btn btn-primary"
                            onClick={handleCloneTable}
                            disabled={cloning}
                            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                        >
                            <Copy size={16} />
                            {cloning ? 'Cloning...' : 'Clone Table'}
                        </button>
                    </>
                }
            >
                <div style={{ padding: '8px 0' }}>
                    <p style={{ fontSize: '13px', marginBottom: '16px', color: 'var(--text-secondary)' }}>
                        Create a new table with the same columns, types, and comments as <strong>{cloneSource?.tableName}</strong>.
                    </p>
                    <div className="form-row">
                        <FormInput
                            label="New Table Name"
                            name="newTableName"
                            value={cloneConfig.newTableName}
                            onChange={(e) => setCloneConfig(prev => ({ ...prev, newTableName: e.target.value }))}
                            placeholder="MY_NEW_TABLE"
                        />
                    </div>
                    <div className="form-row">
                        <FormInput
                            label="Table Comment"
                            name="newTableComment"
                            value={cloneConfig.newTableComment}
                            onChange={(e) => setCloneConfig(prev => ({ ...prev, newTableComment: e.target.value }))}
                            placeholder="Description for the new table"
                        />
                    </div>
                </div>
            </Modal>

            {/* Preview Code Modal */}
            <Modal
                isOpen={isPreviewOpen}
                onClose={() => { setIsPreviewOpen(false); setPreviewData(null); }}
                title="Code Preview"
                size="large"
                footer={
                    <button
                        className="btn btn-secondary"
                        onClick={() => { setIsPreviewOpen(false); setPreviewData(null); }}
                    >
                        Close
                    </button>
                }
            >
                <div style={{ maxHeight: '70vh', overflow: 'auto', paddingRight: '8px' }}>
                    {previewLoading ? (
                        <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                            Loading preview...
                        </div>
                    ) : previewData ? (
                        Object.entries(previewData).map(([filename, code]) => (
                            <div key={filename} style={{ marginBottom: '16px' }}>
                                <div style={{
                                    fontSize: '12px',
                                    fontWeight: 600,
                                    color: 'var(--primary-color)',
                                    marginBottom: '4px',
                                    fontFamily: 'monospace'
                                }}>
                                    {filename}
                                </div>
                                <pre style={{
                                    margin: 0,
                                    padding: '12px',
                                    fontSize: '11px',
                                    lineHeight: 1.5,
                                    background: '#1e1e1e',
                                    color: '#d4d4d4',
                                    borderRadius: '6px',
                                    overflow: 'auto',
                                    maxHeight: '300px',
                                    whiteSpace: 'pre',
                                    tabSize: 4
                                }}>{code}</pre>
                            </div>
                        ))
                    ) : (
                        <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                            No preview data available
                        </div>
                    )}
                </div>
            </Modal>
        </div>
    );
};

export default GenList;