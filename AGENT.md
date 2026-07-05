# AGENT.md

## 1. Project Identity

Project Name: **CodeAtlas**

Description:
CodeAtlas is an AI-assisted legacy project analysis platform for Korean SI/SM development environments.

The main goal is to help developers understand old, undocumented, or poorly documented Java/Spring legacy projects faster.

This project is not just a generic RAG chatbot.
It combines:

* Static code analysis
* Spring project structure analysis
* MyBatis XML analysis
* REST API extraction
* SQL/table usage analysis
* Dependency/call graph generation
* AI-generated documentation
* Onboarding Q&A for new developers

The target users are:

* Java/Spring backend developers
* SI/SM maintenance developers
* New developers joining an existing project
* Tech leads who need to understand project impact quickly
* Companies operating 10+ year old legacy systems

---

## 2. Core Problem

Many Korean enterprise systems have the following problems:

* Source code exists, but documentation is outdated.
* API documents are missing or incorrect.
* Business logic is hidden inside large Service classes.
* MyBatis XML files contain complex SQL.
* Developers rely on senior engineers or manual search.
* New developers need weeks to understand the system.
* Impact analysis is difficult when changing one API, DTO, table, or SQL.

CodeAtlas solves this by automatically analyzing the project and generating a searchable technical knowledge map.

---

## 3. Product Vision

CodeAtlas should answer questions like:

* “Where is the order creation logic?”
* “Which API uses this table?”
* “Which Service calls this Mapper?”
* “What SQL queries access TB_USER?”
* “What happens when OrderDTO changes?”
* “Generate onboarding documentation for this project.”
* “Show the flow from Controller to DB.”
* “Explain this legacy module for a new developer.”
* “Which files are risky to modify?”

The final product should feel like:

> “A map and AI guide for understanding legacy Java/Spring projects.”

---

## 4. Non-Goals

Do not build this as a simple file upload chatbot.

Avoid:

* Blindly embedding every source file without structure
* Letting the LLM guess relationships
* Treating all code as plain text only
* Building only a chat UI
* Generating fake architecture explanations without evidence
* Depending only on LLM output for facts

The system must prioritize deterministic analysis first.
LLM should explain verified facts, not invent facts.

---

## 5. Main Features

### 5.1 Repository Import

The system should allow users to analyze a Java/Spring project from:

* Local uploaded ZIP file
* Git repository URL
* Local directory path, if running in developer mode

Initial MVP may support only local ZIP upload.

---

### 5.2 Project Structure Analysis

Analyze and classify:

* Controller classes
* Service classes
* Repository classes
* Mapper interfaces
* Entity classes
* DTO classes
* Configuration classes
* Scheduler classes
* Batch classes
* Utility classes
* Test classes

For Spring projects, detect annotations such as:

* `@RestController`
* `@Controller`
* `@Service`
* `@Repository`
* `@Component`
* `@Mapper`
* `@Entity`
* `@Configuration`
* `@Scheduled`
* `@Transactional`
* `@GetMapping`
* `@PostMapping`
* `@PutMapping`
* `@DeleteMapping`
* `@RequestMapping`

---

### 5.3 API Extraction

Extract REST API information from Spring controllers.

For each API, collect:

* HTTP method
* URL path
* Controller class
* Method name
* Request DTO
* Response DTO
* Called Service method
* Related Mapper or Repository
* Related SQL, if traceable
* Related tables, if traceable

Output example:

```text
POST /api/orders
Controller: OrderController.createOrder()
Service: OrderService.createOrder()
Mapper: OrderMapper.insertOrder()
Tables: TB_ORDER, TB_ORDER_ITEM
```

---

### 5.4 Call Graph Analysis

Generate function/class call relationships.

Minimum target:

```text
Controller -> Service -> Repository/Mapper -> SQL/Table
```

The graph does not need to be perfect in MVP, but must be useful.

Use deterministic parsing where possible.

Do not rely only on LLM to infer call relationships.

---

### 5.5 MyBatis XML Analysis

Analyze MyBatis mapper XML files.

Extract:

* namespace
* statement id
* statement type: select, insert, update, delete
* SQL text
* referenced tables
* referenced columns, if possible
* parameter type
* result type
* linked Mapper interface method

Example:

```text
Mapper XML: OrderMapper.xml
Statement: selectOrderDetail
Type: SELECT
Tables: TB_ORDER, TB_ORDER_ITEM, TB_PRODUCT
```

---

### 5.6 Table Usage Analysis

For each table, show:

* Which SQL statements use it
* Which Mapper methods use it
* Which Service methods indirectly use it
* Which Controller/API indirectly use it

