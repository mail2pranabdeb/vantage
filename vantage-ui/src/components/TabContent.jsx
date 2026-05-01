import Dashboard from '../pages/Dashboard';
import UserList from '../pages/UserList';
import RoleList from '../pages/RoleList';
import MenuList from '../pages/MenuList';
import ConfigList from '../pages/ConfigList';
import DictList from '../pages/DictList';
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
import DictDataView from '../pages/DictDataView';
import ReportManagement from '../pages/ReportManagement';
import ReportDesigner from '../pages/ReportDesigner';
import DatasourceList from '../pages/DatasourceList';
import ScriptJobList from '../pages/ScriptJobList';
import DashboardBuilder from '../pages/DashboardBuilder';
import MonitoringDashboard from '../pages/MonitoringDashboard';

// Map URLs to components
const pageComponents = {
    '/dashboard': Dashboard,
    '/system/user': UserList,
    '/system/role': RoleList,
    '/system/menu': MenuList,
    '/system/config': SystemSettings,
    '/system/datasource': DatasourceList,
    '/system/dict': DictList,
    '/system/dict/data': DictDataView,
    '/system/logininfor': LogininforList,
    '/system/operlog': OperlogList,
    '/system/dashboards': DashboardBuilder,
    '/system/notice': NoticeList,
    '/system/job': JobList,
    '/system/jobLog': JobLogList,
    '/system/report': ReportManagement,
    '/system/report-designer': ReportDesigner,
    '/system/script-job': ScriptJobList,
    '/tool/gen': GenList,
    '/system/job-calendar': JobCalendar,
    '/system/holiday-calendar': HolidayCalendar,
    '/system/job-logs': LiveJobLogs,
    '/system/email-templates': EmailTemplateManager,
    '/system/email-config': EmailConfig,
    '/system/cache': CacheManagement,
    '/monitoring': MonitoringDashboard,
};

const TabContent = ({ tab, isActive }) => {
    // Strip query parameters for component lookup
    const baseUrl = tab.url ? tab.url.split('?')[0] : '';
    const Component = pageComponents[baseUrl];

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

    // Pass tab as prop so component can access URL and params
    return (
        <div style={{
            flex: 1,
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden',
            background: 'var(--bg-primary)',
            padding: '8px'
        }}>
            <Component tab={tab} />
        </div>
    );
};

export default TabContent;
