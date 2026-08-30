package com.finset.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.finset.app.MainActivity
import com.finset.app.R
import kotlin.random.Random

/**
 * 서버 없이 앱 안에서 실시간 알림을 "시연"하기 위한 헬퍼.
 * 뉴스 알림 / 매매(옵션 레벨) 알림 두 종류를 실제 시스템 알림으로 띄운다.
 * 알림을 탭하면 MainActivity가 열리면서 해당 뉴스/종목 상세로 딥링크된다.
 */
object NotificationHelper {

    const val CHANNEL_ID = "finset_alerts"

    const val EXTRA_DEEPLINK_TYPE = "deeplink_type"   // "news" | "trade"
    const val EXTRA_DEEPLINK_NEWS_ID = "deeplink_news_id"
    const val EXTRA_DEEPLINK_TICKER = "deeplink_ticker"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "핀셋 알림",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "관심 카테고리 뉴스 및 매매 레벨 알림"
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun notifyNews(context: Context, newsId: Long, title: String, body: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DEEPLINK_TYPE, "news")
            putExtra(EXTRA_DEEPLINK_NEWS_ID, newsId)
        }
        show(context, intent, title, body)
    }

    fun notifyTrade(context: Context, ticker: String, title: String, body: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DEEPLINK_TYPE, "trade")
            putExtra(EXTRA_DEEPLINK_TICKER, ticker)
        }
        show(context, intent, title, body)
    }

    private fun show(context: Context, intent: Intent, title: String, body: String) {
        ensureChannel(context)

        val pendingIntent = PendingIntent.getActivity(
            context,
            Random.nextInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // 알림 권한이 없으면(Android 13+ 거부 상태) 조용히 무시
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(Random.nextInt(), notification)
        }
    }
}
