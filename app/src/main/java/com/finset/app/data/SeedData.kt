package com.finset.app.data

/**
 * 서버 없이 로컬 DB에 미리 채워 넣을 하드코딩 데이터.
 * 앱 최초 실행 시 AppDatabase.Callback에서 이 데이터를 한 번만 삽입한다.
 */
object SeedData {

    // ── 1) 관심 카테고리 15개 ─────────────────────────────
    val categories: List<CategoryEntity> = listOf(
        CategoryEntity("all",          "전체속보"),
        CategoryEntity("fed",          "연준(Fed)"),
        CategoryEntity("rates",        "금리·인플레이션"),
        CategoryEntity("trump",        "트럼프"),
        CategoryEntity("tariff",       "관세·무역"),
        CategoryEntity("geopolitics",  "지정학·이란"),
        CategoryEntity("china",        "중국"),
        CategoryEntity("semiconductor","반도체"),
        CategoryEntity("bigtech_ai",   "빅테크·AI"),
        CategoryEntity("earnings",     "실적시즌"),
        CategoryEntity("bonds",        "국채·채권"),
        CategoryEntity("commodities",  "원자재·유가"),
        CategoryEntity("crypto",       "암호화폐"),
        CategoryEntity("banking",      "은행·금융"),
        CategoryEntity("econ_data",    "고용·경제지표"),
    )

    // ── 2) 관심 종목 유니버스 20개 ─────────────────────────
    val stocks: List<StockEntity> = listOf(
        StockEntity("SPX",  "S&P500",           "index", "#EAF1FB", "#1D4E8C", "5,847.66", "+0.42%", true),
        StockEntity("NDX",  "나스닥100",         "index", "#EAF1FB", "#1D4E8C", "20,412.30", "+0.55%", true),
        StockEntity("QQQ",  "Invesco QQQ",      "etf",   "#EAF1FB", "#1D4E8C", "498.31",   "+0.67%", true),
        StockEntity("SPY",  "SPDR S&P500 ETF",   "etf",   "#EAF1FB", "#1D4E8C", "584.12",   "+0.41%", true),
        StockEntity("DIA",  "다우존스 ETF",       "etf",   "#EAF1FB", "#1D4E8C", "412.05",   "+0.18%", true),
        StockEntity("IWM",  "러셀2000 ETF",       "etf",   "#EAF1FB", "#1D4E8C", "221.87",   "-0.22%", false),
        StockEntity("VIX",  "변동성 지수",         "index", "#EAF1FB", "#1D4E8C", "14.22",    "-2.10%", false),
        StockEntity("AAPL", "애플",               "stock", "#EAF1FB", "#1D4E8C", "228.40",   "+0.36%", true),
        StockEntity("MSFT", "마이크로소프트",       "stock", "#EAF1FB", "#1D4E8C", "441.58",   "+0.29%", true),
        StockEntity("NVDA", "엔비디아",            "stock", "#FFF6E0", "#B57900", "187.42",   "+1.15%", true),
        StockEntity("TSLA", "테슬라",              "stock", "#FDEBEA", "#C23B3B", "248.09",   "+1.87%", true),
        StockEntity("AMZN", "아마존",              "stock", "#FFF6E0", "#B57900", "197.63",   "+0.52%", true),
        StockEntity("GOOGL","알파벳(구글)",         "stock", "#EAF1FB", "#1D4E8C", "176.28",   "-0.14%", false),
        StockEntity("META", "메타",                "stock", "#FDEBEA", "#C23B3B", "563.40",   "+0.88%", true),
        StockEntity("NFLX", "넷플릭스",             "stock", "#FDEBEA", "#C23B3B", "689.12",   "-0.31%", false),
        StockEntity("AMD",  "AMD",                 "stock", "#EAF1FB", "#1D4E8C", "142.55",   "+2.02%", true),
        StockEntity("AVGO", "브로드컴",              "stock", "#EAF1FB", "#1D4E8C", "168.90",   "+0.77%", true),
        StockEntity("PLTR", "팔란티어",              "stock", "#FFF6E0", "#B57900", "38.21",    "+3.14%", true),
        StockEntity("COIN", "코인베이스",             "stock", "#FDEBEA", "#C23B3B", "212.66",   "-1.02%", false),
        StockEntity("MU",   "마이크론",               "stock", "#EAF1FB", "#1D4E8C", "104.33",   "+0.95%", true),
    )

