package io.github.teccheck.gear360app.bluetooth

data class Gear360Status(
    var battery: Int,
    var batteryState: String,
    var totalMemory: Int,
    var usedMemory: Int,
    var freeMemory: Int,
    var recordState: String,
    var captureState: String,
    var autoPowerOff: String,
    var recordableTime: Int,
    var capturableCount: Int
) {
    fun isRecording(): Boolean {
        // FIXME
        return false
    }
}