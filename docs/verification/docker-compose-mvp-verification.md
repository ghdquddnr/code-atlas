# Docker Compose MVP Verification

Date: 2026-07-05

## Environment

- App: `http://localhost:8080`
- PostgreSQL host port: `15432`
- Sample path inside container: `/samples/legacy-spring-mybatis`

## Verified Flow

1. Built and started Docker Compose services.
2. Registered sample project with source path `/samples/legacy-spring-mybatis`.
3. Ran analysis with `POST /api/projects/1/analyze`.
4. Verified dashboard, graph, table usage, Q&A, and generated onboarding document.

## Verified Results

Dashboard:

```text
Project: docker-sample
Status: COMPLETED
Source files: 8
Spring APIs: 5
MyBatis statements: 5
API flows: 7
Tables: TB_ORDER, TB_ORDER_ITEM, TB_PRODUCT
```

Graph:

```text
Nodes: 23
Edges: 24
```

Q&A example:

```text
Question: TB_ORDER 어디서 사용돼?
Confidence: HIGH
Related APIs:
- GET /api/orders
- POST /api/orders
- DELETE /api/orders/{orderId}
- GET /api/orders/{orderId}
- PUT /api/orders/{orderId}/status
```

Document generation:

```text
Generated: docker-sample Onboarding Guide
Format: MARKDOWN
Evidence count: 17
```

## Notes

The initial Docker run failed because host port `5432` was already unavailable. Docker Compose now publishes PostgreSQL on host port `15432` by default while the app still connects to PostgreSQL through the internal Docker network at `postgres:5432`.
