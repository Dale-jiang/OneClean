package com.kk.newcleanx.ui.functions.notice.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("MyFirebaseMessagingService", "onNewToken~~~~${token}")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if (message.getData().isNotEmpty()) {
            Log.e("MyFirebaseMessagingService", "onMessageReceived~~~~")
            //val value: String? = message.getData()["XX"]
        }
    }


}