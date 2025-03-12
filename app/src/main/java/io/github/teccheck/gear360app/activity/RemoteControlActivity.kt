package io.github.teccheck.gear360app.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import io.github.teccheck.gear360app.R
import io.github.teccheck.gear360app.service.AutoPowerOffTime
import io.github.teccheck.gear360app.service.BeepVolume
import io.github.teccheck.gear360app.service.CameraMode
import io.github.teccheck.gear360app.service.Gear360Config
import io.github.teccheck.gear360app.service.LedIndicator
import io.github.teccheck.gear360app.service.LoopingVideoTime
import io.github.teccheck.gear360app.service.TimerTime

private const val TAG = "RemoteControlActivity"

class RemoteControlActivity : BaseActivity() {

    private var uiInitialised = false

    private lateinit var captureButton: MaterialButton
    private lateinit var captureStopButton: MaterialButton
    private lateinit var settingsLayout: LinearLayout
    private lateinit var loopingVideoSettings: LinearLayout
    private lateinit var ledIndicatorSwitch: SwitchCompat

    private lateinit var modeSelector: MaterialButtonToggleGroup
    private lateinit var loopingVideoToggle: MaterialButtonToggleGroup
    private lateinit var timerToggle: MaterialButtonToggleGroup
    private lateinit var beepToggle: MaterialButtonToggleGroup
    private lateinit var powerToggle: MaterialButtonToggleGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_control)

        setupBackButton()

        loopingVideoSettings = findViewById(R.id.looping_video_settings)
        settingsLayout = findViewById(R.id.camera_settings)

        captureButton = findViewById(R.id.btn_capture)
        captureButton.setOnClickListener { onCaptureButtonPressed() }

        captureStopButton = findViewById(R.id.btn_capture_stop)
        captureStopButton.setOnClickListener { gear360Service?.requestCaptureStop() }

        val settingsTitle = findViewById<TextView>(R.id.camera_settings_title)
        settingsTitle.setOnClickListener { onSettingsTitleClicked() }

        ledIndicatorSwitch = findViewById(R.id.led_switch)
        ledIndicatorSwitch.setOnCheckedChangeListener { _, checked ->
            if (uiInitialised) onLedSwitchChanged(checked)
        }

        modeSelector = findViewById(R.id.mode_toggle)
        modeSelector.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && uiInitialised) onModeSelected(checkedId)
        }

        loopingVideoToggle = findViewById(R.id.looping_video_toggle)
        loopingVideoToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && uiInitialised) onLoopingVideoRecordTimerSelected(checkedId)
        }

        timerToggle = findViewById(R.id.timer_toggle)
        timerToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && uiInitialised) onTimerTimeSelected(checkedId)
        }

        beepToggle = findViewById(R.id.beep_toggle)
        beepToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && uiInitialised) onBeepVolumeSelected(checkedId)
        }

        powerToggle = findViewById(R.id.power_toggle)
        powerToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && uiInitialised) onPowerTimerSelected(checkedId)
        }

        startGear360Service()
    }

    override fun onGearServiceConnected() {
        gear360Service?.gear360Config?.observe(this) {
            setupUiValues(it)
        }
    }

    private fun setupUiValues(config: Gear360Config) {
        Log.d(TAG, "setupUiValues")

        config.mode?.let { setCameraMode(it) }

        setToggle(
            modeSelector, when (config.mode) {
                CameraMode.PHOTO -> R.id.btn_mode_photo
                CameraMode.VIDEO -> R.id.btn_mode_video
                CameraMode.LOOPING_VIDEO -> R.id.btn_mode_looping_video
                CameraMode.TIME_LAPSE -> R.id.btn_mode_time_lapse
                else -> null
            }
        )

        setToggle(
            loopingVideoToggle, when (config.loopingVideoTime) {
                LoopingVideoTime.MIN_5 -> R.id.btn_looping_5_min
                LoopingVideoTime.MIN_30 -> R.id.btn_looping_30_min
                LoopingVideoTime.MIN_60 -> R.id.btn_looping_60_min
                LoopingVideoTime.MAX -> R.id.btn_looping_max
                else -> null
            }
        )

        setToggle(
            timerToggle, when (config.timer) {
                TimerTime.OFF -> R.id.btn_timer_off
                TimerTime.SEC_2 -> R.id.btn_timer_2_sec
                TimerTime.SEC_5 -> R.id.btn_timer_5_sec
                TimerTime.SEC_10 -> R.id.btn_timer_10_sec
                else -> null
            }
        )

        setToggle(
            beepToggle, when (config.beep) {
                BeepVolume.OFF -> R.id.btn_beep_off
                BeepVolume.LOW -> R.id.btn_beep_low
                BeepVolume.MID -> R.id.btn_beep_mid
                BeepVolume.HIGH -> R.id.btn_beep_high
                else -> null
            }
        )

        setToggle(
            powerToggle, when (config.autoPowerOffTime) {
                AutoPowerOffTime.MIN_1 -> R.id.btn_power_1_min
                AutoPowerOffTime.MIN_3 -> R.id.btn_power_3_min
                AutoPowerOffTime.MIN_5 -> R.id.btn_power_5_min
                AutoPowerOffTime.MIN_30 -> R.id.btn_power_30_min
                else -> null
            }
        )

        config.led?.let { ledIndicatorSwitch.isChecked = it == LedIndicator.LED_ON }
        uiInitialised = true
    }

    private fun setToggle(toggleGroup: MaterialButtonToggleGroup, @IdRes id: Int?) {
        if (id == null) {
            toggleGroup.clearChecked()
        } else {
            toggleGroup.check(id)
        }
    }

    private fun onModeSelected(buttonId: Int) {
        val mode = when (buttonId) {
            R.id.btn_mode_photo -> CameraMode.PHOTO
            R.id.btn_mode_video -> CameraMode.VIDEO
            R.id.btn_mode_looping_video -> CameraMode.LOOPING_VIDEO
            R.id.btn_mode_time_lapse -> CameraMode.TIME_LAPSE
            else -> return
        }

        gear360Service?.setCameraMode(mode)
        setCameraMode(mode)
    }

    private fun onCaptureButtonPressed() {
        gear360Service?.requestCapture()
    }

    private fun onSettingsTitleClicked() {
        if (settingsLayout.visibility == View.VISIBLE) {
            settingsLayout.visibility = View.GONE
        } else {
            settingsLayout.visibility = View.VISIBLE
        }
    }

    private fun onLoopingVideoRecordTimerSelected(buttonId: Int) {
        val time = when (buttonId) {
            R.id.btn_looping_5_min -> LoopingVideoTime.MIN_5
            R.id.btn_looping_30_min -> LoopingVideoTime.MIN_30
            R.id.btn_looping_60_min -> LoopingVideoTime.MIN_60
            R.id.btn_looping_max -> LoopingVideoTime.MAX
            else -> return
        }

        gear360Service?.setLoopingVideoTime(time)
    }

    private fun onLedSwitchChanged(checked: Boolean) {
        gear360Service?.setLedIndicators(checked)
    }

    private fun onTimerTimeSelected(buttonId: Int) {
        val time = when (buttonId) {
            R.id.btn_timer_off -> TimerTime.OFF
            R.id.btn_timer_2_sec -> TimerTime.SEC_2
            R.id.btn_timer_5_sec -> TimerTime.SEC_5
            R.id.btn_timer_10_sec -> TimerTime.SEC_10
            else -> return
        }

        gear360Service?.setTimerTime(time)
    }

    private fun onBeepVolumeSelected(buttonId: Int) {
        val volume = when (buttonId) {
            R.id.btn_beep_off -> BeepVolume.OFF
            R.id.btn_beep_low -> BeepVolume.LOW
            R.id.btn_beep_mid -> BeepVolume.MID
            R.id.btn_beep_high -> BeepVolume.HIGH
            else -> return
        }

        gear360Service?.setBeepVolume(volume)
    }

    private fun onPowerTimerSelected(buttonId: Int) {
        val time = when (buttonId) {
            R.id.btn_power_1_min -> AutoPowerOffTime.MIN_1
            R.id.btn_power_3_min -> AutoPowerOffTime.MIN_3
            R.id.btn_power_5_min -> AutoPowerOffTime.MIN_5
            R.id.btn_power_30_min -> AutoPowerOffTime.MIN_30
            else -> return
        }

        gear360Service?.setAutoPowerOffTime(time)
    }

    private fun setCameraMode(cameraMode: CameraMode) {
        when (cameraMode) {
            CameraMode.PHOTO -> {
                captureButton.setText(R.string.btn_take_photo)
                loopingVideoSettings.visibility = View.GONE
            }

            CameraMode.VIDEO -> {
                captureButton.setText(R.string.btn_capture_video)
                loopingVideoSettings.visibility = View.GONE
            }

            CameraMode.LOOPING_VIDEO -> {
                captureButton.setText(R.string.btn_capture_looping_video)
                loopingVideoSettings.visibility = View.VISIBLE
            }

            CameraMode.TIME_LAPSE -> {
                captureButton.setText(R.string.btn_capture_time_lapse)
                loopingVideoSettings.visibility = View.GONE
            }
        }
    }
}