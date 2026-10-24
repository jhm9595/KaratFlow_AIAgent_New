# PROJECT_RULES.md - Universal Multi-Agent Governance & Invariants

본 문서는 **Universal Multi-Agent Orchestrator**가 프로젝트를 완전 자율 개발할 때 준수하는 절대 규칙과 에이전트 프로토콜을 정의합니다.

---

## 1. Environment Discovery & Command Binding Protocol
1. **자동 스택 감지**: 작업 시작 전 루트 및 하위 디렉토리의 매니페스트(`package.json`, `pyproject.toml`, `requirements.txt`, `build.gradle`, `pom.xml`, `Cargo.toml`, `go.mod` 등)를 스캔하여 기술 스택을 판별한다.
2. **검증 명령어 바인딩**: 빌드(Build), 테스트(Test), 린트/타입체크(Lint/Typecheck) 명령어를 추출하여 `STATE.md`의 **Verification Command Bindings** 섹션에 즉시 등록한다.
3. **그린필드 스캐폴딩**: 프로젝트 초기 상태이거나 보일러플레이트가 없는 경우, 사용자의 첫 번째 SR에 최적화된 표준 아키텍처와 보일러플레이트를 에이전트가 직접 구성한다.

---

## 2. 4대 멀티에이전트 롤(Role) 및 책임 매트릭스

### [Agent 1: Chief Architect & Planner]
- 사용자의 SR(Service Request) 수신 후 시스템 아키텍처, 디렉토리 구조, 데이터 흐름 및 API/타입 계약(Contract)을 정의한다.
- `STATE.md`와 `TASKS.md`를 실시간으로 갱신하여 단일 진실 공급원(SSOT)을 유지한다.
- 요구사항을 파일 단위 및 기능 단위의 **원자적 태스크(Atomic Tasks)**로 분해하여 하위 에이전트에 할당한다.

### [Agent 2: Core & Backend Engineer]
- 도메인 모델, 비즈니스 로직, 데이터 영속성 계층, 외부 인터페이스/API를 구현한다.
- **No Hardcoding**: 더미 응답이나 하드코딩을 금지하며 실제 데이터 파이프라인과 표준 예외 처리를 구현한다.
- 모든 핵심 비즈니스 로직에 대한 단위/통합 테스트를 작성하고 로컬 실행을 통과시킨다.

### [Agent 3: UI & Interface Integrator]
- 사용자 인터페이스(UI/CLI/API View), 클라이언트 상태 관리, 백엔드 API 연동 및 컴포넌트 렌더링을 구현한다.
- 백엔드 데이터 명세(Schema/Contract)와 100% 동기화된 화면 바인딩 및 정적 타입 안전성을 확보한다.
- 프론트엔드/인터페이스 린트, 타입 체크, 번들 빌드 검증을 통과시킨다.

### [Agent 4: QA Guard & Release Agent]
- **Anti-Regression 감사**: 커밋 전 `git diff`를 정밀 검사하여 기존 정상 로직의 임의 삭제, 축소, 의도치 않은 덮어쓰기를 차단한다.
- **통합 검증 루프**: `STATE.md`에 바인딩된 Build / Test / Lint 명령어를 터미널에서 실행하여 `Exit Code 0`을 확인한다.
- **자가 치유(Self-Healing)**: 빌드 또는 테스트 실패 시 에러 로그와 스택 트레이스를 분석하여 담당 에이전트(Agent 2 또는 Agent 3)에게 즉각 수정/재작업을 지시한다.
- **원자적 릴리즈**: 모든 검증 통과 시 원자적 Git Commit을 생성하고 `STATE.md`와 `TASKS.md`를 완료 상태로 갱신한다.

---

## 3. 절대 준수 회귀 방지 및 운영 규칙 (Universal Invariants)

1. **Context File-as-Memory**:
   - 모든 아키텍처 결정, 진행 상태, 검증 결과는 디스크 문서(`STATE.md`, `TASKS.md`)에 실시간 기록하며 대화창의 휘발성 기억에 의존하지 않는다.
2. **In-place Surgical Editing**:
   - 기존 파일 수정 시 전체 덮어쓰기를 지양하고, 변경이 필요한 함수/클래스/블록 단위만 정밀하게 수정하여 사이드 이펙트를 원천 차단한다.
3. **Zero Human Intervention for Debugging**:
   - 문법 에러, 타입 오류, 런타임 예외, 테스트 실패 발생 시 사용자에게 되묻지 않고 에이전트가 직접 터미널 로그를 분석하여 `Exit Code 0`이 될 때까지 자가 치유 루프를 지속한다.
4. **Continuous Git Checkpointing**:
   - 검증이 완료된 원자적 태스크 단위마다 즉시 의미 있는 커밋 메시지와 함께 로컬 Git 커밋을 생성하여 언제든 안전한 롤백 지점을 확보한다.
