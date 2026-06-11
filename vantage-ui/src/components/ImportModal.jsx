import React, { useState, useRef } from 'react';
import { Upload, FileText, AlertCircle, CheckCircle, X, ChevronRight, ChevronLeft, Download } from 'lucide-react';
import Modal from './Modal';
import { toast } from './Toast';

const STEPS = ['Upload', 'Preview', 'Results'];
const ENTITY_TYPES = [
    { value: 'user', label: 'Users' },
    { value: 'role', label: 'Roles' },
    { value: 'config', label: 'Configs' },
];

const ImportModal = ({ isOpen, onClose, onImportComplete }) => {
    const [step, setStep] = useState(0);
    const [file, setFile] = useState(null);
    const [entityType, setEntityType] = useState('user');
    const [preview, setPreview] = useState(null);
    const [loading, setLoading] = useState(false);
    const [result, setResult] = useState(null);
    const fileRef = useRef(null);

    const reset = () => {
        setStep(0); setFile(null); setPreview(null);
        setLoading(false); setResult(null); setEntityType('user');
    };

    const handleClose = () => {
        reset(); onClose();
    };

    const handleFileSelect = (e) => {
        const f = e.target.files?.[0];
        if (f) setFile(f);
    };

    const handleUpload = () => {
        if (!file) return;
        setLoading(true);
        const formData = new FormData();
        formData.append('file', file);
        fetch('/api/system/import/preview', { method: 'POST', body: formData })
            .then(r => r.json())
            .then(d => {
                if (d.code === 200) { setPreview(d.data); setStep(1); }
                else { toast.error(d.msg || 'Preview failed', 5000); }
            })
            .catch(() => { toast.error('Upload failed', 5000); })
            .finally(() => setLoading(false));
    };

    const handleExecute = () => {
        if (!preview) return;
        setLoading(true);
        fetch('/api/system/import/execute', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                entityType,
                headers: preview.headers,
                rows: preview.rows,
            })
        })
            .then(r => r.json())
            .then(d => {
                if (d.code === 200) { setResult(d.data); setStep(2); onImportComplete?.(); }
                else { toast.error(d.msg || 'Import failed', 5000); }
            })
            .catch(() => { toast.error('Import failed', 5000); })
            .finally(() => setLoading(false));
    };

    const dropZoneStyle = {
        border: '2px dashed var(--border-color)', borderRadius: '12px',
        padding: '40px', textAlign: 'center', cursor: 'pointer',
        transition: 'all 0.2s', background: 'var(--bg-tertiary)'
    };

    if (!isOpen) return null;

    return (
        <Modal isOpen={isOpen} onClose={handleClose} title={`Import Data — ${STEPS[step]}`} size="large">
            {/* Step indicator */}
            <div style={{ display: 'flex', gap: '8px', marginBottom: '24px' }}>
                {STEPS.map((s, i) => (
                    <div key={s} style={{ flex: 1, display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <div style={{
                            width: '28px', height: '28px', borderRadius: '50%',
                            background: i <= step ? 'var(--primary-color)' : 'var(--bg-tertiary)',
                            color: i <= step ? 'white' : 'var(--text-muted)',
                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                            fontSize: '12px', fontWeight: 700, transition: 'all 0.3s'
                        }}>{i + 1}</div>
                        <span style={{ fontSize: '12px', color: i <= step ? 'var(--text-primary)' : 'var(--text-muted)', fontWeight: i === step ? 600 : 400 }}>{s}</span>
                        {i < STEPS.length - 1 && <div style={{ flex: 1, height: '2px', background: i < step ? 'var(--primary-color)' : 'var(--border-color)' }} />}
                    </div>
                ))}
            </div>

            {/* Step 0: Upload */}
            {step === 0 && (
                <>
                    <div style={{ marginBottom: '16px' }}>
                        <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-primary)', display: 'block', marginBottom: '6px' }}>Entity Type</label>
                        <select
                            value={entityType}
                            onChange={e => setEntityType(e.target.value)}
                            className="form-control"
                            style={{ width: '200px' }}
                        >
                            {ENTITY_TYPES.map(et => (
                                <option key={et.value} value={et.value}>{et.label}</option>
                            ))}
                        </select>
                    </div>

                    <div
                        style={dropZoneStyle}
                        onClick={() => fileRef.current?.click()}
                        onDragOver={e => { e.preventDefault(); e.currentTarget.style.borderColor = 'var(--primary-color)'; }}
                        onDragLeave={e => { e.currentTarget.style.borderColor = 'var(--border-color)'; }}
                        onDrop={e => {
                            e.preventDefault();
                            e.currentTarget.style.borderColor = 'var(--border-color)';
                            const f = e.dataTransfer.files?.[0];
                            if (f) setFile(f);
                        }}
                    >
                        <input ref={fileRef} type="file" accept=".csv,.xlsx,.xls" onChange={handleFileSelect} style={{ display: 'none' }} />
                        <Upload size={32} style={{ color: 'var(--text-muted)', marginBottom: '12px' }} />
                        <p style={{ margin: '0 0 4px', fontSize: '14px', color: 'var(--text-primary)', fontWeight: 600 }}>
                            {file ? file.name : 'Drop CSV or Excel file here, or click to browse'}
                        </p>
                        <p style={{ margin: 0, fontSize: '11px', color: 'var(--text-muted)' }}>
                            Supported: .csv, .xlsx, .xls
                        </p>
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '16px' }}>
                        <button className="btn btn-secondary" onClick={handleClose}>Cancel</button>
                        <button className="btn btn-primary" onClick={handleUpload} disabled={!file || loading}>
                            {loading ? 'Uploading...' : 'Preview'} <ChevronRight size={16} />
                        </button>
                    </div>
                </>
            )}

            {/* Step 1: Preview */}
            {step === 1 && preview && (
                <>
                    <div style={{ marginBottom: '12px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                            {preview.totalRows} rows found in <strong>{preview.sheetName}</strong>
                        </span>
                        <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                            Headers: <strong>{preview.headers?.length}</strong>
                        </span>
                    </div>

                    <div style={{ overflowX: 'auto', border: '1px solid var(--border-color)', borderRadius: '8px' }}>
                        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '11px' }}>
                            <thead>
                                <tr style={{ background: 'var(--bg-tertiary)' }}>
                                    <th style={{ padding: '8px', textAlign: 'left', borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)', fontWeight: 600, width: '40px' }}>#</th>
                                    {preview.headers?.map(h => (
                                        <th key={h} style={{ padding: '8px', textAlign: 'left', borderBottom: '1px solid var(--border-color)', color: 'var(--text-primary)', fontWeight: 600 }}>{h}</th>
                                    ))}
                                </tr>
                            </thead>
                            <tbody>
                                {preview.rows?.map((row, i) => (
                                    <tr key={i} style={{ borderBottom: '1px solid var(--border-color)' }}>
                                        <td style={{ padding: '8px', color: 'var(--text-muted)' }}>{i + 2}</td>
                                        {preview.headers.map(h => (
                                            <td key={h} style={{ padding: '8px', color: 'var(--text-secondary)', maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis' }}>{row[h] || ''}</td>
                                        ))}
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>

                    {preview.totalRows > preview.rows?.length && (
                        <p style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '8px', textAlign: 'center' }}>
                            Showing {preview.rows?.length} of {preview.totalRows} rows
                        </p>
                    )}

                    <div style={{ display: 'flex', justifyContent: 'space-between', gap: '8px', marginTop: '16px' }}>
                        <button className="btn btn-secondary" onClick={() => setStep(0)}><ChevronLeft size={16} /> Back</button>
                        <div style={{ display: 'flex', gap: '8px' }}>
                            <button className="btn btn-secondary" onClick={handleClose}>Cancel</button>
                            <button className="btn btn-primary" onClick={handleExecute} disabled={loading}>
                                {loading ? 'Importing...' : `Import ${preview.totalRows} rows`} <CheckCircle size={16} />
                            </button>
                        </div>
                    </div>
                </>
            )}

            {/* Step 2: Results */}
            {step === 2 && result && (
                <>
                    <div style={{ textAlign: 'center', padding: '24px' }}>
                        {result.errorCount === 0 ? (
                            <CheckCircle size={48} style={{ color: '#10b981', marginBottom: '12px' }} />
                        ) : (
                            <AlertCircle size={48} style={{ color: result.successCount > 0 ? '#f59e0b' : '#ef4444', marginBottom: '12px' }} />
                        )}
                        <p style={{ fontSize: '16px', fontWeight: 700, margin: '0 0 4px', color: 'var(--text-primary)' }}>
                            {result.successCount} of {result.successCount + result.errorCount} rows imported
                        </p>
                        <p style={{ fontSize: '12px', color: 'var(--text-muted)', margin: 0 }}>
                            {result.errorCount > 0 ? `${result.errorCount} errors encountered` : 'All rows imported successfully'}
                        </p>
                    </div>

                    {result.errors?.length > 0 && (
                        <div style={{ background: 'var(--bg-tertiary)', borderRadius: '8px', padding: '12px', maxHeight: '200px', overflowY: 'auto' }}>
                            <p style={{ fontSize: '11px', fontWeight: 600, color: '#ef4444', margin: '0 0 8px' }}>Errors:</p>
                            {result.errors.map((err, i) => (
                                <p key={i} style={{ fontSize: '11px', color: 'var(--text-secondary)', margin: '0 0 4px' }}>{err}</p>
                            ))}
                        </div>
                    )}

                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '16px' }}>
                        <button className="btn btn-secondary" onClick={handleClose}>Close</button>
                        <button className="btn btn-primary" onClick={() => { reset(); }}>Import Another</button>
                    </div>
                </>
            )}
        </Modal>
    );
};

export default ImportModal;
