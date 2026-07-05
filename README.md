# CodeAtlas

CodeAtlas는 레거시 Java/Spring 시스템을 분석해 API, 서비스 호출, MyBatis SQL, 테이블 사용처, 변경 영향도를 한 화면에서 추적하는 엔터프라이즈 분석 대시보드입니다.

문서가 부족하거나 담당자가 바뀐 시스템에서도 코드 구조를 빠르게 파악할 수 있도록, 정적 분석 결과를 근거 데이터로 저장하고 온보딩 문서, API 문서, 릴리스 비교 리포트, 검토 체크리스트, GitHub Release Draft까지 생성합니다. 로컬 Ollama LLM을 선택적으로 연결할 수 있지만, 핵심 분석 결과는 항상 저장된 코드 분석 근거를 기준으로 생성됩니다.

## 주요 사용 시나리오

- 오래된 Spring/MyBatis 프로젝트의 API, 서비스, Mapper, SQL, 테이블 관계 파악
- 특정 API 또는 테이블 변경 시 영향 범위 확인
- 신규 담당자 온보딩 문서와 API 문서 자동 생성
- 분석 스냅샷 간 API/SQL/DTO/Flow 변경 비교
- 릴리스 준비 점수, 위험 등급, 검토 체크리스트, 릴리스 노트 생성
- GitHub Release Draft 생성, 게시, 성공/실패 이력 추적
- 로컬 LLM을 통한 문서와 Q&A 답변 보강

## 핵심 기능

### 프로젝트 분석

- Java/XML 소스 파일 인덱싱
- Controller, Service, Repository, Mapper, Entity, DTO, Configuration, Scheduler, Batch, Component, Utility, Test 클래스 분류
- Java class/record 필드 추출 및 DTO 메타데이터 구성
- Spring REST API 경로, HTTP 메서드, 컨트롤러 메서드, 요청/응답 DTO 추출
- MyBatis XML statement, SQL 타입, 테이블 이름 추출
- 생성자 주입과 `@Autowired` 필드 주입 기반 Java 호출 관계 분석
- Controller-Service-Mapper-SQL 흐름 매핑
- 헬퍼 메서드와 Service-to-Service 호출 경로 확장

### 대시보드

- Spring Boot에서 제공되는 React/TypeScript/Tailwind 기반 대시보드
- API, 테이블, Flow, 그래프, 영향 분석, 비교, Q&A, 문서 탭 제공
- API/테이블 인라인 상세 패널
- API, 테이블, Flow 검색 및 필터링
- React Flow 기반 호출 그래프
- 대규모 그래프용 노드 타입 필터, 검색, 연결 경로 중심 보기

### 영향 분석과 릴리스 검토

- API, 테이블, Mapper, SQL 키워드, DTO 요청/응답 및 필드 변경 영향 분석
- 분석 스냅샷 저장 및 최신 2개 분석 비교
- 임의의 기준/대상 스냅샷 선택 비교
- Markdown 비교 리포트 생성 및 다운로드
- 릴리스 검토 체크리스트 생성 및 다운로드
- 릴리스 준비 점수와 위험 등급 산정
- 연속 스냅샷 기반 릴리스 위험 추세
- 스냅샷 라벨과 노트 메타데이터 관리
- 릴리스 노트 생성 및 다운로드

### GitHub Release Draft

- 릴리스 노트 기반 GitHub Release Draft 페이로드 생성
- 설정된 GitHub 저장소로 Release Draft 게시
- 게시 성공/실패 상태 저장
- 최근 GitHub 게시 이력 조회 및 대시보드 표시
- GitHub 설정이 없거나 비활성화된 경우에도 실패 이력을 남겨 운영 추적 가능

### 문서와 Q&A

- 저장된 분석 사실 기반 온보딩 문서 생성
- 저장된 분석 사실 기반 API 문서 생성
- 근거 기반 Q&A
- Ollama 로컬 LLM 보강 지원
- LLM 장애 또는 미설정 시 결정론적 기본 응답으로 자동 전환

## 기술 스택

- Backend: Java 21, Spring Boot 3.3, Spring Data JPA, PostgreSQL
- Frontend: React, TypeScript, Vite, Tailwind CSS, React Flow
- Build: Gradle Wrapper, npm
- Runtime: Docker Compose, PostgreSQL
- Optional AI: Ollama, 기본 모델 `gemma4:e4b`
- Tests: JUnit, MockMvc, 프론트엔드 린트 및 빌드 검증

## 빠른 시작

### 1. 요구 사항

- Java 21
- Docker Desktop
- Node.js는 프론트엔드 개발 서버를 별도로 실행할 때 필요합니다.
- Gradle은 저장소에 포함된 Gradle Wrapper를 사용하므로 별도 설치가 필요하지 않습니다.

### 2. Docker Compose로 실행

```bash
docker compose up --build
```

실행 후 브라우저에서 다음 주소를 엽니다.

```text
http://localhost:8080
```

