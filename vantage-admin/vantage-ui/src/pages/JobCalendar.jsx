import { useState, useEffect } from 'react';
import { Calendar, Clock, Play, CheckCircle, XCircle } from 'lucide-react';

const JobCalendar = () => {
    const [jobs, setJobs] = useState([]);
    const [selectedDate, setSelectedDate] = useState(new Date());

    useEffect(() => {
        fetchJobs();
    }, []);

    const fetchJobs = () => {
        fetch('/api/system/job/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) setJobs(data.data || []);
            });
    };

    const getDaysInMonth = (date) => {
        const year = date.getFullYear();
        const month = date.getMonth();
        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const days = [];
        
        for (let i = 1; i <= lastDay.getDate(); i++) {
            days.push(new Date(year, month, i));
        }
        return { firstDay, days };
    };

    const { firstDay, days } = getDaysInMonth(selectedDate);

    const getJobsForDate = (date) => {
        return jobs.filter(job => {
            // Simple check - in real app, parse cron expression
            return job.status === '0'; // Show active jobs
        });
    };

    const monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'];

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
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white'
                    }}>
                        <Calendar size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Job Calendar</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Visual schedule of job executions
                        </p>
                    </div>
                </div>
            </div>

            {/* Month Navigation */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <button className="btn btn-secondary" onClick={() => setSelectedDate(new Date(selectedDate.setMonth(selectedDate.getMonth() - 1)))}>
                    Previous
                </button>
                <h3 style={{ fontSize: '18px', fontWeight: 600 }}>
                    {monthNames[selectedDate.getMonth()]} {selectedDate.getFullYear()}
                </h3>
                <button className="btn btn-secondary" onClick={() => setSelectedDate(new Date(selectedDate.setMonth(selectedDate.getMonth() + 1)))}>
                    Next
                </button>
            </div>

            {/* Calendar Grid */}
            <div style={{
                display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '8px',
                background: 'var(--bg-secondary)', padding: '16px', borderRadius: '12px'
            }}>
                {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(day => (
                    <div key={day} style={{ textAlign: 'center', fontWeight: 600, padding: '8px', color: 'var(--text-secondary)' }}>
                        {day}
                    </div>
                ))}
                
                {Array(firstDay.getDay()).fill(null).map((_, i) => (
                    <div key={`empty-${i}`} style={{ minHeight: '100px' }} />
                ))}

                {days.map((day, idx) => {
                    const dayJobs = getJobsForDate(day);
                    const isToday = day.toDateString() === new Date().toDateString();
                    
                    return (
                        <div key={idx} style={{
                            minHeight: '100px', padding: '8px', borderRadius: '8px',
                            background: isToday ? 'var(--primary)10' : 'var(--bg-tertiary)',
                            border: isToday ? '2px solid var(--primary)' : '1px solid var(--border-color)'
                        }}>
                            <div style={{ fontWeight: 600, marginBottom: '4px', fontSize: '14px' }}>
                                {day.getDate()}
                            </div>
                            {dayJobs.slice(0, 3).map((job, jobIdx) => (
                                <div key={jobIdx} style={{
                                    fontSize: '10px', padding: '2px 4px', borderRadius: '4px',
                                    background: job.status === '0' ? 'var(--success)20' : 'var(--text-muted)20',
                                    color: job.status === '0' ? 'var(--success)' : 'var(--text-muted)',
                                    marginBottom: '2px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap'
                                }}>
                                    {job.jobName}
                                </div>
                            ))}
                            {dayJobs.length > 3 && (
                                <div style={{ fontSize: '9px', color: 'var(--text-muted)', textAlign: 'center' }}>
                                    +{dayJobs.length - 3} more
                                </div>
                            )}
                        </div>
                    );
                })}
            </div>

            {/* Job Legend */}
            <div style={{ marginTop: '20px', display: 'flex', gap: '20px', justifyContent: 'center' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <div style={{ width: '12px', height: '12px', borderRadius: '3px', background: 'var(--success)20' }} />
                    <span style={{ fontSize: '12px' }}>Active Job</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <div style={{ width: '12px', height: '12px', borderRadius: '3px', border: '2px solid var(--primary)' }} />
                    <span style={{ fontSize: '12px' }}>Today</span>
                </div>
            </div>
        </div>
    );
};

export default JobCalendar;
