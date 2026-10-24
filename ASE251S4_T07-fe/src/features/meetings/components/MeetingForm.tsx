import React, { useState, useEffect, useRef } from 'react';
import { FieldMeeting } from '../types/meeting.types';
import { Customer } from '../../customers/types/customer.types';
import { SalesOrderResponse } from '../../sales/types/salesOrder.types';
import { customersService } from '../../customers/services/customersService';
import { salesOrdersService } from '../../sales/services/salesOrdersService';
import { Button } from '../../../shared/components/Button';
import { ChevronDown, X, Check, Sprout, ShoppingCart, Info, Edit3 } from 'lucide-react';

interface MeetingFormProps {
  initialData?: FieldMeeting | null;
  onSubmit: (data: Partial<FieldMeeting>) => Promise<void>;
  onCancel: () => void;
  isSaving: boolean;
}

export const MeetingForm: React.FC<MeetingFormProps> = ({
  initialData,
  onSubmit,
  onCancel,
  isSaving,
}) => {
  // Datos maestros
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [salesOrders, setSalesOrders] = useState<SalesOrderResponse[]>([]);
  const [isLoadingMasterData, setIsLoadingMasterData] = useState<boolean>(true);

  // Campos del formulario
  const [meetingDateTime, setMeetingDateTime] = useState<string>(() => new Date().toISOString().slice(0, 16));
  const [customerId, setCustomerId] = useState<string>('');
  const [customerSearch, setCustomerSearch] = useState<string>('');
  const [isCustomerDropdownOpen, setIsCustomerDropdownOpen] = useState<boolean>(false);

  const [farmName, setFarmName] = useState<string>('');
  const [isManualFarm, setIsManualFarm] = useState<boolean>(false);

  const [orderSalesId, setOrderSalesId] = useState<string>('');
  const [isManualOrder, setIsManualOrder] = useState<boolean>(false);

  const [fruitQuality, setFruitQuality] = useState<string>('Excelente calibre y coloración uniforme');
  const [observation, setObservation] = useState<string>('');
  const [visitStatus, setVisitStatus] = useState<string>('Pendiente');

  const [errors, setErrors] = useState<Record<string, string>>({});
  const comboboxRef = useRef<HTMLDivElement>(null);

  // Cargar clientes y órdenes de venta
  useEffect(() => {
    let isMounted = true;
    setIsLoadingMasterData(true);
    customersService
      .getCustomers()
      .then((custList) => {
        if (!isMounted) return;
        setCustomers(Array.isArray(custList) ? custList : []);
      })
      .catch((err) => {
        console.warn('Error cargando clientes para visitas:', err);
      })
      .finally(() => {
        if (isMounted) setIsLoadingMasterData(false);
      });

    salesOrdersService
      .getOrders()
      .then((orderList) => {
        if (!isMounted) return;
        setSalesOrders(Array.isArray(orderList) ? orderList : []);
      })
      .catch((err) => {
        console.warn('Error cargando órdenes de venta:', err);
      });

    return () => {
      isMounted = false;
    };
  }, []);

  // Precargar en edición
  useEffect(() => {
    if (initialData) {
      setMeetingDateTime(
        initialData.meeting_date_time
          ? new Date(initialData.meeting_date_time).toISOString().slice(0, 16)
          : new Date().toISOString().slice(0, 16)
      );
      const cId = initialData.customer_id || '';
      setCustomerId(cId);
      setFarmName(initialData.farm_name || '');
      setOrderSalesId(String(initialData.order_sales_id || ''));
      setFruitQuality(initialData.fruit_quality || '');
      setObservation(initialData.observation || '');
      setVisitStatus(initialData.visit_status || 'Pendiente');
    }
  }, [initialData]);

  // Sincronizar el texto del input de búsqueda con el cliente seleccionado
  useEffect(() => {
    if (customerId && customers.length > 0) {
      const found = customers.find(
        (c) => (c.id || c._id || '').trim().toUpperCase() === customerId.trim().toUpperCase()
      );
      if (found) {
        setCustomerSearch(`${found.id || found._id} - ${found.first_name} ${found.last_name}`);
      } else if (!customerSearch) {
        setCustomerSearch(customerId);
      }
    }
  }, [customerId, customers]);

  // Cerrar el dropdown al hacer click fuera
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (comboboxRef.current && !comboboxRef.current.contains(event.target as Node)) {
        setIsCustomerDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Cliente seleccionado
  const selectedCustomer = customers.find(
    (c) => (c.id || c._id || '').trim().toUpperCase() === customerId.trim().toUpperCase()
  );

  // Parcelas del cliente seleccionado
  const availablePlots = selectedCustomer?.farm_plot || [];

  // Órdenes asociadas al cliente seleccionado
  const availableOrders = salesOrders.filter((o) => {
    if (!customerId) return false;
    const cidTarget = customerId.trim().toUpperCase();
    const oCid = (o.mongoCustomerId || o.customerInfo?.id || o.customerInfo?._id || '').trim().toUpperCase();
    return oCid === cidTarget;
  });

  // Al seleccionar un cliente desde el dropdown
  const handleSelectCustomer = (c: Customer) => {
    const cid = c.id || c._id || '';
    setCustomerId(cid);
    setCustomerSearch(`${cid} - ${c.first_name} ${c.last_name}`);
    setIsCustomerDropdownOpen(false);
    clearError('customerId');

    // 1. Manejo automático de Fundo / Parcela
    const plots = c.farm_plot || [];
    if (plots.length === 1) {
      // 1 parcela: autocompleta automáticamente
      setFarmName(plots[0].farm_name);
      setIsManualFarm(false);
    } else if (plots.length > 1) {
      // Múltiples parcelas: preselecciona la primera
      setFarmName(plots[0].farm_name);
      setIsManualFarm(false);
    } else {
      // Sin parcelas registradas
      setFarmName('');
      setIsManualFarm(true);
    }
    clearError('farmName');

    // 2. Manejo automático de Orden de Venta
    const clientOrders = salesOrders.filter((o) => {
      const oCid = (o.mongoCustomerId || o.customerInfo?.id || o.customerInfo?._id || '').trim().toUpperCase();
      return oCid === cid.trim().toUpperCase();
    });

    if (clientOrders.length === 1) {
      // 1 orden asociada: autoselecciona
      setOrderSalesId(String(clientOrders[0].orderId));
      setIsManualOrder(false);
    } else if (clientOrders.length > 1) {
      // Múltiples órdenes (ej: 2, 8, 12): preselecciona la primera para facilitar
      setOrderSalesId(String(clientOrders[0].orderId));
      setIsManualOrder(false);
    } else {
      setOrderSalesId('1');
      setIsManualOrder(true);
    }
    clearError('orderSalesId');
  };

  const handleClearCustomer = () => {
    setCustomerId('');
    setCustomerSearch('');
    setFarmName('');
    setIsManualFarm(false);
    setOrderSalesId('');
    setIsManualOrder(false);
  };

  const clearError = (field: string) => {
    setErrors((prev) => {
      if (!prev[field]) return prev;
      const copy = { ...prev };
      delete copy[field];
      return copy;
    });
  };

  const validate = (): boolean => {
    const errs: Record<string, string> = {};

    if (!customerId.trim()) {
      errs.customerId = 'Debe seleccionar o indicar un cliente válido (CUS###).';
    }
    if (!farmName.trim()) {
      errs.farmName = 'El nombre del fundo o parcela es obligatorio.';
    }
    if (!orderSalesId.trim()) {
      errs.orderSalesId = 'Debe seleccionar o ingresar la orden de venta asociada.';
    }
    if (!meetingDateTime) {
      errs.meetingDateTime = 'La fecha y hora de visita es obligatoria.';
    }

    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    // Extraer CUS### en caso el valor contenga texto adicional
    const cleanCustomerId = (customerId.match(/CUS\d{3,}/i)?.[0] || customerId.trim()).toUpperCase();

    // Formatear fecha local en formato requerido por Spring Boot (YYYY-MM-DDTHH:mm:ss sin 'Z')
    let cleanDateTime = meetingDateTime.replace('Z', '').split('.')[0];
    if (cleanDateTime.length === 16) {
      cleanDateTime = cleanDateTime + ':00';
    }

    const payload: Partial<FieldMeeting> = {
      meeting_date_time: cleanDateTime,
      customer_id: cleanCustomerId,
      farm_name: farmName.trim(),
      order_sales_id: String(orderSalesId).trim(),
      fruit_quality: fruitQuality.trim(),
      observation: observation.trim(),
      visit_status: visitStatus,
      farm_address: {
        department: selectedCustomer?.address?.department || 'Ica',
        province: selectedCustomer?.address?.province || 'Ica',
        district: selectedCustomer?.address?.district || 'Subtanjalla',
        fundo: farmName.trim(),
        referencia: 'Visita técnica programada a fundo del cliente',
      },
    };

    await onSubmit(payload);
  };

  // Clientes filtrados por el término de búsqueda
  const filteredCustomers = customers.filter((c) => {
    if (!customerSearch.trim()) return true;
    const term = customerSearch.toLowerCase();
    const id = (c.id || c._id || '').toLowerCase();
    const fullName = `${c.first_name || ''} ${c.last_name || ''}`.toLowerCase();
    const doc = (c.document_number || '').toLowerCase();
    return id.includes(term) || fullName.includes(term) || doc.includes(term);
  });

  return (
    <form className="meeting-form" onSubmit={handleSubmit} noValidate>
      {/* FILA 1: ID CLIENTE + NOMBRE DEL FUNDO / PARCELA */}
      <div className="form-grid-2">
        {/* ID CLIENTE (SEARCHABLE COMBOBOX) */}
        <div className="form-group" ref={comboboxRef}>
          <label className="form-label">
            ID Cliente (CUS###) * {isLoadingMasterData && <span className="text-muted">(Cargando...)</span>}
          </label>
          <div className="searchable-combobox">
            <div className="combobox-input-wrapper">
              <input
                type="text"
                className={`form-input ${errors.customerId ? 'input-error' : ''}`}
                placeholder="Buscar por ID (ej. CUS001) o nombre..."
                value={customerSearch}
                onChange={(e) => {
                  setCustomerSearch(e.target.value);
                  setIsCustomerDropdownOpen(true);
                  // Si el usuario escribe directamente el ID exacto
                  const match = customers.find(
                    (c) => (c.id || c._id || '').toUpperCase() === e.target.value.trim().toUpperCase()
                  );
                  if (match) {
                    handleSelectCustomer(match);
                  } else {
                    setCustomerId(e.target.value.trim());
                  }
                  clearError('customerId');
                }}
                onFocus={() => setIsCustomerDropdownOpen(true)}
                autoComplete="off"
              />
              <div className="combobox-actions">
                {customerId && (
                  <button
                    type="button"
                    className="combobox-btn"
                    title="Limpiar selección"
                    onClick={handleClearCustomer}
                  >
                    <X size={15} />
                  </button>
                )}
                <button
                  type="button"
                  className="combobox-btn"
                  title="Ver lista de clientes"
                  onClick={() => setIsCustomerDropdownOpen((prev) => !prev)}
                >
                  <ChevronDown size={16} />
                </button>
              </div>
            </div>

            {/* Panel flotante de opciones */}
            {isCustomerDropdownOpen && (
              <div className="combobox-dropdown">
                {filteredCustomers.length === 0 ? (
                  <div className="combobox-empty">
                    No se encontraron clientes con "{customerSearch}"
                  </div>
                ) : (
                  filteredCustomers.map((c) => {
                    const cid = c.id || c._id || '';
                    const isSelected = cid.toUpperCase() === customerId.toUpperCase();
                    const plotCount = c.farm_plot?.length || 0;
                    return (
                      <div
                        key={cid}
                        className={`combobox-item ${isSelected ? 'active' : ''}`}
                        onClick={() => handleSelectCustomer(c)}
                      >
                        <div className="combobox-item-header">
                          <span className="combobox-item-id">{cid}</span>
                          <span className="combobox-item-name">
                            {c.first_name} {c.last_name}
                          </span>
                          {isSelected && <Check size={14} className="text-emerald-600" />}
                        </div>
                        <div className="combobox-item-sub">
                          <span>Doc: {c.document_number}</span>
                          {plotCount > 0 ? (
                            <span className="text-emerald-700 font-medium">
                              🌱 {plotCount} {plotCount === 1 ? 'parcela' : 'parcelas'}
                            </span>
                          ) : (
                            <span className="text-muted">Sin parcelas</span>
                          )}
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            )}
          </div>
          {errors.customerId && <span className="field-error">{errors.customerId}</span>}
          {selectedCustomer && (
            <div className="field-badge-helper">
              <Check size={13} />
              <span>Cliente: <strong>{selectedCustomer.first_name} {selectedCustomer.last_name}</strong> ({selectedCustomer.id || selectedCustomer._id})</span>
            </div>
          )}
        </div>

        {/* NOMBRE DEL FUNDO / PARCELA */}
        <div className="form-group">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <label className="form-label" style={{ margin: 0 }}>
              Nombre del Fundo / Parcela *
            </label>
            {selectedCustomer && availablePlots.length > 0 && (
              <button
                type="button"
                className="btn-link"
                style={{ fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '4px', cursor: 'pointer', border: 'none', background: 'none', color: '#059669' }}
                onClick={() => setIsManualFarm((prev) => !prev)}
              >
                <Edit3 size={12} />
                <span>{isManualFarm ? 'Seleccionar de la lista' : 'Escribir manual'}</span>
              </button>
            )}
          </div>

          {!selectedCustomer ? (
            <input
              type="text"
              className="form-input"
              placeholder="Primero seleccione un cliente..."
              disabled
              value=""
            />
          ) : isManualFarm || availablePlots.length === 0 ? (
            <>
              <input
                type="text"
                className={`form-input ${errors.farmName ? 'input-error' : ''}`}
                placeholder="Ej. Fundo Santa Rosa"
                value={farmName}
                onChange={(e) => {
                  setFarmName(e.target.value);
                  clearError('farmName');
                }}
              />
              {availablePlots.length === 0 && (
                <div className="field-badge-info">
                  <Info size={13} />
                  <span>El cliente no tiene parcelas en su ficha. Ingrese el nombre manualmente.</span>
                </div>
              )}
            </>
          ) : availablePlots.length === 1 ? (
            <>
              <input
                type="text"
                className={`form-input ${errors.farmName ? 'input-error' : ''}`}
                value={farmName}
                onChange={(e) => {
                  setFarmName(e.target.value);
                  clearError('farmName');
                }}
              />
              <div className="field-badge-helper">
                <Sprout size={13} />
                <span>
                  Parcela autocompletada: <strong>{availablePlots[0].farm_name}</strong>
                  {availablePlots[0].type_crop ? ` (Cultivo: ${availablePlots[0].type_crop})` : ''}
                </span>
              </div>
            </>
          ) : (
            // 2 o más parcelas vinculadas: Desplegable para elegir
            <>
              <select
                className={`form-select ${errors.farmName ? 'input-error' : ''}`}
                value={farmName}
                onChange={(e) => {
                  setFarmName(e.target.value);
                  clearError('farmName');
                }}
              >
                <option value="">-- Seleccione entre las {availablePlots.length} parcelas del cliente --</option>
                {availablePlots.map((p, idx) => (
                  <option key={idx} value={p.farm_name}>
                    {p.farm_name} {p.type_crop ? `(${p.type_crop})` : ''} {p.hectareas ? `- ${p.hectareas} ha` : ''}
                  </option>
                ))}
              </select>
              <div className="field-badge-helper">
                <Sprout size={13} />
                <span>{availablePlots.length} parcelas disponibles para este cliente.</span>
              </div>
            </>
          )}
          {errors.farmName && <span className="field-error">{errors.farmName}</span>}
        </div>
      </div>

      {/* FILA 2: FECHA Y HORA + ORDEN DE VENTA ASOCIADA + ESTADO DE LA VISITA */}
      <div className="form-grid-3">
        {/* FECHA Y HORA */}
        <div className="form-group">
          <label className="form-label">Fecha y Hora de la Visita *</label>
          <input
            type="datetime-local"
            className={`form-input ${errors.meetingDateTime ? 'input-error' : ''}`}
            value={meetingDateTime}
            onChange={(e) => {
              setMeetingDateTime(e.target.value);
              clearError('meetingDateTime');
            }}
            required
          />
          {errors.meetingDateTime && <span className="field-error">{errors.meetingDateTime}</span>}
        </div>

        {/* ORDEN DE VENTA ASOCIADA (FILTRADA POR CLIENTE) */}
        <div className="form-group">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <label className="form-label" style={{ margin: 0 }}>
              Orden de Venta Asociada *
            </label>
            {selectedCustomer && availableOrders.length > 0 && (
              <button
                type="button"
                className="btn-link"
                style={{ fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '4px', cursor: 'pointer', border: 'none', background: 'none', color: '#059669' }}
                onClick={() => setIsManualOrder((prev) => !prev)}
              >
                <Edit3 size={12} />
                <span>{isManualOrder ? 'Ver órdenes' : 'ID manual'}</span>
              </button>
            )}
          </div>

          {!selectedCustomer ? (
            <select className="form-select" disabled value="">
              <option value="">-- Primero seleccione un cliente --</option>
            </select>
          ) : isManualOrder || availableOrders.length === 0 ? (
            <>
              <input
                type="text"
                className={`form-input ${errors.orderSalesId ? 'input-error' : ''}`}
                placeholder="Ej. 1"
                value={orderSalesId}
                onChange={(e) => {
                  setOrderSalesId(e.target.value);
                  clearError('orderSalesId');
                }}
              />
              {availableOrders.length === 0 && (
                <div className="field-badge-info">
                  <ShoppingCart size={13} />
                  <span>Sin órdenes previas registradas. Ingrese ID manual.</span>
                </div>
              )}
            </>
          ) : (
            // Cliente tiene órdenes vinculadas (ej: Carmela con 2, 8 y 12)
            <>
              <select
                className={`form-select ${errors.orderSalesId ? 'input-error' : ''}`}
                value={orderSalesId}
                onChange={(e) => {
                  setOrderSalesId(e.target.value);
                  clearError('orderSalesId');
                }}
              >
                <option value="">-- Seleccione orden ({availableOrders.length} asociadas) --</option>
                {availableOrders.map((o) => (
                  <option key={o.orderId} value={String(o.orderId)}>
                    Orden #{o.orderId} - Total: S/ {Number(o.totalOrder || 0).toFixed(2)} ({o.orderStatus || 'Registrada'})
                  </option>
                ))}
              </select>
              <div className="field-badge-helper">
                <ShoppingCart size={13} />
                <span>{availableOrders.length} {availableOrders.length === 1 ? 'orden asociada' : 'órdenes asociadas'} a este cliente.</span>
              </div>
            </>
          )}
          {errors.orderSalesId && <span className="field-error">{errors.orderSalesId}</span>}
        </div>

        {/* ESTADO DE LA VISITA */}
        <div className="form-group">
          <label className="form-label">Estado de la Visita *</label>
          <select
            className="form-select"
            value={visitStatus}
            onChange={(e) => setVisitStatus(e.target.value)}
          >
            <option value="Pendiente">Pendiente</option>
            <option value="Completa">Completa</option>
            <option value="Pospuesta">Pospuesta</option>
            <option value="Cancelada">Cancelada</option>
          </select>
        </div>
      </div>

      {/* CALIDAD DEL FRUTO */}
      <div className="form-group">
        <label className="form-label">Calidad del Fruto (Evaluación)</label>
        <input
          type="text"
          className="form-input"
          placeholder="Ej. Excelente calibre y coloración uniforme"
          value={fruitQuality}
          onChange={(e) => setFruitQuality(e.target.value)}
        />
      </div>

      {/* OBSERVACIONES */}
      <div className="form-group">
        <label className="form-label">Observaciones del Especialista Agrícola</label>
        <textarea
          className="form-textarea"
          rows={3}
          placeholder="Comentarios sobre dosis, respuesta del cultivo o recomendaciones adicionales..."
          value={observation}
          onChange={(e) => setObservation(e.target.value)}
        />
      </div>

      {/* BOTONES DE ACCIÓN */}
      <div className="form-actions">
        <Button type="button" variant="outline" onClick={onCancel}>
          Cancelar
        </Button>
        <Button type="submit" variant="primary" isLoading={isSaving}>
          {initialData ? 'Actualizar Visita' : 'Agendar Visita de Campo'}
        </Button>
      </div>
    </form>
  );
};
