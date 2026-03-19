import { useState, useEffect } from 'react';
import { Menu, Plus, Edit, Trash2, Eye, Folder, FolderOpen, File, ChevronRight, ChevronDown, RefreshCw } from 'lucide-react';
import { useToast } from '../components/Toast';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';

const MenuList = () => {
    const { addToast } = useToast();
    const [menus, setMenus] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('add');
    const [currentMenu, setCurrentMenu] = useState(null);
    const [expandedNodes, setExpandedNodes] = useState({});
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
                setLoading(false);
                if (data.code === 200) {
                    const menuTree = buildMenuTree(data.data || []);
                    setMenus(menuTree);
                    addToast('success', `Loaded ${data.data.length} menu item(s)`, 2000);
                    // Expand all by default
                    const allExpanded = {};
                    data.data.forEach(m => {
                        if (m.children && m.children.length > 0) {
                            allExpanded[m.menuId] = true;
                        }
                    });
                    setExpandedNodes(allExpanded);
                } else {
                    addToast('error', data.msg || 'Failed to load menus', 4000);
                }
            })
            .catch(err => {
                console.error("Failed to fetch menus:", err);
                setLoading(false);
                addToast('error', 'Failed to load menus. Please refresh.', 5000);
            });
    };

    const buildMenuTree = (items, parentId = '0') => {
        const result = [];
        items.forEach(item => {
            if (String(item.parentId) === String(parentId)) {
                const children = buildMenuTree(items, item.menuId);
                result.push({
                    ...item,
                    children: children.length > 0 ? children : null
                });
            }
        });
        return result;
    };

    const toggleNode = (menuId) => {
        setExpandedNodes(prev => ({
            ...prev,
            [menuId]: !prev[menuId]
        }));
    };

    const renderTree = (items, level = 0) => {
        return items.map(item => (
            <div key={item.menuId}>
                <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    padding: '8px 10px',
                    background: 'var(--bg-secondary)',
                    borderRadius: '6px',
                    marginBottom: '4px',
                    border: '1px solid var(--border-color)',
                    marginLeft: `${level * 20}px`,
                    fontSize: '12px'
                }}>
                    {/* Expand/Collapse Button */}
                    <button
                        onClick={() => toggleNode(item.menuId)}
                        style={{
                            background: 'transparent',
                            border: 'none',
                            cursor: item.children ? 'pointer' : 'default',
                            padding: '2px',
                            display: 'flex',
                            alignItems: 'center',
                            color: 'var(--text-muted)',
                            opacity: item.children ? 1 : 0,
                            width: '20px',
                            height: '20px'
                        }}
                    >
                        {expandedNodes[item.menuId] ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
                    </button>

                    {/* Icon */}
                    <span style={{
                        marginLeft: '4px',
                        padding: '4px',
                        borderRadius: '4px',
                        background: getMenuTypeColor(item.menuType),
                        display: 'flex',
                        alignItems: 'center',
                        color: '#fff',
                        width: '24px',
                        height: '24px',
                        justifyContent: 'center'
                    }}>
                        {getMenuIcon(item.menuType, item.icon)}
                    </span>

                    {/* Menu Name */}
                    <span style={{
                        marginLeft: '8px',
                        fontWeight: 600,
                        flex: 1,
                        fontSize: '12px'
                    }}>{item.menuName}</span>

                    {/* Type Badge */}
                    <span style={{
                        padding: '2px 8px',
                        borderRadius: '10px',
                        fontSize: '10px',
                        fontWeight: 600,
                        background: getMenuTypeColor(item.menuType),
                        color: '#fff',
                        marginRight: '8px',
                        minWidth: '55px',
                        textAlign: 'center'
                    }}>
                        {getMenuTypeLabel(item.menuType)}
                    </span>

                    {/* Order Num */}
                    <span style={{
                        fontSize: '11px',
                        color: 'var(--text-muted)',
                        marginRight: '12px',
                        minWidth: '30px',
                        textAlign: 'right'
                    }}>
                        #{item.orderNum || '0'}
                    </span>

                    {/* Actions */}
                    <div style={{ display: 'flex', gap: '4px' }}>
                        <button onClick={() => handleViewClick(item)} className="btn-icon" title="View" style={{ padding: '3px' }}>
                            <Eye size={14} />
                        </button>
                        <button onClick={() => handleEditClick(item)} className="btn-icon" title="Edit" style={{ padding: '3px' }}>
                            <Edit size={14} />
                        </button>
                        <button onClick={() => handleDeleteClick(item)} className="btn-icon text-danger" title="Delete" style={{ padding: '3px' }}>
                            <Trash2 size={14} />
                        </button>
                    </div>
                </div>

                {/* Children */}
                {item.children && expandedNodes[item.menuId] && (
                    <div style={{ marginLeft: '8px' }}>
                        {renderTree(item.children, level + 1)}
                    </div>
                )}
            </div>
        ));
    };

    const getMenuTypeLabel = (type) => {
        const labels = { 'M': 'Menu', 'C': 'Sub Menu', 'F': 'Permission' };
        return labels[type] || type;
    };

    const getMenuTypeColor = (type) => {
        const colors = { 'M': '#3b82f6', 'C': '#10b981', 'F': '#f59e0b' };
        return colors[type] || '#6b7280';
    };

    const getMenuIcon = (type, icon) => {
        if (type === 'M') return <Folder size={16} />;
        if (type === 'C') return <Menu size={16} />;
        if (type === 'F') return <File size={16} />;
        return icon || <File size={16} />;
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
                    fetchMenus();
                    addToast('success', `Menu "${row.menuName}" deleted successfully`, 3000);
                } else {
                    addToast('error', data.msg || 'Failed to delete menu', 5000);
                }
            })
            .catch(err => {
                console.error("Failed to delete menu:", err);
                addToast('error', 'Failed to delete menu', 5000);
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
            ? { ...formData, orderNum: parseInt(formData.orderNum) }
            : { ...formData, menuId: currentMenu.menuId, orderNum: parseInt(formData.orderNum) };

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
                addToast('success', `Menu "${formData.menuName}" ${modalMode === 'add' ? 'created' : 'updated'} successfully`, 3000);
                fetchMenus();
            } else {
                addToast('error', data.msg || `Failed to ${modalMode} menu`, 5000);
            }
        })
        .catch(err => {
            setSubmitting(false);
            console.error(`Failed to ${modalMode} menu:`, err);
            addToast('error', `Failed to ${modalMode} menu`, 5000);
        });
    };

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
                            Manage system menus, directories, and buttons
                        </p>
                    </div>
                </div>
                <div style={{ display: 'flex', gap: '8px' }}>
                    <button className="btn btn-primary" onClick={handleAddClick}>
                        <Plus size={16} /> Add Menu
                    </button>
                    <button className="btn btn-secondary" onClick={fetchMenus}>
                        <RefreshCw size={16} /> Refresh
                    </button>
                </div>
            </div>

            <div className="glass-panel" style={{
                padding: '16px',
                borderRadius: '12px',
                minHeight: '400px'
            }}>
                {loading ? (
                    <div style={{ textAlign: 'center', padding: '60px 20px', color: 'var(--text-muted)' }}>
                        <div className="spinner" style={{ margin: '0 auto 12px' }}></div>
                        Loading menus...
                    </div>
                ) : menus.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '60px 20px', color: 'var(--text-muted)' }}>
                        <Folder size={48} style={{ opacity: 0.3, margin: '0 auto 12px' }} />
                        No menus found. Click "Add Menu" to create one.
                    </div>
                ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                        {renderTree(menus)}
                    </div>
                )}
            </div>

            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title={`${modalMode === 'add' ? 'Add' : modalMode === 'edit' ? 'Edit' : 'View'} Menu`}
                size="lg"
            >
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px' }}>
                    <FormInput
                        label="Menu Name"
                        name="menuName"
                        value={formData.menuName}
                        onChange={handleInputChange}
                        disabled={modalMode === 'view'}
                        placeholder="Enter menu name"
                    />
                    <FormInput
                        label="Menu Type"
                        name="menuType"
                        type="select"
                        value={formData.menuType}
                        onChange={handleInputChange}
                        disabled={modalMode === 'view'}
                        options={[
                            { value: 'M', label: 'Menu' },
                            { value: 'C', label: 'Sub Menu' },
                            { value: 'F', label: 'Permission' }
                        ]}
                    />
                    <FormInput
                        label="Parent Menu"
                        name="parentName"
                        value={formData.parentName}
                        onChange={handleInputChange}
                        disabled={true}
                        placeholder="Auto-filled"
                    />
                    <FormInput
                        label="Sort Order"
                        name="orderNum"
                        value={formData.orderNum}
                        onChange={handleInputChange}
                        disabled={modalMode === 'view'}
                        type="number"
                        placeholder="0"
                    />
                    <FormInput
                        label="Path"
                        name="path"
                        value={formData.path}
                        onChange={handleInputChange}
                        disabled={modalMode === 'view'}
                        placeholder="/system/user"
                    />
                    <FormInput
                        label="Component"
                        name="component"
                        value={formData.component}
                        onChange={handleInputChange}
                        disabled={modalMode === 'view'}
                        placeholder="system/user/index"
                    />
                    <FormInput
                        label="Permission"
                        name="perms"
                        value={formData.perms}
                        onChange={handleInputChange}
                        disabled={modalMode === 'view'}
                        placeholder="system:user:list"
                    />
                    <FormInput
                        label="Icon"
                        name="icon"
                        value={formData.icon}
                        onChange={handleInputChange}
                        disabled={modalMode === 'view'}
                        placeholder="user"
                    />
                    <FormInput
                        label="Visible"
                        name="visible"
                        type="select"
                        value={formData.visible}
                        onChange={handleInputChange}
                        disabled={modalMode === 'view'}
                        options={[
                            { value: '0', label: 'Yes' },
                            { value: '1', label: 'No' }
                        ]}
                    />
                </div>

                <div style={{ display: 'flex', gap: '12px', marginTop: '24px', justifyContent: 'flex-end' }}>
                    <button className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
                        Cancel
                    </button>
                    {modalMode !== 'view' && (
                        <button
                            className="btn btn-primary"
                            onClick={handleSubmit}
                            disabled={submitting}
                        >
                            {submitting ? 'Saving...' : modalMode === 'add' ? 'Add Menu' : 'Save Changes'}
                        </button>
                    )}
                </div>
            </Modal>

            <style>{`
                .btn-icon {
                    background: transparent;
                    border: none;
                    cursor: pointer;
                    padding: 6px;
                    border-radius: 6px;
                    color: var(--text-muted);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    transition: all 0.2s;
                }
                .btn-icon:hover {
                    background: var(--bg-tertiary);
                    color: var(--text-primary);
                }
                .btn-icon.text-danger:hover {
                    background: rgba(239, 68, 68, 0.1);
                    color: #ef4444;
                }
                .spinner {
                    width: 32px;
                    height: 32px;
                    border: 3px solid var(--border-color);
                    border-top-color: var(--primary-color);
                    border-radius: 50%;
                    animation: spin 1s linear infinite;
                }
                @keyframes spin {
                    to { transform: rotate(360deg); }
                }
            `}</style>
        </div>
    );
};

export default MenuList;
