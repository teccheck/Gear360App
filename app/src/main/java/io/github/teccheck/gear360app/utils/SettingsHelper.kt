package io.github.teccheck.gear360app.utils

import android.content.Context
import android.content.SharedPreferences
import io.github.teccheck.gear360app.service.DeviceType

private const val key_device_address = "last_connected_device"
private const val key_paired_devices = "paired_devices"
private const val key_device_type = "device_type_"

class SettingsHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("prefs", 0)

    fun getLastConnectedDevice(): DeviceDescription? {
        return getDeviceDescription(prefs.getString(key_device_address, null) ?: return null)
    }

    fun setLastConnectedDevice(deviceDescription: DeviceDescription) {
        val editor = prefs.edit()
        editor.putString(key_device_address, deviceDescription.address)
        editor.apply()
    }

    fun addPairedDevice(deviceDescription: DeviceDescription) {
        val editor = prefs.edit()
        putIntoSet(editor, key_paired_devices, deviceDescription.address)
        editor.putString(getDeviceTypeKey(deviceDescription.address), deviceDescription.type.value)
        editor.apply()
    }

    fun getPairedDevices(): List<DeviceDescription> {
        val pairedDevices = prefs.getStringSet(key_paired_devices, null) ?: return listOf()
        return pairedDevices.mapNotNull(this::getDeviceDescription).toList()
    }

    private fun getDeviceDescription(address: String): DeviceDescription? {
        val type = getDeviceType(address) ?: return null
        return DeviceDescription(address, type)
    }

    private fun getDeviceType(address: String): DeviceType? {
        return prefs.getString(getDeviceTypeKey(address), null)?.let { DeviceType.fromString(it) }
    }

    private fun getDeviceTypeKey(address: String): String {
        return key_device_type + address
    }

    private fun putIntoSet(editor: SharedPreferences.Editor, key: String, element: String) {
        val set = prefs.getStringSet(key, setOf())!!
        set.toMutableSet().add(element)
        editor.putStringSet(key, set)
    }
}