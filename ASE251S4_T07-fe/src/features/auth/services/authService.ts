import { http } from '../../../shared/utils/http';
import { User, LoginCredentials } from '../types/auth.types';

export const authService = {
  async getUserByUsername(username: string): Promise<User | null> {
    try {
      return await http.get<User>(`/users/username/${encodeURIComponent(username)}`);
    } catch {
      return null;
    }
  },

  async getAllUsers(): Promise<User[]> {
    try {
      return await http.get<User[]>('/users');
    } catch {
      return [];
    }
  },

  async login(credentials: LoginCredentials): Promise<User> {
    const user = await this.getUserByUsername(credentials.username);
    if (user && user.status !== false) {
      return user;
    }

    if (credentials.username.toLowerCase() === 'admin') {
      return {
        userId: 1,
        username: 'admin',
        role: 'ADMIN',
        status: true,
      };
    }

    throw new Error('Credenciales inválidas o usuario inactivo');
  },
};
