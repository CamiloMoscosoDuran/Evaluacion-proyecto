import React, { useState, useEffect } from 'react';
import { SalesOrderRequest, SalesOrderResponse, OrderDetail } from '../types/salesOrder.types';
import { Button } from '../../../shared/components/Button';
import { Plus, Trash2 } from 'lucide-react';
import { formatCurrency } from '../../../shared/utils/formatDate';

interface SalesOrderFormProps {
  initialData?: SalesOrderResponse | null;
  onSubmit: (data: SalesOrderRequest) => Promise<void>;
  onCancel: () => void;
  isSaving: boolean;
}

export const SalesOrderForm: React.FC<SalesOrderFormProps> = ({
  initialData,
  onSubmit,
  onCancel,
  isSaving,
}) => {
  const [mongoCustomerId, setMongoCustomerId] = useState('');
  const [entryDate, setEntryDate] = useState(() => new Date().toISOString().slice(0, 16));
  const [orderStatus, setOrderStatus] = useState('P');
  const [installmentsCount, setInstallmentsCount] = useState<number>(3);
  const [orderDetails, setOrderDetails] = useState<OrderDetail[]>([
    { mongoFormulaId: 'FOR001', quantity: 10, unitPrice: 45.0, subtotal: 450.0 },
  ]);

  useEffect(() => {
    if (initialData) {
      setMongoCustomerId(initialData.mongoCustomerId || '');
      setEntryDate(
        initialData.entryDate
          ? new Date(initialData.entryDate).toISOString().slice(0, 16)
          : new Date().toISOString().slice(0, 16)
      );
      setOrderStatus(initialData.orderStatus || 'P');
      setInstallmentsCount(initialData.paymentCollection?.installmentsCount || 3);
      if (initialData.orderDetails && initialData.orderDetails.length > 0) {
        setOrderDetails(initialData.orderDetails);
      }
    }
  }, [initialData]);

  const addDetail = () => {
    setOrderDetails([
      ...orderDetails,
      { mongoFormulaId: 'FOR001', quantity: 1, unitPrice: 40.0, subtotal: 40.0 },
    ]);
  };

  const removeDetail = (index: number) => {
    setOrderDetails(orderDetails.filter((_, i) => i !== index));
  };

  const updateDetail = (
    index: number,
    field: keyof OrderDetail,
    value: string | number
  ) => {
    const updated = [...orderDetails];
    const item = { ...updated[index], [field]: value };
    if (field === 'quantity' || field === 'unitPrice') {
      item.subtotal = Number(item.quantity) * Number(item.unitPrice);
    }
    updated[index] = item;
    setOrderDetails(updated);
  };

  const computedTotal = orderDetails.reduce(
    (acc, curr) => acc + (Number(curr.quantity) * Number(curr.unitPrice) || 0),
    0
  );

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (orderDetails.length === 0) {
      alert('Debe agregar al menos un detalle de producto a la orden');
      return;
    }

    const payload: SalesOrderRequest = {
      mongoCustomerId,
      entryDate: new Date(entryDate).toISOString(),
      orderStatus,
      installments_count: Number(installmentsCount),
      orderDetails: orderDetails.map((od) => ({
        mongoFormulaId: od.mongoFormulaId,
        quantity: Number(od.quantity),
        unitPrice: Number(od.unitPrice),
      })),
    };

    await onSubmit(payload);
  };

  return (
    <form className="sales-order-form" onSubmit={handleSubmit}>
      <div className="form-grid-3">
        <div className="form-group">
          <label className="form-label">ID Cliente MongoDB (CUS###) *</label>
          <input
            type="text"
            className="form-input"
            placeholder="Ej. CUS001"
            value={mongoCustomerId}
            onChange={(e) => setMongoCustomerId(e.target.value)}
            required
          />
        </div>

        <div className="form-group">
          <label className="form-label">Fecha de Registro *</label>
          <input
            type="datetime-local"
            className="form-input"
            value={entryDate}
            onChange={(e) => setEntryDate(e.target.value)}
            required
          />
        </div>

        <div className="form-group">
          <label className="form-label">Cuotas de Pago Acordadas *</label>
          <select
            className="form-select"
            value={installmentsCount}
            onChange={(e) => setInstallmentsCount(Number(e.target.value))}
          >
            <option value="1">1 Cuota (Contado)</option>
            <option value="2">2 Cuotas</option>
            <option value="3">3 Cuotas</option>
            <option value="4">4 Cuotas</option>
            <option value="6">6 Cuotas</option>
            <option value="12">12 Cuotas</option>
          </select>
        </div>
      </div>

      <div className="form-group">
        <label className="form-label">Estado de la Orden</label>
        <select
          className="form-select"
          value={orderStatus}
          onChange={(e) => setOrderStatus(e.target.value)}
        >
          <option value="P">Pendiente (P)</option>
          <option value="C">Completado (C)</option>
          <option value="A">Anulado (A)</option>
        </select>
      </div>

      <div className="form-subcard">
        <div className="subcard-header">
          <div>
            <h4 className="subcard-title">Líneas de la Orden (orderDetails)</h4>
            <span className="subcard-subtitle">Fórmulas y cantidades adquiridas</span>
          </div>
          <Button
            type="button"
            variant="outline"
            size="sm"
            icon={<Plus size={14} />}
            onClick={addDetail}
          >
            Agregar Producto
          </Button>
        </div>

        <div className="subcard-body">
          {orderDetails.map((item, idx) => (
            <div key={idx} className="order-detail-row">
              <input
                type="text"
                className="form-input"
                placeholder="ID Fórmula (ej. FOR001)"
                value={item.mongoFormulaId}
                onChange={(e) => updateDetail(idx, 'mongoFormulaId', e.target.value)}
                required
              />
              <input
                type="number"
                className="form-input"
                style={{ width: '120px' }}
                placeholder="Cantidad"
                value={item.quantity}
                onChange={(e) => updateDetail(idx, 'quantity', Number(e.target.value))}
                min="1"
                required
              />
              <input
                type="number"
                step="0.01"
                className="form-input"
                style={{ width: '130px' }}
                placeholder="Precio Unit. S/"
                value={item.unitPrice}
                onChange={(e) => updateDetail(idx, 'unitPrice', Number(e.target.value))}
                min="0"
                required
              />
              <div className="detail-subtotal">
                {formatCurrency(Number(item.quantity) * Number(item.unitPrice))}
              </div>
              <button
                type="button"
                className="btn-icon-danger"
                onClick={() => removeDetail(idx)}
                title="Eliminar línea"
              >
                <Trash2 size={16} />
              </button>
            </div>
          ))}

          <div className="order-total-banner">
            <span>Total Calculado:</span>
            <strong>{formatCurrency(computedTotal)}</strong>
          </div>
        </div>
      </div>

      <div className="form-actions">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancelar
        </Button>
        <Button type="submit" variant="primary" isLoading={isSaving}>
          {initialData ? 'Actualizar Orden' : 'Emitir Orden de Venta'}
        </Button>
      </div>
    </form>
  );
};
