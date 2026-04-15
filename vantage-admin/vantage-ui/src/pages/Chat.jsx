import { useState, useRef, useEffect } from 'react';
import { Send, Bot, User, Wrench, CheckCircle, XCircle, Loader, Sparkles, RefreshCw, Database, Copy, Check, Trash2, History, ChevronLeft, ChevronRight, MessageSquare } from 'lucide-react';

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
    const [streamingContent, setStreamingContent] = useState('');
    const [toolCalls, setToolCalls] = useState([]);
    const [aiStatus, setAiStatus] = useState({ enabled: false, knowledgeCount: 0 });
    const [showKnowledge, setShowKnowledge] = useState(false);
    const [copiedId, setCopiedId] = useState(null);
    const [showHistory, setShowHistory] = useState(false);
    const [conversationHistory, setConversationHistory] = useState([]);
    const messagesEndRef = useRef(null);
    const streamingRef = useRef(null);

    useEffect(() => {
        fetchAiStatus();
    }, []);

    useEffect(() => {
        scrollToBottom();
    }, [messages, streamingContent]);

    const scrollToBottom = () => {
        if (messagesEndRef.current) {
            messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
        }
    };

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

    const fetchConversationHistory = async () => {
        try {
            const response = await fetch('/api/chat/history');
            const data = await response.json();
            if (data.code === 200) {
                setConversationHistory(data.data || []);
            }
        } catch (error) {
            console.error('Failed to fetch conversation history:', error);
        }
    };

    const clearMemory = async () => {
        if (confirm('Clear conversation history?')) {
            try {
                const response = await fetch('/api/chat/clear-memory', { method: 'POST' });
                const data = await response.json();
                if (data.code === 200) {
                    setMessages([{
                        id: 1,
                        role: 'assistant',
                        content: 'Conversation cleared. How can I help you?',
                        timestamp: new Date().toISOString()
                    }]);
                    setConversationHistory([]);
                }
            } catch (error) {
                console.error('Failed to clear memory:', error);
            }
        }
    };

    const copyToClipboard = (text, id) => {
        navigator.clipboard.writeText(text);
        setCopiedId(id);
        setTimeout(() => setCopiedId(null), 2000);
    };

    const renderMarkdown = (content) => {
        // Simple markdown-like rendering for code blocks, bold, lists
        const lines = content.split('\n');
        const elements = [];
        let inCodeBlock = false;
        let codeContent = [];
        let codeLanguage = '';

        lines.forEach((line, idx) => {
            // Code block detection
            if (line.trim().startsWith('```')) {
                if (!inCodeBlock) {
                    inCodeBlock = true;
                    codeLanguage = line.trim().slice(3).trim();
                    codeContent = [];
                } else {
                    inCodeBlock = false;
                    elements.push(
                        <div key={`code-${idx}`} style={{
                            background: '#1e1e2e',
                            borderRadius: '8px',
                            margin: '8px 0',
                            overflow: 'hidden'
                        }}>
                            <div style={{
                                padding: '6px 12px',
                                background: '#313244',
                                fontSize: '11px',
                                color: '#a6adc8',
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center'
                            }}>
                                <span>{codeLanguage || 'code'}</span>
                                <button
                                    onClick={() => copyToClipboard(codeContent.join('\n'), `code-${idx}`)}
                                    style={{
                                        background: 'none', border: 'none', color: '#a6adc8',
                                        cursor: 'pointer', fontSize: '11px', display: 'flex', alignItems: 'center', gap: '4px'
                                    }}
                                >
                                    {copiedId === `code-${idx}` ? <Check size={12} /> : <Copy size={12} />}
                                    {copiedId === `code-${idx}` ? 'Copied' : 'Copy'}
                                </button>
                            </div>
                            <pre style={{
                                margin: 0,
                                padding: '12px',
                                fontSize: '12px',
                                lineHeight: 1.5,
                                color: '#cdd6f4',
                                fontFamily: "'JetBrains Mono', 'Fira Code', 'Cascadia Code', monospace",
                                overflowX: 'auto',
                                whiteSpace: 'pre-wrap',
                                wordBreak: 'break-word'
                            }}>
                                {codeContent.join('\n')}
                            </pre>
                        </div>
                    );
                    codeContent = [];
                    codeLanguage = '';
                }
                return;
            }

            if (inCodeBlock) {
                codeContent.push(line);
                return;
            }

            // Inline rendering
            let rendered = line;

            // Bold: **text**
            rendered = rendered.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');

            // Bullet lists
            if (rendered.trim().startsWith('•') || rendered.trim().startsWith('-')) {
                elements.push(
                    <div key={idx} style={{ paddingLeft: '16px', margin: '2px 0' }} dangerouslySetInnerHTML={{ __html: rendered }} />
                );
            } else if (rendered.trim().match(/^\d+\./)) {
                elements.push(
                    <div key={idx} style={{ paddingLeft: '8px', margin: '4px 0', fontWeight: 500 }} dangerouslySetInnerHTML={{ __html: rendered }} />
                );
            } else if (rendered.trim()) {
                elements.push(
                    <p key={idx} style={{ margin: '0 0 4px' }} dangerouslySetInnerHTML={{ __html: rendered }} />
                );
            } else {
                elements.push(<br key={idx} />);
            }
        });

        return elements;
    };

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
        setStreamingContent('');
        setToolCalls([]);

        try {
            // Try streaming first
            if (aiStatus.enabled) {
                const eventSource = new EventSourcePolyfill('/api/chat/stream', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    payload: JSON.stringify({ message: input, conversationHistory: messages.slice(-20) })
                });

                eventSource.onmessage = (event) => {
                    setStreamingContent(prev => prev + event.data);
                };

                eventSource.onerror = () => {
                    eventSource.close();
                    // If streaming fails or completes, finalize
                    finalizeStreaming(userMessage);
                };
            } else {
                // Non-streaming fallback
                const response = await fetch('/api/chat', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ message: input, conversationHistory: messages.slice(-10) })
                });
                const data = await response.json();
                const chatData = data.code === 200 ? data.data : data;

                if (chatData.toolCalls && chatData.toolCalls.length > 0) {
                    setToolCalls(chatData.toolCalls);
                    const toolResults = [];
                    for (const toolCall of chatData.toolCalls) {
                        const result = await executeTool(toolCall);
                        toolResults.push({ name: toolCall.name, result });
                    }

                    const finalResponse = await fetch('/api/chat', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ message: input, toolResults, conversationHistory: messages.slice(-10) })
                    });
                    const finalData = await finalResponse.json();
                    const finalChatData = finalData.code === 200 ? finalData.data : finalData;

                    setMessages(prev => [...prev, {
                        id: prev.length + 1,
                        role: 'assistant',
                        content: finalChatData.response || 'Operation completed!',
                        timestamp: new Date().toISOString(),
                        toolExecuted: toolResults
                    }]);
                } else {
                    setMessages(prev => [...prev, {
                        id: prev.length + 1,
                        role: 'assistant',
                        content: chatData.response || 'How can I help you?',
                        timestamp: new Date().toISOString()
                    }]);
                }
                setIsLoading(false);
            }
        } catch (error) {
            console.error('Chat error:', error);
            setMessages(prev => [...prev, {
                id: prev.length + 1,
                role: 'assistant',
                content: 'Sorry, I encountered an error. Please try again.',
                timestamp: new Date().toISOString(),
                error: true
            }]);
            setIsLoading(false);
            setStreamingContent('');
        }
    };

    const finalizeStreaming = (userMessage) => {
        if (streamingContent) {
            setMessages(prev => [...prev, {
                id: prev.length + 1,
                role: 'assistant',
                content: streamingContent,
                timestamp: new Date().toISOString()
            }]);
            setStreamingContent('');
        }
        setIsLoading(false);
    };

    const executeTool = async (toolCall) => {
        const { name, arguments: args } = toolCall;
        try {
            let endpoint = '';
            let method = 'POST';

            switch (name) {
                case 'createUser': endpoint = '/api/system/user'; method = 'POST'; break;
                case 'updateUser': endpoint = '/api/system/user'; method = 'PUT'; break;
                case 'deleteUser': endpoint = `/api/system/user/${args.userId}`; method = 'DELETE'; break;
                case 'listUsers': endpoint = '/api/system/user/list'; method = 'GET'; break;
                case 'createRole': endpoint = '/api/system/role'; method = 'POST'; break;
                case 'listRoles': endpoint = '/api/system/role/list'; method = 'GET'; break;
                default: throw new Error(`Unknown tool: ${name}`);
            }

            const response = await fetch(endpoint, {
                method,
                headers: { 'Content-Type': 'application/json' },
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
        <div className="page-container" style={{ maxWidth: '1200px', margin: '0 auto', display: 'flex', gap: '16px', height: 'calc(100vh - 80px)' }}>
            {/* Main Chat Area */}
            <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                <div className="page-header">
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <div style={{
                            width: '32px', height: '32px', borderRadius: '8px',
                            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                            display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white'
                        }}>
                            <Bot size={16} />
                        </div>
                        <div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                <h2 style={{ fontSize: '16px', fontWeight: 700, margin: 0 }}>AI Assistant</h2>
                                {aiStatus.enabled ? (
                                    <span style={{
                                        display: 'inline-flex', alignItems: 'center', gap: '4px',
                                        padding: '2px 8px', borderRadius: '12px',
                                        background: 'rgba(16, 185, 129, 0.1)', color: '#10b981',
                                        fontSize: '10px', fontWeight: 600
                                    }}>
                                        <Sparkles size={10} /> AI Enhanced
                                    </span>
                                ) : (
                                    <span style={{
                                        display: 'inline-flex', alignItems: 'center', gap: '4px',
                                        padding: '2px 8px', borderRadius: '12px',
                                        background: 'rgba(107, 114, 128, 0.1)', color: '#6b7280',
                                        fontSize: '10px', fontWeight: 600
                                    }}>
                                        Rule-based Mode
                                    </span>
                                )}
                            </div>
                            <p style={{ fontSize: '11px', color: 'var(--text-muted)', margin: '2px 0 0' }}>
                                {aiStatus.enabled ? `Knowledge base: ${aiStatus.knowledgeCount} documents` : 'Chat with MCP tool integration'}
                            </p>
                        </div>
                    </div>
                    <div style={{ display: 'flex', gap: '8px' }}>
                        <button onClick={() => { setShowHistory(!showHistory); if (!showHistory) fetchConversationHistory(); }}
                            style={{ padding: '6px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', background: showHistory ? 'var(--primary-color)' : 'var(--bg-secondary)', color: showHistory ? 'white' : 'var(--text-primary)', fontSize: '11px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <History size={12} /> History
                        </button>
                        <button onClick={clearMemory}
                            style={{ padding: '6px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', background: 'var(--bg-secondary)', color: 'var(--text-primary)', fontSize: '11px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <Trash2 size={12} /> Clear
                        </button>
                        <button onClick={handleRefreshKnowledge}
                            style={{ padding: '6px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', background: 'var(--bg-secondary)', color: 'var(--text-primary)', fontSize: '11px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <RefreshCw size={12} /> Refresh KB
                        </button>
                        <button onClick={() => setShowKnowledge(!showKnowledge)}
                            style={{ padding: '6px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', background: showKnowledge ? 'var(--primary-color)' : 'var(--bg-secondary)', color: showKnowledge ? 'white' : 'var(--text-primary)', fontSize: '11px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <Database size={12} /> KB
                        </button>
                    </div>
                </div>

                {/* Messages */}
                <div style={{
                    flex: 1, overflowY: 'auto', padding: '20px',
                    display: 'flex', flexDirection: 'column', gap: '16px'
                }}>
                    {messages.map((message) => (
                        <div key={message.id} style={{ display: 'flex', gap: '12px', alignItems: 'flex-start', animation: 'fadeIn 0.3s ease' }}>
                            <div style={{
                                width: '32px', height: '32px', borderRadius: '50%', flexShrink: 0,
                                background: message.role === 'assistant' ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' : 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
                                display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white'
                            }}>
                                {message.role === 'assistant' ? <Bot size={16} /> : <User size={16} />}
                            </div>

                            <div style={{ flex: 1, maxWidth: '85%' }}>
                                <div style={{
                                    background: message.role === 'assistant' ? 'var(--bg-tertiary)' : 'var(--primary-color)',
                                    color: message.role === 'assistant' ? 'var(--text-primary)' : 'white',
                                    padding: '12px 16px', borderRadius: '12px',
                                    borderTopLeftRadius: message.role === 'assistant' ? '4px' : '12px',
                                    borderTopRightRadius: message.role === 'user' ? '4px' : '12px',
                                    position: 'relative'
                                }}>
                                    <div style={{ fontSize: '13px', lineHeight: 1.6 }}>
                                        {message.role === 'assistant' ? renderMarkdown(message.content) : message.content}
                                    </div>

                                    {/* Copy button */}
                                    {message.role === 'assistant' && message.content && (
                                        <button
                                            onClick={() => copyToClipboard(message.content, `msg-${message.id}`)}
                                            style={{
                                                position: 'absolute', top: '8px', right: '8px',
                                                background: 'rgba(0,0,0,0.1)', border: 'none', borderRadius: '4px',
                                                padding: '4px 6px', cursor: 'pointer', color: 'inherit', opacity: 0.6,
                                                display: 'flex', alignItems: 'center', gap: '3px', fontSize: '10px'
                                            }}
                                            title="Copy response"
                                        >
                                            {copiedId === `msg-${message.id}` ? <Check size={10} /> : <Copy size={10} />}
                                        </button>
                                    )}

                                    <p style={{ margin: '8px 0 0', fontSize: '10px', opacity: 0.7, textAlign: 'right' }}>
                                        {new Date(message.timestamp).toLocaleTimeString()}
                                    </p>

                                    {/* Tool execution results */}
                                    {message.toolExecuted && message.toolExecuted.length > 0 && (
                                        <div style={{ marginTop: '12px', paddingTop: '12px', borderTop: '1px solid var(--border-color)', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                                            {message.toolExecuted.map((tool, idx) => (
                                                <div key={idx} style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '8px', background: 'var(--bg-secondary)', borderRadius: '6px', fontSize: '11px' }}>
                                                    <Wrench size={12} />
                                                    <span style={{ fontWeight: 600 }}>{tool.name}</span>
                                                    {tool.result.success ? <CheckCircle size={12} style={{ color: '#10b98e', marginLeft: 'auto' }} /> : <XCircle size={12} style={{ color: '#ef4444', marginLeft: 'auto' }} />}
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                    ))}

                    {/* Streaming content */}
                    {streamingContent && (
                        <div style={{ display: 'flex', gap: '12px', alignItems: 'flex-start', animation: 'fadeIn 0.3s ease' }}>
                            <div style={{
                                width: '32px', height: '32px', borderRadius: '50%', flexShrink: 0,
                                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                                display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white'
                            }}>
                                <Bot size={16} />
                            </div>
                            <div style={{ flex: 1, maxWidth: '85%' }}>
                                <div style={{
                                    background: 'var(--bg-tertiary)', color: 'var(--text-primary)',
                                    padding: '12px 16px', borderRadius: '12px', borderTopLeftRadius: '4px'
                                }}>
                                    <div style={{ fontSize: '13px', lineHeight: 1.6 }}>
                                        {renderMarkdown(streamingContent)}
                                        <span className="streaming-cursor" style={{
                                            display: 'inline-block', width: '2px', height: '1em',
                                            background: 'var(--primary-color)', marginLeft: '2px',
                                            animation: 'blink 1s infinite'
                                        }} />
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

                    {isLoading && !streamingContent && (
                        <div style={{ display: 'flex', gap: '12px', alignItems: 'center', padding: '12px', color: 'var(--text-muted)', fontSize: '12px' }}>
                            <Loader size={16} className="spin" />
                            <span>Assistant is thinking...</span>
                        </div>
                    )}

                    {toolCalls.length > 0 && (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', padding: '12px', background: 'var(--bg-tertiary)', borderRadius: '8px', fontSize: '11px' }}>
                            {toolCalls.map((tool, idx) => (
                                <div key={idx} style={{ padding: '6px 10px', background: 'var(--bg-secondary)', borderRadius: '4px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                                    <Loader size={12} className="spin" />
                                    <span style={{ fontWeight: 600 }}>{tool.name}</span>
                                </div>
                            ))}
                        </div>
                    )}

                    <div ref={messagesEndRef} />
                </div>

                {/* Input */}
                <div style={{ padding: '16px', borderTop: '1px solid var(--border-color)', background: 'var(--bg-secondary)' }}>
                    <div style={{ display: 'flex', gap: '12px', alignItems: 'flex-end' }}>
                        <textarea
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            onKeyDown={handleKeyPress}
                            placeholder="Ask me anything... (Shift+Enter for new line)"
                            disabled={isLoading}
                            rows={1}
                            style={{
                                flex: 1,
                                padding: '10px 14px',
                                borderRadius: '12px',
                                border: '1px solid var(--border-color)',
                                background: 'var(--bg-primary)',
                                color: 'var(--text-primary)',
                                fontSize: '13px',
                                resize: 'none',
                                outline: 'none',
                                minHeight: '40px',
                                maxHeight: '120px'
                            }}
                        />
                        <button
                            onClick={handleSend}
                            disabled={isLoading || !input.trim()}
                            style={{
                                padding: '10px 16px',
                                borderRadius: '12px',
                                border: 'none',
                                background: input.trim() && !isLoading ? 'var(--primary-color)' : 'var(--bg-tertiary)',
                                color: input.trim() && !isLoading ? 'white' : 'var(--text-muted)',
                                cursor: input.trim() && !isLoading ? 'pointer' : 'not-allowed',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '6px'
                            }}
                        >
                            {isLoading ? <Loader size={16} className="spin" /> : <Send size={16} />}
                        </button>
                    </div>
                </div>
            </div>

            {/* History Sidebar */}
            {showHistory && (
                <div style={{
                    width: '280px',
                    background: 'var(--bg-secondary)',
                    borderRadius: '12px',
                    border: '1px solid var(--border-color)',
                    overflow: 'hidden',
                    display: 'flex',
                    flexDirection: 'column'
                }}>
                    <div style={{ padding: '12px', borderBottom: '1px solid var(--border-color)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <h3 style={{ fontSize: '13px', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <MessageSquare size={14} /> Conversation ({conversationHistory.length})
                        </h3>
                    </div>
                    <div style={{ flex: 1, overflowY: 'auto', padding: '12px' }}>
                        {conversationHistory.length === 0 ? (
                            <p style={{ fontSize: '12px', color: 'var(--text-muted)', textAlign: 'center' }}>No conversation history</p>
                        ) : (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                                {conversationHistory.map((msg, idx) => (
                                    <div key={idx} style={{
                                        padding: '8px',
                                        borderRadius: '8px',
                                        background: msg.role === 'user' ? 'var(--primary-color)' : 'var(--bg-tertiary)',
                                        color: msg.role === 'user' ? 'white' : 'var(--text-primary)',
                                        fontSize: '11px'
                                    }}>
                                        <div style={{ fontWeight: 600, fontSize: '10px', marginBottom: '4px', opacity: 0.7 }}>
                                            {msg.role === 'user' ? 'You' : 'Assistant'}
                                        </div>
                                        <div style={{
                                            maxHeight: '60px', overflow: 'hidden',
                                            textOverflow: 'ellipsis', lineHeight: 1.4
                                        }}>
                                            {msg.content?.substring(0, 150)}{msg.content?.length > 150 ? '...' : ''}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            )}

            {/* Knowledge Panel */}
            {showKnowledge && (
                <div className="glass-panel" style={{
                    position: 'fixed', bottom: '80px', left: '20px', right: showHistory ? '320px' : '20px',
                    padding: '16px', borderRadius: '12px', zIndex: 10
                }}>
                    <h3 style={{ fontSize: '14px', fontWeight: 600, marginBottom: '12px' }}>Knowledge Base</h3>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '12px' }}>
                        <div style={{ padding: '12px', background: 'var(--bg-secondary)', borderRadius: '8px', textAlign: 'center' }}>
                            <div style={{ fontSize: '24px', fontWeight: 700, color: 'var(--primary-color)' }}>{aiStatus.knowledgeCount}</div>
                            <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>Documents</div>
                        </div>
                        <div style={{ padding: '12px', background: 'var(--bg-secondary)', borderRadius: '8px', textAlign: 'center' }}>
                            <div style={{ fontSize: '24px', fontWeight: 700, color: '#10b981' }}>{aiStatus.enabled ? 'Active' : 'Inactive'}</div>
                            <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>AI Status</div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

// Simple EventSource polyfill that supports POST with payload
class EventSourcePolyfill {
    constructor(url, options) {
        this.onmessage = null;
        this.onerror = null;
        this.readyState = 0;

        fetch(url, {
            method: options.method,
            headers: options.headers,
            body: options.payload
        })
        .then(response => {
            const reader = response.body.getReader();
            const decoder = new TextDecoder();
            let buffer = '';

            const read = () => {
                reader.read().then(({ done, value }) => {
                    if (done) {
                        this.readyState = 2;
                        if (this.onerror) this.onerror();
                        return;
                    }
                    buffer += decoder.decode(value, { stream: true });
                    const lines = buffer.split('\n');
                    buffer = lines.pop() || '';

                    for (const line of lines) {
                        if (line.startsWith('data: ')) {
                            const data = line.slice(6);
                            if (this.onmessage) {
                                this.onmessage({ data });
                            }
                        }
                    }
                    read();
                }).catch(err => {
                    this.readyState = 2;
                    if (this.onerror) this.onerror(err);
                });
            };
            this.readyState = 1;
            read();
        })
        .catch(err => {
            this.readyState = 2;
            if (this.onerror) this.onerror(err);
        });
    }

    close() {
        this.readyState = 2;
    }
}

export default Chat;
