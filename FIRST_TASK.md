# FIRST_TASK.md

You are working on CodeAtlas, an AI-assisted legacy Java/Spring project analysis platform.

Your first goal is to build the smallest working MVP.

## Current Mission

Create the initial project skeleton and implement the first vertical slice:

> Upload or load a sample Spring/MyBatis project and extract basic REST API + MyBatis SQL metadata.

## Required Output

Implement the following:

1. Spring Boot backend project structure
2. Project entity and API
3. Analysis job entity and API
4. Source file indexing
5. Java Controller detection
6. REST API extraction from Spring annotations
7. MyBatis XML detection
8. MyBatis SQL statement extraction
9. Basic PostgreSQL persistence
10. Simple API endpoints to view analysis results

## MVP Backend APIs

Create these endpoints:

```text
POST /api/projects
GET /api/projects
GET /api/projects/{projectId}

POST /api/projects/{projectId}/analyze
GET /api/projects/{projectId}/analysis/status

GET /api/projects/{projectId}/apis
GET /api/projects/{projectId}/mybatis/statements
GET /api/projects/{projectId}/tables
```

## Analyzer Requirements

### Java API Extraction

Detect:

* `@RestController`
* `@Controller`
* `@RequestMapping`
* `@GetMapping`
* `@PostMapping`
* `@PutMapping`
* `@DeleteMapping`
* `@PatchMapping`

Extract:

* controller class name
* method name
* HTTP method
* URL path
* source file path
* line number if available

### MyBatis XML Extraction

Detect XML files under:

```text
src/main/resources
```

Extract:

* mapper namespace
* statement id
* statement type: select, insert, update, delete
* SQL body
* estimated table names

## Important Rules

* Do not build AI chat in the first task.
* Do not build frontend yet unless backend MVP is working.
* Do not add unnecessary infrastructure.
* Do not use Kafka or Kubernetes.
* Keep code clean and modular.
* Add tests for analyzer logic.
* Create a small sample legacy project under `/samples/legacy-spring-mybatis`.
* README must explain how to run the MVP.

## Recommended Implementation Order

1. Create Spring Boot project.
2. Create database schema.
3. Add project registration.
4. Add sample project loader.
5. Implement Java file scanner.
6. Implement Controller annotation parser.
7. Implement MyBatis XML parser.
8. Persist extracted metadata.
9. Expose result APIs.
10. Add tests and README.

## Done Criteria

The task is complete when:

* The app runs with Docker Compose.
* A sample project can be analyzed.
* At least one Controller API is extracted.
* At least one MyBatis SQL statement is extracted.
* Extracted results can be viewed through REST APIs.
* Analyzer tests pass.
* README contains run instructions.
