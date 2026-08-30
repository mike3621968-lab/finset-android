# 핀셋(FinSet) - Android 로컬 DB 버전

서버 없이, 앱 최초 실행 시 하드코딩된 데이터를 로컬 Room DB에 채워 넣고 그 안에서만 동작하는 버전입니다.
단, **관심종목의 실시간 시세는 한국투자증권(KIS) Open API로 실제 조회**합니다 (선택 기능, 토글로 켜고 끌 수 있음).

## 실시간 시세(KIS API) 설정하기 - 최초 1회만

1. 프로젝트 최상위 폴더(`FinSetAndroid`, `settings.gradle.kts`가 있는 폴더)에 `local.properties` 파일을 만듭니다.
   (Android Studio로 프로젝트를 한 번 열면 이미 자동으로 생성되어 있을 수 있습니다. 그 경우 아래 3줄만 추가하면 됩니다.)
2. 아래 내용을 채워 넣습니다.
   ```properties
   kis.appkey=발급받은_App_Key_붙여넣기
   kis.appsecret=발급받은_App_Secret_붙여넣기
   kis.baseUrl=https://openapi.koreainvestment.com:9443
   ```
   - 실전투자 계정: `kis.baseUrl=https://openapi.koreainvestment.com:9443`
   - 모의투자 계정으로 바꾸려면: `kis.baseUrl=https://openapivts.koreainvestment.com:29443`
3. **이 파일은 `.gitignore`에 이미 등록되어 있어 GitHub에는 절대 올라가지 않습니다.** 안심하고 App Key/Secret을 적으셔도 됩니다.
4. 이 값을 못 채운 채로 빌드하면 앱은 정상 실행되지만, "실시간 시세" 토글을 켰을 때 오류 메시지가 표시됩니다.

### 실시간 시세 지원 범위
- **지원**: 개별 종목 13개(AAPL, MSFT, NVDA, TSLA, AMZN, GOOGL, META, NFLX, AMD, AVGO, PLTR, COIN, MU) + ETF 4개(QQQ, SPY, DIA, IWM)
- **미지원**: 지수 3개(SPX, NDX, VIX) - KIS 해외주식 현재가 API는 개별 종목/ETF만 조회되고 지수는 별도 API가 필요해 이번 버전에는 포함하지 않았습니다.
- 15초 주기로 관심종목의 현재가·등락률을 갱신합니다 (홈 화면 상단 "실시간 시세" 토글로 on/off).
- 옵션 데이터(GEX/DEX, 콜월/풋월 등)와 매매신호 시뮬레이션은 여전히 로컬 하드코딩 데이터로 동작합니다 (감마스팟 등 실제 파생 데이터 API는 별도 연동 필요).

## GitHub Actions로 빌드할 때는 (중요)

`local.properties`는 git에 올라가지 않기 때문에, GitHub Actions 빌드 서버에는 API 키가 전달되지 않습니다.
**GitHub Secrets**에 등록해야 Actions로 빌드한 APK에도 실시간 시세가 동작합니다.

1. GitHub 저장소 페이지 → **Settings** → 좌측 메뉴 **Secrets and variables** → **Actions**
2. **New repository secret** 버튼으로 아래 3개를 각각 등록:
   - `KIS_APP_KEY` = 발급받은 App Key
   - `KIS_APP_SECRET` = 발급받은 App Secret
   - `KIS_BASE_URL` = `https://openapi.koreainvestment.com:9443` (실전투자)
3. 등록 후 다시 push하면, Actions가 이 Secrets를 자동으로 빌드에 사용합니다.

Android Studio로 로컬 빌드할 때는 이 Secrets 대신 `local.properties`가 사용됩니다 (둘 다 설정해두면 편합니다).

## APK 만들기 (GitHub Actions, 로컬 설치 불필요)

1. GitHub에서 새 저장소(Repository)를 만듭니다 (Public/Private 상관없음).
2. 이 폴더(`FinSetAndroid`) 전체를 그 저장소에 push합니다.
   ```bash
   cd FinSetAndroid
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin <본인의 저장소 URL>
   git push -u origin main
   ```
