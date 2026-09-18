import { Link } from 'react-router-dom';
import { useAdminEquipments, useAdminRooms } from '@features/admin/application/useAdminCatalog';
import { Card } from '@shared/ui/Card';
import { PageLoader } from '@shared/ui/Spinner';
import styles from './AdminHomePage.module.css';

export function AdminHomePage() {
  const rooms = useAdminRooms();
  const equipments = useAdminEquipments();

  return (
    <div className={styles.page}>
      <header>
        <h1 className={styles.title}>Quản trị phòng khám</h1>
        <p className={styles.subtitle}>Catalog dịch vụ, bác sĩ, lịch làm việc và tài nguyên.</p>
      </header>

      <div className={styles.grid}>
        <Card>
          <h2>Danh mục</h2>
          <ul className={styles.links}>
            <li>
              <Link to="/admin/services">Quản lý dịch vụ</Link>
            </li>
            <li>
              <Link to="/admin/doctors">Quản lý bác sĩ & lịch</Link>
            </li>
            <li>
              <Link to="/admin/audit">Nhật ký audit</Link>
            </li>
          </ul>
        </Card>

        <Card>
          <h2>Phòng ({rooms.data?.length ?? '…'})</h2>
          {rooms.isLoading && <PageLoader />}
          <ul className={styles.plain}>
            {rooms.data?.map((r) => (
              <li key={r.id}>
                {r.code} — {r.name} ({r.roomType})
              </li>
            ))}
          </ul>
        </Card>

        <Card>
          <h2>Thiết bị ({equipments.data?.length ?? '…'})</h2>
          {equipments.isLoading && <PageLoader />}
          <ul className={styles.plain}>
            {equipments.data?.map((e) => (
              <li key={e.id}>
                {e.code} — {e.name} ({e.equipmentType})
              </li>
            ))}
          </ul>
        </Card>
      </div>
    </div>
  );
}
