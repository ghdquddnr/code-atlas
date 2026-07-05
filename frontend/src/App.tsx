import { useEffect, useMemo, useState } from "react";
import {
  Background,
  Controls,
  MarkerType,
  ReactFlow,
  type Edge,
  type Node
} from "@xyflow/react";
import { Activity, FileText, GitBranch, Network, RefreshCw } from "lucide-react";
import { api } from "./lib/api";
import { includesQuery, normalize } from "./lib/utils";
import type {
  ApiFlow,
  AnalysisComparison,
  AnalysisDiffSection,
  AnalysisSnapshot,
  Dashboard,
  GeneratedDocument,
  GitHubReleaseDraft,
  GitHubReleasePublishResult,
  Project,
  ProjectGraph,
  GraphNode,
  QuestionAnswer,
  ReleaseRiskTrendPoint,
  SourceFile,
  SpringApi,
  SpringApiDetail,
  TableUsage,
  TableUsageDetail
} from "./types";
import { Badge } from "./components/ui/badge";
import { Button } from "./components/ui/button";
import { Card } from "./components/ui/card";
import { Input } from "./components/ui/input";
import { Select } from "./components/ui/select";

type TabKey = "apis" | "tables" | "flows" | "graph" | "impact" | "compare" | "qa" | "docs";

const tabs: Array<{ key: TabKey; label: string }> = [
  { key: "apis", label: "API" },
  { key: "tables", label: "Tables" },
  { key: "flows", label: "Flow" },
  { key: "graph", label: "Graph" },
  { key: "impact", label: "Impact" },
  { key: "compare", label: "Compare" },
  { key: "qa", label: "Q&A" },
  { key: "docs", label: "Docs" }
];

function App() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [selectedProject, setSelectedProject] = useState<Project | null>(null);
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);
  const [apis, setApis] = useState<SpringApi[]>([]);
  const [sourceFiles, setSourceFiles] = useState<SourceFile[]>([]);
  const [tables, setTables] = useState<TableUsage[]>([]);
  const [flows, setFlows] = useState<ApiFlow[]>([]);
  const [graph, setGraph] = useState<ProjectGraph | null>(null);
  const [activeTab, setActiveTab] = useState<TabKey>("apis");
  const [status, setStatus] = useState("프로젝트를 등록하거나 선택하면 분석 결과를 볼 수 있습니다.");
  const [isError, setIsError] = useState(false);
  const [projectName, setProjectName] = useState("legacy-spring-mybatis");
  const [sourcePath, setSourcePath] = useState("/samples/legacy-spring-mybatis");
  const [uploadName, setUploadName] = useState("uploaded-legacy-project");
  const [uploadFile, setUploadFile] = useState<File | null>(null);

  async function safeRun(action: () => Promise<void>) {
    try {
      setIsError(false);
      await action();
    } catch (error) {
      setIsError(true);
      setStatus(error instanceof Error ? error.message : "알 수 없는 오류가 발생했습니다.");
    }
  }

  async function loadProjects() {
    const nextProjects = await api.listProjects();
    setProjects(nextProjects);
  }

  async function selectProject(project: Project) {
    setSelectedProject(project);
    setApis([]);
    setSourceFiles([]);
    setTables([]);
    setFlows([]);
    setGraph(null);
    const nextDashboard = await api.dashboard(project.id);
    setDashboard(nextDashboard);
    if (nextDashboard.latestAnalysisJob) {
      setStatus(`${nextDashboard.latestAnalysisJob.status}: ${nextDashboard.latestAnalysisJob.message}`);
      setIsError(nextDashboard.latestAnalysisJob.status === "FAILED");
    }
  }

  async function createProject(event: React.FormEvent) {
    event.preventDefault();
    const project = await api.createProject({ name: projectName.trim(), sourcePath: sourcePath.trim() });
    setStatus(`프로젝트가 등록됐습니다: ${project.name}`);
    await loadProjects();
    await selectProject(project);
  }

  async function uploadProject(event: React.FormEvent) {
    event.preventDefault();
    if (!uploadFile) {
      throw new Error("업로드할 ZIP 파일을 선택하세요.");
    }
    const project = await api.uploadProject({ name: uploadName.trim(), file: uploadFile });
    setStatus(`ZIP 프로젝트가 업로드됐습니다: ${project.name}`);
    setUploadFile(null);
    await loadProjects();
    await selectProject(project);
  }

  async function analyzeSelectedProject() {
    if (!selectedProject) return;
    setStatus("분석 중입니다...");
    const result = await api.analyze(selectedProject.id);
    setStatus(JSON.stringify(result));
    await selectProject(selectedProject);
    await loadTabData(activeTab, selectedProject.id);
  }

  async function resetSelectedProject() {
    if (!selectedProject) return;
    const result = await api.resetAnalysis(selectedProject.id);
    setStatus(`분석 결과를 초기화했습니다: ${JSON.stringify(result)}`);
    await selectProject(selectedProject);
  }

  async function loadTabData(tab: TabKey, projectId = selectedProject?.id) {
    if (!projectId) return;
    if (tab === "apis" && apis.length === 0) setApis(await api.apis(projectId));
    if (tab === "tables" && tables.length === 0) setTables(await api.tables(projectId));
    if ((tab === "flows" || tab === "impact") && flows.length === 0) setFlows(await api.flows(projectId));
    if (tab === "impact" && apis.length === 0) setApis(await api.apis(projectId));
    if (tab === "impact" && sourceFiles.length === 0) setSourceFiles(await api.sourceFiles(projectId));
    if (tab === "graph" && !graph) setGraph(await api.graph(projectId));
  }

  function changeTab(tab: TabKey) {
    setActiveTab(tab);
    void safeRun(() => loadTabData(tab));
  }

  useEffect(() => {
    void safeRun(loadProjects);
  }, []);

  return (
    <div className="min-h-screen bg-slate-100 text-slate-950">
      <header className="sticky top-0 z-20 border-b border-slate-200 bg-white/95 backdrop-blur">
        <div className="mx-auto flex min-h-[72px] max-w-[1560px] items-center justify-between gap-4 px-6">
          <div className="flex items-center gap-3">
            <div className="grid h-11 w-11 place-items-center rounded-lg bg-blue-950 text-sm font-black text-white">
              CA
            </div>
            <div>
              <h1 className="text-xl font-black">CodeAtlas</h1>
              <p className="text-sm text-slate-500">Enterprise legacy intelligence dashboard</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Badge>React Dashboard</Badge>
            <Button variant="secondary" onClick={() => safeRun(loadProjects)}>
              <RefreshCw className="mr-2 h-4 w-4" />
              새로고침
            </Button>
          </div>
        </div>
      </header>

      <main className="mx-auto grid max-w-[1560px] grid-cols-[minmax(300px,390px)_minmax(0,1fr)] gap-5 p-5 max-xl:grid-cols-1">
        <Card className="self-start p-5">
          <div className="mb-1 text-xs font-black uppercase tracking-widest text-blue-700">
            Repository Intake
          </div>
          <div className="mb-4 flex items-start justify-between gap-3">
            <div>
              <h2 className="text-xl font-black">프로젝트</h2>
              <p className="text-sm text-slate-500">분석할 레거시 자산을 등록하고 관리합니다.</p>
            </div>
            <Badge>{projects.length}개</Badge>
          </div>

          <form onSubmit={(event) => safeRun(() => createProject(event))} className="grid gap-3 rounded-lg border border-slate-200 bg-slate-50 p-3">
            <div>
              <strong className="text-sm">경로 등록</strong>
              <p className="text-xs text-slate-500">서버에서 접근 가능한 소스 경로</p>
            </div>
            <Label title="이름">
              <Input value={projectName} onChange={(event) => setProjectName(event.target.value)} />
            </Label>
            <Label title="소스 경로">
              <Input value={sourcePath} onChange={(event) => setSourcePath(event.target.value)} />
            </Label>
            <Button type="submit">등록</Button>
          </form>

          <form onSubmit={(event) => safeRun(() => uploadProject(event))} className="mt-3 grid gap-3 rounded-lg border border-slate-200 bg-white p-3">
            <div>
              <strong className="text-sm">ZIP 업로드</strong>
              <p className="text-xs text-slate-500">로컬 ZIP 파일을 업로드해 분석 프로젝트로 등록합니다.</p>
            </div>
            <Label title="이름">
              <Input value={uploadName} onChange={(event) => setUploadName(event.target.value)} />
            </Label>
            <Label title="ZIP 파일">
              <Input
                type="file"
                accept=".zip,application/zip,application/x-zip-compressed"
                onChange={(event) => setUploadFile(event.target.files?.[0] ?? null)}
              />
            </Label>
            {uploadFile && (
              <div className="break-all rounded-md bg-slate-50 px-3 py-2 text-xs font-semibold text-slate-600">
                선택됨: {uploadFile.name}
              </div>
            )}
            <Button type="submit">ZIP 업로드</Button>
          </form>

          <div className="mt-5 mb-2 flex justify-between text-xs font-bold text-slate-500">
            <span>프로젝트 목록</span>
            <span>선택 시 동기화</span>
          </div>
          <div className="grid max-h-[360px] gap-2 overflow-auto pr-1">
            {projects.map((project) => (
              <button
                key={project.id}
                type="button"
                onClick={() => safeRun(() => selectProject(project))}
                className={[
                  "rounded-lg border bg-white p-3 text-left transition hover:border-blue-300",
                  selectedProject?.id === project.id ? "border-blue-700 bg-blue-50 shadow-[inset_3px_0_0_#1d4ed8]" : "border-slate-200"
                ].join(" ")}
              >
                <strong className="block break-all text-sm">{project.name}</strong>
                <span className="mt-1 block break-all text-xs text-slate-500">#{project.id} {project.sourcePath}</span>
              </button>
            ))}
          </div>
        </Card>

        <Card className="overflow-hidden">
          <section className="flex items-start justify-between gap-4 border-b border-slate-200 p-5 max-md:flex-col">
            <div>
              <div className="mb-1 text-xs font-black uppercase tracking-widest text-blue-700">
                Analysis Workspace
              </div>
              <h2 className="text-2xl font-black">{selectedProject?.name ?? "프로젝트를 선택하세요"}</h2>
              <p className="mt-1 break-all text-sm text-slate-500">{selectedProject?.sourcePath}</p>
            </div>
            <div className="flex flex-wrap gap-2">
              <Button disabled={!selectedProject} onClick={() => safeRun(analyzeSelectedProject)}>
                분석 실행
              </Button>
              <Button variant="secondary" disabled={!selectedProject} onClick={() => safeRun(resetSelectedProject)}>
                결과 초기화
              </Button>
            </div>
          </section>

          <div className={["mx-5 mt-4 rounded-lg border p-3 text-sm", isError ? "border-red-200 bg-red-50 text-red-700" : "border-slate-200 bg-slate-50 text-slate-700"].join(" ")}>
            {status}
          </div>

          <MetricGrid dashboard={dashboard} />

          <nav className="mx-5 mt-4 flex flex-wrap gap-1 rounded-lg border border-slate-200 bg-slate-50 p-1">
            {tabs.map((tab) => (
              <button
                key={tab.key}
                type="button"
                onClick={() => changeTab(tab.key)}
                className={[
                  "rounded-md px-3 py-2 text-sm font-bold transition",
                  activeTab === tab.key ? "bg-blue-700 text-white" : "text-slate-700 hover:bg-white hover:text-blue-700"
                ].join(" ")}
              >
                {tab.label}
              </button>
            ))}
          </nav>

          <section className="p-5">
            {!selectedProject ? (
              <EmptyState message="프로젝트를 선택하면 분석 결과를 볼 수 있습니다." />
            ) : (
              <>
                {activeTab === "apis" && <ApiTab projectId={selectedProject.id} apis={apis} />}
                {activeTab === "tables" && <TableTab projectId={selectedProject.id} tables={tables} />}
                {activeTab === "flows" && <FlowTab flows={flows} />}
                {activeTab === "graph" && <GraphTab graph={graph} />}
                {activeTab === "impact" && <ImpactTab apis={apis} flows={flows} sourceFiles={sourceFiles} />}
                {activeTab === "compare" && <CompareTab projectId={selectedProject.id} />}
                {activeTab === "qa" && <QaTab projectId={selectedProject.id} />}
                {activeTab === "docs" && <DocsTab projectId={selectedProject.id} />}
              </>
            )}
          </section>
        </Card>
      </main>
    </div>
  );
}

