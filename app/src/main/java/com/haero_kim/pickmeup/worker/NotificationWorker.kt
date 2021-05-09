package com.haero_kim.pickmeup.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.haero_kim.pickmeup.R
import com.haero_kim.pickmeup.view.MainActivity


/**
 * 사용자에게 적절한 푸시알림 기능 제공하는 Worker 클래스
 * - 리마인드가 필요한 아이템이 생성되었을 때, WorkManger 에 Task 추가하도록 함
 */
class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    /**
     * DB에 제거되지 않은 Item 이 남아있을 경우 (아직 구매하지 않은 물품이 있는 경우) Notification 생성
     */
    override fun doWork(): Result {
        val itemName: String = inputData.getString(ITEM_NAME)!!
        createNotification(itemName)

        return Result.success()
    }

    /**
     * NotificationCompat.Builder 객체를 사용하여 알림 콘텐츠, 채널 설정
     */
    private fun createNotification(itemName: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

        val builder = NotificationCompat.Builder(applicationContext, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_arrow_back_24)
                .setContentTitle("${itemName}을(를) 구매하셨나요?")
                .setContentText("탭 하여 확인하기")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(MainActivity.notificationId, builder.build())
        }
    }

    companion object {
        const val ITEM_NAME = "ITEM_NAME"
    }
}