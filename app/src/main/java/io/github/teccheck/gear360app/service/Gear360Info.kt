package io.github.teccheck.gear360app.service

data class Gear360Info(
    val modelName: DeviceType,
    val modelVersion: String,
    val channel: Int,
    val wifiDirectMac: String,
    val apSSID: String,
    val apPassword: String,
    val boardRevision: String,
    val serialNumber: String,
    val uniqueNumber: String,
    val wifiMac: String,
    val btMac: String,
    val btFotaTestUrl: String,
    val fwType: Int
) {
    fun isCM200() = modelName == DeviceType.C200
    fun isR210() = modelName == DeviceType.R210

    fun getVersionName() = modelVersion.replaceAfter('_', "").replace("_", "")

    fun getSemanticVersion() = modelVersion.replaceBefore('_', "").replace("_", "")
}