import { useState } from "react";
import type { FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../Components/auth/AuthContext";

export default function LoginPage() {
  const { login, user } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();

    setError("");
    setSubmitting(true);

    try {
      await login(username, password);
      navigate("/");
    } catch {
      setError("Username sau parolă incorectă.");
    } finally {
      setSubmitting(false);
    }
  }

  if (user) {
    navigate("/");
    return null;
  }

  return (
    <div className="container d-flex justify-content-center align-items-center min-vh-100">
      <div
        className="card shadow-sm p-4"
        style={{ width: "100%", maxWidth: "420px" }}
      >
        <h3 className="mb-3 text-center">Smart Home Login</h3>

        {error && <div className="alert alert-danger py-2">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Utilizator</label>
            <input
              type="text"
              className="form-control"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              autoComplete="username"
            />
          </div>

          <div className="mb-3">
            <label className="form-label">Parolă</label>
            <input
              type="password"
              className="form-control"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary w-100"
            disabled={submitting}
          >
            {submitting ? "Se autentifică..." : "Autentificare"}
          </button>
        </form>
      </div>
    </div>
  );
}
