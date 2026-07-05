# CodeAtlas

CodeAtlas는 레거시 Java/Spring 프로젝트를 분석하는 AI 기반 플랫폼입니다.

문서가 부족한 엔터프라이즈 시스템을 더 빠르게 이해할 수 있도록 API, 서비스, MyBatis SQL, 테이블 사용처, 호출 관계를 추출하고, 저장된 분석 근거를 바탕으로 온보딩 문서와 API 문서를 생성합니다.

## MVP 구현 상태

구현 완료:

- Spring Boot 3 백엔드 기본 구조
- Java 21 Gradle 설정
- PostgreSQL 기반 영속성
- 프로젝트 등록 및 조회 API
- ZIP 프로젝트 업로드
- 프로젝트 삭제 및 분석 결과 초기화 API
- 분석 작업 상태 API
- Java/XML 소스 파일 인덱싱
- Controller, Service, Repository, Mapper, Entity, DTO, Configuration, Scheduler, Batch, Component, Utility, Test 클래스 분류
- DTO 메타데이터를 위한 Java class/record 필드 추출
- 컨트롤러 애너테이션 기반 Spring REST API 추출
- Spring REST API 요청/응답 DTO 이름 추출
- MyBatis XML statement 추출
- SQL 테이블 이름 추출
- 기본 Controller-Service-Mapper-SQL 흐름 매핑
- Controller, Service, Mapper 호출에 대한 메서드 단위 그래프 노드
- 의존성 그래프 내 헬퍼 메서드 및 서비스 간 호출 경로 확장
- 대규모 프로젝트용 그래프 검색, 노드 타입 필터링, 연결 경로 중심 보기
- API, SQL, DTO, Flow 변경을 비교하는 저장형 분석 스냅샷과 최신 실행 비교
- 최신 스냅샷이 아닌 스냅샷 간 명시적 비교 선택
- 릴리스 검토 흐름을 위한 Markdown 비교 리포트
- 다운로드 가능한 Markdown 비교 리포트 파일
- 비교 근거 기반 릴리스 검토 체크리스트 생성
- 다운로드 가능한 릴리스 검토 체크리스트 파일
- 비교 심각도 기반 릴리스 준비 점수와 위험 등급
- 연속 분석 스냅샷 기반 릴리스 위험 추세
- 스냅샷 기준선/릴리스 라벨 및 메모 메타데이터
- 라벨이 지정된 분석 스냅샷 기반 릴리스 노트 생성
- 다운로드 가능한 릴리스 노트 파일
- 릴리스 노트 기반 GitHub Release Draft 페이로드 생성
- 설정이 완료된 경우 인증 기반 GitHub Release Draft 게시
- 서비스 내부 헬퍼 및 Service-to-Service 흐름 매핑
- Java 호출 분석에서 생성자 주입 및 `@Autowired` 필드 주입 패턴 분석
- API, 테이블 사용처, 흐름, 대시보드, 그래프 API
- 저장된 분석 사실 기반 Markdown 문서 생성
- 저장된 분석 사실 기반 규칙형 Q&A
- Ollama를 통한 선택적 로컬 LLM 보강, 기본 모델은 `gemma4:e4b`
- Spring Boot에서 제공되는 엔터프라이즈 스타일 React 대시보드 UI
- 대시보드 UI의 API 및 테이블 인라인 상세 패널
- API, 테이블, 흐름에 대한 대시보드 검색 및 필터링
- API, 테이블, Mapper, SQL 키워드, DTO 변경 영향 분석
- `frontend/` 하위 React/TypeScript/Tailwind 프론트엔드
- 프론트엔드의 React Flow 그래프
- Spring Boot test, `bootRun`, `bootJar`, Docker 이미지 빌드에 React 프론트엔드 빌드 통합
- 샘플 레거시 Spring/MyBatis 프로젝트
- 애플리케이션과 PostgreSQL을 위한 Docker Compose
- 분석기, API 계약, UI 계약, 로컬 LLM 클라이언트 테스트

부분 구현:

