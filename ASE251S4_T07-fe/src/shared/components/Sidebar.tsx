import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  FlaskConical,
  Users,
  Package,
  ShoppingCart,
  CreditCard,
  CalendarDays,
  Sprout,
} from 'lucide-react';

export const Sidebar: React.FC = () => {
  const navItems = [
    { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/products', label: 'Fórmulas y Productos', icon: FlaskConical },
    { to: '/customers', label: 'Clientes y Parcelas', icon: Users },
    { to: '/supplies', label: 'Insumos y Stock', icon: Package },
    { to: '/sales', label: 'Órdenes de Venta', icon: ShoppingCart },
    { to: '/collections', label: 'Cobranzas y Cuotas', icon: CreditCard },
    { to: '/meetings', label: 'Visitas de Campo', icon: CalendarDays },
  ];

  return (
    <aside className="app-sidebar">
      <div className="sidebar-brand">
        <div className="brand-icon-wrapper">
          <Sprout size={24} className="brand-icon" />
        </div>
        <div className="brand-text">
          <span className="brand-title">AgroTecno</span>
          <span className="brand-subtitle">Sistema Agrícola 4.0</span>
        </div>
      </div>

      <nav className="sidebar-nav">
        <div className="nav-group-label">MÓDULOS PRINCIPALES</div>
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `nav-item ${isActive ? 'nav-item-active' : ''}`
              }
            >
              <Icon size={20} className="nav-icon" />
              <span className="nav-label">{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      <div className="sidebar-footer">
        <div className="backend-status-pill">
          <span className="status-dot status-online"></span>
          <span className="status-text">API 8082 Conectada</span>
        </div>
      </div>
    </aside>
  );
};
