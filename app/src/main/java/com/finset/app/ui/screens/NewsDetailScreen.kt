package com.finset.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finset.app.data.NewsEntity
import com.finset.app.ui.components.FinTopBar
import com.finset.app.ui.components.TagPill
import com.finset.app.ui.theme.BgColor
import com.finset.app.ui.theme.LineColor
import com.finset.app.ui.theme.TextPrimary
import com.finset.app.ui.theme.TextTertiary
import com.finset.app.viewmodel.MainViewModel

@Composable
fun NewsDetailScreen(viewModel: MainViewModel, newsId: Long, onBack: () -> Unit) {
    var news by remember { mutableStateOf<NewsEntity?>(null) }

    LaunchedEffect(newsId) {
        news = viewModel.getNews(newsId)
    }

    Column(modifier = Modifier.fillMaxSize().background(BgColor)) {
        FinTopBar(onBack = onBack)

        news?.let { n ->
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
                TagPill(text = n.tag, highlighted = true)
                Spacer(Modifier.height(12.dp))
                Text(n.title, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, lineHeight = 28.sp)
                Spacer(Modifier.height(10.dp))
                Text("${n.source} · ${n.timeLabel}", fontSize = 12.sp, color = TextTertiary)
                Spacer(Modifier.height(16.dp))
                Divider(color = LineColor)
                Spacer(Modifier.height(16.dp))
                Text(n.body, fontSize = 14.sp, color = TextPrimary, lineHeight = 26.sp)

                val tickers = n.tickers.split(",").filter { it.isNotBlank() }
                if (tickers.isNotEmpty()) {
                    Spacer(Modifier.height(22.dp))
                    Text("관련 종목", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(10.dp))
                    Row {
                        tickers.forEach { t ->
                            Box(modifier = Modifier.padding(end = 8.dp)) {
                                TagPill(text = t, highlighted = true)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

// Material3 Divider (호환용 간단 래퍼)
@Composable
private fun Divider(color: androidx.compose.ui.graphics.Color) {
    androidx.compose.material3.Divider(color = color, thickness = 1.dp)
}
