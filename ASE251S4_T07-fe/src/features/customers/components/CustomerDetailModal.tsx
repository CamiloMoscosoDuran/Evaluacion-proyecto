import React from "react";
import { Customer } from "../types/customer.types";
import { Modal } from "../../../shared/components/Modal";
import { Badge } from "../../../shared/components/Badge";
import { formatDate } from "../../../shared/utils/formatDate";
import { Phone, Mail, MapPin, Layers } from "lucide-react";

interface CustomerDetailModalProps {
  customer: Customer | null;
  onClose: () => void;
}

export const CustomerDetailModal: React.FC<CustomerDetailModalProps> = ({ customer, onClose }) => {
  if (!customer) return null;
  const id = customer.id || customer._id || "";

  return (
    <Modal isOpen={!!customer} onClose={onClose} title={`Detalles del Cliente`} maxWidth="700px">
      <div className="detail-modal">
        <div className="detail-header-row">
          <div>
            <span className="detail-id">{id}</span>
            <h3 className="detail-name">{customer.first_name} {customer.last_name}</h3>
            <div className="detail-doc-row">
              <span className="doc-tag">{customer.document_type}</span>
              <span>{customer.document_number}</span>
            </div>
          </div>
          <Badge status={customer.status} />
        </div>

        <div className="detail-grid">
          {customer.phone && customer.phone.length > 0 && (
            <div className="detail-item">
              <span className="detail-label"><Phone size={13} style={{ display: "inline" }} /> Teléfonos</span>
              <span className="detail-value">{customer.phone.join(", ")}</span>
            </div>
          )}
          {customer.email && (
            <div className="detail-item">
              <span className="detail-label"><Mail size={13} style={{ display: "inline" }} /> Correo</span>
              <span className="detail-value">{customer.email}</span>
            </div>
          )}
          <div className="detail-item">
            <span className="detail-label">Fecha de Registro</span>
            <span className="detail-value">{formatDate(customer.registration_date)}</span>
          </div>
        </div>

        {customer.address && (
          <div className="detail-section">
            <h4 className="detail-section-title"><MapPin size={14} style={{ display: "inline" }} /> Dirección</h4>
            <div className="detail-grid">
              <div className="detail-item">
                <span className="detail-label">Departamento</span>
                <span className="detail-value">{customer.address.department}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Provincia</span>
                <span className="detail-value">{customer.address.province}</span>
              </div>
              <div className="detail-item">
                <span className="detail-label">Distrito</span>
                <span className="detail-value">{customer.address.district}</span>
              </div>
              {customer.address.especifics && (
                <>
                  <div className="detail-item">
                    <span className="detail-label">Asentamiento</span>
                    <span className="detail-value">{customer.address.especifics.settlement_type} — {customer.address.especifics.settlement_name}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Vía</span>
                    <span className="detail-value">{customer.address.especifics.street_type} {customer.address.especifics.street_name}</span>
                  </div>
                  <div className="detail-item" style={{ gridColumn: "1 / -1" }}>
                    <span className="detail-label">Referencia</span>
                    <span className="detail-value">{customer.address.especifics.reference}</span>
                  </div>
                </>
              )}
            </div>
          </div>
        )}

        {customer.farm_plot && customer.farm_plot.length > 0 && (
          <div className="detail-section">
            <h4 className="detail-section-title"><Layers size={14} style={{ display: "inline" }} /> Parcelas y Cultivos</h4>
            <table className="detail-table">
              <thead>
                <tr>
                  <th>Fundo / Parcela</th>
                  <th>Hectáreas</th>
                  <th>Tipo de Cultivo</th>
                </tr>
              </thead>
              <tbody>
                {customer.farm_plot.map((p, i) => (
                  <tr key={i}>
                    <td>{p.farm_name}</td>
                    <td>{p.hectareas} ha</td>
                    <td>{p.type_crop}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {(customer.created_at || customer.update_at) && (
          <div className="detail-audit">
            {customer.created_at && <span>Creado: {customer.created_at}</span>}
            {customer.update_at && <span>Actualizado: {customer.update_at}</span>}
            {customer.deleted_at && <span className="text-danger">Eliminado: {customer.deleted_at}</span>}
          </div>
        )}
      </div>
    </Modal>
  );
};
