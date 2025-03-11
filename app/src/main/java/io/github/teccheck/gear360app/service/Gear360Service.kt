package io.github.teccheck.gear360app.service

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.samsung.android.sdk.accessory.SAAgentV2
import com.samsung.android.sdk.accessorymanager.SamAccessoryManager
import com.samsung.android.sdk.accessorymanager.SamDevice
import io.github.teccheck.gear360app.bluetooth.BTCameraConfigMessage
import io.github.teccheck.gear360app.bluetooth.BTCameraInfoMessage
import io.github.teccheck.gear360app.bluetooth.BTCommandActionConfig
import io.github.teccheck.gear360app.bluetooth.BTCommandRequest
import io.github.teccheck.gear360app.bluetooth.BTDateTimeRequest
import io.github.teccheck.gear360app.bluetooth.BTMProviderService
import io.github.teccheck.gear360app.bluetooth.BTMessage2
import io.github.teccheck.gear360app.bluetooth.BTRemoteShotResponse
import io.github.teccheck.gear360app.bluetooth.BTWidgetInfoRequest
import io.github.teccheck.gear360app.bluetooth.BTWidgetInfoResponse
import io.github.teccheck.gear360app.bluetooth.MessageHandler
import io.github.teccheck.gear360app.bluetooth.MessageLog
import io.github.teccheck.gear360app.bluetooth.MessageSender
import io.github.teccheck.gear360app.utils.WifiUtils

private const val TAG = "Gear360Service"
private const val SA_TRANSPORT_TYPE = SamAccessoryManager.TRANSPORT_BT

// Tasks this should do
// - receive commands to connect/disconnect/pair a camera
// - report connections status
// - get static camera info on connect
// - periodically get camera status
// Optional things this should do
// - browse/download media
// - handle live viewing

// Messages to be exchanged on connection
// CAMERA: Datetime request
// PHONE:  Datetime response
// CAMERA: Widget info request (not sure why this is sent by both devices)
// PHONE:  Widget info request
// CAMERA: Widget info response
// PHONE:  Phone device info
// CAMERA: Camera device info
// CAMERA: Camera config info
// CAMERA: GSIM (Whatever that is)

// APIs this should provide
// LiveData for connection status
// LiveData for camera status and config
// Methods to connect/disconnect
// Methods to change camera config
// Methods to take photos, start/stop recordings
// Method to start live view
// Whatever the Gallery requires

class Gear360Service : Service() {
    private val binder = LocalBinder()

    private val handler = Handler(Looper.getMainLooper())

    private var connectedDeviceAddress: String? = null
    private var callback: Callback? = null

    private var samAccessoryManager: SamAccessoryManager? = null
    private val samListener = object : SamAccessoryManager.AccessoryEventListener {
        override fun onAccessoryConnected(device: SamDevice) {
            Log.d(TAG, "onAccessoryConnected $device")
            setupBTMProviderService()
        }

        override fun onAccessoryDisconnected(device: SamDevice, reason: Int) {
            Log.d(TAG, "onAccessoryDisconnected $device, $reason")
        }

        override fun onError(device: SamDevice?, reason: Int) {
            Log.d(TAG, "samListener onError $device, $reason")
            if (reason == SamAccessoryManager.ERROR_ACCESSORY_ALREADY_CONNECTED) {
                Log.d(TAG, "Already connected")
                setupBTMProviderService()
            } else {
                callback?.onDeviceDisconnected()
            }
        }

        override fun onAccountLoggedIn(device: SamDevice) {}
        override fun onAccountLoggedOut(device: SamDevice) {}
    }

    private var btmProviderService: BTMProviderService? = null
    private val btmStatusCallback = object : BTMProviderService.StatusCallback {
        override fun onConnectDevice(name: String?, peer: String?, product: String?) {
            Log.d(TAG, "onConnectDevice $name, $peer, $product")
            handler.postDelayed({
                //val macAddress = WifiUtils.getMacAddress(applicationContext)
                //messageSender.sendPhoneInfo(macAddress)
            }, 2000)
            callback?.onDeviceConnected()
        }

        override fun onError(result: Int) {
            Log.d(TAG, "btmStatusCallback onError $result")
            callback?.onDeviceDisconnected()
        }

        override fun onReceive(channelId: Int, data: ByteArray?) {
            Log.d(TAG, "onReceive $channelId, $data")
            data?.let { messageLog.messageReceived(it.toString(Charsets.UTF_8)) }
            messageHandler.onReceive(channelId, data)
        }

        override fun onServiceDisconnection() {
            Log.d(TAG, "onServiceDisconnection")
            btmProviderService?.closeConnection()
            callback?.onDeviceDisconnected()
        }
    }

    private val _gear360Config = MutableLiveData<Gear360Config>()
    val gear360Config: LiveData<Gear360Config> = _gear360Config

    private val _gear360StatusLive = MutableLiveData<Gear360Status>()
    val gear360StatusLive: LiveData<Gear360Status> = _gear360StatusLive