function Label({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <label className="grid gap-1 text-xs font-extrabold uppercase text-slate-700">
      {title}
      {children}
    </label>
  );
}

function MetricGrid({ dashboard }: { dashboard: Dashboard | null }) {
  const metrics = [
    ["Source files", dashboard?.sourceFileCount ?? 0, "스캔된 Java/XML 자산", FileText],
    ["Spring APIs", dashboard?.springApiCount ?? 0, "노출 엔드포인트", Activity],
    ["MyBatis SQL", dashboard?.myBatisStatementCount ?? 0, "매퍼 SQL 구문", GitBranch],
    ["API flows", dashboard?.apiFlowCount ?? 0, "API-Service-SQL 연결", Network]
  ] as const;

  return (
    <div className="grid grid-cols-4 gap-3 px-5 pt-4 max-lg:grid-cols-2 max-sm:grid-cols-1">
      {metrics.map(([label, value, caption, Icon]) => (
        <div key={label} className="rounded-lg border border-slate-200 bg-white p-4">
          <div className="mb-3 flex items-center justify-between">
            <span className="text-3xl font-black text-blue-950">{value}</span>
            <Icon className="h-5 w-5 text-blue-700" />
          </div>
          <strong className="block text-sm">{label}</strong>
          <small className="mt-1 block text-xs text-slate-500">{caption}</small>
        </div>
      ))}
    </div>
  );
}