3. 저장소의 **Actions** 탭으로 이동합니다. `Build APK` 워크플로우가 자동으로 실행됩니다 (`.github/workflows/build-apk.yml`에 정의되어 있음).
4. 빌드가 끝나면(초록색 체크 ✅, 보통 3~7분 소요) 해당 실행(run)을 클릭 → 하단 **Artifacts** 섹션에서 `FinSet-debug-apk`를 다운로드합니다.
5. 압축을 풀면 `app-debug.apk` 파일이 나옵니다. 안드로이드 기기로 옮겨서 설치하면 됩니다 (설치 전 "출처를 알 수 없는 앱 설치 허용" 필요).

빌드가 실패하면 Actions 로그에 빨간 X와 함께 오류 메시지가 나옵니다. 그 로그를 복사해서 알려주시면 바로 고쳐드릴게요.

## 실행 방법 (Android Studio, 로컬 개발용)

1. Android Studio (Koala 이상 권장)로 이 폴더(`FinSetAndroid`)를 **Open**합니다.
2. Gradle Sync가 끝날 때까지 기다립니다 (인터넷 연결 필요 - 의존성 다운로드).
3. 에뮬레이터 또는 실기기(Android 8.0 / API 26 이상)를 연결하고 Run ▶ 을 누릅니다.

## 이 버전에서 서버가 하는 일이 없는 이유

- 감마스팟/뉴스 API를 직접 호출하지 않습니다.
- `data/SeedData.kt`에 하드코딩된 데이터(카테고리 15개, 종목 20개, 뉴스 50개, 옵션 지표 4종목, 알림 4건)를
  앱 최초 실행 시 Room DB(`finset.db`)에 1회만 삽입합니다.
- 이후 종목 추가/삭제, 카테고리 편집 등은 전부 이 로컬 DB 안에서만 이루어집니다 (재실행해도 유지됨, 단 데이터는 이 기기 안에만 존재).

## 프로젝트 구조

```
app/src/main/java/com/finset/app/
├─ MainActivity.kt              # NavHost + 하단 네비게이션 조립
├─ data/
│  ├─ Entities.kt               # Room 엔티티 (Category, Stock, News, OptionMetrics, Alert)
│  ├─ Daos.kt                   # Room DAO (Flow 기반)
│  ├─ AppDatabase.kt            # Room DB + 최초 실행 시드 삽입
│  ├─ SeedData.kt               # ★ 하드코딩된 카테고리/종목/뉴스/옵션데이터
│  └─ Repository.kt             # DAO를 감싸는 단순 접근 계층
├─ viewmodel/
│  └─ MainViewModel.kt          # Room 데이터를 StateFlow로 화면에 제공
└─ ui/
   ├─ theme/                    # 브랜드 컬러(네이비/블루/골드), 타이포그래피
   ├─ navigation/Routes.kt      # 화면 경로 상수
   ├─ components/               # 공통 컴포넌트 (칩, 종목행, 뉴스카드 등)
   └─ screens/                  # 온보딩/홈/종목리스트/종목상세/검색/알림함/마이페이지/뉴스상세
```

## 데이터 수정하는 법

`data/SeedData.kt` 파일 하나만 고치면 됩니다.
- 카테고리 추가/삭제: `categories` 리스트
- 종목 추가/삭제: `stocks` 리스트
- 뉴스 내용 수정: `buildNews()` 함수 내부
- 옵션 지표(GEX/DEX, 콜월/풋월 등) 수정: `optionMetrics` 리스트

단, **기존에 설치된 앱은 DB가 이미 채워져 있어 SeedData를 고쳐도 반영되지 않습니다.**
앱을 삭제 후 재설치하거나, 기기 설정 > 앱 > 핀셋 > 저장공간 > 데이터 삭제를 하면 새 SeedData로 다시 채워집니다.

## 다음 단계로 고려할 것 (나중에 서버를 붙이게 되면)

- `data/Repository.kt`의 각 함수 내부만 "로컬 DB 조회" → "API 호출 + 로컬 캐싱"으로 바꾸면 됩니다.
  화면(ui/screens) 코드는 거의 손댈 필요가 없도록 구조화되어 있습니다.
- 실시간 알림이 필요해지면 `WorkManager`로 주기적 폴링 → 조건 충족 시 로컬 알림(NotificationCompat) 발송을 추가하면 됩니다.
