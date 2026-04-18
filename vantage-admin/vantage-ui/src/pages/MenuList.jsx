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
    const [searchQuery, setSearchQuery] = useState('');

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

    const handleExpandAll = () => {
        const allIds = {};
        const collectIds = (items) => {
            items.forEach(item => {
                if (item.children && item.children.length > 0) {
                    allIds[item.menuId] = true;
                    collectIds(item.children);
                }
            });
        };
        collectIds(menus);
        setExpandedNodes(allIds);
    };

    const handleCollapseAll = () => {
        setExpandedNodes({});
    };

    const filterMenus = (items, query) => {
        if (!query) return items;
        
        return items.filter(item => {
            const matchesCurrent = item.menuName.toLowerCase().includes(query.toLowerCase());
            const filteredChildren = item.children ? filterMenus(item.children, query) : [];
            const matchesChildren = filteredChildren.length > 0;
            
            if (matchesCurrent || matchesChildren) {
                // If query is present, we should probably expand parents that have matching children
                if (matchesChildren) {
                    setExpandedNodes(prev => ({ ...prev, [item.menuId]: true }));
                }
                return true;
            }
            return false;
        });
    };

    const filteredMenus = filterMenus(menus, searchQuery);

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
                <div key={menu.menuId} style={{ position: 'relative' }}>
                    <div style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        padding: '6px 12px',
                        background: level === 0 ? 'var(--bg-tertiary)' : 'transparent',
                        borderBottom: '1px solid var(--border-color)',
                        marginLeft: `${level * 24}px`,
                        position: 'relative',
                        borderRadius: level > 0 ? '0 8px 8px 0' : '0',
                        transition: 'all 0.2s ease',
                        cursor: 'default'
                    }}
                    className="menu-tree-row"
                    onMouseEnter={(e) => {
                        e.currentTarget.style.background = 'var(--sidebar-hover-bg)';
                        e.currentTarget.style.transform = 'translateX(4px)';
                    }}
                    onMouseLeave={(e) => {
                        e.currentTarget.style.background = level === 0 ? 'var(--bg-tertiary)' : 'transparent';
                        e.currentTarget.style.transform = 'translateX(0)';
                    }}
                    >
                        {/* Indentation Guide Line */}
                        {level > 0 && (
                            <div style={{
                                position: 'absolute',
                                left: '-12px',
                                top: '0',
                                bottom: '0',
                                width: '1px',
                                background: 'var(--border-color)',
                                opacity: 0.5
                            }} />
                        )}

                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flex: 1, minWidth: 0 }}>
                            {hasChildren ? (
                                <button
                                    onClick={() => toggleExpand(menu.menuId)}
                                    style={{ 
                                        background: 'var(--bg-secondary)', 
                                        border: '1px solid var(--border-color)', 
                                        cursor: 'pointer',
                                        padding: '2px',
                                        width: '20px',
                                        height: '20px',
                                        borderRadius: '4px',
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        color: 'var(--text-secondary)',
                                        zIndex: 1
                                    }}
                                >
                                    {isExpanded ? <ChevronDown size={12} /> : <ChevronRight size={12} />}
                                </button>
                            ) : (
                                <div style={{ width: '20px', display: 'flex', justifyContent: 'center' }}>
                                    <div style={{ width: '4px', height: '4px', borderRadius: '50%', background: 'var(--border-color)' }} />
                                </div>
                            )}
                            
                            <div style={{ 
                                width: '28px', 
                                height: '28px', 
                                borderRadius: '6px', 
                                background: 'var(--bg-secondary)',
                                border: '1px solid var(--border-color)',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                color: 'var(--primary-color)',
                                flexShrink: 0
                            }}>
                                {getMenuIcon(menu.menuType)}
                            </div>
                            
                            <div style={{ flex: 1, minWidth: 0 }}>
                                <div style={{ fontWeight: 600, fontSize: '13px', color: 'var(--text-primary)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                    {menu.menuName}
                                </div>
                                <div style={{ fontSize: '10px', color: 'var(--text-muted)', display: 'flex', gap: '8px', alignItems: 'center' }}>
                                    <span style={{ 
                                        padding: '1px 4px', 
                                        borderRadius: '4px', 
                                        background: menu.menuType === 'M' ? 'rgba(59, 130, 246, 0.1)' : 'rgba(100, 116, 139, 0.1)',
                                        color: menu.menuType === 'M' ? '#3b82f6' : 'inherit'
                                    }}>
                                        {getMenuTypeLabel(menu.menuType)}
                                    </span>
                                    {menu.perms && (
                                        <span style={{ opacity: 0.7 }}>• {menu.perms}</span>
                                    )}
                                    <span style={{ opacity: 0.7 }}>• Order: {menu.orderNum}</span>
                                </div>
                            </div>
                        </div>

                        <div style={{ display: 'flex', gap: '4px' }} className="row-actions">
                            {menu.menuType !== 'F' && (
                                <button
                                    onClick={() => handleAddClick(menu)}
                                    className="btn btn-secondary"
                                    style={{ width: '26px', height: '26px', padding: 0 }}
                                    title="Add sub-menu"
                                >
                                    <Plus size={14} />
                                </button>
                            )}
                            <button
                                onClick={() => handleEditClick(menu)}
                                className="btn btn-secondary"
                                style={{ width: '26px', height: '26px', padding: 0 }}
                                title="Edit"
                            >
                                <Edit size={14} />
                            </button>
                            <button
                                onClick={() => handleDeleteClick(menu)}
                                className="btn btn-secondary"
                                style={{ 
                                    width: '26px', 
                                    height: '26px', 
                                    padding: 0,
                                    color: 'var(--danger)',
                                    borderColor: 'rgba(239, 68, 68, 0.2)'
                                }}
                                title="Delete"
                            >
                                <Trash2 size={14} />
                            </button>
                        </div>
                    </div>

                    {hasChildren && isExpanded && (
                        <div className="menu-subtree animate-fade-in">
                            {renderMenuTree(menu.children, level + 1)}
                        </div>
                    )}
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
        <div className="page-container" style={{ position: 'relative' }}>
            <div className="mesh-container">
                <div className="mesh-blob" style={{ top: '15%', left: '80%', width: '300px', height: '300px', background: 'rgba(79, 172, 254, 0.1)' }}></div>
                <div className="mesh-blob" style={{ top: '70%', left: '10%', width: '250px', height: '250px', background: 'rgba(0, 242, 254, 0.1)' }}></div>
            </div>

            <div className="page-header" style={{ zIndex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div className="header-icon-wrapper" style={{ background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' }}>
                        <Menu size={18} />
                    </div>
                    <div>
                        <h2>Menu Management</h2>
                        <p>Configure navigation hierarchy and permissions</p>
                    </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{ position: 'relative' }}>
                        <input
                            type="text"
                            placeholder="Search menus..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            style={{
                                padding: '8px 12px 8px 36px',
                                borderRadius: '10px',
                                border: '1px solid var(--border-color)',
                                background: 'var(--bg-tertiary)',
                                fontSize: '12px',
                                width: '220px',
                                outline: 'none',
                                transition: 'all 0.2s'
                            }}
                            onFocus={(e) => e.target.style.borderColor = 'var(--primary-color)'}
                            onBlur={(e) => e.target.style.borderColor = 'var(--border-color)'}
                        />
                        <div style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }}>
                            <RefreshCw size={14} style={{ animation: loading ? 'spin 2s linear infinite' : 'none' }} />
                        </div>
                    </div>

                    <div style={{ display: 'flex', gap: '6px' }}>
                        <button onClick={handleExpandAll} className="btn btn-secondary" style={{ padding: '8px 12px', borderRadius: '10px' }}>Expand All</button>
                        <button onClick={handleCollapseAll} className="btn btn-secondary" style={{ padding: '8px 12px', borderRadius: '10px' }}>Collapse All</button>
                    </div>

                    <div style={{ width: '1px', height: '24px', background: 'var(--border-color)', margin: '0 4px' }} />
                    <button
                        className="btn btn-primary"
                        onClick={() => handleAddClick(null)}
                        style={{ padding: '8px 16px', borderRadius: '10px' }}
                    >
                        <Plus size={16} />
                        Add Menu
                    </button>
                </div>
            </div>

            {/* Menu Tree */}
            <div style={{
                flex: 1,
                background: 'var(--bg-secondary)',
                borderRadius: '12px',
                border: '1px solid var(--border-color)',
                overflow: 'auto',
                boxShadow: 'inset 0 2px 4px rgba(0,0,0,0.02)'
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
                    <div style={{ padding: '4px 0' }}>
                        {renderMenuTree(filteredMenus)}
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
