import { createContext, useContext, useEffect, useState } from "react";
import api from "../../api.ts";

type Role = "ADMIN" | "USER";

type AuthUser = {
  username: string;
  role: Role;
};

type AuthContextType = {
  user: AuthUser | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  isAdmin: boolean;
  isUser: boolean;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkCurrentUser();
  }, []);

  async function checkCurrentUser() {
    try {
      const response = await api.get<AuthUser>("/api/auth/user");
      setUser(response.data);
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  }

  async function login(username: string, password: string) {
    const response = await api.post<AuthUser>("/api/auth/login", {
      username,
      password,
    });

    setUser(response.data);
  }

  async function logout() {
    await api.post("/api/auth/logout");
    setUser(null);
  }

  const isAdmin = user?.role === "ADMIN";
  const isUser = user?.role === "USER";

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        logout,
        isAdmin,
        isUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth not initialized - AuthProvider");
  }

  return context;
}
