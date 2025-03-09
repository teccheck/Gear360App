package io.github.teccheck.gear360app.bluetooth

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.github.teccheck.gear360app.service.AutoPowerOffTime
import io.github.teccheck.gear360app.service.BeepVolume
import io.github.teccheck.gear360app.service.CameraMode
import io.github.teccheck.gear360app.service.Gear360Config
import io.github.teccheck.gear360app.service.LedIndicator
import io.github.teccheck.gear360app.service.LoopingVideoTime
import io.github.teccheck.gear360app.service.TimerTime
import java.text.SimpleDateFormat
import java.util.*

// Because the messages used in bluetooth communication have a very weird format and don't follow
// any good logic, this is quite messy and complicated. I tried my best to make the code
// understandable. There's two parts to this. One is the structure of the json used to feed moshi.
// The other one is the messages in a more user friendly way to be used by this codebase.
// MessageHandler and MessageSender are used to convert between the formats so you don't have to
// worry about it.

// The following classes are used to serialize and deserialize the messages

enum class MsgId {
    @Json(name = "info")
    DEVICE_INFO,

    @Json(name = "config-info")
    CONFIG_INFO,

    @Json(name = "widget-info-req")
    WIDGET_INFO_REQ,

    @Json(name = "widget-info-rsp")
    WIDGET_INFO_RSP,

    @Json(name = "widget-info-update")
    WIDGET_INFO_UPDATE,

    @Json(name = "date-time-req")
    DATE_TIME_REQ,

    @Json(name = "date-time-rsp")
    DATE_TIME_RSP,

    @Json(name = "cmd-req")
    COMMAND_REQ,

    @Json(name = "cmd-rsp")
    COMMAND_RSP,

    @Json(name = "shot-req")
    SHOT_REQ,

    @Json(name = "shot-rst")
    SHOT_RSP,

    @Json(name = "device-desc-url")
    DEVICE_DESC_URL,

    @Json(name = "bigdata-req")
    BIGDATA_REQ,
}

@JsonClass(generateAdapter = true)
class BTMessageContainer(
    // Who decided this was necessary?
    // JSON is not supposed to be human readable (especially not in this case)!
    val title: String = "",
    val description: String = "",
    val type: String = "object",

    // This is the only really useful object here
    val properties: BTMessageProperties,
)

@JsonClass(generateAdapter = true)
class BTMessageProperties(
    val msgId: MsgId,

    // Command Request
    val action: BTMessageAction? = null,

    // Widget Info Response
    val list: BTMessageList? = null,

    // Camera Config
    val functions: BTMessageFunctions? = null,

    // Multiple things
    val result: BTMessageResult? = null,

    // Remote Shot Request
    val items: BTMessageProperty<String>? = null,

    // Datetime Response
    val date: BTMessageProperty<String>? = null,
    val time: BTMessageProperty<String>? = null,
    val region: BTMessageProperty<String>? = null,
    val summer: BTMessageProperty<String>? = null,

    // Phone device info
    @Json(name = "wifi-direct") val wifiDirect: BTMessageWifiDirect? = null,
    @Json(name = "wifi-mac-address") val wifiMacAddress: BTMessageProperty<String>? = null,
    @Json(name = "device-name") val deviceName: BTMessageProperty<String>? = null,
    @Json(name = "app-version") val appVersion: BTMessageProperty<String>? = null,
    @Json(name = "op-mode") val opMode: BTMessageProperty<String>? = null,

    // Camera device info
    @Json(name = "model-name") val modelName: BTMessageProperty<String>? = null,
    @Json(name = "model-version") val modelVersion: BTMessageProperty<String>? = null,
    val channel: BTMessageProperty<Int>? = null,
    @Json(name = "wifi-direct-mac") val wifiDirectMac: BTMessageProperty<String>? = null,
    @Json(name = "softap-ssid") val softApSsid: BTMessageProperty<String>? = null,
    @Json(name = "softap-psword") val softApPassword: BTMessageProperty<String>? = null,
    @Json(name = "board-revision") val boardRevision: BTMessageProperty<String>? = null,
    @Json(name = "serial-number") val serialNumber: BTMessageProperty<String>? = null,
    @Json(name = "unique-number") val uniqueNumber: BTMessageProperty<String>? = null,
    @Json(name = "wifi-mac") val wifiMac: BTMessageProperty<String>? = null,
    @Json(name = "bt-mac") val btMac: BTMessageProperty<String>? = null,
    @Json(name = "bt-fota-test-url") val btFotaTestUrl: BTMessageProperty<String>? = null,
    @Json(name = "fw-type") val firmwareType: BTMessageProperty<Int>? = null,

    // Remote Shot Result
    @Json(name = "r-code") val resultCode: BTMessageProperty<Int>? = null,
    @Json(name = "extension-info") val extensionInfo: BTMessageExtensionInfo? = null,

    // Device description
    val url: BTMessageProperty<String>? = null
)

