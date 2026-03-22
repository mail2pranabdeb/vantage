import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import App from './App.jsx'
import Login from './pages/Login.jsx'
import VantageJobList from './pages/VantageJobList.jsx'
import './index.css'
import './themes/sap-gui.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/*" element={<App />} />
        <Route path="/vantage/job" element={<VantageJobList />} />
      </Routes>
    </BrowserRouter>
  </React.StrictMode>,
)
