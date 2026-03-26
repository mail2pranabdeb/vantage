import { useState, useEffect } from 'react';
import { Shield, Plus, Edit, Trash2, RefreshCw, Check, X, ChevronRight, ChevronDown, Folder, File } from 'lucide-react';
import { useToast } from '../components/Toast';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';

const RoleList = () => {
    const { addToast } = useToast();
    const [roles, setRoles] = useState([]);
    const [menus, setMenus] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isPermissionModalOpen, setIsPermissionModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('add');
    const [currentRole, setCurrentRole] = useState(null);
    const [expandedNodes, setExpandedNodes] = useState({});
    const [checkedMenus, setCheckedMenus] = useState([]);
    const [formData, setFormData] = useState({
        roleName: '',
        roleKey: '',
        roleSort: '1',
        dataScope: '1',
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
                setLoading(false);
                if (data.code === 200) {
                    setRoles(data.data || []);
                    addToast('success', `Loaded ${data.data.length} role(s)`, 2000);
                } else {
                    addToast('error', data.msg || 'Failed to load roles', 4000);
                }
            })
            .catch(err => {
                console.error("Failed to fetch roles:", err);
                setLoading(false);
                addToast('error', 'Failed to load roles', 5000);
            });
    };

    const fetchMenus = () => {
        return fetch('/api/system/menu/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    return buildMenuTree(data.data || []);
                }
                return [];
            });
    };

    const buildMenuTree = (menuItems, parentId = '0') => {
        const filtered = menuItems.filter(m => String(m.parentId) === String(parentId));
        return filtered.map(m => ({
            ...m,
            children: buildMenuTree(menuItems, String(m.menuId))
        }));
    };

    const fetchRoleMenus = (roleId) => {
        fetch(`/api/system/role/menuIds/${roleId}`)
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setCheckedMenus(data.data || []);
                }
            });
    };

    const handleAddClick = () => {
        setModalMode('add');
        setCurrentRole(null);
        setFormData({
            roleName: '',
            roleKey: '',
            roleSort: '1',
            dataScope: '1',
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
            roleSort: row.roleSort?.toString() || '1',
            dataScope: row.dataScope || '1',
            status: row.status || '0',
            remark: row.remark || ''
        });
        setIsModalOpen(true);
    };

    const handlePermissionClick = async (row) => {
        setCurrentRole(row);
        const menuTree = await fetchMenus();
        setMenus(menuTree);
        
        // Fetch role menus
        fetchRoleMenus(row.roleId);
        
        // If admin role, check all menus by default
        if (row.roleKey === 'admin') {
            const allMenuIds = getAllMenuIds(menuTree);
            setCheckedMenus(allMenuIds);
        }
        
        setIsPermissionModalOpen(true);
    };

    const getAllMenuIds = (menuList) => {
        let ids = [];
        for (const menu of menuList) {
            ids.push(menu.menuId);
            if (menu.children && menu.children.length > 0) {
                ids = ids.concat(getAllMenuIds(menu.children));
            }
        }
        return ids;
    };

    const handleDeleteClick = (row) => {
        if (window.confirm(`Are you sure you want to delete role "${row.roleName}"?`)) {
            fetch(`/api/system/role/${row.roleId}`, {
                method: 'DELETE'
            })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    addToast('success', `Role "${row.roleName}" deleted successfully`, 3000);
                    fetchRoles();
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
        if (!formData.roleName || !formData.roleKey) {
            addToast('error', 'Role name and key are required', 3000);
            return;
        }

        setSubmitting(true);

        const url = modalMode === 'add' ? '/api/system/role' : '/api/system/role';
        const method = modalMode === 'add' ? 'POST' : 'PUT';
        const body = { 
            ...formData, 
            roleId: modalMode === 'edit' ? currentRole.roleId : null,
            roleSort: parseInt(formData.roleSort)
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
                addToast('success', data.msg || `Role ${modalMode === 'add' ? 'added' : 'updated'} successfully`, 3000);
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

    const handleSavePermissions = () => {
        if (!currentRole) return;

        setSubmitting(true);

        fetch('/api/system/role/authDataScope', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                roleId: currentRole.roleId,
                menuIds: checkedMenus
            })
        })
        .then(res => res.json())
        .then(data => {
            setSubmitting(false);
            if (data.code === 200) {
                setIsPermissionModalOpen(false);
                addToast('success', 'Permissions updated successfully', 3000);
            } else {
                addToast('error', data.msg || 'Failed to update permissions', 5000);
            }
        })
        .catch(err => {
            setSubmitting(false);
            console.error("Failed to update permissions:", err);
            addToast('error', 'Failed to update permissions', 5000);
        });
    };

    const toggleExpand = (menuId) => {
        setExpandedNodes(prev => ({
            ...prev,
            [menuId]: !prev[menuId]
        }));
    };

    const toggleMenuCheck = (menuId, hasChildren) => {
        if (checkedMenus.includes(menuId)) {
            // Uncheck - remove from list
            setCheckedMenus(prev => prev.filter(id => id !== menuId));
            // If unchecking parent, uncheck all children recursively
            if (hasChildren) {
                uncheckChildren(menuId);
            }
        } else {
            // Check - add to list
            setCheckedMenus(prev => [...prev, menuId]);
        }
    };

    const uncheckChildren = (menuId) => {
        const menu = findMenuById(menus, menuId);
        if (menu && menu.children) {
            menu.children.forEach(child => {
                setCheckedMenus(prev => prev.filter(id => id !== child.menuId));
                uncheckChildren(child.menuId);
            });
        }
    };

    const findMenuById = (menuList, menuId) => {
        for (const menu of menuList) {
            if (menu.menuId === menuId) return menu;
            if (menu.children) {
                const found = findMenuById(menu.children, menuId);
                if (found) return found;
            }
        }
        return null;
    };

    const getMenuIcon = (menuType) => {
        switch(menuType) {
            case 'M': return <Folder size={16} />;
            case 'C': return <File size={16} />;
            case 'F': return <File size={14} />;
            default: return <Folder size={16} />;
        }
    };

    const renderMenuTree = (menuItems, level = 0) => {
        return menuItems.map(menu => {
            const hasChildren = menu.children && menu.children.length > 0;
            const isExpanded = expandedNodes[menu.menuId];
            const isChecked = checkedMenus.includes(menu.menuId);

            return (
                <div key={menu.menuId}>
                    <div style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        padding: '6px 10px',
                        background: level === 0 ? 'var(--bg-secondary)' : 'transparent',
                        borderBottom: '1px solid var(--border-color)',
                        marginLeft: `${level * 20}px`,
                        transition: 'background 0.2s'
                    }}
                    onMouseEnter={(e) => e.currentTarget.style.background = 'var(--bg-tertiary)'}
                    onMouseLeave={(e) => e.currentTarget.style.background = level === 0 ? 'var(--bg-secondary)' : 'transparent'}
                    >
                        {/* Checkbox */}
                        <button
                            onClick={() => toggleMenuCheck(menu.menuId, hasChildren)}
                            style={{
                                width: '18px',
                                height: '18px',
                                borderRadius: '4px',
                                border: '2px solid ' + (isChecked ? 'var(--primary-color)' : 'var(--border-color)'),
                                background: isChecked ? 'var(--primary-color)' : 'transparent',
                                color: 'white',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                cursor: 'pointer',
                                flexShrink: 0
                            }}
                        >
                            {isChecked && <Check size={12} />}
                        </button>

                        {/* Expand/Collapse */}
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
                            <span style={{ width: '24px', flexShrink: 0 }} />
                        )}
                        
                        {/* Menu Icon */}
                        <span style={{ color: 'var(--primary-color)', flexShrink: 0 }}>
                            {getMenuIcon(menu.menuType)}
                        </span>
                        
                        {/* Menu Name */}
                        <div style={{ flex: 1, fontSize: '13px' }}>
                            {menu.menuName}
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
            onClick: fetchRoles
        },
        {
            label: 'Add Role',
            icon: Plus,
            onClick: handleAddClick
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
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <Shield size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '14px', fontWeight: 700, margin: 0 }}>Role Management</h2>
                        <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Manage roles and permissions
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

            {/* Roles Table */}
            <div style={{
                background: 'var(--bg-secondary)',
                borderRadius: '8px',
                border: '1px solid var(--border-color)',
                overflow: 'hidden'
            }}>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                        <tr style={{ background: 'var(--bg-tertiary)', borderBottom: '2px solid var(--border-color)' }}>
                            <th style={{ padding: '10px', textAlign: 'left' }}>Role Name</th>
                            <th style={{ padding: '10px', textAlign: 'left' }}>Role Key</th>
                            <th style={{ padding: '10px', textAlign: 'center' }}>Sort</th>
                            <th style={{ padding: '10px', textAlign: 'center' }}>Status</th>
                            <th style={{ padding: '10px', textAlign: 'center' }}>Create Time</th>
                            <th style={{ padding: '10px', textAlign: 'center', width: '280px' }}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {loading ? (
                            <tr>
                                <td colSpan={6} style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                                    Loading roles...
                                </td>
                            </tr>
                        ) : roles.length === 0 ? (
                            <tr>
                                <td colSpan={6} style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                                    No roles found. Click "Add Role" to create one.
                                </td>
                            </tr>
                        ) : (
                            roles.map(role => (
                                <tr key={role.roleId} style={{ borderBottom: '1px solid var(--border-color)' }}>
                                    <td style={{ padding: '10px', fontWeight: 500 }}>{role.roleName}</td>
                                    <td style={{ padding: '10px', fontFamily: 'monospace', fontSize: '12px' }}>{role.roleKey}</td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}>{role.roleSort}</td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}>
                                        <span className={`status-pill ${role.status === '0' ? 'active' : 'inactive'}`}>
                                            {role.status === '0' ? 'Normal' : 'Disabled'}
                                        </span>
                                    </td>
                                    <td style={{ padding: '10px', textAlign: 'center', fontSize: '12px' }}>
                                        {role.createTime?.replace('T', ' ')}
                                    </td>
                                    <td style={{ padding: '10px', textAlign: 'center' }}>
                                        <div style={{ display: 'flex', gap: '4px', justifyContent: 'center' }}>
                                            <button
                                                onClick={() => handlePermissionClick(role)}
                                                className="btn btn-secondary"
                                                style={{ padding: '4px 8px', fontSize: '11px' }}
                                                title="Permissions"
                                            >
                                                <Shield size={14} />
                                            </button>
                                            <button
                                                onClick={() => handleEditClick(role)}
                                                className="btn btn-secondary"
                                                style={{ padding: '4px 8px', fontSize: '11px' }}
                                                title="Edit"
                                            >
                                                <Edit size={14} />
                                            </button>
                                            <button
                                                onClick={() => handleDeleteClick(role)}
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
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {/* Add/Edit Role Modal */}
            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title={modalMode === 'add' ? 'Add Role' : 'Edit Role'}
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
                            label="Role Name *"
                            name="roleName"
                            value={formData.roleName}
                            onChange={handleInputChange}
                            placeholder="e.g., Administrator"
                            disabled={submitting}
                        />
                        <FormInput
                            label="Role Key *"
                            name="roleKey"
                            value={formData.roleKey}
                            onChange={handleInputChange}
                            placeholder="e.g., admin"
                            disabled={submitting}
                        />
                    </div>

                    <div className="form-row">
                        <FormInput
                            label="Sort Order"
                            name="roleSort"
                            type="number"
                            value={formData.roleSort}
                            onChange={handleInputChange}
                            placeholder="1"
                            disabled={submitting}
                        />
                        <div className="form-group">
                            <label className="form-label">Data Scope</label>
                            <select
                                name="dataScope"
                                value={formData.dataScope}
                                onChange={handleInputChange}
                                className="form-input"
                                disabled={submitting}
                            >
                                <option value="1">All Data</option>
                                <option value="2">Custom Data</option>
                                <option value="3">Department Data</option>
                                <option value="4">Department and Below</option>
                                <option value="5">Only Self Data</option>
                            </select>
                        </div>
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label className="form-label">Status</label>
                            <select
                                name="status"
                                value={formData.status}
                                onChange={handleInputChange}
                                className="form-input"
                                disabled={submitting}
                            >
                                <option value="0">Normal</option>
                                <option value="1">Disabled</option>
                            </select>
                        </div>
                        <FormInput
                            label="Remark"
                            name="remark"
                            value={formData.remark}
                            onChange={handleInputChange}
                            placeholder="Optional notes"
                            disabled={submitting}
                        />
                    </div>
                </div>
            </Modal>

            {/* Permissions Modal */}
            <Modal
                isOpen={isPermissionModalOpen}
                onClose={() => setIsPermissionModalOpen(false)}
                title={`Permissions: ${currentRole?.roleName || ''}`}
                size="large"
                footer={
                    <>
                        <button
                            className="btn btn-secondary"
                            onClick={() => setIsPermissionModalOpen(false)}
                            disabled={submitting}
                        >
                            Cancel
                        </button>
                        <button
                            className="btn btn-primary"
                            onClick={handleSavePermissions}
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
                            <Check size={16} />
                            Save Permissions
                        </button>
                    </>
                }
            >
                <div style={{ 
                    maxHeight: '60vh', 
                    overflowY: 'auto',
                    border: '1px solid var(--border-color)',
                    borderRadius: '8px'
                }}>
                    {menus.length > 0 ? (
                        renderMenuTree(menus)
                    ) : (
                        <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                            Loading menus...
                        </div>
                    )}
                </div>
                <div style={{ 
                    marginTop: '12px', 
                    padding: '10px', 
                    background: 'var(--bg-tertiary)', 
                    borderRadius: '6px',
                    fontSize: '12px'
                }}>
                    <strong>Selected:</strong> {checkedMenus.length} menu(s)
                </div>
            </Modal>
        </div>
    );
};

export default RoleList;
