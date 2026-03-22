import { useState, useEffect, useRef } from 'react';
import { MessageSquare, X, Send, Bot, User, Wrench, CheckCircle, XCircle, Loader } from 'lucide-react';

const FloatingChat = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState(() => {
        // Check for existing session - clear chat for new session
        const sessionId = sessionStorage.getItem('chatSessionId');
        const currentSessionId = Date.now().toString();
        sessionStorage.setItem('chatSessionId', currentSessionId);
        
        // If it's a new session (no previous session ID or different session), clear history
        if (!sessionId || sessionId !== currentSessionId) {
            sessionStorage.removeItem('chatHistory');
            return [{
                id: 1,
                role: 'assistant',
                content: 'Hello! I\'m your Vantage Admin assistant. I can help you manage users, roles, menus, and other system configurations. What would you like to do today?',
                timestamp: new Date().toISOString()
            }];
        }
        
        // Load from session storage for existing session
        const saved = sessionStorage.getItem('chatHistory');
        if (saved) {
            try {
                return JSON.parse(saved);
            } catch (e) {
                console.error('Failed to load chat history:', e);
            }
        }
        return [{
            id: 1,
            role: 'assistant',
            content: 'Hello! I\'m your Vantage Admin assistant. I can help you manage users, roles, menus, and other system configurations. What would you like to do today?',
            timestamp: new Date().toISOString()
        }];
    });
    const [input, setInput] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [toolCalls, setToolCalls] = useState([]);
    const [isServerDown, setIsServerDown] = useState(false);
    const [loadingStartTime, setLoadingStartTime] = useState(null);
    const messagesEndRef = useRef(null);

    // Save to session storage whenever messages change
    useEffect(() => {
        sessionStorage.setItem('chatHistory', JSON.stringify(messages));
    }, [messages]);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    useEffect(() => {
        if (isOpen) {
            scrollToBottom();
        }
    }, [messages, isOpen]);

    // Update loading time every second
    useEffect(() => {
        let interval;
        if (isLoading) {
            setLoadingStartTime(Date.now());
            interval = setInterval(() => {
                // Force re-render to update time display
                setLoadingStartTime(prev => prev);
            }, 1000);
        }
        return () => clearInterval(interval);
    }, [isLoading]);

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
        setIsServerDown(false);

        try {
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

            if (!response.ok) {
                throw new Error('Server responded with ' + response.status);
            }

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
                    content: chatData.response || 'How can I help you with the Vantage Admin system?',
                    timestamp: new Date().toISOString()
                };
                setMessages(prev => [...prev, assistantMessage]);
            }
        } catch (error) {
            console.error('Chat error:', error);
            setIsServerDown(true);
            const errorMessage = {
                id: messages.length + 2,
                role: 'assistant',
                content: 'Sorry, I\'m having trouble connecting to the server. Please make sure the application is running and try again.',
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

    const clearHistory = () => {
        setMessages([{
            id: 1,
            role: 'assistant',
            content: 'Chat history cleared. How can I help you?',
            timestamp: new Date().toISOString()
        }]);
        sessionStorage.removeItem('chatHistory');
    };

    return (
        <>
            {/* Chat Window */}
            {isOpen && (
                <div style={{
                    position: 'fixed',
                    bottom: '100px',
                    right: '32px',
                    width: '400px',
                    height: '450px',
                    background: 'var(--bg-secondary)',
                    borderRadius: '16px',
                    boxShadow: '0 8px 40px rgba(0, 0, 0, 0.15)',
                    zIndex: 999,
                    display: 'flex',
                    flexDirection: 'column',
                    overflow: 'hidden',
                    border: '1px solid var(--border-color)',
                    animation: 'slideIn 0.3s ease'
                }}>
                    {/* Header */}
                    <div style={{
                        padding: '16px',
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        color: 'white',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between'
                    }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <Bot size={20} />
                            <div>
                                <h3 style={{ margin: 0, fontSize: '14px', fontWeight: 600 }}>AI Assistant</h3>
                                <p style={{ margin: 0, fontSize: '11px', opacity: 0.8 }}>Always here to help</p>
                            </div>
                        </div>
                        <button
                            onClick={clearHistory}
                            style={{
                                background: 'rgba(255,255,255,0.2)',
                                border: 'none',
                                color: 'white',
                                padding: '6px 10px',
                                borderRadius: '6px',
                                cursor: 'pointer',
                                fontSize: '11px',
                                fontWeight: 600
                            }}
                            title="Clear chat history"
                        >
                            Clear
                        </button>
                    </div>

                    {/* Messages */}
                    <div style={{
                        flex: 1,
                        overflowY: 'auto',
                        padding: '16px',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: '12px'
                    }}>
                        {messages.map((message) => (
                            <div
                                key={message.id}
                                style={{
                                    display: 'flex',
                                    gap: '8px',
                                    alignItems: 'flex-start',
                                    animation: 'fadeIn 0.3s ease'
                                }}
                            >
                                <div style={{
                                    width: '28px',
                                    height: '28px',
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
                                    {message.role === 'assistant' ? <Bot size={14} /> : <User size={14} />}
                                </div>
                                
                                <div style={{
                                    flex: 1,
                                    background: message.role === 'assistant' 
                                        ? 'var(--bg-tertiary)' 
                                        : 'var(--primary-color)',
                                    color: message.role === 'assistant' 
                                        ? 'var(--text-primary)' 
                                        : 'white',
                                    padding: '10px 14px',
                                    borderRadius: '12px',
                                    borderTopLeftRadius: message.role === 'assistant' ? '12px' : '0',
                                    borderTopRightRadius: message.role === 'user' ? '12px' : '0',
                                    fontSize: '12px',
                                    lineHeight: 1.4
                                }}>
                                    <p style={{ margin: 0, whiteSpace: 'pre-wrap' }}>{message.content}</p>
                                    <p style={{ 
                                        margin: '6px 0 0', 
                                        fontSize: '9px', 
                                        opacity: 0.6,
                                        textAlign: 'right'
                                    }}>
                                        {new Date(message.timestamp).toLocaleTimeString()}
                                    </p>

                                    {message.toolExecuted && message.toolExecuted.length > 0 && (
                                        <div style={{
                                            marginTop: '8px',
                                            paddingTop: '8px',
                                            borderTop: '1px solid var(--border-color)',
                                            display: 'flex',
                                            flexDirection: 'column',
                                            gap: '4px'
                                        }}>
                                            {message.toolExecuted.map((tool, idx) => (
                                                <div key={idx} style={{
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    gap: '6px',
                                                    padding: '4px 8px',
                                                    background: 'var(--bg-secondary)',
                                                    borderRadius: '4px',
                                                    fontSize: '10px'
                                                }}>
                                                    <Wrench size={10} />
                                                    <span style={{ fontWeight: 600 }}>{tool.name}</span>
                                                    {tool.result.success ? (
                                                        <CheckCircle size={10} style={{ color: '#10b981', marginLeft: 'auto' }} />
                                                    ) : (
                                                        <XCircle size={10} style={{ color: '#ef4444', marginLeft: 'auto' }} />
                                                    )}
                                                </div>
                                            ))}
                                        </div>
                                    )}

                                    {message.error && (
                                        <div style={{
                                            marginTop: '8px',
                                            padding: '6px',
                                            background: 'rgba(239, 68, 68, 0.1)',
                                            borderRadius: '4px',
                                            color: '#ef4444',
                                            fontSize: '10px'
                                        }}>
                                            ⚠️ Connection error
                                        </div>
                                    )}
                                </div>
                            </div>
                        ))}

                        {isLoading && (
                            <div style={{
                                display: 'flex',
                                gap: '8px',
                                alignItems: 'center',
                                padding: '8px',
                                color: 'var(--text-muted)',
                                fontSize: '11px'
                            }}>
                                <Loader size={14} className="spin" />
                                <span>
                                    Thinking... {loadingStartTime ? `${Math.floor((Date.now() - loadingStartTime) / 1000)}s` : ''}
                                    {Math.floor((Date.now() - loadingStartTime) / 1000) > 10 ? ' (AI is warming up)' : ''}
                                </span>
                            </div>
                        )}

                        {isServerDown && (
                            <div style={{
                                padding: '10px',
                                background: 'rgba(239, 68, 68, 0.1)',
                                borderRadius: '8px',
                                color: '#ef4444',
                                fontSize: '11px',
                                textAlign: 'center'
                            }}>
                                ⚠️ Server appears to be down. Please restart the application.
                            </div>
                        )}

                        {toolCalls.length > 0 && (
                            <div style={{
                                padding: '10px',
                                background: 'var(--bg-tertiary)',
                                borderRadius: '8px',
                                fontSize: '10px'
                            }}>
                                <p style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '4px', margin: '0 0 6px' }}>
                                    <Wrench size={10} />
                                    Executing:
                                </p>
                                {toolCalls.map((tool, idx) => (
                                    <div key={idx} style={{
                                        padding: '4px 8px',
                                        background: 'var(--bg-secondary)',
                                        borderRadius: '4px',
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '6px',
                                        marginTop: '4px'
                                    }}>
                                        <Loader size={10} className="spin" />
                                        <span style={{ fontWeight: 600 }}>{tool.name}</span>
                                    </div>
                                ))}
                            </div>
                        )}

                        <div ref={messagesEndRef} />
                    </div>

                    {/* Input */}
                    <div style={{
                        padding: '12px',
                        borderTop: '1px solid var(--border-color)',
                        background: 'var(--bg-secondary)'
                    }}>
                        <div style={{
                            display: 'flex',
                            gap: '8px',
                            alignItems: 'flex-end'
                        }}>
                            <textarea
                                value={input}
                                onChange={(e) => setInput(e.target.value)}
                                onKeyPress={handleKeyPress}
                                placeholder="Ask me anything..."
                                rows={2}
                                style={{
                                    flex: 1,
                                    padding: '8px 12px',
                                    borderRadius: '8px',
                                    border: '1px solid var(--border-color)',
                                    background: 'var(--bg-tertiary)',
                                    color: 'var(--text-primary)',
                                    fontSize: '12px',
                                    resize: 'none',
                                    fontFamily: 'inherit'
                                }}
                            />
                            <button
                                onClick={handleSend}
                                disabled={isLoading || !input.trim()}
                                style={{
                                    padding: '8px 14px',
                                    borderRadius: '8px',
                                    border: 'none',
                                    background: isLoading || !input.trim() 
                                        ? 'var(--border-color)' 
                                        : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                                    color: 'white',
                                    fontWeight: 600,
                                    fontSize: '12px',
                                    cursor: isLoading || !input.trim() ? 'not-allowed' : 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '4px'
                                }}
                            >
                                <Send size={14} />
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Floating Button */}
            <button
                onClick={() => setIsOpen(!isOpen)}
                style={{
                    position: 'fixed',
                    bottom: '24px',
                    right: '24px',
                    width: '56px',
                    height: '56px',
                    borderRadius: '50%',
                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    color: 'white',
                    border: 'none',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    boxShadow: '0 4px 20px rgba(102, 126, 234, 0.4)',
                    zIndex: 1000,
                    transition: 'all 0.3s ease'
                }}
                title="AI Chat Assistant"
            >
                {isOpen ? <X size={24} /> : <MessageSquare size={24} />}
            </button>

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
                @keyframes slideIn {
                    from {
                        opacity: 0;
                        transform: translateY(20px);
                    }
                    to {
                        opacity: 1;
                        transform: translateY(0);
                    }
                }
            `}</style>
        </>
    );
};

export default FloatingChat;
