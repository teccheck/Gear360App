package io.github.teccheck.gear360app.service

enum class CameraMode(val value: String) {
    PHOTO("Photo"),
    VIDEO("Video"),
    LOOPING_VIDEO("Looping Video"),
    TIME_LAPSE("Time Lapse")
}

enum class LoopingVideoTime(val value: String) {
    MIN_5("5min"),
    MIN_30("30min"),
    MIN_60("60min"),
    MAX("Max")
}

enum class TimerTime(val value: String) {
    OFF("Off"),
    SEC_2("2sec"),
    SEC_5("5sec"),
    SEC_10("10sec")
}

enum class BeepVolume(val value: String) {
    OFF("Off"),
    LOW("Low"),
    MID("Mid"),
    HIGH("High"),
}

enum class AutoPowerOffTime(val value: String) {
    MIN_1("1min"),
    MIN_3("3min"),
    MIN_5("5min"),
    MIN_30("30min")
}