import React, { useState } from "react";
import { useSupplies } from "../hooks/useSupplies";
import { SupplyTable } from "../components/SupplyTable";
import { SupplyForm } from "../components/SupplyForm";
import { SupplyDetailModal } from "../components/SupplyDetailModal";
import { Modal } from "../../../shared/components/Modal";
import { Button } from "../../../shared/components/Button";
import { Supply } from "../types/supply.types";
import { suppliesService } from "../services/suppliesService";
import { Plus, FileSpreadsheet, FileText, Upload, RefreshCw, Search } from "lucide-react";

export const SuppliesPage: React.FC = () => {
  const { supplies, isLoading, error, filterStatus, setFilterStatus, reload, saveSupply, deleteSupply, restoreSupply } = useSupplies();
  const [searchTerm, setSearchTerm] = useState("");
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedSupply, setSelectedSupply] = useState<Supply | null>(null);
  const [detailSupply, setDetailSupply] = useState<Supply | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [actionMessage, setActionMessage] = useState<string | null>(null);

  const handleOpenNew = () => { setSelectedSupply(null); setIsFormOpen(true); };
  const handleEdit = (s: Supply) => { setSelectedSupply(s); setIsFormOpen(true); };

  const handleSubmit = async (data: Partial<Supply>) => {
    setIsSaving(true);
    try {
      const id = selectedSupply?.id || selectedSupply?._id;
      await saveSupply(data, id);
      setIsFormOpen(false);
      setActionMessage("Insumo guardado exitosamente");
      setTimeout(() => setActionMessage(null), 3000);
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : "Error al guardar el insumo");
    } finally { setIsSaving(false); }
  };

  const handleDelete = async (id: string) => {
    if (confirm("Desea desactivar este insumo?")) {
      try { await deleteSupply(id); setActionMessage("Insumo desactivado correctamente"); setTimeout(() => setActionMessage(null), 3000); }
      catch (err: unknown) { alert(err instanceof Error ? err.message : "Error al desactivar"); }
    }
  };

  const handleRestore = async (id: string) => {
    try { await restoreSupply(id); setActionMessage("Insumo restaurado correctamente"); setTimeout(() => setActionMessage(null), 3000); }
    catch (err: unknown) { alert(err instanceof Error ? err.message : "Error al restaurar"); }
  };

  const handleExportExcel = async () => { try { await suppliesService.exportExcel(filterStatus); } catch (e: unknown) { alert("Error: " + (e instanceof Error ? e.message : "")); } };
  const handleExportPdf = async () => { try { await suppliesService.exportPdf(filterStatus); } catch (e: unknown) { alert("Error: " + (e instanceof Error ? e.message : "")); } };
  const handleImportCsv = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]; if (!file) return;
    try { await suppliesService.importCsv(file); alert("Importacion completada"); reload(); } catch (err: unknown) { alert("Error: " + (err instanceof Error ? err.message : "")); }
  };

  const filteredSupplies = supplies.filter((s) => {
    const term = searchTerm.toLowerCase();
    return (s.id || s._id || "").toLowerCase().includes(term) ||
      (s.name || "").toLowerCase().includes(term) ||
      (s.category || "").toLowerCase().includes(term);
  });

  return (
    <div className="page-container">
      <div className="page-header-row">
        <div>
          <h1 className="page-heading">Insumos y Materias Primas</h1>
          <p className="page-subheading">Control de inventarios, existencias minimas y fechas de caducidad en almacen (MongoDB)</p>
        </div>
        <div className="page-header-actions">
          <label className="btn btn-outline btn-md cursor-pointer" title="Importar CSV">
            <Upload size={16} /><span>Importar</span>
            <input type="file" accept=".csv" style={{ display: "none" }} onChange={handleImportCsv} />
          </label>
          <Button variant="outline" icon={<FileSpreadsheet size={16} />} onClick={handleExportExcel}>Excel</Button>
          <Button variant="outline" icon={<FileText size={16} />} onClick={handleExportPdf}>PDF</Button>
          <Button variant="primary" icon={<Plus size={16} />} onClick={handleOpenNew}>Nuevo Insumo</Button>
        </div>
      </div>

      {actionMessage && <div className="alert alert-success">{actionMessage}</div>}
      {error && <div className="alert alert-danger"><span>{error}</span><Button size="sm" variant="ghost" onClick={reload}>Reintentar</Button></div>}

      <div className="filter-bar">
        <div className="search-input-wrapper">
          <Search size={18} className="search-icon" />
          <input type="text" className="form-input search-input" placeholder="Buscar por nombre, categoria o ID..."
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
        <div className="loading-container"><div className="spinner" /><span>Cargando insumos...</span></div>
      ) : (
        <SupplyTable
          supplies={filteredSupplies}
          onEdit={handleEdit}
          onDelete={handleDelete}
          onRestore={handleRestore}
          onViewDetails={(s) => setDetailSupply(s)}
        />
      )}

      <Modal isOpen={isFormOpen} onClose={() => setIsFormOpen(false)}
        title={selectedSupply ? `Editar Insumo: ${selectedSupply.name}` : "Registrar Nuevo Insumo"} maxWidth="750px">
        <SupplyForm initialData={selectedSupply} onSubmit={handleSubmit} onCancel={() => setIsFormOpen(false)} isSaving={isSaving} />
      </Modal>

      <SupplyDetailModal supply={detailSupply} onClose={() => setDetailSupply(null)} />
    </div>
  );
};
