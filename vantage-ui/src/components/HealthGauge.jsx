import React from 'react';

const HealthGauge = ({ value, label, color = 'var(--primary-color)', size = 80 }) => {
    const radius = size / 2.5;
    const circumference = 2 * Math.PI * radius;
    const strokeDashoffset = circumference - (value / 100) * circumference;

    return (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px' }}>
            <div style={{ position: 'relative', width: size, height: size }}>
                <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
                    {/* Background Circle */}
                    <circle
                        cx={size / 2}
                        cy={size / 2}
                        r={radius}
                        stroke="var(--bg-tertiary)"
                        strokeWidth="6"
                        fill="transparent"
                    />
                    {/* Progress Circle */}
                    <circle
                        cx={size / 2}
                        cy={size / 2}
                        r={radius}
                        stroke={color}
                        strokeWidth="6"
                        fill="transparent"
                        strokeDasharray={circumference}
                        style={{ 
                            strokeDashoffset, 
                            transition: 'stroke-dashoffset 1s ease-out',
                            transform: 'rotate(-90deg)',
                            transformOrigin: '50% 50%'
                        }}
                        strokeLinecap="round"
                    />
                </svg>
                <div style={{
                    position: 'absolute', top: '50%', left: '50%',
                    transform: 'translate(-50%, -50%)',
                    fontSize: '14px', fontWeight: 800, color: 'var(--text-primary)'
                }}>
                    {Math.round(value)}%
                </div>
            </div>
            <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                {label}
            </span>
        </div>
    );
};

export default HealthGauge;
