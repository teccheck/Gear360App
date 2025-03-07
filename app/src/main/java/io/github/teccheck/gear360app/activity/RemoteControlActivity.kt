package io.github.teccheck.gear360app.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import io.github.teccheck.gear360app.R
import io.github.teccheck.gear360app.service.AutoPowerOffTime
import io.github.teccheck.gear360app.service.BeepVolume
import io.github.teccheck.gear360app.service.CameraMode
import io.github.teccheck.gear360app.service.LoopingVideoTime
import io.github.teccheck.gear360app.service.TimerTime

private const val TAG = "RemoteControlActivity"

class RemoteControlActivity : BaseActivity() {

    private var uiInitialised = false

    private lateinit var captureButton: MaterialButton
    private lateinit var settingsLayout: LinearLayout
    private lateinit var loopingVideoSettings: LinearLayout
    private lateinit var ledIndicatorSwitch: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_control)

        setupBackButton()

        loopingVideoSettings = findViewById(R.id.looping_video_settings)
        settingsLayout = findViewById(R.id.camera_settings)

        captureButton = findViewById(R.id.btn_capture)
        captureButton.setOnClickListener { onCaptureButtonPressed() }

        val settingsTitle = findViewById<TextView>(R.id.camera_settings_title)
        settingsTitle.setOnClickListener { onSettingsTitleClicked() }

        ledIndicatorSwitch = findViewById(R.id.led_switch)
        ledIndicatorSwitch.setOnCheckedChangeListener { _, checked ->
            if (uiInitialised) onLedSwitchChanged(checked)
        }

        val modeSelector = findViewById<MaterialButtonToggleGroup>(R.id.mode_toggle)
        modeSelector.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && uiInitialised) onModeSelected(checkedId)
        }

        val loopingVideoToggle = findViewById<MaterialButtonToggleGroup>(R.id.looping_video_toggle)
        loopingVideoToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && uiInitialised) onLoopingVideoRecordTimerSelected(checkedId)
        }

        val timerToggle = findViewById<MaterialButtonToggleGroup>(R.id.timer_toggle)
        timerToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && uiInitialised) onTimerTimeSelected(checkedId)
        }

        val beepToggle = findViewById<MaterialButtonToggleGroup>(R.id.beep_toggle)
        beepToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && uiInitialised) onBeepVolumeSelected(checkedId)
        }

        val powerToggle = findViewById<MaterialButtonToggleGroup>(R.id.power_toggle)
        powerToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && uiInitialised) onPowerTimerSelected(checkedId)
        }

        startGear360Service()
    }

    override fun onGearServiceConnected() {
        setupUiValues()
    }

    private fun setupUiValues() {
        val gear360Configs = gear360Service?.gear360Configs ?: return

        Log.d(TAG, "setupUiValues")

        // TODO: Remove duplicate code

        gear360Configs.getCameraMode()?.let { setCameraMode(it) }
        when (gear360Configs.getCameraMode()) {
            CameraMode.PHOTO -> {
                findViewById<MaterialButton>(R.id.btn_mode_photo).isChecked = true
            }
            CameraMode.VIDEO -> {
                findViewById<MaterialButton>(R.id.btn_mode_video).isChecked = true
            }
            CameraMode.LOOPING_VIDEO -> {
                findViewById<MaterialButton>(R.id.btn_mode_looping_video).isChecked = true
            }
            CameraMode.TIME_LAPSE -> {
                findViewById<MaterialButton>(R.id.btn_mode_time_lapse).isChecked = true
            }
            else -> {
                Log.d(TAG, "Mode else")
            }
        }

        when (gear360Configs.getLoopingVideoTime()) {
            LoopingVideoTime.MIN_5 -> {
                findViewById<MaterialButton>(R.id.btn_looping_5_min).isChecked = true
            }
            LoopingVideoTime.MIN_30 -> {
                findViewById<MaterialButton>(R.id.btn_looping_30_min).isChecked = true
            }
            LoopingVideoTime.MIN_60 -> {
                findViewById<MaterialButton>(R.id.btn_looping_60_min).isChecked = true
            }
            LoopingVideoTime.MAX -> {
                findViewById<MaterialButton>(R.id.btn_looping_max).isChecked = true
            }
            else -> {}
        }

        when (gear360Configs.getTimerTime()) {
            TimerTime.OFF -> {
                findViewById<MaterialButton>(R.id.btn_timer_off).isChecked = true
            }
            TimerTime.SEC_2 -> {
                findViewById<MaterialButton>(R.id.btn_timer_2_sec).isChecked = true
            }
            TimerTime.SEC_5 -> {
                findViewById<MaterialButton>(R.id.btn_timer_5_sec).isChecked = true
            }
            TimerTime.SEC_10 -> {
                findViewById<MaterialButton>(R.id.btn_timer_10_sec).isChecked = true
            }
            else -> {}
        }

        when (gear360Configs.getBeepVolume()) {
            BeepVolume.OFF -> {
                findViewById<MaterialButton>(R.id.btn_beep_off).isChecked = true
            }
            BeepVolume.LOW -> {
                findViewById<MaterialButton>(R.id.btn_beep_low).isChecked = true
            }
            BeepVolume.MID -> {
                findViewById<MaterialButton>(R.id.btn_beep_mid).isChecked = true
            }
            BeepVolume.HIGH -> {
                findViewById<MaterialButton>(R.id.btn_beep_high).isChecked = true
            }
            else -> {}
        }

        when (gear360Configs.getAutoPowerOffTimer()) {
            AutoPowerOffTime.MIN_1 -> {
                findViewById<MaterialButton>(R.id.btn_power_1_min).isChecked = true
            }
            AutoPowerOffTime.MIN_3 -> {
                findViewById<MaterialButton>(R.id.btn_power_3_min).isChecked = true
            }
            AutoPowerOffTime.MIN_5 -> {
                findViewById<MaterialButton>(R.id.btn_power_5_min).isChecked = true
            }
            AutoPowerOffTime.MIN_30 -> {
                findViewById<MaterialButton>(R.id.btn_power_30_min).isChecked = true
            }
            else -> {}
        }

        val enabled = gear360Configs.getIsLedEnabled()
        if (enabled != null)
            ledIndicatorSwitch.isChecked = enabled

        uiInitialised = true
    }

    private fun onModeSelected(buttonId: Int) {
        val mode = when (buttonId) {
            R.id.btn_mode_photo -> CameraMode.PHOTO
            R.id.btn_mode_video -> CameraMode.VIDEO
            R.id.btn_mode_looping_video -> CameraMode.LOOPING_VIDEO
            R.id.btn_mode_time_lapse -> CameraMode.TIME_LAPSE
            else -> return
        }

        Log.d(TAG, "Set mode to $mode")
        gear360Service?.messageSender?.sendChangeMode(mode)
        setCameraMode(mode)
    }

    private fun onCaptureButtonPressed() {
        gear360Service?.let {
            val photoMode = it.gear360Configs.getCameraMode() == CameraMode.PHOTO
            val recording = it.gear360Status?.isRecording() ?: false
            it.messageSender.sendShotRequest(photoMode, recording)
        }
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

        gear360Service?.messageSender?.sendChangeLoopingVideoTime(time)
    }

    private fun onLedSwitchChanged(checked: Boolean) {
        gear360Service?.messageSender?.sendSetLedIndicators(checked)
    }

    private fun onTimerTimeSelected(buttonId: Int) {
        val time = when (buttonId) {
            R.id.btn_timer_off -> TimerTime.OFF
            R.id.btn_timer_2_sec -> TimerTime.SEC_2
            R.id.btn_timer_5_sec -> TimerTime.SEC_5
            R.id.btn_timer_10_sec -> TimerTime.SEC_10
            else -> return
        }

        gear360Service?.messageSender?.sendChangeTimerTimer(time)
    }

    private fun onBeepVolumeSelected(buttonId: Int) {
        val volume = when (buttonId) {
            R.id.btn_beep_off -> BeepVolume.OFF
            R.id.btn_beep_low -> BeepVolume.LOW
            R.id.btn_beep_mid -> BeepVolume.MID
            R.id.btn_beep_high -> BeepVolume.HIGH
            else -> return
        }

        gear360Service?.messageSender?.sendChangeBeepVolume(volume)
    }

    private fun onPowerTimerSelected(buttonId: Int) {
        val time = when (buttonId) {
            R.id.btn_power_1_min -> AutoPowerOffTime.MIN_1
            R.id.btn_power_3_min -> AutoPowerOffTime.MIN_3
            R.id.btn_power_5_min -> AutoPowerOffTime.MIN_5
            R.id.btn_power_30_min -> AutoPowerOffTime.MIN_30
            else -> return
        }

        gear360Service?.messageSender?.sendChangePowerOffTime(time)
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