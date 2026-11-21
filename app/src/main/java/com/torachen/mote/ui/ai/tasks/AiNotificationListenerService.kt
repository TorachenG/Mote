package com.torachen.mote.ui.ai.tasks

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/** 可选：某应用通知到达时触发任务；需用户在系统“通知使用权”里授权。 */
class AiNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName?.toString().orEmpty()
        if (packageName.isBlank() || packageName == this.packageName) return
        AiTaskTriggerBus.onNotification(this, packageName)
    }
}
