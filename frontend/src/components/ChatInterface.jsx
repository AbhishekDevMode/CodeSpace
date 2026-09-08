import React, { useState } from 'react';
import api from '../api/axios';
import { Send, Bot, User, Sparkles, HelpCircle, Code2, Loader2 } from 'lucide-react';

const ChatInterface = ({ project, selectedFileNode }) => {
  const [messages, setMessages] = useState([
    {
      sender: 'ai',
      text: `Hello! I am your AI documentation assistant. Ask me questions like:
- "What does this function do?"
- "Suggest improvements for this class"
- "Show me all dependencies of this file"`,
    },
  ]);

  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);

  const sendMessage = async (customMessage) => {
    const query = customMessage || input;
    if (!query.trim()) return;

    const userMsg = { sender: 'user', text: query };
    setMessages((prev) => [...prev, userMsg]);
    if (!customMessage) setInput('');
    setLoading(true);

    try {
      const response = await api.post('/api/ai/chat', {
        message: query,
        codeContext: selectedFileNode?.content || '',
        filePath: selectedFileNode?.path || '',
      });

      const reply = response.data.reply || response.data.response || 'No response generated.';
      setMessages((prev) => [...prev, { sender: 'ai', text: reply }]);
    } catch (err) {
      setMessages((prev) => [
        ...prev,
        {
          sender: 'ai',
          text: 'Error processing AI query: ' + (err.response?.data?.message || err.message),
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const quickPrompts = [
    { label: 'Explain this file', action: () => sendMessage('What does this function do?') },
    { label: 'Suggest improvements', action: () => sendMessage('Suggest improvements for this class') },
    { label: 'Show dependencies', action: () => sendMessage('Show me all dependencies of this file') },
  ];

  return (
    <div className="flex flex-col h-[550px] bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
      {/* Header */}
      <div className="px-6 py-4 bg-slate-950/80 border-b border-slate-800 flex items-center justify-between">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-blue-600/20 border border-blue-500/30 flex items-center justify-center text-blue-400">
            <Sparkles className="w-4 h-4" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-white">DevDocs AI Assistant</h3>
            <p className="text-xs text-slate-400">
              {selectedFileNode ? `Context: ${selectedFileNode.name}` : 'Context: Whole Project'}
            </p>
          </div>
        </div>
      </div>

      {/* Messages Scroll Area */}
      <div className="flex-1 p-6 overflow-y-auto space-y-4">
        {messages.map((msg, idx) => (
          <div
            key={idx}
            className={`flex items-start gap-3 ${msg.sender === 'user' ? 'flex-row-reverse' : 'flex-row'}`}
          >
            <div
              className={`w-8 h-8 rounded-lg flex items-center justify-center text-xs shrink-0 ${
                msg.sender === 'user'
                  ? 'bg-blue-600 text-white'
                  : 'bg-slate-800 text-blue-400 border border-slate-700'
              }`}
            >
              {msg.sender === 'user' ? <User className="w-4 h-4" /> : <Bot className="w-4 h-4" />}
            </div>

            <div
              className={`max-w-[80%] rounded-2xl px-4 py-3 text-xs leading-relaxed whitespace-pre-wrap ${
                msg.sender === 'user'
                  ? 'bg-blue-600 text-white rounded-tr-none'
                  : 'bg-slate-950/80 text-slate-200 border border-slate-800 rounded-tl-none font-mono'
              }`}
            >
              {msg.text}
            </div>
          </div>
        ))}

        {loading && (
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-slate-800 text-blue-400 border border-slate-700 flex items-center justify-center">
              <Bot className="w-4 h-4" />
            </div>
            <div className="bg-slate-950/80 border border-slate-800 rounded-2xl px-4 py-3 text-xs text-slate-400 flex items-center gap-2">
              <Loader2 className="w-4 h-4 animate-spin text-blue-500" />
              AI is analyzing code...
            </div>
          </div>
        )}
      </div>

      {/* Quick Prompts Bar */}
      <div className="px-6 py-2 bg-slate-950/40 border-t border-slate-800/60 flex items-center gap-2 overflow-x-auto">
        {quickPrompts.map((p, idx) => (
          <button
            key={idx}
            onClick={p.action}
            disabled={loading}
            className="text-[11px] font-medium bg-slate-800/60 hover:bg-blue-600/20 text-slate-300 hover:text-blue-400 border border-slate-700/50 hover:border-blue-500/30 px-3 py-1 rounded-full transition whitespace-nowrap"
          >
            {p.label}
          </button>
        ))}
      </div>

      {/* Input Box */}
      <div className="p-4 bg-slate-950 border-t border-slate-800">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            sendMessage();
          }}
          className="flex items-center gap-2"
        >
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask AI about functions, improvements, or dependencies..."
            className="flex-1 bg-slate-900 border border-slate-800 focus:border-blue-500 text-white rounded-xl px-4 py-2.5 text-xs placeholder:text-slate-500"
            disabled={loading}
          />
          <button
            type="submit"
            disabled={loading || !input.trim()}
            className="bg-blue-600 hover:bg-blue-500 disabled:opacity-50 text-white p-2.5 rounded-xl transition shadow-md shadow-blue-600/20"
          >
            <Send className="w-4 h-4" />
          </button>
        </form>
      </div>
    </div>
  );
};

export default ChatInterface;
