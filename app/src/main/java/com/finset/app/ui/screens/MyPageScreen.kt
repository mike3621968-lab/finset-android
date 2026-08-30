package com.finset.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finset.app.ui.components.FinChip
import com.finset.app.ui.theme.*
import com.finset.app.viewmodel.MainViewModel

@Composable
fun MyPageScreen(viewModel: MainViewModel, onEditStocks: () -> Unit) {
    val categories by viewModel.categories.collectAsState()
    val interestedStocks by viewModel.interestedStocks.collectAsState()
    var editingCategory by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Text(
            "마이페이지",
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(20.dp, 14.dp, 20.dp, 10.dp)
        )

        Row(
            modifier = Modifier.padding(20.dp, 6.dp, 20.dp, 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Navy, Blue)))
            )
            Spacer(Modifier.width(13.dp))
            Column {
                Text("핀셋 찰리님", fontSize = 15.5.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            }
        }

        // 플랜 카드
        Row(
            modifier = Modifier
                .padding(20.dp, 12.dp, 20.dp, 14.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(Navy, Blue)))
                .padding(15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("현재 플랜", fontSize = 12.sp, color = Color(0xFFB9C8DE), fontWeight = FontWeight.SemiBold)
                Text("FinSet Free", fontSize = 14.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            Button(
                onClick = { /* 추후 결제 연동 */ },
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = RoundedCornerShape(9.dp)
            ) {
                Text("업그레이드", fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4A3300))
            }
        }

        // 관심 카테고리 카드
        Column(
            modifier = Modifier
                .padding(20.dp, 0.dp, 20.dp, 14.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, LineColor, RoundedCornerShape(14.dp))
                .padding(15.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("관심 카테고리", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    if (editingCategory) "완료" else "편집",
                    fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Blue,
                    modifier = Modifier.clickable { editingCategory = !editingCategory }
                )
            }
            Spacer(Modifier.height(11.dp))

            val shown = if (editingCategory) categories else categories.filter { it.isInterested }
            if (!editingCategory && shown.isEmpty()) {
                Text("선택된 카테고리가 없어요", fontSize = 12.5.sp, color = TextTertiary)
            } else {
                FlowChipsRow(items = shown.map { it.label to it.isInterested }) { index ->
                    if (editingCategory) viewModel.toggleCategory(shown[index])
                }
            }
        }

        // 관심 종목 카드
        Column(
            modifier = Modifier
                .padding(20.dp, 0.dp, 20.dp, 14.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, LineColor, RoundedCornerShape(14.dp))
                .padding(15.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("관심 종목", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    "편집", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Blue,
                    modifier = Modifier.clickable(onClick = onEditStocks)
                )
            }
            Spacer(Modifier.height(11.dp))
            interestedStocks.forEach { s ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(s.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        s.changePercent, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        color = if (s.isPositive) UpColor else DownColor
                    )
                }
            }
        }
    }
}

/** 칩을 여러 줄로 감싸 보여주는 간단한 Flow 레이아웃 (Row + wrapping 대체용, 세로 스크롤 없이 단순 Row 나열) */
@Composable
private fun FlowChipsRow(items: List<Pair<String, Boolean>>, onToggle: (Int) -> Unit) {
    // 간단화를 위해 3개씩 줄바꿈
    val chunked = items.withIndex().chunked(3)
    Column {
        chunked.forEach { rowItems ->
            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                rowItems.forEach { (index, pair) ->
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        FinChip(text = pair.first, selected = pair.second, onClick = { onToggle(index) })
                    }
                }
            }
        }
    }
}
