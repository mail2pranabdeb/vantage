import React, { useMemo } from 'react';

const TrendChart = ({ data, height = 200 }) => {
    // Generate SVG path for the line chart
    const points = useMemo(() => {
        if (!data || data.length < 2) return null;

        const maxTotal = Math.max(...data.map(d => d.total), 1);
        const padding = 20;
        const chartHeight = height - padding * 2;
        const width = 100 / (data.length - 1);

        const totalPoints = data.map((d, i) => {
            const x = i * width;
            const y = height - (padding + (d.total / maxTotal) * chartHeight);
            return `${x},${y}`;
        }).join(' ');

        const successPoints = data.map((d, i) => {
            const x = i * width;
            const y = height - (padding + (d.success / maxTotal) * chartHeight);
            return `${x},${y}`;
        }).join(' ');

        return { totalPoints, successPoints };
    }, [data, height]);

    if (!data || data.length === 0) {
        return (
            <div style={{ height, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)', fontSize: '13px' }}>
                No trend data available
            </div>
        );
    }

    return (
        <div style={{ width: '100%', height, position: 'relative' }}>
            <svg width="100%" height="100%" viewBox={`0 0 100 ${height}`} preserveAspectRatio="none">
                {/* Background Grid Lines */}
                {[0, 0.25, 0.5, 0.75, 1].map((p, i) => (
                    <line 
                        key={i}
                        x1="0" y1={height - (20 + p * (height - 40))} 
                        x2="100" y2={height - (20 + p * (height - 40))} 
                        stroke="var(--border-color)" 
                        strokeWidth="0.1" 
                    />
                ))}

                {/* Success Line */}
                <polyline
                    fill="none"
                    stroke="#10b981"
                    strokeWidth="1.5"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    points={points?.successPoints}
                    style={{ transition: 'all 0.5s ease' }}
                />

                {/* Total Line */}
                <polyline
                    fill="none"
                    stroke="var(--primary-color)"
                    strokeWidth="1.5"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeDasharray="2,2"
                    points={points?.totalPoints}
                    style={{ transition: 'all 0.5s ease' }}
                />

                {/* Area Gradient */}
                <linearGradient id="chartGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="var(--primary-color)" stopOpacity="0.1" />
                    <stop offset="100%" stopColor="var(--primary-color)" stopOpacity="0" />
                </linearGradient>
            </svg>
            
            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '8px' }}>
                <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>{data[0].date}</span>
                <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>{data[data.length - 1].date}</span>
            </div>

            <div style={{ display: 'flex', gap: '16px', justifyContent: 'center', marginTop: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: 'var(--primary-color)' }}></div>
                    <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-secondary)' }}>Total Runs</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#10b981' }}></div>
                    <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-secondary)' }}>Success</span>
                </div>
            </div>
        </div>
    );
};

export default TrendChart;
