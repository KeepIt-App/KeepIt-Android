package com.haero_kim.keepit.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.haero_kim.keepit.MyApplication
import com.haero_kim.keepit.data.ItemDatabase
import com.haero_kim.keepit.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception


/**
 * 사용자에게 적절한 푸시알림 기능 제공하는 Worker 클래스
 * - 리마인드가 필요한 아이템이 생성되었을 때, WorkManger 에 Task 추가하도록 함
 */
class NotificationWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val itemDatabase = ItemDatabase.getInstance(MyApplication.instance)!!
    private val itemDao = itemDatabase.itemDao()

    /**
     * DB에 제거되지 않은 Item 이 남아있을 경우 (아직 구매하지 않은 물품이 있는 경우) Notification 생성
     */
    override fun doWork(): Result {
        val itemName: String = inputData.getString(ITEM_NAME)!!

        try {
            CoroutineScope(Dispatchers.IO).launch {
                if (itemDao.searchExactlyMatchByName(itemName).isNotEmpty()) {
                    createNotification(itemName)  // 아직 아이템이 남아있을 때 Push 발생
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        return Result.success()
    }

    /**
     * NotificationCompat.Builder 객체를 사용하여 알림 콘텐츠, 채널 설정
     */
    private fun createNotification(itemName: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(applicationContext, 0, intent, 0)

        val builder = NotificationCompat.Builder(applicationContext, MainActivity.CHANNEL_ID)
            .setSmallIcon(android.R.mipmap.sym_def_app_icon)
            .setContentTitle("🤔 ${itemName}을(를) 구매하셨나요?!")
            .setContentText("탭 하여 자세히 보기")
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(MainActivity.notificationId, builder.build())
        }
    }

    companion object {
        const val ITEM_NAME = "ITEM_NAME"
    }
}