package com.example.hassanalhawary.ui.telegram_verification

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class TelegramVerificationActivity : AppCompatActivity() {



    val TAG = "TelegramVerificationActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val uri = intent.data
        if (uri == null) return

        val encoded = uri.getQueryParameter("data")
        val json = Uri.decode(encoded)
        val user = JSONObject(json)

        val telegramId = user.getLong("id")
        val firstName = user.getString("firstName")
        val lastName = user.optString("lastName", "")
        val username = user.optString("username", "")
        val photoUrl = user.optString("photoUrl", "")

        Log.d(TAG, "onCreate: $firstName + $photoUrl")


        // send to cloud function
        //verifyTelegramLogin(telegramData)


    }

    fun verifyTelegramLogin(telegramData: Map<String?, String?>) {

    }
}