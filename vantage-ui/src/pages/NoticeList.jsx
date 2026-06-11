import { useState, useEffect } from 'react';
import { Bell, Plus, Edit, Trash2, Eye, RefreshCw, Download, Upload } from 'lucide-react';
import DataGrid from '../components/DataGrid';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';
import { useToast } from '../components/Toast';
import ImportModal from '../components/ImportModal';

const NoticeList = () => {
    const { addToast } = useToast();
    const [notices, setNotices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('add');
    const [currentNotice, setCurrentNotice] = useState(null);
    const [formData, setFormData] = useState({
        noticeTitle: '',
        noticeType: '1',
        noticeContent: '',
        status: '0'
    });
    const [submitting, setSubmitting] = useState(false);
    const [importModalOpen, setImportModalOpen] = useState(false);

    useEffect(() => {
        fetchNotices();
    }, []);

    const fetchNotices = () => {
        setLoading(true);
        fetch('/api/system/notice/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setNotices(data.data || []);
                    if (data.data && data.data.length > 0) {

                    }
                } else {
                    addToast('error', data.msg || 'Failed to load notices', 4000);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch notices:", err);
                setLoading(false);
                addToast('error', 'Failed to load notices. Please refresh.', 5000);
            });
    };

    const handleAddClick = () => {
        setModalMode('add');
        setCurrentNotice(null);
        setFormData({
            noticeTitle: '',
            noticeType: '1',
            noticeContent: '',
            status: '0'
        });
        setIsModalOpen(true);
    };

    const handleEditClick = (row) => {
        setModalMode('edit');
        setCurrentNotice(row);
        setFormData({
            noticeTitle: row.noticeTitle || '',
            noticeType: row.noticeType || '1',
            noticeContent: row.noticeContent || '',
            status: row.status || '0'
        });
        setIsModalOpen(true);
    };

    const handleViewClick = (row) => {
        setModalMode('view');
        setCurrentNotice(row);
        setFormData({
            noticeTitle: row.noticeTitle || '',
            noticeType: row.noticeType || '1',
            noticeContent: row.noticeContent || '',
            status: row.status || '0'
        });
        setIsModalOpen(true);
    };

    const handleDeleteClick = (row) => {
        if (window.confirm(`Are you sure you want to delete notice "${row.noticeTitle}"?`)) {
            fetch(`/api/system/notice/${row.noticeId}`, {
                method: 'DELETE'
            })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setNotices(notices.filter(n => n.noticeId !== row.noticeId));
                    addToast('success', `Notice "${row.noticeTitle}" deleted successfully`, 3000);
                } else {
                    addToast('error', data.msg || 'Failed to delete notice', 5000);
                }
            })
            .catch(err => {
                console.error("Failed to delete notice:", err);
                addToast('error', 'Failed to delete notice', 5000);
            });
        }
    };

    const handleExport = (format) => {
        const columns = [
            { key: 'noticeId', label: 'ID' },
            { key: 'noticeTitle', label: 'Title' },
            { key: 'noticeType', label: 'Type' },
            { key: 'status', label: 'Status' },
            { key: 'createBy', label: 'Created By' },
            { key: 'createTime', label: 'Created' },
        ];
        const ext = format.toLowerCase();
        fetch('/api/system/export?format=' + format + '&filename=notices', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ columns, rows: notices })
        })
        .then(res => res.blob())
        .then(blob => {
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a'); a.href = url; a.download = 'notices.' + ext;
            document.body.appendChild(a); a.click(); a.remove();
            URL.revokeObjectURL(url);
        })
        .catch(err => console.error('Export failed:', err));
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = () => {
        setSubmitting(true);
        
        const url = modalMode === 'add' 
            ? '/api/system/notice' 
            : '/api/system/notice';
        
        const method = modalMode === 'add' ? 'POST' : 'PUT';
        const body = modalMode === 'add' 
            ? { ...formData } 
            : { ...formData, noticeId: currentNotice.noticeId };

        fetch(url, {
            method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(body)
        })
        .then(res => res.json())
        .then(data => {
            setSubmitting(false);
            if (data.code === 200) {
                setIsModalOpen(false);
                addToast('success', `Notice "${formData.noticeTitle}" ${modalMode === 'add' ? 'created' : 'updated'} successfully`, 3000);
                fetchNotices();
            } else {
                addToast('error', data.msg || `Failed to ${modalMode} notice`, 5000);
            }
        })
        .catch(err => {
            setSubmitting(false);
            console.error(`Failed to ${modalMode} notice:`, err);
            addToast('error', `Failed to ${modalMode} notice`, 5000);
        });
    };

    const columns = [
        { key: 'noticeId', header: 'ID', sortable: true, align: 'center' },
        {
            key: 'noticeTitle',
            header: 'Title',
            sortable: true,
            render: (value, row) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Bell size={16} style={{ opacity: 0.5 }} />
                    <span style={{ fontWeight: 600 }}>{value}</span>
                </div>
            )
        },
        {
            key: 'noticeType',
            header: 'Type',
            sortable: true,
            render: (value) => {
                const types = { '1': 'Notification', '2': 'Announcement' };
                const colors = { '1': '#3b82f6', '2': '#f59e0b' };
                return (
                    <span style={{
                        padding: '4px 10px',
                        borderRadius: '12px',
                        fontSize: '11px',
                        fontWeight: 600,
                        background: `${colors[value]}20`,
                        color: colors[value]
                    }}>
                        {types[value] || value}
                    </span>
                );
            }
        },
        {
            key: 'status',
            header: 'Status',
            sortable: true,
            render: (value) => (
                <span className={`status-pill ${value === '0' ? 'active' : 'inactive'}`}>
                    {value === '0' ? 'Normal' : 'Disabled'}
                </span>
            )
        }
    ];

    const actions = [
        { label: 'View', icon: Eye, onClick: handleViewClick },
        { label: 'Edit', icon: Edit, onClick: handleEditClick },
        { label: 'Delete', icon: Trash2, danger: true, onClick: handleDeleteClick }
    ];

    const toolbarActions = [
        {
            label: 'Refresh',
            icon: RefreshCw,
            onClick: fetchNotices
        },
        {
            label: 'PDF',
            icon: Download,
            onClick: () => handleExport('PDF')
        },
        {
            label: 'CSV',
            icon: Download,
            onClick: () => handleExport('CSV')
        },
        {
            label: 'Import',
            icon: Upload,
            onClick: () => setImportModalOpen(true)
        }
    ];

    return (
        <div className="page-container">
            <div className="page-header">
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{
                        width: '40px',
                        height: '40px',
                        borderRadius: '10px',
                        background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <Bell size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Notice Management</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Manage system notices and announcements
                        </p>
                    </div>
                </div>
                <button 
                    className="btn btn-primary" 
                    onClick={handleAddClick}
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        padding: '10px 16px',
                        borderRadius: '8px',
                        fontWeight: 600
                    }}
                >
                    <Plus size={18} />
                    Add Notice
                </button>
            </div>

            <DataGrid
                data={notices}
                columns={columns}
                actions={actions}
                toolbarActions={toolbarActions}
                loading={loading}
                searchable={true}
                sortable={true}
                filterable={true}
                selectable={true}
                pagination={true}
                pageSize={10}
                emptyMessage="No notices found."
            />

            {/* Add/Edit Modal */}
            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title={modalMode === 'add' ? 'Add Notice' : modalMode === 'edit' ? 'Edit Notice' : 'View Notice'}
                size="small"
                compact={true}
                footer={modalMode !== 'view' && (
                    <>
                        <button
                            className="btn btn-secondary"
                            onClick={() => setIsModalOpen(false)}
                            disabled={submitting}
                        >
                            Cancel
                        </button>
                        <button
                            className="btn btn-primary"
                            onClick={handleSubmit}
                            disabled={submitting}
                            style={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: '6px'
                            }}
                        >
                            {submitting && (
                                <div style={{
                                    width: '12px',
                                    height: '12px',
                                    border: '2px solid white',
                                    borderBottomColor: 'transparent',
                                    borderRadius: '50%',
                                    animation: 'spin 1s linear infinite'
                                }} />
                            )}
                            {modalMode === 'add' ? 'Create' : 'Save'}
                        </button>
                    </>
                )}
            >
                <div style={{ display: 'flex', flexDirection: 'column' }}>
                    <FormInput
                        label="Notice Title"
                        name="noticeTitle"
                        value={formData.noticeTitle}
                        onChange={handleInputChange}
                        placeholder="Enter notice title"
                        required
                        disabled={modalMode === 'view'}
                    />

                    <div className="form-row">
                        <div className="form-group">
                            <label className="form-label">Type</label>
                            <select
                                name="noticeType"
                                value={formData.noticeType}
                                onChange={handleInputChange}
                                className="form-input"
                                disabled={modalMode === 'view'}
                            >
                                <option value="1">Notification</option>
                                <option value="2">Announcement</option>
                            </select>
                        </div>
                        <div className="form-group">
                            <label className="form-label">Status</label>
                            <select
                                name="status"
                                value={formData.status}
                                onChange={handleInputChange}
                                className="form-input"
                                disabled={modalMode === 'view'}
                            >
                                <option value="0">Normal</option>
                                <option value="1">Disabled</option>
                            </select>
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="form-label">Content</label>
                        <textarea
                            name="noticeContent"
                            value={formData.noticeContent}
                            onChange={handleInputChange}
                            placeholder="Enter notice content"
                            className="form-input"
                            rows={8}
                            disabled={modalMode === 'view'}
                        />
                    </div>
                </div>
            </Modal>

            <ImportModal
                isOpen={importModalOpen}
                onClose={() => setImportModalOpen(false)}
                onImportComplete={fetchNotices}
            />
        </div>
    );
};

export default NoticeList;