Example question:

> “Where is TB_USER used?”

Expected answer:

```text
TB_USER is used by:

1. UserMapper.selectUserById
2. UserMapper.updateLoginTime
3. AdminUserMapper.searchUsers

Related APIs:

- GET /api/users/{id}
- PUT /api/users/{id}/login-time
- GET /api/admin/users
```

---

### 5.7 AI Documentation Generation

Generate documentation based on analyzed facts.

Documents to generate:

* Project overview
* Module overview
* API documentation
* Service flow documentation
* Table usage documentation
* New developer onboarding guide
* Risky file summary
* Maintenance guide

All generated documentation must include evidence references:

* File path
* Class name
* Method name
* SQL id
* Table name

Do not generate unsupported explanations.

---

### 5.8 Developer Q&A

Allow users to ask questions about the analyzed project.

The answer must be grounded in indexed project facts.

Answer style:

* Direct answer first
* Related files
* Related APIs
* Related SQL
* Confidence level
* Evidence list

Example:

```text
Question:
주문 생성 로직 어디서 봐야 하나요?

Answer:
주문 생성 로직은 OrderController.createOrder()에서 시작합니다.
이 API는 OrderService.createOrder()를 호출하고, 최종적으로 OrderMapper.insertOrder()를 통해 TB_ORDER에 데이터를 저장합니다.

Related files:
- src/main/java/.../OrderController.java
- src/main/java/.../OrderService.java
- src/main/resources/mapper/OrderMapper.xml

Confidence: High
```

---

## 6. Suggested Architecture

### 6.1 Backend

Recommended stack:

* Java 21
* Spring Boot 3.x
* Gradle
* PostgreSQL
* pgvector, optional
* Redis, optional
* Docker Compose

Backend responsibilities:

* Project upload
* Static analysis orchestration
* Metadata persistence
* API service for frontend
* AI prompt orchestration
* Documentation generation

---

### 6.2 Analyzer Engine

Analyzer may be implemented in Java or Python.

Recommended approach:

* Java analyzer for Java/Spring AST parsing
* Python optional for LLM/RAG pipeline
* Keep analyzer logic modular

Suggested analyzer modules:

```text
analyzer/
  java/
    JavaClassAnalyzer
    SpringAnnotationAnalyzer
    MethodCallAnalyzer
  mybatis/
    MyBatisXmlAnalyzer
    SqlTableExtractor
  api/
    RestApiExtractor
  graph/
    DependencyGraphBuilder
  document/
    DocumentationGenerator
```

---

### 6.3 Frontend

Recommended stack:

* React
* TypeScript
* Tailwind CSS
* shadcn/ui
* React Flow for graph visualization

Main pages:

* Project list
* Project dashboard
* API map
* Call graph
* Table usage
* MyBatis SQL explorer
* AI Q&A
* Generated documents

---

### 6.4 Database

Core tables may include:

```text
projects
source_files
java_classes
java_methods
spring_apis
method_calls
mybatis_mappers
sql_statements
sql_tables
table_usages
documents
chat_sessions
chat_messages
analysis_jobs
```

---

## 7. MVP Scope

Build the MVP in this order.

### Phase 1: Repository Upload and File Indexing

* Upload ZIP
* Extract project
* Store project metadata
* List source files
* Detect Java files
* Detect XML mapper files

### Phase 2: Spring API Extraction

* Detect Controller classes
* Extract HTTP method and path
* Extract controller method name
* Display API list in UI

### Phase 3: MyBatis XML Analysis

* Parse mapper XML
* Extract SQL statement ids
* Extract SQL text
* Extract table names
* Link Mapper namespace and method names

### Phase 4: Basic Flow Mapping

* Link Controller method to Service method if directly called
* Link Service method to Mapper method if directly called
* Link Mapper method to XML statement
* Show simple flow:

```text
API -> Controller -> Service -> Mapper -> SQL -> Table
```

### Phase 5: AI Documentation

* Generate API documentation
* Generate module summary
* Generate onboarding guide
* Generate table usage summary

### Phase 6: AI Q&A

* Ask questions about analyzed code
* Use only stored facts and source snippets
* Return answers with references

---

## 8. Important Engineering Principles

### 8.1 Deterministic First

Use parsing and static analysis first.

LLM should be used for:

* summarization
* explanation
* documentation
* question answering
* risk interpretation

LLM should not be the only source of truth.

---

### 8.2 Evidence-Based Answers

Every AI answer must reference:

* file path
* class name
* method name
* SQL id
* table name

