package com.example.playground.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.playground.util.toast

class MyService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        toast("My Service has started!", this)
        return START_STICKY
    }
    override fun onDestroy() {
        super.onDestroy()
        toast("My Service has been destroyed!", this)
    }
}