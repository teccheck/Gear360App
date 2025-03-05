package io.github.teccheck.gear360app.bluetooth

import android.util.Log
import java.util.Date

class MessageLog {
    val messages = mutableListOf<LogMessage>()

    fun messageReceived(message: String) {
        Log.i(TAG, "RECV: $message")
        messages.add(LogMessage(message, Date(), SendDirection.FROM_CAMERA))
    }

    fun messageSent(message: String) {
        Log.i(TAG, "SEND: $message")
        messages.add(LogMessage(message, Date(), SendDirection.TO_CAMERA))
    }

    enum class SendDirection {
        TO_CAMERA,
        FROM_CAMERA
    }

    class LogMessage(val message: String, val date: Date, val direction: SendDirection)

    companion object {
        private const val TAG = "MessageLog"
    }
}