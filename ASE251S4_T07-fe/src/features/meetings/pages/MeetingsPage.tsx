import React, { useState } from 'react';
import { useMeetings } from '../hooks/useMeetings';
import { MeetingTable } from '../components/MeetingTable';
import { MeetingForm } from '../components/MeetingForm';
import { MeetingDetailModal } from '../components/MeetingDetailModal';
import { Modal } from '../../../shared/components/Modal';
import { Button } from '../../../shared/components/Button';
import { FieldMeeting } from '../types/meeting.types';
import { Plus, RefreshCw, Search } from 'lucide-react';

export const MeetingsPage: React.FC = () => {
  const {
    meetings,
    isLoading,
    error,
    filterStatus,
    setFilterStatus,
    reload,
    saveMeeting,
    deleteMeeting,
    restoreMeeting,
  } = useMeetings();

  const [searchTerm, setSearchTerm] = useState('');
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedMeeting, setSelectedMeeting] = useState<FieldMeeting | null>(null);
  const [detailMeeting, setDetailMeeting] = useState<FieldMeeting | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [actionMessage, setActionMessage] = useState<string | null>(null);

  const handleOpenNew = () => {
    setSelectedMeeting(null);
    setIsFormOpen(true);
  };

  const handleEdit = (m: FieldMeeting) => {
    setSelectedMeeting(m);
    setIsFormOpen(true);
  };

  const handleSubmit = async (data: Partial<FieldMeeting>) => {
    setIsSaving(true);
    try {
      const id = selectedMeeting?.id || selectedMeeting?._id;
      await saveMeeting(data, id);
      setIsFormOpen(false);
      setActionMessage('Visita de campo guardada correctamente');
      setTimeout(() => setActionMessage(null), 3000);
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : 'Error al guardar la visita');
    } finally {
      setIsSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (confirm('¿Desea cancelar y eliminar esta visita de campo?')) {
      try {
        await deleteMeeting(id);
        setActionMessage('Visita cancelada correctamente');
        setTimeout(() => setActionMessage(null), 3000);
      } catch (err: unknown) {
        alert(err instanceof Error ? err.message : 'Error al cancelar la visita');
      }
    }
  };

  const handleRestore = async (id: string) => {
    try {
      await restoreMeeting(id);
      setActionMessage('Visita restaurada a estado Pendiente');
      setTimeout(() => setActionMessage(null), 3000);
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : 'Error al restaurar la visita');
    }
  };

  const filtered = meetings.filter((m) => {
    const term = searchTerm.toLowerCase();
    const id = (m.id || m._id || '').toLowerCase();
    const cust = (m.customer_id || '').toLowerCase();
    const farm = (m.farm_name || '').toLowerCase();
    return id.includes(term) || cust.includes(term) || farm.includes(term);
  });

  return (
    <div className="page-container">
      <div className="page-header-row">
        <div>
          <h1 className="page-heading">Visitas de Campo y Fidelización</h1>
          <p className="page-subheading">
            Seguimiento agronómico de parcelas, monitoreo de calidad del fruto y citas (MongoDB)
          </p>
        </div>

        <div className="page-header-actions">
          <Button
            variant="primary"
            icon={<Plus size={16} />}
            onClick={handleOpenNew}
          >
            Agendar Visita
          </Button>
        </div>
      </div>

      {actionMessage && (
        <div className="alert alert-success">{actionMessage}</div>
      )}

      {error && (
        <div className="alert alert-danger">
          <span>{error}</span>
          <Button size="sm" variant="ghost" onClick={reload}>Reintentar</Button>
        </div>
      )}

      <div className="filter-bar">
        <div className="search-input-wrapper">
          <Search size={18} className="search-icon" />
          <input
            type="text"
            className="form-input search-input"
            placeholder="Buscar por cliente, fundo o ID visita..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        <div className="filter-tabs">
          {['Todos', 'Pendiente', 'Completa', 'Pospuesta', 'Cancelada'].map((st) => (
            <button
              key={st}
              className={`filter-tab ${filterStatus === st ? 'filter-tab-active' : ''}`}
              onClick={() => setFilterStatus(st)}
            >
              {st}
            </button>
          ))}
        </div>

        <Button
          variant="ghost"
          size="sm"
          icon={<RefreshCw size={15} />}
          onClick={reload}
          title="Refrescar datos"
        />
      </div>

      {isLoading ? (
        <div className="loading-container">
          <div className="spinner" />
          <span>Cargando visitas de campo desde MongoDB...</span>
        </div>
      ) : (
        <MeetingTable
          meetings={filtered}
          onEdit={handleEdit}
          onDelete={handleDelete}
          onRestore={handleRestore}
          onViewDetails={(m) => setDetailMeeting(m)}
        />
      )}

      <Modal
        isOpen={isFormOpen}
        onClose={() => setIsFormOpen(false)}
        title={selectedMeeting ? `Editar Visita: ${selectedMeeting.id || selectedMeeting._id}` : 'Programar Visita de Campo'}
        maxWidth="750px"
      >
        <MeetingForm
          initialData={selectedMeeting}
          onSubmit={handleSubmit}
          onCancel={() => setIsFormOpen(false)}
          isSaving={isSaving}
        />
      </Modal>

      <MeetingDetailModal
        meeting={detailMeeting}
        onClose={() => setDetailMeeting(null)}
      />
    </div>
  );
};
