import Dashboard from '../pages/Dashboard';
import UserList from '../pages/UserList';
import RoleList from '../pages/RoleList';
import MenuList from '../pages/MenuList';
import ConfigList from '../pages/ConfigList';
import DictList from '../pages/DictList';
import PostList from '../pages/PostList';
import LogininforList from '../pages/LogininforList';
import OperlogList from '../pages/OperlogList';
import NoticeList from '../pages/NoticeList';
import JobList from '../pages/JobList';
import JobLogList from '../pages/JobLogList';
import GenList from '../pages/GenList';

// Map URLs to components
const pageComponents = {
    '/dashboard': Dashboard,
    '/system/user': UserList,
    '/system/role': RoleList,
    '/system/menu': MenuList,
    '/system/config': ConfigList,
    '/system/dict': DictList,
    '/system/post': PostList,
    '/system/logininfor': LogininforList,
    '/system/operlog': OperlogList,
    '/system/notice': NoticeList,
    '/system/job': JobList,
    '/system/jobLog': JobLogList,
    '/tool/gen': GenList,
};

const TabContent = ({ tab, isActive }) => {
    const Component = pageComponents[tab.url];

    if (!Component) {
        console.log('Page not found for URL:', tab.url);
        return (
            <div style={{ padding: '20px', color: 'var(--text-muted)' }}>
                Page not found: {tab.title} (URL: {tab.url})
            </div>
        );
    }

    // Only render if tab is active (for performance)
    if (!isActive) {
        return null;
    }

    return (
        <div style={{
            flex: 1,
            overflow: 'auto',
            background: 'var(--bg-primary)',
            padding: '8px'
        }}>
            <Component />
        </div>
    );
};

export default TabContent;
