package com.poy.chatters.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AppReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        context.startService(Intent(context, AppService::class.java))
    }
}