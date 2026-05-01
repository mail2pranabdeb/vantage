import { useState, useEffect, useCallback } from 'react';
import { Activity, Cpu, HardDrive, Clock, Server, AlertTriangle, TrendingUp, Zap, Database, Layers, Gauge, Thermometer } from 'lucide-react';

const MonitoringDashboard = () => {
    const [health, setHealth] = useState(null);
    const [metrics, setMetrics] = useState(null);
    const [env, setEnv] = useState(null);
    const [exchanges, setExchanges] = useState([]);
    const [threads, setThreads] = useState(null);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState('overview');
    const [lastUpdated, setLastUpdated] = useState(null);
    const [rawExchanges, setRawExchanges] = useState(null);

    const fetchAll = useCallback(async () => {
        try {
            const [healthRes, metricsRes, envRes, exchangesRes, threadsRes] = await Promise.all([
                fetch('/actuator/health?show-details=always'),
                fetch('/actuator/metrics'),
                fetch('/actuator/env'),
                fetch('/actuator/httpexchanges'),
                fetch('/actuator/threaddump'),
            ]);

            if (healthRes.ok) setHealth(await healthRes.json());
            if (metricsRes.ok) setMetrics(await metricsRes.json());
            if (envRes.ok) setEnv(await envRes.json());
            if (threadsRes.ok) setThreads(await threadsRes.json());
            
            if (exchangesRes.ok) {
                const data = await exchangesRes.json();
                setRawExchanges(data);
                const list = data.httpExchanges || data.exchanges || data || [];
                const filtered = Array.isArray(list) ? list.filter(ex => {
                    const uri = ex.request?.uri || ex.uri || '';
                    return !uri.includes('/actuator');
                }) : [];
                setExchanges(filtered);
            }
            setLastUpdated(new Date());
            setLoading(false);
        } catch (err) {
            console.error('Failed to fetch monitoring data', err);
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchAll();
        const interval = setInterval(fetchAll, 10000);
        return () => clearInterval(interval);
    }, [fetchAll]);

    const getMetric = async (name) => {
        try {
            const res = await fetch(`/actuator/metrics/${name}`);
            if (res.ok) return await res.json();
        } catch {}
        return null;
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'UP': return '#10b981';
            case 'DOWN': return '#ef4444';
            case 'OUT_OF_SERVICE': return '#f59e0b';
            default: return '#6b7280';
        }
    };

    const tabs = [
        { id: 'overview', label: 'Overview', icon: Activity },
        { id: 'traffic', label: 'HTTP Traffic', icon: TrendingUp },
        { id: 'threads', label: 'Threads', icon: Layers },
        { id: 'health', label: 'Health', icon: Server },
    ];

    if (loading) {
        return (
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', background: 'var(--bg-primary)' }}>
                <div className="pulse-dot" style={{ color: 'var(--primary-color)' }}></div>
            </div>
        );
    }

    return (
        <div style={{ padding: '20px', height: '100%', overflow: 'auto', background: 'var(--bg-primary)' }}>
            {/* Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <div>
                    <h1 style={{ margin: 0, fontSize: '24px', fontWeight: 700, color: 'var(--text-primary)' }}>Monitoring Dashboard</h1>
                    <p style={{ margin: '4px 0 0', fontSize: '13px', color: 'var(--text-muted)' }}>
                        Last updated: {lastUpdated?.toLocaleTimeString()} · Auto-refresh every 10s
                    </p>
                </div>
                <button onClick={fetchAll} className="btn" style={{ padding: '8px 16px', background: 'var(--primary-color)', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Activity size={16} /> Refresh
                </button>
            </div>

            {/* Tabs */}
            <div style={{ display: 'flex', gap: '8px', marginBottom: '24px', borderBottom: '1px solid var(--border-color)', paddingBottom: '12px' }}>
                {tabs.map(tab => {
                    const Icon = tab.icon;
                    return (
                        <button
                            key={tab.id}
                            onClick={() => setActiveTab(tab.id)}
                            style={{
                                padding: '8px 16px',
                                background: activeTab === tab.id ? 'var(--primary-color)' : 'transparent',
                                color: activeTab === tab.id ? 'white' : 'var(--text-muted)',
                                border: activeTab === tab.id ? 'none' : '1px solid var(--border-color)',
                                borderRadius: '8px',
                                cursor: 'pointer',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '6px',
                                fontSize: '13px',
                                fontWeight: 500,
                                transition: 'all 0.2s'
                            }}
                        >
                            <Icon size={14} /> {tab.label}
                        </button>
                    );
                })}
            </div>

            {/* Overview Tab */}
            {activeTab === 'overview' && <OverviewTab health={health} metrics={metrics} env={env} exchanges={exchanges} threads={threads} getStatusColor={getStatusColor} getMetric={getMetric} />}

            {/* HTTP Traffic Tab */}
            {activeTab === 'traffic' && <TrafficTab exchanges={exchanges} rawExchanges={rawExchanges} />}

            {/* Threads Tab */}
            {activeTab === 'threads' && <ThreadsTab threads={threads} />}

            {/* Health Tab */}
            {activeTab === 'health' && <HealthTab health={health} getStatusColor={getStatusColor} />}
        </div>
    );
};

