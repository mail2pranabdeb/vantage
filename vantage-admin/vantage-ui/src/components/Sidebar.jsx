import { useState, useEffect } from 'react';
import { NavLink } from 'react-router-dom';
import {
    Home, Users, Shield, Menu, Settings, ChevronLeft, ChevronDown,
    LayoutDashboard, ChevronRight
} from 'lucide-react';

// Icon mapping from DB strings to Lucide components
const iconMap = {
    'fa fa-gear': Settings,
    'fa fa-user-o': Users,
    'fa fa-users': Users,
    'fa fa-lock': Shield,
    'fa fa-list': Menu,
    'fa fa-dashboard': LayoutDashboard,
    'fa fa-bell': Settings,
    'fa fa-th-list': Menu,
    'fa fa-sun-o': Settings,
    'fa fa-bookmark-o': Settings,
    'fa fa-address-card-o': Users,
    'fa fa-file-image-o': Settings,
    'fa fa-bullhorn': Settings,
    'fa fa-tasks': Settings,
    'fa fa-clock-o': Home,
    'fa fa-file-text-o': Settings,
    'fa fa-code': Shield,
    '#': Menu
};

const Sidebar = ({ isCollapsed, toggleSidebar }) => {
    const [menus, setMenus] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [expandedMenus, setExpandedMenus] = useState({});

    useEffect(() => {
        const fetchMenus = async () => {
            try {
                setError(null);
                const response = await fetch('/api/system/menu/tree');
                
                // Check if response is JSON (not HTML login page)
                const contentType = response.headers.get('content-type');
                if (!contentType || !contentType.includes('application/json')) {
                    console.log('Received non-JSON response, likely login page');
                    setLoading(false);
                    return;
                }
                
                const data = await response.json();
                console.log('Menu API Response:', data);

                if (data.code === 200 && data.data && data.data.length > 0) {
                    setMenus(data.data);
                    // Keep all parent menus collapsed by default
                    setExpandedMenus({});
                } else if (data.code === 401 || data.code === 403) {
                    // Not authenticated, will redirect
                    console.log('Not authenticated, waiting for login...');
                } else {
                    console.warn('Menu API returned no data or unexpected format');
                    setMenus([]);
                }
            } catch (err) {
                console.error("Failed to fetch menus:", err);
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };
        fetchMenus();
    }, []);

    const renderIcon = (iconStr) => {
        const Icon = iconMap[iconStr] || Menu;
        return <Icon size={16} style={{ minWidth: '16px', flexShrink: 0 }} />;
    };

    const toggleExpand = (menuId) => {
        setExpandedMenus(prev => ({
            ...prev,
            [menuId]: !prev[menuId]
        }));
    };

    return (
        <aside className={`sidebar ${isCollapsed ? 'collapsed' : ''}`}>
            {/* Logo */}
            <div className="sidebar-header">
                {!isCollapsed && (
                    <span className="sidebar-logo-text">
                        VANTAGE ADMIN
                    </span>
                )}
                <button onClick={toggleSidebar} className="sidebar-toggle">
                    <ChevronLeft size={16} />
                </button>
            </div>

            {/* Nav Items */}
            <nav className="sidebar-nav">
                {/* Dashboard */}
                <NavLink
                    to="/dashboard"
                    className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
                    title={isCollapsed ? 'Dashboard' : undefined}
                >
                    <Home size={16} />
                    {!isCollapsed && <span>Dashboard</span>}
                </NavLink>

                {loading ? (
                    <div className="sidebar-loading">
                        {!isCollapsed && 'Loading menus...'}
                    </div>
                ) : error ? (
                    <div className="sidebar-error">
                        {!isCollapsed && `Error: ${error}`}
                    </div>
                ) : menus.length === 0 ? (
                    <div className="sidebar-empty">
                        {!isCollapsed && 'No menus available'}
                    </div>
                ) : (
                    menus.map((menu) => {
                        const hasChildren = menu.children && menu.children.length > 0;
                        const isExpanded = expandedMenus[menu.menuId];
                        const isParent = menu.menuType === 'M';

                        if (isParent && hasChildren) {
                            return (
                                <div key={menu.menuId} className="menu-group">
                                    <button
                                        onClick={() => toggleExpand(menu.menuId)}
                                        className="nav-item parent-item"
                                        title={isCollapsed ? menu.menuName : undefined}
                                    >
                                        <div className="nav-item-content">
                                            {renderIcon(menu.icon)}
                                            {!isCollapsed && <span>{menu.menuName}</span>}
                                        </div>
                                        {!isCollapsed && (
                                            <ChevronDown
                                                size={12}
                                                className={`chevron-icon ${isExpanded ? 'expanded' : ''}`}
                                            />
                                        )}
                                    </button>

                                    {/* Submenu */}
                                    {!isCollapsed && isExpanded && hasChildren && (
                                        <div className="submenu">
                                            {menu.children.map(child => (
                                                <NavLink
                                                    key={child.menuId}
                                                    to={child.url}
                                                    className={({ isActive }) => `nav-item sub-item ${isActive ? 'active' : ''}`}
                                                >
                                                    <span className="sub-item-dot" />
                                                    <span className="sub-item-text">{child.menuName}</span>
                                                </NavLink>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            );
                        } else {
                            // Single menu item (no children)
                            return (
                                <NavLink
                                    key={menu.menuId}
                                    to={menu.url}
                                    className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
                                    title={isCollapsed ? menu.menuName : undefined}
                                >
                                    {renderIcon(menu.icon)}
                                    {!isCollapsed && <span>{menu.menuName}</span>}
                                </NavLink>
                            );
                        }
                    })
                )}
            </nav>

            {/* Footer */}
            {!isCollapsed && (
                <div className="sidebar-footer">
                    Vantage Admin v1.0
                </div>
            )}

            <style>{`
                /* Sidebar base styles */
                .sidebar {
                    width: 240px;
                    transition: width 0.25s ease;
                    overflow-x: hidden;
                    overflow-y: auto;
                    display: flex;
                    flex-direction: column;
                    background: var(--sidebar-bg);
                    border-right: 1px solid var(--border-color);
                    height: 100vh;
                    position: sticky;
                    top: 0;
                }

                .sidebar.collapsed {
                    width: 64px;
                }

                .sidebar-header {
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    padding: 16px 12px;
                    border-bottom: 1px solid var(--border-color);
                    flex-shrink: 0;
                }

                .sidebar.collapsed .sidebar-header {
                    justify-content: center;
                }

                .sidebar-logo-text {
                    font-size: 14px;
                    font-weight: 800;
                    color: var(--sidebar-text-primary);
                    letter-spacing: 0.5px;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                }

                .sidebar-toggle {
                    background: var(--sidebar-hover-bg);
                    border: none;
                    cursor: pointer;
                    color: var(--sidebar-text-primary);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    padding: 6px;
                    border-radius: 6px;
                    transition: all 0.25s ease;
                    flex-shrink: 0;
                }

                .sidebar.collapsed .sidebar-toggle {
                    transform: rotate(180deg);
                }

                .sidebar-toggle:hover {
                    background: var(--border-color);
                }

                .sidebar-nav {
                    display: flex;
                    flex-direction: column;
                    flex: 1;
                    padding: 8px;
                    overflow-y: auto;
                    overflow-x: hidden;
                    gap: 4px;
                }

                .sidebar-loading,
                .sidebar-error,
                .sidebar-empty {
                    padding: 20px;
                    text-align: center;
                    font-size: 12px;
                }

                .sidebar-loading,
                .sidebar-empty {
                    color: var(--sidebar-text-muted);
                }

                .sidebar-error {
                    color: var(--danger);
                    font-size: 11px;
                }

                .sidebar-footer {
                    padding: 12px;
                    border-top: 1px solid var(--border-color);
                    font-size: 10px;
                    color: var(--sidebar-text-muted);
                    text-align: center;
                    flex-shrink: 0;
                }

                /* Nav items */
                .nav-item {
                    display: flex;
                    align-items: center;
                    gap: 8px;
                    padding: 8px 10px;
                    border-radius: 6px;
                    font-size: 13px;
                    font-weight: 500;
                    color: var(--sidebar-text-secondary);
                    text-decoration: none;
                    transition: all 0.15s ease;
                    cursor: pointer;
                    white-space: nowrap;
                    background: transparent;
                    border: none;
                    width: 100%;
                }

                .sidebar.collapsed .nav-item {
                    justify-content: center;
                }

                .nav-item:hover {
                    background: var(--sidebar-hover-bg);
                    color: var(--sidebar-text-primary);
                }

                .nav-item.active {
                    background: var(--sidebar-active-bg);
                    color: var(--sidebar-active-text);
                }

                .nav-item-content {
                    display: flex;
                    align-items: center;
                    gap: 10px;
                }

                .parent-item {
                    justify-content: space-between;
                    font-size: 11px;
                    font-weight: 600;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                    color: var(--sidebar-text-muted);
                }

                .chevron-icon {
                    transition: transform 0.2s ease;
                    flex-shrink: 0;
                }

                .chevron-icon.expanded {
                    transform: rotate(0deg);
                }

                .chevron-icon:not(.expanded) {
                    transform: rotate(-90deg);
                }

                .submenu {
                    margin-left: 12px;
                    margin-top: 4px;
                    padding-left: 12px;
                    border-left: 2px solid var(--border-color);
                    display: flex;
                    flex-direction: column;
                    gap: 2px;
                    animation: slideDown 0.2s ease;
                }

                .sub-item {
                    color: var(--sidebar-text-secondary);
                    font-weight: 400;
                    border-left: 2px solid transparent;
                    border-radius: 0 6px 6px 0;
                    padding: 8px 12px !important;
                }

                .sub-item:hover {
                    border-left-color: var(--sidebar-active-text);
                    background: var(--sidebar-hover-bg);
                }

                .sub-item.active {
                    background: var(--sidebar-active-bg);
                    color: var(--sidebar-active-text);
                }

                .sub-item-dot {
                    width: 6px;
                    height: 6px;
                    border-radius: 50%;
                    background: var(--sidebar-text-muted);
                    flex-shrink: 0;
                }

                .sub-item.active .sub-item-dot {
                    background: var(--sidebar-active-text);
                }

                .sub-item-text {
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                }

                /* Custom scrollbar */
                .sidebar::-webkit-scrollbar {
                    width: 4px;
                }

                .sidebar::-webkit-scrollbar-track {
                    background: transparent;
                }

                .sidebar::-webkit-scrollbar-thumb {
                    background: rgba(255,255,255,0.1);
                    border-radius: 2px;
                    opacity: 0;
                    transition: opacity 0.2s ease;
                }

                .sidebar:hover::-webkit-scrollbar-thumb {
                    opacity: 1;
                }

                .sidebar::-webkit-scrollbar-thumb:hover {
                    background: rgba(255,255,255,0.2);
                }

                .sidebar {
                    scrollbar-width: thin;
                    scrollbar-color: rgba(255,255,255,0.1) transparent;
                }

                @keyframes slideDown {
                    from {
                        opacity: 0;
                        transform: translateY(-8px);
                    }
                    to {
                        opacity: 1;
                        transform: translateY(0);
                    }
                }
            `}</style>
        </aside>
    );
};

export default Sidebar;
