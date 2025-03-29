package io.github.teccheck.gear360app.service

enum class CameraMode(val value: String) {
    PHOTO("Photo"),
    VIDEO("Video"),
    LOOPING_VIDEO("Looping Video"),
    TIME_LAPSE("Time lapse");

    companion object {
        fun fromString(value: String): CameraMode? {
            return entries.firstOrNull { it.value == value }
        }
    }
}

enum class LoopingVideoTime(val value: String) {
    MIN_5("5min"),
    MIN_30("30min"),
    MIN_60("60min"),
    MAX("Max");

    companion object {
        fun fromString(value: String): LoopingVideoTime? {
            return entries.firstOrNull { it.value == value }
        }
    }
}

enum class TimerTime(val value: String) {
    OFF("Off"),
    SEC_2("2sec"),
    SEC_5("5sec"),
    SEC_10("10sec");

    companion object {
        fun fromString(value: String): TimerTime? {
            return entries.firstOrNull { it.value == value }
        }
    }
}

enum class BeepVolume(val value: String) {
    OFF("Off"),
    LOW("Low"),
    MID("Mid"),
    HIGH("High");


    companion object {
        fun fromString(value: String): BeepVolume? {
            return entries.firstOrNull { it.value == value }
        }
    }
}

enum class AutoPowerOffTime(val value: String) {
    MIN_1("1min"),
    MIN_3("3min"),
    MIN_5("5min"),
    MIN_30("30min");


    companion object {
        fun fromString(value: String): AutoPowerOffTime? {
            return entries.firstOrNull { it.value == value }
        }
    }
}

enum class LedIndicator(val value: String) {
    LED_ON("On"),
    LED_OFF("Off");

    companion object {
        fun fromString(value: String): LedIndicator? {
            return entries.firstOrNull { it.value == value }
        }
    }
}

// This is not reverse engineered. The string values (except false) don' mean anything
enum class CaptureState(val value: String) {
    NONE("false"),
    TIMER("timer"),
    CAPTURING("capturing"),
    RECORDING("recording");

    companion object {
        fun fromString(value: String): CaptureState? {
            return entries.firstOrNull { it.value == value }
        }
    }
}

enum class CaptureCommand(val value: String) {
    CAPTURE("capture"),
    RECORD("record"),
    RECORD_STOP("record stop"),
    TIMER("timer"),
    TIMER_STOP("timer stop");

    companion object {
        fun fromString(value: String): CaptureCommand? {
            return entries.firstOrNull { it.value == value }
        }
    }
}

enum class DeviceType(val value: String) {
    C200("SM-C200"),
    R210("SM-R210");

    companion object {
        fun fromString(value: String): DeviceType? {
            return entries.firstOrNull { it.value == value }
        }
    }
}