If evidence is weak, say so clearly.

Do not pretend certainty.

---

### 8.3 Korean SI/SM Fit

Prioritize support for common Korean legacy patterns:

* Spring MVC
* Spring Boot
* MyBatis
* JSP projects, later phase
* Oracle-style SQL
* Large Service classes
* XML configuration
* Mixed Korean/English naming
* Poorly documented code
* DTO-heavy architecture
* Controller-Service-Mapper structure

---

### 8.4 Portfolio Quality

This project is also a portfolio project.

Therefore, code quality matters.

Required:

* Clear README
* Docker Compose
* Sample legacy project
* Screenshots
* Architecture diagram
* API documentation
* Test data
* Meaningful commit history
* Unit tests for analyzers
* Clean package structure

---

## 9. Coding Rules for AI Agent

When modifying this project, follow these rules:

1. Do not make large unrelated changes.
2. Do not rewrite the whole project without instruction.
3. Prefer small, reviewable commits.
4. Keep architecture modular.
5. Add tests for parser/analyzer logic.
6. Do not hardcode only one sample project path.
7. Do not invent unsupported analysis results.
8. Separate parsing result from AI-generated explanation.
9. Keep domain naming clear and consistent.
10. Document important decisions in `/docs/decisions`.

---

## 10. Recommended Package Structure

Backend example:

```text
src/main/java/com/codeatlas
  CodeAtlasApplication.java

  project/
    controller/
    service/
    domain/
    repository/
    dto/

  analysis/
    controller/
    service/
    domain/
    repository/
    job/

  analyzer/
    java/
    spring/
    mybatis/
    sql/
    graph/

  document/
    service/
    prompt/
    dto/

  chat/
    controller/
    service/
    domain/
    repository/

  common/
    exception/
    response/
    config/
```

Frontend example:

```text
src/
  app/
  components/
  features/
    projects/
    analysis/
    api-map/
    call-graph/
    table-usage/
    documents/
    chat/
  lib/
  types/
```

---

## 11. Sample User Stories

### User Story 1

As a new backend developer,
I want to see all APIs in a legacy project,
so that I can understand the system entry points quickly.

### User Story 2

As a maintenance developer,
I want to search where a DB table is used,
so that I can estimate the impact of schema changes.

### User Story 3

As a tech lead,
I want to generate onboarding documentation,
so that new team members can understand the system faster.

### User Story 4

As a developer,
I want to ask natural language questions about the codebase,
so that I can find related files, APIs, SQL, and tables quickly.

---

## 12. Definition of Done

A feature is done only when:

* It works with the sample legacy project
* It has at least basic tests
* It has clear error handling
* It is visible in UI or API response
* It is documented in README or docs
* It does not break existing analysis results

---

## 13. Sample Demo Scenario

The demo should show this flow:

1. Upload sample legacy Spring project.
2. Run analysis.
3. Open dashboard.
4. Show detected APIs.
5. Click one API.
6. Show Controller → Service → Mapper → SQL → Table flow.
7. Click a table.
8. Show all APIs and SQL statements using that table.
9. Ask AI: “주문 생성 로직 설명해줘.”
10. AI answers with related files and evidence.
11. Generate onboarding document.

This demo scenario is very important for portfolio presentation.

---

## 14. README Message

The README should emphasize:

```text
CodeAtlas is an AI-powered legacy Java/Spring project analysis platform.
It helps developers understand undocumented enterprise systems by extracting APIs, services, MyBatis SQL, table usage, and call relationships, then generating evidence-based documentation and onboarding guides.
```

---

## 15. Future Extensions

Possible future features:

* Git history analysis
* Pull request impact analysis
* Security risk detection
* Transaction boundary analysis
* Circular dependency detection
* Large class detection
* Dead code detection
* Spring Boot 2 to 3 migration helper
* Java 8 to Java 21 migration helper
* MCP server for IDE integration
* VSCode extension
* IntelliJ plugin
* Jira/Confluence integration
* Slack Q&A bot
* Local LLM mode for company security

---

## 16. Agent Behavior

When the AI Agent works on this project:

* Always understand the current file structure first.
* Check existing conventions before creating new code.
* Prefer implementing the smallest useful vertical slice.
* Keep MVP progress visible.
* When uncertain, choose the approach that improves deterministic analysis.
* Do not over-engineer early.
* Do not add Kafka, Kubernetes, or complex infra unless needed.
* Prioritize working product over theoretical architecture.

The first milestone is:

> Upload a sample Spring/MyBatis project and extract API + Mapper + SQL + table usage information.
