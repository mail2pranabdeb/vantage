import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import App from './App.jsx'
import Login from './pages/Login.jsx'
import Dashboard from './pages/Dashboard.jsx'
import UserList from './pages/UserList.jsx'
import RoleList from './pages/RoleList.jsx'
import MenuList from './pages/MenuList.jsx'
import ConfigList from './pages/ConfigList.jsx'
import DictList from './pages/DictList.jsx'
import PostList from './pages/PostList.jsx'
import LogininforList from './pages/LogininforList.jsx'
import OperlogList from './pages/OperlogList.jsx'
import NoticeList from './pages/NoticeList.jsx'
import JobList from './pages/JobList.jsx'
import JobLogList from './pages/JobLogList.jsx'
import GenList from './pages/GenList.jsx'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<App />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          {/* System Module */}
          <Route path="system/user" element={<UserList />} />
          <Route path="system/role" element={<RoleList />} />
          <Route path="system/menu" element={<MenuList />} />
          <Route path="system/config" element={<ConfigList />} />
          <Route path="system/dict" element={<DictList />} />
          <Route path="system/post" element={<PostList />} />
          <Route path="system/logininfor" element={<LogininforList />} />
          <Route path="system/operlog" element={<OperlogList />} />
          <Route path="system/notice" element={<NoticeList />} />
          {/* Job Module */}
          <Route path="system/job" element={<JobList />} />
          <Route path="system/jobLog" element={<JobLogList />} />
          {/* Generator Module */}
          <Route path="tool/gen" element={<GenList />} />
        </Route>
      </Routes>
    </BrowserRouter>
  </React.StrictMode>,
)
