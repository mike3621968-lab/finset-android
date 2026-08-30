package com.finset.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finset.app.ui.components.StockRowItem
import com.finset.app.ui.theme.*
import com.finset.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockSearchScreen(viewModel: MainViewModel, onDone: () -> Unit) {
    val allStocks by viewModel.stocks.collectAsState()
    var query by remember { mutableStateOf("") }

    val candidates = allStocks.filter { !it.isInterested }.filter {
        query.isBlank() || it.name.contains(query) || it.ticker.contains(query, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp, 14.dp, 20.dp, 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("종목명 또는 티커 검색", fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = LineColor,
                    unfocusedBorderColor = LineColor
                )
            )
            Text(
                "취소",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.clickable(onClick = onDone)
            )
        }

        Text(
            "전체 종목",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(20.dp, 10.dp, 20.dp, 6.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(candidates, key = { it.ticker }) { stock ->
                StockRowItem(
                    stock = stock,
                    onClick = {},
                    trailing = {
                        AddButton(onClick = { viewModel.toggleStockInterest(stock) })
                    }
                )
            }
        }
    }
}

@Composable
private fun AddButton(onClick: () -> Unit) {
    var added by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (added) Navy else Color.White)
            .border(1.dp, Navy, RoundedCornerShape(999.dp))
            .clickable(enabled = !added) {
                added = true
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            if (added) "추가됨" else "+ 추가",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (added) Color.White else Navy
        )
    }
}
