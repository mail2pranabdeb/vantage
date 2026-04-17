import { Users, Shield, Settings, Activity, TrendingUp, Clock, CheckCircle, AlertTriangle, Calendar, Play, Pause, Zap, ArrowUpRight, BarChart3, Database } from 'lucide-react';
import { useState, useEffect } from 'react';
import TrendChart from '../components/TrendChart';
import HealthGauge from '../components/HealthGauge';

const StatCard = ({ title, value, icon: Icon, color, trend, delay }) => (
    <div className="glass-panel animate-up" style={{ 
        animationDelay: `${delay}ms`,
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
        minHeight: '140px',
        borderLeft: `3px solid ${color}`
    }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <div style={{
                width: '40px', height: '40px', borderRadius: '12px',
                background: `linear-gradient(135deg, ${color}20, ${color}40)`,
                color: color, display: 'flex', alignItems: 'center', justifyContent: 'center',
                boxShadow: `0 4px 12px ${color}20`
            }}>
                <Icon size={22} />
            </div>
            {trend && (
                <div style={{ 
                    display: 'flex', alignItems: 'center', gap: '4px', 
                    padding: '4px 8px', borderRadius: '8px', 
                    background: trend.startsWith('+') ? 'rgba(16, 185, 129, 0.1)' : 'rgba(239, 68, 68, 0.1)',
                    color: trend.startsWith('+') ? '#10b981' : '#ef4444',
                    fontSize: '11px', fontWeight: 700,
                    border: `1px solid ${trend.startsWith('+') ? 'rgba(16, 185, 129, 0.2)' : 'rgba(239, 68, 68, 0.2)'}`
                }}>
                    {trend}
                    <ArrowUpRight size={12} />
                </div>
            )}
        </div>
        
        <div style={{ marginTop: 'auto' }}>
            <p style={{ margin: '0 0 4px 0', fontSize: '11px', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '1px', fontWeight: 600 }}>
                {title}
            </p>
            <h3 style={{ margin: 0, fontSize: '28px', fontWeight: '800', color: 'var(--text-primary)', letterSpacing: '-1px' }}>
                {value}
            </h3>
        </div>
    </div>
);

const ActivityItem = ({ icon: Icon, color, text, time, detail }) => (
    <div style={{
        display: 'flex', alignItems: 'center', gap: '14px', padding: '12px 10px',
        borderBottom: '1px solid var(--border-color)',
        transition: 'all 0.3s ease',
        borderRadius: '8px',
        cursor: 'default'
    }} className="hover-target animate-slide-in">
        <div style={{
            width: '36px', height: '36px', borderRadius: '10px',
            background: `${color}15`, color: color,
            display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
            boxShadow: `0 4px 12px ${color}10`
        }}>
            <Icon size={16} />
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
            <p style={{ margin: 0, fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{text}</p>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '2px' }}>
                <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{time}</span>
                {detail && <span style={{ fontSize: '10px', color: 'var(--primary-color)', background: 'var(--primary-soft)', padding: '1px 6px', borderRadius: '4px' }}>{detail}</span>}
            </div>
        </div>
    </div>
);

const QuickAction = ({ label, icon: Icon, color, onClick }) => (
    <button style={{
        display: 'flex', alignItems: 'center', gap: '10px',
        padding: '12px', border: '1px solid var(--border-color)', borderRadius: '12px',
        background: 'var(--bg-secondary)', cursor: 'pointer',
        transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)', color: 'var(--text-primary)', width: '100%',
        textAlign: 'left'
    }}
        className="glass-panel hover-grow"
        onClick={onClick}
        onMouseEnter={e => { e.currentTarget.style.borderColor = color; e.currentTarget.style.transform = 'translateY(-2px)'; }}
        onMouseLeave={e => { e.currentTarget.style.borderColor = 'var(--border-color)'; e.currentTarget.style.transform = 'translateY(0)'; }}
    >
        <div style={{ color, display: 'flex' }}>
            <Icon size={18} />
        </div>
        <span style={{ fontSize: '13px', fontWeight: 600 }}>{label}</span>
        <ArrowUpRight size={14} style={{ marginLeft: 'auto', opacity: 0.3 }} />
    </button>
);

