import { Outlet, Navigate } from 'react-router-dom'
import { useState, useEffect } from 'react'
import Sidebar from './components/Sidebar'
import Topbar from './components/Topbar'

function App() {
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [authState, setAuthState] = useState('loading'); // 'loading' | 'authenticated' | 'unauthenticated'

  useEffect(() => {
    fetch('/api/me')
      .then(res => {
        if (res.ok) {
          setAuthState('authenticated');
        } else {
          setAuthState('unauthenticated');
        }
      })
      .catch(() => setAuthState('unauthenticated'));
  }, []);

  const toggleSidebar = () => {
    setIsCollapsed(!isCollapsed);
  };

  if (authState === 'loading') {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100vh', background: 'var(--bg-primary)', color: 'var(--text-muted)' }}>
        Loading...
      </div>
    );
  }

  if (authState === 'unauthenticated') {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="app-container" style={{ display: 'flex', width: '100vw', minHeight: '100vh', background: 'var(--bg-primary)' }}>
      <Sidebar isCollapsed={isCollapsed} toggleSidebar={toggleSidebar} />
      <div className="main-content" style={{ flex: 1, display: 'flex', flexDirection: 'column', height: '100vh', overflowY: 'auto' }}>
        <Topbar />
        <div className="page-content glass-panel animate-fade-in" style={{ margin: '12px 16px 16px', flex: '1 1 auto', position: 'relative' }}>
          <Outlet />
        </div>
      </div>
    </div>
  )
}

export default App
