import React from "react";
import { Supply } from "../types/supply.types";
import { Badge } from "../../../shared/components/Badge";
import { formatDate } from "../../../shared/utils/formatDate";
import { Edit2, Trash2, RotateCcw, Eye, AlertTriangle } from "lucide-react";

interface SupplyTableProps {
  supplies: Supply[];
  onEdit: (supply: Supply) => void;
  onDelete: (id: string) => void;
  onRestore: (id: string) => void;
  onViewDetails: (supply: Supply) => void;
}

export const SupplyTable: React.FC<SupplyTableProps> = ({
  supplies, onEdit, onDelete, onRestore, onViewDetails,
}) => {
  // Ordenar de más reciente a más antiguo por ID (SUP015 -> SUP001)
  const sorted = [...supplies].sort((a, b) => {
    const idA = (a.id || a._id || "").toUpperCase();
    const idB = (b.id || b._id || "").toUpperCase();
    return idB.localeCompare(idA, undefined, { numeric: true, sensitivity: "base" });
  });

  return (
    <div className="table-container">
      <table className="data-table">
        <thead>
          <tr>
            <th style={{ width: "110px" }}>IDENTIFICADOR</th>
            <th>NOMBRE</th>
            <th style={{ width: "130px" }}>STOCK ACTUAL</th>
            <th style={{ width: "100px" }}>UNIDAD</th>
            <th style={{ width: "150px" }}>FECHA EXPIRACIÓN</th>
            <th style={{ width: "200px" }}>UBICACIÓN</th>
            <th style={{ width: "90px" }}>ESTADO</th>
            <th style={{ width: "110px" }}>ACCIONES</th>
          </tr>
        </thead>
        <tbody>
          {sorted.map((s) => {
            const id = s.id || s._id || "";
            const isActive = s.status !== false;
            const current = s.inventory?.current_stock ?? 0;
            const min = s.inventory?.minimum_stock ?? 0;
            const isLowStock = current <= min;

            return (
              <tr key={id} className={`table-row ${!isActive ? "row-inactive" : ""}`}>
                <td className="cell-code"><strong>{id || "-"}</strong></td>
                <td className="cell-primary">
                  <span className="table-item-title">{s.name}</span>
                  {s.description && <span className="table-item-sub">{s.description}</span>}
                </td>
                <td>
                  <div className="flex-center-gap">
                    <span className={`font-semibold ${isLowStock ? "text-danger" : ""}`}>{current}</span>
                    {isLowStock && (
                      <span title="¡Alerta de stock bajo!">
                        <AlertTriangle size={14} className="text-danger" />
                      </span>
                    )}
                  </div>
                </td>
                <td>{s.unit}</td>
                <td>{formatDate(s.inventory?.expiration_date)}</td>
                <td><span className="location-text">{s.location}</span></td>
                <td><Badge status={s.status} /></td>
                <td>
                  <div className="table-actions">
                    <button className="btn-action-icon btn-view" title="Ver Detalles" onClick={() => onViewDetails(s)}>
                      <Eye size={15} />
                    </button>
                    {isActive ? (
                      <>
                        <button className="btn-action-icon" title="Editar Insumo" onClick={() => onEdit(s)}>
                          <Edit2 size={15} />
                        </button>
                        <button className="btn-action-icon btn-danger" title="Desactivar Insumo" onClick={() => onDelete(id)}>
                          <Trash2 size={15} />
                        </button>
                      </>
                    ) : (
                      <button className="btn-action-icon btn-restore" title="Restaurar Insumo" onClick={() => onRestore(id)}>
                        <RotateCcw size={15} />
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            );
          })}
          {sorted.length === 0 && (
            <tr><td colSpan={8} className="table-empty-cell">No se encontraron insumos registrados.</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
};