    val messageLog = MessageLog()
    val messageHandler = MessageHandler()
    val messageSender = MessageSender { channelId, data ->
        btmProviderService?.send(channelId, data)
        messageLog.messageSent(data.toString(Charsets.UTF_8))
    }

    var gear360Info: Gear360Info? = null

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        messageHandler.addMessageListener(this::onMessage)

        // SamAccessoryManager needs to be initialised on another thread for whatever reason
        val handlerThread = HandlerThread("$TAG SAThread")
        handlerThread.start()

        if (handlerThread.looper == null)
            return

        val handler = Handler(handlerThread.looper)
        handler.post {
            samAccessoryManager = SamAccessoryManager.getInstance(applicationContext, samListener)
            Handler(Looper.getMainLooper()).post { callback?.onSAMStarted() }
            handlerThread.quitSafely()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()

        messageHandler.removeMessageListener(this::onMessage)

        disconnect()
        samAccessoryManager?.release()
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun connect(address: String) {
        samAccessoryManager?.let {
            it.connect(address, SA_TRANSPORT_TYPE)
            connectedDeviceAddress = address
        }
    }

    fun disconnect(address: String? = connectedDeviceAddress) {
        btmProviderService?.closeConnection()
        btmProviderService?.releaseAgent()
        address?.let { samAccessoryManager?.disconnect(it, SA_TRANSPORT_TYPE) }
    }

    private fun setupBTMProviderService() {
        Log.d(TAG, "setupProviderService")
        val requestAgentCallback = object : SAAgentV2.RequestAgentCallback {
            override fun onAgentAvailable(agent: SAAgentV2) {
                Log.d(TAG, "Agent available: $agent")
                btmProviderService = agent as BTMProviderService
                btmProviderService?.setup(btmStatusCallback)
                btmProviderService?.findSaPeers()
            }

            override fun onError(errorCode: Int, message: String) {
                Log.d(TAG, "requestAgentCallback onError $errorCode, $message")
            }
        }

        SAAgentV2.requestAgent(
            applicationContext,
            BTMProviderService::class.java.name,
            requestAgentCallback
        )
    }

    private fun onMessage(message: BTMessage2) {
        when (message) {
            is BTDateTimeRequest -> {
                messageSender.sendDateTimeResponse()
            }

            is BTCameraConfigMessage -> {
                updateGear360Config(
                    Gear360Config(
                        mode = CameraMode.fromString(message.mode),
                        timer = TimerTime.fromString(message.timer),
                        beep = BeepVolume.fromString(message.beep),
                        led = LedIndicator.fromString(message.ledIndicator),
                        autoPowerOffTime = AutoPowerOffTime.fromString(message.autoPowerOff),
                        loopingVideoTime = LoopingVideoTime.fromString(message.loopingVideoTime),
                    )
                )
            }

            is BTCameraInfoMessage -> {
                gear360Info = Gear360Info(
                    message.modelName,
                    message.modelVersion,
                    message.channel,
                    message.wifiDirectMac,
                    message.softApSsid,
                    message.softApPassword,
                    message.boardRevision,
                    message.serialNumber,
                    message.uniqueNumber,
                    message.wifiMac,
                    message.bluetoothMac,
                    message.btFotaTestUrl,
                    message.firmwareType
                )

                Log.d(
                    TAG,
                    "Version: ${gear360Info?.getSemanticVersion()} -- ${gear360Info?.getVersionName()}"
                )
            }

            is BTWidgetInfoRequest -> {
                messageSender.sendWidgetInfoRequest()
            }

            is BTWidgetInfoResponse -> {
                updateGear360Status(
                    Gear360Status(
                        message.battery,
                        message.batteryState,
                        message.totalMemory,
                        message.usedMemory,
                        message.freeMemory,
                        message.recordState,
                        message.captureState,
                        message.autoPowerOff,
                        message.recordableTime,
                        message.capturableCount
                    )
                )

                val macAddress = WifiUtils.getMacAddress(applicationContext)
                messageSender.sendPhoneInfo(macAddress)
            }

            is BTRemoteShotResponse -> {
                updateGear360Status(
                    Gear360Status(
                        capturableCount = message.capturableCount,
                        recordableTime = message.recordableTime
                    )
                )
            }

            is BTCommandRequest -> {
                if (message.action is BTCommandActionConfig) {
                    updateGear360Config(message.action.config)
                }
            }
        }
    }

    private fun updateGear360Config(config: Gear360Config) {
        Log.d(TAG, "Update config: $config")
        val old = gear360Config.value ?: Gear360Config()
        _gear360Config.postValue(old.merge(config))
    }

    private fun updateGear360Status(status: Gear360Status) {
        Log.d(TAG, "Update status: $status")
        val old = gear360StatusLive.value ?: Gear360Status()
        _gear360StatusLive.postValue(old.merge(status))
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        Log.d(TAG, "onRebind")
        super.onRebind(intent)
    }

    inner class LocalBinder : Binder() {
        fun getService(): Gear360Service {
            return this@Gear360Service
        }
    }

    interface Callback {
        fun onSAMStarted()

        fun onDeviceConnected()

        fun onDeviceDisconnected()
    }
}