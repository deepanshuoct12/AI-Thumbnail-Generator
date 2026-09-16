export interface Frame {
  id: string;
  videoId: string;
  framePath: string;
  timestampSeconds: number;
  score: number;
  sharpness: number;
  brightness: number;
  contrast: number;
  colorfulness: number;
  faceScore: number;
}
