import React from 'react';
import { Formula } from '../types/product.types';
import { Modal } from '../../../shared/components/Modal';
import { Button } from '../../../shared/components/Button';
import { formatDate } from '../../../shared/utils/formatDate';

interface FormulaInventoryModalProps {
  formula: Formula | null;
  onClose: () => void;
}

export const FormulaInventoryModal: React.FC<FormulaInventoryModalProps> = ({
  formula,
  onClose,
}) => {
  if (!formula) return null;

  return (
    <Modal
      isOpen={!!formula}
      onClose={onClose}
      title={`Detalles de Fórmula: ${formula.name} (${formula._id || 'N/A'})`}
      maxWidth="700px"
      footer={<Button onClick={onClose} variant="primary">Cerrar</Button>}
    >
      <div className="detail-modal-content">
        <div className="detail-section">
          <h4 className="section-heading">Inventario de Presentaciones (Galones)</h4>
          <table className="data-table" style={{ marginTop: '8px' }}>
            <thead>
              <tr>
                <th>PRESENTACIÓN</th>
                <th>STOCK ACTUAL</th>
                <th>ÚLTIMA ACTUALIZACIÓN</th>
              </tr>
            </thead>
            <tbody>
              {formula.formula_inventory && formula.formula_inventory.length > 0 ? (
                formula.formula_inventory.map((inv, idx) => (
                  <tr key={idx}>
                    <td>
                      <strong>
                        {inv.gallon_type === 1 ? 'Galón de 1 Litro (1L)' : `${inv.gallon_type} Litros (${inv.gallon_type}L)`}
                      </strong>
                    </td>
                    <td className="font-semibold">{inv.stock_quantity} unidades</td>
                    <td>{formatDate(inv.last_update)}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={3} className="table-empty-cell">Sin inventario registrado.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="detail-section" style={{ marginTop: '20px' }}>
          <h4 className="section-heading">
            Receta Oficial ({formula.recipe?.version || 'v1.0'})
          </h4>
          <table className="data-table" style={{ marginTop: '8px' }}>
            <thead>
              <tr>
                <th>INSUMO</th>
                <th>CANTIDAD REQUERIDA</th>
                <th>PORCENTAJE (%)</th>
              </tr>
            </thead>
            <tbody>
              {formula.recipe?.ingredients && formula.recipe.ingredients.length > 0 ? (
                formula.recipe.ingredients.map((ing, idx) => (
                  <tr key={idx}>
                    <td>{ing.supply_name}</td>
                    <td>{ing.quantity_required} {formula.unit}</td>
                    <td>{ing.percentage}%</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={3} className="table-empty-cell">Sin ingredientes registrados.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </Modal>
  );
};
