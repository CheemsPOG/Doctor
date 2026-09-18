import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { getAuthSession } from '@shared/lib/authSession';
import { homePathForRole, roleAllowed, type AppRole } from '@shared/lib/roles';

export function ProtectedRoute() {
  const session = getAuthSession();
  const location = useLocation();

  if (!session) {
    return <Navigate to="/auth/login" state={{ from: location }} replace />;
  }

  return <Outlet />;
}

type RoleRouteProps = {
  allow: AppRole[];
};

/** Restricts outlet to allowed roles; otherwise redirects to role home. */
export function RoleRoute({ allow }: RoleRouteProps) {
  const session = getAuthSession();
  const location = useLocation();

  if (!session) {
    return <Navigate to="/auth/login" state={{ from: location }} replace />;
  }

  if (!roleAllowed(session.role, allow)) {
    return <Navigate to={homePathForRole(session.role)} replace />;
  }

  return <Outlet />;
}
