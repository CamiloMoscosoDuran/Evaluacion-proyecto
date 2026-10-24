import React, { useEffect, useState } from 'react';
import { productsService } from '../../products/services/productsService';
import { customersService } from '../../customers/services/customersService';
import { suppliesService } from '../../supplies/services/suppliesService';
import { salesOrdersService } from '../../sales/services/salesOrdersService';
import { collectionsService } from '../../collections/services/collectionsService';
import { formatCurrency } from '../../../shared/utils/formatDate';
import { Link } from 'react-router-dom';
import {
  FlaskConical,
  Users,
  Package,
  ShoppingCart,
  CreditCard,
  AlertTriangle,
  ArrowRight,
  Database,
  CheckCircle2,
} from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const [stats, setStats] = useState({
    totalFormulas: 0,
    totalCustomers: 0,
    totalSupplies: 0,
    lowStockCount: 0,
    totalSalesAmount: 0,
    totalOrders: 0,
    totalCollections: 0,
  });
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const loadDashboardData = async () => {
      try {
        const [formulas, customers, supplies, orders, collections] = await Promise.allSettled([
          productsService.getFormulas(),
          customersService.getCustomers(),
          suppliesService.getSupplies(),
          salesOrdersService.getOrders(),
          collectionsService.getCollections(),
        ]);

        const formulasData = formulas.status === 'fulfilled' && Array.isArray(formulas.value) ? formulas.value : [];
        const customersData = customers.status === 'fulfilled' && Array.isArray(customers.value) ? customers.value : [];
        const suppliesData = supplies.status === 'fulfilled' && Array.isArray(supplies.value) ? supplies.value : [];
        const ordersData = orders.status === 'fulfilled' && Array.isArray(orders.value) ? orders.value : [];
        const collectionsData = collections.status === 'fulfilled' && Array.isArray(collections.value) ? collections.value : [];

        const lowStock = suppliesData.filter((s) => {
          const current = s.inventory?.current_stock ?? 0;
          const min = s.inventory?.minimum_stock ?? 0;
          return current <= min;
        }).length;

        const totalSales = ordersData.reduce((acc, curr) => acc + (curr.totalOrder || 0), 0);

        setStats({
          totalFormulas: formulasData.length,
          totalCustomers: customersData.length,
          totalSupplies: suppliesData.length,
          lowStockCount: lowStock,
          totalSalesAmount: totalSales,
          totalOrders: ordersData.length,
          totalCollections: collectionsData.length,
        });
      } catch (err) {
        console.error('Error loading dashboard data:', err);
      } finally {
        setIsLoading(false);
      }
    };

    loadDashboardData();
  }, []);

  return (
    <div className="page-container">
      <div className="dashboard-hero">
        <div className="hero-content">
          <span className="hero-badge">Panel de Control AgroTecno</span>
          <h1 className="hero-title">Visión General de Operaciones Agrícolas</h1>
          <p className="hero-desc">
            Monitoreo en tiempo real sincronizado entre MongoDB (Catálogos y Visitas) y SQL Server (Ventas y Cobranzas)
          </p>
        </div>

        <div className="backend-sync-card">
          <div className="sync-row">
            <Database size={18} className="text-success" />
            <div>
              <span className="sync-title">Backend Spring Boot</span>
              <span className="sync-host">http://localhost:8082</span>
            </div>
            <CheckCircle2 size={18} className="text-success" />
          </div>
        </div>
      </div>

      <div className="stats-grid">
        <div className="stat-card stat-card-green">
          <div className="stat-icon-wrapper">
            <FlaskConical size={24} />
          </div>
          <div className="stat-info">
            <span className="stat-label">Fórmulas Registradas</span>
            <span className="stat-value">{isLoading ? '...' : stats.totalFormulas}</span>
            <span className="stat-meta">Recetas y Galones (MongoDB)</span>
          </div>
          <Link to="/products" className="stat-link" title="Ir a fórmulas">
            <ArrowRight size={16} />
          </Link>
        </div>

        <div className="stat-card stat-card-blue">
          <div className="stat-icon-wrapper">
            <Users size={24} />
          </div>
          <div className="stat-info">
            <span className="stat-label">Clientes y Productores</span>
            <span className="stat-value">{isLoading ? '...' : stats.totalCustomers}</span>
            <span className="stat-meta">Con parcelas y fundos activos</span>
          </div>
          <Link to="/customers" className="stat-link" title="Ir a clientes">
            <ArrowRight size={16} />
          </Link>
        </div>

        <div className="stat-card stat-card-purple">
          <div className="stat-icon-wrapper">
            <Package size={24} />
          </div>
          <div className="stat-info">
            <span className="stat-label">Insumos en Almacén</span>
            <span className="stat-value">{isLoading ? '...' : stats.totalSupplies}</span>
            <span className="stat-meta">
              {stats.lowStockCount > 0 ? (
                <span className="text-danger font-semibold">
                  {stats.lowStockCount} con stock bajo
                </span>
              ) : (
                'Stock en niveles óptimos'
              )}
            </span>
          </div>
          <Link to="/supplies" className="stat-link" title="Ir a insumos">
            <ArrowRight size={16} />
          </Link>
        </div>

        <div className="stat-card stat-card-emerald">
          <div className="stat-icon-wrapper">
            <ShoppingCart size={24} />
          </div>
          <div className="stat-info">
            <span className="stat-label">Ventas Totales</span>
            <span className="stat-value">
              {isLoading ? '...' : formatCurrency(stats.totalSalesAmount)}
            </span>
            <span className="stat-meta">{stats.totalOrders} órdenes registradas</span>
          </div>
          <Link to="/sales" className="stat-link" title="Ir a ventas">
            <ArrowRight size={16} />
          </Link>
        </div>
      </div>

      {stats.lowStockCount > 0 && (
        <div className="alert alert-warning" style={{ marginTop: '24px' }}>
          <AlertTriangle size={20} />
          <div>
            <strong>¡Atención en Almacén!</strong>
            <p>Se detectaron {stats.lowStockCount} insumos por debajo o cerca del stock mínimo de seguridad.</p>
          </div>
          <Link to="/supplies" className="btn btn-sm btn-outline" style={{ marginLeft: 'auto' }}>
            Revisar Insumos
          </Link>
        </div>
      )}

      <h3 className="section-heading" style={{ marginTop: '32px' }}>Acceso Rápido a Módulos</h3>
      <div className="quick-grid">
        <Link to="/products" className="quick-card">
          <div className="quick-header">
            <FlaskConical size={20} className="text-success" />
            <h4>Fórmulas y Recetas</h4>
          </div>
          <p>Registro maestro de fórmulas químicas, lotes estándar, costos y presentaciones envasadas.</p>
        </Link>

        <Link to="/customers" className="quick-card">
          <div className="quick-header">
            <Users size={20} className="text-blue" />
            <h4>Clientes y Parcelas</h4>
          </div>
          <p>Directorio de clientes, tipo de documento, hectáreas cultivadas y geolocalización.</p>
        </Link>

        <Link to="/supplies" className="quick-card">
          <div className="quick-header">
            <Package size={20} className="text-purple" />
            <h4>Insumos y Materias Primas</h4>
          </div>
          <p>Control de stock actual, mínimo, vencimientos y ubicación física en almacenes.</p>
        </Link>

        <Link to="/sales" className="quick-card">
          <div className="quick-header">
            <ShoppingCart size={20} className="text-emerald" />
            <h4>Órdenes de Venta</h4>
          </div>
          <p>Emisión de transacciones compuestas, detalle de productos y cálculo automático de subtotales.</p>
        </Link>

        <Link to="/collections" className="quick-card">
          <div className="quick-header">
            <CreditCard size={20} className="text-indigo" />
            <h4>Cobranzas y Cuotas</h4>
          </div>
          <p>Cronograma financiero de pagos a crédito, registro de amortizaciones y estados de deuda.</p>
        </Link>
      </div>
    </div>
  );
};