- Controller-Service-Mapper-Table 그래프: 메서드 단위 Controller, Service, Mapper, 헬퍼 메서드, 서비스 간 호출 경로 노드를 포함합니다.
- 문서 생성과 Q&A: 결정론적 기본 결과는 항상 제공되며, 로컬 Ollama가 설정되어 있고 접근 가능하면 LLM 보강을 사용합니다.
- 영향 분석: API, 테이블, Mapper, SQL 키워드, DTO 요청/응답 및 필드 변경에 대해 제공됩니다.
- 프론트엔드: React 앱은 로컬 실행, 테스트, 패키징된 Spring Boot, Docker 빌드에서 기본 대시보드 UI로 사용됩니다.

다음 작업:

1. GitHub 게시 감사 이력과 게시 상태 추적을 추가합니다.

## 요구 사항

- Java 21
- Compose 실행 흐름을 위한 Docker Desktop

이 저장소에는 Gradle Wrapper가 포함되어 있으므로 Gradle을 로컬에 별도로 설치할 필요가 없습니다.

## Docker Compose로 실행

```bash
docker compose up --build
```

애플리케이션은 다음 주소에서 실행됩니다.

```text
http://localhost:8080
```

같은 주소에서 Spring Boot 정적 리소스로 제공되는 React 대시보드 UI를 확인할 수 있습니다.

애플리케이션 컨테이너 안에서 샘플 프로젝트는 다음 경로에 마운트됩니다.

```text
/samples/legacy-spring-mybatis
```

## 로컬 실행

PostgreSQL을 시작합니다.

```bash
docker compose up -d postgres
```

애플리케이션을 실행합니다.

```bash
./gradlew bootRun
```

Windows PowerShell에서는 다음 명령을 사용합니다.

```powershell
.\gradlew.bat bootRun
```

별도 터미널에서 React 프론트엔드 개발 서버를 실행합니다.

```bash
cd frontend
npm install
npm run dev
```

React 앱은 다음 주소에서 실행됩니다.

```text
http://localhost:5173
```

개발 중에는 Vite가 `/api` 요청을 `http://localhost:8080`의 Spring Boot 백엔드로 프록시합니다.

운영용 Spring Boot jar를 빌드할 때 React 앱도 자동으로 빌드됩니다.

```bash
./gradlew bootJar
```

Windows PowerShell에서는 다음 명령을 사용합니다.

```powershell
.\gradlew.bat bootJar
```

기본적으로 애플리케이션은 PostgreSQL이 다음 주소에 있다고 가정합니다.

```text
jdbc:postgresql://localhost:5432/code_atlas
```

Docker Compose의 PostgreSQL 서비스를 로컬 애플리케이션 실행에 사용할 경우 다음 값을 사용합니다.

```text
CODE_ATLAS_DB_URL=jdbc:postgresql://localhost:15432/code_atlas
```

Compose PostgreSQL 호스트 포트는 다음 환경 변수로 변경할 수 있습니다.

```text
CODE_ATLAS_POSTGRES_PORT
```

환경 변수:

```text
CODE_ATLAS_DB_URL
CODE_ATLAS_DB_USERNAME
CODE_ATLAS_DB_PASSWORD
CODE_ATLAS_SERVER_PORT
CODE_ATLAS_PROJECTS_STORAGE_PATH
CODE_ATLAS_MAX_UPLOAD_SIZE
CODE_ATLAS_POSTGRES_PORT
CODE_ATLAS_LLM_ENABLED
CODE_ATLAS_LLM_BASE_URL
CODE_ATLAS_LLM_MODEL
CODE_ATLAS_GITHUB_ENABLED
CODE_ATLAS_GITHUB_BASE_URL
CODE_ATLAS_GITHUB_TOKEN
CODE_ATLAS_GITHUB_OWNER
CODE_ATLAS_GITHUB_REPOSITORY
```

## 로컬 LLM

CodeAtlas는 결정론적 분석 사실을 기준 데이터로 유지하면서, 로컬 Ollama 모델로 생성 문서와 Q&A 답변을 보강할 수 있습니다.

기본 로컬 설정:

```text
CODE_ATLAS_LLM_ENABLED=true
CODE_ATLAS_LLM_BASE_URL=http://localhost:11434
CODE_ATLAS_LLM_MODEL=gemma4:e4b
```