@JsonClass(generateAdapter = true)
class BTMessageWifiDirect(
    val enum: String,
    @Json(name = "ch-negotiation-wa") val negotiation: BTMessageWifiDirectNegotiation,
)

@JsonClass(generateAdapter = true)
class BTMessageWifiDirectNegotiation(
    val description: String
)

@JsonClass(generateAdapter = true)
class BTMessageExtensionInfo(
    @Json(name = "recordable-time") val recordableTime: BTMessageProperty<Int>?,
    @Json(name = "capturable-count") val capturableCount: BTMessageProperty<Int>?,
)

@JsonClass(generateAdapter = true)
class BTMessageResult(
    val enum: String,
    val description: String,
)

@JsonClass(generateAdapter = true)
class BTMessageProperty<T>(
    val type: String,
    val description: T,
)

@JsonClass(generateAdapter = true)
class BTMessageList(
    val type: String,
    val items: BTMessageListItems,
)

@JsonClass(generateAdapter = true)
class BTMessageListItems(
    val result: BTMessageResult?,
    val battery: BTMessageProperty<Int>?,
    @Json(name = "battery-state") val batteryState: BTMessageProperty<String>?,
    @Json(name = "total-memory") val totalMemory: BTMessageProperty<Int>?,
    @Json(name = "used-memory") val usedMemory: BTMessageProperty<Int>?,
    @Json(name = "free-memory") val freeMemory: BTMessageProperty<Int>?,
    @Json(name = "record-state") val recordState: BTMessageProperty<String>?,
    @Json(name = "capture-state") val captureState: BTMessageProperty<String>?,
    @Json(name = "auto-poweroff") val autoPowerOff: BTMessageProperty<String>?,
    @Json(name = "recordable-time") val recordableTime: BTMessageProperty<Int>?,
    @Json(name = "capturable-count") val capturableCount: BTMessageProperty<Int>?,
)

@JsonClass(generateAdapter = true)
class BTMessageFunctions(
    val type: String,
    val count: Int,
    val items: BTMessageFunctionsItems,
)

@JsonClass(generateAdapter = true)
class BTMessageFunctionsItems(
    @Json(name = "1") val beep: BTMessageFunctionsItem,
    @Json(name = "2") val led: BTMessageFunctionsItem,
    @Json(name = "3") val autoPowerOff: BTMessageFunctionsItem,
    @Json(name = "4") val videoOut: BTMessageFunctionsItem,
    @Json(name = "5") val format: BTMessageFunctionsItem,
    @Json(name = "6") val reset: BTMessageFunctionsItem,
    @Json(name = "7") val mode: BTMessageFunctionsItem,
    @Json(name = "8") val timer: BTMessageFunctionsItem,
    @Json(name = "9") val loopingRecordTime: BTMessageFunctionsItem,
)

@JsonClass(generateAdapter = true)
class BTMessageFunctionsItem(
    @Json(name = "sub-title") val subTitle: String,
    val count: Int,
    val value: String,
    val default: String,
)

@JsonClass(generateAdapter = true)
class BTMessageAction(
    val enum: String,
    val description: String,
    val items: BTMessageConfigActionItems? = null,
)

@JsonClass(generateAdapter = true)
class BTMessageConfigActionItems(
    @Json(name = "Mode") val modeSend: BTMessageConfigActionItem? = null,
    @Json(name = "mode") val modeReceive: BTMessageConfigActionItem? = null,
    @Json(name = "timer") val timer: BTMessageConfigActionItem? = null,
    @Json(name = "Beep") val beep: BTMessageConfigActionItem? = null,
    @Json(name = "Led indicator") val ledIndicator: BTMessageConfigActionItem? = null,
    @Json(name = "Auto Power Off") val autoPowerOff: BTMessageConfigActionItem? = null,
    @Json(name = "Looping Video Recording Time") val loopingVideoTime: BTMessageConfigActionItem? = null,
)

@JsonClass(generateAdapter = true)
class BTMessageConfigActionItem(val description: String)

// The following classes are used to actually handle the messages in code