const Dashboard = ({ tab }) => {
    const [jobMetrics, setJobMetrics] = useState(null);
    const [trendData, setTrendData] = useState([]);
    const [health, setHealth] = useState({ cpu: 0, mem: 0, disk: 0 });
    const [recentLogs, setRecentLogs] = useState([]);
    const [loading, setLoading] = useState(true);

    const formatRelativeTime = (dateStr) => {
        if (!dateStr) return '';
        const date = new Date(dateStr);
        const now = new Date();
        const diffInSeconds = Math.floor((now - date) / 1000);
        
        if (diffInSeconds < 60) return 'Just now';
        if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}m ago`;
        if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}h ago`;
        return date.toLocaleDateString();
    };

    const getLogContext = (log) => {
        // Business Type Mapping: 0=Other, 1=Add, 2=Update, 3=Delete, 4=Grant, 5=Export, 6=Import, 7=Force
        const types = {
            1: { icon: CheckCircle, color: '#10b981', label: 'Added' },
            2: { icon: Zap, color: '#3b82f6', label: 'Updated' },
            3: { icon: AlertTriangle, color: '#ef4444', label: 'Deleted' },
            5: { icon: TrendingUp, color: '#8b5cf6', label: 'Exported' }
        };
        return types[log.businessType] || { icon: Activity, color: '#64748b', label: 'Action' };
    };

    const fetchActuatorMetric = async (name) => {
        try {
            const res = await fetch(`/actuator/metrics/${name}`);
            const data = await res.json();
            return data.measurements?.[0]?.value || 0;
        } catch (e) {
            return 0;
        }
    };

    const updateMetrics = async () => {
        const cpuRaw = await fetchActuatorMetric('system.cpu.usage');
        const memUsed = await fetchActuatorMetric('jvm.memory.used');
        const memMax = await fetchActuatorMetric('jvm.memory.max');
        
        setHealth({
            cpu: Math.min(100, Math.round(cpuRaw * 100)),
            mem: memMax ? Math.round((memUsed / memMax) * 100) : 0,
            disk: 62 // Static fallback for disk
        });
    };

    const fetchRecentLogs = () => {
        fetch('/api/system/operlog/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setRecentLogs((data.data || []).slice(0, 5));
                }
            })
            .catch(err => console.error("Failed to fetch logs:", err));
    };

    useEffect(() => {
        // Initial data load
        fetch('/api/system/job-dashboard/metrics')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) setJobMetrics(data.data);
            });

        fetch('/api/system/job-dashboard/trend?days=7')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) setTrendData(data.data);
            });

        updateMetrics();
        fetchRecentLogs();
        setLoading(false);

        // Real-time polling
        const metricInterval = setInterval(updateMetrics, 5000);
        const logInterval = setInterval(fetchRecentLogs, 10000);
        
        return () => {
            clearInterval(metricInterval);
            clearInterval(logInterval);
        };
    }, []);

    const navigateTo = (title, url) => {
        // Generate a simple ID from the URL or mapping
        const id = url.replace(/\//g, ':').replace(/^:/, '');
        console.log(`Navigating to ${title}: ${url}`);
        window.dispatchEvent(new CustomEvent('navigate-to-page', { 
            detail: { 
                pageConfig: { id, title, url, icon: '⚡' } 
            } 
        }));
    };

    return (
        <div className="animate-fade-in" style={{ 
            display: 'flex', 
            flexDirection: 'column',
            height: '100%',
            overflow: 'auto',
            padding: '16px',
            flex: 1,
            minHeight: 0,
            position: 'relative'
        }}>
            <div className="mesh-container">
                <div className="mesh-blob" style={{ top: '10%', left: '10%', width: '400px', height: '400px', background: 'rgba(59, 130, 246, 0.15)' }}></div>
                <div className="mesh-blob" style={{ top: '60%', left: '70%', width: '350px', height: '350px', background: 'rgba(139, 92, 246, 0.15)' }}></div>
                <div className="mesh-blob" style={{ top: '20%', left: '80%', width: '300px', height: '300px', background: 'rgba(16, 185, 129, 0.1)' }}></div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px', zIndex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                    <div style={{
                        width: '42px', height: '42px', borderRadius: '12px',
                        background: 'linear-gradient(135deg, var(--primary-color), var(--primary-hover))',
                        display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white',
                        boxShadow: '0 8px 16px var(--primary-soft)'
                    }}>
                        <BarChart3 size={24} />
                    </div>
                    <div>
                        <h1 style={{ fontSize: '24px', fontWeight: 800, margin: 0, letterSpacing: '-1px' }}>Command Center</h1>
                        <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-muted)', fontWeight: 500 }}>Global system analytics & telemetry</p>
                    </div>
                </div>
                <div className="status-pill animate-fade-in" style={{ padding: '8px 16px', borderRadius: '12px', background: 'var(--glass-bg)', border: '1px solid var(--glass-border)', boxShadow: 'var(--shadow-glass)' }}>
                    <div className="pulse-dot" style={{ color: '#10b981' }}></div>
                    <span style={{ fontWeight: 800, color: 'var(--text-primary)', fontSize: '11px', letterSpacing: '1px' }}>LIVE TELEMETRY</span>
                </div>
            </div>

            {/* Main Stats Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '20px', marginBottom: '32px' }}>
                <StatCard title="Total Population" value="1,248" icon={Users} color="#3b82f6" trend="+12.5%" delay={0} />
                <StatCard title="Security Groups" value="12" icon={Shield} color="#10b981" delay={100} />
                <StatCard title="System Variables" value="48" icon={Settings} color="#f59e0b" delay={200} />
                <StatCard title="Average Uptime" value="99.98%" icon={Zap} color="#8b5cf6" trend="Healthy" delay={300} />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: '24px', marginBottom: '32px' }}>
                {/* Trend Analytics Section */}
                <div className="glass-panel" style={{ padding: '24px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <BarChart3 size={18} style={{ color: 'var(--primary-color)' }} />
                            <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 700, color: 'var(--text-primary)' }}>Execution Trends</h3>
                        </div>
                        <span style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 600 }}>LAST 7 DAYS</span>
                    </div>
                    <TrendChart data={trendData} height={180} />
                </div>

                {/* System Health Section */}
                <div className="glass-panel" style={{ padding: '24px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                        <h3 style={{ margin: 0, fontSize: '15px', fontWeight: 700, color: 'var(--text-primary)' }}>Engine Vitality</h3>
                        <Activity size={14} style={{ color: 'var(--primary-color)', opacity: 0.5 }} />
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-around', gap: '16px' }}>
                        <HealthGauge value={health.cpu} label="CPU" color="#3b82f6" size={70} />
                        <HealthGauge value={health.mem} label="RAM" color="#8b5cf6" size={70} />
                        <HealthGauge value={health.disk} label="Disk" color="#10b981" size={70} />
                    </div>
                    <div style={{ marginTop: '24px', padding: '12px', borderRadius: '12px', background: 'var(--bg-tertiary)', border: '1px solid var(--border-color)' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                            <Database size={14} style={{ color: 'var(--primary-color)' }} />
                            <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--text-primary)' }}>SYSTEM STATUS</span>
                        </div>
                        <p style={{ margin: 0, fontSize: '10px', color: 'var(--text-muted)', lineHeight: '1.4' }}>
                            Node: {window.location.hostname}<br/>
                            Backend: High Availability Replica
                        </p>
                    </div>
                </div>
            </div>

            {/* Bottom Row: Audit & Components */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: '24px' }}>
                <div className="glass-panel" style={{ padding: '24px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                        <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 700, color: 'var(--text-primary)' }}>Live Audit Timeline</h3>
                        <button className="btn btn-secondary" style={{ padding: '4px 10px', fontSize: '11px' }} onClick={() => navigateTo('Oper Log', '/system/operlog')}>Full Logs</button>
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                        {recentLogs.length > 0 ? (
                            recentLogs.map((log, idx) => {
                                const ctx = getLogContext(log);
                                return (
                                    <ActivityItem 
                                        key={log.operId || idx}
                                        icon={ctx.icon} 
                                        color={ctx.color} 
                                        text={`${log.operName} performed ${log.title}`} 
                                        time={formatRelativeTime(log.operTime)}
                                        detail={ctx.label}
                                    />
                                );
                            })
                        ) : (
                            <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)', fontSize: '13px' }}>
                                No recent activity found
                            </div>
                        )}
                    </div>
                </div>

                <div className="glass-panel" style={{ padding: '20px' }}>
                    <h3 style={{ margin: '0 0 16px 0', fontSize: '15px', fontWeight: 700, color: 'var(--text-primary)' }}>Control Center</h3>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '12px' }}>
                        <QuickAction label="Identity Management" icon={Users} color="#3b82f6" onClick={() => navigateTo('User Management', '/system/user')} />
                        <QuickAction label="Access Controls" icon={Shield} color="#10b981" onClick={() => navigateTo('Role Management', '/system/role')} />
                        <QuickAction label="System Variables" icon={Settings} color="#f59e0b" onClick={() => navigateTo('System Config', '/system/config')} />
                        <QuickAction label="Monitor Hub" icon={Activity} color="#8b5cf6" onClick={() => navigateTo('Scheduled Jobs', '/system/job')} />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