const OverviewTab = ({ health, metrics, env, exchanges, threads, getStatusColor, getMetric }) => {
    const [memoryMetrics, setMemoryMetrics] = useState(null);
    const [cpuMetrics, setCpuMetrics] = useState(null);
    const [httpMetrics, setHttpMetrics] = useState(null);

    useEffect(() => {
        const load = async () => {
            const [mem, cpu, http] = await Promise.all([
                getMetric('jvm.memory.used'),
                getMetric('system.cpu.usage'),
                getMetric('http.server.requests'),
            ]);
            setMemoryMetrics(mem);
            setCpuMetrics(cpu);
            setHttpMetrics(http);
        };
        load();
    }, [getMetric]);

    const status = health?.status || 'UNKNOWN';
    const uptime = health?.components?.diskSpace?.details?.free || 0;

    return (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '16px' }}>
            {/* Status Card */}
            <div className="glass-panel" style={{ padding: '24px', background: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px' }}>
                    <div style={{ padding: '10px', borderRadius: '10px', background: `${getStatusColor(status)}20` }}>
                        <Server size={24} color={getStatusColor(status)} />
                    </div>
                    <div>
                        <h3 style={{ margin: 0, fontSize: '14px', color: 'var(--text-muted)' }}>Application Status</h3>
                        <p style={{ margin: '4px 0 0', fontSize: '20px', fontWeight: 700, color: getStatusColor(status) }}>{status}</p>
                    </div>
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginTop: '16px' }}>
                    <Stat label="Uptime" value={health?.components?.uptime?.details?.uptime || 'N/A'} icon={Clock} />
                    <Stat label="DB Status" value={health?.components?.db?.status || 'N/A'} icon={Database} />
                </div>
            </div>

            {/* Memory Card */}
            <div className="glass-panel" style={{ padding: '24px', background: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px' }}>
                    <div style={{ padding: '10px', borderRadius: '10px', background: 'rgba(139, 92, 246, 0.15)' }}>
                        <HardDrive size={24} color="#8b5cf6" />
                    </div>
                    <div>
                        <h3 style={{ margin: 0, fontSize: '14px', color: 'var(--text-muted)' }}>Memory Usage</h3>
                        <p style={{ margin: '4px 0 0', fontSize: '20px', fontWeight: 700, color: 'var(--text-primary)' }}>
                            {memoryMetrics ? `${(memoryMetrics.measurements[0]?.value / 1048576).toFixed(0)} MB` : 'Loading...'}
                        </p>
                    </div>
                </div>
                <div style={{ marginTop: '16px' }}>
                    {memoryMetrics?.measurements?.map(m => (
                        <div key={m.statistic} style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', borderBottom: '1px solid var(--border-color)' }}>
                            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{m.statistic}</span>
                            <span style={{ fontSize: '12px', fontWeight: 600 }}>{(m.value / 1048576).toFixed(1)} MB</span>
                        </div>
                    ))}
                </div>
            </div>

            {/* CPU Card */}
            <div className="glass-panel" style={{ padding: '24px', background: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px' }}>
                    <div style={{ padding: '10px', borderRadius: '10px', background: 'rgba(236, 72, 153, 0.15)' }}>
                        <Cpu size={24} color="#ec4899" />
                    </div>
                    <div>
                        <h3 style={{ margin: 0, fontSize: '14px', color: 'var(--text-muted)' }}>CPU Usage</h3>
                        <p style={{ margin: '4px 0 0', fontSize: '20px', fontWeight: 700, color: 'var(--text-primary)' }}>
                            {cpuMetrics ? `${(cpuMetrics.measurements[0]?.value * 100).toFixed(1)}%` : 'Loading...'}
                        </p>
                    </div>
                </div>
                <div style={{ marginTop: '16px' }}>
                    {cpuMetrics?.measurements?.map(m => (
                        <div key={m.statistic} style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', borderBottom: '1px solid var(--border-color)' }}>
                            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{m.statistic}</span>
                            <span style={{ fontSize: '12px', fontWeight: 600 }}>{(m.value * 100).toFixed(2)}%</span>
                        </div>
                    ))}
                </div>
            </div>

            {/* HTTP Requests Card */}
            <div className="glass-panel" style={{ padding: '24px', background: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px' }}>
                    <div style={{ padding: '10px', borderRadius: '10px', background: 'rgba(59, 130, 246, 0.15)' }}>
                        <Zap size={24} color="#3b82f6" />
                    </div>
                    <div>
                        <h3 style={{ margin: 0, fontSize: '14px', color: 'var(--text-muted)' }}>HTTP Requests</h3>
                        <p style={{ margin: '4px 0 0', fontSize: '20px', fontWeight: 700, color: 'var(--text-primary)' }}>
                            {httpMetrics ? httpMetrics.measurements.find(m => m.statistic === 'COUNT')?.value?.toFixed(0) || '0' : '0'}
                        </p>
                    </div>
                </div>
                <div style={{ marginTop: '16px' }}>
                    {httpMetrics?.measurements?.map(m => (
                        <div key={m.statistic} style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', borderBottom: '1px solid var(--border-color)' }}>
                            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{m.statistic}</span>
                            <span style={{ fontSize: '12px', fontWeight: 600 }}>
                                {m.statistic === 'TOTAL_TIME' ? `${(m.value * 1000).toFixed(0)} ms` : m.value.toFixed(2)}
                            </span>
                        </div>
                    ))}
                </div>
            </div>

            {/* Disk Space */}
            <div className="glass-panel" style={{ padding: '24px', background: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px' }}>
                    <div style={{ padding: '10px', borderRadius: '10px', background: 'rgba(16, 185, 129, 0.15)' }}>
                        <Gauge size={24} color="#10b981" />
                    </div>
                    <div>
                        <h3 style={{ margin: 0, fontSize: '14px', color: 'var(--text-muted)' }}>Disk Space</h3>
                        <p style={{ margin: '4px 0 0', fontSize: '20px', fontWeight: 700, color: 'var(--text-primary)' }}>
                            {health?.components?.diskSpace?.details?.free ? `${(health.components.diskSpace.details.free / 1073741824).toFixed(1)} GB` : 'N/A'}
                        </p>
                    </div>
                </div>
                {health?.components?.diskSpace?.details && (
                    <div style={{ marginTop: '16px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', borderBottom: '1px solid var(--border-color)' }}>
                            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Total</span>
                            <span style={{ fontSize: '12px', fontWeight: 600 }}>{(health.components.diskSpace.details.total / 1073741824).toFixed(1)} GB</span>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', borderBottom: '1px solid var(--border-color)' }}>
                            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Free</span>
                            <span style={{ fontSize: '12px', fontWeight: 600 }}>{(health.components.diskSpace.details.free / 1073741824).toFixed(1)} GB</span>
                        </div>
                    </div>
                )}
            </div>

            {/* Thread Count */}
            <div className="glass-panel" style={{ padding: '24px', background: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px' }}>
                    <div style={{ padding: '10px', borderRadius: '10px', background: 'rgba(245, 158, 11, 0.15)' }}>
                        <Thermometer size={24} color="#f59e0b" />
                    </div>
                    <div>
                        <h3 style={{ margin: 0, fontSize: '14px', color: 'var(--text-muted)' }}>Thread States</h3>
                        <p style={{ margin: '4px 0 0', fontSize: '20px', fontWeight: 700, color: 'var(--text-primary)' }}>
                            {threads?.threads?.length || 0}
                        </p>
                    </div>
                </div>
                {threads && (
                    <div style={{ marginTop: '16px' }}>
                        {(() => {
                            const states = {};
                            threads.threads.forEach(t => {
                                states[t.threadState] = (states[t.threadState] || 0) + 1;
                            });
                            return Object.entries(states).map(([state, count]) => (
                                <div key={state} style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', borderBottom: '1px solid var(--border-color)' }}>
                                    <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{state}</span>
                                    <span style={{ fontSize: '12px', fontWeight: 600 }}>{count}</span>
                                </div>
                            ));
                        })()}
                    </div>
                )}
            </div>
        </div>
    );
};

const TrafficTab = ({ exchanges, rawExchanges }) => {
    const methodColors = { GET: '#10b981', POST: '#3b82f6', PUT: '#f59e0b', DELETE: '#ef4444', PATCH: '#8b5cf6' };

    const getRouteInfo = (uri) => {
        if (!uri) return { source: 'Unknown', target: 'Gateway', module: 'Other', icon: '🔹' };
        const u = uri.replace('http://localhost:8080', '');
        
        if (u.startsWith('/api/login') || u.startsWith('/api/logout')) return { source: 'UI', target: 'Gateway', module: 'Security/Auth', icon: '🔐' };
        if (u.startsWith('/api/system/user')) return { source: 'UI', target: 'Gateway', module: 'System: Users', icon: '👤' };
        if (u.startsWith('/api/system/role')) return { source: 'UI', target: 'Gateway', module: 'System: Roles', icon: '🛡️' };
        if (u.startsWith('/api/system/menu')) return { source: 'UI', target: 'Gateway', module: 'System: Menus', icon: '📋' };
        if (u.startsWith('/api/system/dict')) return { source: 'UI', target: 'Gateway', module: 'System: Dictionary', icon: '📖' };
        if (u.startsWith('/api/system/config')) return { source: 'UI', target: 'Gateway', module: 'System: Config', icon: '⚙️' };
        if (u.startsWith('/api/system/notice')) return { source: 'UI', target: 'Gateway', module: 'System: Notices', icon: '📢' };
        if (u.startsWith('/api/system/operlog') || u.startsWith('/api/system/logininfor')) return { source: 'UI', target: 'Gateway', module: 'System: Logs', icon: '📜' };
        if (u.startsWith('/api/system/cache')) return { source: 'UI', target: 'Gateway', module: 'System: Cache', icon: '💾' };
        
        if (u.startsWith('/api/system/job') || u.startsWith('/api/public/job')) return { source: 'UI/Ext', target: 'Gateway', module: 'Quartz: Jobs', icon: '⏱️' };
        if (u.startsWith('/api/system/report') || u.startsWith('/api/system/dashboard')) return { source: 'UI', target: 'Gateway', module: 'Reporting', icon: '📊' };
        if (u.startsWith('/api/system/email')) return { source: 'UI', target: 'Gateway', module: 'Quartz: Email', icon: '📧' };
        
        if (u.startsWith('/tool/gen') || u.startsWith('/api/tool')) return { source: 'UI', target: 'Gateway', module: 'Code Generator', icon: '🔧' };
        
        if (u.startsWith('/api/me')) return { source: 'UI', target: 'Gateway', module: 'Session/Auth', icon: '👤' };
        if (u.startsWith('/api/system/dashboards')) return { source: 'UI', target: 'Gateway', module: 'Dashboard Builder', icon: '📈' };
        if (u.startsWith('/api/system/holiday')) return { source: 'UI', target: 'Gateway', module: 'System: Holiday', icon: '📅' };
        if (u.startsWith('/api/system/datasource')) return { source: 'UI', target: 'Gateway', module: 'System: Datasource', icon: '🗄️' };
        
        if (u.startsWith('/ws')) return { source: 'UI', target: 'Gateway', module: 'WebSocket', icon: '🔌' };
        if (u.startsWith('/h2-console')) return { source: 'Browser', target: 'H2 Console', module: 'Database', icon: '🗃️' };
        if (u.startsWith('/static') || u.endsWith('.js') || u.endsWith('.css')) return { source: 'Browser', target: 'Static Assets', module: 'Frontend', icon: '🎨' };
        if (u === '/login' || u === '/') return { source: 'Browser', target: 'Gateway', module: 'Navigation', icon: '🧭' };
        
        return { source: 'UI', target: 'Gateway', module: 'Other/Unknown', icon: '🔹' };
    };

    const getDuration = (ex) => {
        if (ex.timeTaken) return (ex.timeTaken / 1_000_000).toFixed(0);
        if (ex.duration) return (ex.duration / 1_000_000).toFixed(0);
        return 'N/A';
    };

    const getStatus = (ex) => ex.response?.status || ex.status || 'N/A';
    const getUri = (ex) => {
        const uri = ex.request?.uri || ex.uri || '';
        return uri.replace('http://localhost:8080', '');
    };
    const getMethod = (ex) => ex.request?.method || ex.method || 'N/A';
    const getTimestamp = (ex) => ex.timestamp || ex.time || new Date().toISOString();

    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {/* Call Flow Summary */}
            <div className="glass-panel" style={{ padding: '20px', background: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: '12px' }}>
                <h3 style={{ margin: '0 0 16px', fontSize: '16px', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Activity size={18} color="#10b981" /> Request Flow Architecture
                </h3>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '12px', padding: '24px', background: 'var(--bg-primary)', borderRadius: '10px', flexWrap: 'wrap' }}>
                    <div style={{ padding: '12px 20px', background: 'linear-gradient(135deg, #3b82f6, #6366f1)', borderRadius: '10px', color: 'white', fontWeight: 700, fontSize: '14px', boxShadow: '0 4px 15px rgba(59,130,246,0.3)' }}>
                        🖥 Browser UI
                    </div>
                    <svg width="60" height="20"><line x1="0" y1="10" x2="50" y2="10" stroke="#6b7280" strokeWidth="2"/><polygon points="50,5 55,10 50,15" fill="#6b7280"/></svg>
                    <div style={{ padding: '12px 20px', background: 'linear-gradient(135deg, #f59e0b, #ef4444)', borderRadius: '10px', color: 'white', fontWeight: 700, fontSize: '14px', boxShadow: '0 4px 15px rgba(245,158,11,0.3)' }}>
                        🚪 Gateway (Port 8080)
                    </div>
                    <svg width="60" height="20"><line x1="0" y1="10" x2="50" y2="10" stroke="#6b7280" strokeWidth="2"/><polygon points="50,5 55,10 50,15" fill="#6b7280"/></svg>
                    <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', justifyContent: 'center' }}>
                        {['⚙ System', '⏱ Quartz', '📊 Reports', '🔧 Generator'].map((m, i) => (
                            <div key={i} style={{ padding: '8px 14px', background: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: '8px', color: 'var(--text-primary)', fontWeight: 600, fontSize: '12px' }}>
                                {m}
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* Live Traffic Table */}
            <div className="glass-panel" style={{ padding: '20px', background: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: '12px', overflow: 'auto' }}>
                <h3 style={{ margin: '0 0 16px', fontSize: '16px', fontWeight: 600 }}>Live HTTP Traffic (Last {exchanges.length})</h3>
                <div style={{ overflowX: 'auto' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px' }}>
                        <thead>
                            <tr style={{ borderBottom: '2px solid var(--border-color)' }}>
                                <th style={{ textAlign: 'left', padding: '10px', color: 'var(--text-muted)', width: '80px' }}>Method</th>
                                <th style={{ textAlign: 'left', padding: '10px', color: 'var(--text-muted)' }}>URI</th>
                                <th style={{ textAlign: 'left', padding: '10px', color: 'var(--text-muted)', width: '200px' }}>Flow</th>
                                <th style={{ textAlign: 'center', padding: '10px', color: 'var(--text-muted)', width: '80px' }}>Status</th>
                                <th style={{ textAlign: 'right', padding: '10px', color: 'var(--text-muted)', width: '100px' }}>Duration</th>
                                <th style={{ textAlign: 'left', padding: '10px', color: 'var(--text-muted)', width: '100px' }}>Time</th>
                            </tr>
                        </thead>
                        <tbody>
                            {exchanges.slice().reverse().slice(0, 100).map((ex, i) => {
                                const method = getMethod(ex);
                                const uri = getUri(ex);
                                const status = getStatus(ex);
                                const duration = getDuration(ex);
                                const timestamp = getTimestamp(ex);
                                const route = getRouteInfo(uri);
                                return (
                                    <tr key={i} style={{ borderBottom: '1px solid var(--border-color)' }}>
                                        <td style={{ padding: '10px' }}>
                                            <span style={{ padding: '3px 8px', borderRadius: '4px', background: `${methodColors[method] || '#6b7280'}20`, color: methodColors[method] || '#6b7280', fontWeight: 600, fontSize: '11px' }}>
                                                {method}
                                            </span>
                                        </td>
                                        <td style={{ padding: '10px', fontFamily: 'monospace', fontSize: '12px', color: 'var(--text-primary)' }}>{uri}</td>
                                        <td style={{ padding: '10px' }}>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '11px' }}>
                                                <span style={{ padding: '2px 6px', borderRadius: '4px', background: 'rgba(59,130,246,0.15)', color: '#60a5fa' }}>{route.source}</span>
                                                <span style={{ color: 'var(--text-muted)' }}>→</span>
                                                <span style={{ padding: '2px 6px', borderRadius: '4px', background: 'rgba(245,158,11,0.15)', color: '#fbbf24' }}>{route.target}</span>
                                                <span style={{ color: 'var(--text-muted)' }}>→</span>
                                                <span style={{ padding: '2px 6px', borderRadius: '4px', background: 'rgba(16,185,129,0.15)', color: '#10b981' }}>{route.icon} {route.module}</span>
                                            </div>
                                        </td>
                                        <td style={{ padding: '10px', textAlign: 'center' }}>
                                            <span style={{
                                                padding: '3px 8px', borderRadius: '4px',
                                                background: status >= 400 ? 'rgba(239,68,68,0.15)' : 'rgba(16,185,129,0.15)',
                                                color: status >= 400 ? '#ef4444' : '#10b981', fontWeight: 600, fontSize: '11px'
                                            }}>
                                                {status}
                                            </span>
                                        </td>
                                        <td style={{ padding: '10px', textAlign: 'right', fontFamily: 'monospace', fontSize: '12px' }}>{duration}ms</td>
                                        <td style={{ padding: '10px', color: 'var(--text-muted)', fontSize: '11px' }}>{new Date(timestamp).toLocaleTimeString()}</td>
                                    </tr>
                                );
                            })}
                            {exchanges.length === 0 && (
                                <tr>
                                    <td colSpan={6} style={{ padding: '20px', textAlign: 'center', color: 'var(--text-muted)' }}>
                                        No exchanges recorded. Make some requests first.
                                        {rawExchanges && <pre style={{ marginTop: '8px', fontSize: '10px', color: 'var(--text-muted)' }}>{JSON.stringify(rawExchanges, null, 2)}</pre>}
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

const ThreadsTab = ({ threads }) => {
    if (!threads) return <div style={{ padding: '20px', color: 'var(--text-muted)' }}>Loading thread data...</div>;

    const stateColors = { RUNNABLE: '#10b981', TIMED_WAITING: '#f59e0b', WAITING: '#6b7280', BLOCKED: '#ef4444', NEW: '#3b82f6', TERMINATED: '#6b7280' };

    return (
        <div className="glass-panel" style={{ padding: '20px', background: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: '12px', overflow: 'auto' }}>
            <h3 style={{ margin: '0 0 16px', fontSize: '16px', fontWeight: 600 }}>Thread Dump ({threads.threads?.length || 0} threads)</h3>
            <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px' }}>
                    <thead>
                        <tr style={{ borderBottom: '2px solid var(--border-color)' }}>
                            <th style={{ textAlign: 'left', padding: '10px', color: 'var(--text-muted)' }}>Thread Name</th>
                            <th style={{ textAlign: 'center', padding: '10px', color: 'var(--text-muted)' }}>State</th>
                            <th style={{ textAlign: 'left', padding: '10px', color: 'var(--text-muted)' }}>Stack Trace</th>
                        </tr>
                    </thead>
                    <tbody>
                        {threads.threads?.slice(0, 50).map((t, i) => (
                            <tr key={i} style={{ borderBottom: '1px solid var(--border-color)' }}>
                                <td style={{ padding: '10px', fontFamily: 'monospace', fontSize: '12px', color: 'var(--text-primary)' }}>{t.threadName}</td>
                                <td style={{ padding: '10px', textAlign: 'center' }}>
                                    <span style={{ padding: '3px 8px', borderRadius: '4px', background: `${stateColors[t.threadState] || '#6b7280'}20`, color: stateColors[t.threadState] || '#6b7280', fontWeight: 600, fontSize: '11px' }}>
                                        {t.threadState}
                                    </span>
                                </td>
                                <td style={{ padding: '10px', fontFamily: 'monospace', fontSize: '11px', color: 'var(--text-muted)', maxWidth: '400px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                    {t.stackTrace[0]?.className}.{t.stackTrace[0]?.methodName}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

const HealthTab = ({ health, getStatusColor }) => {
    if (!health) return <div style={{ padding: '20px', color: 'var(--text-muted)' }}>Loading health data...</div>;

    return (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '16px' }}>
            {Object.entries(health.components || {}).map(([name, component]) => (
                <div key={name} className="glass-panel" style={{ padding: '20px', background: 'var(--bg-secondary)', border: '1px solid var(--border-color)', borderRadius: '12px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                        <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 600, color: 'var(--text-primary)' }}>{name}</h3>
                        <span style={{ padding: '4px 12px', borderRadius: '6px', background: `${getStatusColor(component.status)}20`, color: getStatusColor(component.status), fontWeight: 600, fontSize: '12px' }}>
                            {component.status}
                        </span>
                    </div>
                    {component.details && (
                        <div style={{ display: 'grid', gap: '8px' }}>
                            {Object.entries(component.details).map(([key, value]) => (
                                <div key={key} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid var(--border-color)' }}>
                                    <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>{key}</span>
                                    <span style={{ fontSize: '13px', fontWeight: 600, fontFamily: typeof value === 'number' ? 'monospace' : 'inherit' }}>
                                        {typeof value === 'number' ? (value > 10000 ? `${(value / 1073741824).toFixed(1)} GB` : value) : String(value)}
                                    </span>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            ))}
        </div>
    );
};

const Stat = ({ label, value, icon: Icon }) => (
    <div style={{ padding: '12px', borderRadius: '8px', background: 'var(--bg-primary)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
            <Icon size={14} color="var(--text-muted)" />
            <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{label}</span>
        </div>
        <p style={{ margin: 0, fontSize: '16px', fontWeight: 700, color: 'var(--text-primary)' }}>{value}</p>
    </div>
);

export default MonitoringDashboard;
