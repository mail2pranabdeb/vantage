import { useState, useEffect } from 'react';
import { 
    Menu, Plus, Edit, Trash2, RefreshCw, ChevronRight, 
    ChevronDown, Folder, FolderOpen, File, X
} from 'lucide-react';
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
        parentId: '0',
        parentName: '',
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
                console.log('Menu API response:', data);
                if (data.code === 200 && data.data) {
                    console.log('Building menu tree from', data.data.length, 'items');
                    const menuTree = buildMenuTree(data.data || []);
                    console.log('Menu tree:', menuTree);
                    setMenus(menuTree);
                    addToast('success', `Loaded ${data.data.length} menu item(s)`, 2000);
                } else {
                    addToast('error', data.msg || 'Failed to load menus', 4000);
                }
            })
            .catch(err => {
                console.error("Failed to fetch menus:", err);
                setLoading(false);
                addToast('error', 'Failed to load menus', 5000);
            });
    };

    const buildMenuTree = (menus, parentId = '0') => {
        const filtered = menus.filter(m => String(m.parentId) === String(parentId));
        return filtered.map(m => ({
            ...m,
            children: buildMenuTree(menus, String(m.menuId))
        }));
    };

    const handleAddClick = (parentMenu = null) => {
        setModalMode('add');
        setCurrentMenu(null);
        setFormData({
            menuName: '',
            menuType: parentMenu ? 'C' : 'M',
            parentId: parentMenu ? parentMenu.menuId.toString() : '0',
            parentName: parentMenu ? parentMenu.menuName : '',
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
            parentId: row.parentId?.toString() || '0',
            parentName: row.parentName || '',
            path: row.path || '',
            component: row.component || '',
            query: row.query || '',
            perms: row.perms || '',
            icon: row.icon || '',
            orderNum: row.orderNum?.toString() || '0',
            visible: row.visible || '0'
        });
        setIsModalOpen(true);
    };

    const handleDeleteClick = (row) => {
        if (window.confirm(`Are you sure you want to delete menu "${row.menuName}"? This will also delete all submenus.`)) {
            fetch(`/api/system/menu/${row.menuId}`, {
                method: 'DELETE'
            })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    addToast('success', `Menu "${row.menuName}" deleted successfully`, 3000);
                    fetchMenus();
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
        if (!formData.menuName) {
            addToast('error', 'Menu name is required', 3000);
            return;
        }

        setSubmitting(true);

        const url = modalMode === 'add' ? '/api/system/menu' : '/api/system/menu';
        const method = modalMode === 'add' ? 'POST' : 'PUT';
        const body = { 
            ...formData, 
            menuId: modalMode === 'edit' ? currentMenu.menuId : null,
            orderNum: parseInt(formData.orderNum)
        };

        fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        })
        .then(res => res.json())
        .then(data => {
            setSubmitting(false);
            if (data.code === 200) {
                setIsModalOpen(false);
                addToast('success', data.msg || `Menu ${modalMode === 'add' ? 'added' : 'updated'} successfully`, 3000);
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

    const toggleExpand = (menuId) => {
        setExpandedNodes(prev => ({
            ...prev,
            [menuId]: !prev[menuId]
        }));
    };

    const getMenuIcon = (menuType) => {
        switch(menuType) {
            case 'M': return <Folder size={16} />;
            case 'C': return <File size={16} />;
            case 'F': return <File size={14} />;
            default: return <Folder size={16} />;
        }
    };

    const getMenuTypeLabel = (menuType) => {
        switch(menuType) {
            case 'M': return 'Directory';
            case 'C': return 'Menu';
            case 'F': return 'Button';
            default: return menuType;
        }
    };

    const renderMenuTree = (menuItems, level = 0) => {
        if (!menuItems || menuItems.length === 0) {
            return null;
        }

        return menuItems.map(menu => {
            const hasChildren = menu.children && menu.children.length > 0;
            const isExpanded = expandedNodes[menu.menuId];

            return (
                <div key={menu.menuId}>
                    <div style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        padding: '8px 10px',
                        background: level === 0 ? 'var(--bg-secondary)' : 'transparent',
                        borderBottom: '1px solid var(--border-color)',
                        marginLeft: `${level * 20}px`,
                        transition: 'background 0.2s'
                    }}
                    onMouseEnter={(e) => e.currentTarget.style.background = 'var(--bg-tertiary)'}
                    onMouseLeave={(e) => e.currentTarget.style.background = level === 0 ? 'var(--bg-secondary)' : 'transparent'}
                    >
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flex: 1 }}>
                            {hasChildren ? (
                                <button
                                    onClick={() => toggleExpand(menu.menuId)}
                                    style={{ 
                                        background: 'none', 
                                        border: 'none', 
                                        cursor: 'pointer',
                                        padding: '4px',
                                        display: 'flex',
                                        alignItems: 'center',
                                        color: 'var(--text-secondary)'
                                    }}
                                >
                                    {isExpanded ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
                                </button>
                            ) : (
                                <span style={{ width: '24px' }} />
                            )}
                            
                            <span style={{ color: 'var(--primary-color)' }}>
                                {getMenuIcon(menu.menuType)}
                            </span>
                            
                            <div style={{ flex: 1 }}>
                                <div style={{ fontWeight: 500, fontSize: '13px' }}>
                                    {menu.menuName}
                                </div>
                                <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                                    {getMenuTypeLabel(menu.menuType)}
                                    {menu.perms && ` • ${menu.perms}`}
                                </div>
                            </div>
                        </div>

                        <div style={{ display: 'flex', gap: '4px' }}>
                            {menu.menuType !== 'F' && (
                                <button
                                    onClick={() => handleAddClick(menu)}
                                    className="btn btn-secondary"
                                    style={{ padding: '4px 8px', fontSize: '11px' }}
                                    title="Add submenu"
                                >
                                    <Plus size={14} />
                                </button>
                            )}
                            <button
                                onClick={() => handleEditClick(menu)}
                                className="btn btn-secondary"
                                style={{ padding: '4px 8px', fontSize: '11px' }}
                                title="Edit"
                            >
                                <Edit size={14} />
                            </button>
                            <button
                                onClick={() => handleDeleteClick(menu)}
                                className="btn btn-secondary"
                                style={{ 
                                    padding: '4px 8px', 
                                    fontSize: '11px',
                                    color: 'var(--danger)',
                                    borderColor: 'var(--danger)'
                                }}
                                title="Delete"
                            >
                                <Trash2 size={14} />
                            </button>
                        </div>
                    </div>

                    {hasChildren && isExpanded && renderMenuTree(menu.children, level + 1)}
                </div>
            );
        });
    };

    const toolbarActions = [
        {
            label: 'Refresh',
            icon: RefreshCw,
            onClick: fetchMenus
        },
        {
            label: 'Add Root Menu',
            icon: Plus,
            onClick: () => handleAddClick(null)
        }
    ];

    return (
        <div style={{
            height: 'calc(100vh - 50px)',
            overflow: 'auto',
            padding: '8px'
        }}>
            <div className="page-header" style={{ marginBottom: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{
                        width: '40px',
                        height: '40px',
                        borderRadius: '10px',
                        background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <Menu size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '14px', fontWeight: 700, margin: 0 }}>Menu Management</h2>
                        <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Manage system menus, directories, and buttons
                        </p>
                    </div>
                </div>
                <div style={{ display: 'flex', gap: '8px' }}>
                    {toolbarActions.map((action, idx) => (
                        <button
                            key={idx}
                            className="btn btn-primary"
                            onClick={action.onClick}
                            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
                        >
                            <action.icon size={16} />
                            {action.label}
                        </button>
                    ))}
                </div>
            </div>

            {/* Menu Tree */}
            <div style={{
                background: 'var(--bg-secondary)',
                borderRadius: '8px',
                border: '1px solid var(--border-color)',
                overflow: 'hidden'
            }}>
                {loading ? (
                    <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                        Loading menus...
                    </div>
                ) : menus.length === 0 ? (
                    <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                        <Menu size={48} style={{ margin: '0 auto 16px', opacity: 0.3 }} />
                        <p>No menus found. Click "Add Root Menu" to create one.</p>
                    </div>
                ) : (
                    <div style={{ padding: '10px 0' }}>
                        {renderMenuTree(menus)}
                    </div>
                )}
            </div>

            {/* Add/Edit Modal */}
            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title={modalMode === 'add' ? 'Add Menu' : 'Edit Menu'}
                size="medium"
                footer={
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
                            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
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
                }
            >
                <div style={{ maxHeight: '60vh', overflowY: 'auto', paddingRight: '8px' }}>
                    <div className="form-row">
                        <FormInput
                            label="Menu Name *"
                            name="menuName"
                            value={formData.menuName}
                            onChange={handleInputChange}
                            placeholder="e.g., User Management"
                            disabled={submitting}
                        />
                        <div className="form-group">
                            <label className="form-label">Menu Type</label>
                            <select
                                name="menuType"
                                value={formData.menuType}
                                onChange={handleInputChange}
                                className="form-input"
                                disabled={submitting}
                            >
                                <option value="M">Directory</option>
                                <option value="C">Menu</option>
                                <option value="F">Button</option>
                            </select>
                        </div>
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label className="form-label">Parent Menu</label>
                            <input
                                type="text"
                                className="form-input"
                                value={formData.parentName || 'Root'}
                                disabled
                                placeholder="Root"
                            />
                            <small className="form-help">Parent: {formData.parentId === '0' ? 'Root' : formData.parentName}</small>
                        </div>
                        <FormInput
                            label="Order Number"
                            name="orderNum"
                            type="number"
                            value={formData.orderNum}
                            onChange={handleInputChange}
                            placeholder="0"
                            disabled={submitting}
                        />
                    </div>

                    <div className="form-row">
                        <FormInput
                            label="Path"
                            name="path"
                            value={formData.path}
                            onChange={handleInputChange}
                            placeholder="e.g., /system/user"
                            disabled={submitting}
                        />
                        <FormInput
                            label="Component"
                            name="component"
                            value={formData.component}
                            onChange={handleInputChange}
                            placeholder="e.g., system/user/index"
                            disabled={submitting}
                        />
                    </div>

                    <div className="form-row">
                        <FormInput
                            label="Icon"
                            name="icon"
                            value={formData.icon}
                            onChange={handleInputChange}
                            placeholder="e.g., fa fa-user"
                            disabled={submitting}
                        />
                        <FormInput
                            label="Permission"
                            name="perms"
                            value={formData.perms}
                            onChange={handleInputChange}
                            placeholder="e.g., system:user:list"
                            disabled={submitting}
                        />
                    </div>

                    <div className="form-row">
                        <FormInput
                            label="Query Params"
                            name="query"
                            value={formData.query}
                            onChange={handleInputChange}
                            placeholder="e.g., id=1&type=list"
                            disabled={submitting}
                        />
                        <div className="form-group">
                            <label className="form-label">Visible</label>
                            <select
                                name="visible"
                                value={formData.visible}
                                onChange={handleInputChange}
                                className="form-input"
                                disabled={submitting}
                            >
                                <option value="0">Show</option>
                                <option value="1">Hide</option>
                            </select>
                        </div>
                    </div>
                </div>
            </Modal>
        </div>
    );
};

export default MenuList;
