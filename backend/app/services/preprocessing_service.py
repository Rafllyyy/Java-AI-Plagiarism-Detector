import re
from typing import List


class PreprocessingService:
    """
    Tanggung jawab: membersihkan teks mentah dan memecahnya
    menjadi daftar kalimat, sebelum masuk ke tahap embedding.
    """

    @staticmethod
    def clean_text(text: str) -> str:
        text = text.strip()
        text = re.sub(r"\s+", " ", text)  # normalisasi whitespace/newline berlebih
        return text

    @staticmethod
    def split_sentences(text: str) -> List[str]:
        text = PreprocessingService.clean_text(text)
        if not text:
            return []

        # Split berdasarkan tanda titik/tanya/seru yang diikuti spasi
        raw_sentences = re.split(r"(?<=[.!?])\s+", text)
        sentences = [s.strip() for s in raw_sentences if s.strip()]
        return sentences