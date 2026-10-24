import React from "react";
import { Formula } from "../types/product.types";
import { Modal } from "../../../shared/components/Modal";
import { Badge } from "../../../shared/components/Badge";
import { formatCurrency } from "../../../shared/utils/formatDate";

interface FormulaDetailModalProps {
  formula: Formula | null;
  onClose: () => void;
}

export const FormulaDetailModal: React.FC<FormulaDetailModalProps> = ({ formula, onClose }) => {
  if (!formula) return null;

  const gal1 = formula.formula_inventory?.find((i) => i.gallon_type === 1);
  const gal20 = formula.formula_inventory?.find((i) => i.gallon_type === 20);

  return (
    <Modal isOpen={!!formula} onClose={onClose} title={`Detalles de Fórmula: ${formula.name}`} maxWidth="700px">
      <div className="detail-modal">
        <div className="detail-header-row">
          <div>
            <span className="detail-id">{formula._id}</span>
            <h3 className="detail-name">{formula.name}</h3>
            {formula.description && <p className="detail-desc">{formula.description}</p>}
          </div>
          <Badge status={formula.status} />
        </div>

        <div className="detail-grid">
          <div className="detail-item">
            <span className="detail-label">Unidad de Medida</span>
            <span className="detail-value">{formula.unit}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Lote Estándar</span>
            <span className="detail-value">{formula.standard_batch}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Tiempo de Producción</span>
            <span className="detail-value">{formula.production_time} min</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Costo de Preparación</span>
            <span className="detail-value">{formatCurrency(formula.preparation_cost)}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Precio Sugerido Venta</span>
            <span className="detail-value font-semibold text-primary">{formatCurrency(formula.suggested_price)}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Versión Receta</span>
            <span className="detail-value">{formula.recipe?.version || "-"}</span>
          </div>
        </div>

        <div className="detail-section">
          <h4 className="detail-section-title">Inventario de Presentaciones</h4>
          <div className="detail-grid">
            <div className="detail-item detail-highlight">
              <span className="detail-label">Galón 1 Litro</span>
              <span className="detail-value">{gal1 ? `${gal1.stock_quantity} unidades` : "Sin registro"}</span>
            </div>
            <div className="detail-item detail-highlight">
              <span className="detail-label">Galón 20 Litros</span>
              <span className="detail-value">{gal20 ? `${gal20.stock_quantity} unidades` : "Sin registro"}</span>
            </div>
          </div>
        </div>

        {formula.recipe && formula.recipe.ingredients.length > 0 && (
          <div className="detail-section">
            <h4 className="detail-section-title">Ingredientes de la Receta</h4>
            <table className="detail-table">
              <thead>
                <tr>
                  <th>Insumo</th>
                  <th>Cantidad Requerida</th>
                  <th>Porcentaje (%)</th>
                </tr>
              </thead>
              <tbody>
                {formula.recipe.ingredients.map((ing, i) => (
                  <tr key={i}>
                    <td>{ing.supply_name}</td>
                    <td>{ing.quantity_required}</td>
                    <td>{ing.percentage}%</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {(formula.created_at || formula.update_at) && (
          <div className="detail-audit">
            {formula.created_at && <span>Creado: {formula.created_at}</span>}
            {formula.update_at && <span>Actualizado: {formula.update_at}</span>}
            {formula.deleted_at && <span className="text-danger">Eliminado: {formula.deleted_at}</span>}
          </div>
        )}
      </div>
    </Modal>
  );
};
