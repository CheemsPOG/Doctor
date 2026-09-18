import type { ReactNode } from 'react';
import styles from './Card.module.css';

type CardProps = {
  children: ReactNode;
  className?: string;
  onClick?: () => void;
  selected?: boolean;
};

export function Card({ children, className = '', onClick, selected = false }: CardProps) {
  const Tag = onClick ? 'button' : 'div';
  return (
    <Tag
      type={onClick ? 'button' : undefined}
      className={`${styles.card} ${selected ? styles.selected : ''} ${onClick ? styles.clickable : ''} ${className}`}
      onClick={onClick}
    >
      {children}
    </Tag>
  );
}
