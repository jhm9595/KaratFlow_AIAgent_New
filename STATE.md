# STATE.md - Project Single Source of Truth (SSOT)

## 1. System Overview & Current Status
- **Project Name**: `KaratFlow_AIAgent_New` (주얼리 제조 공장 전용 통합 주문 및 공정 파이프라인 관리 플랫폼 MES/ERP)
- **Orchestration Mode**: Universal Multi-Agent Orchestrator (Zero-Typing Autonomous Pipeline)
- **Current Phase**: `PRODUCTION_READY / SR-001_COMPLETED`
- **Specification Baseline**: `REQUIREMENTS.md` (v2.1 + Non-Negotiable 10 Rules)
- **Last Updated**: 2026-10-24T15:37:00+09:00

---

## 2. Environment Discovery & Toolchain Binding
- **Operating System**: Windows (PowerShell, UTF-8 BOM script standard)
- **Repository Status**: Git Repository connected to `https://github.com/jhm9595/KaratFlow_AIAgent_New.git` (`master`)
- **Available Host Runtimes**:
  - **Java**: `21.0.9 LTS` (Spring Boot / Gradle Backend)
  - **Node.js**: `v24.13.0` / **npm**: `11.6.2` (React 19 + TypeScript + Vite + PrimeReact Frontend)
  - **Python**: `3.13.5`
  - **Go**: `1.26.2 windows/amd64`
  - **Git**: `2.48.1.windows.1`

### Verification Command Bindings & Latest Gate Status
| Verification Stage | Backend (`backend/`) | Frontend (`frontend/`) | Latest Result |
| :--- | :--- | :--- | :--- |
| **Build Command** | `.\gradlew.bat compileJava` | `npm run build` | `PASS (Exit Code 0)` |
| **Test Command** | `.\gradlew.bat test` | `tsc -b && vite build` | `PASS (5/5 JUnit Tests, 729 Vite Modules)` |
| **PowerShell BOM** | `backend/run.ps1`, `backend/run_jar.ps1` | `start.ps1` | `PASS (UTF-8 BOM 239,187,191)` |

---

## 3. Architecture & Domain State (v2.1 Baseline Implemented)
- **Core Domain Model (`backend/src/main/java/com/minibig/karatflow/backend/domain/`)**:
  - `Order` (계약/정산 단위, 1) : `WorkOrder` (개별 물리적 귀금속 작업지시서 `#01`, `#02`..., N) 1:N 분리 구조.
  - `OrderStatus` 표준 ENUM 강제화: `PROCESSING`, `COMPLETED`, `CANCELLED` (`@PrePersist`/`@PreUpdate` DB 레벨 정규화 완료).
  - `Order.stage`: 하위 `WorkOrder` 중 가장 늦은 병목(Bottleneck) 물리적 공정 표시, 완료(`COMPLETED`) 시에도 `"완료"` 덮어쓰기 없이 마지막 실제 공정명(예: `출고`) 유지.
- **Core Feature Modules**:
  1. **KRX Market Data (D-1 Carry-Forward)**: `KrxMarketDataSyncService.java` (한국거래소 API 연동 + 주말/휴일 직전 거래일 종가 이월), `DashboardSidebar.tsx` (**`[금]` ➡️ `[석유]` ➡️ `[코스피]`** 고정 배치), `GoldToolsModal.tsx` (24K/18K/14K 계산기).
  2. **Dynamic Pipeline Template**: `ProcessTemplateService.java`, `OrderService.java` (`stageName` 우선 처리 및 기본값 `접수` 무단 롤백 차단), `PipelineOverview.tsx`, `PipelineStatusBadge.tsx`, `OrderTable.tsx`.
  3. **Product & Candidate Mapping + Image Compression**: `ProductService.java`, `ProductAdmin.tsx`, `CreateOrderModal.tsx` (AutoComplete + `unmappedBrandName`/`unmappedProductName`), `imageCompressor.ts` (클라이언트 `1024px / 82%` 압축).
  4. **Multi-Dimensional Order Filtering (`/orders`)**: `OrderAdminPage.tsx`, `orderStatus.ts` (템플릿 필터, 순수 물리적 공정 단계 필터 `stageFilter`, 주문 상태 필터 `statusFilter`, 통합 검색 독립 동작 및 단일 줄 상태 판별).
  5. **Kakao Integration**: `KakaoBotController.java` & `KakaoBotService.java` (`POST /api/kakao/webhook/order-status`, `POST /api/kakao/webhook/cancel-preview`), OAuth2 소셜 로그인, 계정 관리(`/account`), 채널 연동 가이드(`/kakao-guide`).
  6. **Web Routing & Partner Handshake**: `App.tsx` React Router URL 1:1 동기화(F5 새로고침 및 히스토리 보존), `PartnershipService.java` & `SubcontractService.java` (외주 협력사 PIN 인증 및 중량/공임/해리율 계산), WebSocket/STOMP 실시간 피드(`LiveEventFeed.tsx`).

---

## 4. Active & Recent Checkpoints
- `[BOOTSTRAP]` Initialized SSOT tracking files (`STATE.md`, `TASKS.md`, `PROJECT_RULES.md`) (`0d75b9c`).
- `[SPEC-v2.1]` Registered `REQUIREMENTS.md` containing the 4 Non-Negotiable Core Verification Rules and KaratFlow v2.1 Specification (`b5b9f7a`).
- `[SR-001]` Constructed full-stack KaratFlow v2.1 (`backend/`, `frontend/`, `docs/`, `start.ps1`), added `KaratFlowRequirementsVerificationTest.java`, verified Gradle compile/test (`BUILD SUCCESSFUL`) and Vite production bundle (`0 Error`).

---

## 5. Risk & Regression Watchlist (Non-Negotiable Guards)
1. **No External Yahoo Mock for Market Data**: KRX API 기반 D-1 Carry-Forward 로직 및 `[금] -> [석유] -> [코스피]` 순서 유지됨 (`Verified`).
2. **Custom `stageName` Preservation**: 공정 이동(`nextStage`) 및 이력 기록(`recordHistory`) 시 커스텀 단계명 보존 및 완료 시 마지막 실제 공정명 유지됨 (`Verified by JUnit`).
3. **OrderStatus ENUM Strictness**: 프론트엔드/백엔드 모두 `PROCESSING`, `COMPLETED`, `CANCELLED` 3가지 표준 상태만 사용 (`Verified by JUnit & TypeScript`).
4. **Client Image Compression**: 10MB 이하 제한 및 브라우저 단 `1024px`, `82%` 품질 압축 유지됨 (`Verified`).
