import React from "react";
import { Formula } from "../types/product.types";
import { Badge } from "../../../shared/components/Badge";
import { formatCurrency } from "../../../shared/utils/formatDate";
import { Edit2, Trash2, RotateCcw, Eye } from "lucide-react";

interface ProductTableProps {
  formulas: Formula[];
  onEdit: (formula: Formula) => void;
  onDelete: (id: string) => void;
  onRestore: (id: string) => void;
  onViewDetails: (formula: Formula) => void;
}

export const ProductTable: React.FC<ProductTableProps> = ({
  formulas, onEdit, onDelete, onRestore, onViewDetails,
}) => {
  // Ordenar de más reciente a más antiguo por ID (FOR015 -> FOR001)
  const sorted = [...formulas].sort((a, b) => {
    const idA = (a._id || "").toUpperCase();
    const idB = (b._id || "").toUpperCase();
    return idB.localeCompare(idA, undefined, { numeric: true, sensitivity: "base" });
  });

  return (
    <div className="table-container">
      <table className="data-table">
        <thead>
          <tr>
            <th style={{ width: "110px" }}>ID</th>
            <th>NOMBRE</th>
            <th style={{ width: "130px" }}>COSTO PREP.</th>
            <th style={{ width: "140px" }}>PRECIO VENTA</th>
            <th style={{ width: "120px" }}>GALÓN 1 L.</th>
            <th style={{ width: "130px" }}>GALÓN 20 L.</th>
            <th style={{ width: "90px" }}>ESTADO</th>
            <th style={{ width: "110px" }}>ACCIONES</th>
          </tr>
        </thead>
        <tbody>
          {sorted.map((f) => {
            const id = f._id || "";
            const isActive = f.status !== false;
            const gal1 = f.formula_inventory?.find((i) => i.gallon_type === 1);
            const gal20 = f.formula_inventory?.find((i) => i.gallon_type === 20);
            return (
              <tr key={id} className={`table-row ${!isActive ? "row-inactive" : ""}`}>
                <td className="cell-code"><strong>{id || "-"}</strong></td>
                <td className="cell-primary">
                  <span className="table-item-title">{f.name}</span>
                  {f.description && <span className="table-item-sub">{f.description}</span>}
                </td>
                <td>{formatCurrency(f.preparation_cost)}</td>
                <td className="font-semibold">{formatCurrency(f.suggested_price)}</td>
                <td>
                  <span className="pill-count">{gal1 ? `${gal1.stock_quantity} uds.` : "—"}</span>
                </td>
                <td>
                  <span className="pill-count">{gal20 ? `${gal20.stock_quantity} uds.` : "—"}</span>
                </td>
                <td><Badge status={f.status} /></td>
                <td>
                  <div className="table-actions">
                    <button className="btn-action-icon btn-view" title="Ver Detalles" onClick={() => onViewDetails(f)}>
                      <Eye size={15} />
                    </button>
                    {isActive ? (
                      <>
                        <button className="btn-action-icon" title="Editar Fórmula" onClick={() => onEdit(f)}>
                          <Edit2 size={15} />
                        </button>
                        <button className="btn-action-icon btn-danger" title="Desactivar Fórmula" onClick={() => onDelete(id)}>
                          <Trash2 size={15} />
                        </button>
                      </>
                    ) : (
                      <button className="btn-action-icon btn-restore" title="Restaurar Fórmula" onClick={() => onRestore(id)}>
                        <RotateCcw size={15} />
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            );
          })}
          {sorted.length === 0 && (
            <tr><td colSpan={8} className="table-empty-cell">No se encontraron fórmulas registradas.</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
};
