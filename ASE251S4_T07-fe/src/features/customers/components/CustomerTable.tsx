import React from "react";
import { Customer } from "../types/customer.types";
import { Badge } from "../../../shared/components/Badge";
import { formatDate } from "../../../shared/utils/formatDate";
import { Edit2, Trash2, RotateCcw, Eye, Phone, Mail, MapPin } from "lucide-react";

interface CustomerTableProps {
  customers: Customer[];
  onEdit: (customer: Customer) => void;
  onDelete: (id: string) => void;
  onRestore: (id: string) => void;
  onViewDetails: (customer: Customer) => void;
}

export const CustomerTable: React.FC<CustomerTableProps> = ({
  customers, onEdit, onDelete, onRestore, onViewDetails,
}) => {
  // Ordenar de más reciente a más antiguo por ID (CUS015 -> CUS001)
  const sorted = [...customers].sort((a, b) => {
    const idA = (a.id || a._id || "").toUpperCase();
    const idB = (b.id || b._id || "").toUpperCase();
    return idB.localeCompare(idA, undefined, { numeric: true, sensitivity: "base" });
  });

  return (
    <div className="table-container">
      <table className="data-table customer-table">
        <thead>
          <tr>
            <th style={{ width: "110px" }}>ID</th>
            <th>NOMBRE COMPLETO</th>
            <th style={{ width: "160px" }}>DOCUMENTO</th>
            <th style={{ width: "200px" }}>CONTACTO</th>
            <th style={{ width: "180px" }}>CULTIVO / HA</th>
            <th style={{ width: "190px" }}>DIRECCIÓN</th>
            <th style={{ width: "100px" }}>REGISTRO</th>
            <th style={{ width: "90px" }}>ESTADO</th>
            <th style={{ width: "110px" }}>ACCIONES</th>
          </tr>
        </thead>
        <tbody>
          {sorted.map((c) => {
            const id = c.id || c._id || "";
            const isActive = c.status !== false;
            const mainPlot = c.farm_plot && c.farm_plot.length > 0 ? c.farm_plot[0] : null;
            const district = c.address?.district || "";
            const province = c.address?.province || "";
            const location = [district, province].filter(Boolean).join(", ") || "-";

            return (
              <tr key={id} className={`table-row ${!isActive ? "row-inactive" : ""}`}>
                {/* ID */}
                <td className="cell-code">
                  <strong>{id || "-"}</strong>
                </td>

                {/* Nombre completo */}
                <td>
                  <div className="customer-name-cell">
                    <span className="table-item-title">{c.first_name} {c.last_name}</span>
                  </div>
                </td>

                {/* Documento */}
                <td>
                  <div className="customer-doc-cell">
                    <span className="doc-tag">{c.document_type}</span>
                    <span className="doc-number">{c.document_number}</span>
                  </div>
                </td>

                {/* Contacto */}
                <td>
                  <div className="customer-contact-cell">
                    {c.phone && c.phone.length > 0 && (
                      <div className="contact-row">
                        <Phone size={12} className="contact-icon" />
                        <span>{c.phone[0]}{c.phone.length > 1 ? ` (+${c.phone.length - 1})` : ""}</span>
                      </div>
                    )}
                    {c.email && (
                      <div className="contact-row">
                        <Mail size={12} className="contact-icon" />
                        <span className="contact-email">{c.email}</span>
                      </div>
                    )}
                    {!c.phone?.length && !c.email && (
                      <span className="text-muted">Sin contacto</span>
                    )}
                  </div>
                </td>

                {/* Cultivo */}
                <td>
                  {mainPlot ? (
                    <div className="farm-cell">
                      <span className="farm-crop">{mainPlot.type_crop}</span>
                      <span className="farm-ha">{mainPlot.hectareas} ha — {mainPlot.farm_name}</span>
                      {(c.farm_plot?.length ?? 0) > 1 && (
                        <span className="farm-extra">+{(c.farm_plot?.length ?? 0) - 1} parcela(s) más</span>
                      )}
                    </div>
                  ) : (
                    <span className="text-muted">Sin parcelas</span>
                  )}
                </td>

                {/* Dirección */}
                <td>
                  <div className="contact-row">
                    <MapPin size={12} className="contact-icon" />
                    <span>{location}</span>
                  </div>
                </td>

                {/* Fecha registro */}
                <td>
                  <span className="date-cell">{formatDate(c.registration_date)}</span>
                </td>

                {/* Estado */}
                <td><Badge status={c.status} /></td>

                {/* Acciones */}
                <td>
                  <div className="table-actions">
                    <button className="btn-action-icon btn-view" title="Ver Detalles" onClick={() => onViewDetails(c)}>
                      <Eye size={15} />
                    </button>
                    {isActive ? (
                      <>
                        <button className="btn-action-icon" title="Editar Cliente" onClick={() => onEdit(c)}>
                          <Edit2 size={15} />
                        </button>
                        <button className="btn-action-icon btn-danger" title="Desactivar Cliente" onClick={() => onDelete(id)}>
                          <Trash2 size={15} />
                        </button>
                      </>
                    ) : (
                      <button className="btn-action-icon btn-restore" title="Restaurar Cliente" onClick={() => onRestore(id)}>
                        <RotateCcw size={15} />
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            );
          })}
          {sorted.length === 0 && (
            <tr>
              <td colSpan={9} className="table-empty-cell">
                No se encontraron clientes registrados.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
};
