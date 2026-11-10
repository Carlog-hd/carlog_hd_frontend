# CARLOG Git Convention

## 1. Branch Convention

### 1.1. 기본 브랜치

| 브랜치 | 설명 |
|---|---|
| `main` | 최종 배포 및 안정화 기준 브랜치 |
| `develop` | 개발 통합 브랜치 |
| 작업 브랜치 | 기능 개발, 버그 수정 등의 개별 작업 |

- `main`, `develop`에는 직접 push하지 않는다.
- 모든 작업 브랜치는 `develop`에서 생성한다.
- 작업 완료 후 PR(Pull Request)을 통해 `develop`에 병합한다.
- 코드 리뷰 완료 후 작업 브랜치를 삭제한다.
- 제출 또는 배포 시 검증된 `develop`을 `main`에 병합한다.

---

## 2. Branch Naming Rule

### 2.1. 브랜치명 형식

```text
<type>/<part>/<domain>/<task>
```

- `type`: 작업 유형
- `part`: 담당 영역
- `domain`: 기능 또는 도메인
- `task`: 작업 내용을 나타내는 짧은 영문 이름

**기존 Jira 티켓 번호 대신 작업명을 사용한다.**

### 2.2. 예시

```text
feature/ivi/record/drive-timeline
feature/ivi/collection/challenge-list
feature/mobile/post/hide-location
feature/web/post/add-photos
feature/be/course/save-route
feature/be/media/generate-shortform
fix/ivi/navigation/route-reset
refactor/be/record/service-layer
chore/ivi/setup/pleos-sdk
chore/mobile/setup/app-module
docs/common/convention/git-rules
```

### 2.3. Type

| type | 설명 |
|---|---|
| `feature` | 새로운 기능 개발 |
| `fix` | 버그 수정 |
| `refactor` | 기존 코드 구조 개선 |
| `chore` | 설정, 환경 구성 등 기타 작업 |
| `docs` | 문서 작성 및 수정 |
| `test` | 테스트 코드 작성 및 수정 |

### 2.4. Part

| part | 설명 |
|---|---|
| `ivi` | 차량용 Android/Kotlin 앱 (`app/`) |
| `mobile` | 사용자 휴대폰 Android/Kotlin 앱 (`app-mobile/`) |
| `web` | 모바일 웹 |
| `be` | 백엔드 API 및 서버 |
| `ai` | AI 기능 및 콘텐츠 생성 |
| `infra` | 인프라, 배포, CI/CD |
| `common` | 공통 설정 및 문서 |

### 2.5. Domain

Car로그의 주요 기능을 기준으로 작성하며, 필요시 추가할 수 있다.

| domain | 설명 |
|---|---|
| `home` | 홈 화면 |
| `discovery` | 코스 탐색 및 추천 |
| `record` | 주행 기록 및 타임라인 |
| `course` | 코스 저장 및 따라가기 |
| `collection` | 도감 및 챌린지 |
| `post` | SNS 게시물 |
| `profile` | MY 페이지 및 사용자 정보 |
| `auth` | 로그인 및 인증 |
| `vehicle` | 차량 데이터 및 SDK 연동 |
| `navigation` | 내비게이션 연동 |
| `media` | 사진, 영상, 숏폼 생성 |
| `setup` | 초기 환경 설정 |
| `convention` | 협업 컨벤션 |

### 2.6. 네이밍 규칙

- 영문 소문자를 사용한다.
- 단어 사이는 하이픈(`-`)으로 연결한다.
- 공백, 한글, 특수문자 사용을 지양한다.
- `task`에는 실제 작업 내용을 간결하게 작성한다.
- 동일 도메인에서 여러 작업을 진행할 경우 `task`로 구분한다.
- 하나의 작업 브랜치에는 가능한 한 하나의 목적만 포함한다.

```text
# Good
feature/ivi/record/drive-timeline
fix/be/course/duplicate-save

# Bad
feature/ivi/record
feature/ivi/record/작업중
feature/ivi/record/123
feature/ivi/record/new-feature
```

---

## 3. Commit Convention

### 3.1. 커밋 메시지 형식

```text
<type>(<scope>): <commit message>
```

Jira 티켓 번호는 작성하지 않는다.

### 3.2. 예시

