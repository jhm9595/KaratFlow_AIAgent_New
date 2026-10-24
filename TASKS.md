# TASKS.md - Atomic Task Execution Ledger

## 1. Service Request (SR) Queue
| SR ID | Title / Summary | Status | Assigned Lead | Created At |
| :--- | :--- | :--- | :--- | :--- |
| `BOOTSTRAP` | Environment Discovery & SSOT Document Initialization | `DONE` | Agent 1 & Agent 4 | 2026-10-24 |
| `SPEC-v2.1` | KaratFlow Non-Negotiable Verification Rules & v2.1 Requirements Baseline Registration | `DONE` | Agent 1 & Agent 4 | 2026-10-24 |
| `SR-001` | Autonomous Full-Stack Construction of KaratFlow v2.1 & Non-Negotiable Verification Suite | `DONE` | Agent 1, 2, 3, 4 | 2026-10-24 |

---

## 2. Active Atomic Tasks
> `SR-001` (KaratFlow v2.1 풀스택 자율 구축 및 4대 핵심 요구사항 통합 검증)의 모든 원자적 태스크가 100% 완료되었습니다. 추가 비즈니스 요구사항(SR) 입력 시 즉시 다음 사이클을 개시합니다.

### Completed [SR-001] Atomic Breakdown
- [x] **TASK-001-1** `[Agent 1: Chief Architect & Planner]` :
  - Analyze `REQUIREMENTS.md` (v2.1 + 10 Non-Negotiable Rules) and establish the full-stack project structure (`backend/`, `frontend/`, `docs/`, `start.ps1` with UTF-8 BOM).
  - Decompose implementation into atomic backend, frontend, and QA verification tasks in `TASKS.md` and `STATE.md`.
- [x] **TASK-001-2** `[Agent 2: Core & Backend Engineer]` :
  - Construct and verify the Spring Boot Gradle backend (`backend/`):
    1. `Order` (1) : `WorkOrder` (N) domain separation and DB-level `OrderStatus` ENUM (`PROCESSING`, `COMPLETED`, `CANCELLED`).
    2. `KrxMarketDataSyncService` (KRX API sync + weekend/holiday D-1 Carry-Forward closing price logic).
    3. `OrderService` & `ProcessTemplateService` (`stageName` priority resolution, bottleneck stage calculation, promotion to `COMPLETED` on last template step while preserving physical `order.stage` name).
    4. `ProductService` & `ProductAdminController` (AutoComplete product catalog + unmapped product candidate approval/mapping).
    5. `KakaoBotController` & `KakaoBotService` (`POST /api/kakao/webhook/order-status`, `POST /api/kakao/webhook/cancel-preview`), OAuth2 Social Login, Partner PIN Handshake, Subcontract loss calculation, and WebSocket/STOMP live feed.
  - Author comprehensive JUnit 5 tests (`KaratFlowRequirementsVerificationTest.java`) explicitly testing the Non-Negotiable invariants and v2.1 lifecycle rules.
  - Self-healed Kakao response map extraction in test and passed `.\gradlew.bat compileJava test` (`BUILD SUCCESSFUL`, 5/5 tests passed).
- [x] **TASK-001-3** `[Agent 3: UI & Interface Integrator]` :
  - Construct and verify the React 19 + TypeScript + Vite + PrimeReact frontend (`frontend/`):
    1. `DashboardSidebar.tsx`: Strict `[금] ➡️ [석유] ➡️ [코스피]` widget order + 24K/18K/14K Gold calculator modal.
    2. `PipelineOverview.tsx`, `OrderTable.tsx`, `PipelineStatusBadge.tsx`: 100% dynamic ProcessTemplate binding with custom gradient/color and `stageName`.
    3. `OrderAdminPage.tsx` (`/orders`): Independent multi-dimensional filtering (`templateFilter`, pure physical `stageFilter`, `statusFilter`, unified search) and single-line `order.status === 'COMPLETED' | 'CANCELLED'` checks.
    4. `CreateOrderModal.tsx`, `ProductAdmin.tsx`, `imageCompressor.ts`: AutoComplete + unmapped product input, Candidate Confirm/Map tabs, and client-side `1024px / 82%` image compression.
    5. `App.tsx`, `KakaoGuidePage.tsx`, `UserAccountPage.tsx`: URL routing 1:1 sync (preserving view on F5 refresh across `/`, `/orders`, `/customer`, `/analytics`, `/kakao-guide`, `/account`).
  - Passed `npm run build` (`tsc -b && vite build` with `0 Error`, 729 modules transformed).
- [x] **TASK-001-4** `[Agent 4: QA Guard & Release Agent]` :
  - Verified `start.ps1`, `backend/run.ps1`, and `backend/run_jar.ps1` are encoded in **UTF-8 with BOM** (`239,187,191`) and use workspace-relative `$PSScriptRoot` paths.
  - Executed full verification loop (`.\gradlew.bat compileJava test`, `npm run build`) and confirmed Exit Code 0.
  - Created atomic Git commit and pushed to `origin/master`.

---

## 3. Completed Task History
- [x] **BOOTSTRAP-01** `[Agent 1]` : Scan workspace directory, Git state, and available OS runtimes (`Node`, `Python`, `Java`, `Go`).
- [x] **BOOTSTRAP-02** `[Agent 1]` : Generate `STATE.md`, `TASKS.md`, and `PROJECT_RULES.md` in project root.
- [x] **BOOTSTRAP-03** `[Agent 4]` : Verify document integrity, create initial baseline Git commit (`0d75b9c`), and push to `origin/master`.
- [x] **SPEC-v2.1-01** `[Agent 1]` : Register KaratFlow Non-Negotiable 10 Rules and v2.1 Requirements Specification into `REQUIREMENTS.md`, `STATE.md`, and `PROJECT_RULES.md`.
- [x] **SPEC-v2.1-02** `[Agent 4]` : Commit and push updated SSOT specification baseline (`b5b9f7a`) to `origin/master`.
- [x] **TASK-001-1** `[Agent 1]` : Decompose `SR-001` into atomic tasks across Agents 1-4 for full-stack KaratFlow v2.1 autonomous construction.
- [x] **TASK-001-2** `[Agent 2]` : Implement & verify Spring Boot backend and `KaratFlowRequirementsVerificationTest` (`BUILD SUCCESSFUL`).
- [x] **TASK-001-3** `[Agent 3]` : Implement & verify React + TypeScript + Vite frontend (`npm run build` `0 Error`).
- [x] **TASK-001-4** `[Agent 4]` : UTF-8 BOM verification, QA gate pass, atomic commit, and GitHub push.
