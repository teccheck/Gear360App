package io.github.teccheck.gear360app.bluetooth

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
            val msgId =
                jsonObject.getJSONObject(MsgConst.PROPERTIES).getString(MsgConst.MSGID)

            when (msgId) {
                MessageIds.DATE_TIME_REQ -> {
                    handleMessage(BTDateTimeReq.fromJson(jsonObject))
                }
                MessageIds.CONFIG_INFO -> {
                    handleMessage(BTConfigMsg.fromJson(jsonObject))
                }
                MessageIds.DEVICE_INFO -> {
                    handleMessage(BTInfoRsp.fromJson(jsonObject))
                }
                MessageIds.WIDGET_INFO_REQ -> {
                    handleMessage(BTWidgetReq.fromJson(jsonObject))
                }
                MessageIds.WIDGET_INFO_RSP -> {
                    handleMessage(BTWidgetRsp.fromJson(jsonObject))
                }
                MessageIds.SHOT_RSP -> {
                    handleMessage(BTShotRsp.fromJson(jsonObject))
                }
                MessageIds.COMMAND_RSP -> {
                    handleMessage(BTCommandRsp.fromJson(jsonObject))
                }
                MessageIds.DEVICE_DESC_URL -> {
                    handleMessage(BTDeviceDescUrlMsg.fromJson(jsonObject))
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