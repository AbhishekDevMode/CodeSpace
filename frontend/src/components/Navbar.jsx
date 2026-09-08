import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Code2, LogOut, LayoutDashboard, User } from 'lucide-react';

const Navbar = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="bg-slate-900 border-b border-slate-800 text-white sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <Link to="/" className="flex items-center gap-2 font-bold text-xl text-blue-400">
            <Code2 className="w-7 h-7 text-blue-500" />
            <span>DevDocs <span className="text-xs bg-blue-500/20 text-blue-300 border border-blue-500/30 px-2 py-0.5 rounded-full font-normal">AI</span></span>
          </Link>

          {isAuthenticated ? (
            <div className="flex items-center gap-4">
              <Link
                to="/dashboard"
                className="flex items-center gap-1.5 text-sm font-medium text-slate-300 hover:text-white px-3 py-1.5 rounded-lg hover:bg-slate-800 transition"
              >
                <LayoutDashboard className="w-4 h-4" />
                Dashboard
              </Link>
              <div className="flex items-center gap-2 border-l border-slate-800 pl-4">
                <div className="w-8 h-8 rounded-full bg-blue-600/30 border border-blue-500/40 flex items-center justify-center text-blue-300 font-semibold text-xs">
                  {user?.email ? user.email.charAt(0).toUpperCase() : <User className="w-4 h-4" />}
                </div>
                <span className="text-sm font-medium text-slate-300 hidden sm:inline">{user?.email}</span>
                <button
                  onClick={handleLogout}
                  className="flex items-center gap-1 text-sm font-medium text-red-400 hover:text-red-300 hover:bg-red-500/10 px-3 py-1.5 rounded-lg transition ml-2"
                >
                  <LogOut className="w-4 h-4" />
                  Logout
                </button>
              </div>
            </div>
          ) : (
            <div className="flex items-center gap-3">
              <Link
                to="/login"
                className="text-sm font-medium text-slate-300 hover:text-white px-4 py-2 rounded-lg hover:bg-slate-800 transition"
              >
                Sign In
              </Link>
              <Link
                to="/signup"
                className="text-sm font-medium bg-blue-600 hover:bg-blue-500 text-white px-4 py-2 rounded-lg transition shadow-md shadow-blue-600/20"
              >
                Get Started
              </Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