/**
 * Base class for all bluetooth messages.
 */
open class BTMessage2

/**
 * This request comes from the camera to request current date and time information
 * The camera expects a BTDateTimeResponse in return.
 * @see BTDateTimeResponse
 */
class BTDateTimeRequest() : BTMessage2()

/**
 * This response comes from the phone with current date and time
 * information to sync the clock of the camera.
 * @see BTDateTimeRequest
 * @param date the current date
 */
class BTDateTimeResponse(private val date: Date) : BTMessage2() {
    fun asBtMessageContainer(): BTMessageContainer {
        val dateFormat = SimpleDateFormat("yyy/MM/dd")
        val timeFormat = SimpleDateFormat("HH:mm:ss")
        val zoneFormat = SimpleDateFormat("Z")

        val zone = zoneFormat.format(date)
        val region = zone.substring(0, 3) + ":" + zone.substring(3, 5)

        val isSummerTime = TimeZone.getDefault().useDaylightTime()

        return BTMessageContainer(
            properties = BTMessageProperties(
                msgId = MsgId.DATE_TIME_RSP,
                date = BTMessageProperty("string", dateFormat.format(date)),
                time = BTMessageProperty("string", timeFormat.format(date)),
                region = BTMessageProperty("string", region),
                summer = BTMessageProperty("string", isSummerTime.toString()),
            )
        )
    }
}

/**
 * Sent by both camera and phone. The camera sends this first and the phone responds with the same
 * message. Then the camera sends BTWidgetInfoResponse.
 * @see BTWidgetInfoResponse
 */
class BTWidgetInfoRequest() : BTMessage2() {
    fun asBtMessageContainer(): BTMessageContainer {
        return BTMessageContainer(properties = BTMessageProperties(MsgId.WIDGET_INFO_REQ))
    }
}

/**
 * Sent by the camera. Contains current status information.
 * @param battery current battery level (0-3)
 * @param batteryState current battery state (normal, charging, ...)
 * @param totalMemory the total memory available to the camera
 * @param usedMemory the used memory
 * @param freeMemory the free memory
 * @param recordState whether the camera is recording or not
 * @param captureState whether the camera is capturing or not
 * @param autoPowerOff how long before the camera shuts of automatically
 * @param recordableTime how much can be recorded to the sdcard
 * @param capturableCount how many images can be captured on to the sdcard
 */
class BTWidgetInfoResponse(
    val battery: Int,
    val batteryState: String,
    val totalMemory: Int,
    val usedMemory: Int,
    val freeMemory: Int,
    val recordState: String,
    val captureState: String,
    val autoPowerOff: String,
    val recordableTime: Int,
    val capturableCount: Int,
) : BTMessage2() {
    companion object {
        fun fromBTMessageContainer(msg: BTMessageContainer): BTWidgetInfoResponse? {
            if (msg.properties.msgId != MsgId.WIDGET_INFO_RSP) return null
            val list = msg.properties.list ?: return null

            val battery = list.items.battery?.description ?: return null
            val batteryState = list.items.batteryState?.description ?: return null
            val totalMemory = list.items.totalMemory?.description ?: return null
            val usedMemory = list.items.usedMemory?.description ?: return null
            val freeMemory = list.items.freeMemory?.description ?: return null
            val recordState = list.items.recordState?.description ?: return null
            val captureState = list.items.captureState?.description ?: return null
            val autoPowerOff = list.items.autoPowerOff?.description ?: return null
            val recordableTime = list.items.recordableTime?.description ?: return null
            val capturableCount = list.items.capturableCount?.description ?: return null

            return BTWidgetInfoResponse(
                battery,
                batteryState,
                totalMemory,
                usedMemory,
                freeMemory,
                recordState,
                captureState,
                autoPowerOff,
                recordableTime,
                capturableCount
            )
        }
    }
}

/**
 * Is sent by the phone to inform the camera about some more or less relevant information.
 * @param wifiDirect whether to use wifi direct
 * @param wifiMacAddress the mac address of the wifi adapter of this phone. Seems to not be relevant
 * @param deviceName the name of the phone. Can be whatever
 * @param appVersion the version of the app. Can be whatever
 * @param retailMode not sure what retail mode is. Keep it off to be safe
 */
