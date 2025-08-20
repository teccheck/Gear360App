package io.github.teccheck.gear360app.utils

import androidx.annotation.DrawableRes
import io.github.teccheck.gear360app.R
import io.github.teccheck.gear360app.service.BatteryState
import io.github.teccheck.gear360app.service.DeviceType

object ResUtils {
    @DrawableRes
    fun getModelIcon(type: DeviceType) = when (type) {
        DeviceType.C200 -> R.drawable.ic_gear_360_2016_baseline
        DeviceType.R210 -> R.drawable.ic_gear_360_2017_baseline
    }

    @DrawableRes
    fun getConnectModelIcon(type: DeviceType) = when (type) {
        DeviceType.C200 -> R.drawable.ic_connect_g360_2016
        DeviceType.R210 -> R.drawable.ic_connect_g360_2017
    }

    @DrawableRes
    fun getBatteryIcon(value: Int, state: BatteryState) = when (state) {
        BatteryState.NORMAL -> when (value) {
            1 -> R.drawable.battery_low
            2 -> R.drawable.battery_medium
            3 -> R.drawable.battery_high
            else -> R.drawable.battery_outline
        }

        BatteryState.CHARGE -> when (value) {
            1 -> R.drawable.battery_charging_low
            2 -> R.drawable.battery_charging_medium
            3 -> R.drawable.battery_charging_high
            else -> R.drawable.battery_charging_outline
        }

        BatteryState.ERROR_CF -> R.drawable.battery_alert_variant_outline
        BatteryState.ERROR_TEMP -> R.drawable.thermometer_alert
        else -> R.drawable.battery_unknown
    }

    @DrawableRes
    fun getStorageIcon(hasStorage: Boolean) =
        if (hasStorage) R.drawable.baseline_sd_storage_24
        else R.drawable.baseline_sd_card_alert_24
}