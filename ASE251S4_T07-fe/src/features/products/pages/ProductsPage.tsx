import React, { useState } from "react";
import { useProducts } from "../hooks/useProducts";
import { ProductTable } from "../components/ProductTable";
import { ProductForm } from "../components/ProductForm";
import { FormulaInventoryModal } from "../components/FormulaInventoryModal";
import { FormulaDetailModal } from "../components/FormulaDetailModal";
import { Modal } from "../../../shared/components/Modal";
import { Button } from "../../../shared/components/Button";
import { Formula } from "../types/product.types";
import { productsService } from "../services/productsService";
import { Plus, FileSpreadsheet, FileText, Upload, RefreshCw, Search } from "lucide-react";

export const ProductsPage: React.FC = () => {
  const { formulas, isLoading, error, filterStatus, setFilterStatus, reload, saveFormula, deleteFormula, restoreFormula } = useProducts();
  const [searchTerm, setSearchTerm] = useState("");
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedFormula, setSelectedFormula] = useState<Formula | null>(null);
  const [detailFormula, setDetailFormula] = useState<Formula | null>(null);
  const [inventoryFormula, setInventoryFormula] = useState<Formula | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [actionMessage, setActionMessage] = useState<string | null>(null);

  const handleOpenNew = () => { setSelectedFormula(null); setIsFormOpen(true); };
  const handleEdit = (f: Formula) => { setSelectedFormula(f); setIsFormOpen(true); };

  const handleSubmit = async (data: Partial<Formula>) => {
    setIsSaving(true);
    try {
      await saveFormula(data, selectedFormula?._id);
      setIsFormOpen(false);
      setActionMessage("Formula guardada exitosamente");
      setTimeout(() => setActionMessage(null), 3000);
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : "Error al guardar la formula");
    } finally { setIsSaving(false); }
  };

  const handleDelete = async (id: string) => {
    if (confirm("Desea desactivar esta formula?")) {
      try {
        await deleteFormula(id);
        setActionMessage("Formula desactivada correctamente");
        setTimeout(() => setActionMessage(null), 3000);
      } catch (err: unknown) { alert(err instanceof Error ? err.message : "Error al desactivar"); }
    }
  };

  const handleRestore = async (id: string) => {
    try {
      await restoreFormula(id);
      setActionMessage("Formula restaurada correctamente");
      setTimeout(() => setActionMessage(null), 3000);
    } catch (err: unknown) { alert(err instanceof Error ? err.message : "Error al restaurar"); }
  };

  const handleExportExcel = async () => { try { await productsService.exportExcel(filterStatus); } catch (e: unknown) { alert("Error: " + (e instanceof Error ? e.message : "")); } };
  const handleExportPdf = async () => { try { await productsService.exportPdf(filterStatus); } catch (e: unknown) { alert("Error: " + (e instanceof Error ? e.message : "")); } };
  const handleImportCsv = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try { await productsService.importCsv(file); alert("Importacion completada"); reload(); } catch (err: unknown) { alert("Error: " + (err instanceof Error ? err.message : "")); }
  };

  const filteredFormulas = formulas.filter((f) =>
    (f.name || "").toLowerCase().includes(searchTerm.toLowerCase()) ||
    (f._id || "").toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="page-container">
      <div className="page-header-row">
        <div>
          <h1 className="page-heading">Formulas y Productos</h1>
          <p className="page-subheading">Gestion de formulas maestras, recetas, costos e inventario de galones envasados (MongoDB)</p>
        </div>
        <div className="page-header-actions">
          <label className="btn btn-outline btn-md cursor-pointer" title="Importar CSV">
            <Upload size={16} /><span>Importar</span>
            <input type="file" accept=".csv" style={{ display: "none" }} onChange={handleImportCsv} />
          </label>
          <Button variant="outline" icon={<FileSpreadsheet size={16} />} onClick={handleExportExcel}>Excel</Button>
          <Button variant="outline" icon={<FileText size={16} />} onClick={handleExportPdf}>PDF</Button>
          <Button variant="primary" icon={<Plus size={16} />} onClick={handleOpenNew}>Nueva Formula</Button>
        </div>
      </div>

      {actionMessage && <div className="alert alert-success">{actionMessage}</div>}
      {error && <div className="alert alert-danger"><span>{error}</span><Button size="sm" variant="ghost" onClick={reload}>Reintentar</Button></div>}

      <div className="filter-bar">
        <div className="search-input-wrapper">
          <Search size={18} className="search-icon" />
          <input type="text" className="form-input search-input" placeholder="Buscar por nombre o ID de formula..."
            value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
        </div>
        <div className="filter-tabs">
          {(["todos", "activos", "inactivos"] as const).map((t) => (
            <button key={t} className={`filter-tab ${filterStatus === t ? "filter-tab-active" : ""}`}
              onClick={() => setFilterStatus(t)}>{t.charAt(0).toUpperCase() + t.slice(1)}</button>
          ))}
        </div>
        <Button variant="ghost" size="sm" icon={<RefreshCw size={15} />} onClick={reload} title="Refrescar" />
      </div>

      {isLoading ? (
        <div className="loading-container"><div className="spinner" /><span>Cargando formulas...</span></div>
      ) : (
        <ProductTable
          formulas={filteredFormulas}
          onEdit={handleEdit}
          onDelete={handleDelete}
          onRestore={handleRestore}
          onViewDetails={(f) => setDetailFormula(f)}
        />
      )}

      <Modal isOpen={isFormOpen} onClose={() => setIsFormOpen(false)}
        title={selectedFormula ? `Editar Formula: ${selectedFormula.name}` : "Registrar Nueva Formula"} maxWidth="800px">
        <ProductForm initialData={selectedFormula} onSubmit={handleSubmit} onCancel={() => setIsFormOpen(false)} isSaving={isSaving} />
      </Modal>

      <FormulaDetailModal formula={detailFormula} onClose={() => setDetailFormula(null)} />
      <FormulaInventoryModal formula={inventoryFormula} onClose={() => setInventoryFormula(null)} />
    </div>
  );
};
