import { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { CheckCircle, XCircle, AlertTriangle, Info, X } from 'lucide-react';

// Create Toast Context
const ToastContext = createContext(null);

// Toast Provider Component
export const ToastProvider = ({ children }) => {
    const [toasts, setToasts] = useState([]);

    const addToast = useCallback((type, message, duration = 3000) => {
        const id = Date.now();
        setToasts(prev => [...prev, { id, type, message, duration }]);
        
        // Auto-remove after duration
        setTimeout(() => {
            setToasts(prev => prev.filter(t => t.id !== id));
        }, duration);
        
        return id;
    }, []);

    const removeToast = useCallback((id) => {
        setToasts(prev => prev.filter(t => t.id !== id));
    }, []);

    return (
        <ToastContext.Provider value={{ addToast }}>
            {children}
            <ToastContainer toasts={toasts} removeToast={removeToast} />
        </ToastContext.Provider>
    );
};

// Custom hook to use toast
export const useToast = () => {
    const context = useContext(ToastContext);
    if (!context) {
        throw new Error('useToast must be used within ToastProvider');
    }
    return context;
};

// Convenience functions that will be exported
let toastApi = null;

export const setupToastApi = (addToast) => {
    toastApi = addToast;
};

export const toast = {
    success: (message, duration) => toastApi?.('success', message, duration),
    error: (message, duration) => toastApi?.('error', message, duration),
    warning: (message, duration) => toastApi?.('warning', message, duration),
    info: (message, duration) => toastApi?.('info', message, duration),
};

// Toast Container Component
const ToastContainer = ({ toasts, removeToast }) => {
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
            success: { borderLeft: '4px solid #10b981', background: '#10b981' },
            error: { borderLeft: '4px solid #ef4444', background: '#ef4444' },
            warning: { borderLeft: '4px solid #f59e0b', background: '#f59e0b' },
            info: { borderLeft: '4px solid #3b82f6', background: '#3b82f6' },
        };

        return { ...base, ...typeStyles[type] };
    };

    const getColor = (type) => {
        switch (type) {
            case 'success': return '#10b981';
            case 'error': return '#ef4444';
            case 'warning': return '#f59e0b';
            case 'info': return '#3b82f6';
            default: return '#3b82f6';
        }
    };

    return (
        <div style={{
            position: 'fixed',
            top: '20px',
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
                    <span style={{ color: '#fff' }}>
                        {getIcon(toast.type)}
                    </span>
                    <span style={{ 
                        flex: 1, 
                        fontSize: '13px', 
                        fontWeight: 500,
                        color: '#fff'
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
