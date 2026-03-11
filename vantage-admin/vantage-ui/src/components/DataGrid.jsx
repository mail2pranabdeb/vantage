import { useState, useMemo } from 'react';
import {
    ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight,
    Search, Filter, X, ArrowUpDown, ArrowUp, ArrowDown,
    CheckSquare, Square, MoreVertical, Eye, Edit, Trash2,
    Download, Upload, RefreshCw, Plus
} from 'lucide-react';

const DataGrid = ({
    data = [],
    columns = [],
    loading = false,
    emptyMessage = 'No data available',
    searchable = true,
    sortable = true,
    filterable = true,
    selectable = true,
    pagination = true,
    pageSize = 10,
    actions = [],
    onSelectionChange,
    onRowClick,
    toolbarActions = []
}) => {
    const [searchTerm, setSearchTerm] = useState('');
    const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' });
    const [filters, setFilters] = useState({});
    const [selectedRows, setSelectedRows] = useState(new Set());
    const [currentPage, setCurrentPage] = useState(1);
    const [showFilters, setShowFilters] = useState(false);

    // Filter data based on search term and column filters
    const filteredData = useMemo(() => {
        return data.filter(row => {
            // Global search
            if (searchTerm) {
                const searchLower = searchTerm.toLowerCase();
                const matchesSearch = columns.some(col => {
                    const value = row[col.key];
                    return value && String(value).toLowerCase().includes(searchLower);
                });
                if (!matchesSearch) return false;
            }

            // Column filters
            for (const [key, filterValue] of Object.entries(filters)) {
                if (filterValue) {
                    const rowValue = String(row[key] || '').toLowerCase();
                    if (!rowValue.includes(filterValue.toLowerCase())) {
                        return false;
                    }
                }
            }

            return true;
        });
    }, [data, searchTerm, filters, columns]);

    // Sort data
    const sortedData = useMemo(() => {
        if (!sortConfig.key) return filteredData;

        return [...filteredData].sort((a, b) => {
            const aValue = a[sortConfig.key];
            const bValue = b[sortConfig.key];

            if (aValue === bValue) return 0;
            if (aValue === null || aValue === undefined) return 1;
            if (bValue === null || bValue === undefined) return -1;

            const comparison = aValue < bValue ? -1 : 1;
            return sortConfig.direction === 'asc' ? comparison : -comparison;
        });
    }, [filteredData, sortConfig]);

    // Pagination
    const totalPages = Math.ceil(sortedData.length / pageSize);
    const paginatedData = useMemo(() => {
        if (!pagination) return sortedData;
        const start = (currentPage - 1) * pageSize;
        return sortedData.slice(start, start + pageSize);
    }, [sortedData, currentPage, pageSize, pagination]);

    // Handle sort
    const handleSort = (key) => {
        if (!sortable) return;
        setSortConfig(prev => ({
            key,
            direction: prev.key === key && prev.direction === 'asc' ? 'desc' : 'asc'
        }));
    };

    // Handle row selection
    const toggleRowSelection = (id) => {
        const newSelected = new Set(selectedRows);
        if (newSelected.has(id)) {
            newSelected.delete(id);
        } else {
            newSelected.add(id);
        }
        setSelectedRows(newSelected);
        onSelectionChange?.(Array.from(newSelected));
    };

    const toggleSelectAll = () => {
        if (selectedRows.size === paginatedData.length) {
            setSelectedRows(new Set());
            onSelectionChange?.([]);
        } else {
            const newSelected = new Set(paginatedData.map(row => row.id || row.key));
            setSelectedRows(newSelected);
            onSelectionChange?.(Array.from(newSelected));
        }
    };

    // Reset pagination when search changes
    useState(() => {
        setCurrentPage(1);
    }, [searchTerm]);

    if (loading) {
        return (
            <div className="ag-loading">
                <div className="spinner" />
                Loading data...
            </div>
        );
    }

    return (
        <div className="ag-grid-wrapper animate-fade-in">
            {/* Toolbar */}
            <div className="ag-toolbar">
                <div className="ag-toolbar-left">
                    {/* Search */}
                    {searchable && (
                        <div className="ag-search">
                            <Search size={14} />
                            <input
                                type="text"
                                placeholder="Search..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                            />
                            {searchTerm && (
                                <button onClick={() => setSearchTerm('')} className="ag-clear-btn">
                                    <X size={12} />
                                </button>
                            )}
                        </div>
                    )}

                    {/* Filter Toggle */}
                    {filterable && (
                        <button
                            onClick={() => setShowFilters(!showFilters)}
                            className={`ag-filter-btn ${showFilters ? 'active' : ''}`}
                        >
                            <Filter size={14} />
                            <span>Filters</span>
                        </button>
                    )}

                    {/* Selected count */}
                    {selectable && selectedRows.size > 0 && (
                        <span className="ag-selected-count">
                            {selectedRows.size} selected
                        </span>
                    )}
                </div>

                {/* Toolbar Actions */}
                <div className="ag-toolbar-right">
                    {toolbarActions.map((action, idx) => (
                        <button key={idx} onClick={action.onClick} className="ag-toolbar-btn">
                            {action.icon && <action.icon size={14} />}
                            {action.label && <span>{action.label}</span>}
                        </button>
                    ))}
                </div>
            </div>

            {/* Filter Bar */}
            {showFilters && (
                <div className="ag-filter-bar">
                    {columns.filter(col => col.filterable !== false).map(col => (
                        <div key={col.key} className="ag-filter-item">
                            <label>
                                {col.header}
                            </label>
                            <input
                                type="text"
                                placeholder={`Filter ${col.header}...`}
                                value={filters[col.key] || ''}
                                onChange={(e) => setFilters(prev => ({
                                    ...prev,
                                    [col.key]: e.target.value
                                }))}
                            />
                        </div>
                    ))}
                    <button
                        onClick={() => setFilters({})}
                        className="ag-clear-filters-btn"
                    >
                        Clear Filters
                    </button>
                </div>
            )}

            {/* Table Container */}
            <div className="ag-table-container">
                <table className="ag-table">
                    <thead>
                        <tr>
                            {selectable && (
                                <th className="ag-col-select">
                                    <button
                                        onClick={toggleSelectAll}
                                        className="ag-checkbox-btn"
                                    >
                                        {selectedRows.size === paginatedData.length && paginatedData.length > 0 ? (
                                            <CheckSquare size={14} className="ag-checked" />
                                        ) : (
                                            <Square size={14} />
                                        )}
                                    </button>
                                </th>
                            )}
                            {columns.map(col => (
                                <th
                                    key={col.key}
                                    className={`ag-col-header ${sortable && col.sortable !== false ? 'ag-sortable' : ''}`}
                                    style={{ 
                                        width: col.width,
                                        minWidth: col.minWidth || '100px',
                                        textAlign: col.align || 'left'
                                    }}
                                    onClick={() => handleSort(col.key)}
                                >
                                    <div className="ag-col-header-content">
                                        <span className="ag-col-title">{col.header}</span>
                                        {sortable && col.sortable !== false && (
                                            <span className="ag-sort-indicator">
                                                {sortConfig.key === col.key ? (
                                                    sortConfig.direction === 'asc' ? (
                                                        <ArrowUp size={12} />
                                                    ) : (
                                                        <ArrowDown size={12} />
                                                    )
                                                ) : (
                                                    <ArrowUpDown size={12} />
                                                )}
                                            </span>
                                        )}
                                    </div>
                                </th>
                            ))}
                            {actions.length > 0 && (
                                <th className="ag-col-actions">
                                    Actions
                                </th>
                            )}
                        </tr>
                    </thead>
                    <tbody>
                        {paginatedData.length === 0 ? (
                            <tr>
                                <td
                                    colSpan={columns.length + (selectable ? 1 : 0) + (actions.length > 0 ? 1 : 0)}
                                    className="ag-empty-cell"
                                >
                                    {emptyMessage}
                                </td>
                            </tr>
                        ) : (
                            paginatedData.map((row, idx) => (
                                <tr
                                    key={row.id || row.key || idx}
                                    className={`ag-row ${selectedRows.has(row.id || row.key) ? 'ag-row-selected' : ''}`}
                                    onClick={() => onRowClick?.(row)}
                                >
                                    {selectable && (
                                        <td className="ag-cell-select">
                                            <button
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    toggleRowSelection(row.id || row.key);
                                                }}
                                                className="ag-checkbox-btn"
                                            >
                                                {selectedRows.has(row.id || row.key) ? (
                                                    <CheckSquare size={14} className="ag-checked" />
                                                ) : (
                                                    <Square size={14} />
                                                )}
                                            </button>
                                        </td>
                                    )}
                                    {columns.map(col => (
                                        <td
                                            key={col.key}
                                            className="ag-cell"
                                            style={{ textAlign: col.align || 'left' }}
                                        >
                                            {col.render ? col.render(row[col.key], row) : row[col.key]}
                                        </td>
                                    ))}
                                    {actions.length > 0 && (
                                        <td className="ag-cell-actions">
                                            <div className="ag-actions-wrapper">
                                                {actions.map((action, actionIdx) => (
                                                    <button
                                                        key={actionIdx}
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            action.onClick(row);
                                                        }}
                                                        title={action.label}
                                                        className={`ag-action-btn ${action.danger ? 'ag-action-danger' : ''}`}
                                                    >
                                                        {action.icon ? <action.icon size={14} /> : <MoreVertical size={14} />}
                                                    </button>
                                                ))}
                                            </div>
                                        </td>
                                    )}
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {/* Pagination */}
            {pagination && totalPages > 1 && (
                <div className="ag-pagination">
                    <div className="ag-pagination-info">
                        Showing {(currentPage - 1) * pageSize + 1} to {Math.min(currentPage * pageSize, sortedData.length)} of {sortedData.length} entries
                    </div>

                    <div className="ag-pagination-controls">
                        <button
                            onClick={() => setCurrentPage(1)}
                            disabled={currentPage === 1}
                            className="ag-page-btn"
                            title="First page"
                        >
                            <ChevronsLeft size={14} />
                        </button>
                        <button
                            onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
                            disabled={currentPage === 1}
                            className="ag-page-btn"
                            title="Previous page"
                        >
                            <ChevronLeft size={14} />
                        </button>

                        <div className="ag-page-numbers">
                            {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                                let pageNum;
                                if (totalPages <= 5) {
                                    pageNum = i + 1;
                                } else if (currentPage <= 3) {
                                    pageNum = i + 1;
                                } else if (currentPage >= totalPages - 2) {
                                    pageNum = totalPages - 4 + i;
                                } else {
                                    pageNum = currentPage - 2 + i;
                                }
                                return (
                                    <button
                                        key={pageNum}
                                        onClick={() => setCurrentPage(pageNum)}
                                        className={`ag-page-num ${currentPage === pageNum ? 'active' : ''}`}
                                    >
                                        {pageNum}
                                    </button>
                                );
                            })}
                        </div>

                        <button
                            onClick={() => setCurrentPage(prev => Math.min(totalPages, prev + 1))}
                            disabled={currentPage === totalPages}
                            className="ag-page-btn"
                            title="Next page"
                        >
                            <ChevronRight size={14} />
                        </button>
                        <button
                            onClick={() => setCurrentPage(totalPages)}
                            disabled={currentPage === totalPages}
                            className="ag-page-btn"
                            title="Last page"
                        >
                            <ChevronsRight size={14} />
                        </button>
                    </div>

                    <div className="ag-page-size">
                        <span>Rows per page:</span>
                        <select 
                            value={pageSize} 
                            onChange={(e) => {
                                setCurrentPage(1);
                            }}
                            className="ag-page-size-select"
                        >
                            <option value={10}>10</option>
                            <option value={25}>25</option>
                            <option value={50}>50</option>
                            <option value={100}>100</option>
                        </select>
                    </div>
                </div>
            )}

            <style>{`
                @keyframes spin {
                    to { transform: rotate(360deg); }
                }

                /* Loading State */
                .ag-loading {
                    padding: 40px 20px;
                    text-align: center;
                    color: var(--text-muted);
                    font-size: 13px;
                    background: var(--bg-secondary);
                    border: 1px solid var(--border-color);
                    border-radius: 8px;
                }

                .ag-loading .spinner {
                    width: 24px;
                    height: 24px;
                    border: 2px solid var(--border-color);
                    border-top-color: var(--primary-color);
                    border-radius: 50%;
                    animation: spin 1s linear infinite;
                    margin: 0 auto 12px;
                }

                /* Toolbar Layout */
                .ag-toolbar-left,
                .ag-toolbar-right {
                    display: flex;
                    align-items: center;
                    gap: 8px;
                }

                .ag-toolbar-left {
                    flex: 1;
                }

                /* Toolbar */
                .ag-toolbar {
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    padding: 6px 10px;
                    background: var(--bg-secondary);
                    border: 1px solid var(--border-color);
                    border-bottom: none;
                    border-radius: 8px 8px 0 0;
                }

                .ag-search {
                    position: relative;
                    display: flex;
                    align-items: center;
                }

                .ag-search input {
                    width: 200px;
                    padding: 5px 24px 5px 28px;
                    border: 1px solid var(--border-color);
                    border-radius: 5px;
                    font-size: 12px;
                    color: var(--text-primary);
                    background: var(--bg-tertiary);
                    transition: all 0.2s ease;
                }

                .ag-search input:focus {
                    outline: none;
                    border-color: var(--primary-color);
                    background: var(--bg-secondary);
                    box-shadow: 0 0 0 2px var(--primary-soft);
                }

                .ag-search svg {
                    color: var(--text-muted);
                    position: absolute;
                    left: 6px;
                    top: 50%;
                    transform: translateY(-50%);
                }

                .ag-clear-btn {
                    position: absolute;
                    right: 6px;
                    top: 50%;
                    transform: translateY(-50%);
                    background: none;
                    border: none;
                    cursor: pointer;
                    color: var(--text-muted);
                    padding: 2px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    border-radius: 4px;
                    transition: all 0.15s ease;
                }

                .ag-clear-btn:hover {
                    background: var(--bg-tertiary);
                    color: var(--text-primary);
                }

                .ag-filter-btn {
                    display: flex;
                    align-items: center;
                    gap: 4px;
                    padding: 5px 10px;
                    background: var(--bg-tertiary);
                    border: 1px solid var(--border-color);
                    border-radius: 5px;
                    font-size: 11px;
                    font-weight: 500;
                    color: var(--text-secondary);
                    cursor: pointer;
                    transition: all 0.15s ease;
                }

                .ag-filter-btn:hover {
                    background: var(--border-color);
                    color: var(--text-primary);
                }

                .ag-filter-btn.active {
                    background: var(--primary-color);
                    border-color: var(--primary-color);
                    color: #fff;
                }

                .ag-selected-count {
                    font-size: 11px;
                    color: var(--primary-color);
                    font-weight: 500;
                    padding: 3px 8px;
                    background: var(--primary-soft);
                    border-radius: 5px;
                }

                .ag-toolbar-btn {
                    display: flex;
                    align-items: center;
                    gap: 4px;
                    padding: 5px 10px;
                    background: var(--bg-tertiary);
                    border: 1px solid var(--border-color);
                    border-radius: 6px;
                    font-size: 12px;
                    font-weight: 500;
                    color: var(--text-secondary);
                    cursor: pointer;
                    transition: all 0.15s ease;
                }

                .ag-toolbar-btn:hover {
                    background: var(--border-color);
                    color: var(--text-primary);
                }

                /* Filter Bar */
                .ag-filter-bar {
                    display: flex;
                    align-items: flex-end;
                    gap: 8px;
                    padding: 6px 10px;
                    background: var(--bg-tertiary);
                    border: 1px solid var(--border-color);
                    border-left: 1px solid var(--border-color);
                    border-right: 1px solid var(--border-color);
                }

                .ag-filter-item {
                    display: flex;
                    flex-direction: column;
                    gap: 3px;
                    min-width: 120px;
                }

                .ag-filter-item label {
                    font-size: 9px;
                    font-weight: 600;
                    color: var(--text-muted);
                    text-transform: uppercase;
                    letter-spacing: 0.3px;
                }

                .ag-filter-item input {
                    padding: 5px 8px;
                    border: 1px solid var(--border-color);
                    border-radius: 5px;
                    font-size: 11px;
                    color: var(--text-primary);
                    background: var(--bg-secondary);
                    transition: all 0.2s ease;
                }

                .ag-filter-item input:focus {
                    outline: none;
                    border-color: var(--primary-color);
                    box-shadow: 0 0 0 2px var(--primary-soft);
                }

                .ag-clear-filters-btn {
                    padding: 5px 10px;
                    background: var(--bg-tertiary);
                    border: 1px solid var(--border-color);
                    border-radius: 5px;
                    font-size: 11px;
                    color: var(--text-secondary);
                    cursor: pointer;
                    transition: all 0.15s ease;
                    align-self: flex-end;
                }

                .ag-clear-filters-btn:hover {
                    background: var(--bg-secondary);
                    border-color: var(--border-color);
                    color: var(--text-primary);
                }

                /* Table */
                .ag-table-container {
                    overflow: auto;
                    border: 1px solid var(--border-color);
                    border-top: none;
                    background: var(--bg-secondary);
                }

                .ag-table {
                    width: 100%;
                    border-collapse: collapse;
                    font-size: 12px;
                    font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                }

                .ag-table thead {
                    background: var(--bg-tertiary);
                }

                .ag-table thead tr {
                    border-bottom: 1px solid var(--border-color);
                }

                .ag-col-select,
                .ag-col-actions {
                    padding: 6px 8px;
                    width: 36px;
                    text-align: center;
                    font-size: 10px;
                    font-weight: 600;
                    color: var(--text-muted);
                    text-transform: uppercase;
                    letter-spacing: 0.3px;
                }

                .ag-col-header {
                    padding: 6px 8px;
                    font-size: 10px;
                    font-weight: 600;
                    color: var(--text-muted);
                    text-transform: uppercase;
                    letter-spacing: 0.3px;
                    border-right: 1px solid var(--border-color);
                    cursor: default;
                    user-select: none;
                    white-space: nowrap;
                }

                .ag-col-header:last-child {
                    border-right: none;
                }

                .ag-col-header.ag-sortable {
                    cursor: pointer;
                    transition: background 0.15s ease;
                }

                .ag-col-header.ag-sortable:hover {
                    background: var(--bg-secondary);
                }

                .ag-col-header-content {
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    gap: 6px;
                }

                .ag-sort-indicator {
                    color: var(--text-muted);
                    display: flex;
                    align-items: center;
                    flex-shrink: 0;
                }

                .ag-col-header.ag-sortable:hover .ag-sort-indicator {
                    color: var(--text-secondary);
                }

                .ag-table tbody tr {
                    border-bottom: 1px solid var(--border-color);
                    transition: background 0.15s ease;
                }

                .ag-table tbody tr:last-child {
                    border-bottom: none;
                }

                .ag-row {
                    cursor: default;
                }

                .ag-row:hover {
                    background: var(--primary-soft);
                }

                .ag-row-selected {
                    background: var(--primary-soft);
                    border-left: 3px solid var(--primary-color);
                }

                .ag-row-selected:hover {
                    background: var(--primary-soft);
                }

                .ag-cell {
                    padding: 6px 8px;
                    color: var(--text-primary);
                    border-right: 1px solid var(--border-color);
                    vertical-align: middle;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                }

                .ag-cell:last-child {
                    border-right: none;
                }

                .ag-cell-select,
                .ag-cell-actions {
                    padding: 6px 8px;
                    text-align: center;
                }

                .ag-checkbox-btn {
                    background: none;
                    border: none;
                    cursor: pointer;
                    color: var(--text-muted);
                    padding: 2px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    border-radius: 4px;
                    transition: all 0.15s ease;
                }

                .ag-checkbox-btn:hover {
                    color: var(--primary-color);
                    background: var(--primary-soft);
                }

                .ag-checked {
                    color: var(--primary-color);
                }

                .ag-empty-cell {
                    padding: 48px 20px;
                    text-align: center;
                    color: var(--text-muted);
                    font-size: 13px;
                }

                .ag-actions-wrapper {
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    gap: 4px;
                }

                .ag-action-btn {
                    background: none;
                    border: none;
                    cursor: pointer;
                    color: var(--text-muted);
                    padding: 4px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    border-radius: 4px;
                    transition: all 0.15s ease;
                }

                .ag-action-btn:hover {
                    background: var(--bg-tertiary);
                    color: var(--primary-color);
                }

                .ag-action-danger:hover {
                    color: var(--danger);
                    background: rgba(239, 68, 68, 0.1);
                }

                /* Pagination */
                .ag-pagination {
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    padding: 6px 10px;
                    background: var(--bg-secondary);
                    border: 1px solid var(--border-color);
                    border-top: none;
                    border-radius: 0 0 8px 8px;
                }

                .ag-pagination-info {
                    font-size: 11px;
                    color: var(--text-secondary);
                }

                .ag-pagination-controls {
                    display: flex;
                    align-items: center;
                    gap: 3px;
                }

                .ag-page-btn {
                    width: 24px;
                    height: 24px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    background: var(--bg-secondary);
                    border: 1px solid var(--border-color);
                    border-radius: 5px;
                    color: var(--text-secondary);
                    cursor: pointer;
                    transition: all 0.15s ease;
                }

                .ag-page-btn:hover:not(:disabled) {
                    background: var(--bg-tertiary);
                    border-color: var(--border-color);
                    color: var(--primary-color);
                }

                .ag-page-btn:disabled {
                    opacity: 0.4;
                    cursor: not-allowed;
                }

                .ag-page-numbers {
                    display: flex;
                    align-items: center;
                    gap: 3px;
                }

                .ag-page-num {
                    min-width: 24px;
                    height: 24px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    background: var(--bg-secondary);
                    border: 1px solid var(--border-color);
                    border-radius: 5px;
                    font-size: 11px;
                    color: var(--text-secondary);
                    cursor: pointer;
                    transition: all 0.15s ease;
                }

                .ag-page-num:hover {
                    background: var(--bg-tertiary);
                    border-color: var(--border-color);
                    color: var(--primary-color);
                }

                .ag-page-num.active {
                    background: var(--primary-color);
                    border-color: var(--primary-color);
                    color: #fff;
                    font-weight: 500;
                }

                .ag-page-size {
                    display: flex;
                    align-items: center;
                    gap: 6px;
                    font-size: 11px;
                    color: var(--text-secondary);
                }

                .ag-page-size-select {
                    padding: 4px 6px;
                    border: 1px solid var(--border-color);
                    border-radius: 5px;
                    font-size: 11px;
                    color: var(--text-primary);
                    background: var(--bg-secondary);
                    cursor: pointer;
                    outline: none;
                    transition: all 0.15s ease;
                }

                .ag-page-size-select:focus {
                    border-color: var(--primary-color);
                }
            `}</style>
        </div>
    );
};

export default DataGrid;
