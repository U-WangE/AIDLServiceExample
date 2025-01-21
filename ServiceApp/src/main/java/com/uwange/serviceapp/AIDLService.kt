package com.uwange.serviceapp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.DeadObjectException
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.uwange.common.IAIDLCallback
import com.uwange.common.IAIDLService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AIDLService : Service() {

    private val callbacks = mutableListOf<IAIDLCallback>()

    private val mBinder = object: IAIDLService.Stub() {
        override fun basicTypes(anInt: Int, aLong: Long, aBoolean: Boolean, aFloat: Float, aDouble: Double, aString: String?) {
            Log.d("BindService Callback", "CALLBACK || anInt : $anInt aLong : $aLong aBoolean : $aBoolean aFloat : $aFloat aDouble : $aDouble aString : $aString")
        }
        override fun registerCallback(callback: IAIDLCallback): Boolean {
            return if (boundClientApp != null) {
                callbacks.add(callback)
                true
            } else false
        }
    }

    private var boundClientApp : String? = null

    override fun onBind(intent: Intent): IBinder {
        boundClientApp = intent.extras?.getString("client_package")
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        sendingText()
        setNotificationChannel()
    }

    private fun sendingText() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                Thread.sleep(5000) // 5초 대기
                val text = "Hello from Service!"
                callbacks.forEach {
                    try {
                        val result = it.onReceiveText(text)
                        Log.d("BindService Result", "RESULT : $result")
                    } catch (deadObjectException: DeadObjectException) {
                        boundClientApp = null
                        callbacks.remove(it)
                    }
                }
            }
        }
    }

    @SuppressLint("ForegroundServiceType")
    private fun setNotificationChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel =
            NotificationChannel(
                "Service Noti Id","AIDL Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "This is AIDL Service"
                enableVibration(false)
                setShowBadge(false)
            }

        val notificationBuilder =
            NotificationCompat.Builder(
                this, "Service Noti Id"
            ).apply {
                setAutoCancel(false)
                setDefaults(Notification.DEFAULT_ALL)
                setSmallIcon(R.drawable.ic_launcher_foreground)
                setContentTitle("AIDL Service")
                setContentText("This is AIDL Service")
            }

        notificationManager.createNotificationChannel(notificationChannel)
        notificationManager.notify(123123, notificationBuilder.build())

        startForeground(123123, notificationBuilder.build())
    }
}