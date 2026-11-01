# Episode 1: Can Android Run AI Without the Internet? Gemma + RAG

This directory contains the foundational materials for **Episode 1** of the [On-Device RAG on Android with Gemma](https://simplifiedcoding.net/courses/on-device-rag-android-gemma) course.

In this episode, we answer the fundamental question: **Can modern Android devices run real Retrieval-Augmented Generation (RAG) completely offline without sending private data to cloud APIs?**

---

## Companion Assets

- **Presentation Deck:** [`episode-01-on-device-rag-android.pptx`](episode-01-on-device-rag-android.pptx) — The complete slide deck covering architecture, device tradeoffs, and the on-device AI landscape.

---

## Core Concept: The On-Device RAG Pipeline

Traditional AI applications act as simple thin clients wrapping cloud APIs (such as OpenAI, Anthropic, or Gemini Developer API). On-device RAG shifts the entire pipeline onto the user's physical Android device:

```text
Private Files (PDF / Docs)
          │
          ▼
   Text Extraction
          │
          ▼
 Overlapping Chunking
          │
          ▼
   Vector Embedding (EmbeddingGemma)
          │
          ▼
  Local Persistence (Room Database / Vector Index)
          │
  ┌───────┴────────────────────────┐
  │ Query submitted by user        │
  ▼                                ▼
Semantic Search (Cosine)    Lexical Search (BM25)
  └───────┬────────────────────────┘
          ▼
Hybrid Retrieval & Re-ranking (RRF)
          │
          ▼
 Context Engineering & Budgeting
          │
          ▼
On-Device LLM (Gemma via LiteRT LM)
          │
          ▼
Grounded Answer with Source Citations
```

---

## Why On-Device RAG Matters

1. **Absolute Data Privacy:**
   Sensitive files—such as medical lab results, financial contracts, tax returns, and private diaries—never leave the device. No cloud logging, no data telemetry, and no third-party data processing agreements required.

2. **Full Offline Availability:**
   Answers can be generated on airplanes, subways, remote locations, or during network outages without an active internet connection.

3. **Grounded, Verifiable Outputs:**
   Instead of relying on the base model's memory (which is prone to hallucination), Gemma answers strictly from retrieved evidence and provides explicit citation anchors (`[S1]`, `[S2]`).

4. **Zero API Costs & Quotas:**
   No monthly API bills, rate limits, or latency spikes caused by server queuing or network congestion.

---

## Real-World Mobile Constraints

Running AI on mobile requires navigating physical device boundaries:

- **RAM and OOM Kills:** Modern mobile operating systems strictly enforce process memory limits. Running an LLM requires balancing weights (2 GB+ for quantised 2B/E2B models), KV cache, and Android system processes.
- **Hardware Acceleration:** Leveraging GPU and NPU delegates via Google's LiteRT LM runtime to achieve acceptable time-to-first-token and decoding throughput.
- **Battery & Thermals:** Sustained generation generates heat and throttles clock speeds; inference must be budgeted and cancelable.
- **Storage Management:** Safely managing on-device downloads with integrity checks, pause/resume support, and storage validation.

---

## Course Roadmap

| Episode | Topic | Core Focus |
|---|---|---|
| **01** | **Viability & Architecture** *(Here)* | Theoretical foundations, tradeoffs, and system architecture. |
| **02** | **Running Gemma on Android** | Running Gemma locally using LiteRT LM and measuring performance. |
| **03** | **Semantic Search** | Vector embeddings, cosine similarity, and nearest-neighbor search with EmbeddingGemma. |
| **04** | **PDF Chunking & Indexing** | Ingesting PDFs, sliding-window chunking, and Room vector persistence. |
| **05** | **BM25 Hybrid Retrieval** | Combining semantic similarity with BM25 lexical keyword matching via Reciprocal Rank Fusion. |
| **06** | **Context Engineering** | Bounding token budgets, source citation blocks, prompt injection defense, and prompt export. |
