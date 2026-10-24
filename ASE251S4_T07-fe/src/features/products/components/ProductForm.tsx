import React, { useState, useEffect } from "react";
import { Formula, Ingredient } from "../types/product.types";
import { Supply } from "../../supplies/types/supply.types";
import { suppliesService } from "../../supplies/services/suppliesService";
import { Button } from "../../../shared/components/Button";
import { Plus, Trash2 } from "lucide-react";

interface ProductFormProps {
  initialData?: Formula | null;
  onSubmit: (data: Partial<Formula>) => Promise<void>;
  onCancel: () => void;
  isSaving: boolean;
}

interface FieldErrors {
  [key: string]: string;
}

const ONLY_LETTERS = /^[A-Za-zÁáÉéÍíÓóÚúÑñüÜ\s,.]+$/;

export const ProductForm: React.FC<ProductFormProps> = ({
  initialData,
  onSubmit,
  onCancel,
  isSaving,
}) => {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [standardBatch, setStandardBatch] = useState<number>(5);
  const [unit, setUnit] = useState("Litros");
  const [productionTime, setProductionTime] = useState<number>(60);
  const [preparationCost, setPreparationCost] = useState<number>(0);
  const [suggestedPrice, setSuggestedPrice] = useState<number>(0);
  const [recipeVersion, setRecipeVersion] = useState("v1.0");
  const [ingredients, setIngredients] = useState<Ingredient[]>([]);
  const [stock1L, setStock1L] = useState<number>(0);
  const [stock20L, setStock20L] = useState<number>(0);
  const [errors, setErrors] = useState<FieldErrors>({});
  const [availableSupplies, setAvailableSupplies] = useState<Supply[]>([]);

  useEffect(() => {
    suppliesService.getSuppliesByEstado("activos").then(setAvailableSupplies).catch(() => {});
  }, []);

  useEffect(() => {
    if (initialData) {
      setName(initialData.name || "");
      setDescription(initialData.description || "");
      setStandardBatch(initialData.standard_batch ?? 5);
      setUnit(initialData.unit || "Litros");
      setProductionTime(initialData.production_time ?? 60);
      setPreparationCost(initialData.preparation_cost ?? 0);
      setSuggestedPrice(initialData.suggested_price ?? 0);
      if (initialData.recipe) {
        setRecipeVersion(initialData.recipe.version || "v1.0");
        setIngredients(initialData.recipe.ingredients || []);
      }
      if (initialData.formula_inventory) {
        const item1 = initialData.formula_inventory.find((i) => i.gallon_type === 1);
        const item20 = initialData.formula_inventory.find((i) => i.gallon_type === 20);
        if (item1) setStock1L(item1.stock_quantity);
        if (item20) setStock20L(item20.stock_quantity);
      }
    }
  }, [initialData]);

  const addIngredient = () =>
    setIngredients([...ingredients, { supply_id: "", supply_name: "", quantity_required: 1, percentage: 10 }]);

  const removeIngredient = (index: number) =>
    setIngredients(ingredients.filter((_, i) => i !== index));

  const updateIngredient = (index: number, field: keyof Ingredient, value: string | number) => {
    const updated = [...ingredients];
    updated[index] = { ...updated[index], [field]: value };
    if (field === "supply_id") {
      const found = availableSupplies.find((s) => (s._id || s.id) === value);
      if (found) updated[index].supply_name = found.name;
    }
    setIngredients(updated);
  };

  const clearError = (field: string) =>
    setErrors((prev) => ({ ...prev, [field]: "" }));

  const validate = (): boolean => {
    const e: FieldErrors = {};
    if (!name.trim()) e.name = "El nombre es obligatorio.";
    else if (name.trim().length <= 10) e.name = "El nombre debe tener más de 10 caracteres.";
    else if (!ONLY_LETTERS.test(name.trim())) e.name = "El nombre debe contener solo letras, sin números ni símbolos especiales.";

    if (!description.trim()) e.description = "La descripción es obligatoria.";
    else if (description.trim().length <= 10) e.description = "La descripción debe tener más de 10 caracteres.";
    else if (!ONLY_LETTERS.test(description.trim())) e.description = "La descripción debe contener solo letras, sin números ni símbolos.";

    if (standardBatch <= 0) e.standardBatch = "El lote estándar debe ser un número positivo mayor a 0.";
    if (productionTime <= 0) e.productionTime = "El tiempo de producción debe ser mayor a 0.";
    if (preparationCost < 0) e.preparationCost = "El costo de preparación no puede ser negativo.";
    if (suggestedPrice <= 0) e.suggestedPrice = "El precio sugerido debe ser mayor a 0.";

    if (!recipeVersion.trim() || recipeVersion.trim().length <= 2)
      e.recipeVersion = "La versión de la receta debe tener más de 2 caracteres (ej: v1.0).";

    ingredients.forEach((ing, idx) => {
      if (!ing.supply_id) e[`ing_supply_${idx}`] = "Debe seleccionar un insumo.";
      if (!ing.quantity_required || ing.quantity_required <= 0) e[`ing_qty_${idx}`] = "La cantidad debe ser mayor a 0.";
      if (!ing.percentage || ing.percentage <= 0) e[`ing_pct_${idx}`] = "El porcentaje debe ser mayor a 0.";
    });

    if (stock1L < 0) e.stock1L = "El stock no puede ser negativo.";
    if (stock20L < 0) e.stock20L = "El stock no puede ser negativo.";

    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (ev: React.FormEvent) => {
    ev.preventDefault();
    if (!validate()) return;
    const payload: Partial<Formula> = {
      name, description,
      standard_batch: Number(standardBatch),
      unit,
      production_time: Number(productionTime),
      preparation_cost: Number(preparationCost),
      suggested_price: Number(suggestedPrice),
      status: initialData ? (initialData.status ?? true) : true,
      recipe: {
        version: recipeVersion,
        ingredients: ingredients.map((ing) => ({
          ...ing,
          quantity_required: Number(ing.quantity_required),
          percentage: Number(ing.percentage),
        })),
      },
      formula_inventory: [
        { gallon_type: 1, stock_quantity: Number(stock1L), last_update: new Date().toISOString().split("T")[0] },
        { gallon_type: 20, stock_quantity: Number(stock20L), last_update: new Date().toISOString().split("T")[0] },
      ],
    };
    await onSubmit(payload);
  };

  const ec = (f: string) => (errors[f] ? "form-input input-error" : "form-input");

  return (
    <form className="product-form" onSubmit={handleSubmit} noValidate>
      <div className="form-grid-2">
        <div className="form-group">
          <label className="form-label">Nombre de la Fórmula *</label>
          <input type="text" className={ec("name")} placeholder="Ej. BioFertil Max Plus Agro"
            value={name} onChange={(e) => { setName(e.target.value); clearError("name"); }} />
          {errors.name && <span className="field-error">{errors.name}</span>}
        </div>
        <div className="form-group">
          <label className="form-label">Unidad de Medida *</label>
          <select className="form-select" value={unit} onChange={(e) => setUnit(e.target.value)}>
            <option value="Litros">Litros</option>
            <option value="Toneladas">Toneladas</option>
            <option value="Kilogramos">Kilogramos</option>
          </select>
        </div>
      </div>

      <div className="form-group">
        <label className="form-label">Descripción *</label>
        <textarea className={errors.description ? "form-textarea input-error" : "form-textarea"} rows={2}
          placeholder="Descripción de la fórmula o instrucciones generales..."
          value={description} onChange={(e) => { setDescription(e.target.value); clearError("description"); }} />
        {errors.description && <span className="field-error">{errors.description}</span>}
      </div>

      <div className="form-grid-2">
        <div className="form-group">
          <label className="form-label">Lote Estándar *</label>
          <input type="number" className={ec("standardBatch")} value={standardBatch} min="1"
            onChange={(e) => { setStandardBatch(Number(e.target.value)); clearError("standardBatch"); }} />
          {errors.standardBatch && <span className="field-error">{errors.standardBatch}</span>}
        </div>
        <div className="form-group">
          <label className="form-label">Tiempo Producción (min) *</label>
          <input type="number" className={ec("productionTime")} value={productionTime} min="1"
            onChange={(e) => { setProductionTime(Number(e.target.value)); clearError("productionTime"); }} />
          {errors.productionTime && <span className="field-error">{errors.productionTime}</span>}
        </div>
      </div>

      <div className="form-grid-2">
        <div className="form-group">
          <label className="form-label">Costo de Preparación (S/) *</label>
          <input type="number" step="0.01" className={ec("preparationCost")} value={preparationCost} min="0"
            onChange={(e) => { setPreparationCost(Number(e.target.value)); clearError("preparationCost"); }} />
          {errors.preparationCost && <span className="field-error">{errors.preparationCost}</span>}
        </div>
        <div className="form-group">
          <label className="form-label">Precio Sugerido Venta (S/) *</label>
          <input type="number" step="0.01" className={ec("suggestedPrice")} value={suggestedPrice} min="0.01"
            onChange={(e) => { setSuggestedPrice(Number(e.target.value)); clearError("suggestedPrice"); }} />
          {errors.suggestedPrice && <span className="field-error">{errors.suggestedPrice}</span>}
        </div>
      </div>

      <div className="form-subcard">
        <div className="subcard-header">
          <div>
            <h4 className="subcard-title">Especificaciones de la Receta</h4>
            <span className="subcard-subtitle">Insumos requeridos para la preparación</span>
          </div>
          <div style={{ display: "flex", gap: "8px", alignItems: "flex-start" }}>
            <div className="form-group" style={{ margin: 0 }}>
              <input type="text"
                className={errors.recipeVersion ? "form-input form-input-sm input-error" : "form-input form-input-sm"}
                style={{ width: "100px" }} value={recipeVersion} placeholder="Versión"
                onChange={(e) => { setRecipeVersion(e.target.value); clearError("recipeVersion"); }} />
              {errors.recipeVersion && <span className="field-error" style={{ fontSize: "10px" }}>{errors.recipeVersion}</span>}
            </div>
            <Button type="button" variant="outline" size="sm" icon={<Plus size={14} />} onClick={addIngredient}>
              Insumo
            </Button>
          </div>
        </div>

        <div className="subcard-body">
          {ingredients.map((ing, idx) => (
            <div key={idx} className="ingredient-row-extended">
              <div className="form-group" style={{ flex: "1 1 180px", margin: 0 }}>
                <select
                  className={errors[`ing_supply_${idx}`] ? "form-select input-error" : "form-select"}
                  value={ing.supply_id || ""}
                  onChange={(e) => { updateIngredient(idx, "supply_id", e.target.value); clearError(`ing_supply_${idx}`); }}
                >
                  <option value="">-- Seleccionar Insumo --</option>
                  {availableSupplies.map((s) => (
                    <option key={s._id || s.id} value={s._id || s.id}>{s.name}</option>
                  ))}
                </select>
                {errors[`ing_supply_${idx}`] && <span className="field-error">{errors[`ing_supply_${idx}`]}</span>}
              </div>

              <div className="form-group" style={{ flex: "0 0 130px", margin: 0 }}>
                <input type="text" className="form-input" value={ing.supply_name} readOnly
                  placeholder="Nombre auto" style={{ background: "var(--bg-secondary)", cursor: "not-allowed", opacity: 0.7 }} />
              </div>

              <div className="form-group" style={{ flex: "0 0 110px", margin: 0 }}>
                <input type="number" step="0.01" min="0.01"
                  className={errors[`ing_qty_${idx}`] ? "form-input input-error" : "form-input"}
                  placeholder="Cantidad" value={ing.quantity_required}
                  onChange={(e) => { updateIngredient(idx, "quantity_required", Number(e.target.value)); clearError(`ing_qty_${idx}`); }} />
                {errors[`ing_qty_${idx}`] && <span className="field-error">{errors[`ing_qty_${idx}`]}</span>}
              </div>

              <div className="form-group" style={{ flex: "0 0 80px", margin: 0 }}>
                <input type="number" step="0.1" min="0.1"
                  className={errors[`ing_pct_${idx}`] ? "form-input input-error" : "form-input"}
                  placeholder="%" value={ing.percentage}
                  onChange={(e) => { updateIngredient(idx, "percentage", Number(e.target.value)); clearError(`ing_pct_${idx}`); }} />
                {errors[`ing_pct_${idx}`] && <span className="field-error">{errors[`ing_pct_${idx}`]}</span>}
              </div>

              <button type="button" className="btn-icon-danger" onClick={() => removeIngredient(idx)} title="Eliminar insumo"
                style={{ alignSelf: "flex-start", marginTop: "2px" }}>
                <Trash2 size={16} />
              </button>
            </div>
          ))}
          {ingredients.length === 0 && <p className="empty-hint">No hay insumos añadidos a la receta.</p>}
        </div>
      </div>

      <div className="form-subcard">
        <h4 className="subcard-title">Inventario de Presentaciones Envasadas</h4>
        <div className="form-grid-2" style={{ marginTop: "8px" }}>
          <div className="form-group">
            <label className="form-label">Stock Presentación 1 Litro (Galón tipo 1)</label>
            <input type="number" className={ec("stock1L")} value={stock1L} min="0"
              onChange={(e) => { setStock1L(Number(e.target.value)); clearError("stock1L"); }} />
            {errors.stock1L && <span className="field-error">{errors.stock1L}</span>}
          </div>
          <div className="form-group">
            <label className="form-label">Stock Presentación 20 Litros (Galón tipo 20)</label>
            <input type="number" className={ec("stock20L")} value={stock20L} min="0"
              onChange={(e) => { setStock20L(Number(e.target.value)); clearError("stock20L"); }} />
            {errors.stock20L && <span className="field-error">{errors.stock20L}</span>}
          </div>
        </div>
      </div>

      <div className="form-actions">
        <Button type="button" variant="outline" onClick={onCancel}>Cancelar</Button>
        <Button type="submit" variant="primary" isLoading={isSaving}>
          {initialData ? "Actualizar Fórmula" : "Registrar Fórmula"}
        </Button>
      </div>
    </form>
  );
};