    // ── 3) 뉴스 50개 (오늘의 주요뉴스 2개 + 내 관심 매칭 3개 + 전체 45개) ──
    val news: List<NewsEntity> by lazy { buildNews() }

    private fun buildNews(): List<NewsEntity> {
        val list = mutableListOf<NewsEntity>()

        // 오늘의 주요뉴스 (featured, 상단 가로 스크롤 카드)
        list += NewsEntity(
            tag = "연준(Fed)",
            title = "파월 의장, 9월 금리 동결 시사… 시장 변동성 확대",
            source = "로이터", timeLabel = "12분 전",
            body = "파월 연준 의장이 9월 FOMC에서 기준금리를 현 수준에서 동결할 가능성이 높다고 언급하며 시장 변동성이 확대됐다. " +
                "인플레이션 지표가 목표치에 근접하고 있으나 고용시장 지표를 조금 더 지켜볼 필요가 있다고 밝혔다.\n\n" +
                "시장 참여자들은 다음 FOMC 회의에서 위원들의 점도표 변화를 주의 깊게 살펴볼 필요가 있다고 진단했다. " +
                "옵션 시장에서는 변동성 지수가 소폭 상승하며 단기 헤지 수요가 늘어난 것으로 나타났다.",
            tickers = "SPX,QQQ", isFeatured = true, isMatched = true
        )
        list += NewsEntity(
            tag = "엔비디아",
            title = "엔비디아, 신규 AI 칩 수주 소식에 시간외 강세",
            source = "블룸버그", timeLabel = "28분 전",
            body = "엔비디아가 주요 클라우드 업체와 차세대 AI 가속기 대규모 공급 계약을 체결했다는 소식이 전해지며 " +
                "시간외 거래에서 강세를 보였다. 업계 관계자에 따르면 이번 계약 규모는 기존 대비 큰 폭으로 확대된 것으로 알려졌다.\n\n" +
                "애널리스트들은 이번 수주가 내년 실적 가이던스 상향으로 이어질 가능성이 있다고 평가했다.",
            tickers = "NVDA", isFeatured = true, isMatched = true
        )

        // 내 관심 매칭 뉴스 (리스트 상단, isMatched = true)
        list += NewsEntity(
            tag = "테슬라",
            title = "테슬라, 3분기 인도량 예상치 상회… 목표주가 잇따라 상향",
            source = "CNBC", timeLabel = "5분 전",
            body = "테슬라가 발표한 3분기 인도량이 시장 예상치를 웃돌면서 주요 투자은행들이 잇따라 목표주가를 상향 조정했다. " +
                "특히 중국과 유럽 시장에서의 판매 회복이 두드러졌다는 평가다.\n\n" +
                "일부 애널리스트는 마진 개선 여부가 다음 실적 발표의 핵심 관전 포인트가 될 것이라고 내다봤다.",
            tickers = "TSLA", isMatched = true
        )
        list += NewsEntity(
            tag = "트럼프",
            title = "트럼프, 반도체 관세 추가 발표 예고… 관련주 주목",
            source = "AP", timeLabel = "19분 전",
            body = "반도체 수입에 대한 추가 관세 조치를 조만간 발표할 예정이라는 소식에 관련주가 주목받고 있다. " +
                "구체적인 세율과 적용 범위는 아직 공개되지 않았다.\n\n" +
                "업계에서는 공급망 재편 비용 부담이 커질 수 있다는 우려와 함께, 국내 생산 확대 기업에는 반사이익이 될 수 있다는 전망이 엇갈리고 있다.",
            tickers = "NVDA,QQQ", isMatched = true
        )
        list += NewsEntity(
            tag = "S&P500",
            title = "S&P500, 4일 연속 상승세… 금리 인하 기대감 반영",
            source = "WSJ", timeLabel = "41분 전",
            body = "S&P500 지수가 4거래일 연속 상승하며 연중 최고치에 근접했다. 다음 FOMC에서의 금리 인하 기대감이 반영된 결과라는 분석이 나온다.\n\n" +
                "옵션 시장의 감마 포지셔닝은 여전히 포지티브 구간에 머물러 있어 단기적으로는 변동성이 제한적일 것이라는 전망도 함께 제기된다.",
            tickers = "SPX", isMatched = true
        )

        // 전체 뉴스 나머지 (isMatched = false), 45개를 조합으로 생성
        val subjects = listOf(
            "전체속보","연준(Fed)","트럼프","관세·무역","지정학·이란","중국","반도체",
            "빅테크·AI","실적시즌","국채·채권","원자재·유가","암호화폐","은행·금융",
            "애플","마이크로소프트","아마존","알파벳(구글)","메타","AMD","브로드컴"
        )
        val tickerBySubject = mapOf(
            "애플" to "AAPL", "마이크로소프트" to "MSFT", "아마존" to "AMZN",
            "알파벳(구글)" to "GOOGL", "메타" to "META", "AMD" to "AMD", "브로드컴" to "AVGO"
        )
        val templates: List<(String) -> String> = listOf(
            { s -> "$s 관련 지표 발표에 시장 촉각" },
            { s -> "$s, 예상치 상회하며 투자심리 개선" },
            { s -> "$s 이슈로 변동성 확대… 옵션시장도 반응" },
            { s -> "$s 관련 규제 우려 부각, 관련주 주목" },
            { s -> "$s, 기관 매수세 유입되며 강세" },
            { s -> "$s 목표주가 잇따라 상향 조정" },
            { s -> "$s 리스크 재부각… 신중론 확산" },
            { s -> "$s, 공급망 이슈로 하락 압력" },
            { s -> "$s 관련 발언에 국채 금리 요동" },
            { s -> "$s, 실적 가이던스 상향 조정" },
        )
        val sources = listOf("로이터", "블룸버그", "CNBC", "AP", "WSJ", "파이낸셜타임스", "마켓워치", "다우존스")

        // 주제 x 템플릿 전체 조합(20 x 10 = 200가지)을 섞어서 중복 없이 45개를 뽑는다.
        // (기존 방식은 i%subjects.size 와 (i*7)%templates.size 의 주기가 맞물려
        //  20개마다 정확히 같은 조합이 반복되는 버그가 있었음)
        val allCombos = subjects.flatMap { s -> templates.indices.map { t -> s to t } }
        val chosenCombos = allCombos.shuffled().take(45)

        chosenCombos.forEachIndexed { i, (subject, templateIdx) ->
            val title = templates[templateIdx](subject)
            val source = sources[i % sources.size]
            val time = "${(chosenCombos.size - 1 - i) * 4 + 45}분 전"
            val ticker = tickerBySubject[subject] ?: ""
            val body = "$subject 관련 소식이 전해지며 시장의 관심이 집중되고 있다. 관련 업계 관계자들은 이번 흐름이 단기적인 이슈에 " +
                "그칠지, 추세적인 변화로 이어질지 주시하고 있다고 밝혔다.\n\n" +
                "옵션 시장에서는 이와 관련한 포지셔닝 변화가 감지되고 있으며, 애널리스트들은 다음 지표 발표 전까지 변동성이 " +
                "이어질 수 있다고 진단했다."
            list += NewsEntity(
                tag = subject, title = title, source = source, timeLabel = time,
                body = body, tickers = ticker, isFeatured = false, isMatched = false
            )
        }

        // ── 찌라시(미확인 루머) 뉴스 20개 ──
        // 제목에 [찌라시]를 붙여 정식 보도와 명확히 구분되도록 함.
        val rumorTemplates: List<(String) -> String> = listOf(
            { s -> "[찌라시] $s 대규모 인수설 솔솔... 조회공시 임박?" },
            { s -> "[찌라시] $s 내부자 매도설 확산, 진위 파악 중" },
            { s -> "[찌라시] $s CEO 사임설 급부상" },
            { s -> "[찌라시] $s 실적 어닝쇼크 루머 확산" },
            { s -> "[찌라시] $s 대형 계약 파기설... 소문만 무성" },
            { s -> "[찌라시] $s 상장폐지설 재점화, 사실무근 가능성" },
            { s -> "[찌라시] $s 대주주 지분 매각 정황 포착됐다는 루머" },
            { s -> "[찌라시] $s 규제 리스크 확대설, 커뮤니티 중심 확산" },
            { s -> "[찌라시] $s 신제품 출시 연기설" },
            { s -> "[찌라시] $s 회계 이슈 루머, 회사 측 \"노코멘트\"" },
        )
        val rumorSources = listOf("커뮤니티 지라시", "익명 제보", "사설 리서치방", "SNS 루머", "미확인 소식통")

        val rumorCombos = subjects.flatMap { s -> rumorTemplates.indices.map { t -> s to t } }
            .shuffled().take(20)

        rumorCombos.forEachIndexed { i, (subject, templateIdx) ->
            val title = rumorTemplates[templateIdx](subject)
            val source = rumorSources[i % rumorSources.size]
            val time = "${(rumorCombos.size - 1 - i) * 3 + 30}분 전"
            val ticker = tickerBySubject[subject] ?: ""
            val body = "$subject 관련하여 미확인 루머가 온라인 커뮤니티와 사설 리서치방을 중심으로 확산되고 있다.\n\n" +
                "⚠️ 이 내용은 공식적으로 확인되지 않은 루머성 정보입니다. 사실 관계가 검증되지 않았으니 투자 판단에 " +
                "신중을 기해야 하며, 공식 공시나 신뢰할 수 있는 언론 보도를 통해 재확인하시기 바랍니다."
            list += NewsEntity(
                tag = subject, title = title, source = source, timeLabel = time,
                body = body, tickers = ticker, isFeatured = false, isMatched = false
            )
        }

        return list
    }

