from fastapi import APIRouter
import time

from app.models.schemas import CompareRequest, CompareResponse, SentenceMatch
from app.services.preprocessing_service import PreprocessingService
from app.services.embedding_service import EmbeddingService
from app.services.similarity_service import SimilarityService

router = APIRouter()


@router.post("/compare", response_model=CompareResponse)
def compare_documents(request: CompareRequest):
    start_time = time.time()

    sentencesA = PreprocessingService.split_sentences(request.documentA)
    sentencesB = PreprocessingService.split_sentences(request.documentB)

    embeddingsA = EmbeddingService.encode(sentencesA)
    embeddingsB = EmbeddingService.encode(sentencesB)

    sim_matrix = SimilarityService.cosine_similarity_matrix(embeddingsA, embeddingsB)
    matches    = SimilarityService.find_best_matches(sentencesA, sentencesB, sim_matrix)
    overall    = SimilarityService.overall_similarity(sim_matrix)

    matched_sentences = [
        SentenceMatch(sentenceA=a, sentenceB=b, similarity=round(score * 100, 2))
        for a, b, score in matches
    ]

    processing_time_ms = (time.time() - start_time) * 1000

    return CompareResponse(
        overallSimilarity=round(overall, 2),
        matchedSentences=matched_sentences,
        sentenceCountA=len(sentencesA),
        sentenceCountB=len(sentencesB),
        processingTimeMs=round(processing_time_ms, 2)
    )