package io.github.teccheck.gear360app.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import io.github.teccheck.gear360app.R
import io.github.teccheck.gear360app.bluetooth.BTCommandRsp
import io.github.teccheck.gear360app.bluetooth.BTDeviceDescUrlMsg
import io.github.teccheck.gear360app.bluetooth.BTMessage
import io.github.teccheck.gear360app.bluetooth.MessageHandler
import io.github.teccheck.gear360app.utils.ConnectionState
import io.github.teccheck.gear360app.utils.SettingsHelper
import io.github.teccheck.gear360app.utils.WifiUtils
import io.github.teccheck.gear360app.widget.ConnectionDots

private const val TAG = "HomeActivity"
const val EXTRA_MAC_ADDRESS = "mac_address"

class HomeActivity : BaseActivity() {

    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var connectionDots: ConnectionDots
    private lateinit var connectionDevice: ImageView
    private lateinit var connectionGear: ImageView
    private lateinit var connectButton: Button

    private val messageListener = object : MessageHandler.MessageListener {
        override fun onMessageReceive(message: BTMessage) {
            if (message is BTCommandRsp) {
                if (message.isSuccess("liveview")) {
                    val ssid = gear360Service?.gear360Info?.apSSID ?: return
                    val password = gear360Service?.gear360Info?.apPassword ?: return
                    WifiUtils.connectToWifi(this@HomeActivity, ssid, password, true)
                }
            } else if (message is BTDeviceDescUrlMsg) {
                startActivity(Intent(this@HomeActivity, ExoplayerActivity::class.java))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        connectionDevice = findViewById(R.id.connect_phone_image)
        connectionDots = findViewById(R.id.dots)
        connectionGear = findViewById(R.id.connect_camera_image)

        setDeviceConnectivityIndicator(false)

        val settings = SettingsHelper(this)
        val editor = settings.edit()

        val mac = intent.getStringExtra(EXTRA_MAC_ADDRESS)
        mac?.let {
            editor.setLastConnectedDeviceAddress(it)
            editor.save()
        }

        startGear360Service()

        connectButton = findViewById(R.id.btn_connect)
        connectButton.setOnClickListener {
            connect()
        }

        findViewById<LinearLayout>(R.id.layout_camera).setOnClickListener {
            gear360Service?.messageSender?.sendLiveViewRequest()
        }

        findViewById<LinearLayout>(R.id.layout_remote_control).setOnClickListener {
            startActivity(Intent(this, RemoteControlActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.layout_hardware).setOnClickListener {
            startActivity(Intent(this, HardwareInfoActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.layout_messages).setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.layout_test).setOnClickListener {
            startActivity(Intent(this, TestActivity::class.java))
        }
    }

    override fun onGearServiceConnected() {
        gear360Service?.messageHandler?.addMessageListener(messageListener)
        setDeviceConnectivityIndicator(true)
    }

    override fun onSAMStarted() {
        connect()
    }

    override fun onDeviceConnected() {
        mainHandler.post { setGearConnectivityIndicator(ConnectionState.CONNECTED) }
    }

    override fun onDeviceDisconnected() {
        mainHandler.post { setGearConnectivityIndicator(ConnectionState.DISCONNECTED) }
    }

    private fun connect() {
        intent.getStringExtra(EXTRA_MAC_ADDRESS)?.let {
            Log.d(TAG, "Connect to $it")
            gear360Service?.connect(it)
            setGearConnectivityIndicator(ConnectionState.CONNECTING)
        }
    }

    private fun setDeviceConnectivityIndicator(active: Boolean) {
        val colorRes = if (active) {
            R.color.connection_indicators_connected
        } else {
            R.color.connection_indicators_disconnected
        }
        val color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getColor(colorRes)
        } else {
            resources.getColor(colorRes)
        }

        connectionDevice.imageTintList = ColorStateList.valueOf(color)
    }

    private fun setGearConnectivityIndicator(connectionState: ConnectionState) {
        connectionDots.setDotState(connectionState)

        val colorRes = if (connectionState == ConnectionState.CONNECTED) {
            R.color.connection_indicators_connected
        } else {
            R.color.connection_indicators_disconnected
        }
        val color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getColor(colorRes)
        } else {
            resources.getColor(colorRes)
        }

        connectionGear.imageTintList = ColorStateList.valueOf(color)

        when (connectionState) {
            ConnectionState.DISCONNECTED -> {
                connectButton.visibility = View.VISIBLE
                connectButton.isEnabled = true
            }
            ConnectionState.CONNECTING -> {
                connectButton.visibility = View.VISIBLE
                connectButton.isEnabled = false
            }
            ConnectionState.CONNECTED -> {
                connectButton.visibility = View.GONE
                connectButton.isEnabled = false
            }
        }
    }
}
