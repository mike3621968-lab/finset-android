package com.finset.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finset.app.ui.components.FinChip
import com.finset.app.ui.components.FinSetMark
import com.finset.app.ui.theme.Navy
import com.finset.app.ui.theme.TextSecondary
import com.finset.app.viewmodel.MainViewModel

@Composable
fun OnboardingScreen(viewModel: MainViewModel, onFinish: () -> Unit) {
    val categories by viewModel.categories.collectAsState()
    val stocks by viewModel.stocks.collectAsState()
    val interestedStocks = stocks.filter { it.isInterested }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(36.dp))
        FinSetMark(markSize = 68.dp, cornerRadius = 18.dp)
        Spacer(Modifier.height(18.dp))
        Text("핀셋에 오신 것을\n환영합니다", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF12203D))
        Spacer(Modifier.height(6.dp))
        Text(
            "관심 있는 카테고리와 종목을 골라주시면\n필요한 정보만 정밀하게 집어드릴게요.",
            fontSize = 13.5.sp, color = TextSecondary, lineHeight = 20.sp
        )

        Spacer(Modifier.height(28.dp))
        Text("관심 카테고리 (다중 선택)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF12203D))
        Spacer(Modifier.height(10.dp))
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 96.dp),
            modifier = Modifier.height(180.dp)
        ) {
            items(categories) { cat ->
                Box(modifier = Modifier.padding(4.dp)) {
                    FinChip(text = cat.label, selected = cat.isInterested, onClick = { viewModel.toggleCategory(cat) })
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("관심 종목", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF12203D))
        Spacer(Modifier.height(10.dp))
        Text(
            if (interestedStocks.isEmpty()) "관심 종목이 아직 없어요 (관심종목 화면에서 추가할 수 있어요)"
            else interestedStocks.joinToString(" · ") { it.name },
            fontSize = 12.5.sp, color = TextSecondary
        )

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Navy)
        ) {
            Text("시작하기", fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(28.dp))
    }
}
