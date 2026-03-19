import { useEffect, useState } from 'react';
import { CheckCircle, XCircle, AlertTriangle, Info, X } from 'lucide-react';

// Toast context for global access
let toastListeners = [];

export const toast = {
    success: (message, duration = 3000) => notify('success', message, duration),
    error: (message, duration = 5000) => notify('error', message, duration),
    warning: (message, duration = 4000) => notify('warning', message, duration),
    info: (message, duration = 3000) => notify('info', message, duration),
};

function notify(type, message, duration) {
    const id = Date.now();
    toastListeners.forEach(listener => listener({ id, type, message, duration }));
    return id;
}

const ToastContainer = () => {
    const [toasts, setToasts] = useState([]);

    useEffect(() => {
        const listener = (newToast) => {
            setToasts(prev => [...prev, newToast]);
            // Auto-remove after duration
            setTimeout(() => {
                setToasts(prev => prev.filter(t => t.id !== newToast.id));
            }, newToast.duration);
        };

        toastListeners.push(listener);
        return () => {
            toastListeners = toastListeners.filter(l => l !== listener);
        };
    }, []);

    const removeToast = (id) => {
        setToasts(prev => prev.filter(t => t.id !== id));
    };

    const getIcon = (type) => {
        switch (type) {
            case 'success': return <CheckCircle size={20} />;
            case 'error': return <XCircle size={20} />;
            case 'warning': return <AlertTriangle size={20} />;
            case 'info': return <Info size={20} />;
            default: return <Info size={20} />;
        }
    };

    const getStyles = (type) => {
        const base = {
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            padding: '14px 16px',
            borderRadius: '10px',
            boxShadow: '0 8px 24px rgba(0, 0, 0, 0.15)',
            background: 'var(--bg-secondary)',
            border: '1px solid var(--border-color)',
            minWidth: '320px',
            maxWidth: '450px',
            animation: 'slideInRight 0.3s ease',
        };

        const typeStyles = {
            success: { borderLeft: '4px solid #10b981', background: 'rgba(16, 185, 129, 0.05)' },
            error: { borderLeft: '4px solid #ef4444', background: 'rgba(239, 68, 68, 0.05)' },
            warning: { borderLeft: '4px solid #f59e0b', background: 'rgba(245, 158, 11, 0.05)' },
            info: { borderLeft: '4px solid #3b82f6', background: 'rgba(59, 130, 246, 0.05)' },
        };

        return { ...base, ...typeStyles[type] };
    };

    return (
        <div style={{
            position: 'fixed',
            top: '80px',
            right: '24px',
            zIndex: 9999,
            display: 'flex',
            flexDirection: 'column',
            gap: '10px',
            pointerEvents: 'none'
        }}>
            {toasts.map((toast) => (
                <div
                    key={toast.id}
                    style={{
                        ...getStyles(toast.type),
                        pointerEvents: 'auto'
                    }}
                >
                    <span style={{ 
                        color: toast.type === 'error' ? '#ef4444' : 
                               toast.type === 'success' ? '#10b981' : 
                               toast.type === 'warning' ? '#f59e0b' : '#3b82f6' 
                    }}>
                        {getIcon(toast.type)}
                    </span>
                    <span style={{ 
                        flex: 1, 
                        fontSize: '13px', 
                        fontWeight: 500,
                        color: 'var(--text-primary)'
                    }}>
                        {toast.message}
                    </span>
                    <button
                        onClick={() => removeToast(toast.id)}
                        style={{
                            background: 'transparent',
                            border: 'none',
                            cursor: 'pointer',
                            padding: '4px',
                            display: 'flex',
                            alignItems: 'center',
                            color: 'var(--text-muted)',
                            opacity: 0.7
                        }}
                    >
                        <X size={16} />
                    </button>
                </div>
            ))}

            <style>{`
                @keyframes slideInRight {
                    from {
                        opacity: 0;
                        transform: translateX(100%);
                    }
                    to {
                        opacity: 1;
                        transform: translateX(0);
                    }
                }
            `}</style>
        </div>
    );
};

export default ToastContainer;
