import { useState, useRef, useEffect } from 'react';
import { Send, Bot, User, Wrench, CheckCircle, XCircle, Loader, Sparkles, RefreshCw, Database } from 'lucide-react';

const Chat = () => {
    const [messages, setMessages] = useState([
        {
            id: 1,
            role: 'assistant',
            content: 'Hello! I\'m your Vantage Admin assistant. I can help you manage users, roles, menus, and other system configurations. What would you like to do today?',
            timestamp: new Date().toISOString()
        }
    ]);
    const [input, setInput] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [toolCalls, setToolCalls] = useState([]);
    const [aiStatus, setAiStatus] = useState({ enabled: false, knowledgeCount: 0 });
    const [showKnowledge, setShowKnowledge] = useState(false);
    const messagesEndRef = useRef(null);

    // Fetch AI status on mount
    useEffect(() => {
        fetchAiStatus();
    }, []);

    const fetchAiStatus = async () => {
        try {
            const response = await fetch('/api/chat/status');
            const data = await response.json();
            if (data.code === 200) {
                setAiStatus(data.data);
            }
        } catch (error) {
            console.error('Failed to fetch AI status:', error);
        }
    };

    const handleRefreshKnowledge = async () => {
        try {
            const response = await fetch('/api/chat/knowledge/refresh', { method: 'POST' });
            const data = await response.json();
            if (data.code === 200) {
                alert('Knowledge base refreshed successfully!');
                fetchAiStatus();
            }
        } catch (error) {
            console.error('Failed to refresh knowledge:', error);
            alert('Failed to refresh knowledge base');
        }
    };

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const handleSend = async () => {
        if (!input.trim() || isLoading) return;

        const userMessage = {
            id: messages.length + 1,
            role: 'user',
            content: input,
            timestamp: new Date().toISOString()
        };

        setMessages(prev => [...prev, userMessage]);
        setInput('');
        setIsLoading(true);
        setToolCalls([]);

        try {
            // Call the chat API
            const response = await fetch('/api/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    message: input,
                    conversationHistory: messages.slice(-10)
                })
            });

            const data = await response.json();
            console.log('Chat response:', data);

            // Check if data is wrapped in AjaxResult structure
            const chatData = data.code === 200 ? data.data : data;

            if (chatData.toolCalls && chatData.toolCalls.length > 0) {
                setToolCalls(chatData.toolCalls);

                // Execute tool calls
                const toolResults = [];
                for (const toolCall of chatData.toolCalls) {
                    const result = await executeTool(toolCall);
                    toolResults.push({ name: toolCall.name, result });
                }

                // Send tool results back to get final response
                const finalResponse = await fetch('/api/chat', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        message: input,
                        toolResults,
                        conversationHistory: messages.slice(-10)
                    })
                });

                const finalData = await finalResponse.json();
                const finalChatData = finalData.code === 200 ? finalData.data : finalData;

                const assistantMessage = {
                    id: messages.length + 2,
                    role: 'assistant',
                    content: finalChatData.response || 'Operation completed!',
                    timestamp: new Date().toISOString(),
                    toolExecuted: toolResults
                };
                setMessages(prev => [...prev, assistantMessage]);
            } else {
                const assistantMessage = {
                    id: messages.length + 2,
                    role: 'assistant',
                    content: chatData.response || 'I received your message. How can I help you?',
                    timestamp: new Date().toISOString()
                };
                setMessages(prev => [...prev, assistantMessage]);
            }
        } catch (error) {
            console.error('Chat error:', error);
            const errorMessage = {
                id: messages.length + 2,
                role: 'assistant',
                content: 'Sorry, I encountered an error. Please try again.',
                timestamp: new Date().toISOString(),
                error: true
            };
            setMessages(prev => [...prev, errorMessage]);
        } finally {
            setIsLoading(false);
            setToolCalls([]);
        }
    };

    const executeTool = async (toolCall) => {
        const { name, arguments: args } = toolCall;
        
        try {
            let endpoint = '';
            let method = 'POST';
            
            switch (name) {
                case 'createUser':
                    endpoint = '/api/system/user';
                    method = 'POST';
                    break;
                case 'updateUser':
                    endpoint = '/api/system/user';
                    method = 'PUT';
                    break;
                case 'deleteUser':
                    endpoint = `/api/system/user/${args.userId}`;
                    method = 'DELETE';
                    break;
                case 'listUsers':
                    endpoint = '/api/system/user/list';
                    method = 'GET';
                    break;
                case 'createRole':
                    endpoint = '/api/system/role';
                    method = 'POST';
                    break;
                case 'listRoles':
                    endpoint = '/api/system/role/list';
                    method = 'GET';
                    break;
                default:
                    throw new Error(`Unknown tool: ${name}`);
            }

            const response = await fetch(endpoint, {
                method,
                headers: {
                    'Content-Type': 'application/json'
                },
                body: method !== 'GET' ? JSON.stringify(args) : undefined
            });

            const result = await response.json();
            return { success: result.code === 200, data: result };
        } catch (error) {
            return { success: false, error: error.message };
        }
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            handleSend();
        }
    };

    return (
        <div className="page-container" style={{ maxWidth: '1200px', margin: '0 auto' }}>
            <div className="page-header">
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <div style={{
                        width: '32px',
                        height: '32px',
                        borderRadius: '8px',
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'white'
                    }}>
                        <Bot size={16} />
                    </div>
                    <div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <h2 style={{ fontSize: '16px', fontWeight: 700, margin: 0 }}>AI Assistant</h2>
                            {aiStatus.enabled ? (
                                <span style={{
                                    display: 'inline-flex',
                                    alignItems: 'center',
                                    gap: '4px',
                                    padding: '2px 8px',
                                    borderRadius: '12px',
                                    background: 'rgba(16, 185, 129, 0.1)',
                                    color: '#10b981',
                                    fontSize: '10px',
                                    fontWeight: 600
                                }}>
                                    <Sparkles size={10} />
                                    AI Enhanced
                                </span>
                            ) : (
                                <span style={{
                                    display: 'inline-flex',
                                    alignItems: 'center',
                                    gap: '4px',
                                    padding: '2px 8px',
                                    borderRadius: '12px',
                                    background: 'rgba(107, 114, 128, 0.1)',
                                    color: '#6b7280',
                                    fontSize: '10px',
                                    fontWeight: 600
                                }}>
                                    Rule-based Mode
                                </span>
                            )}
                        </div>
                        <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '2px 0 0' }}>
                            {aiStatus.enabled 
                                ? `Knowledge base: ${aiStatus.knowledgeCount} documents • RAG enabled`
                                : 'Chat with MCP tool integration'}
                        </p>
                    </div>
                </div>
                <div style={{ display: 'flex', gap: '8px' }}>
                    <button
                        onClick={handleRefreshKnowledge}
                        title="Refresh Knowledge Base"
                        style={{
                            padding: '6px 12px',
                            borderRadius: '6px',
                            border: '1px solid var(--border-color)',
                            background: 'var(--bg-secondary)',
                            color: 'var(--text-primary)',
                            fontSize: '11px',
                            fontWeight: 500,
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '6px'
                        }}
                    >
                        <RefreshCw size={12} />
                        Refresh KB
                    </button>
                    <button
                        onClick={() => setShowKnowledge(!showKnowledge)}
                        title="View Knowledge Base Stats"
                        style={{
                            padding: '6px 12px',
                            borderRadius: '6px',
                            border: '1px solid var(--border-color)',
                            background: showKnowledge ? 'var(--primary-color)' : 'var(--bg-secondary)',
                            color: showKnowledge ? 'white' : 'var(--text-primary)',
                            fontSize: '11px',
                            fontWeight: 500,
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '6px'
                        }}
                    >
                        <Database size={12} />
                        Knowledge Base
                    </button>
                </div>
            </div>

            {/* Knowledge Base Stats Panel */}
            {showKnowledge && (
                <div className="glass-panel" style={{
                    marginTop: '12px',
                    padding: '16px',
                    borderRadius: '12px'
                }}>
                    <h3 style={{ fontSize: '14px', fontWeight: 600, marginBottom: '12px' }}>
                        Knowledge Base Statistics
                    </h3>
                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
                        gap: '12px'
                    }}>
                        <div style={{
                            padding: '12px',
                            background: 'var(--bg-secondary)',
                            borderRadius: '8px',
                            textAlign: 'center'
                        }}>
                            <div style={{ fontSize: '24px', fontWeight: 700, color: 'var(--primary-color)' }}>
                                {aiStatus.knowledgeCount}
                            </div>
                            <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>
                                Total Documents
                            </div>
                        </div>
                        <div style={{
                            padding: '12px',
                            background: 'var(--bg-secondary)',
                            borderRadius: '8px',
                            textAlign: 'center'
                        }}>
                            <div style={{ fontSize: '24px', fontWeight: 700, color: '#10b981' }}>
                                {aiStatus.enabled ? 'Active' : 'Inactive'}
                            </div>
                            <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>
                                AI Status
                            </div>
                        </div>
                        <div style={{
                            padding: '12px',
                            background: 'var(--bg-secondary)',
                            borderRadius: '8px',
                            textAlign: 'center'
                        }}>
                            <div style={{ fontSize: '24px', fontWeight: 700, color: '#f59e0b' }}>
                                RAG
                            </div>
                            <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>
                                Retrieval Mode
                            </div>
                        </div>
                    </div>
                    <div style={{
                        marginTop: '12px',
                        padding: '10px',
                        background: 'var(--bg-tertiary)',
                        borderRadius: '6px',
                        fontSize: '11px',
                        color: 'var(--text-muted)'
                    }}>
                        <strong>Knowledge Categories:</strong> User Management, Role Management, Menu Management, 
                        Config Management, Dict Management, Job Scheduling, Code Generation, Operation Logging, Login Monitoring
                    </div>
                </div>
            )}

            <div className="glass-panel" style={{
                marginTop: '20px',
                display: 'flex',
                flexDirection: 'column',
                height: 'calc(100vh - 200px)',
                borderRadius: '12px',
                overflow: 'hidden'
            }}>
                {/* Messages Area */}
                <div style={{
                    flex: 1,
                    overflowY: 'auto',
                    padding: '20px 20px 20px 20px',
                    paddingTop: '30px',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '16px'
                }}>
                    {messages.map((message) => (
                        <div
                            key={message.id}
                            style={{
                                display: 'flex',
                                gap: '12px',
                                alignItems: 'flex-start',
                                animation: 'fadeIn 0.3s ease'
                            }}
                        >
                            <div style={{
                                width: '32px',
                                height: '32px',
                                borderRadius: '50%',
                                background: message.role === 'assistant' 
                                    ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' 
                                    : 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                color: 'white',
                                flexShrink: 0
                            }}>
                                {message.role === 'assistant' ? <Bot size={16} /> : <User size={16} />}
                            </div>
                            
                            <div style={{
                                flex: 1,
                                maxWidth: '80%',
                                background: message.role === 'assistant' 
                                    ? 'var(--bg-tertiary)' 
                                    : 'var(--primary-color)',
                                color: message.role === 'assistant' 
                                    ? 'var(--text-primary)' 
                                    : 'white',
                                padding: '12px 16px',
                                borderRadius: '12px',
                                borderTopLeftRadius: message.role === 'assistant' ? '12px' : '0',
                                borderTopRightRadius: message.role === 'user' ? '12px' : '0'
                            }}>
                                <p style={{ margin: 0, fontSize: '13px', lineHeight: 1.5 }}>
                                    {message.content}
                                </p>
                                <p style={{ 
                                    margin: '8px 0 0', 
                                    fontSize: '10px', 
                                    opacity: 0.7,
                                    textAlign: 'right'
                                }}>
                                    {new Date(message.timestamp).toLocaleTimeString()}
                                </p>

                                {/* Tool Execution Results */}
                                {message.toolExecuted && message.toolExecuted.length > 0 && (
                                    <div style={{
                                        marginTop: '12px',
                                        paddingTop: '12px',
                                        borderTop: '1px solid var(--border-color)',
                                        display: 'flex',
                                        flexDirection: 'column',
                                        gap: '8px'
                                    }}>
                                        <p style={{ fontSize: '11px', fontWeight: 600, opacity: 0.8 }}>
                                            Tools Executed:
                                        </p>
                                        {message.toolExecuted.map((tool, idx) => (
                                            <div key={idx} style={{
                                                display: 'flex',
                                                alignItems: 'center',
                                                gap: '8px',
                                                padding: '8px',
                                                background: 'var(--bg-secondary)',
                                                borderRadius: '6px',
                                                fontSize: '11px'
                                            }}>
                                                <Wrench size={12} style={{ flexShrink: 0 }} />
                                                <span style={{ fontWeight: 600 }}>{tool.name}</span>
                                                {tool.result.success ? (
                                                    <CheckCircle size={12} style={{ color: '#10b981', marginLeft: 'auto' }} />
                                                ) : (
                                                    <XCircle size={12} style={{ color: '#ef4444', marginLeft: 'auto' }} />
                                                )}
                                            </div>
                                        ))}
                                    </div>
                                )}

                                {message.error && (
                                    <div style={{
                                        marginTop: '12px',
                                        padding: '8px',
                                        background: 'rgba(239, 68, 68, 0.1)',
                                        borderRadius: '6px',
                                        color: '#ef4444',
                                        fontSize: '11px'
                                    }}>
                                        ⚠️ Error occurred
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}

                    {isLoading && (
                        <div style={{
                            display: 'flex',
                            gap: '12px',
                            alignItems: 'center',
                            padding: '12px',
                            color: 'var(--text-muted)',
                            fontSize: '12px'
                        }}>
                            <Loader size={16} className="spin" />
                            <span>Assistant is thinking...</span>
                        </div>
                    )}

                    {toolCalls.length > 0 && (
                        <div style={{
                            display: 'flex',
                            flexDirection: 'column',
                            gap: '8px',
                            padding: '12px',
                            background: 'var(--bg-tertiary)',
                            borderRadius: '8px',
                            fontSize: '11px'
                        }}>
                            <p style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '6px' }}>
                                <Wrench size={12} />
                                Executing Tools:
                            </p>
                            {toolCalls.map((tool, idx) => (
                                <div key={idx} style={{
                                    padding: '6px 10px',
                                    background: 'var(--bg-secondary)',
                                    borderRadius: '4px',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '8px'
                                }}>
                                    <Loader size={12} className="spin" />
                                    <span style={{ fontWeight: 600 }}>{tool.name}</span>
                                    <span style={{ opacity: 0.7 }}>({Object.keys(tool.arguments || {}).join(', ')})</span>
                                </div>
                            ))}
                        </div>
                    )}

                    <div ref={messagesEndRef} />
                </div>

                {/* Input Area */}
                <div style={{
                    padding: '16px',
                    borderTop: '1px solid var(--border-color)',
                    background: 'var(--bg-secondary)'
                }}>
                    <div style={{
                        display: 'flex',
                        gap: '12px',
                        alignItems: 'flex-end'
                    }}>
                        <textarea
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            onKeyPress={handleKeyPress}
                            placeholder="Ask me to create a user, manage roles, or configure the system..."
                            rows={2}
                            style={{
                                flex: 1,
                                padding: '12px',
                                borderRadius: '8px',
                                border: '1px solid var(--border-color)',
                                background: 'var(--bg-tertiary)',
                                color: 'var(--text-primary)',
                                fontSize: '13px',
                                resize: 'none',
                                fontFamily: 'inherit'
                            }}
                        />
                        <button
                            onClick={handleSend}
                            disabled={isLoading || !input.trim()}
                            style={{
                                padding: '12px 20px',
                                borderRadius: '8px',
                                border: 'none',
                                background: isLoading || !input.trim() 
                                    ? 'var(--border-color)' 
                                    : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                                color: 'white',
                                fontWeight: 600,
                                fontSize: '13px',
                                cursor: isLoading || !input.trim() ? 'not-allowed' : 'pointer',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '8px',
                                transition: 'all 0.2s'
                            }}
                        >
                            <Send size={16} />
                            Send
                        </button>
                    </div>
                    <p style={{
                        margin: '8px 0 0',
                        fontSize: '10px',
                        color: 'var(--text-muted)',
                        textAlign: 'center'
                    }}>
                        Try: "Create a user with login name john and name John Doe" or "List all users"
                    </p>
                </div>
            </div>

            <style>{`
                @keyframes spin {
                    to { transform: rotate(360deg); }
                }
                .spin {
                    animation: spin 1s linear infinite;
                }
                @keyframes fadeIn {
                    from {
                        opacity: 0;
                        transform: translateY(10px);
                    }
                    to {
                        opacity: 1;
                        transform: translateY(0);
                    }
                }
            `}</style>
        </div>
    );
};

export default Chat;