Docker Compose로 실행할 때 애플리케이션 컨테이너는 다음 값을 사용합니다.

```text
CODE_ATLAS_LLM_BASE_URL=http://host.docker.internal:11434
CODE_ATLAS_LLM_MODEL=gemma4:e4b
```

Ollama에 접근할 수 없거나 생성에 실패하면 CodeAtlas는 결정론적 문서 및 Q&A 출력으로 자동 전환합니다.

## GitHub Release Draft 게시

분석 스냅샷 비교 결과에서 GitHub Release Draft 페이로드를 생성할 수 있습니다. 다음 설정이 제공되면 CodeAtlas가 GitHub API를 통해 Release Draft를 게시할 수 있습니다.

```text
CODE_ATLAS_GITHUB_ENABLED=true
CODE_ATLAS_GITHUB_BASE_URL=https://api.github.com
CODE_ATLAS_GITHUB_TOKEN
CODE_ATLAS_GITHUB_OWNER
CODE_ATLAS_GITHUB_REPOSITORY
```

토큰에는 대상 저장소의 Release 생성 권한이 필요합니다. 설정이 없거나 GitHub 연동이 비활성화된 경우에는 게시 API가 설정 오류를 반환하지만, Release Draft 페이로드 생성은 계속 사용할 수 있습니다.

## MVP API

```text
POST /api/projects
POST /api/projects/upload
GET /api/projects
GET /api/projects/{projectId}
DELETE /api/projects/{projectId}
GET /api/projects/{projectId}/dashboard

POST /api/projects/{projectId}/analyze
GET /api/projects/{projectId}/analysis/status
GET /api/projects/{projectId}/analysis/snapshots
PATCH /api/projects/{projectId}/analysis/snapshots/{snapshotId}
GET /api/projects/{projectId}/analysis/risk-trend
GET /api/projects/{projectId}/analysis/comparison/latest
GET /api/projects/{projectId}/analysis/comparison/latest/report
GET /api/projects/{projectId}/analysis/comparison/latest/report/download
GET /api/projects/{projectId}/analysis/comparison/latest/checklist
GET /api/projects/{projectId}/analysis/comparison/latest/checklist/download
GET /api/projects/{projectId}/analysis/comparison/latest/release-notes
GET /api/projects/{projectId}/analysis/comparison/latest/release-notes/download
GET /api/projects/{projectId}/analysis/comparison/latest/github-release-draft
POST /api/projects/{projectId}/analysis/comparison/latest/github-release-draft/publish
GET /api/projects/{projectId}/analysis/comparison?baseSnapshotId={baseSnapshotId}&targetSnapshotId={targetSnapshotId}
GET /api/projects/{projectId}/analysis/comparison/report?baseSnapshotId={baseSnapshotId}&targetSnapshotId={targetSnapshotId}
GET /api/projects/{projectId}/analysis/comparison/report/download?baseSnapshotId={baseSnapshotId}&targetSnapshotId={targetSnapshotId}
GET /api/projects/{projectId}/analysis/comparison/checklist?baseSnapshotId={baseSnapshotId}&targetSnapshotId={targetSnapshotId}
GET /api/projects/{projectId}/analysis/comparison/checklist/download?baseSnapshotId={baseSnapshotId}&targetSnapshotId={targetSnapshotId}
GET /api/projects/{projectId}/analysis/comparison/release-notes?baseSnapshotId={baseSnapshotId}&targetSnapshotId={targetSnapshotId}
GET /api/projects/{projectId}/analysis/comparison/release-notes/download?baseSnapshotId={baseSnapshotId}&targetSnapshotId={targetSnapshotId}
GET /api/projects/{projectId}/analysis/comparison/github-release-draft?baseSnapshotId={baseSnapshotId}&targetSnapshotId={targetSnapshotId}
POST /api/projects/{projectId}/analysis/comparison/github-release-draft/publish?baseSnapshotId={baseSnapshotId}&targetSnapshotId={targetSnapshotId}
DELETE /api/projects/{projectId}/analysis/results
GET /api/projects/{projectId}/graph
GET /api/projects/{projectId}/source-files
GET /api/projects/{projectId}/apis
GET /api/projects/{projectId}/apis/{apiId}
GET /api/projects/{projectId}/mybatis/statements
GET /api/projects/{projectId}/tables
GET /api/projects/{projectId}/tables/{tableName}
GET /api/projects/{projectId}/flows
GET /api/projects/{projectId}/documents/onboarding
GET /api/projects/{projectId}/documents/apis
POST /api/projects/{projectId}/questions
```

