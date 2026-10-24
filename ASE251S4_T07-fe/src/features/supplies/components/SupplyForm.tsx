import React, { useState, useEffect } from "react";
import { Supply } from "../types/supply.types";
import { Button } from "../../../shared/components/Button";

interface SupplyFormProps {
  initialData?: Supply | null;
  onSubmit: (data: Partial<Supply>) => Promise<void>;
  onCancel: () => void;
  isSaving: boolean;
}

interface FieldErrors { [key: string]: string; }

export const SupplyForm: React.FC<SupplyFormProps> = ({ initialData, onSubmit, onCancel, isSaving }) => {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [category, setCategory] = useState("Fertilizantes");
  const [unit, setUnit] = useState("kilos");
  const [location, setLocation] = useState("");
  const [currentStock, setCurrentStock] = useState<number>(100);
  const [minimumStock, setMinimumStock] = useState<number>(10);
  const [expirationDate, setExpirationDate] = useState(() => "2027-12-31");
  const [errors, setErrors] = useState<FieldErrors>({});

  useEffect(() => {
    if (initialData) {
      setName(initialData.name || "");
      setDescription(initialData.description || "");
      setCategory(initialData.category || "Fertilizantes");
      setUnit(initialData.unit || "kilos");
      setLocation(initialData.location || "");
      if (initialData.inventory) {
        setCurrentStock(initialData.inventory.current_stock ?? 0);
        setMinimumStock(initialData.inventory.minimum_stock ?? 0);
        setExpirationDate(initialData.inventory.expiration_date || "2027-12-31");
      }
    }
  }, [initialData]);

  const clearError = (f: string) => setErrors((prev) => ({ ...prev, [f]: "" }));

  const validate = (): boolean => {
    const e: FieldErrors = {};

    if (!name.trim()) e.name = "El nombre del insumo es obligatorio.";
    else if (name.trim().length < 10) e.name = "El nombre debe tener al menos 10 caracteres.";

    if (!description.trim()) e.description = "La descripción es obligatoria.";
    else if (description.trim().length < 10) e.description = "La descripción debe tener al menos 10 caracteres.";

    if (!unit) e.unit = "Debe seleccionar una unidad de medida.";

    if (!location.trim()) e.location = "La ubicación es obligatoria.";
    else if (location.trim().length < 10) e.location = "La ubicación debe tener al menos 10 caracteres.";

    if (minimumStock === null || minimumStock === undefined || minimumStock <= 0)
      e.minimumStock = "El stock mínimo debe ser mayor a 0.";

    if (currentStock < minimumStock)
      e.currentStock = `El stock actual (${currentStock}) no puede ser menor al stock mínimo (${minimumStock}).`;
    else if (currentStock < 0)
      e.currentStock = "El stock actual no puede ser negativo.";

    if (!expirationDate) e.expirationDate = "La fecha de vencimiento es obligatoria.";

    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (ev: React.FormEvent) => {
    ev.preventDefault();
    if (!validate()) return;
    const payload: Partial<Supply> = {
      name, description, category, unit, location,
      status: initialData ? (initialData.status ?? true) : true,
      inventory: {
        current_stock: Number(currentStock),
        minimum_stock: Number(minimumStock),
        expiration_date: expirationDate,
      },
    };
    await onSubmit(payload);
  };

  const ec = (f: string) => (errors[f] ? "form-input input-error" : "form-input");

  return (
    <form className="supply-form" onSubmit={handleSubmit} noValidate>
      <div className="form-grid-2">
        <div className="form-group">
          <label className="form-label">Nombre del Insumo *<span className="form-hint"> (min. 10 caracteres)</span></label>
          <input type="text" className={ec("name")} placeholder="Ej. Nitrato de Calcio Premium" value={name}
            onChange={(e) => { setName(e.target.value); clearError("name"); }} />
          {errors.name && <span className="field-error">{errors.name}</span>}
        </div>
        <div className="form-group">
          <label className="form-label">Categoría *</label>
          <select className="form-select" value={category} onChange={(e) => setCategory(e.target.value)}>
            <option value="Fertilizantes">Fertilizantes</option>
            <option value="Insecticidas">Insecticidas</option>
            <option value="Fungicidas">Fungicidas</option>
            <option value="Reguladores">Reguladores de Crecimiento</option>
            <option value="Micronutrientes">Micronutrientes</option>
            <option value="General">General</option>
          </select>
        </div>
      </div>

      <div className="form-group">
        <label className="form-label">Descripción / Beneficio de uso *<span className="form-hint"> (min. 10 caracteres)</span></label>
        <textarea className={errors.description ? "form-textarea input-error" : "form-textarea"} rows={2}
          placeholder="Descripción del aporte y función técnica del insumo..."
          value={description} onChange={(e) => { setDescription(e.target.value); clearError("description"); }} />
        {errors.description && <span className="field-error">{errors.description}</span>}
      </div>

      <div className="form-grid-2">
        <div className="form-group">
          <label className="form-label">Unidad de Medida *</label>
          <select className={errors.unit ? "form-select input-error" : "form-select"} value={unit}
            onChange={(e) => { setUnit(e.target.value); clearError("unit"); }}>
            <option value="">-- Seleccionar unidad --</option>
            <option value="litros">Litros</option>
            <option value="kilos">Kilos</option>
            <option value="gramos">Gramos</option>
            <option value="toneladas">Toneladas</option>
          </select>
          {errors.unit && <span className="field-error">{errors.unit}</span>}
        </div>
        <div className="form-group">
          <label className="form-label">Ubicación en Almacén *<span className="form-hint"> (min. 10 caracteres)</span></label>
          <input type="text" className={ec("location")} placeholder="Almacén A - Estante 2, Zona Fría"
            value={location} onChange={(e) => { setLocation(e.target.value); clearError("location"); }} />
          {errors.location && <span className="field-error">{errors.location}</span>}
        </div>
      </div>

      <div className="form-subcard">
        <h4 className="subcard-title">Control de Stock y Caducidad</h4>
        <div className="form-grid-3" style={{ marginTop: "8px" }}>
          <div className="form-group">
            <label className="form-label">Stock Mínimo (Alerta) *</label>
            <input type="number" step="0.1" min="0.1" className={ec("minimumStock")} value={minimumStock}
              onChange={(e) => { setMinimumStock(Number(e.target.value)); clearError("minimumStock"); clearError("currentStock"); }} />
            {errors.minimumStock && <span className="field-error">{errors.minimumStock}</span>}
          </div>
          <div className="form-group">
            <label className="form-label">Stock Actual *<span className="form-hint"> (≥ stock mínimo)</span></label>
            <input type="number" step="0.1" min="0" className={ec("currentStock")} value={currentStock}
              onChange={(e) => { setCurrentStock(Number(e.target.value)); clearError("currentStock"); }} />
            {errors.currentStock && <span className="field-error">{errors.currentStock}</span>}
          </div>
          <div className="form-group">
            <label className="form-label">Fecha de Vencimiento *</label>
            <input type="date" className={ec("expirationDate")} value={expirationDate}
              onChange={(e) => { setExpirationDate(e.target.value); clearError("expirationDate"); }} />
            {errors.expirationDate && <span className="field-error">{errors.expirationDate}</span>}
          </div>
        </div>
      </div>

      <div className="form-actions">
        <Button type="button" variant="outline" onClick={onCancel}>Cancelar</Button>
        <Button type="submit" variant="primary" isLoading={isSaving}>
          {initialData ? "Actualizar Insumo" : "Registrar Insumo"}
        </Button>
      </div>
    </form>
  );
};
