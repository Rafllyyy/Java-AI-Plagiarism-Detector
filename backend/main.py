from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.controllers.comparison_controller import router as comparison_router

app = FastAPI(
    title="Document Similarity AI Backend",
    description="Backend AI untuk deteksi plagiarisme dokumen menggunakan Sentence Transformer",
    version="1.0.0"
)

# CORS diaktifkan agar Java Swing (via HTTP client) bebas mengakses API ini
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(comparison_router)


@app.get("/")
def root():
    return {"status": "ok", "message": "Document Similarity AI Backend is running"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)