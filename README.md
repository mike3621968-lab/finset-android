# 핀셋(FinSet) - Android 로컬 DB 버전

서버 없이, 앱 최초 실행 시 하드코딩된 데이터를 로컬 Room DB에 채워 넣고 그 안에서만 동작하는 버전입니다.

## 실행 방법

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