function ApiTab({ projectId, apis }: { projectId: number; apis: SpringApi[] }) {
  const [query, setQuery] = useState("");
  const [method, setMethod] = useState("");
  const [openApiId, setOpenApiId] = useState<number | null>(null);
  const [detail, setDetail] = useState<SpringApiDetail | null>(null);
  const [detailError, setDetailError] = useState("");
  const filtered = apis.filter((item) => {
    const searchable = `${item.httpMethod} ${item.path} ${item.controllerClassName} ${item.methodName} ${item.sourceFilePath}`;
    return (!method || item.httpMethod === method) && includesQuery(searchable, normalize(query));
  });

  async function toggleDetail(apiId: number) {
    if (openApiId === apiId) {
      setOpenApiId(null);
      setDetail(null);
      setDetailError("");
      return;
    }
    setOpenApiId(apiId);
    setDetail(null);
    setDetailError("");
    try {
      setDetail(await api.apiDetail(projectId, apiId));
    } catch (exception) {
      setDetailError(exception instanceof Error ? exception.message : "API 상세 정보를 불러오지 못했습니다.");
    }
  }

  return (
    <div className="grid gap-3">
      <SectionTitle title="API 목록" description="컨트롤러 엔드포인트와 원본 위치를 추적합니다." />
      <FilterRow>
        <Input placeholder="path, controller, method, source" value={query} onChange={(event) => setQuery(event.target.value)} />
        <Select value={method} onChange={(event) => setMethod(event.target.value)}>
          <option value="">전체</option>
          {["GET", "POST", "PUT", "PATCH", "DELETE"].map((value) => (
            <option key={value} value={value}>{value}</option>
          ))}
        </Select>
        <Badge>{filtered.length}/{apis.length}건</Badge>
      </FilterRow>
      <ResultList empty="분석된 API가 없습니다.">
        {filtered.map((item) => (
          <DetailItem key={item.id}>
            <Row
              title={`${item.httpMethod} ${item.path}`}
              meta={`${item.controllerClassName}.${item.methodName}()`}
              detail={`${item.sourceFilePath}:${item.lineNumber ?? "unknown"}`}
              actionLabel={openApiId === item.id ? "닫기" : "상세"}
              onAction={() => void toggleDetail(item.id)}
            />
            {openApiId === item.id && (
              <ApiDetailPanel detail={detail} error={detailError} />
            )}
          </DetailItem>
        ))}
      </ResultList>
    </div>
  );
}

function TableTab({ projectId, tables }: { projectId: number; tables: TableUsage[] }) {
  const [query, setQuery] = useState("");
  const [openTableName, setOpenTableName] = useState<string | null>(null);
  const [detail, setDetail] = useState<TableUsageDetail | null>(null);
  const [detailError, setDetailError] = useState("");
  const filtered = tables.filter((table) => includesQuery(table.tableName, normalize(query)));

  async function toggleDetail(tableName: string) {
    if (openTableName === tableName) {
      setOpenTableName(null);
      setDetail(null);
      setDetailError("");
      return;
    }
    setOpenTableName(tableName);
    setDetail(null);
    setDetailError("");
    try {
      setDetail(await api.tableDetail(projectId, tableName));
    } catch (exception) {
      setDetailError(exception instanceof Error ? exception.message : "테이블 상세 정보를 불러오지 못했습니다.");
    }
  }

  return (
    <div className="grid gap-3">
      <SectionTitle title="테이블 사용처" description="SQL 구문에서 참조된 데이터 자산을 집계합니다." />
      <FilterRow>
        <Input placeholder="table name" value={query} onChange={(event) => setQuery(event.target.value)} />
        <Badge>{filtered.length}/{tables.length}건</Badge>
      </FilterRow>
      <ResultList empty="분석된 테이블이 없습니다.">
        {filtered.map((table) => (
          <DetailItem key={table.tableName}>
            <Row
              title={table.tableName}
              meta={`${table.statementCount} SQL statement(s)`}
              actionLabel={openTableName === table.tableName ? "닫기" : "상세"}
              onAction={() => void toggleDetail(table.tableName)}
            />
            {openTableName === table.tableName && (
              <TableDetailPanel detail={detail} error={detailError} />
            )}
          </DetailItem>
        ))}
      </ResultList>
    </div>
  );
}

function FlowTab({ flows }: { flows: ApiFlow[] }) {
  const [query, setQuery] = useState("");
  const [method, setMethod] = useState("");
  const [table, setTable] = useState("");
  const filtered = flows.filter((flow) => {
    const tableNames = flow.tableNames ?? [];
    const searchable = `${flow.httpMethod} ${flow.apiPath} ${flow.controllerClassName} ${flow.serviceClassName} ${flow.mapperNamespace} ${flow.mapperStatementId} ${tableNames.join(" ")}`;
    return (!method || flow.httpMethod === method)
      && (!table || tableNames.some((name) => includesQuery(name, normalize(table))))
      && includesQuery(searchable, normalize(query));
  });

  return (
    <div className="grid gap-3">
      <SectionTitle title="API Flow" description="요청 진입점부터 매퍼와 테이블까지의 호출 경로입니다." />
      <FilterRow>
        <Input placeholder="api, service, mapper, table" value={query} onChange={(event) => setQuery(event.target.value)} />
        <Select value={method} onChange={(event) => setMethod(event.target.value)}>
          <option value="">전체</option>
          {["GET", "POST", "PUT", "PATCH", "DELETE"].map((value) => (
            <option key={value} value={value}>{value}</option>
          ))}
        </Select>
        <Input placeholder="TB_ORDER" value={table} onChange={(event) => setTable(event.target.value)} />
        <Badge>{filtered.length}/{flows.length}건</Badge>
      </FilterRow>
      <ResultList empty="분석된 Flow가 없습니다.">
        {filtered.map((flow) => (
          <Row
            key={flow.id}
            title={`${flow.httpMethod} ${flow.apiPath}`}
            meta={`${flow.controllerClassName}.${flow.controllerMethodName}()`}
            detail={`${flow.serviceClassName}.${flow.serviceMethodName}() -> ${flow.mapperStatementId} [${flow.mapperStatementType}] -> ${(flow.tableNames ?? []).join(", ")}`}
          />
        ))}
      </ResultList>
    </div>
  );
}

function GraphTab({ graph }: { graph: ProjectGraph | null }) {
  const [query, setQuery] = useState("");
  const [typeFilter, setTypeFilter] = useState<GraphTypeFilter>("ALL");
  const [focusedOnly, setFocusedOnly] = useState(false);
  const filteredGraph = useMemo(
    () => filterProjectGraph(graph, query, typeFilter, focusedOnly),
    [focusedOnly, graph, query, typeFilter]
  );
  const elements = useMemo(() => toReactFlowElements(filteredGraph, query), [filteredGraph, query]);

  return (
    <div className="grid gap-3">
      <SectionTitle title="Method-Level Dependency Graph" description="API부터 클래스와 메서드 호출, 매퍼 SQL, 테이블까지의 관계를 확인합니다." />
      <FilterRow>
        <Input placeholder="API, class, method, table" value={query} onChange={(event) => setQuery(event.target.value)} />
        <Select value={typeFilter} onChange={(event) => setTypeFilter(event.target.value as GraphTypeFilter)}>
          <option value="ALL">전체</option>
          <option value="API">API</option>
          <option value="CLASS">Class</option>
          <option value="METHOD">Method</option>
          <option value="MAPPER_METHOD">Mapper SQL</option>
          <option value="TABLE">Table</option>
        </Select>
        <label className="flex items-center gap-2 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm font-bold text-slate-700">
          <input
            type="checkbox"
            checked={focusedOnly}
            onChange={(event) => setFocusedOnly(event.target.checked)}
          />
          연결 경로만
        </label>
        <Badge>{filteredGraph?.nodes.length ?? 0}/{graph?.nodes.length ?? 0} nodes</Badge>
      </FilterRow>
      <div className="h-[620px] overflow-hidden rounded-lg border border-slate-200 bg-slate-50">
        <ReactFlow nodes={elements.nodes} edges={elements.edges} fitView>
          <Background />
          <Controls />
        </ReactFlow>
      </div>
    </div>
  );
}

