package io.github.teccheck.gear360app.service

data class Gear360Config(
    val mode: CameraMode? = null,
    val timer: TimerTime? = null,
    val beep: BeepVolume? = null,
    val led: LedIndicator? = null,
    val autoPowerOffTime: AutoPowerOffTime? = null,
    val loopingVideoTime: LoopingVideoTime? = null,
) {
    fun modify(
        mode: CameraMode? = null,
        timer: TimerTime? = null,
        beep: BeepVolume? = null,
        led: LedIndicator? = null,
        autoPowerOffTime: AutoPowerOffTime? = null,
        loopingVideoTime: LoopingVideoTime? = null,
    ): Gear360Config {
        return Gear360Config(
            mode ?: this.mode,
            timer ?: this.timer,
            beep ?: this.beep,
            led ?: this.led,
            autoPowerOffTime ?: this.autoPowerOffTime,
            loopingVideoTime ?: this.loopingVideoTime,
        )
    }

    fun merge(config: Gear360Config): Gear360Config {
        return modify(
            config.mode,
            config.timer,
            config.beep,
            config.led,
            config.autoPowerOffTime,
            config.loopingVideoTime
        )
    }
}
