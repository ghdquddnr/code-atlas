# 0001. Backend MVP Uses Deterministic Analysis First

Date: 2026-07-04

## Status

Accepted

## Context

CodeAtlas must avoid behaving like a generic source-code chatbot. The first milestone needs verified facts about a Java/Spring/MyBatis project before any AI explanation is introduced.

## Decision

The backend MVP extracts facts deterministically:

- Source files are indexed from the registered project path.
- Spring APIs are extracted from Java AST parsing with JavaParser.
- MyBatis statements are extracted with an XML parser.
- SQL table names are estimated from common SQL clauses.
- Extracted facts are persisted and exposed through REST APIs.

AI documentation and Q&A will be added only after this factual analysis layer is useful.

## Consequences

The MVP is narrower, but easier to verify. The sample project and integration test now act as the baseline for future analyzer changes.

## Current Follow-up

The MVP includes deterministic Markdown documentation and rule-based Q&A built only from stored analysis facts. These deterministic outputs remain available as the fallback path whenever LLM enhancement is disabled or unavailable.

## LLM Integration Update

Local Ollama integration was added as an optional enhancement layer. The deterministic analysis result remains the source of truth. The LLM receives only extracted APIs, Mapper statements, tables, and flow evidence, and the application falls back to deterministic output when the local model is disabled or unavailable.
