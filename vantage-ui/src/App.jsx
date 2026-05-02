import { useState, useEffect } from 'react';
import { Navigate, useNavigate, useLocation } from 'react-router-dom';
import Sidebar from './components/Sidebar';
import Topbar from './components/Topbar';
import FloatingChat from './components/FloatingChat';
import CommandPalette from './components/CommandPalette';
import { ToastProvider } from './components/Toast';
import TabBar from './components/TabBar';
import TabContent from './components/TabContent';
import { menuCache } from './services/menuCache';
import { useAuth } from './context/AuthContext';

function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, loading } = useAuth();
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [tabs, setTabs] = useState([]);
  const [activeTabId, setActiveTabId] = useState(null);

  useEffect(() => {
    if (user) {
      if (tabs.length === 0) {
        const dashboardConfig = {
          id: 'dashboard',
          title: 'Dashboard',
          url: '/dashboard',
          icon: '📊',
          closable: false
        };
        addTab(dashboardConfig);
        setActiveTabId('dashboard');
        return;
      }

      if (location.pathname !== '/' && location.pathname !== '/dashboard') {
        menuCache.getPageConfig(location.pathname).then(pageConfig => {
          if (pageConfig && !tabs.find(t => t.id === pageConfig.id)) {
            addTab(pageConfig);
          }
        });
      }
    }
  }, [location, user]);

  useEffect(() => {
    const handleChildNavigate = (event) => {
      const { pageConfig } = event.detail || {};
      if (pageConfig && user) {
        setTabs(currentTabs => {
          const existingTab = currentTabs.find(t => t.id === pageConfig.id);
          if (existingTab) {
            setActiveTabId(existingTab.id);
          } else {
            const newTabs = [...currentTabs, { ...pageConfig, timestamp: Date.now() }];
            setActiveTabId(pageConfig.id);
            return newTabs;
          }
          return currentTabs;
        });
      }
    };
    window.addEventListener('navigate-to-page', handleChildNavigate);
    return () => window.removeEventListener('navigate-to-page', handleChildNavigate);
  }, [user]);

  const toggleSidebar = () => {
    setIsCollapsed(!isCollapsed);
  };

  const addTab = (pageConfig) => {
    setTabs(prev => [...prev, {
      ...pageConfig,
      timestamp: Date.now()
    }]);
  };

  const handleNavigate = (page) => {
    setTabs(currentTabs => {
      const existingTab = currentTabs.find(t => t.id === page.id);
      if (existingTab) {
        setActiveTabId(existingTab.id);
        return currentTabs;
      } else {
        const newTabs = [...currentTabs, { ...page, timestamp: Date.now() }];
        setActiveTabId(page.id);
        return newTabs;
      }
    });
  };

  const closeTab = (tabId) => {
    if (tabId === 'dashboard') {
      return;
    }
    
    setTabs(prev => {
      const newTabs = prev.filter(t => t.id !== tabId);
      if (activeTabId === tabId && newTabs.length > 0) {
        const newActive = newTabs[newTabs.length - 1];
        setActiveTabId(newActive.id);
      } else if (newTabs.length === 0) {
        setActiveTabId(null);
      }
      return newTabs;
    });
  };

  const refreshTab = (tabId) => {
    const tab = tabs.find(t => t.id === tabId);
    if (tab) {
      setTabs(prev => prev.map(t => 
        t.id === tabId ? { ...t, timestamp: Date.now() } : t
      ));
    }
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100vh', background: 'var(--bg-primary)', color: 'var(--text-muted)' }}>
        <div className="pulse-dot" style={{ color: 'var(--primary-color)' }}></div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return (
    <ToastProvider>
      <CommandPalette onNavigate={handleNavigate} />
      <div className="app-container" style={{ display: 'flex', width: '100vw', height: '100vh', background: 'var(--bg-primary)', overflow: 'hidden' }}>
        <Sidebar 
          isCollapsed={isCollapsed} 
          toggleSidebar={toggleSidebar} 
          onNavigate={handleNavigate}
          activeTabUrl={tabs.find(t => t.id === activeTabId)?.url || null}
        />
        <div className="main-content" style={{ flex: 1, display: 'flex', flexDirection: 'column', height: '100vh', overflow: 'hidden' }}>
          <Topbar />
          {tabs.length > 0 && (
            <>
              <TabBar
                tabs={tabs}
                activeTab={activeTabId}
                onTabClick={(id) => {
                  setActiveTabId(id);
                }}
                onTabClose={closeTab}
                onRefresh={refreshTab}
              />
              <div style={{ flex: 1, overflow: 'hidden', background: 'var(--bg-primary)', padding: '0 8px 8px 8px' }}>
                <div style={{ 
                    display: 'flex',
                    flexDirection: 'column',
                    height: '100%', 
                    overflow: 'hidden', 
                    borderRadius: '12px',
                    background: 'var(--bg-secondary)',
                    border: '1px solid var(--border-color)',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.03)'
                }}>
                  {tabs.map(tab => (
                    <TabContent
                      key={tab.id}
                      tab={tab}
                      isActive={activeTabId === tab.id}
                    />
                  ))}
                </div>
              </div>
            </>
          )}
        </div>
        <FloatingChat />
      </div>
    </ToastProvider>
  );
}

export default App;
