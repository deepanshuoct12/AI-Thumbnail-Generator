import { Frame } from './frame.model';

export interface Video {
  id: string;
  filename: string;
  fileFormat: string;
  status: string;
  style: string;
  resolution: string;
  count: number;
  frames: Frame[];
}
