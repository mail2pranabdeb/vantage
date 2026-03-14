import { useState, useEffect } from 'react';
import { User, Mail, Phone, ShieldCheck, Clock, Plus, Edit, Trash2, Eye, RefreshCw, X } from 'lucide-react';
import DataGrid from '../components/DataGrid';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';

const formatDate = (dateValue) => {
    if (!dateValue) return 'N/A';
    try {
        return new Date(dateValue).toLocaleString();
    } catch {
        return 'N/A';
    }
};

const UserList = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('add');
    const [currentUser, setCurrentUser] = useState(null);
    const [formData, setFormData] = useState({
        loginName: '',
        userName: '',
        email: '',
        phonenumber: '',
        sex: '0',
        status: '0',
        password: '',
        remark: ''
    });
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = () => {
        setLoading(true);
        console.log('Fetching users...');
        return fetch('/api/system/user/list')
            .then(res => {
                console.log('Fetch users response status:', res.status);
                return res.json();
            })
            .then(data => {
                console.log('Fetch users response data:', data);
                if (data.code === 200) {
                    console.log('Users data:', data.data);
                    setUsers(data.data || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch users:", err);
                setLoading(false);
            });
    };

    const handleAddClick = () => {
        setModalMode('add');
        setCurrentUser(null);
        setFormData({
            loginName: '',
            userName: '',
            email: '',
            phonenumber: '',
            sex: '0',
            status: '0',
            password: '',
            remark: ''
        });
        setIsModalOpen(true);
    };

    const handleEditClick = (row) => {
        setModalMode('edit');
        setCurrentUser(row);
        setFormData({
            loginName: row.loginName || '',
            userName: row.userName || '',
            email: row.email || '',
            phonenumber: row.phonenumber || '',
            sex: row.sex || '0',
            status: row.status || '0',
            remark: row.remark || ''
        });
        setIsModalOpen(true);
    };

    const handleViewClick = (row) => {
        setModalMode('view');
        setCurrentUser(row);
        setFormData({
            loginName: row.loginName || '',
            userName: row.userName || '',
            email: row.email || '',
            phonenumber: row.phonenumber || '',
            sex: row.sex || '0',
            status: row.status || '0',
            remark: row.remark || ''
        });
        setIsModalOpen(true);
    };

    const handleDeleteClick = (row) => {
        if (window.confirm(`Are you sure you want to delete user "${row.userName}"?`)) {
            fetch(`/api/system/user/${row.userId}`, {
                method: 'DELETE'
            })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setUsers(users.filter(u => u.userId !== row.userId));
                } else {
                    alert(data.msg || 'Failed to delete user');
                }
            })
            .catch(err => {
                console.error("Failed to delete user:", err);
                alert('Failed to delete user');
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
            ? '/api/system/user'
            : '/api/system/user';

        const method = modalMode === 'add' ? 'POST' : 'PUT';
        const body = modalMode === 'add'
            ? { ...formData }
            : { ...formData, userId: currentUser.userId };

        console.log('Submitting user:', body);

        fetch(url, {
            method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(body)
        })
        .then(res => {
            console.log('Response status:', res.status);
            return res.json();
        })
        .then(data => {
            console.log('Response data:', data);
            setSubmitting(false);
            if (data.code === 200) {
                console.log('User added successfully, fetching users...');
                setIsModalOpen(false);
                // Add a small delay to ensure database transaction is committed
                setTimeout(() => {
                    fetchUsers().then(() => {
                        console.log('Users refreshed');
                    });
                }, 100);
            } else {
                alert(data.msg || `Failed to ${modalMode} user`);
            }
        })
        .catch(err => {
            setSubmitting(false);
            console.error(`Failed to ${modalMode} user:`, err);
            alert(`Failed to ${modalMode} user: ${err.message}`);
        });
    };

    const columns = [
        {
            key: 'loginName',
            header: 'Login Name',
            sortable: true,
            render: (value) => (
                <span className="badge-outline" style={{ fontWeight: 600 }}>
                    {value}
                </span>
            )
        },
        {
            key: 'userName',
            header: 'User Name',
            sortable: true
        },
        {
            key: 'email',
            header: 'Email',
            sortable: true,
            render: (value) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Mail size={14} style={{ opacity: 0.5 }} />
                    {value}
                </div>
            )
        },
        {
            key: 'phonenumber',
            header: 'Phone',
            sortable: true,
            render: (value) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Phone size={14} style={{ opacity: 0.5 }} />
                    {value || '-'}
                </div>
            )
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
        },
        {
            key: 'createTime',
            header: 'Create Time',
            sortable: true,
            render: (value) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Clock size={14} style={{ opacity: 0.5 }} />
                    {formatDate(value)}
                </div>
            )
        }
    ];

    const actions = [
        {
            label: 'View',
            icon: Eye,
            onClick: handleViewClick
        },
        {
            label: 'Edit',
            icon: Edit,
            onClick: handleEditClick
        },
        {
            label: 'Delete',
            icon: Trash2,
            danger: true,
            onClick: handleDeleteClick
        }
    ];

    const toolbarActions = [
        {
            label: 'Refresh',
            icon: RefreshCw,
            onClick: fetchUsers
        }
    ];

    return (
        <div className="page-container">
            <div className="page-header">
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <div style={{
                        width: '32px',
                        height: '32px',
                        borderRadius: '8px',
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <User size={16} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '16px', fontWeight: 700, margin: 0 }}>User Management</h2>
                        <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '2px 0 0' }}>
                            Manage system users
                        </p>
                    </div>
                </div>
                <button
                    className="btn btn-primary"
                    onClick={handleAddClick}
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '6px',
                        padding: '8px 14px',
                        borderRadius: '6px',
                        fontWeight: 600,
                        fontSize: '13px'
                    }}
                >
                    <Plus size={16} />
                    Add User
                </button>
            </div>

            <DataGrid
                data={users}
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
                emptyMessage="No users found. Add your first user to get started."
            />

            {/* Add/Edit Modal */}
            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title={modalMode === 'add' ? 'Add User' : modalMode === 'edit' ? 'Edit User' : 'View User'}
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
                    <div className="form-row" style={{ gap: '10px' }}>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                            <label className="form-label" style={{ fontSize: '11px', marginBottom: '4px' }}>Login Name</label>
                            <FormInput
                                name="loginName"
                                value={formData.loginName}
                                onChange={handleInputChange}
                                placeholder="Enter login name"
                                required
                                disabled={modalMode === 'view' || modalMode === 'edit'}
                                style={{ padding: '7px 10px', fontSize: '13px' }}
                            />
                        </div>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                            <label className="form-label" style={{ fontSize: '11px', marginBottom: '4px' }}>User Name</label>
                            <FormInput
                                name="userName"
                                value={formData.userName}
                                onChange={handleInputChange}
                                placeholder="Enter user name"
                                required
                                disabled={modalMode === 'view'}
                                style={{ padding: '7px 10px', fontSize: '13px' }}
                            />
                        </div>
                    </div>

                    <div className="form-row" style={{ gap: '10px' }}>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                            <label className="form-label" style={{ fontSize: '11px', marginBottom: '4px' }}>Password</label>
                            <FormInput
                                name="password"
                                type="password"
                                value={formData.password}
                                onChange={handleInputChange}
                                placeholder="Enter password"
                                required={modalMode === 'add'}
                                disabled={modalMode === 'view' || modalMode === 'edit'}
                                style={{ padding: '7px 10px', fontSize: '13px' }}
                            />
                        </div>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                            <label className="form-label" style={{ fontSize: '11px', marginBottom: '4px' }}>Phone Number</label>
                            <FormInput
                                name="phonenumber"
                                value={formData.phonenumber}
                                onChange={handleInputChange}
                                placeholder="Enter phone number"
                                disabled={modalMode === 'view'}
                                style={{ padding: '7px 10px', fontSize: '13px' }}
                            />
                        </div>
                    </div>

                    <div className="form-row" style={{ gap: '10px' }}>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                            <label className="form-label" style={{ fontSize: '11px', marginBottom: '4px' }}>Email</label>
                            <FormInput
                                name="email"
                                type="email"
                                value={formData.email}
                                onChange={handleInputChange}
                                placeholder="Enter email address"
                                disabled={modalMode === 'view'}
                                style={{ padding: '7px 10px', fontSize: '13px' }}
                            />
                        </div>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                            <label className="form-label" style={{ fontSize: '11px', marginBottom: '4px' }}>Gender</label>
                            <select
                                name="sex"
                                value={formData.sex}
                                onChange={handleInputChange}
                                className="form-input"
                                disabled={modalMode === 'view'}
                                style={{ padding: '7px 10px', fontSize: '13px' }}
                            >
                                <option value="0">Male</option>
                                <option value="1">Female</option>
                                <option value="2">Unknown</option>
                            </select>
                        </div>
                    </div>

                    <div className="form-row" style={{ gap: '10px' }}>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                            <label className="form-label" style={{ fontSize: '11px', marginBottom: '4px' }}>Status</label>
                            <select
                                name="status"
                                value={formData.status}
                                onChange={handleInputChange}
                                className="form-input"
                                disabled={modalMode === 'view'}
                                style={{ padding: '7px 10px', fontSize: '13px' }}
                            >
                                <option value="0">Normal</option>
                                <option value="1">Disabled</option>
                            </select>
                        </div>
                        <div className="form-group" style={{ marginBottom: 0 }}>
                            <label className="form-label" style={{ fontSize: '11px', marginBottom: '4px' }}>Remark</label>
                            <textarea
                                name="remark"
                                value={formData.remark}
                                onChange={handleInputChange}
                                placeholder="Optional"
                                className="form-input"
                                rows={2}
                                disabled={modalMode === 'view'}
                                style={{ padding: '7px 10px', fontSize: '13px', resize: 'none' }}
                            />
                        </div>
                    </div>

                    {(modalMode === 'edit' || modalMode === 'view') && currentUser && (
                        <div className="audit-section">
                            <h4>Audit Information</h4>
                            <div className="form-row">
                                <div className="form-group">
                                    <label className="form-label">Created By</label>
                                    <input
                                        type="text"
                                        className="form-input"
                                        value={currentUser.createBy || '-'}
                                        disabled
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Created Time</label>
                                    <input
                                        type="text"
                                        className="form-input"
                                        value={currentUser.createTime ? new Date(currentUser.createTime).toLocaleString() : '-'}
                                        disabled
                                    />
                                </div>
                            </div>
                            <div className="form-row">
                                <div className="form-group">
                                    <label className="form-label">Updated By</label>
                                    <input
                                        type="text"
                                        className="form-input"
                                        value={currentUser.updateBy || '-'}
                                        disabled
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Updated Time</label>
                                    <input
                                        type="text"
                                        className="form-input"
                                        value={currentUser.updateTime ? new Date(currentUser.updateTime).toLocaleString() : '-'}
                                        disabled
                                    />
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </Modal>
        </div>
    );
};

export default UserList;
