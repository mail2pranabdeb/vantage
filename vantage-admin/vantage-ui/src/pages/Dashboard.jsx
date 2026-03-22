import { Users, Shield, Settings, Activity, TrendingUp, Clock, CheckCircle, AlertTriangle } from 'lucide-react';

const StatCard = ({ title, value, icon: Icon, color, trend }) => (
    <div className="glass-panel" style={{
        padding: '20px',
        display: 'flex',
        alignItems: 'center',
        gap: '14px',
        transition: 'transform 0.2s ease, box-shadow 0.2s ease',
        cursor: 'default'
    }}
        onMouseEnter={e => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 12px 40px rgba(31,38,135,0.12)'; }}
        onMouseLeave={e => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = 'var(--shadow-glass)'; }}
    >
        <div style={{
            width: '42px', height: '42px', borderRadius: '10px',
            background: `linear-gradient(135deg, ${color}18, ${color}30)`,
            color: color, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0
        }}>
            <Icon size={20} />
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
            <p style={{ margin: 0, fontSize: '12px', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.5px', fontWeight: 500 }}>{title}</p>
            <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px' }}>
                <h3 style={{ margin: 0, fontSize: '22px', fontWeight: '700', color: 'var(--text-primary)' }}>{value}</h3>
                {trend && <span style={{ fontSize: '11px', color: '#10b981', fontWeight: 600 }}>{trend}</span>}
            </div>
        </div>
    </div>
);

const ActivityItem = ({ icon: Icon, color, text, time }) => (
    <div style={{
        display: 'flex', alignItems: 'center', gap: '12px', padding: '10px 0',
        borderBottom: '1px solid var(--border-color)'
    }}>
        <div style={{
            width: '32px', height: '32px', borderRadius: '8px',
            background: `${color}15`, color: color,
            display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0
        }}>
            <Icon size={15} />
        </div>
        <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-primary)', flex: 1 }}>{text}</p>
        <span style={{ fontSize: '11px', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>{time}</span>
    </div>
);

const QuickAction = ({ label, icon: Icon, color }) => (
    <button style={{
        display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px',
        padding: '16px 12px', border: '1px solid var(--border-color)', borderRadius: '12px',
        background: 'var(--bg-secondary)', cursor: 'pointer',
        transition: 'all 0.2s ease', color: 'var(--text-primary)', flex: '1'
    }}
        onMouseEnter={e => { e.currentTarget.style.borderColor = color; e.currentTarget.style.transform = 'translateY(-1px)'; }}
        onMouseLeave={e => { e.currentTarget.style.borderColor = 'var(--border-color)'; e.currentTarget.style.transform = 'translateY(0)'; }}
    >
        <Icon size={18} style={{ color }} />
        <span style={{ fontSize: '12px', fontWeight: 500 }}>{label}</span>
    </button>
);

const Dashboard = () => {
    return (
        <div className="animate-fade-in">
            <div style={{ marginBottom: '20px' }}>
                <h1 style={{ fontSize: '20px', marginBottom: '4px' }}>Dashboard</h1>
                <p style={{ margin: 0, fontSize: '13px', color: 'var(--text-muted)' }}>System overview and quick actions</p>
            </div>

            {/* Stat Cards */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: '20px' }}>
                <StatCard title="Users" value="1,248" icon={Users} color="#3b82f6" trend="+12%" />
                <StatCard title="Roles" value="12" icon={Shield} color="#10b981" />
                <StatCard title="Configs" value="48" icon={Settings} color="#f59e0b" />
                <StatCard title="Uptime" value="99.9%" icon={Activity} color="#8b5cf6" trend="Healthy" />
            </div>

            {/* Bottom row: Activity + Quick Actions */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 300px', gap: '16px' }}>
                {/* Recent Activity */}
                <div className="glass-panel" style={{ padding: '20px' }}>
                    <h3 style={{ margin: '0 0 12px 0', fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)' }}>Recent Activity</h3>
                    <div>
                        <ActivityItem icon={CheckCircle} color="#10b981" text="Database migrated to H2 (Oracle Mode)" time="Just now" />
                        <ActivityItem icon={Shield} color="#3b82f6" text="Security upgraded to Spring Security 6" time="2m ago" />
                        <ActivityItem icon={TrendingUp} color="#8b5cf6" text="MyBatis replaced with JdbcTemplate" time="5m ago" />
                        <ActivityItem icon={CheckCircle} color="#10b981" text="Frontend modernized with React & Vite" time="10m ago" />
                        <ActivityItem icon={AlertTriangle} color="#f59e0b" text="Legacy Thymeleaf templates removed" time="15m ago" />
                    </div>
                </div>

                {/* Quick Actions */}
                <div className="glass-panel" style={{ padding: '20px' }}>
                    <h3 style={{ margin: '0 0 12px 0', fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)' }}>Quick Actions</h3>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                        <QuickAction label="Users" icon={Users} color="#3b82f6" />
                        <QuickAction label="Roles" icon={Shield} color="#10b981" />
                        <QuickAction label="Config" icon={Settings} color="#f59e0b" />
                        <QuickAction label="Health" icon={Activity} color="#8b5cf6" />
                    </div>
                    <div style={{ marginTop: '16px', padding: '12px', borderRadius: '10px', background: 'linear-gradient(135deg, rgba(59,130,246,0.08), rgba(139,92,246,0.08))', border: '1px solid rgba(59,130,246,0.15)' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                            <Clock size={14} style={{ color: 'var(--primary-color)' }} />
                            <span style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary)' }}>System Status</span>
                        </div>
                        <p style={{ margin: 0, fontSize: '11px', color: 'var(--text-muted)' }}>All services operational. Last checked 1m ago.</p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
