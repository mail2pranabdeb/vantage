import { useState, useEffect } from 'react';
import { Database, Plus, Edit, Trash2, Eye, RefreshCw } from 'lucide-react';
import DataGrid from '../components/DataGrid';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';
import { useToast } from '../components/Toast';

const DictList = () => {
    const { addToast } = useToast();
    const [dicts, setDicts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('add');
    const [currentDict, setCurrentDict] = useState(null);
    const [formData, setFormData] = useState({
        dictName: '',
        dictType: '',
        status: '0',
        remark: ''
    });
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        fetchDicts();
    }, []);

    const fetchDicts = () => {
        setLoading(true);
        fetch('/api/system/dict/type/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setDicts(data.data || []);
                    if (data.data && data.data.length > 0) {
                        addToast('success', `Loaded ${data.data.length} dict ionary(ies)`, 2000);
                    }
                } else {
                    addToast('error', data.msg || 'Failed to load dictionaries', 4000);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch dicts:", err);
                setLoading(false);
                addToast('error', 'Failed to load dictionaries. Please refresh.', 5000);
            });
    };

    const handleAddClick = () => {
        setModalMode('add');
        setCurrentDict(null);
        setFormData({
            dictName: '',
            dictType: '',
            status: '0',
            remark: ''
        });
        setIsModalOpen(true);
    };

    const handleEditClick = (row) => {
        setModalMode('edit');
        setCurrentDict(row);
        setFormData({
            dictName: row.dictName || '',
            dictType: row.dictType || '',
            status: row.status || '0',
            remark: row.remark || ''
        });
        setIsModalOpen(true);
    };

    const handleViewClick = (row) => {
        setModalMode('view');
        setCurrentDict(row);
        setFormData({
            dictName: row.dictName || '',
            dictType: row.dictType || '',
            status: row.status || '0',
            remark: row.remark || ''
        });
        setIsModalOpen(true);
    };

    const handleViewDataClick = (row) => {
        // Navigate to dictionary data view within app
        const url = `#/system/dict/data?dictType=${row.dictType}`;
        window.location.href = url;
    };

    const handleDeleteClick = (row) => {
        if (window.confirm(`Are you sure you want to delete dictionary "${row.dictName}"?`)) {
            fetch(`/api/system/dict/type/${row.dictId}`, {
                method: 'DELETE'
            })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setDicts(dicts.filter(d => d.dictId !== row.dictId));
                    addToast('success', `Dictionary "${row.dictName}" deleted successfully`, 3000);
                } else {
                    addToast('error', data.msg || 'Failed to delete dictionary', 5000);
                }
            })
            .catch(err => {
                console.error("Failed to delete dictionary:", err);
                addToast('error', 'Failed to delete dictionary', 5000);
            });
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = () => {
        setSubmitting(true);
        
        const url = modalMode === 'add' 
            ? '/api/system/dict/type' 
            : '/api/system/dict/type';
        
        const method = modalMode === 'add' ? 'POST' : 'PUT';
        const body = modalMode === 'add' 
            ? { ...formData } 
            : { ...formData, dictId: currentDict.dictId };

        fetch(url, {
            method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(body)
        })
        .then(res => res.json())
        .then(data => {
            setSubmitting(false);
            if (data.code === 200) {
                setIsModalOpen(false);
                addToast('success', `Dictionary "${formData.dictName}" ${modalMode === 'add' ? 'created' : 'updated'} successfully`, 3000);
                fetchDicts();
            } else {
                addToast('error', data.msg || `Failed to ${modalMode} dictionary`, 5000);
            }
        })
        .catch(err => {
            setSubmitting(false);
            console.error(`Failed to ${modalMode} dictionary:`, err);
            addToast('error', `Failed to ${modalMode} dictionary`, 5000);
        });
    };

    const columns = [
        { key: 'dictId', header: 'ID', sortable: true, align: 'center' },
        {
            key: 'dictName',
            header: 'Dict Name',
            sortable: true,
            render: (value, row) => (
                <div>
                    <div style={{ fontWeight: 600 }}>{value}</div>
                    <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{row.dictType}</div>
                </div>
            )
        },
        {
            key: 'dictType',
            header: 'Dict Type',
            sortable: true,
            render: (value) => (
                <span className="badge-outline" style={{ fontSize: '11px', fontFamily: 'monospace' }}>{value}</span>
            )
        },
        {
            key: 'status',
            header: 'Status',
            sortable: true,
            render: (value) => (
                <span className={`status-pill ${value === '0' ? 'active' : 'inactive'}`}>
                    {value === '0' ? 'Normal' : 'Disabled'}
                </span>
            )
        },
        {
            key: 'remark',
            header: 'Remark',
            sortable: false,
            render: (value) => <span style={{ color: 'var(--text-muted)' }}>{value || '-'}</span>
        },
        {
            key: 'createTime',
            header: 'Create Time',
            sortable: true,
            render: (value) => <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>{value ? new Date(value).toLocaleDateString() : '-'}</span>
        }
    ];

    const actions = [
        { label: 'View Data', icon: Database, onClick: handleViewDataClick },
        { label: 'View', icon: Eye, onClick: handleViewClick },
        { label: 'Edit', icon: Edit, onClick: handleEditClick },
        { label: 'Delete', icon: Trash2, danger: true, onClick: handleDeleteClick }
    ];

    const toolbarActions = [
        {
            label: 'Refresh',
            icon: RefreshCw,
            onClick: fetchDicts
        }
    ];

    return (
        <div className="page-container">
            <div className="page-header">
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <div style={{
                        width: '32px',
                        height: '32px',
                        borderRadius: '8px',
                        background: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: '#333'
                    }}>
                        <Database size={16} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '16px', fontWeight: 700, margin: 0 }}>Dict Management</h2>
                        <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '2px 0 0' }}>
                            Manage dictionaries
                        </p>
                    </div>
                </div>
                <button
                    className="btn btn-primary"
                    onClick={handleAddClick}
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '6px',
                        padding: '8px 14px',
                        borderRadius: '6px',
                        fontWeight: 600,
                        fontSize: '13px'
                    }}
                >
                    <Plus size={16} />
                    Add Dict
                </button>
            </div>

            <DataGrid
                data={dicts}
                columns={columns}
                actions={actions}
                toolbarActions={toolbarActions}
                loading={loading}
                searchable={true}
                sortable={true}
                filterable={true}
                selectable={true}
                pagination={true}
                pageSize={10}
                emptyMessage="No dictionaries found."
            />

            {/* Add/Edit Modal */}
            <Modal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                title={modalMode === 'add' ? 'Add Dictionary' : modalMode === 'edit' ? 'Edit Dictionary' : 'View Dictionary'}
                size="small"
                compact={true}
                footer={modalMode !== 'view' && (
                    <>
                        <button
                            className="btn btn-secondary"
                            onClick={() => setIsModalOpen(false)}
                            disabled={submitting}
                        >
                            Cancel
                        </button>
                        <button
                            className="btn btn-primary"
                            onClick={handleSubmit}
                            disabled={submitting}
                            style={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: '6px'
                            }}
                        >
                            {submitting && (
                                <div style={{
                                    width: '12px',
                                    height: '12px',
                                    border: '2px solid white',
                                    borderBottomColor: 'transparent',
                                    borderRadius: '50%',
                                    animation: 'spin 1s linear infinite'
                                }} />
                            )}
                            {modalMode === 'add' ? 'Create' : 'Save'}
                        </button>
                    </>
                )}
            >
                <div style={{ display: 'flex', flexDirection: 'column' }}>
                    <div className="form-row">
                        <FormInput
                            label="Dictionary Name"
                            name="dictName"
                            value={formData.dictName}
                            onChange={handleInputChange}
                            placeholder="Enter dictionary name"
                            required
                            disabled={modalMode === 'view'}
                        />
                        <FormInput
                            label="Dictionary Type"
                            name="dictType"
                            value={formData.dictType}
                            onChange={handleInputChange}
                            placeholder="e.g., sys_user_sex"
                            required
                            disabled={modalMode === 'view' || modalMode === 'edit'}
                        />
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label className="form-label">Status</label>
                            <select
                                name="status"
                                value={formData.status}
                                onChange={handleInputChange}
                                className="form-input"
                                disabled={modalMode === 'view'}
                            >
                                <option value="0">Normal</option>
                                <option value="1">Disabled</option>
                            </select>
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="form-label">Remark</label>
                        <textarea
                            name="remark"
                            value={formData.remark}
                            onChange={handleInputChange}
                            placeholder="Enter any remarks"
                            className="form-input"
                            rows={3}
                            disabled={modalMode === 'view'}
                        />
                    </div>
                </div>
            </Modal>
        </div>
    );
};

export default DictList;