    // ── 4) 옵션 파생 데이터 (관심종목 유니버스 20개 전체) ─────
    val optionMetrics: List<OptionMetricsEntity> by lazy { buildOptionMetrics() }

    private fun buildOptionMetrics(): List<OptionMetricsEntity> {
        fun note(regime: String, extra: String) = if (regime == "positive")
            "감마 포지티브 구간에 위치해 있어 단기 변동성이 제한적일 것으로 예상됩니다. $extra"
        else
            "감마 네거티브 구간으로, 가격 변동에 따라 딜러 헤지가 방향성을 증폭시킬 수 있어 상대적으로 높은 변동성이 예상됩니다. $extra"

        // ticker to (price, gex, dex, zeroGamma, volTrigger, putWall, callWall, regime)
        data class M(
            val ticker: String, val gex: String, val dex: String,
            val zeroGamma: String, val volTrigger: String,
            val putWall: String, val callWall: String,
            val putPct: Int, val curPct: Int, val callPct: Int,
            val regime: String, val extraNote: String
        )

        val rows = listOf(
            M("SPX", "+9.6B", "+2.1B", "5,760", "5,700", "5,600", "5,950", 20, 58, 84, "positive",
                "5,950 콜월 부근에서 상단 저항이 예상됩니다."),
            M("NDX", "+7.2B", "+1.4B", "20,100", "19,850", "19,500", "20,900", 22, 56, 82, "positive",
                "빅테크 실적 발표를 앞두고 콜월 부근 변동성 확대 가능성이 있습니다."),
            M("QQQ", "+3.1B", "+0.7B", "$489.00", "$482.00", "$470", "$510", 24, 60, 80, "positive",
                "510 콜월까지는 뚜렷한 저항 없이 완만한 상승이 가능해 보입니다."),
            M("SPY", "+5.4B", "+1.1B", "$576.00", "$568.00", "$560", "$598", 21, 57, 83, "positive",
                "지수 ETF 특성상 SPX와 유사한 흐름을 보일 가능성이 높습니다."),
            M("DIA", "+2.0B", "+0.4B", "$408.00", "$402.00", "$395", "$425", 23, 54, 81, "positive",
                "경기민감주 비중이 높아 매크로 지표 발표에 민감하게 반응할 수 있습니다."),
            M("IWM", "-1.1B", "-0.3B", "$225.00", "$230.00", "$205", "$238", 28, 47, 90, "negative",
                "소형주 특성상 변동성이 상대적으로 크게 나타날 수 있습니다."),
            M("VIX", "-0.6B", "-0.2B", "15.20", "16.50", "12.00", "22.00", 30, 42, 92, "negative",
                "변동성 지수 특성상 역방향 해석이 필요합니다 - 낮을수록 시장 안정을 의미합니다."),
            M("AAPL", "+2.8B", "+0.6B", "$225.00", "$220.00", "$210", "$238", 24, 59, 81, "positive",
                "실적 시즌 전까지는 박스권 흐름이 예상됩니다."),
            M("MSFT", "+3.6B", "+0.8B", "$436.00", "$428.00", "$415", "$455", 23, 58, 82, "positive",
                "클라우드 부문 성장세가 지속되며 안정적인 흐름이 예상됩니다."),
            M("NVDA", "+4.2B", "-1.8B", "$182.50", "$179.00", "$170", "$195", 22, 55, 82, "positive",
                "$182.50 제로감마 상단에서는 딜러의 매수 헤지가 주가를 지지할 가능성이 높습니다."),
            M("TSLA", "-2.4B", "-0.9B", "$240.00", "$252.00", "$220", "$270", 26, 48, 88, "negative",
                "인도량 발표 등 이벤트 리스크에 따라 변동성이 커질 수 있습니다."),
            M("AMZN", "+2.1B", "+0.5B", "$195.00", "$190.00", "$180", "$205", 25, 57, 83, "positive",
                "실적 발표 전까지 완만한 상승 흐름이 예상됩니다."),
            M("GOOGL", "-0.8B", "-0.2B", "$178.00", "$182.00", "$165", "$190", 27, 46, 87, "negative",
                "규제 이슈 관련 헤드라인에 따라 변동성이 확대될 수 있습니다."),
            M("META", "+3.3B", "+0.9B", "$558.00", "$548.00", "$530", "$585", 23, 59, 81, "positive",
                "광고 매출 성장세가 지속되며 안정적인 흐름이 예상됩니다."),
            M("NFLX", "-1.5B", "-0.4B", "$695.00", "$705.00", "$650", "$730", 27, 45, 89, "negative",
                "구독자 지표 발표에 따라 단기 변동성이 확대될 수 있습니다."),
            M("AMD", "-0.9B", "-0.3B", "$145.00", "$150.00", "$130", "$158", 26, 47, 88, "negative",
                "반도체 업황 관련 뉴스에 민감하게 반응할 수 있습니다."),
            M("AVGO", "+2.6B", "+0.6B", "$165.00", "$160.00", "$150", "$178", 24, 58, 82, "positive",
                "AI 반도체 수요 확대 기대감이 반영되어 있습니다."),
            M("PLTR", "-1.8B", "-0.6B", "$36.50", "$39.00", "$30", "$44", 29, 44, 91, "negative",
                "고평가 논란과 함께 변동성이 큰 구간입니다."),
            M("COIN", "-2.2B", "-0.7B", "$208.00", "$218.00", "$185", "$235", 28, 46, 90, "negative",
                "가상자산 가격 변동에 연동되어 높은 변동성을 보일 수 있습니다."),
            M("MU", "+1.9B", "+0.4B", "$102.00", "$99.00", "$92", "$112", 25, 56, 83, "positive",
                "메모리 반도체 업황 개선 기대감이 반영되어 있습니다."),
        )

        return rows.map { r ->
            // positive 레짐: 현재가가 이미 제로감마 위쪽 → 제로감마는 현재보다 낮게, VT는 그보다 더 낮게(풋월 쪽)
            // negative 레짐: 현재가가 이미 제로감마 아래쪽 → 제로감마는 현재보다 높게, VT는 그보다 더 높게(콜월 쪽)
            val zgPct = if (r.regime == "positive")
                (r.curPct - 12).coerceIn(r.putPct + 4, r.callPct - 4)
            else
                (r.curPct + 12).coerceIn(r.putPct + 4, r.callPct - 4)

            val vtPct = if (r.regime == "positive")
                (zgPct - 10).coerceIn(r.putPct + 2, r.callPct - 2)
            else
                (zgPct + 10).coerceIn(r.putPct + 2, r.callPct - 2)

            OptionMetricsEntity(
                ticker = r.ticker,
                gammaExposure = r.gex, deltaExposure = r.dex,
                zeroGamma = r.zeroGamma, volatilityTrigger = r.volTrigger,
                putWall = r.putWall, callWall = r.callWall,
                putWallPercent = r.putPct, currentPercent = r.curPct, callWallPercent = r.callPct,
                zeroGammaPercent = zgPct, volatilityTriggerPercent = vtPct,
                expertNote = note(r.regime, r.extraNote),
                updatedAt = "오늘 08:30 업데이트"
            )
        }
    }

    // ── 5) 알림함 샘플 ────────────────────────────────────
    val alerts: List<AlertEntity> = listOf(
        AlertEntity(type = "trade", title = "NVDA 콜월 근접 알림", subtitle = "현재가 $187.42, 콜월 $195까지 4.1% 남음", timeLabel = "3분 전", relatedTicker = "NVDA"),
        AlertEntity(type = "news",  title = "테슬라, 3분기 인도량 예상치 상회", subtitle = "관심 종목 테슬라 관련 속보가 도착했어요", timeLabel = "5분 전", relatedTicker = "TSLA"),
        AlertEntity(type = "trade", title = "SPX 제로감마 이탈 알림", subtitle = "변동성 확대 국면 전환 가능성, 리스크 관리 필요", timeLabel = "22분 전", relatedTicker = "SPX"),
        AlertEntity(type = "news",  title = "연준(Fed) 카테고리 속보", subtitle = "파월 의장, 9월 금리 동결 시사", timeLabel = "41분 전"),
    )
}
