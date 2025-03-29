package io.github.teccheck.gear360app.utils

import androidx.annotation.DrawableRes
import io.github.teccheck.gear360app.R
import io.github.teccheck.gear360app.service.DeviceType

object ResUtils {
    @DrawableRes
    fun getModelIcon(type: DeviceType) = when (type) {
        DeviceType.C200 -> R.drawable.ic_gear_360_2016
        DeviceType.R210 -> R.drawable.ic_gear_360_2017
    }

    @DrawableRes
    fun getConnectModelIcon(type: DeviceType) = when (type) {
        DeviceType.C200 -> R.drawable.ic_connect_g360_2016
        DeviceType.R210 -> R.drawable.ic_connect_g360_2017
    }
}