Spring Boot가 React 대시보드 UI를 정적 리소스로 제공합니다. Compose 환경에서 샘플 프로젝트는 컨테이너 내부의 다음 경로에 마운트됩니다.

```text
/samples/legacy-spring-mybatis
```

### 3. 로컬 개발 실행

PostgreSQL만 먼저 실행합니다.

```bash
docker compose up -d postgres
```

Spring Boot 애플리케이션을 실행합니다.

```bash
./gradlew bootRun
```

Windows PowerShell에서는 다음 명령을 사용합니다.

```powershell
.\gradlew.bat bootRun
```

프론트엔드 개발 서버를 별도 터미널에서 실행합니다.

```bash
cd frontend
npm install
npm run dev
```

프론트엔드 개발 서버 주소:

```text
http://localhost:5173
```

개발 중 Vite는 `/api` 요청을 `http://localhost:8080`의 Spring Boot 백엔드로 프록시합니다.

### 4. 운영용 jar 빌드

```bash
./gradlew bootJar
```

Windows PowerShell:

```powershell
.\gradlew.bat bootJar
```

`bootJar` 빌드 과정에서 React 앱도 자동으로 빌드되어 Spring Boot 정적 리소스에 포함됩니다.

## 샘플 프로젝트 분석

저장소에는 테스트용 Spring/MyBatis 샘플이 포함되어 있습니다.

```text
samples/legacy-spring-mybatis
```

Docker Compose로 실행 중일 때 샘플 프로젝트를 등록합니다.

```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -d '{"name":"legacy-spring-mybatis","sourcePath":"/samples/legacy-spring-mybatis"}'
```

로컬 작업 공간에서 직접 실행 중일 때는 실제 경로를 등록합니다.

```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -d '{"name":"legacy-spring-mybatis","sourcePath":"D:/DEV/code-atlas/samples/legacy-spring-mybatis"}'
```

ZIP 파일 업로드도 지원합니다.

```bash
curl -X POST http://localhost:8080/api/projects/upload \
  -F "name=legacy-spring-mybatis" \
  -F "file=@legacy-spring-mybatis.zip"
```

분석을 실행합니다.

```bash
curl -X POST http://localhost:8080/api/projects/1/analyze
```

샘플 분석 예상 결과:

- 인덱싱된 소스 파일 8개
- Spring API 5개
- MyBatis statement 5개
- 테이블 3개: `TB_ORDER`, `TB_ORDER_ITEM`, `TB_PRODUCT`
- API Flow 7개

## 주요 API

### 프로젝트

```text
POST /api/projects
POST /api/projects/upload
GET /api/projects
GET /api/projects/{projectId}
DELETE /api/projects/{projectId}
GET /api/projects/{projectId}/dashboard
```

### 분석

```text
POST /api/projects/{projectId}/analyze
GET /api/projects/{projectId}/analysis/status
DELETE /api/projects/{projectId}/analysis/results
GET /api/projects/{projectId}/source-files
GET /api/projects/{projectId}/apis
GET /api/projects/{projectId}/apis/{apiId}
GET /api/projects/{projectId}/mybatis/statements
GET /api/projects/{projectId}/tables
GET /api/projects/{projectId}/tables/{tableName}
GET /api/projects/{projectId}/flows
GET /api/projects/{projectId}/graph
```

### 문서와 Q&A

```text
GET /api/projects/{projectId}/documents/onboarding
GET /api/projects/{projectId}/documents/apis
POST /api/projects/{projectId}/questions
```

### 스냅샷 비교와 릴리스 검토

```text
GET /api/projects/{projectId}/analysis/snapshots
PATCH /api/projects/{projectId}/analysis/snapshots/{snapshotId}
GET /api/projects/{projectId}/analysis/risk-trend
GET /api/projects/{projectId}/analysis/comparison/latest
GET /api/projects/{projectId}/analysis/comparison
GET /api/projects/{projectId}/analysis/comparison/latest/report
GET /api/projects/{projectId}/analysis/comparison/latest/report/download
GET /api/projects/{projectId}/analysis/comparison/report
GET /api/projects/{projectId}/analysis/comparison/report/download
GET /api/projects/{projectId}/analysis/comparison/latest/checklist
GET /api/projects/{projectId}/analysis/comparison/latest/checklist/download
GET /api/projects/{projectId}/analysis/comparison/checklist
GET /api/projects/{projectId}/analysis/comparison/checklist/download
GET /api/projects/{projectId}/analysis/comparison/latest/release-notes
GET /api/projects/{projectId}/analysis/comparison/latest/release-notes/download
GET /api/projects/{projectId}/analysis/comparison/release-notes
GET /api/projects/{projectId}/analysis/comparison/release-notes/download
```

### GitHub Release Draft

```text
GET /api/projects/{projectId}/analysis/comparison/latest/github-release-draft
POST /api/projects/{projectId}/analysis/comparison/latest/github-release-draft/publish
GET /api/projects/{projectId}/analysis/comparison/github-release-draft
POST /api/projects/{projectId}/analysis/comparison/github-release-draft/publish
GET /api/projects/{projectId}/analysis/github-release-publish-history
```

