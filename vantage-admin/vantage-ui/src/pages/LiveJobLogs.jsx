import { useState, useEffect, useRef } from 'react';
import { 
    Terminal, Play, Pause, RotateCcw, Download, Trash2, 
    Filter, Search, Clock, CheckCircle, XCircle, AlertCircle,
    ChevronDown, ChevronUp, Copy, Check
} from 'lucide-react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const LiveJobLogs = () => {
    const [logs, setLogs] = useState([]);
    const [isConnected, setIsConnected] = useState(false);
    const [isPaused, setIsPaused] = useState(false);
    const [autoScroll, setAutoScroll] = useState(true);
    const [filterStatus, setFilterStatus] = useState('all'); // all, success, failed
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedLog, setSelectedLog] = useState(null);
    const [showFilters, setShowFilters] = useState(false);
    const [copiedId, setCopiedId] = useState(null);
    const logsEndRef = useRef(null);
    const wsClientRef = useRef(null);

    // WebSocket connection for live logs
    useEffect(() => {
        const client = new Client({
            webSocketFactory: () => new SockJS('/ws-job'),
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('WebSocket connected for live logs');
                setIsConnected(true);
                
                // Subscribe to job execution logs
                client.subscribe('/topic/job/updates', (message) => {
                    if (!isPaused) {
                        const event = JSON.parse(message.body);
                        if (event.type === 'JOB_STARTED' || event.type === 'JOB_COMPLETED' || event.type === 'JOB_FAILED') {
                            addLog(event);
                        }
                    }
                });
            },
            onDisconnect: () => {
                setIsConnected(false);
            },
            onStompError: (frame) => {
                console.error('WebSocket error:', frame);
            }
        });
        
        client.activate();
        wsClientRef.current = client;

        // Fetch recent logs
        fetchRecentLogs();

        return () => {
            if (wsClientRef.current) {
                wsClientRef.current.deactivate();
            }
        };
    }, [isPaused]);

    // Auto-scroll to bottom when new logs arrive
    useEffect(() => {
        if (autoScroll && logsEndRef.current) {
            logsEndRef.current.scrollIntoView({ behavior: 'smooth' });
        }
    }, [logs, autoScroll]);

    const fetchRecentLogs = () => {
        fetch('/api/system/job-log/list?limit=100')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    const formattedLogs = (data.data || []).map(log => ({
                        id: log.jobLogId,
                        timestamp: log.startTime,
                        jobId: log.jobId,
                        jobName: log.jobName,
                        jobGroup: log.jobGroup,
                        status: log.status === '0' ? 'success' : log.status === '2' ? 'running' : 'failed',
                        message: log.jobMessage,
                        duration: log.executionDuration,
                        retryCount: log.retryCount || 0,
                        exceptionInfo: log.exceptionInfo,
                        invokeTarget: log.invokeTarget,
                        type: log.status === '0' ? 'JOB_COMPLETED' : log.status === '2' ? 'JOB_STARTED' : 'JOB_FAILED'
                    }));
                    setLogs(formattedLogs);
                }
            })
            .catch(err => console.error('Failed to fetch recent logs:', err));
    };

    const addLog = (event) => {
        const newLog = {
            id: event.jobId + '-' + Date.now(),
            timestamp: event.timestamp || new Date().toISOString(),
            jobId: event.jobId,
            jobName: event.jobName,
            jobGroup: event.jobGroup || 'default',
            status: event.type === 'JOB_FAILED' ? 'failed' : event.type === 'JOB_STARTED' ? 'running' : 'success',
            message: event.errorMessage || event.message || 'Execution started',
            duration: event.duration || 0,
            retryCount: 0,
            type: event.type
        };

        setLogs(prev => {
            const updated = [...prev, newLog];
            return updated.slice(-100); // Keep last 100 logs
        });
    };

    const togglePause = () => {
        setIsPaused(!isPaused);
    };

    const clearLogs = () => {
        setLogs([]);
    };

    const exportLogs = () => {
        const blob = new Blob([JSON.stringify(logs, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `job-logs-${new Date().toISOString().split('T')[0]}.json`;
        a.click();
        URL.revokeObjectURL(url);
    };

    const copyLogDetails = (log) => {
        const text = JSON.stringify(log, null, 2);
        navigator.clipboard.writeText(text);
        setCopiedId(log.id);
        setTimeout(() => setCopiedId(null), 2000);
    };

    const getStatusIcon = (status) => {
        switch (status) {
            case 'success': return <CheckCircle size={16} style={{ color: 'var(--success)' }} />;
            case 'failed': return <XCircle size={16} style={{ color: 'var(--danger)' }} />;
            case 'running': return <AlertCircle size={16} style={{ color: 'var(--warning)' }} />;
            default: return <Clock size={16} />;
        }
    };

    const getStatusBadge = (status) => {
        const badges = {
            success: { bg: '#11998e20', color: '#11998e', text: 'Success' },
            failed: { bg: '#f5576c20', color: '#f5576c', text: 'Failed' },
            running: { bg: '#f59e0b20', color: '#f59e0b', text: 'Running' }
        };
        const badge = badges[status] || badges.running;
        return (
            <span style={{
                padding: '4px 10px',
                borderRadius: '12px',
                fontSize: '11px',
                fontWeight: 600,
                background: badge.bg,
                color: badge.color
            }}>
                {badge.text}
            </span>
        );
    };

    const formatDuration = (ms) => {
        if (ms < 1000) return `${ms}ms`;
        return `${(ms / 1000).toFixed(2)}s`;
    };

    const formatTimestamp = (timestamp) => {
        if (!timestamp) return '-';
        const date = new Date(timestamp);
        return date.toLocaleTimeString('en-US', {
            hour12: false,
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    };

    // Determine execution steps based on log data
    const getExecutionSteps = (log) => {
        const steps = [
            { name: 'Job Initialization', detail: null, status: 'pending' },
            { name: 'Report Execution', detail: null, status: 'pending' },
            { name: 'Email Template Processing', detail: null, status: 'pending' },
            { name: 'SQL Data Query', detail: null, status: 'pending' },
            { name: 'Email Sending', detail: null, status: 'pending' },
            { name: 'Completion', detail: null, status: 'pending' }
        ];

        if (log.status === 'success' || log.status === 'failed') {
            // All completed steps
            steps[0].status = 'completed';
            steps[0].detail = 'Job loaded and validated';
            steps[1].status = 'completed';
            steps[1].detail = log.message ? log.message.substring(0, 50) : 'Executed successfully';
            steps[2].status = 'completed';
            steps[2].detail = 'Template variables processed';
            steps[3].status = 'completed';
            steps[3].detail = 'Query executed, data retrieved';
            steps[4].status = 'completed';
            steps[4].detail = 'Email sent successfully';
            steps[5].status = log.status === 'success' ? 'completed' : 'failed';
            steps[5].detail = log.duration ? `Completed in ${formatDuration(log.duration)}` : 'Done';
        } else if (log.status === 'running') {
            steps[0].status = 'completed';
            steps[0].detail = 'Job loaded and validated';
            steps[1].status = 'running';
            steps[1].detail = 'Executing...';
        }

        return steps;
    };

    // Filter logs
    const filteredLogs = logs.filter(log => {
        const matchesStatus = filterStatus === 'all' || log.status === filterStatus;
        const matchesSearch = searchTerm === '' || 
            log.jobName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            log.jobGroup?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            log.message?.toLowerCase().includes(searchTerm.toLowerCase());
        return matchesStatus && matchesSearch;
    });

    return (
        <div style={{
            height: 'calc(100vh - 70px)',
            overflow: 'auto',
            padding: '8px',
            background: 'var(--bg-primary)'
        }}>
            {/* Header */}
            <div style={{ 
                padding: '16px', 
                borderBottom: '1px solid var(--border-color)',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center'
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{
                        width: '36px',
                        height: '36px',
                        borderRadius: '8px',
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <Terminal size={18} />
                    </div>
                    <div>
                        <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 600 }}>Live Job Logs</h3>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '12px', color: 'var(--text-muted)' }}>
                            <span style={{ 
                                width: '8px', 
                                height: '8px', 
                                borderRadius: '50%', 
                                background: isConnected ? '#11998e' : '#f5576c',
                                display: 'inline-block'
                            }} />
                            {isConnected ? 'Connected' : 'Disconnected'}
                            {logs.length > 0 && <span>• {logs.length} logs</span>}
                        </div>
                    </div>
                </div>

                <div style={{ display: 'flex', gap: '8px' }}>
                    <button
                        onClick={togglePause}
                        className="btn btn-secondary"
                        style={{ padding: '8px 12px', fontSize: '12px' }}
                        title={isPaused ? 'Resume' : 'Pause'}
                    >
                        {isPaused ? <Play size={16} /> : <Pause size={16} />}
                    </button>
                    <button
                        onClick={() => setAutoScroll(!autoScroll)}
                        className="btn btn-secondary"
                        style={{ 
                            padding: '8px 12px', 
                            fontSize: '12px',
                            background: autoScroll ? 'var(--primary)' : 'transparent',
                            color: autoScroll ? 'white' : 'var(--text-primary)'
                        }}
                        title="Auto-scroll"
                    >
                        <RotateCcw size={16} />
                    </button>
                    <button
                        onClick={exportLogs}
                        className="btn btn-secondary"
                        style={{ padding: '8px 12px', fontSize: '12px' }}
                        title="Export logs"
                    >
                        <Download size={16} />
                    </button>
                    <button
                        onClick={clearLogs}
                        className="btn btn-secondary"
                        style={{ padding: '8px 12px', fontSize: '12px' }}
                        title="Clear logs"
                    >
                        <Trash2 size={16} />
                    </button>
                </div>
            </div>

            {/* Filters */}
            <div style={{ 
                padding: '12px 16px', 
                borderBottom: '1px solid var(--border-color)',
                background: 'var(--bg-secondary)'
            }}>
                <div style={{ display: 'flex', gap: '12px', alignItems: 'center', flexWrap: 'wrap' }}>
                    <div style={{ position: 'relative', flex: 1, minWidth: '200px' }}>
                        <Search size={16} style={{ 
                            position: 'absolute', 
                            left: '10px', 
                            top: '50%', 
                            transform: 'translateY(-50%)',
                            color: 'var(--text-muted)'
                        }} />
                        <input
                            type="text"
                            placeholder="Search logs..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            style={{
                                width: '100%',
                                padding: '8px 10px 8px 36px',
                                borderRadius: '6px',
                                border: '1px solid var(--border-color)',
                                background: 'var(--bg-primary)',
                                color: 'var(--text-primary)',
                                fontSize: '13px'
                            }}
                        />
                    </div>
                    
                    <button
                        onClick={() => setShowFilters(!showFilters)}
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: '6px',
                            padding: '8px 12px',
                            borderRadius: '6px',
                            border: '1px solid var(--border-color)',
                            background: 'var(--bg-primary)',
                            color: 'var(--text-primary)',
                            cursor: 'pointer',
                            fontSize: '13px'
                        }}
                    >
                        <Filter size={14} />
                        Filters
                        {showFilters ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
                    </button>
                </div>

                {showFilters && (
                    <div style={{ 
                        marginTop: '12px', 
                        display: 'flex', 
                        gap: '8px',
                        paddingTop: '12px',
                        borderTop: '1px solid var(--border-color)'
                    }}>
                        <button
                            onClick={() => setFilterStatus('all')}
                            style={{
                                padding: '6px 12px',
                                borderRadius: '6px',
                                border: 'none',
                                background: filterStatus === 'all' ? 'var(--primary)' : 'var(--bg-tertiary)',
                                color: filterStatus === 'all' ? 'white' : 'var(--text-primary)',
                                cursor: 'pointer',
                                fontSize: '12px',
                                fontWeight: 500
                            }}
                        >
                            All
                        </button>
                        <button
                            onClick={() => setFilterStatus('success')}
                            style={{
                                padding: '6px 12px',
                                borderRadius: '6px',
                                border: 'none',
                                background: filterStatus === 'success' ? 'var(--success)' : 'var(--bg-tertiary)',
                                color: filterStatus === 'success' ? 'white' : 'var(--text-primary)',
                                cursor: 'pointer',
                                fontSize: '12px',
                                fontWeight: 500
                            }}
                        >
                            Success
                        </button>
                        <button
                            onClick={() => setFilterStatus('failed')}
                            style={{
                                padding: '6px 12px',
                                borderRadius: '6px',
                                border: 'none',
                                background: filterStatus === 'failed' ? 'var(--danger)' : 'var(--bg-tertiary)',
                                color: filterStatus === 'failed' ? 'white' : 'var(--text-primary)',
                                cursor: 'pointer',
                                fontSize: '12px',
                                fontWeight: 500
                            }}
                        >
                            Failed
                        </button>
                        <button
                            onClick={() => setFilterStatus('running')}
                            style={{
                                padding: '6px 12px',
                                borderRadius: '6px',
                                border: 'none',
                                background: filterStatus === 'running' ? 'var(--warning)' : 'var(--bg-tertiary)',
                                color: filterStatus === 'running' ? 'white' : 'var(--text-primary)',
                                cursor: 'pointer',
                                fontSize: '12px',
                                fontWeight: 500
                            }}
                        >
                            Running
                        </button>
                    </div>
                )}
            </div>

            {/* Logs List */}
            <div style={{
                flex: 1,
                overflowY: 'auto',
                overflowX: 'hidden',
                padding: '16px',
                background: 'var(--bg-primary)',
                minHeight: 0,
                paddingBottom: '100px'
            }}>
                {filteredLogs.length === 0 ? (
                    <div style={{ 
                        textAlign: 'center', 
                        padding: '60px 20px',
                        color: 'var(--text-muted)'
                    }}>
                        <Terminal size={48} style={{ margin: '0 auto 16px', opacity: 0.3 }} />
                        <p style={{ fontSize: '14px' }}>No logs to display</p>
                        <p style={{ fontSize: '12px', marginTop: '8px' }}>
                            {isPaused ? 'Resume to see live logs' : 'Waiting for job executions...'}
                        </p>
                    </div>
                ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        {filteredLogs.map((log, idx) => (
                            <div
                                key={log.id}
                                onClick={() => setSelectedLog(selectedLog?.id === log.id ? null : log)}
                                style={{
                                    padding: '12px 16px',
                                    borderRadius: '8px',
                                    border: `1px solid ${log.status === 'failed' ? '#f5576c40' : 'var(--border-color)'}`,
                                    background: log.status === 'failed' ? '#f5576c10' : 'var(--bg-secondary)',
                                    cursor: 'pointer',
                                    transition: 'all 0.2s'
                                }}
                            >
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                                    <div style={{ display: 'flex', gap: '12px', alignItems: 'center', flex: 1 }}>
                                        {getStatusIcon(log.status)}
                                        <div style={{ flex: 1 }}>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                                                <span style={{ fontWeight: 600, fontSize: '13px' }}>{log.jobName}</span>
                                                {getStatusBadge(log.status)}
                                                <span style={{ 
                                                    fontSize: '11px', 
                                                    color: 'var(--text-muted)',
                                                    padding: '2px 6px',
                                                    background: 'var(--bg-tertiary)',
                                                    borderRadius: '4px'
                                                }}>
                                                    {log.jobGroup}
                                                </span>
                                            </div>
                                            <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                                                {log.message}
                                            </div>
                                        </div>
                                    </div>
                                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '4px' }}>
                                        <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                                            {formatTimestamp(log.timestamp)}
                                        </span>
                                        {log.duration > 0 && (
                                            <span style={{ 
                                                fontSize: '11px', 
                                                color: 'var(--text-secondary)',
                                                fontFamily: 'monospace'
                                            }}>
                                                {formatDuration(log.duration)}
                                            </span>
                                        )}
                                    </div>
                                </div>

                                {/* Expanded Details */}
                                {selectedLog?.id === log.id && (
                                    <div style={{
                                        marginTop: '12px',
                                        paddingTop: '12px',
                                        borderTop: '1px solid var(--border-color)',
                                        fontSize: '12px'
                                    }}>
                                        {/* Execution Steps Visual */}
                                        <div style={{ marginBottom: '16px' }}>
                                            <div style={{ fontWeight: 600, fontSize: '13px', marginBottom: '12px', color: 'var(--text-primary)' }}>
                                                Execution Steps
                                            </div>
                                            <div style={{ display: 'flex', flexDirection: 'column', gap: '0' }}>
                                                {getExecutionSteps(log).map((step, stepIdx) => (
                                                    <div key={stepIdx} style={{ display: 'flex', gap: '12px' }}>
                                                        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                                                            <div style={{
                                                                width: '24px',
                                                                height: '24px',
                                                                borderRadius: '50%',
                                                                display: 'flex',
                                                                alignItems: 'center',
                                                                justifyContent: 'center',
                                                                background: step.status === 'completed' ? 'var(--success)' :
                                                                    step.status === 'running' ? 'var(--warning)' :
                                                                    step.status === 'failed' ? 'var(--danger)' :
                                                                    'var(--bg-tertiary)',
                                                                color: step.status === 'pending' ? 'var(--text-muted)' : 'white',
                                                                fontSize: '10px',
                                                                fontWeight: 600,
                                                                flexShrink: 0
                                                            }}>
                                                                {step.status === 'completed' ? '✓' :
                                                                 step.status === 'running' ? '⟳' :
                                                                 step.status === 'failed' ? '✕' :
                                                                 (stepIdx + 1)}
                                                            </div>
                                                            {stepIdx < getExecutionSteps(log).length - 1 && (
                                                                <div style={{
                                                                    width: '2px',
                                                                    height: '20px',
                                                                    background: step.status === 'completed' ? 'var(--success)' : 'var(--border-color)',
                                                                    flexShrink: 0
                                                                }} />
                                                            )}
                                                        </div>
                                                        <div style={{ flex: 1, paddingBottom: '8px' }}>
                                                            <div style={{ fontWeight: 500, color: step.status === 'pending' ? 'var(--text-muted)' : 'var(--text-primary)' }}>
                                                                {step.name}
                                                            </div>
                                                            {step.detail && (
                                                                <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '2px' }}>
                                                                    {step.detail}
                                                                </div>
                                                            )}
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        </div>

                                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '12px', marginBottom: '12px' }}>
                                            <div>
                                                <span style={{ color: 'var(--text-muted)' }}>Job ID:</span>
                                                <span style={{ marginLeft: '8px', fontFamily: 'monospace' }}>{log.jobId}</span>
                                            </div>
                                            <div>
                                                <span style={{ color: 'var(--text-muted)' }}>Retry Count:</span>
                                                <span style={{ marginLeft: '8px' }}>{log.retryCount}</span>
                                            </div>
                                            <div>
                                                <span style={{ color: 'var(--text-muted)' }}>Start Time:</span>
                                                <span style={{ marginLeft: '8px' }}>{log.timestamp}</span>
                                            </div>
                                            <div>
                                                <span style={{ color: 'var(--text-muted)' }}>Event Type:</span>
                                                <span style={{ marginLeft: '8px' }}>{log.type}</span>
                                            </div>
                                        </div>
                                        
                                        {log.exceptionInfo && (
                                            <div style={{ 
                                                padding: '12px', 
                                                background: 'var(--bg-tertiary)', 
                                                borderRadius: '6px',
                                                fontFamily: 'monospace',
                                                fontSize: '11px',
                                                whiteSpace: 'pre-wrap',
                                                wordBreak: 'break-all',
                                                maxHeight: '200px',
                                                overflowY: 'auto',
                                                color: 'var(--danger)'
                                            }}>
                                                {log.exceptionInfo}
                                            </div>
                                        )}
                                        
                                        <div style={{ marginTop: '12px', display: 'flex', justifyContent: 'flex-end' }}>
                                            <button
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    copyLogDetails(log);
                                                }}
                                                style={{
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    gap: '6px',
                                                    padding: '6px 12px',
                                                    borderRadius: '6px',
                                                    border: '1px solid var(--border-color)',
                                                    background: 'transparent',
                                                    color: 'var(--text-primary)',
                                                    cursor: 'pointer',
                                                    fontSize: '11px'
                                                }}
                                            >
                                                {copiedId === log.id ? <Check size={14} style={{ color: 'var(--success)' }} /> : <Copy size={14} />}
                                                {copiedId === log.id ? 'Copied' : 'Copy Details'}
                                            </button>
                                        </div>
                                    </div>
                                )}
                            </div>
                        ))}
                        <div ref={logsEndRef} />
                    </div>
                )}
            </div>
        </div>
    );
};

export default LiveJobLogs;
