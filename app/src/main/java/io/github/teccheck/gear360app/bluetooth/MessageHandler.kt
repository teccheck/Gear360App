package io.github.teccheck.gear360app.bluetooth

import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

private const val TAG = "MessageHandler"

class MessageHandler {

    private val listeners = mutableListOf<MessageListener>()
    private val moshi = Moshi.Builder().build()
    private val adapter: JsonAdapter<BTMessageContainer> = moshi.adapter(BTMessageContainer::class.java)

    fun onReceive(channelId: Int, data: ByteArray?) {
        if (data == null || channelId != 204)
            return

        try {
            val message: BTMessageContainer? = adapter.fromJson(String(data))
            val msgId = message?.properties?.msgId ?: return

            when (msgId) {
                MsgId.DATE_TIME_REQ -> {
                    handleMessage(BTDateTimeRequest())
                }
                MsgId.CONFIG_INFO -> {
                    handleMessage(BTCameraConfigMessage.fromBTMessageContainer(message))
                }
                MsgId.DEVICE_INFO -> {
                    handleMessage(BTCameraInfoMessage.fromBTMessageContainer(message))
                }
                MsgId.WIDGET_INFO_REQ -> {
                    handleMessage(BTWidgetInfoRequest())
                }
                MsgId.WIDGET_INFO_RSP -> {
                    handleMessage(BTWidgetInfoResponse.fromBTMessageContainer(message))
                }
                MsgId.SHOT_RSP -> {
                    handleMessage(BTRemoteShotResponse.fromBTMessageContainer(message))
                }
                MsgId.COMMAND_RSP -> {
                    handleMessage(BTCommandResponse.fromBTMessageContainer(message))
                }
                MsgId.COMMAND_REQ -> {
                    handleMessage(BTCommandRequest.fromBTMessageContainer(message))
                }
                MsgId.DEVICE_DESC_URL -> {
                    handleMessage(BTDeviceDescriptionUrlMessage.fromBTMessageContainer(message))
                }
                else -> {
                    Log.w(TAG, "Couldn't handle message with id $msgId")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }
    }

    private fun handleMessage(message: BTMessage2?) {
        if (message == null) return

        for (listener in listeners)
            listener.onMessageReceive(message)
    }

    fun addMessageListener(messageListener: MessageListener) {
        listeners.add(messageListener)
    }

    fun removeMessageListener(messageListener: MessageListener) {
        listeners.remove(messageListener)
    }

    fun interface MessageListener {
        fun onMessageReceive(message: BTMessage2)
    }
}