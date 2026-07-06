import type {
  ApiFlow,
  AnalysisComparison,
  AnalysisSnapshot,
  GitHubReleaseDraft,
  GitHubReleasePublishResult,
  ReleaseRiskTrendPoint,
  ApiResponse,
  Dashboard,
  GeneratedDocument,
  Project,
  ProjectGraph,
  QuestionAnswer,
  SourceFile,
  SpringApi,
  SpringApiDetail,
  TableUsage,
  TableUsageDetail,
  LlmSetting,
  LlmTestResponse
} from "../types";

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(path, options);
  const body = (await response.json().catch(() => ({}))) as Partial<ApiResponse<T>> & {
    message?: string;
  };

  if (!response.ok) {
    throw new Error(body.message || `HTTP ${response.status}`);
  }

  return body.data as T;
}

function download(path: string) {
  window.location.href = path;
}

export const api = {
  listProjects: () => request<Project[]>("/api/projects"),
  createProject: (payload: { name: string; sourcePath: string }) =>
    request<Project>("/api/projects", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    }),
  uploadProject: (payload: { name: string; file: File }) => {
    const formData = new FormData();
    formData.append("name", payload.name);
    formData.append("file", payload.file);
    return request<Project>("/api/projects/upload", {
      method: "POST",
      body: formData
    });
  },
  analyze: (projectId: number) =>
    request(`/api/projects/${projectId}/analyze`, { method: "POST" }),
  resetAnalysis: (projectId: number) =>
    request(`/api/projects/${projectId}/analysis/results`, { method: "DELETE" }),
  dashboard: (projectId: number) => request<Dashboard>(`/api/projects/${projectId}/dashboard`),
  sourceFiles: (projectId: number) => request<SourceFile[]>(`/api/projects/${projectId}/source-files`),
  apis: (projectId: number) => request<SpringApi[]>(`/api/projects/${projectId}/apis`),
  apiDetail: (projectId: number, apiId: number) =>
    request<SpringApiDetail>(`/api/projects/${projectId}/apis/${apiId}`),
  tables: (projectId: number) => request<TableUsage[]>(`/api/projects/${projectId}/tables`),
  tableDetail: (projectId: number, tableName: string) =>
    request<TableUsageDetail>(`/api/projects/${projectId}/tables/${encodeURIComponent(tableName)}`),
  flows: (projectId: number) => request<ApiFlow[]>(`/api/projects/${projectId}/flows`),
  graph: (projectId: number) => request<ProjectGraph>(`/api/projects/${projectId}/graph`),
  snapshots: (projectId: number) =>
    request<AnalysisSnapshot[]>(`/api/projects/${projectId}/analysis/snapshots`),
  updateSnapshot: (projectId: number, snapshotId: number, payload: { label: string; note: string }) =>
    request<AnalysisSnapshot>(`/api/projects/${projectId}/analysis/snapshots/${snapshotId}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    }),
  riskTrend: (projectId: number) =>
    request<ReleaseRiskTrendPoint[]>(`/api/projects/${projectId}/analysis/risk-trend`),
  githubPublishHistory: (projectId: number) =>
    request<GitHubReleasePublishResult[]>(`/api/projects/${projectId}/analysis/github-release-publish-history`),
  comparison: (projectId: number) =>
    request<AnalysisComparison>(`/api/projects/${projectId}/analysis/comparison/latest`),
  comparisonReport: (projectId: number) =>
    request<GeneratedDocument>(`/api/projects/${projectId}/analysis/comparison/latest/report`),
  releaseChecklist: (projectId: number) =>
    request<GeneratedDocument>(`/api/projects/${projectId}/analysis/comparison/latest/checklist`),
  releaseNotes: (projectId: number) =>
    request<GeneratedDocument>(`/api/projects/${projectId}/analysis/comparison/latest/release-notes`),
  githubReleaseDraft: (projectId: number) =>
    request<GitHubReleaseDraft>(`/api/projects/${projectId}/analysis/comparison/latest/github-release-draft`),
  publishGitHubReleaseDraft: (projectId: number) =>
    request<GitHubReleasePublishResult>(`/api/projects/${projectId}/analysis/comparison/latest/github-release-draft/publish`, {
      method: "POST"
    }),
  compareSnapshots: (projectId: number, baseSnapshotId: number, targetSnapshotId: number) =>
    request<AnalysisComparison>(
      `/api/projects/${projectId}/analysis/comparison?baseSnapshotId=${baseSnapshotId}&targetSnapshotId=${targetSnapshotId}`
    ),
  snapshotComparisonReport: (projectId: number, baseSnapshotId: number, targetSnapshotId: number) =>
    request<GeneratedDocument>(
      `/api/projects/${projectId}/analysis/comparison/report?baseSnapshotId=${baseSnapshotId}&targetSnapshotId=${targetSnapshotId}`
    ),
  snapshotReleaseChecklist: (projectId: number, baseSnapshotId: number, targetSnapshotId: number) =>
    request<GeneratedDocument>(
      `/api/projects/${projectId}/analysis/comparison/checklist?baseSnapshotId=${baseSnapshotId}&targetSnapshotId=${targetSnapshotId}`
    ),
  snapshotReleaseNotes: (projectId: number, baseSnapshotId: number, targetSnapshotId: number) =>
    request<GeneratedDocument>(
      `/api/projects/${projectId}/analysis/comparison/release-notes?baseSnapshotId=${baseSnapshotId}&targetSnapshotId=${targetSnapshotId}`
    ),
  snapshotGitHubReleaseDraft: (projectId: number, baseSnapshotId: number, targetSnapshotId: number) =>
    request<GitHubReleaseDraft>(
      `/api/projects/${projectId}/analysis/comparison/github-release-draft?baseSnapshotId=${baseSnapshotId}&targetSnapshotId=${targetSnapshotId}`
    ),
  publishSnapshotGitHubReleaseDraft: (projectId: number, baseSnapshotId: number, targetSnapshotId: number) =>
    request<GitHubReleasePublishResult>(
      `/api/projects/${projectId}/analysis/comparison/github-release-draft/publish?baseSnapshotId=${baseSnapshotId}&targetSnapshotId=${targetSnapshotId}`,
      { method: "POST" }
    ),
  downloadComparisonReport: (projectId: number, baseSnapshotId?: number, targetSnapshotId?: number) => {
    if (baseSnapshotId && targetSnapshotId) {
      download(`/api/projects/${projectId}/analysis/comparison/report/download?baseSnapshotId=${baseSnapshotId}&targetSnapshotId=${targetSnapshotId}`);
      return;
    }
    download(`/api/projects/${projectId}/analysis/comparison/latest/report/download`);
  },
  downloadReleaseChecklist: (projectId: number, baseSnapshotId?: number, targetSnapshotId?: number) => {
    if (baseSnapshotId && targetSnapshotId) {
      download(`/api/projects/${projectId}/analysis/comparison/checklist/download?baseSnapshotId=${baseSnapshotId}&targetSnapshotId=${targetSnapshotId}`);
      return;
    }
    download(`/api/projects/${projectId}/analysis/comparison/latest/checklist/download`);
  },
  downloadReleaseNotes: (projectId: number, baseSnapshotId?: number, targetSnapshotId?: number) => {
    if (baseSnapshotId && targetSnapshotId) {
      download(`/api/projects/${projectId}/analysis/comparison/release-notes/download?baseSnapshotId=${baseSnapshotId}&targetSnapshotId=${targetSnapshotId}`);
      return;
    }
    download(`/api/projects/${projectId}/analysis/comparison/latest/release-notes/download`);
  },
  document: (projectId: number, kind: "onboarding" | "apis") =>
    request<GeneratedDocument>(`/api/projects/${projectId}/documents/${kind}`),
  ask: (projectId: number, question: string) =>
    request<QuestionAnswer>(`/api/projects/${projectId}/questions`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ question })
    }),
  getLlmSettings: () => request<LlmSetting>("/api/llm/settings"),
  saveLlmSettings: (payload: LlmSetting) =>
    request<LlmSetting>("/api/llm/settings", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    }),
  testLlmConnection: (payload: LlmSetting) =>
    request<LlmTestResponse>("/api/llm/settings/test", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    })
};
