import { useState, useEffect } from 'react';
import { Database, RefreshCw, ArrowLeft } from 'lucide-react';
import DataGrid from '../components/DataGrid';

const DictDataView = () => {
    const [dictData, setDictData] = useState([]);
    const [dictType, setDictType] = useState('');
    const [dictName, setDictName] = useState('');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Get dictType from URL query params
        const params = new URLSearchParams(window.location.search);
        const type = params.get('dictType');
        if (type) {
            setDictType(type);
            fetchDictData(type);
        }
    }, []);

    const fetchDictData = (type) => {
        setLoading(true);
        fetch(`/api/system/dict/data/list?dictType=${type}`)
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setDictData(data.data || []);
                    if (data.data && data.data.length > 0) {
                        setDictName(data.data[0].dictLabel || type);
                    }
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch dict data:", err);
                setLoading(false);
            });
    };

    const handleGoBack = () => {
        window.close(); // Try to close the tab
        // If close doesn't work, redirect to dict list
        setTimeout(() => {
            window.location.href = '/#/system/dict';
        }, 100);
    };

    const columns = [
        { key: 'dictLabel', header: 'Data Label', sortable: true },
        { 
            key: 'dictValue', 
            header: 'Data Value',
            render: (value) => (
                <span style={{ 
                    padding: '4px 8px', 
                    borderRadius: '4px', 
                    fontSize: '11px',
                    background: 'var(--primary-soft)',
                    color: 'var(--primary-color)',
                    fontFamily: 'monospace'
                }}>
                    {value}
                </span>
            )
        },
        { 
            key: 'dictSort', 
            header: 'Sort Order',
            align: 'center'
        },
        {
            key: 'status',
            header: 'Status',
            render: (value) => (
                <span className={`status-pill ${value === '0' ? 'active' : 'inactive'}`}>
                    {value === '0' ? 'Normal' : 'Disabled'}
                </span>
            )
        },
        { key: 'remark', header: 'Remark', sortable: false }
    ];

    const toolbarActions = [
        {
            label: 'Go Back',
            icon: ArrowLeft,
            onClick: handleGoBack
        },
        {
            label: 'Refresh',
            icon: RefreshCw,
            onClick: () => fetchDictData(dictType)
        }
    ];

    return (
        <div className="page-container">
            <div className="page-header">
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{
                        width: '40px',
                        height: '40px',
                        borderRadius: '10px',
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <Database size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '13px', fontWeight: 700, margin: 0 }}>
                            Dictionary Data: {dictType}
                        </h2>
                        <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Viewing dictionary data in new tab
                        </p>
                    </div>
                </div>
            </div>

            <DataGrid
                data={dictData}
                columns={columns}
                toolbarActions={toolbarActions}
                loading={loading}
                searchable={true}
                sortable={true}
                pagination={true}
                pageSize={20}
                emptyMessage="No dictionary data found for this type."
            />
        </div>
    );
};

export default DictDataView;
