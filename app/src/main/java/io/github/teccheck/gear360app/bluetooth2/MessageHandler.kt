package io.github.teccheck.gear360app.bluetooth2

import android.util.Log
import org.json.JSONException
import org.json.JSONObject

private const val TAG = "MessageHandler"

class MessageHandler {

    private val listeners = mutableListOf<MessageListener>()

    fun onReceive(channelId: Int, data: ByteArray?) {
        if (data == null || channelId != 204)
            return

        try {
            val jsonObject = JSONObject(String(data))
            val msgId = jsonObject.getJSONObject(KEY_PROPERTIES).getString(KEY_MSGID)

            when (msgId) {
                CONFIG_INFO_MSGID -> {
                    handleMessage(BTConfigMsg.fromJson(jsonObject))
                }
                DEVICE_INFO_MSGID -> {
                    handleMessage(BTInfoRsp.fromJson(jsonObject))
                }
                else -> {
                    Log.w(TAG, "Couldn't handle message with id $msgId")
                }
            }
        } catch (e: JSONException) {
            Log.e(TAG, e.message, e)
        }
    }

    private fun handleMessage(message: BTMessage) {
        for (listener in listeners)
            listener.onMessageReceive(message)
    }

    fun addMessageListener(messageListener: MessageListener) {
        listeners.add(messageListener)
    }

    fun removeMessageListener(messageListener: MessageListener) {
        listeners.remove(messageListener)
    }

    interface MessageListener {
        fun onMessageReceive(message: BTMessage)
    }
}