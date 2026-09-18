import { Link } from 'react-router-dom';
import { Button } from '@shared/ui/Button';
import styles from './LandingPage.module.css';

export function LandingPage() {
  return (
    <div className={styles.page}>
      <div className={styles.bgShape1} aria-hidden="true" />
      <div className={styles.bgShape2} aria-hidden="true" />
      <div className={styles.bgShape3} aria-hidden="true" />

      <header className={styles.header}>
        <span className={styles.logoMark}>🌿</span>
        <span className={styles.logoText}>Doctor Ri Clinic</span>
      </header>

      <section className={styles.hero}>
        <p className={`${styles.eyebrow} animate-fade-in-up`}>Phòng khám Sản — Phụ khoa</p>

        <h1 className={`${styles.brandHero} animate-fade-in-up animate-delay-1`}>
          Doctor Ri
        </h1>

        <p className={`${styles.headline} animate-fade-in-up animate-delay-2`}>
          Đồng hành cùng mẹ bầu trên mỗi cột mốc — từ thai kỳ đến ngày con chào đời
        </p>

        <p className={`${styles.support} animate-fade-in-up animate-delay-3`}>
          Đặt lịch khám nhanh chóng, theo dõi lịch hẹn và chăm sóc sức khỏe mẹ bé
          trong không gian ấm áp, riêng tư.
        </p>

        <div className={`${styles.ctaGroup} animate-fade-in-up animate-delay-3`}>
          <Link to="/patient/book">
            <Button size="lg">Đặt lịch khám</Button>
          </Link>
          <Link to="/auth/login">
            <Button variant="outline" size="lg">
              Đăng nhập
            </Button>
          </Link>
        </div>
      </section>

      <section className={styles.features}>
        <div className={styles.feature}>
          <span className={styles.featureIcon}>🤰</span>
          <h3>Chăm sóc thai kỳ</h3>
          <p>Theo dõi sức khỏe mẹ và bé với đội ngũ bác sĩ giàu kinh nghiệm.</p>
        </div>
        <div className={styles.feature}>
          <span className={styles.featureIcon}>📅</span>
          <h3>Đặt lịch dễ dàng</h3>
          <p>Chọn dịch vụ, bác sĩ và khung giờ phù hợp chỉ trong vài bước.</p>
        </div>
        <div className={styles.feature}>
          <span className={styles.featureIcon}>💚</span>
          <h3>Không gian ấm áp</h3>
          <p>Phòng khám thiết kế dành riêng cho mẹ bầu — nhẹ nhàng và an tâm.</p>
        </div>
      </section>
    </div>
  );
}
