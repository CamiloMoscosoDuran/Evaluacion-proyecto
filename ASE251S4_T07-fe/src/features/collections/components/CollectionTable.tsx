import React from 'react';
import { PaymentCollection } from '../types/collection.types';
import { Badge } from '../../../shared/components/Badge';
import { formatCurrency, formatDateTime } from '../../../shared/utils/formatDate';
import { CreditCard } from 'lucide-react';

interface CollectionTableProps {
  collections: PaymentCollection[];
  onViewInstallments: (col: PaymentCollection) => void;
}

export const CollectionTable: React.FC<CollectionTableProps> = ({
  collections,
  onViewInstallments,
}) => {
  return (
    <div className="table-container">
      <table className="data-table">
        <thead>
          <tr>
            <th>COBRANZA #</th>
            <th>ORDEN #</th>
            <th>CLIENTE</th>
            <th>TOTAL MONTO</th>
            <th>N.° CUOTAS</th>
            <th>ESTADO PAGO</th>
            <th>FECHA REGISTRO</th>
            <th>GESTIÓN DE CUOTAS</th>
          </tr>
        </thead>
        <tbody>
          {collections.map((col) => (
            <tr key={col.collectionId} className="table-row">
              <td className="cell-code">
                <strong>COB-{col.collectionId}</strong>
              </td>
              <td>
                <span className="doc-tag">Orden #{col.orderId}</span>
              </td>
              <td className="cell-primary">
                <span className="table-item-title">{col.mongoCustomerId}</span>
              </td>
              <td className="font-bold text-success">
                {formatCurrency(col.totalAmount)}
              </td>
              <td>
                <span className="pill-count">{col.installmentsCount} cuotas</span>
              </td>
              <td>
                <Badge status={col.paymentStatus} />
              </td>
              <td>{formatDateTime(col.createdAt)}</td>
              <td>
                <button
                  className="btn btn-outline btn-sm"
                  onClick={() => onViewInstallments(col)}
                >
                  <CreditCard size={14} />
                  <span>Ver Cuotas</span>
                </button>
              </td>
            </tr>
          ))}
          {collections.length === 0 && (
            <tr>
              <td colSpan={8} className="table-empty-cell">
                No hay cobranzas registradas en el backend.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
};