```text
feat(record): 주행 타임라인 화면 구현
feat(vehicle): 차량 상태 데이터 조회 기능 추가
feat(course): 주행 코스 저장 API 구현
fix(navigation): 경로 초기화 오류 수정
refactor(record): 주행 기록 처리 로직 분리
chore(ivi): Pleos SDK 초기 설정
chore(mobile): 휴대폰 앱 모듈 초기 설정
docs(convention): Git 협업 규칙 작성
test(course): 코스 저장 API 테스트 추가
```

### 3.3. Commit Type

| type | 설명 |
|---|---|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 기능 변경 없는 코드 개선 |
| `chore` | 기타 설정 및 유지보수 |
| `docs` | 문서 수정 |
| `test` | 테스트 코드 추가 및 수정 |
| `style` | 코드 포맷팅 |
| `comment` | 주석 추가 및 변경 |
| `ci` | CI/CD 설정 |
| `build` | 빌드 및 의존성 변경 |

### 3.4. Scope

브랜치의 `domain`과 동일하게 작성하는 것을 권장한다.

```text
feat(record): 주행 기록 상세 화면 구현
fix(auth): 로그인 토큰 처리 오류 수정
feat(collection): 챌린지 달성 조건 추가
```

공통 작업은 담당 영역을 scope로 작성할 수 있다.

```text
chore(ivi): Android 프로젝트 초기 설정
chore(mobile): 휴대폰 앱 모듈 초기 설정
chore(be): FastAPI 프로젝트 초기 설정
docs(convention): Git 컨벤션 문서 작성
```

### 3.5. 커밋 작성 규칙

- 커밋 메시지는 한국어로 작성한다.
- 메시지 끝에 마침표를 붙이지 않는다.
- 작업 내용을 구체적으로 작성한다.
- 서로 다른 목적의 변경 사항은 가능한 한 분리하여 커밋한다.
- `수정`, `작업`, `완료`처럼 변경 내용을 알기 어려운 메시지는 지양한다.

```text
# Good
feat(record): 주행 종료 시 기록 저장 기능 추가
fix(vehicle): 차량 데이터 null 처리 오류 수정

# Bad
feat: 기능 추가
fix: 오류 수정
chore: 작업 완료
```

---

## 4. Pull Request Convention

### 4.1. PR 제목 형식

```text
[<Part>/<type>] <작업 내용>
```

### 4.2. 예시

```text
[IVI/feat] 주행 타임라인 화면 구현
[IVI/fix] 차량 상태 조회 오류 수정
[Mobile/feat] 드라이브 로그 공유 화면 구현
[Web/feat] 드라이브 사진 추가 기능 구현
[Back/feat] 주행 기록 저장 API 구현
[AI/feat] 주행 회고록 생성 기능 구현
[Infra/chore] 서버 배포 환경 설정
[Docs/docs] Git 컨벤션 문서 작성
```

### 4.3. Part 표기

| part | PR 표기 |
|---|---|
| `ivi` | `IVI` |
| `mobile` | `Mobile` |
| `web` | `Web` |
| `be` | `Back` |
| `ai` | `AI` |
| `infra` | `Infra` |
| `common` | `Common` |
| 문서 전용 작업 | `Docs` |

### 4.4. PR 템플릿

PR 템플릿은 [`.github/PULL_REQUEST_TEMPLATE.md`](.github/PULL_REQUEST_TEMPLATE.md)에 있으며, GitHub에서 PR을 생성하면 본문에 자동으로 입력된다.

```markdown
## 개요 (Overview)
<!-- 작업 목적을 간단히 작성합니다. -->


## 작업 내용 (Details)
- [ ] 작업 내용 1
- [ ] 작업 내용 2

## 스크린샷 (Screenshots)
<!-- UI 변경이 있는 경우 첨부합니다. -->


## 테스트 (Test)
- [ ] 로컬 환경에서 기능 테스트 완료
- [ ] 기존 기능에 영향 없는지 확인 완료

## 참고 사항 (Notes)
<!-- 리뷰 시 확인이 필요한 사항이나 추가 작업을 작성합니다. -->
```

### 4.5. PR 규칙

- PR에는 리뷰어를 최소 1명 지정한다.
- 리뷰어는 변경 사항을 확인한 후 병합한다.
- PR 제목만으로 변경 내용을 파악할 수 있도록 작성한다.
- UI 변경 시 가능하면 스크린샷을 첨부한다.
- 기능 구현과 관련 없는 대규모 변경은 별도 PR로 분리한다.
- 병합 완료 후 작업 브랜치를 삭제한다.

