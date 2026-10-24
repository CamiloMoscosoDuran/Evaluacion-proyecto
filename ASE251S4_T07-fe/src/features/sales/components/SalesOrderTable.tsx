import React from 'react';
import { SalesOrderResponse } from '../types/salesOrder.types';
import { Badge } from '../../../shared/components/Badge';
import { formatCurrency, formatDateTime } from '../../../shared/utils/formatDate';
import { Edit2, Eye } from 'lucide-react';

interface SalesOrderTableProps {
  orders: SalesOrderResponse[];
  onEdit: (order: SalesOrderResponse) => void;
  onView: (order: SalesOrderResponse) => void;
}

export const SalesOrderTable: React.FC<SalesOrderTableProps> = ({
  orders,
  onEdit,
  onView,
}) => {
  return (
    <div className="table-container">
      <table className="data-table">
        <thead>
          <tr>
            <th>ORDEN #</th>
            <th>GUÍA REMISIÓN</th>
            <th>CLIENTE</th>
            <th>TOTAL</th>
            <th>ESTADO</th>
            <th>FECHA REGISTRO</th>
            <th>PRODUCTOS</th>
            <th>ACCIONES</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((o) => {
            const clientName = o.customerInfo
              ? `${o.customerInfo.first_name} ${o.customerInfo.last_name}`
              : o.mongoCustomerId;
            const itemsCount = o.orderDetails ? o.orderDetails.length : 0;

            return (
              <tr key={o.orderId} className="table-row">
                <td className="cell-code">
                  <strong>#{o.orderId}</strong>
                </td>
                <td>
                  <span className="doc-tag">{o.referralGuide || 'GR-AUTO'}</span>
                </td>
                <td className="cell-primary">
                  <span className="table-item-title">{clientName}</span>
                  <span className="table-item-sub">{o.mongoCustomerId}</span>
                </td>
                <td className="font-bold text-success">
                  {formatCurrency(o.totalOrder)}
                </td>
                <td>
                  <Badge status={o.orderStatus} />
                </td>
                <td>{formatDateTime(o.entryDate)}</td>
                <td>
                  <span className="pill-count">{itemsCount} ítem(s)</span>
                </td>
                <td>
                  <div className="table-actions">
                    <button
                      className="btn-action-icon"
                      title="Ver detalles de orden"
                      onClick={() => onView(o)}
                    >
                      <Eye size={16} />
                    </button>
                    <button
                      className="btn-action-icon"
                      title="Editar Orden"
                      onClick={() => onEdit(o)}
                    >
                      <Edit2 size={16} />
                    </button>
                  </div>
                </td>
              </tr>
            );
          })}
          {orders.length === 0 && (
            <tr>
              <td colSpan={8} className="table-empty-cell">
                No hay órdenes de venta registradas en el backend.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
};
