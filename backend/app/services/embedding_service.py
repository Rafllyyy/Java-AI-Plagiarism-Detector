from typing import List
import numpy as np
from sentence_transformers import SentenceTransformer


class EmbeddingService:
    """
    Tanggung jawab: memuat model Sentence Transformer sekali saja (singleton),
    lalu mengubah kalimat menjadi vector embedding.
    """

    _model = None
    MODEL_NAME = "paraphrase-multilingual-MiniLM-L12-v2"

    @classmethod
    def get_model(cls) -> SentenceTransformer:
        if cls._model is None:
            # Model di-download otomatis saat pertama kali dipanggil,
            # lalu di-cache secara lokal oleh library sentence-transformers.
            cls._model = SentenceTransformer(cls.MODEL_NAME)
        return cls._model

    @classmethod
    def encode(cls, sentences: List[str]) -> np.ndarray:
        if not sentences:
            return np.array([])
        model = cls.get_model()
        embeddings = model.encode(
            sentences,
            convert_to_numpy=True,
            normalize_embeddings=True  # supaya dot product = cosine similarity
        )
        return embeddings