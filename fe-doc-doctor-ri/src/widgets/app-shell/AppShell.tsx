import { useEffect, useId, useMemo, useRef, useState } from 'react';
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useNotifications } from '@features/notifications/application/useNotifications';
import { authApi } from '@features/auth/infrastructure/authApi';
import { clearAuthSession, getAuthSession } from '@shared/lib/authSession';
import { useLocale } from '@shared/i18n/LocaleProvider';
import styles from '@widgets/patient-layout/PatientLayout.module.css';

export type ShellNavItem = {
  to: string;
  label: string;
  end?: boolean;
};

type AppShellProps = {
  brandTo: string;
  navItems: ShellNavItem[];
  /** Links shown in the user avatar menu (profile, settings, …). */
  accountItems?: ShellNavItem[];
  /** When set, shows notification bell next to avatar. */
  notificationsPath?: string;
  footerNote?: string;
};

function initials(fullName: string | undefined): string {
  if (!fullName?.trim()) return 'U';
  const parts = fullName.trim().split(/\s+/);
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase();
}

function NotificationBell({ to }: { to: string }) {
  const { t } = useLocale();
  const location = useLocation();
  const { data } = useNotifications();
  const unread = useMemo(
    () => data?.filter((n) => !n.read).length ?? 0,
    [data],
  );
  const active = location.pathname === to || location.pathname.startsWith(`${to}/`);

  return (
    <Link
      to={to}
      className={`${styles.bellBtn} ${active ? styles.bellActive : ''}`}
      aria-label={
        unread > 0
          ? t(`Thông báo (${unread} chưa đọc)`, `Notifications (${unread} unread)`)
          : t('Thông báo', 'Notifications')
      }
    >
      <span className={styles.bellIcon} aria-hidden>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
          <path
            d="M12 22a2.2 2.2 0 0 0 2.2-2.2h-4.4A2.2 2.2 0 0 0 12 22Zm7-6.2V11a7 7 0 1 0-14 0v4.8L3 17.8V19h18v-1.2l-2-2Z"
            fill="currentColor"
          />
        </svg>
      </span>
      {unread > 0 && (
        <span className={styles.bellBadge}>{unread > 99 ? '99+' : unread}</span>
      )}
    </Link>
  );
}

export function AppShell({
  brandTo,
  navItems,
  accountItems = [],
  notificationsPath,
  footerNote,
}: AppShellProps) {
  const session = getAuthSession();
  const navigate = useNavigate();
  const { t } = useLocale();
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);
  const menuId = useId();

  useEffect(() => {
    if (!menuOpen) return;
    const onPointer = (e: MouseEvent) => {
      if (!menuRef.current?.contains(e.target as Node)) setMenuOpen(false);
    };
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setMenuOpen(false);
    };
    document.addEventListener('mousedown', onPointer);
    document.addEventListener('keydown', onKey);
    return () => {
      document.removeEventListener('mousedown', onPointer);
      document.removeEventListener('keydown', onKey);
    };
  }, [menuOpen]);

  const handleLogout = async () => {
    setMenuOpen(false);
    await authApi.logout(session?.refreshToken);
    clearAuthSession();
    navigate('/');
  };

  return (
    <div className={styles.layout}>
      <header className={styles.header}>
        <div className={styles.headerInner}>
          <Link to={brandTo} className={styles.brand}>
            <span className={styles.brandIcon} aria-hidden>
              ✦
            </span>
            <span className={styles.brandText}>Doctor Ri</span>
          </Link>

          <nav className={styles.nav} aria-label={t('Điều hướng chính', 'Main navigation')}>
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) =>
                  `${styles.navLink} ${isActive ? styles.navLinkActive : ''}`
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>

          <div className={styles.userArea}>
            {session ? (
              <>
                {notificationsPath && <NotificationBell to={notificationsPath} />}

                <div className={styles.avatarWrap} ref={menuRef}>
                  <button
                    type="button"
                    className={styles.avatarBtn}
                    aria-haspopup="menu"
                    aria-expanded={menuOpen}
                    aria-controls={menuId}
                    onClick={() => setMenuOpen((v) => !v)}
                  >
                    <span className={styles.avatar} aria-hidden>
                      {initials(session.fullName)}
                    </span>
                    <span className={styles.avatarMeta}>
                      <span className={styles.avatarName}>
                        {session.fullName.split(' ').pop()}
                      </span>
                      <span className={styles.avatarRole}>{session.role}</span>
                    </span>
                  </button>

                  {menuOpen && (
                    <div id={menuId} className={styles.menu} role="menu">
                      <div className={styles.menuHeader}>
                        <strong>{session.fullName}</strong>
                        <span>{session.email}</span>
                      </div>
                      {accountItems.map((item) => (
                        <Link
                          key={item.to}
                          to={item.to}
                          role="menuitem"
                          className={styles.menuItem}
                          onClick={() => setMenuOpen(false)}
                        >
                          {item.label}
                        </Link>
                      ))}
                      <button
                        type="button"
                        role="menuitem"
                        className={`${styles.menuItem} ${styles.menuDanger}`}
                        onClick={() => void handleLogout()}
                      >
                        {t('Đăng xuất', 'Sign out')}
                      </button>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <Link to="/auth/login" className={styles.navLink}>
                {t('Đăng nhập', 'Sign in')}
              </Link>
            )}
          </div>
        </div>
      </header>

      <main className={styles.main}>
        <Outlet />
      </main>

      <footer className={styles.footer}>
        <p>{footerNote ?? 'Doctor Ri Clinic — Đồng hành cùng hành trình làm mẹ'}</p>
      </footer>
    </div>
  );
}