## 샘플 분석

저장소에는 작은 Spring/MyBatis 샘플이 포함되어 있습니다.

```text
samples/legacy-spring-mybatis
```

Docker Compose로 실행 중일 때 샘플을 등록합니다.

```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -d '{"name":"legacy-spring-mybatis","sourcePath":"/samples/legacy-spring-mybatis"}'
```

또는 ZIP 파일을 업로드합니다.

```bash
curl -X POST http://localhost:8080/api/projects/upload \
  -F "name=legacy-spring-mybatis" \
  -F "file=@legacy-spring-mybatis.zip"
```

이 작업 공간에서 로컬로 실행 중일 때 샘플을 등록합니다.

```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -d '{"name":"legacy-spring-mybatis","sourcePath":"D:/DEV/code-atlas/samples/legacy-spring-mybatis"}'
```

분석을 실행합니다.

```bash
curl -X POST http://localhost:8080/api/projects/1/analyze
```

분석이 실패해도 최신 작업 상태는 다음 API로 확인할 수 있습니다.

```bash
curl http://localhost:8080/api/projects/1/analysis/status
```

프로젝트와 분석 작업 이력은 유지하면서 추출된 분석 결과를 초기화합니다.

```bash
curl -X DELETE http://localhost:8080/api/projects/1/analysis/results
```

프로젝트와 저장된 분석 데이터를 삭제합니다.

```bash
curl -X DELETE http://localhost:8080/api/projects/1
```

프로젝트 대시보드를 조회합니다.

```bash
curl http://localhost:8080/api/projects/1/dashboard
```

API-Service-Mapper-Table 그래프를 조회합니다.

```bash
curl http://localhost:8080/api/projects/1/graph
```

추출된 API 목록을 조회합니다.

```bash
curl http://localhost:8080/api/projects/1/apis
```

관련 흐름이 포함된 단일 API 상세를 조회합니다.

```bash
curl http://localhost:8080/api/projects/1/apis/1
```

추출된 MyBatis statement를 조회합니다.

```bash
curl http://localhost:8080/api/projects/1/mybatis/statements
```

테이블 사용 요약을 조회합니다.

```bash
curl http://localhost:8080/api/projects/1/tables
```

테이블 사용 상세를 조회합니다.

```bash
curl http://localhost:8080/api/projects/1/tables/TB_ORDER
```

API 흐름을 조회합니다.

```bash
curl http://localhost:8080/api/projects/1/flows
```

온보딩 문서를 생성합니다.

```bash
curl http://localhost:8080/api/projects/1/documents/onboarding
```

API 문서를 생성합니다.

```bash
curl http://localhost:8080/api/projects/1/documents/apis
```

근거 기반 질문을 실행합니다.

```bash
curl -X POST http://localhost:8080/api/projects/1/questions \
  -H "Content-Type: application/json" \
  -d '{"question":"TB_ORDER 어디서 사용돼?"}'
```

예상 샘플 결과:

- 인덱싱된 소스 파일 8개
- Spring API 5개
- MyBatis statement 5개
- 테이블 3개: `TB_ORDER`, `TB_ORDER_ITEM`, `TB_PRODUCT`
- API 흐름 행 7개

## 테스트

```bash
./gradlew test
```

Windows PowerShell에서는 다음 명령을 사용합니다.

```powershell
.\gradlew.bat test
```

테스트 스위트에는 분석기 단위 테스트, 서비스 수준 통합 테스트, MockMvc 기반 HTTP API 계약 테스트가 포함됩니다.

## 검증

Docker Compose MVP 검증 내용은 다음 문서에 정리되어 있습니다.

```text
docs/verification/docker-compose-mvp-verification.md
```