function ImpactTab({ apis, flows, sourceFiles }: { apis: SpringApi[]; flows: ApiFlow[]; sourceFiles: SourceFile[] }) {
  const [type, setType] = useState("TABLE");
  const [query, setQuery] = useState("TB_ORDER");
  const normalizedQuery = normalize(query);
  const dtoMatches = useMemo(() => {
    if (type !== "DTO" || !normalizedQuery) {
      return { dtoNames: new Set<string>(), fields: [] as Array<{ dtoName: string; fieldName: string; fieldType: string }> };
    }

    const fields: Array<{ dtoName: string; fieldName: string; fieldType: string }> = [];
    const dtoNames = new Set<string>();

    sourceFiles
      .filter((file) => file.classCategory === "DTO" && file.className)
      .forEach((file) => {
        const className = file.className ?? "";
        const matchedClass = includesQuery(className, normalizedQuery) || includesQuery(file.packageName ?? "", normalizedQuery);
        const matchedFields = file.classFields.filter((field) =>
          includesQuery(`${field.name} ${field.type}`, normalizedQuery)
        );

        if (matchedClass || matchedFields.length > 0) {
          dtoNames.add(className);
        }

        matchedFields.forEach((field) => {
          fields.push({ dtoName: className, fieldName: field.name, fieldType: field.type });
        });
      });

    return { dtoNames, fields };
  }, [normalizedQuery, sourceFiles, type]);

  const matchedApis = useMemo(() => {
    if (type !== "DTO") return [];
    return apis.filter((item) => {
      const dtoNames = [item.requestDtoName, item.responseDtoName].filter(Boolean) as string[];
      if (dtoNames.some((dtoName) => includesQuery(dtoName, normalizedQuery))) return true;
      return dtoNames.some((dtoName) => dtoMatches.dtoNames.has(dtoName));
    });
  }, [apis, dtoMatches.dtoNames, normalizedQuery, type]);

  const dtoApiKeys = new Set(matchedApis.map((item) => `${item.httpMethod} ${item.path}`));
  const matches = flows.filter((flow) => {
    const tables = flow.tableNames ?? [];
    if (!normalizedQuery) return false;
    if (type === "DTO") return dtoApiKeys.has(`${flow.httpMethod} ${flow.apiPath}`);
    if (type === "TABLE") return tables.some((table) => includesQuery(table, normalizedQuery));
    if (type === "API") return includesQuery(`${flow.httpMethod} ${flow.apiPath}`, normalizedQuery);
    if (type === "MAPPER") return includesQuery(`${flow.mapperNamespace}.${flow.mapperStatementId}`, normalizedQuery);
    if (type === "SQL") return includesQuery(`${flow.mapperStatementId} ${flow.mapperStatementType} ${tables.join(" ")}`, normalizedQuery);
    return false;
  });
  const impactedApis = type === "DTO"
    ? new Set(matchedApis.map((item) => `${item.httpMethod} ${item.path}`))
    : new Set(matches.map((flow) => `${flow.httpMethod} ${flow.apiPath}`));
  const mappers = new Set(matches.map((flow) => `${flow.mapperNamespace}.${flow.mapperStatementId}`));
  const tables = new Set(matches.flatMap((flow) => flow.tableNames ?? []));
  const dtoNames = Array.from(dtoMatches.dtoNames).sort((left, right) => left.localeCompare(right));

  return (
    <div className="grid gap-3">
      <SectionTitle title="변경 영향도 분석" description="Flow와 DTO 메타데이터를 기준으로 변경 영향을 계산합니다." />
      <FilterRow>
        <Select value={type} onChange={(event) => setType(event.target.value)}>
          <option value="TABLE">Table</option>
          <option value="API">API</option>
          <option value="MAPPER">Mapper</option>
          <option value="SQL">SQL keyword</option>
          <option value="DTO">DTO</option>
        </Select>
        <Input value={query} onChange={(event) => setQuery(event.target.value)} />
        <Badge>{type === "DTO" ? `${matchedApis.length} API(s)` : `${matches.length} flow(s)`}</Badge>
      </FilterRow>
      <div className="grid grid-cols-3 gap-3 max-md:grid-cols-1">
        <ImpactMetric title="APIs" value={impactedApis.size} />
        <ImpactMetric title="Mappers" value={mappers.size} />
        <ImpactMetric title="Tables" value={tables.size} />
      </div>
      {type === "DTO" && (
        <ResultList empty="매칭된 DTO가 없습니다. DTO 클래스명 또는 필드명을 입력하세요.">
          {dtoNames.map((dtoName) => (
            <Row
              key={dtoName}
              title={dtoName}
              meta="Matched DTO"
              detail={dtoMatches.fields
                .filter((field) => field.dtoName === dtoName)
                .map((field) => `${field.fieldName}:${field.fieldType}`)
                .join(", ") || "class/request/response match"}
            />
          ))}
        </ResultList>
      )}
      {type === "DTO" && (
        <ResultList empty="DTO와 연결된 API가 없습니다.">
          {matchedApis.map((item) => (
            <Row
              key={item.id}
              title={`${item.httpMethod} ${item.path}`}
              meta={`${item.controllerClassName}.${item.methodName}()`}
              detail={`request=${item.requestDtoName ?? "-"} response=${item.responseDtoName ?? "-"}`}
            />
          ))}
        </ResultList>
      )}
      <ResultList empty="영향 Flow가 없습니다.">
        {matches.map((flow) => (
          <Row
            key={flow.id}
            title={`${flow.httpMethod} ${flow.apiPath}`}
            meta={`${flow.serviceClassName}.${flow.serviceMethodName}()`}
            detail={`${flow.mapperNamespace}.${flow.mapperStatementId} -> ${(flow.tableNames ?? []).join(", ")}`}
          />
        ))}
      </ResultList>
    </div>
  );
}

