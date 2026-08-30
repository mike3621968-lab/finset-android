package com.finset.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.finset.app.ui.theme.DownColor
import com.finset.app.ui.theme.Navy
import com.finset.app.ui.theme.TextPrimary
import com.finset.app.ui.theme.TextSecondary
import com.finset.app.ui.theme.UpColor
import com.finset.app.viewmodel.TradeSignalPopup

@Composable
fun TradeSignalPopupDialog(
    popup: TradeSignalPopup,
    onDismiss: () -> Unit,
    onViewDetail: () -> Unit
) {
    val isBullish = popup.signalType.startsWith("상승")
    val accent = if (isBullish) UpColor else DownColor

    // 팝업이 뜰 때 테두리가 accent 색으로 3번 깜빡였다가 사라짐
    val borderAlpha = remember(popup) { Animatable(0f) }
    LaunchedEffect(popup) {
        repeat(3) {
            borderAlpha.animateTo(1f, animationSpec = tween(220))
            borderAlpha.animateTo(0f, animationSpec = tween(220))
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.5.dp, accent.copy(alpha = borderAlpha.value), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(22.dp, 22.dp, 22.dp, 18.dp)) {

                Row {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accent.copy(alpha = 0.12f))
                            .padding(horizontal = 11.dp, vertical = 5.dp)
                    ) {
                        Text(
                            popup.signalType,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accent
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text(
                    "${popup.stockName} (${popup.ticker})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "현재가 ${popup.price}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    popup.message,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    color = TextPrimary
                )

                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("닫기")
                    }
                    Button(
                        onClick = onViewDetail,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy)
                    ) {
                        Text("상세보기", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
