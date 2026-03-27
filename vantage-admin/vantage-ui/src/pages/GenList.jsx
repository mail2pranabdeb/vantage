import { useState, useEffect } from 'react';
import { Code, Download, Database, Table, Play, Check, X } from 'lucide-react';
import { useToast } from '../components/Toast';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';

const GenList = () => {
    const { addToast } = useToast();
    const [tables, setTables] = useState([]);
    const [selectedTables, setSelectedTables] = useState([]);
    const [loading, setLoading] = useState(true);
    const [generating, setGenerating] = useState(false);
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

    useEffect(() => {
        fetchTables();
    }, []);

    const fetchTables = () => {
        setLoading(true);
        fetch('/api/tool/gen/db/tables')
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

        fetch('/api/tool/gen/batch', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                tables: selectedTables,
                config: config
            })
        })
        .then(res => res.json())
        .then(data => {
            setGenerating(false);
            if (data.code === 200) {
                addToast('success', `Generated code for ${selectedTables.length} table(s)! Downloading...`, 5000);
                // Download generated zip
                window.location.href = '/api/tool/gen/download?tables=' + selectedTables.join(',');
                setSelectedTables([]);
            } else {
                addToast('error', data.msg || 'Failed to generate code', 5000);
            }
        })
        .catch(err => {
            setGenerating(false);
            console.error("Failed to generate code:", err);
            addToast('error', 'Failed to generate code', 5000);
        });
    };

    const handlePreview = (tableName) => {
        window.open(`/api/tool/gen/preview?table=${tableName}`, '_blank');
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
                <div style={{ display: 'flex', gap: '8px' }}>
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
                    </div>
                ) : (
                    tables.map(table => (
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
                            <button
                                onClick={(e) => {
                                    e.stopPropagation();
                                    handlePreview(table.tableName);
                                }}
                                style={{
                                    position: 'absolute',
                                    bottom: '8px',
                                    right: '8px',
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
                                <Download size={12} />
                                Preview
                            </button>
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
        </div>
    );
};

export default GenList;
