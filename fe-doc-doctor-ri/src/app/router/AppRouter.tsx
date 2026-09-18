import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { ProtectedRoute, RoleRoute } from './ProtectedRoute';
import { LandingPage } from '@pages/LandingPage';
import { LoginPage } from '@pages/auth/LoginPage';
import { RegisterPage } from '@pages/auth/RegisterPage';
import { PatientHomePage } from '@pages/patient/PatientHomePage';
import { ServicesPage } from '@pages/patient/ServicesPage';
import { BookingPage } from '@pages/patient/BookingPage';
import { AppointmentsPage } from '@pages/patient/AppointmentsPage';
import { ProfilePage } from '@pages/patient/ProfilePage';
import { NotificationsPage } from '@pages/patient/NotificationsPage';
import { SettingsPage } from '@pages/patient/SettingsPage';
import { ResultsPage } from '@pages/patient/ResultsPage';
import { ClinicQueuePage } from '@pages/clinic/ClinicQueuePage';
import { ClinicWalkInPage } from '@pages/clinic/ClinicWalkInPage';
import { DoctorSchedulePage } from '@pages/doctor/DoctorSchedulePage';
import { DoctorQueuePage } from '@pages/doctor/DoctorQueuePage';
import { AdminHomePage } from '@pages/admin/AdminHomePage';
import { AdminServicesPage } from '@pages/admin/AdminServicesPage';
import { AdminDoctorsPage } from '@pages/admin/AdminDoctorsPage';
import { AdminAuditPage } from '@pages/admin/AdminAuditPage';
import { PatientLayout } from '@widgets/patient-layout/PatientLayout';
import { ClinicLayout } from '@widgets/clinic-layout/ClinicLayout';
import { DoctorLayout } from '@widgets/doctor-layout/DoctorLayout';
import { AdminLayout } from '@widgets/admin-layout/AdminLayout';
import { AuthLayout } from '@widgets/auth-layout/AuthLayout';
import { getAuthSession } from '@shared/lib/authSession';
import { homePathForRole } from '@shared/lib/roles';

function NotFoundPage() {
  return (
    <div style={{ textAlign: 'center', padding: '4rem 2rem' }}>
      <h1 style={{ fontFamily: 'var(--font-display)', color: 'var(--color-sage)' }}>
        404
      </h1>
      <p style={{ color: 'var(--color-text-muted)' }}>Trang bạn tìm không tồn tại.</p>
      <a href="/" style={{ fontWeight: 600 }}>
        Về trang chủ
      </a>
    </div>
  );
}

function HomeRedirect() {
  const session = getAuthSession();
  if (!session) return <Navigate to="/auth/login" replace />;
  return <Navigate to={homePathForRole(session.role)} replace />;
}

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/app" element={<HomeRedirect />} />

        <Route element={<AuthLayout />}>
          <Route path="/auth/login" element={<LoginPage />} />
          <Route path="/auth/register" element={<RegisterPage />} />
        </Route>

        <Route element={<ProtectedRoute />}>
          <Route element={<RoleRoute allow={['PATIENT']} />}>
            <Route element={<PatientLayout />}>
              <Route path="/patient" element={<PatientHomePage />} />
              <Route path="/patient/services" element={<ServicesPage />} />
              <Route path="/patient/book" element={<BookingPage />} />
              <Route path="/patient/appointments" element={<AppointmentsPage />} />
              <Route path="/patient/results" element={<ResultsPage />} />
              <Route path="/patient/notifications" element={<NotificationsPage />} />
              <Route path="/patient/settings" element={<SettingsPage />} />
              <Route path="/patient/profile" element={<ProfilePage />} />
            </Route>
          </Route>

          <Route element={<RoleRoute allow={['RECEPTIONIST', 'NURSE', 'CLINIC_ADMIN', 'SYSTEM_ADMIN']} />}>
            <Route element={<ClinicLayout />}>
              <Route path="/clinic" element={<ClinicQueuePage />} />
              <Route path="/clinic/walk-in" element={<ClinicWalkInPage />} />
            </Route>
          </Route>

          <Route element={<RoleRoute allow={['DOCTOR', 'CLINIC_ADMIN', 'SYSTEM_ADMIN']} />}>
            <Route element={<DoctorLayout />}>
              <Route path="/doctor" element={<DoctorSchedulePage />} />
              <Route path="/doctor/queue" element={<DoctorQueuePage />} />
            </Route>
          </Route>

          <Route element={<RoleRoute allow={['CLINIC_ADMIN', 'SYSTEM_ADMIN']} />}>
            <Route element={<AdminLayout />}>
              <Route path="/admin" element={<AdminHomePage />} />
              <Route path="/admin/services" element={<AdminServicesPage />} />
              <Route path="/admin/doctors" element={<AdminDoctorsPage />} />
              <Route path="/admin/audit" element={<AdminAuditPage />} />
            </Route>
          </Route>
        </Route>

        <Route path="/auth" element={<Navigate to="/auth/login" replace />} />
        <Route path="/login" element={<Navigate to="/auth/login" replace />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  );
}
