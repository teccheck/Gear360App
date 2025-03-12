package io.github.teccheck.gear360app.service

data class Gear360Status(
    val battery: Int? = null,
    val batteryState: String? = null,
    val totalMemory: Int? = null,
    val usedMemory: Int? = null,
    val freeMemory: Int? = null,
    val recordState: CaptureState? = null,
    val captureState: CaptureState? = null,
    val autoPowerOff: String? = null,
    val recordableTime: Int? = null,
    val capturableCount: Int? = null,
) {
    fun modify(
        battery: Int? = null,
        batteryState: String? = null,
        totalMemory: Int? = null,
        usedMemory: Int? = null,
        freeMemory: Int? = null,
        recordState: CaptureState? = null,
        captureState: CaptureState? = null,
        autoPowerOff: String? = null,
        recordableTime: Int? = null,
        capturableCount: Int? = null,
    ): Gear360Status {
        return Gear360Status(
            battery ?: this.battery,
            batteryState ?: this.batteryState,
            totalMemory ?: this.totalMemory,
            usedMemory ?: this.usedMemory,
            freeMemory ?: this.freeMemory,
            recordState ?: this.recordState,
            captureState ?: this.captureState,
            autoPowerOff ?: this.autoPowerOff,
            recordableTime ?: this.recordableTime,
            capturableCount ?: this.capturableCount,
        )
    }

    fun merge(status: Gear360Status): Gear360Status {
        return modify(
            status.battery,
            status.batteryState,
            status.totalMemory,
            status.usedMemory,
            status.freeMemory,
            status.recordState,
            status.captureState,
            status.autoPowerOff,
            status.recordableTime,
            status.capturableCount,
        )
    }

    fun isRecording(): Boolean {
        // FIXME
        return false
    }

    override fun toString(): String {
        return "Gear360Status(battery=$battery, batteryState=$batteryState, totalMemory=$totalMemory, usedMemory=$usedMemory, freeMemory=$freeMemory, recordState=$recordState, captureState=$captureState, autoPowerOff=$autoPowerOff, recordableTime=$recordableTime, capturableCount=$capturableCount)"
    }
}