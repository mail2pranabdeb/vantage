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
import JobCalendar from '../pages/JobCalendar';
import HolidayCalendar from '../pages/HolidayCalendar';
import LiveJobLogs from '../pages/LiveJobLogs';
import EmailTemplateManager from '../pages/EmailTemplateManager';
import EmailConfig from '../pages/EmailConfig';
import SystemSettings from '../pages/SystemSettings';
import CacheManagement from '../pages/CacheManagement';

// Map URLs to components
const pageComponents = {
    '/dashboard': Dashboard,
    '/system/user': UserList,
    '/system/role': RoleList,
    '/system/menu': MenuList,
    '/system/config': SystemSettings,
    '/system/dict': DictList,
    '/system/post': PostList,
    '/system/logininfor': LogininforList,
    '/system/operlog': OperlogList,
    '/system/notice': NoticeList,
    '/system/job': JobList,
    '/system/jobLog': JobLogList,
    '/tool/gen': GenList,
    '/system/job-calendar': JobCalendar,
    '/system/holiday-calendar': HolidayCalendar,
    '/system/job-logs': LiveJobLogs,
    '/system/email-templates': EmailTemplateManager,
    '/system/email-config': EmailConfig,
    '/system/cache': CacheManagement,
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