function CompareTab({ projectId }: { projectId: number }) {
  const [comparison, setComparison] = useState<AnalysisComparison | null>(null);
  const [snapshots, setSnapshots] = useState<AnalysisSnapshot[]>([]);
  const [baseSnapshotId, setBaseSnapshotId] = useState("");
  const [targetSnapshotId, setTargetSnapshotId] = useState("");
  const [snapshotLabel, setSnapshotLabel] = useState("");
  const [snapshotNote, setSnapshotNote] = useState("");
  const [report, setReport] = useState<GeneratedDocument | null>(null);
  const [githubDraft, setGithubDraft] = useState<GitHubReleaseDraft | null>(null);
  const [githubPublishResult, setGithubPublishResult] = useState<GitHubReleasePublishResult | null>(null);
  const [githubPublishHistory, setGithubPublishHistory] = useState<GitHubReleasePublishResult[]>([]);
  const [riskTrend, setRiskTrend] = useState<ReleaseRiskTrendPoint[]>([]);
  const [error, setError] = useState("");

  async function loadComparison() {
    setError("");
    try {
      setComparison(await api.comparison(projectId));
      setReport(null);
    } catch (exception) {
      setComparison(null);
      setError(exception instanceof Error ? exception.message : "비교 결과를 불러오지 못했습니다.");
    }
  }

  async function loadSnapshots() {
    setError("");
    try {
      const nextSnapshots = await api.snapshots(projectId);
      const nextTrend = await api.riskTrend(projectId);
      const nextPublishHistory = await api.githubPublishHistory(projectId);
      setSnapshots(nextSnapshots);
      setRiskTrend(nextTrend);
      setGithubPublishHistory(nextPublishHistory);
      setTargetSnapshotId(String(nextSnapshots[0]?.id ?? ""));
      setBaseSnapshotId(String(nextSnapshots[1]?.id ?? ""));
      setSnapshotLabel(nextSnapshots[0]?.label ?? "");
      setSnapshotNote(nextSnapshots[0]?.note ?? "");
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : "스냅샷 목록을 불러오지 못했습니다.");
    }
  }

  async function compareSelected(event: React.FormEvent) {
    event.preventDefault();
    setError("");
    if (!baseSnapshotId || !targetSnapshotId) {
      setError("비교할 기준/대상 스냅샷을 선택하세요.");
      return;
    }
    try {
      setComparison(await api.compareSnapshots(projectId, Number(baseSnapshotId), Number(targetSnapshotId)));
      setReport(null);
    } catch (exception) {
      setComparison(null);
      setError(exception instanceof Error ? exception.message : "선택한 스냅샷을 비교하지 못했습니다.");
    }
  }

  async function loadReport() {
    setError("");
    try {
      if (baseSnapshotId && targetSnapshotId) {
        setReport(await api.snapshotComparisonReport(projectId, Number(baseSnapshotId), Number(targetSnapshotId)));
      } else {
        setReport(await api.comparisonReport(projectId));
      }
      setGithubDraft(null);
      setGithubPublishResult(null);
    } catch (exception) {
      setReport(null);
      setError(exception instanceof Error ? exception.message : "비교 리포트를 생성하지 못했습니다.");
    }
  }

  async function loadChecklist() {
    setError("");
    try {
      if (baseSnapshotId && targetSnapshotId) {
        setReport(await api.snapshotReleaseChecklist(projectId, Number(baseSnapshotId), Number(targetSnapshotId)));
      } else {
        setReport(await api.releaseChecklist(projectId));
      }
      setGithubDraft(null);
      setGithubPublishResult(null);
    } catch (exception) {
      setReport(null);
      setError(exception instanceof Error ? exception.message : "릴리즈 체크리스트를 생성하지 못했습니다.");
    }
  }

  async function loadReleaseNotes() {
    setError("");
    try {
      if (baseSnapshotId && targetSnapshotId) {
        setReport(await api.snapshotReleaseNotes(projectId, Number(baseSnapshotId), Number(targetSnapshotId)));
      } else {
        setReport(await api.releaseNotes(projectId));
      }
      setGithubDraft(null);
      setGithubPublishResult(null);
    } catch (exception) {
      setReport(null);
      setGithubPublishResult(null);
      setError(exception instanceof Error ? exception.message : "릴리즈 노트를 생성하지 못했습니다.");
    }
  }

  async function publishGitHubDraft() {
    setError("");
    try {
      const result = baseSnapshotId && targetSnapshotId
        ? await api.publishSnapshotGitHubReleaseDraft(projectId, Number(baseSnapshotId), Number(targetSnapshotId))
        : await api.publishGitHubReleaseDraft(projectId);
      setGithubPublishResult(result);
      setGithubPublishHistory(await api.githubPublishHistory(projectId));
    } catch (exception) {
      setGithubPublishResult(null);
      setError(exception instanceof Error ? exception.message : "GitHub Release Draft 게시에 실패했습니다.");
    }
  }

  async function loadGitHubDraft() {
    setError("");
    try {
      if (baseSnapshotId && targetSnapshotId) {
        setGithubDraft(await api.snapshotGitHubReleaseDraft(projectId, Number(baseSnapshotId), Number(targetSnapshotId)));
      } else {
        setGithubDraft(await api.githubReleaseDraft(projectId));
      }
      setReport(null);
    } catch (exception) {
      setGithubDraft(null);
      setError(exception instanceof Error ? exception.message : "GitHub Release Draft를 생성하지 못했습니다.");
    }
  }

  function downloadReport() {
    api.downloadComparisonReport(
      projectId,
      baseSnapshotId ? Number(baseSnapshotId) : undefined,
      targetSnapshotId ? Number(targetSnapshotId) : undefined
    );
  }

  function downloadChecklist() {
    api.downloadReleaseChecklist(
      projectId,
      baseSnapshotId ? Number(baseSnapshotId) : undefined,
      targetSnapshotId ? Number(targetSnapshotId) : undefined
    );
  }

  function downloadReleaseNotes() {
    api.downloadReleaseNotes(
      projectId,
      baseSnapshotId ? Number(baseSnapshotId) : undefined,
      targetSnapshotId ? Number(targetSnapshotId) : undefined
    );
  }

  async function saveTargetSnapshotMetadata(event: React.FormEvent) {
    event.preventDefault();
    setError("");
    if (!targetSnapshotId) {
      setError("메타데이터를 저장할 대상 스냅샷을 선택하세요.");
      return;
    }
    try {
      const updated = await api.updateSnapshot(projectId, Number(targetSnapshotId), {
        label: snapshotLabel,
        note: snapshotNote
      });
      setSnapshots((current) => current.map((snapshot) => snapshot.id === updated.id ? updated : snapshot));
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : "스냅샷 메타데이터를 저장하지 못했습니다.");
    }
  }

  const targetSnapshot = snapshots.find((snapshot) => String(snapshot.id) === targetSnapshotId);

  return (
    <div className="grid gap-3">
      <SectionTitle title="분석 이력 비교" description="최근 두 번의 완료된 분석 스냅샷에서 API, SQL, DTO, Flow 변경점을 비교합니다." />
      <div className="flex flex-wrap items-center gap-2 rounded-lg border border-slate-200 bg-slate-50 p-3">
        <Button variant="secondary" onClick={loadSnapshots}>스냅샷 목록</Button>
        <Button onClick={loadComparison}>최신 2개 분석 비교</Button>
        <Button variant="secondary" onClick={loadReport}>Markdown 리포트</Button>
        <Button variant="secondary" onClick={loadChecklist}>체크리스트</Button>
        <Button variant="secondary" onClick={loadReleaseNotes}>릴리즈 노트</Button>
        <Button variant="secondary" onClick={loadGitHubDraft}>GitHub Draft</Button>
        <Button variant="secondary" onClick={publishGitHubDraft}>GitHub 게시</Button>
        <Button variant="secondary" onClick={downloadReport}>다운로드</Button>
        <Button variant="secondary" onClick={downloadChecklist}>체크리스트 다운로드</Button>
        <Button variant="secondary" onClick={downloadReleaseNotes}>릴리즈 노트 다운로드</Button>
        {comparison && (
          <Badge>{`job #${comparison.baseJobId} -> #${comparison.targetJobId}`}</Badge>
        )}
      </div>
      {snapshots.length > 0 && (
        <form onSubmit={compareSelected} className="grid grid-cols-[minmax(180px,1fr)_minmax(180px,1fr)_auto] gap-2 rounded-lg border border-slate-200 bg-white p-3 max-lg:grid-cols-1">
          <Label title="기준 스냅샷">
            <Select value={baseSnapshotId} onChange={(event) => setBaseSnapshotId(event.target.value)}>
              <option value="">선택</option>
              {snapshots.map((snapshot) => (
                <option key={snapshot.id} value={snapshot.id}>
                  #{snapshot.id} job #{snapshot.analysisJobId} {new Date(snapshot.createdAt).toLocaleString()}
                </option>
              ))}
            </Select>
          </Label>
          <Label title="대상 스냅샷">
            <Select
              value={targetSnapshotId}
              onChange={(event) => {
                const nextId = event.target.value;
                const nextSnapshot = snapshots.find((snapshot) => String(snapshot.id) === nextId);
                setTargetSnapshotId(nextId);
                setSnapshotLabel(nextSnapshot?.label ?? "");
                setSnapshotNote(nextSnapshot?.note ?? "");
              }}
            >
              <option value="">선택</option>
              {snapshots.map((snapshot) => (
                <option key={snapshot.id} value={snapshot.id}>
                  #{snapshot.id} {snapshot.label ? `[${snapshot.label}] ` : ""}job #{snapshot.analysisJobId} {new Date(snapshot.createdAt).toLocaleString()}
                </option>
              ))}
            </Select>
          </Label>
          <Button type="submit">선택 비교</Button>
        </form>
      )}
      {targetSnapshot && (
        <form onSubmit={saveTargetSnapshotMetadata} className="grid grid-cols-[minmax(160px,0.7fr)_minmax(220px,1fr)_auto] gap-2 rounded-lg border border-slate-200 bg-white p-3 max-lg:grid-cols-1">
          <Label title="대상 라벨">
            <Input value={snapshotLabel} onChange={(event) => setSnapshotLabel(event.target.value)} placeholder="baseline, v1.2.0, release-candidate" />
          </Label>
          <Label title="노트">
            <Input value={snapshotNote} onChange={(event) => setSnapshotNote(event.target.value)} placeholder="릴리즈 메모 또는 기준 설명" />
          </Label>
          <Button type="submit">메타데이터 저장</Button>
        </form>
      )}
      {riskTrend.length > 0 && <RiskTrend points={riskTrend} />}
      {error && <EmptyState message={error} />}
      {report && (
        <pre className="max-h-[520px] overflow-auto rounded-lg border border-slate-800 bg-slate-950 p-4 text-xs leading-relaxed text-blue-100">
          {report.content}
        </pre>
      )}
      {githubDraft && <GitHubDraftPanel draft={githubDraft} />}
      {githubPublishResult && (
        <Row
          title={`GitHub Release Draft ${githubPublishResult.status}`}
          meta={`${githubPublishResult.tagName} · #${githubPublishResult.baseSnapshotId} -> #${githubPublishResult.targetSnapshotId}`}
          detail={githubPublishResult.errorMessage ?? githubPublishResult.htmlUrl ?? githubPublishResult.apiUrl ?? githubPublishResult.releaseName}
        />
      )}
      {githubPublishHistory.length > 0 && <GitHubPublishHistoryPanel history={githubPublishHistory} />}
      {comparison && (
        <div className="grid gap-3">
          <div className="grid grid-cols-5 gap-3 max-lg:grid-cols-2 max-sm:grid-cols-1">
            <ImpactMetric title={`Risk: ${comparison.releaseRisk.riskLevel}`} value={comparison.releaseRisk.readinessScore} />
            <ImpactMetric title="API changes" value={comparison.apis.addedCount + comparison.apis.removedCount} />
            <ImpactMetric title="SQL changes" value={comparison.sqlStatements.addedCount + comparison.sqlStatements.removedCount} />
            <ImpactMetric title="DTO changes" value={comparison.dtos.addedCount + comparison.dtos.removedCount} />
            <ImpactMetric title="Flow changes" value={comparison.flows.addedCount + comparison.flows.removedCount} />
          </div>
          <div className="rounded-lg border border-slate-200 bg-white p-3 text-sm text-slate-600">
            {comparison.releaseRisk.riskReason}
          </div>
          <DiffSection title="APIs" section={comparison.apis} />
          <DiffSection title="SQL Statements" section={comparison.sqlStatements} />
          <DiffSection title="DTOs" section={comparison.dtos} />
          <DiffSection title="Flows" section={comparison.flows} />
        </div>
      )}
    </div>
  );
}

