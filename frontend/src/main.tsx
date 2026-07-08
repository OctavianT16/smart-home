import React from "react";
import ReactDOM from "react-dom/client";
import { RouterProvider, createBrowserRouter } from "react-router-dom";
import Layout from "./Layout";
import { Dht22LivePage } from "./pages/Dht22LivePage";
import "bootstrap/dist/css/bootstrap.min.css";
import "./Layout.css";
import Lights from "./pages/Lights";
import TapoPlugPage from "./pages/TapoPlugPage";
import { AuthProvider } from "./Components/auth/AuthContext";
import ProtectedRoute from "./ProtectedRoute";
import LoginPage from "./pages/LoginPage";
import ScenesPage from "./pages/ScenePage";
import DehumidifierPage from "./pages/DehumidifierPage";
import AutomationsPage from "./pages/AutomationsPage";

const router = createBrowserRouter([
  {
    path: "/login",
    element: <LoginPage />,
  },
  {
    element: (
      <ProtectedRoute allowedRoles={["ADMIN", "USER"]}>
        <Layout />
      </ProtectedRoute>
    ),
    children: [
      { path: "/", element: <Dht22LivePage /> },
      { path: "/lights", element: <Lights /> },
      { path: "/tapo", element: <TapoPlugPage /> },
      { path: "/scenes", element: <ScenesPage /> },
      { path: "/automation", element: <AutomationsPage /> },
      { path: "/dehumidifier", element: <DehumidifierPage /> },
    ],
  },
]);

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  </React.StrictMode>,
);
