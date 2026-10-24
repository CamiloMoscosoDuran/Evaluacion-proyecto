import React, { useState } from "react";
import { useCustomers } from "../hooks/useCustomers";
import { CustomerTable } from "../components/CustomerTable";
import { CustomerForm } from "../components/CustomerForm";
import { CustomerDetailModal } from "../components/CustomerDetailModal";
import { Modal } from "../../../shared/components/Modal";
import { Button } from "../../../shared/components/Button";
import { Customer } from "../types/customer.types";
import { customersService } from "../services/customersService";
import { Plus, FileSpreadsheet, FileText, Upload, RefreshCw, Search } from "lucide-react";

export const CustomersPage: React.FC = () => {
  const { customers, isLoading, error, filterStatus, setFilterStatus, reload, saveCustomer, deleteCustomer, restoreCustomer } = useCustomers();
  const [searchTerm, setSearchTerm] = useState("");
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  const [detailCustomer, setDetailCustomer] = useState<Customer | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [actionMessage, setActionMessage] = useState<string | null>(null);

  const handleOpenNew = () => { setSelectedCustomer(null); setIsFormOpen(true); };
  const handleEdit = (c: Customer) => { setSelectedCustomer(c); setIsFormOpen(true); };

  const handleSubmit = async (data: Partial<Customer>) => {
    setIsSaving(true);
    try {
      const id = selectedCustomer?.id || selectedCustomer?._id;
      await saveCustomer(data, id);
      setIsFormOpen(false);
      setActionMessage("Cliente guardado exitosamente");
      setTimeout(() => setActionMessage(null), 3000);
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : "Error al guardar el cliente");
    } finally { setIsSaving(false); }
  };

  const handleDelete = async (id: string) => {
    if (confirm("Desea desactivar este cliente?")) {
      try { await deleteCustomer(id); setActionMessage("Cliente desactivado correctamente"); setTimeout(() => setActionMessage(null), 3000); }
      catch (err: unknown) { alert(err instanceof Error ? err.message : "Error al desactivar"); }
    }
  };

  const handleRestore = async (id: string) => {
    try { await restoreCustomer(id); setActionMessage("Cliente restaurado correctamente"); setTimeout(() => setActionMessage(null), 3000); }
    catch (err: unknown) { alert(err instanceof Error ? err.message : "Error al restaurar"); }
  };

  const handleExportExcel = async () => { try { await customersService.exportExcel(filterStatus); } catch (e: unknown) { alert("Error: " + (e instanceof Error ? e.message : "")); } };
  const handleExportPdf = async () => { try { await customersService.exportPdf(filterStatus); } catch (e: unknown) { alert("Error: " + (e instanceof Error ? e.message : "")); } };
  const handleImportCsv = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]; if (!file) return;
    try { await customersService.importCsv(file); alert("Importacion completada"); reload(); } catch (err: unknown) { alert("Error: " + (err instanceof Error ? err.message : "")); }
  };

  const filteredCustomers = customers.filter((c) => {
    const term = searchTerm.toLowerCase();
    return (c.id || c._id || "").toLowerCase().includes(term) ||
      `${c.first_name || ""} ${c.last_name || ""}`.toLowerCase().includes(term) ||
      (c.document_number || "").toLowerCase().includes(term);
  });

  return (
    <div className="page-container">
      <div className="page-header-row">
        <div>
          <h1 className="page-heading">Clientes y Parcelas</h1>
          <p className="page-subheading">Directorio maestro de clientes agricolas, ubicacion geografica y parcelas cultivadas (MongoDB)</p>
        </div>
        <div className="page-header-actions">
          <label className="btn btn-outline btn-md cursor-pointer" title="Importar CSV">
            <Upload size={16} /><span>Importar</span>
            <input type="file" accept=".csv" style={{ display: "none" }} onChange={handleImportCsv} />
          </label>
          <Button variant="outline" icon={<FileSpreadsheet size={16} />} onClick={handleExportExcel}>Excel</Button>
          <Button variant="outline" icon={<FileText size={16} />} onClick={handleExportPdf}>PDF</Button>
          <Button variant="primary" icon={<Plus size={16} />} onClick={handleOpenNew}>Nuevo Cliente</Button>
        </div>
      </div>

      {actionMessage && <div className="alert alert-success">{actionMessage}</div>}
      {error && <div className="alert alert-danger"><span>{error}</span><Button size="sm" variant="ghost" onClick={reload}>Reintentar</Button></div>}

      <div className="filter-bar">
        <div className="search-input-wrapper">
          <Search size={18} className="search-icon" />
          <input type="text" className="form-input search-input" placeholder="Buscar por nombre, documento o ID..."
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
        <div className="loading-container"><div className="spinner" /><span>Cargando clientes...</span></div>
      ) : (
        <CustomerTable
          customers={filteredCustomers}
          onEdit={handleEdit}
          onDelete={handleDelete}
          onRestore={handleRestore}
          onViewDetails={(c) => setDetailCustomer(c)}
        />
      )}

      <Modal isOpen={isFormOpen} onClose={() => setIsFormOpen(false)}
        title={selectedCustomer ? `Editar Cliente: ${selectedCustomer.first_name} ${selectedCustomer.last_name}` : "Registrar Nuevo Cliente"} maxWidth="850px">
        <CustomerForm initialData={selectedCustomer} onSubmit={handleSubmit} onCancel={() => setIsFormOpen(false)} isSaving={isSaving} />
      </Modal>

      <CustomerDetailModal customer={detailCustomer} onClose={() => setDetailCustomer(null)} />
    </div>
  );
};
