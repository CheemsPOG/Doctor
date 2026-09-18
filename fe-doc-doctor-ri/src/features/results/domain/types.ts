export type ResultMediaType = 'IMAGE' | 'VIDEO' | string;

export type ResultAsset = {
  id: number;
  appointmentId: number | null;
  serviceId: number | null;
  mediaType: ResultMediaType;
  title: string;
  caption: string | null;
  url: string;
  thumbnailUrl: string | null;
  createdAt: string;
};
