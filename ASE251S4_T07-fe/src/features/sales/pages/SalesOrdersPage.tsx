import React, { useState } from 'react';
import { useSalesOrders } from '../hooks/useSalesOrders';
import { SalesOrderTable } from '../components/SalesOrderTable';
import { SalesOrderForm } from '../components/SalesOrderForm';
import { Modal } from '../../../shared/components/Modal';
import { Button } from '../../../shared/components/Button';
import { SalesOrderResponse, SalesOrderRequest } from '../types/salesOrder.types';
import { formatCurrency, formatDateTime } from '../../../shared/utils/formatDate';
import { Plus, RefreshCw, Search } from 'lucide-react';

export const SalesOrdersPage: React.FC = () => {
  const { orders, isLoading, error, reload, saveOrder } = useSalesOrders();
  const [searchTerm, setSearchTerm] = useState('');
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<SalesOrderResponse | null>(null);
  const [viewingOrder, setViewingOrder] = useState<SalesOrderResponse | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [actionMessage, setActionMessage] = useState<string | null>(null);

  const handleOpenNew = () => {
    setSelectedOrder(null);
    setIsFormOpen(true);
  };

  const handleEdit = (order: SalesOrderResponse) => {
    setSelectedOrder(order);
    setIsFormOpen(true);
  };

  const handleSubmit = async (data: SalesOrderRequest) => {
    setIsSaving(true);
    try {
      await saveOrder(data, selectedOrder?.orderId);
      setIsFormOpen(false);
      setActionMessage('Orden de venta procesada con éxito');
      setTimeout(() => setActionMessage(null), 3000);
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : 'Error al guardar la orden');
    } finally {
      setIsSaving(false);
    }
  };

  const filteredOrders = orders.filter((o) => {
    const term = searchTerm.toLowerCase();
    const id = String(o.orderId).toLowerCase();
    const customer = (o.mongoCustomerId || '').toLowerCase();
    const guide = (o.referralGuide || '').toLowerCase();
    return id.includes(term) || customer.includes(term) || guide.includes(term);
  });

  return (
    <div className="page-container">
      <div className="page-header-row">
        <div>
          <h1 className="page-heading">Órdenes de Venta</h1>
          <p className="page-subheading">
            Transacciones compuestas con detalles de productos y enlace a Cobranzas (SQL Server)
          </p>
        </div>

        <div className="page-header-actions">
          <Button
            variant="primary"
            icon={<Plus size={16} />}
            onClick={handleOpenNew}
          >
            Nueva Orden
          </Button>
        </div>
      </div>

      {actionMessage && (
        <div className="alert alert-success">{actionMessage}</div>
      )}

      {error && (
        <div className="alert alert-danger">
          <span>{error}</span>
          <Button size="sm" variant="ghost" onClick={reload}>Reintentar</Button>
        </div>
      )}

      <div className="filter-bar">
        <div className="search-input-wrapper">
          <Search size={18} className="search-icon" />
          <input
            type="text"
            className="form-input search-input"
            placeholder="Buscar por orden #, guía o cliente..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        <Button
          variant="ghost"
          size="sm"
          icon={<RefreshCw size={15} />}
          onClick={reload}
          title="Refrescar datos"
        />
      </div>

      {isLoading ? (
        <div className="loading-container">
          <div className="spinner" />
          <span>Cargando órdenes de venta desde SQL Server...</span>
        </div>
      ) : (
        <SalesOrderTable
          orders={filteredOrders}
          onEdit={handleEdit}
          onView={(o) => setViewingOrder(o)}
        />
      )}

      <Modal
        isOpen={isFormOpen}
        onClose={() => setIsFormOpen(false)}
        title={selectedOrder ? `Editar Orden #${selectedOrder.orderId}` : 'Crear Nueva Orden de Venta'}
        maxWidth="800px"
      >
        <SalesOrderForm
          initialData={selectedOrder}
          onSubmit={handleSubmit}
          onCancel={() => setIsFormOpen(false)}
          isSaving={isSaving}
        />
      </Modal>

      <Modal
        isOpen={!!viewingOrder}
        onClose={() => setViewingOrder(null)}
        title={`Detalle de Orden #${viewingOrder?.orderId || ''}`}
        maxWidth="700px"
        footer={<Button onClick={() => setViewingOrder(null)}>Cerrar</Button>}
      >
        {viewingOrder && (
          <div className="detail-modal-content">
            <div className="detail-summary-grid">
              <div>
                <span className="summary-label">Cliente:</span>
                <strong>{viewingOrder.mongoCustomerId}</strong>
              </div>
              <div>
                <span className="summary-label">Guía de Remisión:</span>
                <strong>{viewingOrder.referralGuide || 'GR-001'}</strong>
              </div>
              <div>
                <span className="summary-label">Fecha:</span>
                <span>{formatDateTime(viewingOrder.entryDate)}</span>
              </div>
              <div>
                <span className="summary-label">Monto Total:</span>
                <strong className="text-success font-bold">
                  {formatCurrency(viewingOrder.totalOrder)}
                </strong>
              </div>
            </div>

            <h4 className="section-heading" style={{ marginTop: '16px' }}>
              Productos en la Orden (orderDetails)
            </h4>
            <table className="data-table" style={{ marginTop: '8px' }}>
              <thead>
                <tr>
                  <th>FÓRMULA / PRODUCTO</th>
                  <th>CANTIDAD</th>
                  <th>PRECIO UNIT.</th>
                  <th>SUBTOTAL</th>
                </tr>
              </thead>
              <tbody>
                {viewingOrder.orderDetails && viewingOrder.orderDetails.length > 0 ? (
                  viewingOrder.orderDetails.map((item, idx) => (
                    <tr key={idx}>
                      <td className="cell-code"><strong>{item.mongoFormulaId}</strong></td>
                      <td>{item.quantity}</td>
                      <td>{formatCurrency(item.unitPrice)}</td>
                      <td className="font-semibold">
                        {formatCurrency(item.subtotal || item.quantity * item.unitPrice)}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={4} className="table-empty-cell">Sin productos asociados.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </Modal>
    </div>
  );
};
