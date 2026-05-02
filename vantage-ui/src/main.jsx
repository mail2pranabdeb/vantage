import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import App from './App.jsx'
import Login from './pages/Login.jsx'
import OAuth2Callback from './pages/OAuth2Callback.jsx'
import VantageJobList from './pages/VantageJobList.jsx'
import MonitoringDashboard from './pages/MonitoringDashboard.jsx'
import { AuthProvider, useAuth } from './context/AuthContext.jsx'
import { setupAuthInterceptor } from './services/api.js'
import './index.css'
import './themes/sap-gui.css'

console.log('[main] URL:', window.location.href);
console.log('[main] Search:', window.location.search);
console.log('[main] Pathname:', window.location.pathname);

// Unregister any stale service workers from previous PWA setup
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.getRegistrations().then(registrations => {
    registrations.forEach(reg => {
      console.log('[main] Unregistering service worker:', reg.scope);
      reg.unregister();
    });
  });
}

setupAuthInterceptor()

function ProtectedRoute({ children }) {
    const { user, loading } = useAuth();
    if (loading) {
        return <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100vh', background: 'var(--bg-primary)', color: 'var(--text-muted)' }}>Loading...</div>;
    }
    if (!user) {
        return <Navigate to="/login" replace />;
    }
    return children;
}

ReactDOM.createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/auth/google/callback" element={<OAuth2Callback />} />
        <Route path="/*" element={<App />} />
        <Route path="/vantage/job" element={<ProtectedRoute><VantageJobList /></ProtectedRoute>} />
        <Route path="/monitoring" element={<ProtectedRoute><MonitoringDashboard /></ProtectedRoute>} />
      </Routes>
    </AuthProvider>
  </BrowserRouter>,
)