class BTPhoneInfoMessage(
    val wifiDirect: Boolean,
    val wifiMacAddress: String,
    val deviceName: String,
    val appVersion: String,
    val retailMode: Boolean
) : BTMessage2() {
    fun asBtMessageContainer(): BTMessageContainer {
        return BTMessageContainer(
            // Title seems to be required for whatever reason
            title = "Phone Device information Message",
            //description = "Message structure in JSON for Phone Device information",
            properties = BTMessageProperties(
                MsgId.DEVICE_INFO,
                wifiDirect = BTMessageWifiDirect(
                    wifiDirect.toString(), BTMessageWifiDirectNegotiation("5G-GO")
                ),
                wifiMacAddress = BTMessageProperty("string", wifiMacAddress),
                deviceName = BTMessageProperty("string", deviceName),
                appVersion = BTMessageProperty("string", appVersion),
                opMode = BTMessageProperty("string", if (retailMode) "retail" else "user")
            )
        )
    }
}

/**
 * Is sent by the camera to inform the phone about some basic camera information.
 * @param modelName the camera model name (SM-C200 for 2016 model, SM-R210 for 2017 model)
 * @param modelVersion the firmware version of the camera
 * @param channel not quite sure what this is
 * @param wifiDirectMac mac address for wifi direct
 * @param softApSsid SSID for the soft access point
 * @param softApPassword password for the soft access point
 * @param boardRevision the hardware revision of the camera main-board
 * @param serialNumber the camera's serial number
 * @param uniqueNumber the camera's unique number
 * @param wifiMac the camera's wifi mac address
 * @param bluetoothMac the camera's bluetooth mac address
 * @param btFotaTestUrl not quite sure what this is
 * @param firmwareType the type of firmware on the camera (only ever encountered user)
 */
class BTCameraInfoMessage(
    val modelName: String,
    val modelVersion: String,
    val channel: Int,
    val wifiDirectMac: String,
    val softApSsid: String,
    val softApPassword: String,
    val boardRevision: String,
    val serialNumber: String,
    val uniqueNumber: String,
    val wifiMac: String,
    val bluetoothMac: String,
    val btFotaTestUrl: String,
    val firmwareType: Int,
) : BTMessage2() {
    companion object {
        fun fromBTMessageContainer(msg: BTMessageContainer): BTCameraInfoMessage? {
            if (msg.properties.msgId != MsgId.DEVICE_INFO) return null

            val modelName = msg.properties.modelName?.description ?: return null
            val modelVersion = msg.properties.modelVersion?.description ?: return null
            val channel = msg.properties.channel?.description ?: return null
            val wifiDirectMac = msg.properties.wifiDirectMac?.description ?: return null
            val softApSsid = msg.properties.softApSsid?.description ?: return null
            val softApPassword = msg.properties.softApPassword?.description ?: return null
            val boardRevision = msg.properties.boardRevision?.description ?: return null
            val serialNumber = msg.properties.serialNumber?.description ?: return null
            val uniqueNumber = msg.properties.uniqueNumber?.description ?: return null
            val wifiMac = msg.properties.wifiMac?.description ?: return null
            val bluetoothMac = msg.properties.btMac?.description ?: return null
            val btFotaTestUrl = msg.properties.btFotaTestUrl?.description ?: return null
            val firmwareType = msg.properties.firmwareType?.description ?: return null

            return BTCameraInfoMessage(
                modelName,
                modelVersion,
                channel,
                wifiDirectMac,
                softApSsid,
                softApPassword,
                boardRevision,
                serialNumber,
                uniqueNumber,
                wifiMac,
                bluetoothMac,
                btFotaTestUrl,
                firmwareType
            )
        }
    }
}

class BTCameraConfigMessage(
    val beep: String,
    val ledIndicator: String,
    val autoPowerOff: String,
    val videoOut: String,
    val format: String,
    val reset: String,
    val mode: String,
    val timer: String,
    val loopingVideoTime: String,
) : BTMessage2() {
    companion object {
        fun fromBTMessageContainer(msg: BTMessageContainer): BTCameraConfigMessage? {
            if (msg.properties.msgId != MsgId.CONFIG_INFO) return null

            val items = msg.properties.functions?.items ?: return null

            return BTCameraConfigMessage(
                items.beep.default,
                items.led.default,
                items.autoPowerOff.default,
                items.videoOut.default,
                items.format.default,
                items.reset.default,
                items.mode.default,
                items.timer.default,
                items.loopingRecordTime.default
            )
        }
    }
}

class BTRemoteShotRequest(val mode: String) : BTMessage2() {
    fun asBtMessageContainer(): BTMessageContainer {
        return BTMessageContainer(
            properties = BTMessageProperties(
                MsgId.SHOT_REQ, items = BTMessageProperty("string", mode)
            )
        )
    }
}

