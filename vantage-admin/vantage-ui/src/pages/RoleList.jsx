import { useState, useEffect } from 'react';
import { Shield, Plus, Edit, Trash2, Eye, RefreshCw } from 'lucide-react';
import DataGrid from '../components/DataGrid';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';
import { useToast } from '../components/Toast';

const RoleList = () => {
    const { addToast } = useToast();
    const [roles, setRoles] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('add');
    const [currentRole, setCurrentRole] = useState(null);
    const [formData, setFormData] = useState({
        roleName: '',
        roleKey: '',
        roleSort: '0',
        status: '0',
        remark: ''
    });
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        fetchRoles();
    }, []);

    const fetchRoles = () => {
        setLoading(true);
        fetch('/api/system/role/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setRoles(data.data || []);
                    if (data.data && data.data.length > 0) {
                        addToast('success', `Loaded ${data.data.length} role(s)`, 2000);
                    }
                } else {
                    addToast('error', data.msg || 'Failed to load roles', 4000);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch roles:", err);
                addToast('error', 'Failed to load roles. Please refresh.', 5000);
                setLoading(false);
            });
    };

    const handleAddClick = () => {
        setModalMode('add');
        setCurrentRole(null);
        setFormData({
            roleName: '',
            roleKey: '',
            roleSort: '0',
            status: '0',
            remark: ''
        });
        setIsModalOpen(true);
    };

    const handleEditClick = (row) => {
        setModalMode('edit');
        setCurrentRole(row);
        setFormData({
            roleName: row.roleName || '',
            roleKey: row.roleKey || '',
            roleSort: String(row.roleSort || '0'),
            status: row.status || '0',
            remark: row.remark || ''
        });
        setIsModalOpen(true);
    };

    const handleViewClick = (row) => {
        setModalMode('view');
        setCurrentRole(row);
        setFormData({
            roleName: row.roleName || '',
            roleKey: row.roleKey || '',
            roleSort: String(row.roleSort || '0'),
            status: row.status || '0',
            remark: row.remark || ''
        });
        setIsModalOpen(true);
    };

    const handleDeleteClick = (row) => {
        if (window.confirm(`Are you sure you want to delete role "${row.roleName}"?`)) {
            fetch(`/api/system/role/${row.roleId}`, {
                method: 'DELETE'
            })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setRoles(roles.filter(r => r.roleId !== row.roleId));
                    addToast('success', `Role "${row.roleName}" deleted successfully`, 3000);
                } else {
                    addToast('error', data.msg || 'Failed to delete role', 5000);
                }
            })
            .catch(err => {
                console.error("Failed to delete role:", err);
                addToast('error', 'Failed to delete role', 5000);
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
            ? '/api/system/role'
            : '/api/system/role';

        const method = modalMode === 'add' ? 'POST' : 'PUT';
        const body = modalMode === 'add'
            ? { ...formData, roleSort: parseInt(formData.roleSort) }
            : { ...formData, roleId: currentRole.roleId, roleSort: parseInt(formData.roleSort) };

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
                addToast('success', `Role "${formData.roleName}" ${modalMode === 'add' ? 'created' : 'updated'} successfully`, 3000);
                fetchRoles();
            } else {
                addToast('error', data.msg || `Failed to ${modalMode} role`, 5000);
            }
        })
        .catch(err => {
            setSubmitting(false);
            console.error(`Failed to ${modalMode} role:`, err);
            addToast('error', `Failed to ${modalMode} role`, 5000);
        });
    };

    const columns = [
        {
            key: 'roleId',
            header: 'Role ID',
            sortable: true
        },
        {
            key: 'roleName',
            header: 'Role Name',
            sortable: true,
            render: (value, row) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
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
                        <Shield size={16} />
                    </div>
                    <div>
                        <div style={{ fontWeight: 600 }}>{value}</div>
                        <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{row.roleKey}</div>
                    </div>
                </div>
            )
        },
        {
            key: 'roleKey',
            header: 'Role Key',
            sortable: true
        },
        {
            key: 'roleSort',
            header: 'Sort',
            sortable: true,
            align: 'center'
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
                <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                    {value ? new Date(value).toLocaleDateString() : '-'}
                </span>
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
            onClick: fetchRoles
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
                        background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <Shield size={16} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '16px', fontWeight: 700, margin: 0 }}>Role Management</h2>
                        <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '2px 0 0' }}>
                            Manage user roles
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
                    Add Role
                </button>
            </div>

            <DataGrid
                data={roles}
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
                emptyMessage="No roles found. Create your first role to get started."
            />

            {/* Add/Edit Modal */}
            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title={modalMode === 'add' ? 'Add Role' : modalMode === 'edit' ? 'Edit Role' : 'View Role'}
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
                            label="Role Name"
                            name="roleName"
                            value={formData.roleName}
                            onChange={handleInputChange}
                            placeholder="Enter role name"
                            required
                            disabled={modalMode === 'view'}
                        />
                        <FormInput
                            label="Role Key"
                            name="roleKey"
                            value={formData.roleKey}
                            onChange={handleInputChange}
                            placeholder="e.g., admin, user"
                            required
                            disabled={modalMode === 'view' || modalMode === 'edit'}
                        />
                    </div>

                    <div className="form-row">
                        <FormInput
                            label="Sort Order"
                            name="roleSort"
                            type="number"
                            value={formData.roleSort}
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

export default RoleList;
