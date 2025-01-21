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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AIDLClientService : Service() {

    private var mIAIDLService: IAIDLService? = null

    private var reconnectJob: Job? = null

    private val callback = object: IAIDLCallback.Stub() {
        override fun onReceiveText(text: String?): Int {
            Log.d("BindService Callback", "CALLBACK : $text")
            return 5
        }
    }

    private val mConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            mIAIDLService = IAIDLService.Stub.asInterface(binder)

            mIAIDLService?.basicTypes(0, 1L, true, 2f, 3.0, "4")
            val result = mIAIDLService?.registerCallback(callback)

            Log.i("onServiceConnected", "AIDL SERVICE CONNECT : $name, result : $result")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i("onServiceDisconnected", "AIDL SERVICE DISCONNECT : $name")
            mIAIDLService = null
            Log.i("Reconnection", "Attempting to start reconnect job.")
            startReconnect()
        }
    }
    private fun startReconnect() {
        if (reconnectJob?.isActive == true) {
            Log.i("Reconnection", "Reconnect job is already active.")
            return
        }
        Log.d("Reconnection", "Starting new reconnect job. $mIAIDLService")
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            while (mIAIDLService == null) {
                Log.i("startReconnect", "Reconnection attempt in progress...")
                bindService()
                delay(5000L)
            }
        }
    }

    private fun stopReconnect() {
        reconnectJob?.cancel()
        reconnectJob = null
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        bindService()
        setNotificationChannel()
    }

    private fun bindService() {
        val intent = Intent().apply {
            action = "Action.AIDLService"
            setPackage("com.uwange.serviceapp")
            putExtra("client_package", packageName)
        }
        val result = bindService(intent, mConnection, BIND_AUTO_CREATE)
        Log.d("BindService Result", "RESULT : $result")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopReconnect()
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