import { useState, useEffect } from 'react';
import { Save, Play, Download, Database, Table, Columns, Filter, BarChart3, Settings, Trash2, Plus, Eye, X } from 'lucide-react';

const ReportDesigner = ({ tab }) => {
    const [template, setTemplate] = useState({
        templateName: '',
        templateKey: '',
        description: '',
        datasourceKey: '',
        reportMode: 'SQL',
        sqlContent: '',
        columnsConfig: '[]',
        outputFormat: 'EXCEL',
        status: '0'
    });
    const [datasources, setDatasources] = useState([]);
    const [tables, setTables] = useState([]);
    const [previewData, setPreviewData] = useState(null);
    const [previewParams, setPreviewParams] = useState('{}');
    const [loading, setLoading] = useState(false);
    const [activeTab, setActiveTab] = useState('datasource'); // datasource, columns, sql, preview, email
    
    // Table filter and pagination state
    const [tableSearch, setTableSearch] = useState('');
    const [tablePage, setTablePage] = useState(1);
    const tablesPerPage = 5;
    
    // System metadata tables to exclude (database internal, not application tables)
    const systemTables = [
        'CONSTANT_CATALOG', 'CONSTANT_SCHEMA',
        'INDEXES', 'INDEX_COLUMNS', 'INDEX_STATISTICS',
        'INFORMATION_SCHEMA',
        'RIGHTS', 'ROLES', 'SESSIONS', 'SESSION_STATE',
        'SETTINGS', 'SYNONYMS', 'TABLE_PRIVILEGES',
        'TABLE_TYPES', 'TYPE_INFO',
        'USERS', 'QUERY_STATISTICS', 'LOCK_STATISTICS',
        'CROSS_REFERENCE', 'DOMAINS', 'DOMAIN_CONSTRAINTS',
        'KEY_COLUMN_USAGE', 'REFERENTIAL_CONSTRAINTS',
        'SEQUENCES', 'CHECK_CONSTRAINTS', 'CONSTRAINTS',
        'COLLATIONS', 'FUNCTIONS', 'FUNCTION_COLUMNS',
        'METHODS', 'PARAMETERS', 'SCHEMATA',
        'TABLE_CONSTRAINTS', 'VIEWS', 'TRIGGERS',
        'CATALOGS', 'HELP',
        'QRTZ_' // Quartz internal tables
    ];

    // Load template from tab URL (passed from App.jsx via TabContent)
    useEffect(() => {
        if (tab && tab.url) {
            const params = new URLSearchParams(tab.url.split('?')[1] || '');
            const templateId = params.get('templateId');
            if (templateId) {
                fetch(`/api/system/report-designer/templates/${templateId}`)
                    .then(res => res.json())
                    .then(data => {
                        if (data.code === 200 && data.data) {
                            setTemplate(data.data);
                            if (data.data.datasourceKey) {
                                loadTables(data.data.datasourceKey);
                            }
                        }
                    });
            }
        }
    }, [tab]);

    useEffect(() => {
        fetch('/api/system/datasource/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) setDatasources(data.data || []);
            });
    }, []);

    const loadTables = (dsKey) => {
        fetch(`/api/system/report-designer/datasource/${dsKey}/tables`)
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    const allTables = data.data || [];
                    // Filter out system tables
                    const userTables = allTables.filter(t => {
                        const name = t.tableName.toLowerCase();
                        return !systemTables.some(sys => name.includes(sys.toLowerCase()));
                    });
                    setTables(userTables);
                    setTablePage(1);
                }
            });
    };

    const handleDatasourceChange = (dsKey) => {
        setTemplate(prev => ({ ...prev, datasourceKey: dsKey }));
        loadTables(dsKey);
    };

    const addColumn = (table, column) => {
        const cols = JSON.parse(template.columnsConfig || '[]');
        cols.push({
            tableName: table,
            columnName: column.columnName,
            alias: '',
            label: column.columnName,
            visible: true,
            width: 120
        });
        setTemplate(prev => ({ ...prev, columnsConfig: JSON.stringify(cols) }));
    };

    const removeColumn = (index) => {
        const cols = JSON.parse(template.columnsConfig || '[]');
        cols.splice(index, 1);
        setTemplate(prev => ({ ...prev, columnsConfig: JSON.stringify(cols) }));
    };

    const executePreview = () => {
        setLoading(true);
        
        let params = {};
        try {
            params = JSON.parse(previewParams || '{}');
        } catch (e) {
            alert('Invalid JSON in parameters');
            setLoading(false);
            return;
        }
        
        const payload = {
            ...template,
            sqlContent: template.reportMode === 'SQL' ? template.sqlContent : buildSql()
        };

        const queryString = Object.keys(params).length > 0 ? `?params=${encodeURIComponent(JSON.stringify(params))}` : '';

        fetch(`/api/system/report-designer/preview${queryString}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                setPreviewData(data.data);
                setActiveTab('preview');
            } else {
                alert(data.msg || 'Preview failed');
            }
            setLoading(false);
        })
        .catch(() => { setLoading(false); });
    };

    const buildSql = () => {
        const cols = JSON.parse(template.columnsConfig || '[]');
        if (cols.length === 0) return 'SELECT 1';
        const selectCols = cols.map(c => c.alias ? `${c.tableName}.${c.columnName} AS ${c.alias}` : `${c.tableName}.${c.columnName}`);
        return `SELECT ${selectCols.join(', ')} FROM ${cols[0].tableName}`;
    };

    // Filter and paginate tables
    const filteredTables = tables.filter(t => 
        !tableSearch || t.tableName.toLowerCase().includes(tableSearch.toLowerCase())
    );
    const totalPages = Math.ceil(filteredTables.length / tablesPerPage);
    const paginatedTables = filteredTables.slice(
        (tablePage - 1) * tablesPerPage,
        tablePage * tablesPerPage
    );

    const saveTemplate = () => {
        const isEdit = !!template.templateId;
        const url = isEdit ? '/api/system/report-designer/templates' : '/api/system/report-designer/templates';
        const method = isEdit ? 'PUT' : 'POST';

        fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(template)
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                alert('Template saved successfully!');
                if (!isEdit) setTemplate(data.data);
            } else {
                alert(data.msg || 'Save failed');
            }
        });
    };

    const activateTemplate = () => {
        if (!template.templateId) {
            alert('Please save the template first');
            return;
        }
        if (!confirm('Create a new version and activate? This will increment the version number.')) return;

        fetch(`/api/system/report-designer/templates/${template.templateId}/activate`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' }
        })
        .then(res => res.json())
        .then(data => {
            if (data.code === 200) {
                alert('New version activated successfully!');
                setTemplate(data.data);
            } else {
                alert(data.msg || 'Activate failed');
            }
        });
    };

    const exportReport = (format) => {
        const params = JSON.stringify({});
        window.open(`/api/system/report-designer/export/${template.templateId}?params=${encodeURIComponent(params)}&format=${format}`, '_blank');
    };

    const tabs = [
        { key: 'datasource', label: 'Datasource', icon: Database },
        { key: 'columns', label: 'Columns', icon: Columns },
        { key: 'sql', label: 'SQL', icon: Table },
        { key: 'preview', label: 'Preview', icon: Eye },
        { key: 'email', label: 'Email & Schedule', icon: Settings }
    ];

    return (
        <div className="page-container">
            <div className="page-header">
                <h2>Report Designer</h2>
                <div style={{ display: 'flex', gap: '8px' }}>
                    <button className="btn btn-secondary" onClick={saveTemplate} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <Save size={16} /> Save
                    </button>
                    {template.templateId && (
                        <button className="btn btn-primary" onClick={activateTemplate} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <Play size={16} /> Activate
                        </button>
                    )}
                    <button className="btn btn-secondary" onClick={executePreview} disabled={loading} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <Play size={16} /> {loading ? 'Running...' : 'Preview'}
                    </button>
                    {template.templateId && (
                        <>
                            <button className="btn btn-secondary" onClick={() => exportReport('EXCEL')} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                <Download size={16} /> Excel
                            </button>
                            <button className="btn btn-secondary" onClick={() => exportReport('CSV')} style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                <Download size={16} /> CSV
                            </button>
                        </>
                    )}
                </div>
            </div>

            {/* Tabs */}
            <div style={{ display: 'flex', gap: '4px', marginBottom: '16px', background: 'var(--bg-secondary)', padding: '8px', borderRadius: '8px' }}>
                {tabs.map(tab => (
                    <button key={tab.key} onClick={() => setActiveTab(tab.key)}
                        style={{
                            padding: '8px 16px', border: 'none', borderRadius: '6px', cursor: 'pointer',
                            background: activeTab === tab.key ? 'var(--primary-color)' : 'transparent',
                            color: activeTab === tab.key ? '#fff' : 'var(--text-secondary)',
                            display: 'flex', alignItems: 'center', gap: '6px', fontSize: '13px', fontWeight: 500
                        }}>
                        <tab.icon size={14} /> {tab.label}
                    </button>
                ))}
            </div>

            {/* Tab Content */}
            <div style={{ background: 'var(--bg-secondary)', padding: '16px', borderRadius: '8px', minHeight: '500px' }}>
                {/* Datasource Tab */}
                {activeTab === 'datasource' && (
                    <div>
                        <h3 style={{ marginBottom: '16px' }}>Select Datasource</h3>
                        <div className="form-group">
                            <label className="form-label">Datasource</label>
                            <select className="form-input" value={template.datasourceKey} onChange={e => handleDatasourceChange(e.target.value)}>
                                <option value="">-- Select Datasource --</option>
                                {datasources.map(ds => (
                                    <option key={ds.datasourceKey} value={ds.datasourceKey}>{ds.datasourceName} ({ds.datasourceKey})</option>
                                ))}
                            </select>
                        </div>
                        <div className="form-group">
                            <label className="form-label">Template Name</label>
                            <input className="form-input" value={template.templateName} onChange={e => setTemplate(prev => ({ ...prev, templateName: e.target.value }))} placeholder="e.g., Sales Report" />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Template Key</label>
                            <input className="form-input" value={template.templateKey} onChange={e => setTemplate(prev => ({ ...prev, templateKey: e.target.value }))} placeholder="e.g., sales_report" />
                        </div>
                    </div>
                )}

                {/* Columns Tab */}
                {activeTab === 'columns' && (
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', height: 'calc(100vh - 280px)', minHeight: '500px' }}>
                        <div style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden', paddingRight: '8px' }}>
                            <div style={{ flexShrink: 0, marginBottom: '12px' }}>
                                <h3 style={{ marginBottom: '8px' }}>Available Tables & Columns</h3>
                                <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                                    <input className="form-input" placeholder="Search tables..." value={tableSearch} onChange={e => { setTableSearch(e.target.value); setTablePage(1); }}
                                        style={{ fontSize: '12px', padding: '6px 10px' }} />
                                    <span style={{ fontSize: '11px', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
                                        {filteredTables.length} tables
                                    </span>
                                </div>
                            </div>
                            <div style={{ flex: 1, overflow: 'auto' }}>
                                {paginatedTables.length === 0 ? (
                                    <p style={{ color: 'var(--text-muted)' }}>
                                        {tables.length === 0 ? 'Select a datasource first' : 'No tables match your search'}
                                    </p>
                                ) : (
                                    paginatedTables.map(table => (
                                        <div key={table.tableName} style={{ marginBottom: '12px', padding: '12px', background: 'var(--bg-tertiary)', borderRadius: '8px' }}>
                                            <div style={{ fontWeight: 600, marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                                                <Table size={14} /> {table.tableName}
                                                {table.tableComment && <span style={{ fontSize: '10px', color: 'var(--text-muted)', fontWeight: 400 }}>({table.tableComment})</span>}
                                            </div>
                                            {table.columns.map(col => (
                                                <div key={col.columnName} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '4px 8px', marginBottom: '4px', background: 'var(--bg-secondary)', borderRadius: '4px', fontSize: '12px' }}>
                                                    <span>{col.columnName} <span style={{ color: 'var(--text-muted)' }}>({col.dataType})</span></span>
                                                    <button className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '11px' }} onClick={() => addColumn(table.tableName, col)}>
                                                        <Plus size={12} /> Add
                                                    </button>
                                                </div>
                                            ))}
                                        </div>
                                    ))
                                )}
                            </div>
                            {/* Pagination */}
                            {totalPages > 1 && (
                                <div style={{ flexShrink: 0, marginTop: '12px', display: 'flex', justifyContent: 'center', gap: '4px', alignItems: 'center' }}>
                                    <button className="btn btn-secondary" disabled={tablePage === 1} onClick={() => setTablePage(p => p - 1)}
                                        style={{ padding: '4px 8px', fontSize: '11px' }}>Prev</button>
                                    <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{tablePage} / {totalPages}</span>
                                    <button className="btn btn-secondary" disabled={tablePage === totalPages} onClick={() => setTablePage(p => p + 1)}
                                        style={{ padding: '4px 8px', fontSize: '11px' }}>Next</button>
                                </div>
                            )}
                        </div>
                        <div style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden', borderLeft: '1px solid var(--border-color)', paddingLeft: '16px' }}>
                            <h3 style={{ marginBottom: '16px', flexShrink: 0 }}>Selected Columns ({JSON.parse(template.columnsConfig || '[]').length})</h3>
                            <div style={{ flex: 1, overflow: 'auto' }}>
                                {JSON.parse(template.columnsConfig || '[]').length === 0 ? (
                                    <p style={{ color: 'var(--text-muted)' }}>No columns selected</p>
                                ) : (
                                    <table className="ag-table" style={{ fontSize: '12px' }}>
                                        <thead>
                                            <tr>
                                                <th>Column</th>
                                                <th>Alias</th>
                                                <th>Width</th>
                                                <th></th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {JSON.parse(template.columnsConfig || '[]').map((col, idx) => (
                                                <tr key={idx}>
                                                    <td>{col.tableName}.{col.columnName}</td>
                                                    <td><input className="form-input" style={{ width: '80px', padding: '2px 4px', fontSize: '11px' }} value={col.alias || ''} onChange={e => {
                                                        const cols = JSON.parse(template.columnsConfig);
                                                        cols[idx].alias = e.target.value;
                                                        setTemplate(prev => ({ ...prev, columnsConfig: JSON.stringify(cols) }));
                                                    }} /></td>
                                                    <td><input type="number" className="form-input" style={{ width: '50px', padding: '2px 4px', fontSize: '11px' }} value={col.width} onChange={e => {
                                                        const cols = JSON.parse(template.columnsConfig);
                                                        cols[idx].width = parseInt(e.target.value);
                                                        setTemplate(prev => ({ ...prev, columnsConfig: JSON.stringify(cols) }));
                                                    }} /></td>
                                                    <td><button className="btn btn-secondary" style={{ padding: '2px 4px' }} onClick={() => removeColumn(idx)}><X size={12} /></button></td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                )}
                            </div>
                        </div>
                    </div>
                )}

                {/* SQL Tab */}
                {activeTab === 'sql' && (
                    <div>
                        <h3 style={{ marginBottom: '16px' }}>SQL Query</h3>
                        <p style={{ color: 'var(--text-muted)', marginBottom: '12px', fontSize: '12px' }}>
                            Write your SQL query manually or use the Columns tab to build it visually. Use :paramName for parameters.
                        </p>
                        <textarea className="form-input" rows="15" style={{ fontFamily: 'monospace', fontSize: '13px' }}
                            value={template.sqlContent}
                            onChange={e => setTemplate(prev => ({ ...prev, sqlContent: e.target.value }))}
                            placeholder="SELECT * FROM users WHERE status = :status" />
                    </div>
                )}

                {/* Preview Tab */}
                {activeTab === 'preview' && (
                    <div style={{ height: 'calc(100vh - 280px)', display: 'flex', flexDirection: 'column' }}>
                        <h3 style={{ marginBottom: '12px', flexShrink: 0 }}>Preview Results</h3>
                        <div className="form-group" style={{ marginBottom: '8px', flexShrink: 0 }}>
                            <label className="form-label">Parameters (JSON)</label>
                            <textarea
                                className="form-input"
                                rows={2}
                                value={previewParams || '{}'}
                                onChange={e => setPreviewParams(e.target.value)}
                                placeholder='{"status": "0"}'
                                style={{ fontFamily: 'monospace', fontSize: '12px' }}
                            />
                            <p style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '2px' }}>
                                Example: status = "0". Use :paramName in SQL
                            </p>
                        </div>
                        <button className="btn btn-primary" onClick={executePreview} disabled={loading} style={{ marginBottom: '8px', flexShrink: 0 }}>
                            <Play size={14} /> {loading ? 'Running...' : 'Run Preview'}
                        </button>
                        {previewData ? (
                            <div style={{ flex: 1, overflow: 'auto', minHeight: '200px' }}>
                                <p style={{ marginBottom: '6px', fontSize: '12px', color: 'var(--text-muted)', flexShrink: 0 }}>
                                    {previewData.count} rows returned
                                </p>
                                {previewData.sql && (
                                    <pre style={{ background: 'var(--bg-tertiary)', padding: '6px', borderRadius: '4px', fontSize: '10px', maxHeight: '50px', overflow: 'auto', marginBottom: '6px', whiteSpace: 'pre-wrap' }}>
                                        {previewData.sql}
                                    </pre>
                                )}
                                <div style={{ overflow: 'auto', flex: 1, border: '1px solid var(--border-color)', borderRadius: '8px' }}>
                                    <table className="ag-table" style={{ minWidth: '100%' }}>
                                        <thead style={{ position: 'sticky', top: 0, background: 'var(--bg-tertiary)' }}>
                                            <tr>
                                                {previewData.data.length > 0 && Object.keys(previewData.data[0]).map(key => (
                                                    <th key={key} style={{ padding: '6px 8px', textAlign: 'left', whiteSpace: 'nowrap' }}>{key}</th>
                                                ))}
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {previewData.data.map((row, idx) => (
                                                <tr key={idx}>
                                                    {Object.values(row).map((val, i) => (
                                                        <td key={i} style={{ padding: '4px 8px' }}>{val !== null ? val.toString() : '-'}</td>
                                                    ))}
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        ) : (
                            <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
                                <p>Click Run Preview to see results</p>
                            </div>
                        )}
                    </div>
                )}

                {/* Email & Schedule Tab */}
                {activeTab === 'email' && (
                    <div style={{ maxHeight: 'calc(100vh - 280px)', overflow: 'auto' }}>
                        <h3 style={{ marginBottom: '16px' }}>Email & Scheduling</h3>
                        <p style={{ color: 'var(--text-muted)', marginBottom: '16px', fontSize: '12px' }}>
                            Configure how this report will be sent via email and scheduled.
                        </p>
                        
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                            <div>
                                <div className="form-group">
                                    <label className="form-label">Output Format</label>
                                    <select className="form-input" value={template.outputFormat} onChange={e => setTemplate(prev => ({ ...prev, outputFormat: e.target.value }))}>
                                        <option value="EXCEL">Excel (.xls)</option>
                                        <option value="CSV">CSV</option>
                                        <option value="HTML">HTML</option>
                                        <option value="JSON">JSON</option>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div style={{ marginTop: '24px', padding: '20px', background: 'var(--bg-tertiary)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                            <h4 style={{ marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                                <Settings size={16} /> How to Schedule this Report
                            </h4>
                            <ol style={{ fontSize: '13px', lineHeight: '2', paddingLeft: '20px' }}>
                                <li><strong>Save this report template</strong> using the Save button above</li>
                                <li><strong>Activate</strong> the report using the Activate button to create a new version</li>
                                <li>Go to <strong>Job Scheduling → Add Job</strong></li>
                                <li>Set Job Name (e.g., "Sales Report Email")</li>
                                <li>Set Job Group (e.g., "reports")</li>
                                <li>Set invoke target:
                                    <pre style={{ background: 'var(--bg-secondary)', padding: '12px', borderRadius: '6px', fontSize: '11px', marginTop: '8px', overflow: 'auto' }}>
reportExecutionJob.execute(TEMPLATE_ID, 'EXCEL', ['user@email.com'], null, 'Report Subject', 'Email body text', '{}')</pre>
                                </li>
                                <li>Set cron expression (e.g., <code style={{ background: 'var(--bg-secondary)', padding: '2px 6px', borderRadius: '4px' }}>0 0 9 * * ?</code> for daily at 9 AM)</li>
                                <li>Enable the job and save - the report will be emailed automatically</li>
                            </ol>
                        </div>

                        <div style={{ marginTop: '20px', padding: '16px', background: 'rgba(59, 130, 246, 0.1)', borderRadius: '8px', border: '1px solid rgba(59, 130, 246, 0.2)' }}>
                            <h4 style={{ marginBottom: '8px', color: '#3b82f6' }}>Quick Test</h4>
                            <p style={{ fontSize: '12px', color: 'var(--text-secondary)', marginBottom: '12px' }}>
                                Use the Preview button to test your report with parameters before scheduling.
                            </p>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default ReportDesigner;
