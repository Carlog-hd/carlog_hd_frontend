# CARLOG Mobile

차량(IVI)에서 자동으로 기록된 드라이브 로그를 휴대폰에서 확인·편집·공유하는 Android 앱입니다.

> 현재 **디자인 목업 단계**입니다. 데모 HTML(`carlog-demo.html`) 기준으로 화면 레이아웃만 구현되어 있으며, 데이터 연동과 화면 이동 로직은 없습니다.

## Tech Stack

| 구분 | 내용 |
|---|---|
| Language | Kotlin 1.9.0 |
| UI | XML View, Material Components 1.12.0 |
| Build | Gradle 8.0, Android Gradle Plugin 8.1.1 |
| SDK | minSdk 28 / compileSdk 34 / targetSdk 34 |
| JDK | 17 |

## Getting Started

### 요구 사항
- Android Studio
- JDK 17
- Android SDK Platform 34

### 실행
1. 저장소를 클론하고 Android Studio에서 **`mobile` 폴더**를 엽니다.
   - 차량(IVI) 앱은 별도 프로젝트인 `ivi` 폴더에 있습니다.
2. **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK**를 17로 설정합니다.
3. Gradle Sync가 끝나면 실행 설정에서 **`app`**을 선택하고 휴대폰 에뮬레이터에서 실행합니다.

### 빌드 명령
```bash
cd mobile

# 디버그 APK 빌드
./gradlew :app:assembleDebug

# Lint 검사
./gradlew :app:lintDebug
```

## Project Structure

```text
mobile/app/src/main/
├── java/com/carlog/mobile/
│   ├── MainActivity.kt          # 하단 탭 + 화면 전환
│   └── ui/
│       ├── common/              # 공통 커스텀 View (StatView, MapBoxView)
│       ├── home/                # 홈
│       ├── discover/            # 발견
│       ├── logs/                # 내 기록, 기록 상세
│       ├── share/               # 공유 편집, 미리보기, 공유 수단
│       ├── dex/                 # 도감
│       └── my/                  # MY
└── res/
    ├── layout/
    │   ├── activity_main.xml          # 앱 전체 틀 (화면 영역 + 하단 탭)
    │   ├── fragment_*.xml             # 화면
    │   ├── fragment_template_*.xml    # 새 화면 템플릿
    │   ├── item_*.xml                 # 목록 아이템
    │   └── view_*.xml                 # 커스텀 View 내부 구성
    ├── values/
    │   ├── colors.xml, dimens.xml, styles.xml, strings.xml   # 공통
    │   ├── themes.xml, attrs.xml                              # 공통
    │   └── strings_<screen>.xml, styles_<screen>.xml          # 화면 전용
    ├── drawable/      # ic_* 아이콘, bg_* 배경, img_* 자리 표시 이미지
    ├── color/         # selector_* 선택 상태별 색상
    └── menu/          # 하단 탭 메뉴
```

## Screens

| ID | 화면 | Fragment | Layout |
|---|---|---|---|
| M01 | 홈·알림 | `ui/home/HomeFragment` | `fragment_home.xml` |
| M02 | 발견 | `ui/discover/DiscoverFragment` | `fragment_discover.xml` |
| M03 | 내 기록 | `ui/logs/LogsFragment` | `fragment_logs.xml` |
| M03 | 기록 상세 | `ui/logs/LogDetailFragment` | `fragment_log_detail.xml` |
| M04 | 공유 편집 | `ui/share/ShareEditFragment` | `fragment_share_edit.xml` |
| M05 | 피드 미리보기 | `ui/share/SharePreviewFragment` | `fragment_share_preview.xml` |
| M05 | 공유 수단 | `ui/share/ShareOptionsFragment` | `fragment_share_options.xml` |
| M06 | 친구 로그 | - | - |
| M07 | 차량 전송 | - | - |
| M08 | 도감 | `ui/dex/DexFragment` | `fragment_dex.xml` |
| M08 | MY | `ui/my/MyFragment` | `fragment_my.xml` |

- 하단 탭(홈 / 발견 / 기록 / 도감 / MY) 전환만 연결되어 있습니다.
- 하위 화면(기록 상세, 공유 편집 등)은 Android Studio 레이아웃 미리보기로 확인할 수 있습니다.

### M05 미리보기·공유 목업
- 두 XML을 Android Studio의 **Design / Split**에서 열면 바텀시트 형태로 확인할 수 있습니다.
- `SharePreviewFragment`, `ShareOptionsFragment`는 기존 화면처럼 레이아웃만 표시하는 Fragment입니다. M04 버튼, 실제 바텀시트 표시·닫기·드래그는 연결하지 않았습니다.
- 기본 예시는 **친구 공개 / 출발지·도착지 숨김**입니다. 지도는 비공개 구간의 회색 경로를 제외한 자리 표시 이미지이며 실제 위치 정보나 편집 상태를 처리하지 않습니다.
- 너비 360dp 이상에서는 통계·공유 수단이 4열이며, 좁은 화면에서는 2열로 표시됩니다. 시트 높이는 화면의 최대 86%이고 내용이 길면 스크롤됩니다.
- 피드 게시, 링크 복사, 메시지 공유, 이미지 저장 및 반응 버튼은 디자인만 구현되어 있습니다.
- M05 전용 문구·스타일은 `strings_preview.xml`, `styles_preview.xml`에 있습니다. 기존 공통 리소스는 수정하지 않았습니다.

