package io.github.teccheck.gear360app.bluetooth

private const val TAG = "MessageSender"

class MessageSender(private val sender: Sender) {

    fun sendPhoneInfo(wifiMac: String) {
        val versionName = "1.2.00.8"
        sendCommand2(BTInfoMsg(false, wifiMac, "test", versionName, false))
    }

    fun sendWidgetInfoRequest() {
        sendCommand2(BTWidgetReq())
    }

    fun sendDateTimeResponse() {
        sendCommand2(BTDateTimeRsp())
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
        val action = BTCommandReq.ConfigAction(configName, configValue)
        val message = BTCommandReq(action)
        sendCommand2(message)
    }

    fun sendShotRequest(isPhotoMode: Boolean, isRecording: Boolean) {
        val mode = if (isPhotoMode)
            "capture"
        else if (isRecording)
            "record"
        else
            "record stop"

        sendCommand2(BTShotReq(mode))
    }

    fun sendLiveViewRequest() {
        val action = BTCommandReq.Action("execute", "liveview")
        val message = BTCommandReq(action)
        sendCommand2(message)
    }

    private fun sendCommand2(message: BTMessage) {
        sender.send(204, message.toJson().toString().encodeToByteArray())
    }

    fun interface Sender {
        fun send(channelId: Int, data: ByteArray)
    }
}