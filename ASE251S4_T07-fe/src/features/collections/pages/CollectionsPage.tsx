import React, { useState } from 'react';
import { useCollections } from '../hooks/useCollections';
import { CollectionTable } from '../components/CollectionTable';
import { InstallmentModal } from '../components/InstallmentModal';
import { PaymentCollection } from '../types/collection.types';
import { Button } from '../../../shared/components/Button';
import { RefreshCw, Search } from 'lucide-react';

export const CollectionsPage: React.FC = () => {
  const { collections, isLoading, error, reload } = useCollections();
  const [searchTerm, setSearchTerm] = useState('');
  const [activeCollection, setActiveCollection] = useState<PaymentCollection | null>(null);

  const filtered = collections.filter((col) => {
    const term = searchTerm.toLowerCase();
    const id = String(col.collectionId).toLowerCase();
    const order = String(col.orderId).toLowerCase();
    const customer = (col.mongoCustomerId || '').toLowerCase();
    return id.includes(term) || order.includes(term) || customer.includes(term);
  });

  return (
    <div className="page-container">
      <div className="page-header-row">
        <div>
          <h1 className="page-heading">Cobranzas y Cuotas</h1>
          <p className="page-subheading">
            Control financiero de pagos en cuotas asociados a las órdenes de venta (SQL Server)
          </p>
        </div>
      </div>

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
            placeholder="Buscar por cobranza, orden o cliente..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
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
          <span>Cargando cobranzas desde SQL Server...</span>
        </div>
      ) : (
        <CollectionTable
          collections={filtered}
          onViewInstallments={(col) => setActiveCollection(col)}
        />
      )}

      <InstallmentModal
        collection={activeCollection}
        onClose={() => {
          setActiveCollection(null);
          reload();
        }}
      />
    </div>
  );
};
