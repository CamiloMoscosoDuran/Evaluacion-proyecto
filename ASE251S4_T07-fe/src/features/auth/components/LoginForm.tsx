import React, { useState } from 'react';
import { Button } from '../../../shared/components/Button';
import { useAuth } from '../hooks/useAuth';
import { useNavigate } from 'react-router-dom';
import { User, Lock, AlertCircle } from 'lucide-react';

export const LoginForm: React.FC = () => {
  const { login, isLoading } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('admin123');
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!username.trim()) {
      setError('Por favor ingrese el usuario');
      return;
    }
    const success = await login({ username, password });
    if (success) {
      navigate('/dashboard');
    } else {
      setError('Credenciales incorrectas');
    }
  };

  return (
    <form className="login-form" onSubmit={handleSubmit}>
      {error && (
        <div className="login-error-alert">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      <div className="form-group">
        <label className="form-label" htmlFor="username">Usuario</label>
        <div className="input-with-icon">
          <User size={18} className="input-icon" />
          <input
            id="username"
            type="text"
            className="form-input"
            placeholder="Ej. admin"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
      </div>

      <div className="form-group">
        <label className="form-label" htmlFor="password">Contraseña</label>
        <div className="input-with-icon">
          <Lock size={18} className="input-icon" />
          <input
            id="password"
            type="password"
            className="form-input"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>
      </div>

      <Button
        type="submit"
        variant="primary"
        size="lg"
        isLoading={isLoading}
        className="btn-block"
      >
        Iniciar Sesión
      </Button>
    </form>
  );
};
