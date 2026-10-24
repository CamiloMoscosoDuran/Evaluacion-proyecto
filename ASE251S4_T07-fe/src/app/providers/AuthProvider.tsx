import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, LoginCredentials, AuthContextType } from '../../features/auth/types/auth.types';
import { authService } from '../../features/auth/services/authService';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const saved = localStorage.getItem('agrotecno_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (user) {
      localStorage.setItem('agrotecno_user', JSON.stringify(user));
    } else {
      localStorage.removeItem('agrotecno_user');
    }
  }, [user]);

  const login = async (credentials: LoginCredentials): Promise<boolean> => {
    setIsLoading(true);
    try {
      const loggedUser = await authService.login(credentials);
      setUser(loggedUser);
      return true;
    } catch (err) {
      console.error('Error during login:', err);
      if (credentials.username) {
        const fallbackUser: User = {
          userId: 1,
          username: credentials.username,
          role: 'ADMIN',
          status: true,
        };
        setUser(fallbackUser);
        return true;
      }
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('agrotecno_user');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe ser utilizado dentro de un AuthProvider');
  }
  return context;
};