class BTRemoteShotResponse(
    val result: String,
    val resultCode: Int,
    val recordableTime: Int,
    val capturableCount: Int,
) : BTMessage2() {
    companion object {
        fun fromBTMessageContainer(msg: BTMessageContainer): BTRemoteShotResponse? {
            if (msg.properties.msgId != MsgId.SHOT_RSP) return null

            val result = msg.properties.result?.enum ?: return null
            val responseCode = msg.properties.resultCode?.description ?: return null
            val recordableTime =
                msg.properties.extensionInfo?.recordableTime?.description ?: return null
            val capturableCount =
                msg.properties.extensionInfo.capturableCount?.description ?: return null

            return BTRemoteShotResponse(
                result, responseCode, recordableTime, capturableCount
            )
        }
    }
}

class BTCommandRequest(
    val action: BTCommandAction
) : BTMessage2() {
    fun asBtMessageContainer(): BTMessageContainer {
        val items = if (action is BTCommandActionConfig) {
            BTMessageConfigActionItems(
                modeSend = action.config.mode?.value?.let { BTMessageConfigActionItem(it) },
                timer = action.config.timer?.value?.let { BTMessageConfigActionItem(it) },
                beep = action.config.beep?.value?.let { BTMessageConfigActionItem(it) },
                ledIndicator = action.config.led?.value?.let { BTMessageConfigActionItem(it) },
                autoPowerOff = action.config.autoPowerOffTime?.value?.let {
                    BTMessageConfigActionItem(
                        it
                    )
                },
                loopingVideoTime = action.config.loopingVideoTime?.value?.let {
                    BTMessageConfigActionItem(
                        it
                    )
                },
            )
        } else {
            null
        }

        return BTMessageContainer(
            properties = BTMessageProperties(
                MsgId.COMMAND_REQ, action = BTMessageAction(action.enum, action.description, items)
            )
        )
    }

    companion object {
        fun fromBTMessageContainer(msg: BTMessageContainer): BTCommandRequest? {
            if (msg.properties.msgId != MsgId.COMMAND_REQ) return null
            val items = msg.properties.action?.items

            val action = when (msg.properties.action?.enum) {
                "liveview" -> BTCommandActionLiveView()
                "set" -> BTCommandActionConfig(
                    Gear360Config(
                        mode = items?.modeReceive?.description?.let { CameraMode.fromString(it) },
                        timer = items?.timer?.description?.let { TimerTime.fromString(it) },
                        beep = items?.beep?.description?.let { BeepVolume.fromString(it) },
                        led = items?.ledIndicator?.description?.let { LedIndicator.fromString(it) },
                        autoPowerOffTime = items?.autoPowerOff?.description?.let {
                            AutoPowerOffTime.fromString(it)
                        },
                        loopingVideoTime = items?.loopingVideoTime?.description?.let {
                            LoopingVideoTime.fromString(it)
                        },
                    )
                )

                else -> null
            }

            return BTCommandRequest(action ?: return null)
        }
    }
}

class BTCommandResponse(
    val resultEnum: String,
    val resultDescription: String,
    val resultCode: Int,
) : BTMessage2() {
    fun asBtMessageContainer(): BTMessageContainer {
        return BTMessageContainer(
            properties = BTMessageProperties(
                MsgId.COMMAND_RSP,
                result = BTMessageResult(resultEnum, resultDescription),
                resultCode = BTMessageProperty("number", resultCode),
            )
        )
    }

    fun isSuccess() = resultCode == 100

    companion object {
        fun fromBTMessageContainer(msg: BTMessageContainer): BTCommandResponse? {
            if (msg.properties.msgId != MsgId.COMMAND_RSP) return null
            return BTCommandResponse(
                msg.properties.result?.enum ?: return null,
                msg.properties.result.description,
                msg.properties.resultCode?.description ?: return null,
            )
        }
    }
}

abstract class BTCommandAction(
    val enum: String = "execute", val description: String
)

class BTCommandActionConfig(val config: Gear360Config) : BTCommandAction(description = "config")

class BTCommandActionLiveView() : BTCommandAction(description = "liveview")

class BTDeviceDescriptionUrlMessage(val url: String) : BTMessage2() {
    companion object {
        fun fromBTMessageContainer(msg: BTMessageContainer): BTDeviceDescriptionUrlMessage? {
            return BTDeviceDescriptionUrlMessage(msg.properties.url?.description ?: return null)
        }
    }
}
