package com.finset.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.finset.app.notification.NotificationHelper
import com.finset.app.ui.components.BottomNavItem
import com.finset.app.ui.components.TradeSignalPopupDialog
import com.finset.app.ui.navigation.Routes
import com.finset.app.ui.screens.*
import com.finset.app.ui.theme.FinSetTheme
import com.finset.app.ui.theme.Navy
import com.finset.app.viewmodel.MainViewModel

/** 알림 탭으로 앱이 열렸을 때 어디로 이동할지 담는 간단한 딥링크 정보 */
data class DeepLink(val type: String, val newsId: Long? = null, val ticker: String? = null, val nonce: Long = System.nanoTime())

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(application)
    }

    // Compose가 직접 구독하는 반응형 상태. recreate() 없이 onNewIntent에서 바로 갱신한다.
    private val deepLinkState = mutableStateOf<DeepLink?>(null)

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* 결과는 무시 - 거부해도 앱은 정상 동작 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.ensureChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        deepLinkState.value = extractDeepLink(intent)

        setContent {
            FinSetTheme {
                FinSetApp(viewModel, deepLink = deepLinkState.value)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // recreate() 없이, 상태만 갱신 - Compose가 알아서 재구성(recompose)한다.
        deepLinkState.value = extractDeepLink(intent)
    }

    private fun extractDeepLink(intent: Intent?): DeepLink? {
        val type = intent?.getStringExtra(NotificationHelper.EXTRA_DEEPLINK_TYPE) ?: return null
        return when (type) {
            "news" -> DeepLink("news", newsId = intent.getLongExtra(NotificationHelper.EXTRA_DEEPLINK_NEWS_ID, 0L))
            "trade" -> DeepLink("trade", ticker = intent.getStringExtra(NotificationHelper.EXTRA_DEEPLINK_TICKER))
            else -> null
        }
    }
}

private val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "홈", Icons.Filled.Home),
    BottomNavItem(Routes.STOCK_LIST, "내관심종목", Icons.Filled.Bookmark),
    BottomNavItem(Routes.ALERTS, "알림", Icons.Filled.Notifications),
    BottomNavItem(Routes.MY_PAGE, "MY", Icons.Filled.Person),
)

@Composable
fun FinSetApp(viewModel: MainViewModel, deepLink: DeepLink? = null) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // 최초 컴포지션 시점의 딥링크 값만 시작화면 결정에 사용 (그래프는 한 번만 만들어지므로)
    val initialDeepLink = remember { deepLink }

    // deepLink 값이 바뀔 때마다(=새 알림을 탭할 때마다) 실행 - 앱이 이미 떠있는 상태에서도 동작
    LaunchedEffect(deepLink) {
        if (deepLink != null) {
            when (deepLink.type) {
                "news" -> deepLink.newsId?.let {
                    navController.navigate(Routes.newsDetail(it))
                }
                "trade" -> deepLink.ticker?.let {
                    navController.navigate(Routes.stockDetail(it))
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute != null && currentRoute in Routes.bottomNavRoutes) {
                NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigateSingleTopTo(item.route)
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Navy,
                                selectedTextColor = Navy,
                                indicatorColor = androidx.compose.ui.graphics.Color(0xFFEAF1FB)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (initialDeepLink != null) Routes.HOME else Routes.ONBOARDING,
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(viewModel) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            }
            composable(Routes.HOME) {
                HomeScreen(viewModel, onNewsClick = { news ->
                    navController.navigate(Routes.newsDetail(news.id))
                })
            }
            composable(Routes.STOCK_LIST) {
                StockListScreen(
                    viewModel,
                    onStockClick = { stock -> navController.navigate(Routes.stockDetail(stock.ticker)) },
                    onAddClick = { navController.navigate(Routes.STOCK_SEARCH) }
                )
            }
            composable(
                Routes.STOCK_DETAIL,
                arguments = listOf(navArgument("ticker") { type = NavType.StringType })
            ) { backStack ->
                val ticker = backStack.arguments?.getString("ticker") ?: ""
                StockDetailScreen(viewModel, ticker, onBack = { navController.popBackStack() })
            }
            composable(Routes.STOCK_SEARCH) {
                StockSearchScreen(viewModel, onDone = { navController.popBackStack() })
            }
            composable(Routes.ALERTS) {
                AlertsScreen(viewModel, onAlertClick = { alert ->
                    when (alert.type) {
                        "news" -> alert.relatedNewsId?.let { navController.navigate(Routes.newsDetail(it)) }
                        "trade" -> alert.relatedTicker?.let { navController.navigate(Routes.stockDetail(it)) }
                    }
                })
            }
            composable(Routes.MY_PAGE) {
                MyPageScreen(viewModel, onEditStocks = { navController.navigate(Routes.STOCK_LIST) })
            }
            composable(
                Routes.NEWS_DETAIL,
                arguments = listOf(navArgument("newsId") { type = NavType.LongType })
            ) { backStack ->
                val newsId = backStack.arguments?.getLong("newsId") ?: 0L
                NewsDetailScreen(viewModel, newsId, onBack = { navController.popBackStack() })
            }
        }
    }

    // 상승/하락 진입·청산 시그널 발생 시 - 어떤 화면에 있든 눈에 띄는 팝업으로 표시
    val popup by viewModel.popupEvent.collectAsState()
    popup?.let { p ->
        TradeSignalPopupDialog(
            popup = p,
            onDismiss = { viewModel.consumePopup() },
            onViewDetail = {
                viewModel.consumePopup()
                navController.navigate(Routes.stockDetail(p.ticker))
            }
        )
    }
}

private fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(this@navigateSingleTopTo.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
