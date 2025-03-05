package io.github.teccheck.gear360app.bluetooth

object ConfigConstants {
    const val MODE = "mode"
    const val LOOPING_VIDEO_TIME = "looping video recording time"
    const val TIMER = "timer"
    const val BEEP = "beep"
    const val AUTO_POWER_OFF = "auto power off"
    const val LED_INDICATOR = "led indicator"

    const val LED_ON = "On"
    const val LED_OFF = "Off"
}

class Gear360Configs {
    private val map = HashMap<String, Config>()

    fun setConfigs(configs: List<Config>) {
        for (config in configs)
            setConfig(config)
    }

    fun setConfig(config: Config) {
        map[config.name] = config
    }

    fun getConfig(name: String): Config? {
        return map[name.lowercase()]
    }

    fun getCameraMode(): CameraMode? {
        val config = getConfig(ConfigConstants.MODE) ?: return null

        return if (config.defaultValue.equals("Photo", true))
            CameraMode.PHOTO
        else if (config.defaultValue.equals("Video", true))
            CameraMode.VIDEO
        else if (config.defaultValue.equals("Looping Video", true))
            CameraMode.LOOPING_VIDEO
        else if (config.defaultValue.equals("Time Lapse", true))
            CameraMode.TIME_LAPSE
        else
            null
    }

    fun getLoopingVideoTime(): LoopingVideoTime? {
        val config = getConfig(ConfigConstants.LOOPING_VIDEO_TIME) ?: return null

        return if (config.defaultValue.equals("5min", true))
            LoopingVideoTime.MIN_5
        else if (config.defaultValue.equals("30min", true))
            LoopingVideoTime.MIN_30
        else if (config.defaultValue.equals("60min", true))
            LoopingVideoTime.MIN_60
        else if (config.defaultValue.equals("max", true))
            LoopingVideoTime.MAX
        else
            null
    }

    fun getTimerTime(): TimerTime? {
        val config = getConfig(ConfigConstants.TIMER) ?: return null

        return if (config.defaultValue.equals("Off", true))
            TimerTime.OFF
        else if (config.defaultValue.equals("2sec", true))
            TimerTime.SEC_2
        else if (config.defaultValue.equals("5sec", true))
            TimerTime.SEC_5
        else if (config.defaultValue.equals("10sec", true))
            TimerTime.SEC_10
        else
            null
    }

    fun getBeepVolume(): BeepVolume? {
        val config = getConfig(ConfigConstants.BEEP) ?: return null

        return if (config.defaultValue.equals("Off", true))
            BeepVolume.OFF
        else if (config.defaultValue.equals("Low", true))
            BeepVolume.LOW
        else if (config.defaultValue.equals("Mid", true))
            BeepVolume.MID
        else if (config.defaultValue.equals("High", true))
            BeepVolume.HIGH
        else
            null
    }

    fun getAutoPowerOffTimer(): AutoPowerOffTime? {
        val config = getConfig(ConfigConstants.AUTO_POWER_OFF) ?: return null

        return if (config.defaultValue.equals("1min", true))
            AutoPowerOffTime.MIN_1
        else if (config.defaultValue.equals("3min", true))
            AutoPowerOffTime.MIN_3
        else if (config.defaultValue.equals("5min", true))
            AutoPowerOffTime.MIN_5
        else if (config.defaultValue.equals("30min", true))
            AutoPowerOffTime.MIN_30
        else
            null
    }

    fun getIsLedEnabled(): Boolean? {
        val config = getConfig(ConfigConstants.LED_INDICATOR) ?: return null

        if (config.defaultValue.equals(ConfigConstants.LED_ON, true))
            return true
        else if (config.defaultValue.equals(ConfigConstants.LED_OFF, true))
            return false

        return null
    }

    data class Config(
        val name: String,
        val defaultValue: String,
        val values: Array<String>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Config

            if (name != other.name) return false
            if (defaultValue != other.defaultValue) return false
            if (!values.contentEquals(other.values)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + defaultValue.hashCode()
            result = 31 * result + values.contentHashCode()
            return result
        }

        override fun toString(): String {
            return "Config(name='$name', defaultValue='$defaultValue', values=${values.contentToString()})"
        }
    }
}