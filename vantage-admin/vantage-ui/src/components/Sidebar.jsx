import { useState, useEffect } from 'react';
import { NavLink } from 'react-router-dom';
import { menuCache } from '../services/menuCache';
import {
    Home, Users, Shield, Menu as MenuIcon, Settings, ChevronLeft, ChevronDown,
    LayoutDashboard, Zap, Target, Database, Activity, LogOut
} from 'lucide-react';

const iconMap = {
    'fa fa-gear': Settings,
    'fa fa-user-o': Users,
    'fa fa-users': Users,
    'fa fa-lock': Shield,
    'fa fa-list': MenuIcon,
    'fa fa-dashboard': LayoutDashboard,
    'fa fa-bell': Activity,
    'fa fa-th-list': MenuIcon,
    'fa fa-sun-o': Zap,
    'fa fa-bookmark-o': Target,
    'fa fa-address-card-o': Users,
    'fa fa-file-image-o': Database,
    'fa fa-bullhorn': Activity,
    'fa fa-tasks': Target,
    'fa fa-clock-o': Home,
    'fa fa-file-text-o': Database,
    'fa fa-code': Shield,
    '#': MenuIcon
};

const Sidebar = ({ isCollapsed, toggleSidebar, onNavigate, activeTabUrl }) => {
    const [menus, setMenus] = useState([]);
    const [loading, setLoading] = useState(true);
    const [expandedMenus, setExpandedMenus] = useState({});

    useEffect(() => {
        const fetchMenus = async () => {
            try {
                const response = await fetch('/api/system/menu/tree');
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    const data = await response.json();
                    if (data.code === 200 && data.data) {
                        setMenus(data.data);
                        menuCache.refresh();
                    }
                }
            } catch (err) {
                console.error("Failed to fetch menus:", err);
            } finally {
                setLoading(false);
            }
        };
        fetchMenus();
    }, []);

    const renderIcon = (iconStr, isActive = false) => {
        const Icon = iconMap[iconStr] || MenuIcon;
        return <Icon size={18} style={{ 
            minWidth: '18px', 
            color: isActive ? '#60a5fa' : 'rgba(255, 255, 255, 0.5)',
            transition: 'all 0.3s ease'
        }} />;
    };

    const toggleExpand = (menuId) => {
        setExpandedMenus(prev => ({ ...prev, [menuId]: !prev[menuId] }));
    };

    return (
        <aside className={`sidebar ${isCollapsed ? 'collapsed' : ''}`}>
            {/* Header / Logo */}
            <div className="sidebar-header">
                {!isCollapsed && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                        <div style={{ 
                            width: '38px', height: '38px', borderRadius: '12px', 
                            background: 'linear-gradient(135deg, var(--primary-color), var(--primary-hover))',
                            display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white',
                            boxShadow: '0 8px 25px rgba(59, 130, 246, 0.5)',
                            border: '1px solid rgba(255, 255, 255, 0.2)'
                        }}>
                            <Zap size={22} fill="currentColor" />
                        </div>
                        <div style={{ display: 'flex', flexDirection: 'column' }}>
                            <span className="sidebar-logo-text">VANTAGE</span>
                            <span style={{ fontSize: '9px', color: 'rgba(255, 255, 255, 0.4)', letterSpacing: '2px', fontWeight: 800 }}>PRO PLATFORM</span>
                        </div>
                    </div>
                )}
                <button onClick={toggleSidebar} className="sidebar-toggle">
                    <ChevronLeft size={16} />
                </button>
            </div>

            {/* Navigation */}
            <nav className="sidebar-nav">
                <div className="nav-group-label">{!isCollapsed && 'CORE NAVIGATOR'}</div>
                <NavLink
                    to="/dashboard"
                    className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
                    title={isCollapsed ? 'Dashboard' : undefined}
                >
                    <div className="nav-item-content">
                        <Home size={18} />
                        {!isCollapsed && <span>Dashboard</span>}
                    </div>
                    <div className="active-glow"></div>
                </NavLink>

                {!loading && menus.map((menu) => {
                    const hasChildren = menu.children && menu.children.length > 0;
                    const isExpanded = expandedMenus[menu.menuId];
                    const isParentActive = menu.children?.some(c => activeTabUrl === c.url);

                    if (hasChildren) {
                        return (
                            <div key={menu.menuId} className={`menu-group ${isExpanded ? 'group-expanded' : ''}`}>
                                <button
                                    onClick={() => toggleExpand(menu.menuId)}
                                    className={`nav-item parent-item ${isParentActive ? 'parent-active' : ''} ${isExpanded ? 'is-expanded' : ''}`}
                                    title={isCollapsed ? menu.menuName : undefined}
                                >
                                    <div className="nav-item-content">
                                        {renderIcon(menu.icon, isParentActive)}
                                        {!isCollapsed && <span>{menu.menuName}</span>}
                                    </div>
                                    {!isCollapsed && (
                                        <ChevronDown size={14} className={`chevron-icon ${isExpanded ? 'expanded' : ''}`} />
                                    )}
                                </button>

                                {!isCollapsed && (
                                    <div className={`submenu-container ${isExpanded ? 'expanded' : ''}`}>
                                        <div className="submenu">
                                            {menu.children.map(child => {
                                                const isActive = activeTabUrl === child.url;
                                                return (
                                                    <div
                                                        key={child.menuId}
                                                        className={`sub-item ${isActive ? 'active' : ''}`}
                                                        onClick={() => onNavigate({
                                                            id: child.url.replace('/', ''),
                                                            title: child.menuName,
                                                            url: child.url,
                                                            icon: '⚡'
                                                        })}
                                                    >
                                                        <div className="sub-item-dot"></div>
                                                        <span>{child.menuName}</span>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                    </div>
                                )}
                            </div>
                        );
                    } else {
                        return (
                            <NavLink
                                key={menu.menuId}
                                to={menu.url}
                                className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
                                title={isCollapsed ? menu.menuName : undefined}
                            >
                                <div className="nav-item-content">
                                    {renderIcon(menu.icon)}
                                    {!isCollapsed && <span>{menu.menuName}</span>}
                                </div>
                                <div className="active-glow"></div>
                            </NavLink>
                        );
                    }
                })}
            </nav>

            {/* Footer / User Profile */}
            <div className="sidebar-footer">
                {!isCollapsed ? (
                    <div className="user-profile-glass">
                        <div className="user-avatar-wrapper">
                            <div className="avatar-main">
                                <Users size={18} />
                            </div>
                            <div className="online-status-ring"></div>
                        </div>
                        <div className="user-text-content">
                            <p className="user-display-name">Administrator</p>
                            <span className="user-access-tag">SYSTEM MASTER</span>
                        </div>
                        <button className="power-off-btn" title="Sign Out">
                            <LogOut size={16} />
                        </button>
                    </div>
                ) : (
                    <div className="user-avatar-collapsed">
                        <Users size={18} />
                        <div className="status-dot"></div>
                    </div>
                )}
            </div>

            <style>{`
                .sidebar {
                    width: 260px;
                    background: linear-gradient(180deg, rgba(13, 17, 23, 0.95) 0%, rgba(10, 12, 16, 0.98) 100%);
                    backdrop-filter: blur(32px);
                    -webkit-backdrop-filter: blur(32px);
                    border-right: 1px solid rgba(255, 255, 255, 0.08);
                    height: 100vh;
                    display: flex;
                    flex-direction: column;
                    transition: all 0.5s cubic-bezier(0.2, 1, 0.2, 1);
                    z-index: 1000;
                    box-shadow: 20px 0 80px rgba(0, 0, 0, 0.5);
                }

                .sidebar.collapsed { width: 88px; }

                .sidebar-header {
                    height: 64px;
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    padding: 0 20px;
                    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
                }

                .sidebar.collapsed .sidebar-header { justify-content: center; padding: 0; }

                .sidebar-logo-text {
                    font-size: 16px;
                    font-weight: 900;
                    color: white;
                    letter-spacing: 2px;
                    background: linear-gradient(to bottom, #fff, #94a3b8);
                    -webkit-background-clip: text;
                    -webkit-text-fill-color: transparent;
                }

                .sidebar-toggle {
                    width: 32px; height: 32px;
                    background: rgba(255, 255, 255, 0.03);
                    border: 1px solid rgba(255, 255, 255, 0.08);
                    border-radius: 10px;
                    color: rgba(255, 255, 255, 0.6);
                    display: flex;
                    align-items: center; justify-content: center;
                    cursor: pointer;
                    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
                }

                .sidebar-toggle:hover {
                    background: rgba(59, 130, 246, 0.2);
                    color: #60a5fa;
                    border-color: rgba(59, 130, 246, 0.4);
                    transform: scale(1.05);
                }

                .sidebar.collapsed .sidebar-toggle { transform: rotate(180deg); }

                .sidebar-nav {
                    flex: 1;
                    padding: 16px 12px;
                    overflow-y: auto;
                    display: flex;
                    flex-direction: column;
                    gap: 4px;
                }

                .nav-group-label {
                    font-size: 9px;
                    font-weight: 800;
                    color: rgba(255, 255, 255, 0.25);
                    padding: 12px 14px 8px;
                    letter-spacing: 2px;
                    text-transform: uppercase;
                }

                .nav-item {
                    position: relative;
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    padding: 10px 14px;
                    border-radius: 12px;
                    color: rgba(255, 255, 255, 0.5);
                    text-decoration: none;
                    font-size: 13px;
                    font-weight: 600;
                    transition: all 0.4s cubic-bezier(0.2, 1, 0.2, 1);
                    border: 1px solid transparent;
                    background: transparent;
                    width: 100%;
                    cursor: pointer;
                    overflow: hidden;
                }

                .sidebar.collapsed .nav-item { justify-content: center; padding: 14px; }

                .nav-item:hover {
                    background: rgba(255, 255, 255, 0.03);
                    color: white;
                    border-color: rgba(255, 255, 255, 0.05);
                }

                .nav-item.active {
                    background: rgba(59, 130, 246, 0.1);
                    color: #60a5fa;
                    border-color: rgba(59, 130, 246, 0.2);
                    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
                }

                .active-glow {
                    position: absolute;
                    left: 0;
                    top: 15%;
                    height: 70%;
                    width: 3px;
                    background: #60a5fa;
                    border-radius: 0 4px 4px 0;
                    transform: translateX(-3px);
                    transition: all 0.4s cubic-bezier(0.2, 1, 0.2, 1);
                    box-shadow: 0 0 15px #60a5fa;
                }

                .nav-item.active .active-glow { transform: translateX(0); }

                .nav-item-content {
                    display: flex;
                    align-items: center;
                    gap: 12px;
                    flex: 1;
                    min-width: 0;
                }

                .nav-item-content span {
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                }

                .chevron-icon {
                    transition: transform 0.4s cubic-bezier(0.2, 1, 0.2, 1);
                    opacity: 0.3;
                    flex-shrink: 0;
                }

                .chevron-icon.expanded { transform: rotate(180deg); opacity: 0.8; }

                .submenu-container {
                    max-height: 0;
                    overflow: hidden;
                    transition: all 0.5s cubic-bezier(0.2, 1, 0.2, 1);
                    opacity: 0;
                }

                .submenu-container.expanded {
                    max-height: 500px;
                    opacity: 1;
                    margin-top: 6px;
                    margin-bottom: 12px;
                }

                .submenu {
                    position: relative;
                    margin-left: 14px;
                    padding-left: 18px;
                    border-left: 1px dashed rgba(255, 255, 255, 0.1);
                    display: flex;
                    flex-direction: column;
                    gap: 4px;
                }

                .sub-item {
                    display: flex;
                    align-items: center;
                    gap: 10px;
                    padding: 7px 10px;
                    font-size: 12px;
                    color: rgba(255, 255, 255, 0.45);
                    border-radius: 10px;
                    cursor: pointer;
                    transition: all 0.3s ease;
                    border: 1px solid transparent;
                    min-width: 0;
                }

                .sub-item span {
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                }

                .sub-item:hover { 
                    color: white; 
                    background: rgba(255, 255, 255, 0.03);
                    padding-left: 16px;
                }

                .sub-item.active { 
                    color: #60a5fa; 
                    font-weight: 700; 
                    background: rgba(59, 130, 246, 0.08);
                    border-color: rgba(59, 130, 246, 0.15);
                }

                .sub-item-dot {
                    width: 5px; height: 5px;
                    border-radius: 50%;
                    background: rgba(255, 255, 255, 0.15);
                    transition: all 0.3s ease;
                }

                .sub-item.active .sub-item-dot {
                    background: #60a5fa;
                    box-shadow: 0 0 10px #60a5fa;
                }

                .sidebar-footer {
                    padding: 16px 14px;
                    border-top: 1px solid rgba(255, 255, 255, 0.05);
                }

                .user-profile-glass {
                    display: flex;
                    align-items: center;
                    gap: 10px;
                    background: rgba(255, 255, 255, 0.02);
                    padding: 10px;
                    border-radius: 14px;
                    border: 1px solid rgba(255, 255, 255, 0.05);
                    backdrop-filter: blur(10px);
                    transition: all 0.3s ease;
                }

                .user-profile-glass:hover {
                    background: rgba(255, 255, 255, 0.04);
                    border-color: rgba(255, 255, 255, 0.1);
                }

                .user-avatar-wrapper {
                    position: relative;
                }

                .avatar-main {
                    width: 36px; height: 36px;
                    background: linear-gradient(135deg, rgba(59, 130, 246, 0.2), rgba(139, 92, 246, 0.2));
                    border: 1px solid rgba(255, 255, 255, 0.1);
                    border-radius: 12px;
                    display: flex;
                    align-items: center; justify-content: center;
                    color: #60a5fa;
                }

                .online-status-ring {
                    position: absolute;
                    bottom: -2px; right: -2px;
                    width: 12px; height: 12px;
                    background: #10b981;
                    border: 3px solid #0d1117;
                    border-radius: 50%;
                }

                .user-text-content { flex: 1; min-width: 0; }
                .user-display-name { margin: 0; font-size: 13px; font-weight: 700; color: white; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
                .user-access-tag { 
                    font-size: 9px; 
                    color: #60a5fa; 
                    font-weight: 800;
                    letter-spacing: 1px;
                    background: rgba(59, 130, 246, 0.1);
                    padding: 1px 6px;
                    border-radius: 4px;
                }

                .power-off-btn {
                    background: transparent;
                    border: none;
                    color: rgba(255, 255, 255, 0.2);
                    cursor: pointer;
                    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                    padding: 6px;
                    border-radius: 8px;
                }

                .power-off-btn:hover { 
                    color: #f87171; 
                    background: rgba(248, 113, 113, 0.1);
                    transform: scale(1.1);
                }

                .user-avatar-collapsed {
                    width: 44px; height: 44px;
                    margin: 0 auto;
                    background: rgba(255, 255, 255, 0.03);
                    border-radius: 14px;
                    display: flex;
                    align-items: center; justify-content: center;
                    color: rgba(255, 255, 255, 0.5);
                    position: relative;
                    border: 1px solid rgba(255, 255, 255, 0.05);
                }

                .user-avatar-collapsed .status-dot {
                    position: absolute;
                    bottom: 8px; right: 8px;
                    width: 8px; height: 8px;
                    background: #10b981;
                    border-radius: 50%;
                    border: 2px solid #0d1117;
                }

                .sidebar-nav::-webkit-scrollbar { width: 4px; }
                .sidebar-nav::-webkit-scrollbar-track { background: transparent; }
                .sidebar-nav::-webkit-scrollbar-thumb { background: transparent; border-radius: 2px; }
                .sidebar:hover .sidebar-nav::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.1); }
            `}</style>
        </aside>
    );
};

export default Sidebar;
