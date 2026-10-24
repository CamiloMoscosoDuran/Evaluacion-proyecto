import React from 'react';
import { FieldMeeting } from '../types/meeting.types';
import { Badge } from '../../../shared/components/Badge';
import { formatDateTime } from '../../../shared/utils/formatDate';
import { Eye, Edit2, Trash2, RotateCcw } from 'lucide-react';

interface MeetingTableProps {
  meetings: FieldMeeting[];
  onEdit: (meeting: FieldMeeting) => void;
  onDelete: (id: string) => void;
  onRestore: (id: string) => void;
  onViewDetails: (meeting: FieldMeeting) => void;
}

export const MeetingTable: React.FC<MeetingTableProps> = ({
  meetings,
  onEdit,
  onDelete,
  onRestore,
  onViewDetails,
}) => {
  // Ordenar de más reciente a más antiguo por ID (MET017 -> MET001)
  const sorted = [...meetings].sort((a, b) => {
    const idA = (a.id || a._id || '').toUpperCase();
    const idB = (b.id || b._id || '').toUpperCase();
    return idB.localeCompare(idA, undefined, { numeric: true, sensitivity: 'base' });
  });

  return (
    <div className="table-container">
      <table className="data-table">
        <thead>
          <tr>
            <th style={{ width: '110px' }}>ID VISITA</th>
            <th style={{ width: '130px' }}>CLIENTE</th>
            <th>FUNDO / PARCELA</th>
            <th style={{ width: '170px' }}>FECHA Y HORA</th>
            <th style={{ width: '130px' }}>ORDEN VENTA</th>
            <th>CALIDAD FRUTO</th>
            <th style={{ width: '110px' }}>ESTADO</th>
            <th style={{ width: '120px' }}>ACCIONES</th>
          </tr>
        </thead>
        <tbody>
          {sorted.map((m) => {
            const id = m.id || m._id || '';
            const isCancelled = m.visit_status === 'Cancelada';
            return (
              <tr key={id} className={`table-row ${isCancelled ? 'row-inactive' : ''}`}>
                <td className="cell-code">
                  <strong>{id}</strong>
                </td>
                <td className="cell-primary">
                  <span className="table-item-title">{m.customer_id}</span>
                </td>
                <td>{m.farm_name}</td>
                <td>{formatDateTime(m.meeting_date_time)}</td>
                <td>
                  <span className="doc-tag" style={{ background: '#eff6ff', color: '#1d4ed8' }}>
                    Orden #{m.order_sales_id}
                  </span>
                </td>
                <td>{m.fruit_quality || '-'}</td>
                <td>
                  <Badge status={m.visit_status} />
                </td>
                <td>
                  <div className="table-actions">
                    <button
                      className="btn-action-icon btn-view"
                      title="Ver Detalles"
                      onClick={() => onViewDetails(m)}
                    >
                      <Eye size={15} />
                    </button>
                    {isCancelled ? (
                      <button
                        className="btn-action-icon btn-restore"
                        title="Restaurar Visita"
                        onClick={() => onRestore(id)}
                      >
                        <RotateCcw size={15} />
                      </button>
                    ) : (
                      <>
                        <button
                          className="btn-action-icon"
                          title="Editar Visita"
                          onClick={() => onEdit(m)}
                        >
                          <Edit2 size={15} />
                        </button>
                        <button
                          className="btn-action-icon btn-danger"
                          title="Eliminar Visita"
                          onClick={() => onDelete(id)}
                        >
                          <Trash2 size={15} />
                        </button>
                      </>
                    )}
                  </div>
                </td>
              </tr>
            );
          })}
          {sorted.length === 0 && (
            <tr>
              <td colSpan={8} className="table-empty-cell">
                No hay visitas de campo programadas en el sistema.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
};