---

## 5. Sync Merge Convention

작업 브랜치에서 최신 `develop`의 변경 사항을 가져올 때 사용한다.

### 5.1. 동기화 방법

```bash
git switch develop
git pull origin develop

git switch feature/ivi/record/drive-timeline
git merge develop
```

### 5.2. Merge Commit Message

병합 커밋 메시지를 직접 작성해야 하는 경우 다음 형식을 사용한다.

```text
Sync: 최신 develop 브랜치와 동기화
```

Fast-forward로 병합되는 경우 별도 병합 커밋을 생성하지 않는다.

### 5.3. 주의 사항

- 동기화 전에 현재 작업 내용을 커밋하거나 임시 저장한다.
- 충돌 발생 시 관련 코드를 확인하고 해결한다.
- 충돌을 해결한 후 기존 기능이 정상 동작하는지 확인한다.
- 공동 작업 중인 브랜치에 무분별한 강제 push를 하지 않는다.

---

## 6. Merge Convention

### 6.1. 기본 병합 규칙

- `작업 브랜치 → develop`: PR을 통해 병합
- `develop → main`: 제출 또는 배포 시 병합
- PR 병합 전 변경 사항 및 테스트 결과 확인
- 병합 후 원격 작업 브랜치 삭제

### 6.2. PR Merge 방식

작업 브랜치의 커밋이 많을 경우 **Squash and Merge**를 권장한다.

- 여러 작업 커밋을 하나로 정리할 수 있다.
- `develop`의 커밋 이력을 간결하게 유지할 수 있다.
- Squash 커밋 메시지는 Commit Convention을 따른다.

예시:

```text
feat(record): 주행 타임라인 기능 구현
```

---

## 7. 작업 흐름

### 7.1. 전체 흐름

```text
main
  └── develop
       ├── feature/ivi/record/drive-timeline
       │     └── PR → develop
       │
       ├── feature/mobile/post/hide-location
       │     └── PR → develop
       │
       ├── feature/be/course/save-route
       │     └── PR → develop
       │
       └── fix/ivi/navigation/route-reset
             └── PR → develop

develop → PR → main (제출 및 배포)
```

### 7.2. 브랜치 생성

`develop`의 최신 코드를 가져온 후 작업 브랜치를 생성한다.

```bash
git switch develop
git pull origin develop

git switch -c feature/ivi/record/drive-timeline
```

### 7.3. 작업 및 커밋

```bash
git add .
git commit -m "feat(record): 주행 타임라인 화면 구현"
```

변경된 파일을 확인하고 필요한 파일만 `git add`하는 것을 권장한다.

### 7.4. 원격 브랜치 Push

```bash
git push -u origin feature/ivi/record/drive-timeline
```

### 7.5. Pull Request

1. GitHub에서 PR을 생성한다.
2. 대상 브랜치를 `develop`으로 설정한다.
3. PR 템플릿에 따라 작업 내용을 작성한다.
4. 리뷰어를 1명 이상 지정한다.
5. 코드 리뷰 및 테스트 확인 후 병합한다.
6. 병합된 작업 브랜치를 삭제한다.

### 7.6. 다음 작업 시작

```bash
git switch develop
git pull origin develop

git switch -c feature/ivi/collection/challenge-list
```

### 7.7. 제출 및 배포

- 주요 기능을 `develop`에 통합한다.
- 통합 테스트를 진행한다.
- 제출 및 배포에 사용할 버전을 `main`에 병합한다.
- 필요한 경우 Git 태그로 제출 버전을 표시한다.

```bash
git tag v1.0.0
git push origin v1.0.0
```

---

## 8. 공통 협업 규칙

1. 하나의 브랜치는 하나의 주요 작업을 담당한다.
2. 다른 팀원과 동일 파일을 수정할 경우 사전에 공유한다.
3. 작업 중 변경 사항을 작은 단위로 커밋한다.
4. 주요 기능 구현 후 가능한 한 빠르게 PR을 생성한다.
5. PR 병합 전 최신 `develop`과 충돌 여부를 확인한다.
6. `.env`, API 키, 토큰 등 민감한 정보는 커밋하지 않는다.
7. 제출 전 `main` 브랜치에서 전체 기능을 검증한다.
