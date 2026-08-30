package com.finset.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finset.app.ui.components.FinTopBar
import com.finset.app.ui.theme.*
import com.finset.app.viewmodel.MainViewModel

@Composable
fun StockDetailScreen(viewModel: MainViewModel, ticker: String, onBack: () -> Unit) {
    val metrics by viewModel.optionMetricsFlow(ticker).collectAsState(initial = null)
    var stockName by remember { mutableStateOf(ticker) }
    var price by remember { mutableStateOf("") }
    var change by remember { mutableStateOf("") }
    var isPositive by remember { mutableStateOf(true) }

    LaunchedEffect(ticker) {
        viewModel.getStock(ticker)?.let {
            stockName = it.name
            price = it.price
            change = it.changePercent
            isPositive = it.isPositive
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BgColor)) {
        FinTopBar(onBack = onBack)

        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp, 6.dp, 20.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(stockName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(Modifier.height(2.dp))
                Text(ticker, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(price, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text(change, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = if (isPositive) UpColor else DownColor)
            }
        }

        metrics?.let { m ->
            Spacer(Modifier.height(16.dp))
            // 2x2 지표 그리드
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("Gamma Exposure", m.gammaExposure, Modifier.weight(1f))
                    MetricCard("Delta Exposure", m.deltaExposure, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("Zero Gamma", m.zeroGamma, Modifier.weight(1f))
                    MetricCard("Volatility Trigger", m.volatilityTrigger, Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("풋월 · 콜월", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(28.dp))
                LevelBar(putPercent = m.putWallPercent, curPercent = m.currentPercent, callPercent = m.callWallPercent)
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(m.putWall, fontSize = 10.5.sp, color = TextTertiary)
                    Text(m.callWall, fontSize = 10.5.sp, color = TextTertiary)
                }
            }

            Spacer(Modifier.height(18.dp))
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BlueLight)
                    .border(1.dp, Color(0xFFD8E5F7), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Text("전문가 투자의견", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Blue)
                Spacer(Modifier.height(8.dp))
                Text(m.expertNote, fontSize = 12.5.sp, lineHeight = 20.sp, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Text(m.updatedAt, fontSize = 10.5.sp, color = TextTertiary)
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, LineColor, RoundedCornerShape(14.dp))
                    .padding(horizontal = 15.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("매매 레벨 알림", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("진입·청산 레벨 도달 시 즉시 알림", fontSize = 10.5.sp, color = TextTertiary)
                }
                ToggleSwitch(checked = m.alertEnabled, onToggle = { viewModel.toggleAlertEnabled(m) })
            }
            Spacer(Modifier.height(24.dp))
        } ?: run {
            Spacer(Modifier.height(40.dp))
            Text(
                "이 종목은 아직 옵션 데이터가 준비되지 않았어요.",
                fontSize = 13.sp,
                color = TextTertiary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, LineColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 13.dp, vertical = 12.dp)
    ) {
        Text(label, fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        val color = when {
            value.startsWith("+") -> UpColor
            value.startsWith("-") -> DownColor
            else -> TextPrimary
        }
        Text(value, fontSize = 15.5.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@Composable
private fun LevelBar(putPercent: Int, curPercent: Int, callPercent: Int) {
    Box(modifier = Modifier.fillMaxWidth().height(8.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barHeight = size.height
            drawLine(
                brush = Brush.horizontalGradient(listOf(DownColor, LineColor, UpColor)),
                start = Offset(0f, barHeight / 2),
                end = Offset(size.width, barHeight / 2),
                strokeWidth = barHeight,
                cap = StrokeCap.Round
            )
            listOf(putPercent, curPercent, callPercent).forEach { pct ->
                val x = size.width * (pct / 100f)
                drawLine(
                    color = Navy,
                    start = Offset(x, -6f),
                    end = Offset(x, barHeight + 6f),
                    strokeWidth = 4f
                )
            }
        }
    }
}

@Composable
private fun ToggleSwitch(checked: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .width(42.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (checked) Navy else LineColor)
            .clickable(onClick = onToggle)
            .padding(2.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White)
        )
    }
}
