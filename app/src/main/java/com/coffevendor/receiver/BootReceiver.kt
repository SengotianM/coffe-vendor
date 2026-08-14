package com.coffevendor.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.coffevendor.service.OrderWebSocketService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, OrderWebSocketService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
