import { useState, useEffect } from 'react';
import { BarChart, Plus, RefreshCw, Settings, Trash2 } from 'lucide-react';
import { useToast } from '../components/Toast';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';

const DashboardBuilder = () => {
    const { addToast } = useToast();
    const [dashboards, setDashboards] = useState([]);
    const [widgets, setWidgets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isWidgetModalOpen, setIsWidgetModalOpen] = useState(false);
    const [widgetForm, setWidgetForm] = useState({
        title: '',
        widgetType: 'chart',
        chartType: 'bar',
        querySql: '',
        refreshInterval: 60
    });

    useEffect(() => {
        fetchDashboards();
    }, []);

    const fetchDashboards = () => {
        setLoading(true);
        // Fetch dashboards from API
        fetch('/api/system/dashboards')
            .then(res => res.json())
            .then(data => {
                setLoading(false);
                if (data.code === 200) {
                    setDashboards(data.data || []);
                }
            })
            .catch(err => {
                console.error("Failed to fetch dashboards:", err);
                setLoading(false);
            });
    };

    const handleAddWidget = () => {
        setIsWidgetModalOpen(true);
    };

    const handleSaveWidget = () => {
        // Save widget to dashboard
        const newWidget = {
            id: Date.now(),
            ...widgetForm,
            position: { x: 0, y: widgets.length },
            size: { w: 6, h: 4 }
        };
        setWidgets([...widgets, newWidget]);
        setIsWidgetModalOpen(false);
        addToast('success', 'Widget added successfully', 3000);
    };

    const handleRemoveWidget = (id) => {
        setWidgets(widgets.filter(w => w.id !== id));
        addToast('success', 'Widget removed', 3000);
    };

    const handleRefreshWidget = (id) => {
        // Refresh widget data
        addToast('info', 'Refreshing widget data...', 2000);
    };

    const renderWidget = (widget) => {
        return (
            <div key={widget.id} style={{
                background: 'var(--bg-secondary)',
                borderRadius: '8px',
                border: '1px solid var(--border-color)',
                padding: '16px',
                position: 'relative'
            }}>
                {/* Widget Header */}
                <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: '12px'
                }}>
                    <h3 style={{ fontSize: '14px', fontWeight: 600, margin: 0 }}>{widget.title}</h3>
                    <div style={{ display: 'flex', gap: '4px' }}>
                        <button
                            onClick={() => handleRefreshWidget(widget.id)}
                            style={{
                                background: 'transparent',
                                border: 'none',
                                cursor: 'pointer',
                                padding: '4px',
                                color: 'var(--text-secondary)'
                            }}
                            title="Refresh"
                        >
                            <RefreshCw size={14} />
                        </button>
                        <button
                            onClick={() => handleRemoveWidget(widget.id)}
                            style={{
                                background: 'transparent',
                                border: 'none',
                                cursor: 'pointer',
                                padding: '4px',
                                color: 'var(--danger)'
                            }}
                            title="Remove"
                        >
                            <Trash2 size={14} />
                        </button>
                    </div>
                </div>

                {/* Widget Content */}
                <div style={{
                    height: '200px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    background: 'var(--bg-tertiary)',
                    borderRadius: '6px'
                }}>
                    {widget.chartType === 'bar' && <BarChart size={48} style={{ opacity: 0.3 }} />}
                    {widget.chartType === 'line' && <div style={{ fontSize: '48px', opacity: 0.3 }}>📈</div>}
                    {widget.chartType === 'pie' && <div style={{ fontSize: '48px', opacity: 0.3 }}>🥧</div>}
                    {widget.chartType === 'number' && (
                        <div style={{ fontSize: '36px', fontWeight: 700, color: 'var(--primary)' }}>
                            1,234
                        </div>
                    )}
                </div>
            </div>
        );
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
                        <BarChart size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '14px', fontWeight: 700, margin: 0 }}>Dashboard Builder</h2>
                        <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Create custom dashboards with drag-and-drop widgets
                        </p>
                    </div>
                </div>
                <button
                    className="btn btn-primary"
                    onClick={handleAddWidget}
                    style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                >
                    <Plus size={16} />
                    Add Widget
                </button>
            </div>

            {/* Dashboard Grid */}
            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(400px, 1fr))',
                gap: '16px'
            }}>
                {loading ? (
                    <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)', gridColumn: '1 / -1' }}>
                        Loading dashboards...
                    </div>
                ) : widgets.length === 0 ? (
                    <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)', gridColumn: '1 / -1' }}>
                        <BarChart size={48} style={{ margin: '0 auto 16px', opacity: 0.3 }} />
                        <p>No widgets configured</p>
                        <p style={{ fontSize: '12px' }}>Click "Add Widget" to create your first widget</p>
                    </div>
                ) : (
                    widgets.map(widget => renderWidget(widget))
                )}
            </div>

            {/* Add Widget Modal */}
            <Modal
                isOpen={isWidgetModalOpen}
                onClose={() => setIsWidgetModalOpen(false)}
                title="Add Widget"
                size="medium"
                footer={
                    <>
                        <button
                            className="btn btn-secondary"
                            onClick={() => setIsWidgetModalOpen(false)}
                        >
                            Cancel
                        </button>
                        <button
                            className="btn btn-primary"
                            onClick={handleSaveWidget}
                            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                        >
                            <Plus size={16} />
                            Add Widget
                        </button>
                    </>
                }
            >
                <div style={{ maxHeight: '60vh', overflowY: 'auto', paddingRight: '8px' }}>
                    <div className="form-row">
                        <FormInput
                            label="Widget Title"
                            name="title"
                            value={widgetForm.title}
                            onChange={(e) => setWidgetForm(prev => ({ ...prev, title: e.target.value }))}
                            placeholder="e.g., User Statistics"
                        />
                        <div className="form-group">
                            <label className="form-label">Widget Type</label>
                            <select
                                value={widgetForm.widgetType}
                                onChange={(e) => setWidgetForm(prev => ({ ...prev, widgetType: e.target.value }))}
                                className="form-input"
                            >
                                <option value="chart">Chart</option>
                                <option value="number">Number Card</option>
                                <option value="table">Table</option>
                            </select>
                        </div>
                    </div>

                    {widgetForm.widgetType === 'chart' && (
                        <div className="form-group">
                            <label className="form-label">Chart Type</label>
                            <select
                                value={widgetForm.chartType}
                                onChange={(e) => setWidgetForm(prev => ({ ...prev, chartType: e.target.value }))}
                                className="form-input"
                            >
                                <option value="bar">Bar Chart</option>
                                <option value="line">Line Chart</option>
                                <option value="pie">Pie Chart</option>
                                <option value="area">Area Chart</option>
                            </select>
                        </div>
                    )}

                    <div className="form-group">
                        <label className="form-label">SQL Query</label>
                        <textarea
                            value={widgetForm.querySql}
                            onChange={(e) => setWidgetForm(prev => ({ ...prev, querySql: e.target.value }))}
                            placeholder="SELECT COUNT(*) as count FROM sys_user"
                            rows={4}
                            className="form-input"
                            style={{ fontFamily: 'monospace', fontSize: '12px' }}
                        />
                        <small className="form-help">SQL query to fetch widget data</small>
                    </div>

                    <div className="form-group">
                        <label className="form-label">Refresh Interval (seconds)</label>
                        <input
                            type="number"
                            value={widgetForm.refreshInterval}
                            onChange={(e) => setWidgetForm(prev => ({ ...prev, refreshInterval: parseInt(e.target.value) }))}
                            className="form-input"
                            min="30"
                            max="3600"
                        />
                        <small className="form-help">Auto-refresh interval (30-3600 seconds)</small>
                    </div>
                </div>
            </Modal>
        </div>
    );
};

export default DashboardBuilder;
