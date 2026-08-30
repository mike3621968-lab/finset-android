package com.finset.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finset.app.data.StockEntity
import com.finset.app.ui.components.FinTopBar
import com.finset.app.ui.components.LivePriceToggle
import com.finset.app.ui.components.StockRowItem
import com.finset.app.ui.theme.*
import com.finset.app.viewmodel.MainViewModel

@Composable
fun StockListScreen(
    viewModel: MainViewModel,
    startInEditMode: Boolean = false,
    onStockClick: (StockEntity) -> Unit,
    onAddClick: () -> Unit
) {
    val stocks by viewModel.interestedStocks.collectAsState()
    val livePriceEnabled by viewModel.livePriceEnabled.collectAsState()
    val liveConnectionError by viewModel.liveConnectionError.collectAsState()
    var editMode by remember { mutableStateOf(startInEditMode) }

    Column(modifier = Modifier.fillMaxSize().background(BgColor)) {
        FinTopBar(
            title = "내 관심종목",
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EditPill(editing = editMode, onClick = { editMode = !editMode })
                    Spacer(Modifier.width(8.dp))
                    IconCircle(onClick = onAddClick, symbol = "+")
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp, 0.dp, 20.dp, 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            LivePriceToggle(enabled = livePriceEnabled, onClick = { viewModel.toggleLivePrice() })
        }
        if (livePriceEnabled && liveConnectionError != null) {
            Text(
                liveConnectionError ?: "",
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = UpColor,
                modifier = Modifier.fillMaxWidth().padding(20.dp, 0.dp, 20.dp, 6.dp)
            )
        }

        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp).fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)).background(Color.White)
            .border(1.dp, LineColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onAddClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text("종목명 또는 티커 검색", fontSize = 13.sp, color = TextTertiary)
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(stocks, key = { it.ticker }) { stock ->
                StockRowItem(
                    stock = stock,
                    onClick = { onStockClick(stock) },
                    showDelete = editMode,
                    onDelete = { viewModel.removeStockInterest(stock) },
                    trailing = {
                        if (!editMode) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(stock.price, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    stock.changePercent,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (stock.isPositive) UpColor else DownColor
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun EditPill(editing: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (editing) Navy else Color.White)
            .border(1.dp, if (editing) Navy else LineColor, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(if (editing) "완료" else "편집", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = if (editing) Color.White else TextSecondary)
    }
}

@Composable
private fun IconCircle(onClick: () -> Unit, symbol: String) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White)
            .border(1.dp, LineColor, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}
