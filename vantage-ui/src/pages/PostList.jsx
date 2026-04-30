import { useState, useEffect } from 'react';
import { Briefcase, Plus, Edit, Trash2, Eye, RefreshCw } from 'lucide-react';
import DataGrid from '../components/DataGrid';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';
import { useToast } from '../components/Toast';

const PostList = () => {
    const { addToast } = useToast();
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('add');
    const [currentPost, setCurrentPost] = useState(null);
    const [formData, setFormData] = useState({
        postCode: '',
        postName: '',
        postSort: '0',
        status: '0',
        remark: ''
    });
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        fetchPosts();
    }, []);

    const fetchPosts = () => {
        setLoading(true);
        fetch('/api/system/post/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setPosts(data.data || []);
                    if (data.data && data.data.length > 0) {

                    }
                } else {
                    addToast('error', data.msg || 'Failed to load posts', 4000);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch posts:", err);
                setLoading(false);
                addToast('error', 'Failed to load posts. Please refresh.', 5000);
            });
    };

    const handleAddClick = () => {
        setModalMode('add');
        setCurrentPost(null);
        setFormData({
            postCode: '',
            postName: '',
            postSort: '0',
            status: '0',
            remark: ''
        });
        setIsModalOpen(true);
    };

    const handleEditClick = (row) => {
        setModalMode('edit');
        setCurrentPost(row);
        setFormData({
            postCode: row.postCode || '',
            postName: row.postName || '',
            postSort: String(row.postSort || '0'),
            status: row.status || '0',
            remark: row.remark || ''
        });
        setIsModalOpen(true);
    };

    const handleViewClick = (row) => {
        setModalMode('view');
        setCurrentPost(row);
        setFormData({
            postCode: row.postCode || '',
            postName: row.postName || '',
            postSort: String(row.postSort || '0'),
            status: row.status || '0',
            remark: row.remark || ''
        });
        setIsModalOpen(true);
    };

    const handleDeleteClick = (row) => {
        if (window.confirm(`Are you sure you want to delete post "${row.postName}"?`)) {
            fetch(`/api/system/post/${row.postId}`, {
                method: 'DELETE'
            })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setPosts(posts.filter(p => p.postId !== row.postId));
                    addToast('success', `Post "${row.postName}" deleted successfully`, 3000);
                } else {
                    addToast('error', data.msg || 'Failed to delete post', 5000);
                }
            })
            .catch(err => {
                console.error("Failed to delete post:", err);
                addToast('error', 'Failed to delete post', 5000);
            });
        }
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
            ? '/api/system/post'
            : '/api/system/post';

        const method = modalMode === 'add' ? 'POST' : 'PUT';
        const body = modalMode === 'add'
            ? { ...formData, postSort: parseInt(formData.postSort) }
            : { ...formData, postId: currentPost.postId, postSort: parseInt(formData.postSort) };

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
                addToast('success', `Post "${formData.postName}" ${modalMode === 'add' ? 'created' : 'updated'} successfully`, 3000);
                fetchPosts();
            } else {
                addToast('error', data.msg || `Failed to ${modalMode} post`, 5000);
            }
        })
        .catch(err => {
            setSubmitting(false);
            console.error(`Failed to ${modalMode} post:`, err);
            addToast('error', `Failed to ${modalMode} post`, 5000);
        });
    };

    const columns = [
        { key: 'postId', header: 'ID', sortable: true, align: 'center' },
        {
            key: 'postCode',
            header: 'Code',
            sortable: true,
            render: (value) => <span className="badge-outline" style={{ fontSize: '12px' }}>{value}</span>
        },
        {
            key: 'postName',
            header: 'Post Name',
            sortable: true,
            render: (value, row) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Briefcase size={16} style={{ opacity: 0.5 }} />
                    <span style={{ fontWeight: 600 }}>{value}</span>
                </div>
            )
        },
        { key: 'postSort', header: 'Sort', sortable: true, align: 'center' },
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
            onClick: fetchPosts
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
                        background: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: '#333'
                    }}>
                        <Briefcase size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Post Management</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Manage job positions and posts
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
                    Add Post
                </button>
            </div>

            <DataGrid
                data={posts}
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
                emptyMessage="No posts found."
            />

            {/* Add/Edit Modal */}
            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title={modalMode === 'add' ? 'Add Post' : modalMode === 'edit' ? 'Edit Post' : 'View Post'}
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
                    <div className="form-row">
                        <FormInput
                            label="Post Code"
                            name="postCode"
                            value={formData.postCode}
                            onChange={handleInputChange}
                            placeholder="Enter post code"
                            required
                            disabled={modalMode === 'view' || modalMode === 'edit'}
                        />
                        <FormInput
                            label="Post Name"
                            name="postName"
                            value={formData.postName}
                            onChange={handleInputChange}
                            placeholder="Enter post name"
                            required
                            disabled={modalMode === 'view'}
                        />
                    </div>

                    <div className="form-row">
                        <FormInput
                            label="Sort Order"
                            name="postSort"
                            type="number"
                            value={formData.postSort}
                            onChange={handleInputChange}
                            placeholder="Enter sort order"
                            disabled={modalMode === 'view'}
                        />
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
                        <label className="form-label">Remark</label>
                        <textarea
                            name="remark"
                            value={formData.remark}
                            onChange={handleInputChange}
                            placeholder="Enter any remarks"
                            className="form-input"
                            rows={3}
                            disabled={modalMode === 'view'}
                        />
                    </div>
                </div>
            </Modal>
        </div>
    );
};

export default PostList;
