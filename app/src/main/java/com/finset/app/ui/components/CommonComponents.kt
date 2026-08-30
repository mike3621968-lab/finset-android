package com.finset.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finset.app.data.NewsEntity
import com.finset.app.data.StockEntity
import com.finset.app.ui.theme.*

/** 선택 가능한 칩 (카테고리, 필터 등에서 공용으로 사용) */
@Composable
fun FinChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Navy else Color.White)
            .border(1.dp, if (selected) Navy else LineColor, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else TextSecondary
        )
    }
}

/** 상단 공통 탑바 (뒤로가기 + 타이틀) */
@Composable
fun FinTopBar(
    title: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기", tint = TextPrimary)
                }
            }
            if (title != null) {
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            }
        }
        trailing?.invoke()
    }
}

/** 뉴스 리스트 항목 */
@Composable
fun NewsListItem(news: NewsEntity, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, LineColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row {
            TagPill(text = news.tag, highlighted = news.isMatched)
        }
        Spacer(Modifier.height(6.dp))
        Text(news.title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary, lineHeight = 19.sp)
        Spacer(Modifier.height(6.dp))
        Text("${news.source} · ${news.timeLabel}", fontSize = 11.5.sp, color = TextTertiary)
    }
}

@Composable
fun TagPill(text: String, highlighted: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (highlighted) Color(0xFFFFF6E0) else BlueLight)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (highlighted) GoldDark else Blue
        )
    }
}

/** 종목 리스트 행 (관심종목, 검색 결과 공용) */
@Composable
fun StockRowItem(
    stock: StockEntity,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null,
    showDelete: Boolean = false,
    onDelete: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, LineColor, RoundedCornerShape(14.dp))
            .clickable(enabled = !showDelete, onClick = onClick)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showDelete && onDelete != null) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(UpColor)
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "삭제", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(11.dp))
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(hexToColor(stock.iconBg)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stock.ticker.take(4),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = hexToColor(stock.iconColor)
                )
            }
            Spacer(Modifier.width(11.dp))
            Column {
                Text(stock.name, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text("${stock.ticker} · ${typeLabel(stock.type)}", fontSize = 11.sp, color = TextTertiary)
            }
        }
        trailing?.invoke()
    }
}

private fun typeLabel(type: String) = when (type) {
    "index" -> "지수"
    "etf" -> "ETF"
    else -> "주식"
}

fun hexToColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: Exception) {
    Color.LightGray
}

/** 하단 네비게이션 아이템 데이터 */
data class BottomNavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
