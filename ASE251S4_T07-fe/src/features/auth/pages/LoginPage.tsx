import React from 'react';
import { LoginForm } from '../components/LoginForm';
import { Sprout } from 'lucide-react';

export const LoginPage: React.FC = () => {
  return (
    <div className="login-page-container">
      <div className="login-card">
        <div className="login-header">
          <div className="login-logo-badge">
            <Sprout size={32} />
          </div>
          <h1 className="login-title">AgroTecno</h1>
          <p className="login-desc">Gestión de Fórmulas, Cultivos y Cobranzas</p>
        </div>

        <LoginForm />

        <div className="login-footer">
          <span>Conectado a Spring Boot API en <code>localhost:8082</code></span>
        </div>
      </div>
    </div>
  );
};
