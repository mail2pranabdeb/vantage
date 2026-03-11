import { useState, useEffect } from 'react';
import { Briefcase, Plus, Edit, Trash2, Eye } from 'lucide-react';
import DataGrid from '../components/DataGrid';

const PostList = () => {
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch('/api/system/post/list')
            .then(res => res.json())
            .then(data => {
                if (data.code === 200) {
                    setPosts(data.data || []);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Failed to fetch posts:", err);
                setLoading(false);
            });
    }, []);

    const columns = [
        { key: 'postId', header: 'ID', sortable: true, align: 'center' },
        {
            key: 'postCode',
            header: 'Code',
            sortable: true,
            render: (value) => <span className="badge-outline" style={{ fontSize: '12px' }}>{value}</span>
        },
        {
            key: 'postName',
            header: 'Post Name',
            sortable: true,
            render: (value, row) => (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Briefcase size={16} style={{ opacity: 0.5 }} />
                    <span style={{ fontWeight: 600 }}>{value}</span>
                </div>
            )
        },
        { key: 'postSort', header: 'Sort', sortable: true, align: 'center' },
        {
            key: 'status',
            header: 'Status',
            sortable: true,
            render: (value) => (
                <span className={`status-pill ${value === '0' ? 'active' : 'inactive'}`}>
                    {value === '0' ? 'Normal' : 'Disabled'}
                </span>
            )
        }
    ];

    const actions = [
        { label: 'View', icon: Eye, onClick: (row) => console.log('View:', row) },
        { label: 'Edit', icon: Edit, onClick: (row) => console.log('Edit:', row) },
        { label: 'Delete', icon: Trash2, danger: true, onClick: (row) => console.log('Delete:', row) }
    ];

    return (
        <div className="page-container">
            <div className="page-header">
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{
                        width: '40px',
                        height: '40px',
                        borderRadius: '10px',
                        background: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: '#333'
                    }}>
                        <Briefcase size={20} />
                    </div>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 700, margin: 0 }}>Post Management</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: '4px 0 0' }}>
                            Manage job positions and posts
                        </p>
                    </div>
                </div>
                <button className="btn btn-primary" style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    padding: '10px 16px',
                    borderRadius: '8px',
                    fontWeight: 600
                }}>
                    <Plus size={18} />
                    Add Post
                </button>
            </div>

            <DataGrid
                data={posts}
                columns={columns}
                actions={actions}
                loading={loading}
                searchable={true}
                sortable={true}
                filterable={true}
                selectable={true}
                pagination={true}
                pageSize={10}
                emptyMessage="No posts found."
            />
        </div>
    );
};

export default PostList;