## Conventions

### 리소스 사용
- 색상은 `@color/cl_*`, 크기·여백·글자 크기는 `@dimen/cl_*`를 사용합니다. 레이아웃에 값을 직접 쓰지 않습니다.
- 이름은 데모 CSS 변수와 대응됩니다. 예: `--ink` → `cl_ink`, `--accent` → `cl_accent`

### 공통 / 화면 전용 구분
| 사용 범위 | 위치 |
|---|---|
| 여러 화면 | 공통 리소스 (`styles.xml`, `colors.xml`, `dimens.xml`, `strings.xml`), `ui/common` |
| 한 화면 | `strings_<screen>.xml`, `styles_<screen>.xml` |

- 공통 리소스는 여러 명이 함께 수정하므로 변경 전에 공유합니다.
- 아이콘(`ic_*`)은 추가 전에 이미 있는지 확인합니다.

### 네이밍
| 종류 | 규칙 | 예시 |
|---|---|---|
| 화면 레이아웃 | `fragment_<screen>` | `fragment_log_detail.xml` |
| 목록 아이템 | `item_<name>` | `item_feed_card.xml` |
| 아이콘 / 배경 | `ic_<name>` / `bg_<name>` | `ic_share.xml`, `bg_card.xml` |
| 상태별 색상 | `selector_<name>` | `selector_chip_bg.xml` |
| 스타일 | `Widget.Carlog.<Name>`, `Carlog.Text.<Name>` | `Widget.Carlog.Button.Accent` |
| View ID | `<type>_<name>` | `btn_share`, `tv_log_name`, `stat_distance` |

### 새 화면 추가
1. `fragment_template_tab.xml`(탭 화면) 또는 `fragment_template_sub.xml`(하위 화면)을 복사합니다.
2. `ui/<screen>/`에 Fragment를 추가합니다.
   ```kotlin
   class XxxFragment : Fragment(R.layout.fragment_xxx)
   ```
3. 화면 문구와 전용 스타일은 `strings_<screen>.xml`, `styles_<screen>.xml`에 둡니다.

### 목업 단계
- 로직 없이 XML 레이아웃만 작성합니다.
- 목록은 `item_*.xml`을 `<include>`로 반복해 표시합니다. 데이터 연동 시 RecyclerView로 교체합니다.
- 데모 문구는 레이아웃에 직접 작성할 수 있습니다. (`tools:ignore="HardcodedText"`)

## UI Components

### 레이아웃 스타일
| 데모 | 스타일 |
|---|---|
| `.phead` 탭 화면 제목 | `Widget.Carlog.PageHeader` + `Carlog.Text.PageTitle` |
| `.navbar` 하위 화면 상단바 | `Widget.Carlog.TopBar` + `Carlog.Text.TopBarTitle` |
| `.sec` 섹션 | `Widget.Carlog.Section` |
| `.sec h2` 섹션 제목 | `Widget.Carlog.SectionHeader` + `Carlog.Text.SectionTitle.Fill` |
| `.card` / `.pad` | `MaterialCardView` (기본 적용) / `Widget.Carlog.CardContent` |
| `.btnbar` 하단 고정 버튼 | `Widget.Carlog.ButtonBar` |

### 요소 스타일
| 데모 | 스타일 |
|---|---|
| 텍스트 | `Carlog.Text.Display`, `PageTitle`, `SectionTitle`, `Body`, `Body.Bold`, `Caption` |
| `.btn` | `MaterialButton` (기본), `Widget.Carlog.Button.Accent`, `.Sub`, `.Small` |
| `.chip` 필터 | `Chip` + `Widget.Carlog.FilterChip` |
| `.title-chip` 칭호 | `TextView` + `Widget.Carlog.TitleChip` (`.Teal`, `.Violet`, `.Blue`) |
| `.toggle` | `SwitchMaterial` + `Widget.Carlog.Switch` |
| `.li` / 구분선 | `Widget.Carlog.ListItem` / `Widget.Carlog.Divider` |
| `.iconbtn` | `ImageButton` + `Widget.Carlog.IconButton` |
| `.av` 프로필 | `TextView` + `Widget.Carlog.Avatar` (색은 `backgroundTint`) |
| `.actions` 피드 버튼 | `Widget.Carlog.FeedActions` + `Widget.Carlog.FeedAction` |

### 커스텀 View
`ui/common`의 커스텀 View는 XML 속성을 표시하기만 하며, 기능 로직은 없습니다.

```xml
<!-- StatView: 통계 칸 -->
<LinearLayout style="@style/Widget.Carlog.StatsRow">
    <com.carlog.mobile.ui.common.StatView
        style="@style/Widget.Carlog.Stat"
        app:statValue="167km"
        app:statLabel="거리" />
</LinearLayout>

<!-- MapBoxView: 지도 + 장소 라벨 (mapImage 생략 시 기본 자리 표시 이미지) -->
<com.carlog.mobile.ui.common.MapBoxView
    android:layout_width="match_parent"
    android:layout_height="@dimen/cl_map_height"
    app:mapTagText="7번 국도 새벽 바닷길" />
```

## Contributing

브랜치, 커밋, PR 규칙은 루트의 [CONTRIBUTING.md](../CONTRIBUTING.md)를 따릅니다.
