import React from "react";
import { Supply } from "../types/supply.types";
import { Modal } from "../../../shared/components/Modal";
import { Badge } from "../../../shared/components/Badge";
import { formatDate } from "../../../shared/utils/formatDate";
import { AlertTriangle } from "lucide-react";

interface SupplyDetailModalProps {
  supply: Supply | null;
  onClose: () => void;
}

export const SupplyDetailModal: React.FC<SupplyDetailModalProps> = ({ supply, onClose }) => {
  if (!supply) return null;
  const id = supply.id || supply._id || "";
  const current = supply.inventory?.current_stock ?? 0;
  const min = supply.inventory?.minimum_stock ?? 0;
  const isLow = current <= min;

  return (
    <Modal isOpen={!!supply} onClose={onClose} title={`Detalles del Insumo`} maxWidth="600px">
      <div className="detail-modal">
        <div className="detail-header-row">
          <div>
            <span className="detail-id">{id}</span>
            <h3 className="detail-name">{supply.name}</h3>
            {supply.description && <p className="detail-desc">{supply.description}</p>}
          </div>
          <Badge status={supply.status} />
        </div>

        <div className="detail-grid">
          <div className="detail-item">
            <span className="detail-label">Categoría</span>
            <span className="detail-value"><span className="category-tag">{supply.category}</span></span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Unidad de Medida</span>
            <span className="detail-value">{supply.unit}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Ubicación en Almacén</span>
            <span className="detail-value">{supply.location}</span>
          </div>
        </div>

        <div className="detail-section">
          <h4 className="detail-section-title">Control de Stock</h4>
          <div className="detail-grid">
            <div className={`detail-item ${isLow ? "detail-danger" : "detail-highlight"}`}>
              <span className="detail-label">
                Stock Actual
                {isLow && <AlertTriangle size={13} className="text-danger" style={{ display: "inline", marginLeft: "4px" }} />}
              </span>
              <span className={`detail-value font-semibold ${isLow ? "text-danger" : ""}`}>
                {current} {supply.unit}
                {isLow && <span className="detail-badge-warn"> ¡Stock bajo!</span>}
              </span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Stock Mínimo (Alerta)</span>
              <span className="detail-value">{min} {supply.unit}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Fecha de Vencimiento</span>
              <span className="detail-value">{formatDate(supply.inventory?.expiration_date)}</span>
            </div>
          </div>
        </div>

        {(supply.created_at || supply.update_at) && (
          <div className="detail-audit">
            {supply.created_at && <span>Creado: {supply.created_at}</span>}
            {supply.update_at && <span>Actualizado: {supply.update_at}</span>}
            {supply.deleted_at && <span className="text-danger">Eliminado: {supply.deleted_at}</span>}
          </div>
        )}
      </div>
    </Modal>
  );
};
