from typing import List, Tuple
import numpy as np


class SimilarityService:
    """
    Tanggung jawab: menghitung cosine similarity antar embedding kalimat,
    mencari pasangan kalimat paling mirip, dan menghitung skor keseluruhan.
    """

    @staticmethod
    def cosine_similarity_matrix(embeddingsA: np.ndarray, embeddingsB: np.ndarray) -> np.ndarray:
        if embeddingsA.size == 0 or embeddingsB.size == 0:
            return np.zeros((0, 0))
        # Embedding sudah dinormalisasi -> dot product = cosine similarity
        return np.matmul(embeddingsA, embeddingsB.T)

    @staticmethod
    def find_best_matches(
        sentencesA: List[str],
        sentencesB: List[str],
        sim_matrix: np.ndarray
    ) -> List[Tuple[str, str, float]]:
        matches = []
        for i, sentA in enumerate(sentencesA):
            if sim_matrix.shape[1] == 0:
                break
            best_j = int(np.argmax(sim_matrix[i]))
            best_score = float(sim_matrix[i][best_j])
            matches.append((sentA, sentencesB[best_j], best_score))
        return matches

    @staticmethod
    def overall_similarity(sim_matrix: np.ndarray) -> float:
        if sim_matrix.size == 0:
            return 0.0
        row_max = np.max(sim_matrix, axis=1)
        return float(np.mean(row_max)) * 100