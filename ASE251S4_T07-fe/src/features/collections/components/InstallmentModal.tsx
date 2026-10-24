import React, { useState, useEffect } from 'react';
import { PaymentCollection, Installment } from '../types/collection.types';
import { collectionsService } from '../services/collectionsService';
import { Modal } from '../../../shared/components/Modal';
import { Button } from '../../../shared/components/Button';
import { Badge } from '../../../shared/components/Badge';
import { formatCurrency, formatDate } from '../../../shared/utils/formatDate';
import { CheckCircle2 } from 'lucide-react';

interface InstallmentModalProps {
  collection: PaymentCollection | null;
  onClose: () => void;
}

export const InstallmentModal: React.FC<InstallmentModalProps> = ({
  collection,
  onClose,
}) => {
  const [installments, setInstallments] = useState<Installment[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [payingId, setPayingId] = useState<number | null>(null);

  const fetchInstallments = async () => {
    if (!collection) return;
    setIsLoading(true);
    try {
      const data = await collectionsService.getInstallmentsByCollection(collection.collectionId);
      setInstallments(data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchInstallments();
  }, [collection]);

  const handlePayInstallment = async (inst: Installment) => {
    if (!inst.installmentId) return;
    setPayingId(inst.installmentId);
    try {
      await collectionsService.updateInstallment(inst.installmentId, {
        amountPaid: inst.amountDue,
        paymentType: 'EFECTIVO',
        paymentDate: new Date().toISOString().split('T')[0],
        status: 'A',
        observation: 'Pago procesado desde plataforma web',
      });
      await fetchInstallments();
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : 'Error al registrar pago de cuota');
    } finally {
      setPayingId(null);
    }
  };

  if (!collection) return null;

  return (
    <Modal
      isOpen={!!collection}
      onClose={onClose}
      title={`Cronograma de Pagos: Cobranza COB-${collection.collectionId} (Orden #${collection.orderId})`}
      maxWidth="800px"
      footer={<Button onClick={onClose} variant="primary">Cerrar</Button>}
    >
      <div className="detail-modal-content">
        <div className="detail-summary-grid">
          <div>
            <span className="summary-label">Cliente:</span>
            <strong>{collection.mongoCustomerId}</strong>
          </div>
          <div>
            <span className="summary-label">Monto Total:</span>
            <strong className="text-success font-bold">{formatCurrency(collection.totalAmount)}</strong>
          </div>
          <div>
            <span className="summary-label">Total Cuotas:</span>
            <span>{collection.installmentsCount}</span>
          </div>
          <div>
            <span className="summary-label">Estado General:</span>
            <Badge status={collection.paymentStatus} />
          </div>
        </div>

        <h4 className="section-heading" style={{ marginTop: '20px' }}>
          Cuotas Programadas (SQL Server)
        </h4>

        {isLoading ? (
          <div className="loading-container">
            <div className="spinner" />
            <span>Cargando cuotas...</span>
          </div>
        ) : (
          <table className="data-table" style={{ marginTop: '8px' }}>
            <thead>
              <tr>
                <th>CUOTA #</th>
                <th>VENCIMIENTO</th>
                <th>MONTO CUOTA</th>
                <th>MONTO PAGADO</th>
                <th>MEDIO PAGO</th>
                <th>ESTADO</th>
                <th>ACCIÓN</th>
              </tr>
            </thead>
            <tbody>
              {installments.map((inst) => {
                const isPaid = inst.status === 'A';
                return (
                  <tr key={inst.installmentId || inst.installmentNum} className="table-row">
                    <td className="cell-code">Cuota {inst.installmentNum}</td>
                    <td>{formatDate(inst.dueDate)}</td>
                    <td className="font-semibold">{formatCurrency(inst.amountDue)}</td>
                    <td className="text-success">{formatCurrency(inst.amountPaid || 0)}</td>
                    <td>{inst.paymentType || '—'}</td>
                    <td>
                      <Badge
                        status={inst.status}
                        label={isPaid ? 'Pagada' : 'Pendiente'}
                      />
                    </td>
                    <td>
                      {!isPaid && inst.installmentId ? (
                        <button
                          className="btn btn-success btn-sm"
                          disabled={payingId === inst.installmentId}
                          onClick={() => handlePayInstallment(inst)}
                        >
                          <CheckCircle2 size={14} />
                          <span>Pagar Cuota</span>
                        </button>
                      ) : (
                        <span className="text-muted text-sm">Completada</span>
                      )}
                    </td>
                  </tr>
                );
              })}
              {installments.length === 0 && (
                <tr>
                  <td colSpan={7} className="table-empty-cell">
                    No hay cuotas registradas para esta cobranza.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </Modal>
  );
};
