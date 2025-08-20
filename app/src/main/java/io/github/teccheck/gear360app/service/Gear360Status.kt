package io.github.teccheck.gear360app.service

data class Gear360Status(
    val battery: Int? = null,
    val batteryState: BatteryState? = null,
    val totalStorage: Int? = null,
    val usedStorage: Int? = null,
    val freeStorage: Int? = null,
    val recordState: CaptureState? = null,
    val captureState: CaptureState? = null,
    val autoPowerOff: String? = null,
    val recordableTime: Int? = null,
    val capturableCount: Int? = null,
) {
    fun modify(
        battery: Int? = null,
        batteryState: BatteryState? = null,
        totalStorage: Int? = null,
        usedStorage: Int? = null,
        freeStorage: Int? = null,
        recordState: CaptureState? = null,
        captureState: CaptureState? = null,
        autoPowerOff: String? = null,
        recordableTime: Int? = null,
        capturableCount: Int? = null,
    ): Gear360Status {
        return Gear360Status(
            battery ?: this.battery,
            batteryState ?: this.batteryState,
            totalStorage ?: this.totalStorage,
            usedStorage ?: this.usedStorage,
            freeStorage ?: this.freeStorage,
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
            status.totalStorage,
            status.usedStorage,
            status.freeStorage,
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

    fun recordableTimeString(): String? {
        val time = recordableTime ?: return null
        val hours = (time / 3600).toString().padStart(2, '0')
        val minutes = ((time / 60) % 60).toString().padStart(2, '0')
        val seconds = (time % 60).toString().padStart(2, '0')

        return "$hours:$minutes:$seconds"
    }

    fun usedStoragePercentage(): Int? {
        if (!hasStorage()) return null
        val used = usedStorage ?: return  null
        val total = totalStorage ?: return null

        return 100 * used / total
    }

    fun hasStorage(): Boolean = totalStorage != 0

    override fun toString(): String {
        return "Gear360Status(battery=$battery, batteryState=$batteryState, totalMemory=$totalStorage, usedMemory=$usedStorage, freeMemory=$freeStorage, recordState=$recordState, captureState=$captureState, autoPowerOff=$autoPowerOff, recordableTime=$recordableTime, capturableCount=$capturableCount)"
    }
}