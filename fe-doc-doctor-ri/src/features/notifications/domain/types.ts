export type NotificationItem = {
  id: number;
  type: string;
  title: string;
  content: string;
  referenceType: string | null;
  referenceId: number | null;
  read: boolean;
  readAt: string | null;
  createdAt: string;
};

export type NotificationPreference = {
  notificationType: string;
  inAppEnabled: boolean;
  emailEnabled: boolean;
  smsEnabled: boolean;
  pushEnabled: boolean;
};