function GitHubPublishHistoryPanel({ history }: { history: GitHubReleasePublishResult[] }) {
  return (
    <div className="grid gap-2 rounded-lg border border-slate-200 bg-white p-3">
      <div className="flex items-center justify-between gap-2">
        <strong className="text-sm">GitHub 게시 이력</strong>
        <Badge>{history.length}건</Badge>
      </div>
      <div className="grid gap-2">
        {history.map((item) => (
          <Row
            key={item.id}
            title={`${item.status} · ${item.releaseName}`}
            meta={`${item.tagName} · #${item.baseSnapshotId} -> #${item.targetSnapshotId}`}
            detail={item.errorMessage ?? item.htmlUrl ?? item.apiUrl ?? new Date(item.requestedAt).toLocaleString()}
          />
        ))}
      </div>
    </div>
  );
}

function DiffSection({ title, section }: { title: string; section: AnalysisDiffSection }) {
  return (
    <div className="grid gap-2 rounded-lg border border-slate-200 bg-white p-3">
      <div className="flex items-center justify-between gap-2">
        <strong className="text-sm">{title}</strong>
        <Badge>+{section.addedCount} / -{section.removedCount}</Badge>
      </div>
      <div className="grid grid-cols-2 gap-2 max-lg:grid-cols-1">
        <DiffList title="Added" items={section.added} tone="text-emerald-700" />
        <DiffList title="Removed" items={section.removed} tone="text-red-700" />
      </div>
    </div>
  );
}

function GitHubDraftPanel({ draft }: { draft: GitHubReleaseDraft }) {
  return (
    <div className="grid gap-3 rounded-lg border border-slate-200 bg-white p-3">
      <div className="flex flex-wrap items-center gap-2">
        <strong className="text-sm">GitHub Release Draft</strong>
        <Badge>{draft.tagName}</Badge>
        <Badge>{draft.draft ? "draft" : "published"}</Badge>
        {draft.prerelease && <Badge>prerelease</Badge>}
      </div>
      <div className="grid grid-cols-2 gap-2 max-lg:grid-cols-1">
        <Row title="Release name" detail={draft.releaseName} />
        <Row title="Tag name" detail={draft.tagName} />
      </div>
      <pre className="max-h-[420px] overflow-auto rounded-lg border border-slate-800 bg-slate-950 p-4 text-xs leading-relaxed text-blue-100">
        {draft.body}
      </pre>
    </div>
  );
}

