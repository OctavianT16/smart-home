import { Navigate } from "react-router-dom";
import { useAuth } from "./Components/auth/AuthContext";

type ProtectedRouteProps = {
  children: React.ReactNode;
  allowedRoles?: Array<"ADMIN" | "USER">;
};

export default function ProtectedRoute({
  children,
  allowedRoles,
}: ProtectedRouteProps) {
  const { user, loading } = useAuth();

  if (loading) {
    return <div className="container py-5">Se verifică sesiunea...</div>;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
}
