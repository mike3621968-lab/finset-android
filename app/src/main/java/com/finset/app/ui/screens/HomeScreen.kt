package com.finset.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finset.app.data.NewsEntity
import com.finset.app.ui.components.FinChip
import com.finset.app.ui.components.FinSetMark
import com.finset.app.ui.components.NewsListItem
import com.finset.app.ui.theme.*
import com.finset.app.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNewsClick: (NewsEntity) -> Unit
) {
    val featured by viewModel.featuredNews.collectAsState()
    val myNews by viewModel.myNews.collectAsState()
    val allNews by viewModel.allNews.collectAsState()
    val simulationEnabled by viewModel.simulationEnabled.collectAsState()
    val livePriceEnabled by viewModel.livePriceEnabled.collectAsState()
    val liveConnectionError by viewModel.liveConnectionError.collectAsState()
    var filter by remember { mutableStateOf("mine") } // "mine" | "all"

    LazyColumn(modifier = Modifier.fillMaxSize().background(BgColor)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp, 14.dp, 20.dp, 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FinSetMark(markSize = 30.dp, cornerRadius = 9.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("핀셋", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
                }
                SimulationToggle(enabled = simulationEnabled, onClick = { viewModel.toggleSimulation() })
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp, 0.dp, 20.dp, 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                LivePriceToggle(enabled = livePriceEnabled, onClick = { viewModel.toggleLivePrice() })
            }
        }
        if (livePriceEnabled && liveConnectionError != null) {
            item {
                Text(
                    liveConnectionError ?: "",
                    fontSize = 10.5.sp,
                    color = UpColor,
                    modifier = Modifier.fillMaxWidth().padding(20.dp, 0.dp, 20.dp, 4.dp)
                )
            }
        }

        if (simulationEnabled) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp, 0.dp, 20.dp, 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TestPopupChip("상승진입", UpColor, Modifier.weight(1f)) { viewModel.fireTestPopup("bull_entry") }
                    TestPopupChip("하락진입", DownColor, Modifier.weight(1f)) { viewModel.fireTestPopup("bear_entry") }
                    TestPopupChip("상승청산", UpColor, Modifier.weight(1f)) { viewModel.fireTestPopup("bull_exit") }
                    TestPopupChip("하락청산", DownColor, Modifier.weight(1f)) { viewModel.fireTestPopup("bear_exit") }
                }
            }
        }

        item {
            Text("오늘의 주요뉴스", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(20.dp, 14.dp, 20.dp, 10.dp))
        }
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                items(featured) { news ->
                    FeaturedNewsCard(news = news, onClick = { onNewsClick(news) })
                }
            }
        }

        item {
            Row(
                modifier = Modifier.padding(20.dp, 16.dp, 20.dp, 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FinChip("내 관심", selected = filter == "mine", onClick = { filter = "mine" })
                FinChip("전체", selected = filter == "all", onClick = { filter = "all" })
            }
        }

        val listToShow = if (filter == "mine") myNews else allNews
        items(listToShow) { news ->
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                NewsListItem(news = news, onClick = { onNewsClick(news) })
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun FeaturedNewsCard(news: NewsEntity, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(210.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(Navy, Blue)))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Text(news.tag, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Gold)
        Spacer(Modifier.height(8.dp))
        Text(news.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, lineHeight = 19.sp)
        Spacer(Modifier.height(10.dp))
        Text("${news.source} · ${news.timeLabel}", fontSize = 10.5.sp, color = Color(0xFFB9C8DE))
    }
}

@Composable
private fun SimulationToggle(enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (enabled) Navy else Color.White)
            .border(1.dp, if (enabled) Navy else LineColor, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (enabled) Gold else TextTertiary)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            if (enabled) "시뮬레이션 ON" else "시뮬레이션 OFF",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Color.White else TextSecondary
        )
    }
}

@Composable
private fun TestPopupChip(text: String, accent: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = accent,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun LivePriceToggle(enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (enabled) Color(0xFFEAF7EE) else Color.White)
            .border(1.dp, if (enabled) Color(0xFF2FA84F) else LineColor, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (enabled) Color(0xFF2FA84F) else TextTertiary)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            if (enabled) "실시간 시세 ON (KIS)" else "실시간 시세 OFF",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Color(0xFF2FA84F) else TextSecondary
        )
    }
}
