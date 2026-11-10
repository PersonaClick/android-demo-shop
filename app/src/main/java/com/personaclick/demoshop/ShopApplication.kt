package com.personaclick.demoshop

import android.app.Application
import android.util.Log
import com.personalization.NetworkLogger
import com.personalization.PersonaClick
import com.personalization.PersonaClickConfig
import com.personalization.SDK

/**
 * Initializes the SDK once, for the whole process. The UI kit's widgets find this instance by
 * [ShopConfig.SHOP_ID]; the app's own calls reach it with `PersonaClick.getInstance(SHOP_ID)`.
 *
 * Dashboard popups need nothing here: the SDK shows them over the activity on screen by default.
 */
class ShopApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Debug builds print every SDK request to logcat (tag ShopHttp).
        if (BuildConfig.DEBUG) SDK.networkLogger = NetworkLogger(::logHttpCall)
        PersonaClick.initialize(
            context = this,
            config = PersonaClickConfig(
                shopId = ShopConfig.SHOP_ID,
                stream = ShopConfig.STREAM,
                // The app has no push: there is no Firebase configuration, so no token to send.
                autoSendPushToken = false,
            ),
        )
    }

    private fun logHttpCall(
        method: String,
        url: String,
        requestBody: String?,
        responseCode: Int,
        responseBody: String?,
        durationMs: Long,
        error: String?,
    ) {
        val line = buildString {
            append(method).append(' ').append(url).append(" -> ").append(responseCode)
            append(" (").append(durationMs).append(" ms)")
            if (!requestBody.isNullOrEmpty()) append("\n  body: ").append(requestBody)
            if (error != null) append("\n  error: ").append(error)
            // Error answers carry the reason; successful ones are too long for the log.
            if (responseCode >= 400 && !responseBody.isNullOrEmpty()) append("\n  answer: ").append(responseBody)
        }
        if (responseCode in 200..399) Log.d(HTTP_TAG, line) else Log.w(HTTP_TAG, line)
    }

    private companion object {
        const val HTTP_TAG = "ShopHttp"
    }
}