function RiskTrend({ points }: { points: ReleaseRiskTrendPoint[] }) {
  return (
    <div className="grid gap-2 rounded-lg border border-slate-200 bg-white p-3">
      <div className="flex items-center justify-between gap-2">
        <strong className="text-sm">Release Risk Trend</strong>
        <Badge>{points.length} comparison(s)</Badge>
      </div>
      <div className="grid gap-2">
        {points.map((point) => (
          <div
            key={`${point.baseSnapshotId}-${point.targetSnapshotId}`}
            className="grid grid-cols-[150px_minmax(0,1fr)_90px] items-center gap-3 max-lg:grid-cols-1"
          >
            <span className="text-xs font-bold text-slate-500">
              {`#${point.baseSnapshotId} -> #${point.targetSnapshotId}`}
            </span>
            <div className="h-3 overflow-hidden rounded-full bg-slate-100">
              <div
                className={["h-full rounded-full", riskBarClass(point.releaseRisk.riskLevel)].join(" ")}
                style={{ width: `${Math.max(4, point.releaseRisk.readinessScore)}%` }}
              />
            </div>
            <span className="text-xs font-black text-slate-700">
              {point.releaseRisk.readinessScore} / {point.releaseRisk.riskLevel}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}

function riskBarClass(riskLevel: string) {
  if (riskLevel === "HIGH") return "bg-red-500";
  if (riskLevel === "MEDIUM") return "bg-amber-500";
  if (riskLevel === "LOW") return "bg-blue-500";
  return "bg-emerald-500";
}

function DiffList({ title, items, tone }: { title: string; items: string[]; tone: string }) {
  return (
    <div className="rounded-md border border-slate-200 bg-slate-50 p-3">
      <strong className={`mb-2 block text-xs uppercase ${tone}`}>{title}</strong>
      {items.length === 0 ? (
        <span className="text-xs text-slate-500">변경 없음</span>
      ) : (
        <ul className="grid gap-1">
          {items.map((item) => (
            <li key={item} className="break-all rounded bg-white px-2 py-1 text-xs text-slate-700">
              {item}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

function QaTab({ projectId }: { projectId: number }) {
  const [question, setQuestion] = useState("TB_ORDER 어디서 사용돼?");
  const [answer, setAnswer] = useState<QuestionAnswer | null>(null);

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setAnswer(await api.ask(projectId, question));
  }

  return (
    <div className="grid gap-3">
      <form onSubmit={submit} className="grid grid-cols-[minmax(0,1fr)_auto] gap-2 max-sm:grid-cols-1">
        <Input value={question} onChange={(event) => setQuestion(event.target.value)} />
        <Button type="submit">질문</Button>
      </form>
      <pre className="min-h-40 overflow-auto rounded-lg border border-slate-800 bg-slate-950 p-4 text-xs leading-relaxed text-blue-100">
        {answer ? JSON.stringify(answer, null, 2) : "질문하면 근거 기반 답변이 표시됩니다."}
      </pre>
    </div>
  );
}

function DocsTab({ projectId }: { projectId: number }) {
  const [document, setDocument] = useState<GeneratedDocument | null>(null);

  return (
    <div className="grid gap-3">
      <div className="flex flex-wrap gap-2">
        <Button onClick={() => api.document(projectId, "onboarding").then(setDocument)}>온보딩 문서</Button>
        <Button variant="secondary" onClick={() => api.document(projectId, "apis").then(setDocument)}>API 문서</Button>
      </div>
      <pre className="min-h-80 overflow-auto rounded-lg border border-slate-800 bg-slate-950 p-4 text-xs leading-relaxed text-blue-100">
        {document?.content ?? "문서를 생성하면 이 영역에 표시됩니다."}
      </pre>
    </div>
  );
}

function SectionTitle({ title, description }: { title: string; description: string }) {
  return (
    <div>
      <h3 className="text-lg font-black">{title}</h3>
      <p className="text-sm text-slate-500">{description}</p>
    </div>
  );
}

function DetailItem({ children }: { children: React.ReactNode }) {
  return (
    <div className="grid gap-2">
      {children}
    </div>
  );
}

function ApiDetailPanel({ detail, error }: { detail: SpringApiDetail | null; error: string }) {
  if (error) return <EmptyState message={error} />;
  if (!detail) return <EmptyState message="API 상세 정보를 불러오는 중입니다." />;

  return (
    <div className="grid gap-3 rounded-lg border border-blue-200 bg-blue-50 p-3">
      <div className="grid grid-cols-4 gap-2 max-lg:grid-cols-2 max-sm:grid-cols-1">
        <DetailMetric title="Request DTO" value={detail.api.requestDtoName ?? "-"} />
        <DetailMetric title="Response DTO" value={detail.api.responseDtoName ?? "-"} />
        <DetailMetric title="Controller" value={detail.api.controllerClassName} />
        <DetailMetric title="Method" value={detail.api.methodName} />
      </div>
      <div className="rounded-md border border-blue-100 bg-white p-3">
        <strong className="mb-2 block text-xs uppercase text-blue-700">연결 Flow</strong>
        {detail.flows.length === 0 ? (
          <span className="text-xs text-slate-500">연결된 Flow가 없습니다.</span>
        ) : (
          <div className="grid gap-2">
            {detail.flows.map((flow) => (
              <FlowDetail key={flow.id} flow={flow} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

function TableDetailPanel({ detail, error }: { detail: TableUsageDetail | null; error: string }) {
  if (error) return <EmptyState message={error} />;
  if (!detail) return <EmptyState message="테이블 상세 정보를 불러오는 중입니다." />;

  return (
    <div className="grid gap-3 rounded-lg border border-blue-200 bg-blue-50 p-3">
      <div className="grid grid-cols-3 gap-2 max-md:grid-cols-1">
        <DetailMetric title="Table" value={detail.tableName} />
        <DetailMetric title="SQL Statements" value={String(detail.statements.length)} />
        <DetailMetric title="API Flows" value={String(detail.flows.length)} />
      </div>
      <div className="grid grid-cols-2 gap-3 max-xl:grid-cols-1">
        <div className="rounded-md border border-blue-100 bg-white p-3">
          <strong className="mb-2 block text-xs uppercase text-blue-700">MyBatis Statements</strong>
          {detail.statements.length === 0 ? (
            <span className="text-xs text-slate-500">연결된 SQL이 없습니다.</span>
          ) : (
            <div className="grid gap-2">
              {detail.statements.map((statement) => (
                <div key={statement.id} className="grid gap-1 rounded-md border border-slate-200 bg-slate-50 p-2">
                  <strong className="break-all text-xs">{statement.namespace}.{statement.statementId}</strong>
                  <span className="text-xs font-bold text-slate-500">{statement.statementType} · {statement.sourceFilePath}</span>
                  <pre className="max-h-32 overflow-auto whitespace-pre-wrap rounded bg-slate-950 p-2 text-[11px] leading-relaxed text-blue-100">
                    {statement.sql}
                  </pre>
                </div>
              ))}
            </div>
          )}
        </div>
        <div className="rounded-md border border-blue-100 bg-white p-3">
          <strong className="mb-2 block text-xs uppercase text-blue-700">API Flows</strong>
          {detail.flows.length === 0 ? (
            <span className="text-xs text-slate-500">연결된 Flow가 없습니다.</span>
          ) : (
            <div className="grid gap-2">
              {detail.flows.map((flow) => (
                <FlowDetail key={flow.id} flow={flow} />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function DetailMetric({ title, value }: { title: string; value: string }) {
  return (
    <div className="rounded-md border border-blue-100 bg-white p-3">
      <span className="block text-xs font-black uppercase text-blue-700">{title}</span>
      <strong className="mt-1 block break-all text-sm text-slate-800">{value}</strong>
    </div>
  );
}

function FlowDetail({ flow }: { flow: ApiFlow }) {
  return (
    <div className="grid gap-1 rounded-md border border-slate-200 bg-slate-50 p-2">
      <strong className="break-all text-xs">{flow.httpMethod} {flow.apiPath}</strong>
      <span className="break-all text-xs text-slate-600">
        {flow.serviceClassName}.{flow.serviceMethodName}() -&gt; {flow.mapperNamespace}.{flow.mapperStatementId}
      </span>
      <span className="break-all text-xs text-slate-500">
        Tables: {(flow.tableNames ?? []).join(", ") || "-"}
      </span>
      {flow.methodCallPath.length > 0 && (
        <span className="break-all text-xs text-slate-500">
          Call path: {flow.methodCallPath.map((step) => `${step.sourceClassName}.${step.sourceMethodName} -> ${step.targetClassName}.${step.targetMethodName}`).join(" / ")}
        </span>
      )}
    </div>
  );
}

function FilterRow({ children }: { children: React.ReactNode }) {
  return (
    <div className="grid grid-cols-[minmax(220px,1fr)_repeat(3,auto)] items-end gap-2 rounded-lg border border-slate-200 bg-slate-50 p-3 max-lg:grid-cols-1">
      {children}
    </div>
  );
}

function ResultList({ children, empty }: { children: React.ReactNode; empty: string }) {
  const childArray = Array.isArray(children) ? children : [children];
  return <div className="grid gap-2">{childArray.length > 0 ? children : <EmptyState message={empty} />}</div>;
}

function Row({
  title,
  meta,
  detail,
  actionLabel,
  onAction
}: {
  title: string;
  meta?: string;
  detail?: string;
  actionLabel?: string;
  onAction?: () => void;
}) {
  return (
    <article className="grid grid-cols-[minmax(220px,1.1fr)_minmax(180px,1fr)_minmax(140px,0.8fr)_auto] items-center gap-3 rounded-lg border border-slate-200 bg-white p-3 max-lg:grid-cols-1">
      <strong className="break-all text-sm">{title}</strong>
      <span className="break-all text-xs text-slate-500">{meta}</span>
      <span className="break-all text-xs text-slate-500">{detail}</span>
      {onAction && (
        <Button variant="secondary" onClick={onAction}>
          {actionLabel ?? "상세"}
        </Button>
      )}
    </article>
  );
}

function EmptyState({ message }: { message: string }) {
  return (
    <div className="rounded-lg border border-dashed border-slate-300 bg-slate-50 p-4 text-sm text-slate-500">
      {message}
    </div>
  );
}

function ImpactMetric({ title, value }: { title: string; value: number }) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4">
      <span className="block text-3xl font-black text-blue-950">{value}</span>
      <strong className="mt-2 block text-sm">{title}</strong>
    </div>
  );
}

type GraphTypeFilter = "ALL" | "API" | "CLASS" | "METHOD" | "MAPPER_METHOD" | "TABLE";

function filterProjectGraph(
  graph: ProjectGraph | null,
  query: string,
  typeFilter: GraphTypeFilter,
  focusedOnly: boolean
): ProjectGraph | null {
  if (!graph) return null;

  const normalizedQuery = normalize(query);
  const nodeById = new Map(graph.nodes.map((node) => [node.id, node]));
  const searchableNode = (node: GraphNode) => `${node.id} ${node.type} ${node.label}`;
  const matchesQuery = (node: GraphNode) => !normalizedQuery || includesQuery(searchableNode(node), normalizedQuery);
  const matchesType = (node: GraphNode) => {
    if (typeFilter === "ALL") return true;
    if (typeFilter === "CLASS") return ["CONTROLLER", "SERVICE", "MAPPER"].includes(node.type);
    if (typeFilter === "METHOD") return node.type.endsWith("_METHOD");
    return node.type === typeFilter;
  };

  const seedIds = new Set(graph.nodes.filter(matchesQuery).map((node) => node.id));
  let visibleIds = new Set(
    graph.nodes
      .filter((node) => matchesType(node))
      .filter((node) => matchesQuery(node))
      .map((node) => node.id)
  );

  if (focusedOnly && normalizedQuery) {
    visibleIds = collectConnectedNodeIds(graph, seedIds);
  } else if (!normalizedQuery) {
    visibleIds = new Set(graph.nodes.filter(matchesType).map((node) => node.id));
  }

  const nodes = graph.nodes.filter((node) => visibleIds.has(node.id) && matchesType(node));
  const finalVisibleIds = new Set(nodes.map((node) => node.id));
  const edges = graph.edges.filter((edge) => finalVisibleIds.has(edge.source) && finalVisibleIds.has(edge.target));

  return {
    nodes,
    edges: edges.filter((edge) => nodeById.has(edge.source) && nodeById.has(edge.target))
  };
}

function collectConnectedNodeIds(graph: ProjectGraph, seedIds: Set<string>) {
  const connectedIds = new Set(seedIds);
  let changed = true;

  while (changed) {
    changed = false;
    for (const edge of graph.edges) {
      if (connectedIds.has(edge.source) || connectedIds.has(edge.target)) {
        if (!connectedIds.has(edge.source)) {
          connectedIds.add(edge.source);
          changed = true;
        }
        if (!connectedIds.has(edge.target)) {
          connectedIds.add(edge.target);
          changed = true;
        }
      }
    }
  }

  return connectedIds;
}

function toReactFlowElements(graph: ProjectGraph | null, query = ""): { nodes: Node[]; edges: Edge[] } {
  if (!graph) return { nodes: [], edges: [] };

  const normalizedQuery = normalize(query);
  const order = ["API", "CONTROLLER", "CONTROLLER_METHOD", "SERVICE", "SERVICE_METHOD", "MAPPER", "MAPPER_METHOD", "TABLE"];
  const xByType = new Map(order.map((type, index) => [type, index * 220]));
  const grouped = new Map<string, number>();

  const nodes = graph.nodes.map((node) => {
    const row = grouped.get(node.type) ?? 0;
    grouped.set(node.type, row + 1);
    const isHighlighted = normalizedQuery && includesQuery(`${node.id} ${node.type} ${node.label}`, normalizedQuery);
    return {
      id: node.id,
      position: { x: xByType.get(node.type) ?? 0, y: row * 92 },
      data: { label: `${node.type}\n${node.label}` },
      style: {
        width: 210,
        borderRadius: 8,
        border: isHighlighted ? "2px solid #1d4ed8" : "1px solid #93c5fd",
        background: isHighlighted ? "#dbeafe" : "#ffffff",
        color: "#0f172a",
        fontSize: 12,
        fontWeight: 700,
        whiteSpace: "pre-line"
      }
    } satisfies Node;
  });

  const edges = graph.edges.map((edge, index) => ({
    id: `edge-${index}`,
    source: edge.source,
    target: edge.target,
    label: edge.label,
    markerEnd: { type: MarkerType.ArrowClosed },
    style: { stroke: "#64748b" }
  }));

  return { nodes, edges };
}

export default App;
