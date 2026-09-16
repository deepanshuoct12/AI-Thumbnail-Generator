from fastapi import FastAPI, File, UploadFile, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from PIL import Image
import numpy as np
import mediapipe as mp
from io import BytesIO
import uvicorn

app = FastAPI(title="AI Thumbnail Scorer")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

mp_face_detection = mp.solutions.face_detection.FaceDetection(min_detection_confidence=0.3)


def analyze(image: Image.Image, style: str = "natural") -> dict:
    img = image.convert("RGB")
    arr = np.array(img)
    img_h, img_w = arr.shape[:2]
    gray = np.array(img.convert("L"))

    mean_brightness = float(gray.mean())
    brightness_score = max(0.0, 1.0 - abs(mean_brightness - 128) / 128)

    contrast = float(gray.std()) / 128.0
    contrast_score = min(contrast, 1.0)

    dx = np.gradient(gray, axis=1)
    dy = np.gradient(gray, axis=0)
    gradient = np.sqrt(dx ** 2 + dy ** 2)
    sharpness = float(gradient.var()) / 1000.0
    sharpness_score = min(sharpness, 1.0)

    r = arr[:, :, 0].astype(float)
    g = arr[:, :, 1].astype(float)
    b = arr[:, :, 2].astype(float)
    rg = np.abs(r - g)
    yb = np.abs(0.5 * (r + g) - b)
    colorfulness = float(np.sqrt(np.mean(rg ** 2) + np.mean(yb ** 2))) / 100.0
    colorfulness_score = min(colorfulness, 1.0)

    rgb = arr
    results = mp_face_detection.process(rgb)
    face_score = 0.0
    if results and results.detections:
        for det in results.detections:
            bbox = det.location_data.relative_bounding_box
            area = bbox.width * bbox.height
            face_score += area * det.score[0]
    face_score = min(face_score, 1.0)

    brightness_for_final = brightness_score
    if style == "dark":
        brightness_for_final = (255 - mean_brightness) / 255.0
    elif style == "bright":
        brightness_for_final = mean_brightness / 255.0

    weights = {
        "natural": (0.25, 0.20, 0.15, 0.15, 0.25),
        "bright": (0.15, 0.20, 0.10, 0.10, 0.15),
        "dark": (0.15, 0.40, 0.10, 0.10, 0.15),
        "colorful": (0.15, 0.10, 0.10, 0.50, 0.15),
        "sharp": (0.50, 0.10, 0.20, 0.10, 0.10),
        "face-focus": (0.15, 0.10, 0.10, 0.10, 0.55),
    }
    style_weights = weights.get(style, weights["natural"])

    final = (
        style_weights[0] * sharpness_score +
        style_weights[1] * brightness_for_final +
        style_weights[2] * contrast_score +
        style_weights[3] * colorfulness_score +
        style_weights[4] * face_score
    )

    return {
        "score": round(final * 100, 2),
        "sharpness": round(sharpness_score * 100, 2),
        "brightness": round(brightness_score * 100, 2),
        "contrast": round(contrast_score * 100, 2),
        "colorfulness": round(colorfulness_score * 100, 2),
        "face_score": round(face_score * 100, 2),
        "width": img_w,
        "height": img_h,
    }


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/score")
async def score(file: UploadFile = File(...), style: str = Query("natural")):
    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="File must be an image")
    contents = await file.read()
    image = Image.open(BytesIO(contents))
    return analyze(image, style)


@app.post("/score/batch")
async def score_batch(files: list[UploadFile] = File(...), style: str = Query("natural")):
    results = []
    for f in files:
        contents = await f.read()
        image = Image.open(BytesIO(contents))
        r = analyze(image, style)
        r["filename"] = f.filename
        results.append(r)
    return {"results": results}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
