import { useMyResultAssets } from '@features/results/application/useResults';
import { Card } from '@shared/ui/Card';
import { PageLoader } from '@shared/ui/Spinner';
import { Link } from 'react-router-dom';
import styles from './ResultsPage.module.css';

export function ResultsPage() {
  const { data, isLoading, isError, error } = useMyResultAssets();

  const images = data?.filter((a) => a.mediaType === 'IMAGE') ?? [];
  const videos = data?.filter((a) => a.mediaType === 'VIDEO') ?? [];

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1 className={styles.title}>Đánh giá kết quả</h1>
        <p className={styles.subtitle}>
          Hình ảnh và video đính kèm sau siêu âm / tư vấn kết quả. Nếu chưa có tệp, mục tương ứng
          sẽ ẩn. Đặt lịch tư vấn tại{' '}
          <Link to="/patient/services">Dịch vụ khám</Link>.
        </p>
      </header>

      {isLoading && <PageLoader />}
      {isError && (
        <p className={styles.error} role="alert">
          {error.message}
        </p>
      )}

      {!isLoading && data?.length === 0 && (
        <Card>
          <p className={styles.empty}>
            Chưa có hình ảnh hoặc video kết quả. Sau khi phòng khám tải tệp (hoặc sau buổi siêu âm /
            tư vấn), nội dung sẽ xuất hiện tại đây.
          </p>
        </Card>
      )}

      {images.length > 0 && (
        <section className={styles.section} aria-labelledby="images-heading">
          <h2 id="images-heading" className={styles.sectionTitle}>
            Hình ảnh
          </h2>
          <div className={styles.mediaGrid}>
            {images.map((asset) => (
              <figure key={asset.id} className={styles.figure}>
                <a href={asset.url} target="_blank" rel="noreferrer">
                  <img
                    src={asset.thumbnailUrl || asset.url}
                    alt={asset.title}
                    loading="lazy"
                  />
                </a>
                <figcaption>
                  <strong>{asset.title}</strong>
                  {asset.caption && <span>{asset.caption}</span>}
                </figcaption>
              </figure>
            ))}
          </div>
        </section>
      )}

      {videos.length > 0 && (
        <section className={styles.section} aria-labelledby="videos-heading">
          <h2 id="videos-heading" className={styles.sectionTitle}>
            Video
          </h2>
          <div className={styles.videoList}>
            {videos.map((asset) => (
              <Card key={asset.id}>
                <h3 className={styles.videoTitle}>{asset.title}</h3>
                {asset.caption && <p className={styles.caption}>{asset.caption}</p>}
                <video className={styles.video} controls preload="metadata" src={asset.url}>
                  Trình duyệt không hỗ trợ phát video.
                </video>
              </Card>
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
