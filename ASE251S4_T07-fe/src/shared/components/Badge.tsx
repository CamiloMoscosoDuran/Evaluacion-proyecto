import React from 'react';

interface BadgeProps {
  status: boolean | string;
  label?: string;
}

export const Badge: React.FC<BadgeProps> = ({ status, label }) => {
  let type = 'neutral';
  let text = label || String(status);

  if (typeof status === 'boolean') {
    type = status ? 'success' : 'danger';
    text = label || (status ? 'Activo' : 'Inactivo');
  } else if (typeof status === 'string') {
    const s = status.toUpperCase();
    if (s === 'A' || s === 'ACTIVO' || s === 'COMPLETA' || s === 'COMPLETADO') {
      type = 'success';
      text = label || (s === 'A' ? 'Activo' : status);
    } else if (s === 'P' || s === 'PENDIENTE') {
      type = 'warning';
      text = label || 'Pendiente';
    } else if (s === 'I' || s === 'INACTIVO' || s === 'CANCELADA' || s === 'ANULADO') {
      type = 'danger';
      text = label || (s === 'I' ? 'Inactivo' : status);
    } else if (s === 'POSPUESTA') {
      type = 'info';
      text = label || 'Pospuesta';
    }
  }

  return <span className={`badge badge-${type}`}>{text}</span>;
};
