import React from 'react';
import { FieldMeeting } from '../types/meeting.types';
import { Modal } from '../../../shared/components/Modal';
import { Badge } from '../../../shared/components/Badge';
import { formatDateTime } from '../../../shared/utils/formatDate';
import { Calendar, User, MapPin, ShoppingBag, FileText, Activity } from 'lucide-react';

interface MeetingDetailModalProps {
  meeting: FieldMeeting | null;
  onClose: () => void;
}

export const MeetingDetailModal: React.FC<MeetingDetailModalProps> = ({ meeting, onClose }) => {
  if (!meeting) return null;
  const id = meeting.id || meeting._id || '';

  const address = meeting.farm_address as Record<string, any> | undefined;
  const addressDetails = address
    ? [address.department, address.province, address.district].filter(Boolean).join(' - ')
    : '';

  return (
    <Modal isOpen={!!meeting} onClose={onClose} title="Detalles de la Visita de Campo" maxWidth="720px">
      <div className="detail-modal">
        {/* Cabecera con ID y Estado */}
        <div className="detail-header-row">
          <div>
            <span className="detail-id">{id}</span>
            <h3 className="detail-name">{meeting.farm_name}</h3>
            <div className="detail-doc-row">
              <span className="doc-tag">Cliente: {meeting.customer_id}</span>
              <span className="doc-tag" style={{ background: '#eff6ff', color: '#1d4ed8' }}>
                Orden #{meeting.order_sales_id}
              </span>
            </div>
          </div>
          <Badge status={meeting.visit_status} />
        </div>

        {/* Grilla Principal */}
        <div className="detail-grid">
          <div className="detail-item">
            <span className="detail-label">
              <Calendar size={13} style={{ display: 'inline', marginRight: '4px' }} />
              Fecha y Hora Programada
            </span>
            <span className="detail-value">{formatDateTime(meeting.meeting_date_time)}</span>
          </div>

          <div className="detail-item">
            <span className="detail-label">
              <User size={13} style={{ display: 'inline', marginRight: '4px' }} />
              Identificador Cliente
            </span>
            <span className="detail-value font-semibold">{meeting.customer_id}</span>
          </div>

          <div className="detail-item">
            <span className="detail-label">
              <ShoppingBag size={13} style={{ display: 'inline', marginRight: '4px' }} />
              Orden de Venta
            </span>
            <span className="detail-value">Orden #{meeting.order_sales_id}</span>
          </div>

          <div className="detail-item">
            <span className="detail-label">
              <Activity size={13} style={{ display: 'inline', marginRight: '4px' }} />
              Estado de Visita
            </span>
            <span className="detail-value">{meeting.visit_status}</span>
          </div>
        </div>

        {/* Sección: Fundo y Ubicación */}
        <div className="detail-section">
          <h4 className="detail-section-title">
            <MapPin size={14} style={{ display: 'inline', marginRight: '6px' }} />
            Ubicación del Fundo / Parcela
          </h4>
          <div className="detail-grid">
            <div className="detail-item">
              <span className="detail-label">Nombre del Fundo</span>
              <span className="detail-value font-semibold">{meeting.farm_name}</span>
            </div>
            {addressDetails && (
              <div className="detail-item">
                <span className="detail-label">Ubicación Geográfica</span>
                <span className="detail-value">{addressDetails}</span>
              </div>
            )}
            {address?.referencia && (
              <div className="detail-item" style={{ gridColumn: '1 / -1' }}>
                <span className="detail-label">Referencia</span>
                <span className="detail-value">{address.referencia}</span>
              </div>
            )}
          </div>
        </div>

        {/* Sección: Evaluación Agronómica */}
        <div className="detail-section">
          <h4 className="detail-section-title">
            <FileText size={14} style={{ display: 'inline', marginRight: '6px' }} />
            Evaluación Agronómica y Observaciones
          </h4>
          <div className="detail-grid">
            <div className="detail-item" style={{ gridColumn: '1 / -1' }}>
              <span className="detail-label">Calidad del Fruto (Evaluación)</span>
              <span className="detail-value" style={{ color: '#047857', fontWeight: 600 }}>
                {meeting.fruit_quality || 'Sin evaluación registrada'}
              </span>
            </div>

            <div className="detail-item" style={{ gridColumn: '1 / -1' }}>
              <span className="detail-label">Observaciones del Especialista Agrícola</span>
              <p
                style={{
                  margin: '4px 0 0',
                  fontSize: '0.85rem',
                  lineHeight: '1.45',
                  color: 'var(--slate-700)',
                  background: '#f8fafc',
                  padding: '10px 12px',
                  borderRadius: '6px',
                  border: '1px solid var(--slate-200)',
                }}
              >
                {meeting.observation || 'Sin observaciones registradas para esta visita.'}
              </p>
            </div>
          </div>
        </div>

        {/* Auditoría */}
        {(meeting.created_at || meeting.update_at) && (
          <div className="detail-audit">
            {meeting.created_at && <span>Creado: {meeting.created_at}</span>}
            {meeting.update_at && <span>Actualizado: {meeting.update_at}</span>}
            {meeting.deleted_at && <span className="text-danger">Eliminado: {meeting.deleted_at}</span>}
          </div>
        )}
      </div>
    </Modal>
  );
};
