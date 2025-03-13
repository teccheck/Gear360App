package io.github.teccheck.gear360app.bluetooth

import android.location.Location
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.github.teccheck.gear360app.service.AutoPowerOffTime
import io.github.teccheck.gear360app.service.BeepVolume
import io.github.teccheck.gear360app.service.CameraMode
import io.github.teccheck.gear360app.service.CaptureCommand
import io.github.teccheck.gear360app.service.CaptureState
import io.github.teccheck.gear360app.service.Gear360Config
import io.github.teccheck.gear360app.service.LedIndicator
import io.github.teccheck.gear360app.service.LoopingVideoTime
import io.github.teccheck.gear360app.service.TimerTime
import java.util.Date

private const val TAG = "MessageSender"

class MessageSender(private val sender: Sender) {

    private val moshi = Moshi.Builder().build()
    private val adapter: JsonAdapter<BTMessageContainer> =
        moshi.adapter(BTMessageContainer::class.java)

    fun sendPhoneInfo(wifiMac: String) {
        sendMessage(
            BTPhoneInfoMessage(
                false,
                wifiMac,
                "test",
                "1.2.00.8",
                false
            ).asBtMessageContainer()
        )
    }

    fun sendWidgetInfoRequest() {
        sendMessage(BTWidgetInfoRequest().asBtMessageContainer())
    }

    fun sendWidgetInfoResponse(location: Location?) {
        sendMessage(BTWidgetInfoResponsePhone(location).asBtMessageContainer())
    }

    fun sendDateTimeResponse() {
        sendMessage(BTDateTimeResponse(Date(System.currentTimeMillis())).asBtMessageContainer())
    }

    fun sendChangeMode(mode: CameraMode) {
        sendConfigChangeCommand(Gear360Config(mode = mode))
    }

    fun sendChangeLoopingVideoTime(timer: LoopingVideoTime) {
        sendConfigChangeCommand(Gear360Config(loopingVideoTime = timer))
    }

    fun sendSetLedIndicators(active: LedIndicator) {
        sendConfigChangeCommand(Gear360Config(led = active))
    }

    fun sendChangeTimerTimer(time: TimerTime) {
        sendConfigChangeCommand(Gear360Config(timer = time))
    }

    fun sendChangeBeepVolume(volume: BeepVolume) {
        sendConfigChangeCommand(Gear360Config(beep = volume))
    }

    fun sendChangePowerOffTime(time: AutoPowerOffTime) {
        sendConfigChangeCommand(Gear360Config(autoPowerOffTime = time))
    }

    private fun sendConfigChangeCommand(config: Gear360Config) {
        sendMessage(BTCommandRequest(action = BTCommandActionConfig(config)).asBtMessageContainer())
    }

    fun sendCaptureRequest(cameraMode: CameraMode) {
        val command = when (cameraMode) {
            CameraMode.PHOTO -> CaptureCommand.CAPTURE
            else -> CaptureCommand.RECORD
        }

        sendMessage(BTRemoteShotRequest(command).asBtMessageContainer())
    }

    fun sendCaptureStopRequest(cameraMode: CameraMode, captureState: CaptureState) {
        val command = when(captureState) {
            CaptureState.NONE -> return
            CaptureState.TIMER -> CaptureCommand.TIMER_STOP
            else -> CaptureCommand.RECORD_STOP
        }

        sendMessage(BTRemoteShotRequest(command).asBtMessageContainer())
    }

    fun sendLiveViewRequest() {
        sendMessage(BTCommandRequest(action = BTCommandActionLiveView()).asBtMessageContainer())
    }

    private fun sendMessage(message: BTMessageContainer) {
        sender.send(204, adapter.toJson(message).encodeToByteArray())
    }

    fun interface Sender {
        fun send(channelId: Int, data: ByteArray)
    }
}