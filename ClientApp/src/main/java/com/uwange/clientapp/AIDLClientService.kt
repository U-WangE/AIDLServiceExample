package com.uwange.clientapp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.uwange.common.IAIDLCallback
import com.uwange.common.IAIDLService

class AIDLClientService : Service() {

    private var mIAIDLService: IAIDLService? = null

    override fun onBind(intent: Intent): IBinder? = null

    private val mConnection = object: ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            mIAIDLService = IAIDLService.Stub.asInterface(binder)

            mIAIDLService?.basicTypes(0, 1L, true, 2f, 3.0, "4")
            mIAIDLService?.registerCallback(callback)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            TODO("Not yet implemented")
        }
    }

    private val callback = object: IAIDLCallback.Stub() {
        override fun onReceiveText(text: String?): Int {
            Log.d("BindService Callback", "CALLBACK : $text")
            return 5
        }
    }

    override fun onCreate() {
        super.onCreate()

        bindService()
        setNotificationChannel()
    }

    private fun bindService() {
        val intent = Intent("Action.AIDLService")
        intent.setPackage("com.uwange.serviceapp")
        val result = bindService(intent, mConnection, BIND_AUTO_CREATE)
        Log.d("BindService Result", "RESULT : ${result.toString()}")
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mConnection)
    }

    @SuppressLint("ForegroundServiceType")
    private fun setNotificationChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel =
            NotificationChannel(
                "Client Noti Id","AIDL Client",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "This is AIDL Client"
                enableVibration(false)
                setShowBadge(false)
            }

        val notificationBuilder =
            NotificationCompat.Builder(
                this, "Client Noti Id"
            ).apply {
                setAutoCancel(false)
                setDefaults(Notification.DEFAULT_ALL)
                setSmallIcon(R.drawable.ic_launcher_foreground)
                setContentTitle("AIDL Client")
                setContentText("This is AIDL Client")
            }

        notificationManager.createNotificationChannel(notificationChannel)
        notificationManager.notify(456456, notificationBuilder.build())

        startForeground(456456, notificationBuilder.build())
    }
}