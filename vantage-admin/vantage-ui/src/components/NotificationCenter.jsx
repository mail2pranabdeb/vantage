import { useState, useEffect } from 'react';
import { Bell, Check, CheckCheck, Trash2, AlertCircle, Info, AlertTriangle, XCircle } from 'lucide-react';
import { useToast } from '../components/Toast';

const NotificationCenter = () => {
    const { addToast } = useToast();
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [isOpen, setIsOpen] = useState(false);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        fetchNotifications();
        // Poll for new notifications every 30 seconds
        const interval = setInterval(fetchNotifications, 30000);
        return () => clearInterval(interval);
    }, []);

    const fetchNotifications = () => {
        setLoading(true);
        fetch('/api/system/notifications/list?pageNum=1&pageSize=10')
            .then(res => res.json())
            .then(data => {
                setLoading(false);
                if (data.code === 200) {
                    setNotifications(data.data.rows || []);
                    setUnreadCount(data.data.unreadCount || 0);
                }
            })
            .catch(err => {
                console.error("Failed to fetch notifications:", err);
                setLoading(false);
            });
    };

    const markAsRead = (id) => {
        fetch(`/api/system/notifications/${id}/read`, { method: 'PUT' })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setUnreadCount(prev => Math.max(0, prev - 1));
                    fetchNotifications();
                }
            });
    };

    const markAllAsRead = () => {
        fetch('/api/system/notifications/read-all', { method: 'PUT' })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setUnreadCount(0);
                    fetchNotifications();
                }
            });
    };

    const getNotificationIcon = (type) => {
        switch (type) {
            case 'SUCCESS':
                return <CheckCircle size={18} style={{ color: 'var(--success)' }} />;
            case 'WARNING':
                return <AlertTriangle size={18} style={{ color: 'var(--warning)' }} />;
            case 'ERROR':
                return <XCircle size={18} style={{ color: 'var(--danger)' }} />;
            default:
                return <Info size={18} style={{ color: 'var(--primary)' }} />;
        }
    };

    const formatTime = (timestamp) => {
        if (!timestamp) return '';
        const date = new Date(timestamp);
        const now = new Date();
        const diff = now - date;
        const minutes = Math.floor(diff / 60000);
        const hours = Math.floor(diff / 3600000);
        const days = Math.floor(diff / 86400000);

        if (minutes < 1) return 'Just now';
        if (minutes < 60) return `${minutes}m ago`;
        if (hours < 24) return `${hours}h ago`;
        if (days < 7) return `${days}d ago`;
        return date.toLocaleDateString();
    };

    return (
        <div style={{ position: 'relative' }}>
            <button
                onClick={() => setIsOpen(!isOpen)}
                style={{
                    position: 'relative',
                    background: 'transparent',
                    border: 'none',
                    cursor: 'pointer',
                    padding: '8px',
                    color: 'var(--text-primary)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center'
                }}
            >
                <Bell size={20} />
                {unreadCount > 0 && (
                    <span style={{
                        position: 'absolute',
                        top: '4px',
                        right: '4px',
                        background: 'var(--danger)',
                        color: 'white',
                        borderRadius: '50%',
                        width: '18px',
                        height: '18px',
                        fontSize: '10px',
                        fontWeight: 600,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center'
                    }}>
                        {unreadCount > 99 ? '99+' : unreadCount}
                    </span>
                )}
            </button>

            {isOpen && (
                <>
                    <div
                        style={{
                            position: 'fixed',
                            top: 0,
                            left: 0,
                            right: 0,
                            bottom: 0,
                            zIndex: 999
                        }}
                        onClick={() => setIsOpen(false)}
                    />
                    <div style={{
                        position: 'absolute',
                        top: '100%',
                        right: 0,
                        width: '400px',
                        maxHeight: '500px',
                        background: 'var(--bg-secondary)',
                        borderRadius: '8px',
                        boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
                        border: '1px solid var(--border-color)',
                        zIndex: 1000,
                        display: 'flex',
                        flexDirection: 'column'
                    }}>
                        {/* Header */}
                        <div style={{
                            padding: '12px 16px',
                            borderBottom: '1px solid var(--border-color)',
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center'
                        }}>
                            <h3 style={{ fontSize: '14px', fontWeight: 600, margin: 0 }}>Notifications</h3>
                            <div style={{ display: 'flex', gap: '8px' }}>
                                <button
                                    onClick={markAllAsRead}
                                    style={{
                                        background: 'transparent',
                                        border: 'none',
                                        cursor: 'pointer',
                                        padding: '4px',
                                        color: 'var(--text-secondary)',
                                        fontSize: '11px',
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '4px'
                                    }}
                                    title="Mark all as read"
                                >
                                    <CheckCheck size={14} />
                                    Mark all read
                                </button>
                                <button
                                    onClick={fetchNotifications}
                                    style={{
                                        background: 'transparent',
                                        border: 'none',
                                        cursor: 'pointer',
                                        padding: '4px',
                                        color: 'var(--text-secondary)',
                                        fontSize: '11px'
                                    }}
                                    title="Refresh"
                                >
                                    <RefreshCw size={14} />
                                </button>
                            </div>
                        </div>

                        {/* Notifications List */}
                        <div style={{
                            flex: 1,
                            overflowY: 'auto',
                            padding: '8px'
                        }}>
                            {loading ? (
                                <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                                    Loading...
                                </div>
                            ) : notifications.length === 0 ? (
                                <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                                    <Bell size={48} style={{ margin: '0 auto 16px', opacity: 0.3 }} />
                                    <p>No notifications</p>
                                </div>
                            ) : (
                                notifications.map(notif => (
                                    <div
                                        key={notif.notificationId}
                                        style={{
                                            padding: '12px',
                                            borderRadius: '6px',
                                            marginBottom: '8px',
                                            background: notif.status === '0' ? 'var(--bg-tertiary)' : 'transparent',
                                            border: notif.status === '0' ? '1px solid var(--primary)' : '1px solid transparent',
                                            cursor: 'pointer',
                                            transition: 'all 0.2s'
                                        }}
                                        onMouseEnter={(e) => e.currentTarget.style.background = 'var(--bg-tertiary)'}
                                        onMouseLeave={(e) => e.currentTarget.style.background = notif.status === '0' ? 'var(--bg-tertiary)' : 'transparent'}
                                        onClick={() => markAsRead(notif.notificationId)}
                                    >
                                        <div style={{ display: 'flex', gap: '12px' }}>
                                            <div style={{ flexShrink: 0 }}>
                                                {getNotificationIcon(notif.notificationType)}
                                            </div>
                                            <div style={{ flex: 1 }}>
                                                <h4 style={{
                                                    fontSize: '13px',
                                                    fontWeight: 600,
                                                    margin: '0 0 4px',
                                                    color: notif.status === '0' ? 'var(--text-primary)' : 'var(--text-secondary)'
                                                }}>
                                                    {notif.title}
                                                </h4>
                                                <p style={{
                                                    fontSize: '12px',
                                                    color: 'var(--text-secondary)',
                                                    margin: '0 0 6px',
                                                    display: '-webkit-box',
                                                    WebkitLineClamp: 2,
                                                    WebkitBoxOrient: 'vertical',
                                                    overflow: 'hidden'
                                                }}>
                                                    {notif.content}
                                                </p>
                                                <div style={{
                                                    fontSize: '11px',
                                                    color: 'var(--text-muted)',
                                                    display: 'flex',
                                                    justifyContent: 'space-between',
                                                    alignItems: 'center'
                                                }}>
                                                    <span>{formatTime(notif.createTime)}</span>
                                                    {notif.status === '0' && (
                                                        <button
                                                            onClick={(e) => {
                                                                e.stopPropagation();
                                                                markAsRead(notif.notificationId);
                                                            }}
                                                            style={{
                                                                background: 'transparent',
                                                                border: 'none',
                                                                cursor: 'pointer',
                                                                padding: '4px',
                                                                color: 'var(--primary)',
                                                                fontSize: '11px',
                                                                display: 'flex',
                                                                alignItems: 'center',
                                                                gap: '4px'
                                                            }}
                                                        >
                                                            <Check size={12} />
                                                            Mark read
                                                        </button>
                                                    )}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>

                        {/* Footer */}
                        {notifications.length > 0 && (
                            <div style={{
                                padding: '12px',
                                borderTop: '1px solid var(--border-color)',
                                textAlign: 'center'
                            }}>
                                <button
                                    onClick={() => setIsOpen(false)}
                                    style={{
                                        background: 'var(--primary)',
                                        color: 'white',
                                        border: 'none',
                                        borderRadius: '6px',
                                        padding: '8px 16px',
                                        fontSize: '12px',
                                        cursor: 'pointer',
                                        width: '100%'
                                    }}
                                >
                                    Close
                                </button>
                            </div>
                        )}
                    </div>
                </>
            )}
        </div>
    );
};

export default NotificationCenter;
