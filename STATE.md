# STATE.md - Project Single Source of Truth (SSOT)

## 1. System Overview & Current Status
- **Project Name**: `KaratFlow_AIAgent_New` (주얼리 제조 공장 전용 통합 주문 및 공정 파이프라인 관리 플랫폼 MES/ERP)
- **Orchestration Mode**: Universal Multi-Agent Orchestrator (Zero-Typing Autonomous Pipeline)
- **Current Phase**: `SPEC_LOCKED / AWAITING_SR`
- **Specification Baseline**: `REQUIREMENTS.md` (v2.1 + Non-Negotiable 10 Rules)
- **Last Updated**: 2026-10-24T15:26:30+09:00

---

## 2. Environment Discovery & Toolchain Binding
- **Operating System**: Windows (PowerShell, UTF-8 BOM script standard)
- **Repository Status**: Git Repository connected to `https://github.com/jhm9595/KaratFlow_AIAgent_New.git` (`master`)
- **Available Host Runtimes**:
  - **Java**: `21.0.9 LTS` (Spring Boot / Gradle Backend Target)
  - **Node.js**: `v24.13.0` / **npm**: `11.6.2` (React + TypeScript + Vite Frontend Target)
  - **Python**: `3.13.5`
  - **Go**: `1.26.2 windows/amd64`
  - **Git**: `2.48.1.windows.1`

### Verification Command Bindings (Standardized per REQUIREMENTS.md)
| Verification Stage | Backend (`backend/`) | Frontend (`frontend/`) |
| :--- | :--- | :--- |
| **Build Command** | `./gradlew compileJava` | `npm run build` |
| **Test Command** | `./gradlew test` | `npm run build` (TypeScript + Vite bundle check) |
| **Release Gate** | `BUILD SUCCESSFUL` (Exit Code 0) | `0 Error` Vite Bundle Completion (Exit Code 0) |

---

## 3. Architecture & Domain State (v2.1 Baseline)
- **Core Domain Model**:
  - `Order` (계약/정산 단위, 1) : `WorkOrder` (개별 물리적 귀금속 작업지시서 `#01`, `#02`..., N) 분리 구조.
  - `OrderStatus` 표준 ENUM 강제화: `PROCESSING`, `COMPLETED`, `CANCELLED` (한글/구형 상태값 혼용 금지).
  - `Order.stage`: 하위 `WorkOrder` 중 가장 늦은 병목(Bottleneck) 물리적 공정 표시, 완료(`COMPLETED`) 시에도 `"완료"` 덮어쓰기 없이 마지막 실제 공정명(예: `출고`) 유지.
- **Core Feature Modules**:
  1. **KRX Market Data (D-1 Carry-Forward)**: 한국거래소 API 연동(`KrxMarketDataSyncService`), 주말/휴일 직전 거래일 종가 이월, 사이드바 **`[금]` ➡️ `[석유]` ➡️ `[코스피]`** 고정 배치, 24K/18K/14K 계산기.
  2. **Dynamic Pipeline Template**: 공정 단계/색상/그라디언트 커스터마이징, `stageName` 우선 처리(`접수` 무단 롤백 차단), 실시간 공정 현황 및 상태 배지 동기화.
  3. **Product & Candidate Mapping + Image Compression**: 등록 제품 AutoComplete + 미등록 제품(`unmappedBrandName`, `unmappedProductName`) 주문 생성, 후보 리스트(Candidates) 승인/매핑, 클라이언트 이미지 `1024px / 82%` 압축 업로드.
  4. **Multi-Dimensional Order Filtering (`/orders`)**: 템플릿 필터, 순수 물리적 공정 단계 필터(`stageFilter`), 주문 상태 필터(`statusFilter`), 통합 검색 독립 동작.
  5. **Kakao Integration**: 카카오 i 오픈빌더 웹훅(`POST /api/kakao/webhook/order-status`, `POST /api/kakao/webhook/cancel-preview`), 카카오 OAuth2 간편 로그인 및 계정 연동(`/account`), 채널 연동 가이드(`/kakao-guide`).
  6. **Web Routing & Partner Handshake**: React Router URL 1:1 동기화(F5 새로고침 및 히스토리 보존), 외주 협력사 PIN 인증 및 중량/공임/해리율 계산, WebSocket/STOMP 실시간 피드.

---

## 4. Active & Recent Checkpoints
- `[BOOTSTRAP]` Initialized SSOT tracking files (`STATE.md`, `TASKS.md`, `PROJECT_RULES.md`) (`0d75b9c`).
- `[SPEC-v2.1]` Registered `REQUIREMENTS.md` containing the 4 Non-Negotiable Core Verification Rules and KaratFlow v2.1 Specification.

---

## 5. Risk & Regression Watchlist (Non-Negotiable Guards)
1. **No External Yahoo Mock for Market Data**: 반드시 KRX API 기반 D-1 Carry-Forward 로직 및 `[금] -> [석유] -> [코스피]` 순서를 유지할 것.
2. **Custom `stageName` Preservation**: 공정 이동(`nextStage`) 및 이력 기록(`recordHistory`) 시 기본값(`접수`)으로 롤백되거나 완료 시 `"완료"` 문자열로 `stage`가 덮어써지는 현상 절대 금지.
3. **OrderStatus ENUM Strictness**: 프론트엔드/백엔드 모두 `PROCESSING`, `COMPLETED`, `CANCELLED` 3가지 표준 상태만 사용.
4. **Client Image Compression**: 10MB 이하 제한 및 브라우저 단 `1024px`, `82%` 품질 압축 유지.
