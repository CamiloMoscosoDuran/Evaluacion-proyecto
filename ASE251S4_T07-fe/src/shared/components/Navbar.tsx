import React from 'react';
import { Bell, LogOut, User as UserIcon } from 'lucide-react';
import { useAuth } from '../../features/auth/hooks/useAuth';

export const Navbar: React.FC = () => {
  const { user, logout } = useAuth();

  return (
    <header className="app-navbar">
      <div className="navbar-left">
        <span className="navbar-breadcrumb">Valle Grande • AgroTecno ERP</span>
      </div>

      <div className="navbar-right">
        <button className="navbar-icon-btn" title="Notificaciones">
          <Bell size={18} />
          <span className="notification-badge"></span>
        </button>

        <div className="user-profile-menu">
          <div className="user-avatar">
            <UserIcon size={18} />
          </div>
          <div className="user-info">
            <span className="user-name">{user?.username || 'Administrador'}</span>
            <span className="user-role">{user?.role || 'ADMIN'}</span>
          </div>
          <button className="btn-logout" onClick={logout} title="Cerrar sesión">
            <LogOut size={16} />
          </button>
        </div>
      </div>
    </header>
  );
};
