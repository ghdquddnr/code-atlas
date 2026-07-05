export type ApiResponse<T> = {
  data: T;
};

export type Project = {
  id: number;
  name: string;
  sourcePath: string;
};

export type AnalysisJob = {
  id: number;
  status: "PENDING" | "RUNNING" | "COMPLETED" | "FAILED";
  message: string;
};

export type Dashboard = {
  projectId: number;
  projectName: string;
  sourceFileCount: number;
  springApiCount: number;
  myBatisStatementCount: number;
  apiFlowCount: number;
  tables: TableUsage[];
  latestAnalysisJob?: AnalysisJob;
};

export type SpringApi = {
  id: number;
  projectId: number;
  httpMethod: string;
  path: string;
  controllerClassName: string;
  methodName: string;
  requestDtoName?: string;
  responseDtoName?: string;
  sourceFilePath: string;
  lineNumber?: number;
  extractedAt?: string;
};

export type JavaClassField = {
  name: string;
  type: string;
};

export type SourceFile = {
  id: number;
  projectId: number;
  path: string;
  relativePath: string;
  type: "JAVA" | "XML" | "OTHER";
  packageName?: string;
  className?: string;
  classCategory?:
    | "CONTROLLER"
    | "SERVICE"
    | "REPOSITORY"
    | "MAPPER"
    | "ENTITY"
    | "DTO"
    | "CONFIGURATION"
    | "SCHEDULER"
    | "BATCH"
    | "COMPONENT"
    | "UTILITY"
    | "TEST"
    | "UNKNOWN";
  classFields: JavaClassField[];
  sizeBytes: number;
  indexedAt: string;
};

export type TableUsage = {
  tableName: string;
  statementCount: number;
};

export type ApiFlow = {
  id: number;
  httpMethod: string;
  apiPath: string;
  controllerClassName: string;
  controllerMethodName: string;
  serviceClassName: string;
  serviceMethodName: string;
  mapperNamespace: string;
  mapperStatementId: string;
  mapperStatementType: string;
  methodCallPath: MethodCallStep[];
  tableNames: string[];
};

export type MethodCallStep = {
  sourceClassName: string;
  sourceMethodName: string;
  targetClassName: string;
  targetMethodName: string;
};

export type GraphNode = {
  id: string;
  type:
    | "API"
    | "CONTROLLER"
    | "CONTROLLER_METHOD"
    | "SERVICE"
    | "SERVICE_METHOD"
    | "MAPPER"
    | "MAPPER_METHOD"
    | "TABLE";
  label: string;
};

export type GraphEdge = {
  source: string;
  target: string;
  label: string;
};

export type ProjectGraph = {
  nodes: GraphNode[];
  edges: GraphEdge[];
};

export type AnalysisDiffSection = {
  addedCount: number;
  removedCount: number;
  added: string[];
  removed: string[];
};

export type AnalysisComparison = {
  projectId: number;
  baseSnapshotId: number;
  targetSnapshotId: number;
  baseJobId: number;
  targetJobId: number;
  baseCreatedAt: string;
  targetCreatedAt: string;
  apis: AnalysisDiffSection;
  sqlStatements: AnalysisDiffSection;
  dtos: AnalysisDiffSection;
  flows: AnalysisDiffSection;
  releaseRisk: ReleaseRisk;
};

export type ReleaseRisk = {
  readinessScore: number;
  riskLevel: string;
  riskReason: string;
};

export type ReleaseRiskTrendPoint = {
  baseSnapshotId: number;
  targetSnapshotId: number;
  baseJobId: number;
  targetJobId: number;
  targetCreatedAt: string;
  releaseRisk: ReleaseRisk;
  totalChangeCount: number;
};

export type GitHubReleaseDraft = {
  tagName: string;
  releaseName: string;
  body: string;
  draft: boolean;
  prerelease: boolean;
};

export type GitHubReleasePublishResult = {
  id?: string;
  tagName: string;
  releaseName: string;
  htmlUrl?: string;
  apiUrl?: string;
  draft: boolean;
  prerelease: boolean;
};

export type AnalysisSnapshot = {
  id: number;
  projectId: number;
  analysisJobId: number;
  createdAt: string;
  label?: string;
  note?: string;
};

export type GeneratedDocument = {
  title: string;
  format: string;
  content: string;
  evidenceCount: number;
};

export type QuestionAnswer = {
  question: string;
  answer: string;
  confidence: string;
  relatedApis: string[];
  relatedMapperStatements: string[];
  relatedTables: string[];
  evidence: string[];
};
