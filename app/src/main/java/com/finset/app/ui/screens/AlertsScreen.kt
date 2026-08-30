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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finset.app.data.AlertEntity
import com.finset.app.ui.components.FinChip
import com.finset.app.ui.components.FinTopBar
import com.finset.app.ui.components.SimulationToggle
import com.finset.app.ui.components.TestPopupChip
import com.finset.app.ui.theme.*
import com.finset.app.viewmodel.MainViewModel

@Composable
fun AlertsScreen(
    viewModel: MainViewModel,
    onAlertClick: (AlertEntity) -> Unit
) {
    val alerts by viewModel.alerts.collectAsState()
    val simulationEnabled by viewModel.simulationEnabled.collectAsState()
    var filter by remember { mutableStateOf("all") } // all | news | trade

    val filtered = when (filter) {
        "news" -> alerts.filter { it.type == "news" }
        "trade" -> alerts.filter { it.type == "trade" }
        else -> alerts
    }

    Column(modifier = Modifier.fillMaxSize().background(BgColor)) {
        FinTopBar(
            title = "알림함",
            trailing = {
                SimulationToggle(enabled = simulationEnabled, onClick = { viewModel.toggleSimulation() })
            }
        )

        if (simulationEnabled) {
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

        Row(
            modifier = Modifier.padding(20.dp, 4.dp, 20.dp, 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FinChip("전체", selected = filter == "all", onClick = { filter = "all" })
            FinChip("뉴스", selected = filter == "news", onClick = { filter = "news" })
            FinChip("매매신호", selected = filter == "trade", onClick = { filter = "trade" })
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.id }) { alert ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, LineColor, RoundedCornerShape(14.dp))
                        .clickable(onClick = { onAlertClick(alert) })
                        .padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (alert.type == "trade") Color(0xFFFFF6E0) else BlueLight)
                    )
                    Spacer(Modifier.width(11.dp))
                    Column {
                        Text(alert.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, lineHeight = 19.sp)
                        Spacer(Modifier.height(3.dp))
                        Text(alert.subtitle, fontSize = 11.5.sp, color = TextSecondary, lineHeight = 17.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(alert.timeLabel, fontSize = 10.5.sp, color = TextTertiary)
                    }
                }
            }
        }
    }
}
