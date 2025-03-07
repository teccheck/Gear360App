package io.github.teccheck.gear360app.bluetooth

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.github.teccheck.gear360app.service.AutoPowerOffTime
import io.github.teccheck.gear360app.service.BeepVolume
import io.github.teccheck.gear360app.service.CameraMode
import io.github.teccheck.gear360app.service.ConfigConstants
import io.github.teccheck.gear360app.service.LoopingVideoTime
import io.github.teccheck.gear360app.service.TimerTime
import java.util.Date

private const val TAG = "MessageSender"

class MessageSender(private val sender: Sender) {

    private val moshi = Moshi.Builder().build()
    private val adapter: JsonAdapter<BTMessageContainer> =
        moshi.adapter(BTMessageContainer::class.java)

    fun sendPhoneInfo(wifiMac: String) {
        sendMessage(BTPhoneInfoMessage(false, wifiMac, "test", "1.2.00.8", false).asBtMessageContainer())
    }

    fun sendWidgetInfoRequest() {
        sendMessage(BTWidgetInfoRequest().asBtMessageContainer())
    }

    fun sendDateTimeResponse() {
        sendMessage(BTDateTimeResponse(Date(System.currentTimeMillis())).asBtMessageContainer())
    }

    fun sendChangeMode(mode: CameraMode) {
        sendConfigChangeCommand(ConfigConstants.MODE, mode.value)
    }

    fun sendChangeLoopingVideoTime(timer: LoopingVideoTime) {
        sendConfigChangeCommand(ConfigConstants.LOOPING_VIDEO_TIME, timer.value)
    }

    fun sendSetLedIndicators(active: Boolean) {
        val value = if (active) ConfigConstants.LED_ON else ConfigConstants.LED_OFF
        sendConfigChangeCommand(ConfigConstants.LED_INDICATOR, value)
    }

    fun sendChangeTimerTimer(time: TimerTime) {
        sendConfigChangeCommand(ConfigConstants.TIMER, time.value)
    }

    fun sendChangeBeepVolume(volume: BeepVolume) {
        sendConfigChangeCommand(ConfigConstants.BEEP, volume.value)
    }

    fun sendChangePowerOffTime(time: AutoPowerOffTime) {
        sendConfigChangeCommand(ConfigConstants.AUTO_POWER_OFF, time.value)
    }

    private fun sendConfigChangeCommand(configName: String, configValue: String) {
        sendMessage(BTCommandRequest(BTCommandActionConfig(configName, configValue)).asBtMessageContainer())
    }

    fun sendShotRequest(isPhotoMode: Boolean, isRecording: Boolean) {
        val mode = if (isPhotoMode)
            "capture"
        else if (isRecording)
            "record"
        else
            "record stop"

        sendMessage(BTRemoteShotRequest(mode).asBtMessageContainer())
    }

    fun sendLiveViewRequest() {
        sendMessage(BTCommandRequest(BTCommandActionLiveView()).asBtMessageContainer())
    }

    private fun sendMessage(message: BTMessageContainer) {
        sender.send(204, adapter.toJson(message).encodeToByteArray())
    }

    fun interface Sender {
        fun send(channelId: Int, data: ByteArray)
    }
}