package io.github.teccheck.gear360app.utils

import io.github.teccheck.gear360app.service.DeviceType
import java.io.Serializable

data class DeviceDescription(val address: String, val type: DeviceType) : Serializable
