# Building an on-device RAG app on Android with Gemma

Build a private, offline Retrieval-Augmented Generation (RAG) system for Android with Gemma and LiteRT LM. This comprehensive course covers local inference, document indexing, embeddings, retrieval, grounded answers, evaluation, and real-device constraints.

**[Explore the free course](https://simplifiedcoding.net/courses/on-device-rag-android-gemma)** · **[Watch the course promo](https://www.youtube.com/watch?v=8j_KOA8Y4bU)**

[![Building an on-device RAG app on Android with Gemma course promo](images/on-device-rag-android-gemma-promo.jpg)](https://www.youtube.com/watch?v=8j_KOA8Y4bU)

---

## About the Course

This course follows the engineering of a complete on-device RAG app, from private files to answers backed by retrieved evidence. The local path works completely offline without an internet connection after the required model assets are available and the files have been indexed.

The core pipeline is:

```text
Private files ──► Text chunks ──► Embeddings ──► Retrieval ──► Context ──► Gemma ──► Grounded answer
```

The goal is not another cloud API wrapper. You will learn how the system works, where it fails, how to measure it, and what must change before an on-device AI feature is ready for real users.

---

## Repository Structure

```text
on-device-rag-android/
│
├── 1-can-android-run-ai-without-the-internet-gemma-rag/
│   ├── episode-01-on-device-rag-android.pptx   # Course intro & architecture slides
│   └── README.md                              # Theoretical foundations & trade-offs
│
├── 2-can-you-run-gemma-4-directly-on-android/
│   ├── ondevice-llm-android/                  # Reference project: Pocket AI + Performance Overlay
│   ├── starter-project/                       # Hands-on interactive project with specs
│   └── README.md                              # LiteRT LM, Gemma 4 E2B, HF token setup
│
├── 3-build-semantic-search-on-android-with-embeddinggemma/
│   ├── semantic-search-android/               # Demo app: Literal phrase vs. Semantic vector search
│   ├── images/                                # Screenshots & comparison charts
│   └── README.md                              # Cosine similarity, Top-K, thresholds
│
├── 4-how-to-chunk-pdfs-for-rag-on-android-without-losing-context/
│   ├── rag-document-lab/                      # RAG Document Lab: PDF extraction & Room persistence
│   ├── starter-project/                       # Starter project template with spec
│   └── README.md                              # Sliding-window chunking & vector storage
│
├── 5-build-hybrid-search-for-rag-on-android-with-bm25/
│   ├── rag-document-lab/                      # Extended app: Hybrid retrieval (BM25 + Semantic)
│   └── README.md                              # BM25 scoring & Reciprocal Rank Fusion (RRF)
│
├── 6-context-engineering-for-rag-on-android-with-gemma/
│   ├── rag-document-lab/                      # Complete Context Lab & Prompt Builder
│   └── README.md                              # Token budgeting, source blocks, prompt export
│
├── images/
│   └── on-device-rag-android-gemma-promo.jpg  # Banner image
├── .gitignore                                 # Git ignore rules for Android, models, & caches
└── README.md                                  # Repository documentation & course guide
```

---

## Course Episodes

| Episode | Directory | Topic | Key Concepts |
|---|---|---|---|
| **01** | [`1-can-android-run-ai-without-the-internet-gemma-rag`](1-can-android-run-ai-without-the-internet-gemma-rag) | **Can Android Run AI Without the Internet?** | Offline viability, system constraints (RAM, battery, thermals), privacy advantages. |
| **02** | [`2-can-you-run-gemma-4-directly-on-android`](2-can-you-run-gemma-4-directly-on-android) | **Run Gemma Directly on Android** | Google LiteRT LM, Hugging Face model gating, resumable downloads, streaming tokens. |
| **03** | [`3-build-semantic-search-on-android-with-embeddinggemma`](3-build-semantic-search-on-android-with-embeddinggemma) | **Semantic Search with EmbeddingGemma** | 768D vector embeddings, Cosine similarity, Top-K, threshold rejection. |
| **04** | [`4-how-to-chunk-pdfs-for-rag-on-android-without-losing-context`](4-how-to-chunk-pdfs-for-rag-on-android-without-losing-context) | **PDF Chunking for RAG** | Android PDF text extraction, sliding-window chunking (800 chars / 120 overlap), Room DB vectors. |
| **05** | [`5-build-hybrid-search-for-rag-on-android-with-bm25`](5-build-hybrid-search-for-rag-on-android-with-bm25) | **Hybrid Search with BM25** | Lexical BM25 ranking, combining with semantic search via Reciprocal Rank Fusion (RRF). |
| **06** | [`6-context-engineering-for-rag-on-android-with-gemma`](6-context-engineering-for-rag-on-android-with-gemma) | **Context Engineering with Gemma** | Bounding token budgets, source citation blocks (`[S1]`), prompt injection defense, prompt export. |

---

## What You Will Learn

- **Run Gemma locally on Android** with Google LiteRT LM.
- **Manage model lifecycles:** Initialization, inference, concurrency, cancellation, and storage.
- **Extract and chunk text** from private PDF files for optimal retrieval.
- **Create embeddings** with Google's EmbeddingGemma and query local vector storage.
- **Combine retrieval signals:** Semantic embeddings, lexical BM25, and Reciprocal Rank Fusion (RRF).
- **Context Engineering:** Build compact, bounded prompts that fit into mobile LLM context windows.
- **Grounded answer generation:** Ensure answers cite evidence sources (`[S1]`, `[S2]`) and resist prompt injection.
- **Separate failures:** Distinguish retrieval failures from model generation failures.
- **Device constraints:** Manage memory pressure, thermal throttling, and battery drain across physical devices.

---

## Why On-Device RAG Matters

- **Absolute Privacy:** Files, chunks, and embeddings never leave the device.
- **100% Offline Resilience:** Operates without a network connection once models are indexed.
- **Grounded Output:** Gemma receives selected evidence instead of hallucinating from parametric memory.
- **Deterministic Engineering Control:** You control chunking, ranking, thresholds, citations, and evaluation.
- **Zero API Billing:** No per-token cloud costs, rate limits, or latency spikes.

---

## Prerequisites & Environment Setup

### Tools & SDKs
- **Android Studio:** Android Studio Ladybug (2024.2+) or Android Studio 2026+
- **JDK:** JDK 17 or JDK 25 (bundled JBR in Android Studio)
- **Compile / Target SDK:** 37 (Build Tools 36.0.0+)
- **Minimum SDK:** Android 8.0 (API 26) or Android 13 (API 33 for Pocket AI)

### Model Assets Setup

#### 1. Gemma LLM (Episode 2)
The Gemma 4 model is downloaded from Hugging Face:
1. Accept the model terms on Hugging Face: [gemma-4-E2B-it-litert-lm](https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm).
2. Create a `local.properties` file in `2-can-you-run-gemma-4-directly-on-android/starter-project/` (or `ondevice-llm-android/`):
   ```properties
   HF_TOKEN=your_hugging_face_token_here
   ```

#### 2. EmbeddingGemma (Episodes 3, 4, 5, 6)
EmbeddingGemma creates 768-dimensional float vectors on-device:
1. Obtain the `embedding_gemma.task` file.
2. Place the model bundle in:
   ```text
   <episode-project>/app/src/main/assets/embedding_gemma.task
   ```
   *(See `rag-document-lab/app/src/main/assets/README.md` for details).*

---

## Building and Testing

Each episode project includes standard Gradle wrapper scripts:

```powershell
# Run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug
```

---

## About the Instructor

The course is created by **[Belal Khan](https://github.com/probelalkhan)**, a Google Developer Expert for Android and the creator of [Simplified Coding](https://simplifiedcoding.net). It is based on practical decisions, real device limitations, and production-tested patterns for on-device AI.
