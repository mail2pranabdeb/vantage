import { useState, useEffect } from 'react';
import { Menu, Plus, Edit, Trash2, Eye, Folder, RefreshCw } from 'lucide-react';
import DataGrid from '../components/DataGrid';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';

const MenuList = () => {
    const [menus, setMenus] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('add');
    const [currentMenu, setCurrentMenu] = useState(null);
    const [formData, setFormData] = useState({
        menuName: '',
        menuType: 'M',
        parentName: '',
        parentId: '0',
        path: '',
        component: '',
        query: '',
        perms: '',
        icon: '',
        orderNum: '0',
        visible: '0'
    });
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        fetchMenus();
    }, []);

    const fetchMenus = () => {
        setLoading(true);
        fetch('/api/system/menu/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setMenus(data.data || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch menus:", err);
                setLoading(false);
            });
    };

    const handleAddClick = () => {
        setModalMode('add');
        setCurrentMenu(null);
        setFormData({
            menuName: '',
            menuType: 'M',
            parentName: '',
            parentId: '0',
            path: '',
            component: '',
            query: '',
            perms: '',
            icon: '',
            orderNum: '0',
            visible: '0'
        });
        setIsModalOpen(true);
    };

    const handleEditClick = (row) => {
        setModalMode('edit');
        setCurrentMenu(row);
        setFormData({
            menuName: row.menuName || '',
            menuType: row.menuType || 'M',
            parentName: row.parentName || '',
            parentId: String(row.parentId || '0'),
            path: row.path || '',
            component: row.component || '',
            query: row.query || '',
            perms: row.perms || '',
            icon: row.icon || '',
            orderNum: String(row.orderNum || '0'),
            visible: row.visible || '0'
        });
        setIsModalOpen(true);
    };

    const handleViewClick = (row) => {
        setModalMode('view');
        setCurrentMenu(row);
        setFormData({
            menuName: row.menuName || '',
            menuType: row.menuType || 'M',
            parentName: row.parentName || '',
            parentId: String(row.parentId || '0'),
            path: row.path || '',
            component: row.component || '',
            query: row.query || '',
            perms: row.perms || '',
            icon: row.icon || '',
            orderNum: String(row.orderNum || '0'),
            visible: row.visible || '0'
        });
        setIsModalOpen(true);
    };

    const handleDeleteClick = (row) => {
        if (window.confirm(`Are you sure you want to delete menu "${row.menuName}"?`)) {
            fetch(`/api/system/menu/${row.menuId}`, {
                method: 'DELETE'
            })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setMenus(menus.filter(m => m.menuId !== row.menuId));
                } else {
                    alert(data.msg || 'Failed to delete menu');
                }
            })
            .catch(err => {
                console.error("Failed to delete menu:", err);
                alert('Failed to delete menu');
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
            ? '/api/system/menu' 
            : '/api/system/menu';
        
        const method = modalMode === 'add' ? 'POST' : 'PUT';
        const body = modalMode === 'add' 
            ? { ...formData, parentId: parseInt(formData.parentId), orderNum: parseInt(formData.orderNum) } 
            : { ...formData, menuId: currentMenu.menuId, parentId: parseInt(formData.parentId), orderNum: parseInt(formData.orderNum) };

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
                fetchMenus();
            } else {
                alert(data.msg || `Failed to ${modalMode} menu`);
            }
        })
        .catch(err => {
            setSubmitting(false);
            console.error(`Failed to ${modalMode} menu:`, err);
            alert(`Failed to ${modalMode} menu`);
        });
    };

    const columns = [
        {
            key: 'menuId',
            header: 'ID',
            sortable: true,
            align: 'center'
        },
        {
            key: 'menuName',
            header: 'Menu Name',
            sortable: true,
            render: (value, row) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span style={{
                        fontSize: '16px',
                        opacity: 0.7
                    }}>{row.icon || '📄'}</span>
                    <span style={{ fontWeight: 600 }}>{value}</span>
                </div>
            )
        },
        {
            key: 'menuType',
            header: 'Type',
            sortable: true,
            align: 'center',
            render: (value) => {
                const types = { 'M': 'Directory', 'C': 'Menu', 'F': 'Button' };
                const colors = { 'M': '#3b82f6', 'C': '#10b981', 'F': '#f59e0b' };
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
            key: 'url',
            header: 'URL',
            sortable: false,
            render: (value) => (
                <span style={{ fontSize: '12px', color: 'var(--text-muted)', fontFamily: 'monospace' }}>
                    {value || '-'}
                </span>
            )
        },
        {
            key: 'perms',
            header: 'Permission',
            sortable: true,
            render: (value) => (
                <span className="badge-outline" style={{ fontSize: '11px' }}>
                    {value || '-'}
                </span>
            )
        },
        {
            key: 'visible',
            header: 'Visible',
            sortable: true,
            align: 'center',
            render: (value) => (
                <span className={`status-pill ${value === '0' ? 'active' : 'inactive'}`}>
                    {value === '0' ? 'Yes' : 'No'}
                </span>
            )
        },
        {
            key: 'orderNum',
            header: 'Sort',
            sortable: true,
            align: 'center'
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
            onClick: fetchMenus
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
                        background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <Menu size={16} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '16px', fontWeight: 700, margin: 0 }}>Menu Management</h2>
                        <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '2px 0 0' }}>
                            Manage system menus
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
                    Add Menu
                </button>
            </div>

            <DataGrid
                data={menus}
                columns={columns}
                actions={actions}
                toolbarActions={toolbarActions}
                loading={loading}
                searchable={true}
                sortable={true}
                filterable={true}
                selectable={true}
                pagination={true}
                pageSize={15}
                emptyMessage="No menus found."
            />

            {/* Add/Edit Modal */}
            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title={modalMode === 'add' ? 'Add Menu' : modalMode === 'edit' ? 'Edit Menu' : 'View Menu'}
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
                            label="Menu Name"
                            name="menuName"
                            value={formData.menuName}
                            onChange={handleInputChange}
                            placeholder="Enter menu name"
                            required
                            disabled={modalMode === 'view'}
                        />
                        <div className="form-group">
                            <label className="form-label">Menu Type</label>
                            <select
                                name="menuType"
                                value={formData.menuType}
                                onChange={handleInputChange}
                                className="form-input"
                                disabled={modalMode === 'view'}
                            >
                                <option value="M">Directory</option>
                                <option value="C">Menu</option>
                                <option value="F">Button</option>
                            </select>
                        </div>
                    </div>

                    <div className="form-row">
                        <FormInput
                            label="Parent Name"
                            name="parentName"
                            value={formData.parentName}
                            onChange={handleInputChange}
                            placeholder="Parent menu name"
                            disabled={modalMode === 'view'}
                        />
                        <FormInput
                            label="Icon"
                            name="icon"
                            value={formData.icon}
                            onChange={handleInputChange}
                            placeholder="e.g., user, settings"
                            disabled={modalMode === 'view'}
                        />
                    </div>

                    <div className="form-row">
                        <FormInput
                            label="Path"
                            name="path"
                            value={formData.path}
                            onChange={handleInputChange}
                            placeholder="e.g., /system/user"
                            disabled={modalMode === 'view'}
                        />
                        <FormInput
                            label="Component"
                            name="component"
                            value={formData.component}
                            onChange={handleInputChange}
                            placeholder="e.g., system/user/index"
                            disabled={modalMode === 'view'}
                        />
                    </div>

                    <div className="form-row">
                        <FormInput
                            label="Permission"
                            name="perms"
                            value={formData.perms}
                            onChange={handleInputChange}
                            placeholder="e.g., system:user:list"
                            disabled={modalMode === 'view'}
                        />
                        <FormInput
                            label="Sort Order"
                            name="orderNum"
                            type="number"
                            value={formData.orderNum}
                            onChange={handleInputChange}
                            placeholder="Enter sort order"
                            disabled={modalMode === 'view'}
                        />
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label className="form-label">Visible</label>
                            <select
                                name="visible"
                                value={formData.visible}
                                onChange={handleInputChange}
                                className="form-input"
                                disabled={modalMode === 'view'}
                            >
                                <option value="0">Yes</option>
                                <option value="1">No</option>
                            </select>
                        </div>
                    </div>
                </div>
            </Modal>
        </div>
    );
};

export default MenuList;
