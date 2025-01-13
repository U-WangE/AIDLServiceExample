package com.uwange.clientapp

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.uwange.clientapp.databinding.ActivityMainBinding
import com.uwange.common.IAIDLCallback
import com.uwange.common.IAIDLService

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding!!

    private var mIAIDLService: IAIDLService? = null
    private val mConnection = object: ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            mIAIDLService = IAIDLService.Stub.asInterface(binder)
            Log.d("여기", mIAIDLService.toString())

            mIAIDLService?.basicTypes(0, 1L, true, 2f, 3.0, "4")
            mIAIDLService?.registerCallback(callback)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            TODO("Not yet implemented")
        }
    }

    private val callback = object: IAIDLCallback.Stub() {
        override fun onReceiveText(text: String?): Int {
            Log.d("여기", "Callback : $text")
            return 5
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindService()
    }

    private fun bindService() {
        val intent = Intent("Action.AIDLService")
        intent.setPackage("com.uwange.serviceapp")
        val result = bindService(intent, mConnection, BIND_AUTO_CREATE)
        Log.d("여기", result.toString())

    }
}