## 환경 변수

### 데이터베이스와 서버

```text
CODE_ATLAS_DB_URL
CODE_ATLAS_DB_USERNAME
CODE_ATLAS_DB_PASSWORD
CODE_ATLAS_SERVER_PORT
CODE_ATLAS_PROJECTS_STORAGE_PATH
CODE_ATLAS_MAX_UPLOAD_SIZE
CODE_ATLAS_POSTGRES_PORT
```

기본 데이터베이스 주소:

```text
jdbc:postgresql://localhost:5432/code_atlas
```

Docker Compose의 PostgreSQL을 로컬 애플리케이션에서 사용할 경우:

```text
CODE_ATLAS_DB_URL=jdbc:postgresql://localhost:15432/code_atlas
```

### 로컬 LLM

```text
CODE_ATLAS_LLM_ENABLED=true
CODE_ATLAS_LLM_BASE_URL=http://localhost:11434
CODE_ATLAS_LLM_MODEL=gemma4:e4b
```

Docker Compose에서 호스트의 Ollama를 사용할 경우:

```text
CODE_ATLAS_LLM_BASE_URL=http://host.docker.internal:11434
CODE_ATLAS_LLM_MODEL=gemma4:e4b
```

Ollama에 접근할 수 없거나 생성이 실패하면 CodeAtlas는 저장된 분석 근거 기반의 기본 문서와 Q&A 응답을 반환합니다.

### GitHub 연동

```text
CODE_ATLAS_GITHUB_ENABLED=true
CODE_ATLAS_GITHUB_BASE_URL=https://api.github.com
CODE_ATLAS_GITHUB_TOKEN
CODE_ATLAS_GITHUB_OWNER
CODE_ATLAS_GITHUB_REPOSITORY
```

`CODE_ATLAS_GITHUB_TOKEN`에는 대상 저장소의 Release 생성 권한이 필요합니다. GitHub 설정이 없거나 비활성화된 경우 게시 요청은 `FAILED` 상태로 이력에 저장되며, Release Draft 페이로드 생성 기능은 계속 사용할 수 있습니다.

## 테스트와 검증

백엔드, 통합 API, 프론트엔드 빌드를 포함한 전체 테스트:

```bash
./gradlew test
```

Windows PowerShell:

```powershell
.\gradlew.bat test
```

프론트엔드 린트:

```bash
cd frontend
npm run lint
```

Docker Compose MVP 검증 기록:

```text
docs/verification/docker-compose-mvp-verification.md
```

## 운영 참고 사항

- `spring.jpa.hibernate.ddl-auto`는 기본적으로 `update`입니다. 운영 환경에서는 별도 마이그레이션 전략 적용을 권장합니다.
- 분석 대상 프로젝트 경로는 애플리케이션 프로세스 또는 컨테이너에서 접근 가능해야 합니다.
- ZIP 업로드 시 최대 크기는 `CODE_ATLAS_MAX_UPLOAD_SIZE`로 조정할 수 있습니다.
- LLM은 보강 기능입니다. 의사결정의 기준은 저장된 정적 분석 결과입니다.
- GitHub Release 게시 기능은 GitHub 토큰과 대상 저장소 설정이 정확해야 성공합니다.

## 기여와 문의

버그 제보, 기능 제안, 사용 문의는 GitHub Issue를 통해 남겨 주세요. 가능한 경우 다음 정보를 함께 제공하면 원인 파악이 빨라집니다.

- 실행 방식: Docker Compose, 로컬 `bootRun`, 패키징 jar 중 하나
- Java 버전과 운영체제
- 재현 절차
- 기대한 결과와 실제 결과
- 관련 로그 또는 화면 캡처
- 분석 대상 프로젝트의 구조 설명, 공개 가능한 경우 최소 재현 샘플

Pull Request를 보낼 때는 다음 기준을 지켜 주세요.

- 변경 목적과 사용자 영향을 PR 설명에 명확히 작성합니다.
- 신규 기능 또는 버그 수정에는 가능한 범위의 테스트를 포함합니다.
- UI 변경은 주요 화면 흐름과 오류 상태를 함께 확인합니다.
- 분석 로직 변경은 샘플 프로젝트 또는 최소 재현 코드로 검증합니다.
- 민감 정보, 실제 고객 코드, 개인 토큰은 커밋하지 않습니다.

## 라이선스

현재 이 저장소에는 별도의 오픈소스 라이선스 파일이 포함되어 있지 않습니다. 명시적인 라이선스가 추가되기 전까지 모든 권리는 저장소 소유자에게 있으며, 복제, 배포, 상업적 사용, 2차 저작물 작성 전에는 저장소 소유자의 허가를 받아야 합니다.

라이선스 정책이 확정되면 `LICENSE` 파일과 이 섹션을 함께 갱신합니다.
