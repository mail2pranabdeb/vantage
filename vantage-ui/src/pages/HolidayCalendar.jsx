import { useState, useEffect } from 'react';
import { Calendar, Plus, Trash2, Edit, Filter, X } from 'lucide-react';
import Modal from '../components/Modal';

const HolidayCalendar = () => {
    const [holidays, setHolidays] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [currentHoliday, setCurrentHoliday] = useState(null);
    const [formData, setFormData] = useState({
        holidayName: '',
        holidayDate: '',
        holidayType: '1',
        recurring: false,
        description: '',
        status: '0'
    });

    useEffect(() => {
        fetchHolidays();
    }, []);

    const fetchHolidays = () => {
        setLoading(true);
        fetch('/api/system/holiday/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setHolidays(data.data || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch holidays:", err);
                setLoading(false);
            });
    };

    const handleAddClick = () => {
        setCurrentHoliday(null);
        setFormData({
            holidayName: '',
            holidayDate: '',
            holidayType: '1',
            recurring: false,
            description: '',
            status: '0'
        });
        setIsModalOpen(true);
    };

    const handleEditClick = (holiday) => {
        setCurrentHoliday(holiday);
        setFormData({
            holidayName: holiday.holidayName || '',
            holidayDate: holiday.holidayDate || '',
            holidayType: holiday.holidayType || '1',
            recurring: holiday.recurring || false,
            description: holiday.description || '',
            status: holiday.status || '0'
        });
        setIsModalOpen(true);
    };

    const handleDeleteClick = (holiday) => {
        if (window.confirm(`Delete holiday "${holiday.holidayName}"?`)) {
            fetch(`/api/system/holiday/${holiday.holidayId}`, { method: 'DELETE' })
                .then(res => res.json())
                .then(data => {
                    if (data.code === 200) {
                        fetchHolidays();
                    }
                });
        }
    };

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSubmit = () => {
        const url = currentHoliday ? '/api/system/holiday' : '/api/system/holiday';
        const method = currentHoliday ? 'PUT' : 'POST';
        const body = { ...formData, holidayId: currentHoliday?.holidayId };

        fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setIsModalOpen(false);
                    fetchHolidays();
                }
            });
    };

    const getHolidayTypeLabel = (type) => {
        const types = { '1': 'National', '2': 'Company', '3': 'Optional' };
        return types[type] || 'Unknown';
    };

    const getHolidayTypeColor = (type) => {
        const colors = { '1': '#ef4444', '2': '#3b82f6', '3': '#10b981' };
        return colors[type] || '#666';
    };

    // Group holidays by year/month
    const groupedHolidays = holidays.reduce((acc, holiday) => {
        const date = new Date(holiday.holidayDate);
        const year = date.getFullYear();
        const month = date.toLocaleString('en-US', { month: 'long' });
        if (!acc[year]) acc[year] = {};
        if (!acc[year][month]) acc[year][month] = [];
        acc[year][month].push(holiday);
        return acc;
    }, {});

    return (
        <div style={{
            height: 'calc(100vh - 70px)',
            overflow: 'auto',
            padding: '8px'
        }}>
            <div className="page-header" style={{ marginBottom: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{
                        width: '40px', height: '40px', borderRadius: '10px',
                        background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
                        display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white'
                    }}>
                        <Calendar size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Holiday Calendar</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Manage holidays for job scheduling
                        </p>
                    </div>
                </div>
                <button className="btn btn-primary" onClick={handleAddClick} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Plus size={18} /> Add Holiday
                </button>
            </div>

            {/* Calendar View */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px' }}>
                {Object.entries(groupedHolidays).map(([year, months]) => (
                    <div key={year} style={{ background: 'var(--bg-secondary)', borderRadius: '12px', padding: '16px' }}>
                        <h3 style={{ margin: '0 0 16px', fontSize: '16px', fontWeight: 600, color: 'var(--primary)' }}>{year}</h3>
                        {Object.entries(months).map(([month, holidays]) => (
                            <div key={month} style={{ marginBottom: '16px' }}>
                                <h4 style={{ margin: '0 0 8px', fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>{month}</h4>
                                {holidays.map(holiday => (
                                    <div key={holiday.holidayId} style={{
                                        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                                        padding: '8px 12px', background: 'var(--bg-tertiary)', borderRadius: '6px', marginBottom: '6px'
                                    }}>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                            <div style={{
                                                width: '8px', height: '8px', borderRadius: '50%',
                                                background: getHolidayTypeColor(holiday.holidayType)
                                            }} />
                                            <div>
                                                <div style={{ fontSize: '13px', fontWeight: 500 }}>{holiday.holidayName}</div>
                                                <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                                                    {new Date(holiday.holidayDate).toLocaleDateString('en-US', { day: 'numeric' })} • {getHolidayTypeLabel(holiday.holidayType)}
                                                </div>
                                            </div>
                                        </div>
                                        <div style={{ display: 'flex', gap: '4px' }}>
                                            <button onClick={() => handleEditClick(holiday)} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '11px' }}>
                                                <Edit size={12} />
                                            </button>
                                            <button onClick={() => handleDeleteClick(holiday)} className="btn btn-secondary" style={{ padding: '4px 8px', fontSize: '11px', color: 'var(--danger)' }}>
                                                <Trash2 size={12} />
                                            </button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        ))}
                    </div>
                ))}
            </div>

            {/* Add/Edit Modal */}
            <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={currentHoliday ? 'Edit Holiday' : 'Add Holiday'} size="small" footer={
                <>
                    <button className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>Cancel</button>
                    <button className="btn btn-primary" onClick={handleSubmit}>Save</button>
                </>
            }>
                <div className="form-group">
                    <label className="form-label">Holiday Name</label>
                    <input name="holidayName" value={formData.holidayName} onChange={handleInputChange} className="form-input" placeholder="e.g., Christmas Day" />
                </div>
                <div className="form-group">
                    <label className="form-label">Date</label>
                    <input name="holidayDate" type="date" value={formData.holidayDate} onChange={handleInputChange} className="form-input" />
                </div>
                <div className="form-row">
                    <div className="form-group">
                        <label className="form-label">Type</label>
                        <select name="holidayType" value={formData.holidayType} onChange={handleInputChange} className="form-input">
                            <option value="1">National</option>
                            <option value="2">Company</option>
                            <option value="3">Optional</option>
                        </select>
                    </div>
                    <div className="form-group">
                        <label className="form-label">Status</label>
                        <select name="status" value={formData.status} onChange={handleInputChange} className="form-input">
                            <option value="0">Active</option>
                            <option value="1">Inactive</option>
                        </select>
                    </div>
                </div>
                <div className="form-group">
                    <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                        <input type="checkbox" name="recurring" checked={formData.recurring} onChange={handleInputChange} />
                        <span>Recurring (every year)</span>
                    </label>
                </div>
                <div className="form-group">
                    <label className="form-label">Description</label>
                    <textarea name="description" value={formData.description} onChange={handleInputChange} className="form-input" rows={3} />
                </div>
            </Modal>
        </div>
    );
};

export default HolidayCalendar;
