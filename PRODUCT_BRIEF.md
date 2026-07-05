# PRODUCT_BRIEF.md

# CodeAtlas

## 한 줄 소개

**CodeAtlas는 문서가 부족한 Java/Spring 레거시 프로젝트를 분석하여 API, 서비스 흐름, MyBatis SQL, 테이블 사용처, 온보딩 문서를 자동 생성하는 AI 기반 개발자 도구입니다.**

---

## 왜 만들었는가?

한국 SI/SM 개발 환경에서는 10년 이상 운영된 레거시 프로젝트가 많습니다.

현장에서 자주 발생하는 문제는 다음과 같습니다.

* 문서가 최신화되어 있지 않음
* 담당자가 퇴사하여 히스토리를 알기 어려움
* API 문서와 실제 코드가 다름
* MyBatis XML SQL이 복잡함
* DB 테이블 사용처를 찾기 어려움
* 신규 투입자가 프로젝트를 이해하는 데 시간이 오래 걸림
* 기능 변경 시 영향도 분석이 어렵음

CodeAtlas는 이런 문제를 해결하기 위해 만들었습니다.

---

## 핵심 가치

CodeAtlas의 핵심 가치는 단순한 AI 챗봇이 아닙니다.

핵심은 다음입니다.

> 정적 분석으로 사실을 수집하고, AI로 이해하기 쉽게 설명한다.

즉, AI가 마음대로 추측하지 않도록 실제 코드, API, SQL, 테이블 정보를 먼저 분석합니다.

---

## 주요 기능

### 1. Spring API 자동 추출

Controller의 Mapping Annotation을 분석해 API 목록을 생성합니다.

예:

```text
POST /api/orders
GET /api/orders/{orderId}
PUT /api/orders/{orderId}/status
DELETE /api/orders/{orderId}
```

---

### 2. Controller-Service-Mapper 흐름 분석

API가 어떤 Service와 Mapper를 거쳐 DB에 접근하는지 보여줍니다.

예:

```text
OrderController.createOrder()
→ OrderService.createOrder()
→ OrderMapper.insertOrder()
→ TB_ORDER
```

---

### 3. MyBatis SQL 분석

MyBatis XML 파일을 분석합니다.

수집 정보:

* namespace
* statement id
* SQL type
* SQL body
* 사용 테이블
* Mapper interface 연결

---

### 4. 테이블 사용처 분석

특정 테이블이 어느 API, Service, Mapper, SQL에서 사용되는지 확인할 수 있습니다.

예:

```text
TB_USER 사용처:
- UserMapper.selectUserById
- UserMapper.updateUserLoginTime
- AdminUserMapper.searchUsers
```

---

### 5. AI 온보딩 문서 생성

신규 개발자를 위한 문서를 자동 생성합니다.

예:

* 프로젝트 개요
* 주요 모듈 설명
* API 목록
* 주문 흐름 설명
* 회원 흐름 설명
* DB 테이블 사용처
* 유지보수 주의사항

---

### 6. AI 질의응답

개발자가 자연어로 질문할 수 있습니다.

예:

```text
Q. 주문 생성 로직 어디서 봐야 하나요?

A. 주문 생성 로직은 OrderController.createOrder()에서 시작합니다.
이 메서드는 OrderService.createOrder()를 호출하고,
OrderMapper.insertOrder()를 통해 TB_ORDER에 주문 데이터를 저장합니다.
```

---

## 기술 스택

### Backend

* Java 21
* Spring Boot 3.x
* Gradle
* PostgreSQL
* Docker Compose

### Analyzer

* JavaParser or equivalent AST parser
* XML parser for MyBatis
* SQL table extractor
* Graph builder

### AI

* OpenAI API or local LLM
* RAG optional
* Evidence-based response generation

### Frontend

* React
* TypeScript
* Tailwind CSS
* shadcn/ui
* React Flow

---

## MVP 목표

MVP는 다음 시나리오가 가능해야 합니다.

1. 샘플 Spring/MyBatis 프로젝트 업로드
2. 프로젝트 분석 실행
3. API 목록 확인
4. 특정 API의 흐름 확인
5. MyBatis SQL 확인
6. 테이블 사용처 확인
7. AI에게 “이 기능 설명해줘” 질문
8. 근거 기반 답변 확인
9. 온보딩 문서 생성

---

## 차별점

일반적인 RAG 프로젝트와 다릅니다.

일반 RAG:

```text
소스코드 임베딩 → 질문 → LLM 답변
```

CodeAtlas:

```text
정적 분석 → 구조화된 사실 추출 → 그래프 생성 → 근거 기반 AI 설명
```

따라서 더 정확하고, 실무 유지보수에 적합합니다.

---

## 포트폴리오 관점의 강점

이 프로젝트는 다음 역량을 보여줍니다.

* Java/Spring 백엔드 이해
* 레거시 시스템 분석 경험
* MyBatis/SQL 이해
* AI Agent 활용 능력
* RAG 이상의 구조적 분석 설계
* 개발자 도구 기획 능력
* 실무 문제 해결 능력
* 한국 SI/SM 환경 이해

---

## 최종 데모 문장

면접에서 이 프로젝트는 이렇게 설명할 수 있습니다.

> CodeAtlas는 문서가 부족한 Java/Spring 레거시 프로젝트를 분석해서 API, Service 흐름, MyBatis SQL, 테이블 사용처를 자동 추출하고, AI가 신규 개발자용 온보딩 문서와 근거 기반 답변을 생성하는 개발자 도구입니다.
