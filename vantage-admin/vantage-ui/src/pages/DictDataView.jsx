import { useState, useEffect } from 'react';
import { Database, RefreshCw, ArrowLeft, Plus, Edit, Trash2 } from 'lucide-react';
import DataGrid from '../components/DataGrid';
import Modal from '../components/Modal';
import FormInput from '../components/FormInput';
import { useToast } from '../components/Toast';

const DictDataView = () => {
    const toast = useToast();
    const [dictData, setDictData] = useState([]);
    const [dictType, setDictType] = useState('');
    const [dictName, setDictName] = useState('');
    const [loading, setLoading] = useState(true);

    // Form modal state
    const [isFormModalOpen, setIsFormModalOpen] = useState(false);
    const [formMode, setFormMode] = useState('add'); // 'add' or 'edit'
    const [currentData, setCurrentData] = useState(null);
    const [formData, setFormData] = useState({
        dictLabel: '',
        dictValue: '',
        dictSort: 0,
        status: '0',
        remark: ''
    });
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        // Get dictType from URL query params
        const searchParams = new URLSearchParams(window.location.search);
        const type = searchParams.get('dictType');
        if (type) {
            setDictType(type);
            fetchDictData(type);
        }
    }, []);

    const fetchDictData = (type) => {
        setLoading(true);
        // Fetch dict data
        fetch(`/api/system/dict/data/list?dictType=${type}`)
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setDictData(data.data || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch dict data:", err);
                setLoading(false);
            });

        // Fetch dict type info to get the display name
        fetch(`/api/system/dict/type/get-by-code/${type}`)
            .then(res => res.json())
            .then(data => {
                if (data.code === 200 && data.data) {
                    setDictName(data.data.dictName || type);
                } else {
                    setDictName(type);
                }
            })
            .catch(err => {
                console.error("Failed to fetch dict type:", err);
                setDictName(type);
            });
    };

    const handleGoBack = () => {
        window.close(); // Try to close the tab
        // If close doesn't work, redirect to dict list
        setTimeout(() => {
            window.location.href = '/#/system/dict';
        }, 100);
    };

    const handleAddClick = () => {
        setFormMode('add');
        setCurrentData(null);
        setFormData({
            dictLabel: '',
            dictValue: '',
            dictSort: 0,
            status: '0',
            remark: ''
        });
        setIsFormModalOpen(true);
    };

    const handleEditClick = (row) => {
        setFormMode('edit');
        setCurrentData(row);
        setFormData({
            dictLabel: row.dictLabel || '',
            dictValue: row.dictValue || '',
            dictSort: row.dictSort || 0,
            status: row.status || '0',
            remark: row.remark || ''
        });
        setIsFormModalOpen(true);
    };

    const handleDeleteClick = (row) => {
        if (window.confirm(`Delete dictionary data "${row.dictLabel}"?`)) {
            fetch(`/api/system/dict/data/${row.dictCode}`, {
                method: 'DELETE'
            })
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    toast.success('Dictionary data deleted successfully');
                    fetchDictData(dictType);
                } else {
                    toast.error(data.msg || 'Failed to delete');
                }
            })
            .catch(err => {
                console.error("Failed to delete:", err);
                toast.error('Failed to delete');
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
        if (!formData.dictLabel || !formData.dictValue) {
            toast.error('Label and Value are required');
            return;
        }

        setSubmitting(true);

        const url = '/api/system/dict/data';
        const method = formMode === 'add' ? 'POST' : 'PUT';
        const body = {
            ...formData,
            dictType: dictType,
            dictSort: parseInt(formData.dictSort) || 0,
            dictCode: formMode === 'edit' ? currentData.dictCode : null
        };

        fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        })
        .then(res => res.json())
        .then(data => {
            setSubmitting(false);
            if (data.code === 200) {
                setIsFormModalOpen(false);
                toast.success(`Dictionary data ${formMode === 'add' ? 'added' : 'updated'} successfully`);
                fetchDictData(dictType);
            } else {
                toast.error(data.msg || `Failed to ${formMode} dictionary data`);
            }
        })
        .catch(err => {
            setSubmitting(false);
            console.error(`Failed to ${formMode} dictionary data:`, err);
            toast.error(`Failed to ${formMode} dictionary data`);
        });
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

    const actions = [
        { label: 'Edit', icon: Edit, onClick: handleEditClick },
        { label: 'Delete', icon: Trash2, danger: true, onClick: handleDeleteClick }
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
        },
        {
            label: 'Add Data',
            icon: Plus,
            onClick: handleAddClick
        }
    ];

    return (
        <div style={{
            height: 'calc(100vh - 70px)',
            overflow: 'auto',
            padding: '8px'
        }}>
            <div className="page-header" style={{ marginBottom: '12px' }}>
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
                            Dictionary Data: {dictName}
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
                actions={actions}
                toolbarActions={toolbarActions}
                loading={loading}
                searchable={true}
                sortable={true}
                pagination={true}
                pageSize={20}
                emptyMessage="No dictionary data found for this type."
            />

            {/* Add/Edit Form Modal */}
            <Modal
                isOpen={isFormModalOpen}
                onClose={() => setIsFormModalOpen(false)}
                title={formMode === 'add' ? 'Add Dictionary Data' : 'Edit Dictionary Data'}
                size="medium"
                footer={
                    <>
                        <button
                            className="btn btn-secondary"
                            onClick={() => setIsFormModalOpen(false)}
                            disabled={submitting}
                        >
                            Cancel
                        </button>
                        <button
                            className="btn btn-primary"
                            onClick={handleSubmit}
                            disabled={submitting}
                            style={{ display: 'flex', alignItems: 'center', gap: '6px' }}
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
                            {formMode === 'add' ? 'Create' : 'Save'}
                        </button>
                    </>
                }
            >
                <div style={{ maxHeight: '60vh', overflowY: 'auto', paddingRight: '8px' }}>
                    <div className="form-row">
                        <FormInput
                            label="Dict Name"
                            name="dictName"
                            value={dictName}
                            disabled
                        />
                        <FormInput
                            label="Data Label *"
                            name="dictLabel"
                            value={formData.dictLabel}
                            onChange={handleInputChange}
                            placeholder="e.g., Normal"
                        />
                    </div>

                    <div className="form-row">
                        <FormInput
                            label="Data Value *"
                            name="dictValue"
                            value={formData.dictValue}
                            onChange={handleInputChange}
                            placeholder="e.g., 0"
                        />
                        <FormInput
                            label="Sort Order"
                            name="dictSort"
                            type="number"
                            value={formData.dictSort}
                            onChange={handleInputChange}
                            placeholder="0"
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
                            placeholder="Optional remark"
                            rows={3}
                            className="form-input"
                            style={{ resize: 'vertical' }}
                        />
                    </div>
                </div>
            </Modal>
        </div>
    );
};

export default DictDataView;
