package com.finset.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.finset.app.ui.components.BottomNavItem
import com.finset.app.ui.navigation.Routes
import com.finset.app.ui.screens.*
import com.finset.app.ui.theme.FinSetTheme
import com.finset.app.ui.theme.Navy
import com.finset.app.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinSetTheme {
                FinSetApp(viewModel)
            }
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
fun FinSetApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

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
            startDestination = Routes.ONBOARDING,
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
                AlertsScreen(viewModel)
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
}

private fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(this@navigateSingleTopTo.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
