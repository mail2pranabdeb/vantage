import { useState } from 'react';

const CronBuilder = ({ value, onChange, disabled = false }) => {
    const [mode, setMode] = useState('simple');
    const [second, setSecond] = useState({ type: 'every', value: '', range: '0-59' });
    const [minute, setMinute] = useState({ type: 'every', value: '', range: '0-59' });
    const [hour, setHour] = useState({ type: 'every', value: '', range: '0-23' });
    const [day, setDay] = useState({ type: 'every', value: '', range: '1-31' });
    const [month, setMonth] = useState({ type: 'every', value: '', range: '1-12' });
    const [weekday, setWeekday] = useState({ type: 'every', value: '', range: '0-6' });

    const generateCron = () => {
        const getValue = (field) => {
            if (field.type === 'every') return '*';
            if (field.type === 'interval') return `*/${field.value || '1'}`;
            if (field.type === 'range') return field.range;
            if (field.type === 'specific') return field.value || '*';
            return '*';
        };

        return `${getValue(second)} ${getValue(minute)} ${getValue(hour)} ${getValue(day)} ${getValue(month)} ${getValue(weekday)} ?`;
    };

    const handleFieldChange = (field, setter, newType, newValue = '') => {
        const updated = { ...field, type: newType };
        if (newValue !== '') {
            updated.value = newValue;
        }
        setter(updated);
        
        // Auto-generate cron after a short delay
        setTimeout(() => {
            const cron = generateCron();
            onChange(cron);
        }, 100);
    };

    const FieldEditor = ({ label, field, setter, min, max, examples }) => (
        <div style={{ marginBottom: '12px' }}>
            <label style={{ display: 'block', fontWeight: 600, marginBottom: '6px', fontSize: '13px' }}>{label}</label>
            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', alignItems: 'center' }}>
                <select
                    value={field.type}
                    onChange={(e) => handleFieldChange(field, setter, e.target.value)}
                    disabled={disabled}
                    style={{ padding: '6px 10px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
                >
                    <option value="every">Every</option>
                    <option value="interval">Every X</option>
                    <option value="range">Range</option>
                    <option value="specific">Specific</option>
                </select>
                
                {(field.type === 'interval' || field.type === 'specific') && (
                    <input
                        type="text"
                        value={field.value}
                        onChange={(e) => handleFieldChange(field, setter, field.type, e.target.value)}
                        disabled={disabled}
                        placeholder={field.type === 'interval' ? 'Interval' : `${min}-${max}`}
                        style={{ padding: '6px 10px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px', width: '100px' }}
                    />
                )}
                
                {field.type === 'range' && (
                    <input
                        type="text"
                        value={field.range}
                        onChange={(e) => handleFieldChange(field, setter, field.type, e.target.value)}
                        disabled={disabled}
                        placeholder={`${min}-${max}`}
                        style={{ padding: '6px 10px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px', width: '120px' }}
                    />
                )}
            </div>
            {examples && <small style={{ color: 'var(--text-muted)', fontSize: '11px', marginTop: '4px', display: 'block' }}>{examples}</small>}
        </div>
    );

    const quickPresets = [
        { label: 'Every 5 seconds', cron: '*/5 * * * * * ?' },
        { label: 'Every minute', cron: '0 */1 * * * ?' },
        { label: 'Every 5 minutes', cron: '0 */5 * * * ?' },
        { label: 'Every hour', cron: '0 0 */1 * * ?' },
        { label: 'Every day at midnight', cron: '0 0 0 * * ?' },
        { label: 'Every Monday 9 AM', cron: '0 0 9 ? * MON' },
        { label: '1st of every month', cron: '0 0 0 1 * ?' },
    ];

    return (
        <div style={{ background: 'var(--bg-secondary)', borderRadius: '8px', padding: '16px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                <h4 style={{ margin: 0, fontSize: '14px', fontWeight: 600 }}>Cron Expression Builder</h4>
                <select
                    value={mode}
                    onChange={(e) => setMode(e.target.value)}
                    disabled={disabled}
                    style={{ padding: '4px 8px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '12px' }}
                >
                    <option value="simple">Simple Mode</option>
                    <option value="advanced">Advanced Mode</option>
                </select>
            </div>

            {/* Quick Presets */}
            <div style={{ marginBottom: '16px' }}>
                <label style={{ display: 'block', fontWeight: 600, marginBottom: '6px', fontSize: '13px' }}>Quick Presets</label>
                <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                    {quickPresets.map((preset, idx) => (
                        <button
                            key={idx}
                            type="button"
                            onClick={() => { onChange(preset.cron); }}
                            disabled={disabled}
                            style={{
                                padding: '4px 10px',
                                borderRadius: '6px',
                                border: '1px solid var(--border-color)',
                                background: 'transparent',
                                color: 'var(--text-primary)',
                                fontSize: '11px',
                                cursor: 'pointer',
                                transition: 'all 0.2s'
                            }}
                            onMouseEnter={(e) => {
                                e.target.style.background = 'var(--primary)';
                                e.target.style.borderColor = 'var(--primary)';
                                e.target.style.color = 'white';
                            }}
                            onMouseLeave={(e) => {
                                e.target.style.background = 'transparent';
                                e.target.style.borderColor = 'var(--border-color)';
                                e.target.style.color = 'var(--text-primary)';
                            }}
                        >
                            {preset.label}
                        </button>
                    ))}
                </div>
            </div>

            {mode === 'simple' && (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '12px' }}>
                    <FieldEditor
                        label="Seconds"
                        field={second}
                        setter={setSecond}
                        min={0}
                        max={59}
                        examples="Examples: */5 (every 5 sec), 0-30 (range)"
                    />
                    <FieldEditor
                        label="Minutes"
                        field={minute}
                        setter={setMinute}
                        min={0}
                        max={59}
                        examples="Examples: */10 (every 10 min), 0-30 (range)"
                    />
                    <FieldEditor
                        label="Hours"
                        field={hour}
                        setter={setHour}
                        min={0}
                        max={23}
                        examples="Examples: */2 (every 2 hrs), 9-17 (9AM-5PM)"
                    />
                </div>
            )}

            {/* Preview */}
            <div style={{ marginTop: '16px', padding: '12px', background: 'var(--bg-tertiary)', borderRadius: '6px' }}>
                <label style={{ display: 'block', fontWeight: 600, marginBottom: '6px', fontSize: '13px' }}>Generated Cron Expression</label>
                <code style={{ 
                    display: 'block', 
                    padding: '8px 12px', 
                    background: 'var(--bg-secondary)', 
                    borderRadius: '4px', 
                    fontFamily: 'monospace',
                    fontSize: '14px',
                    color: 'var(--primary)'
                }}>
                    {value || '* * * * * * ?'}
                </code>
            </div>
        </div>
    );
};

export default CronBuilder;
