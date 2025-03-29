package io.github.teccheck.gear360app.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.os.*
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.teccheck.gear360app.R
import io.github.teccheck.gear360app.bluetooth.BTCommandResponse
import io.github.teccheck.gear360app.bluetooth.BTDeviceDescriptionUrlMessage
import io.github.teccheck.gear360app.bluetooth.BTMessage2
import io.github.teccheck.gear360app.bluetooth.MessageHandler
import io.github.teccheck.gear360app.service.ConnectionState
import io.github.teccheck.gear360app.utils.DeviceDescription
import io.github.teccheck.gear360app.utils.ResUtils
import io.github.teccheck.gear360app.utils.SettingsHelper
import io.github.teccheck.gear360app.utils.WifiUtils
import io.github.teccheck.gear360app.widget.ConnectionDots

private const val TAG = "HomeActivity"
const val EXTRA_DEVICE_DESCRIPTION = "device_description"

class HomeActivity : BaseActivity() {
    private lateinit var connectionDots: ConnectionDots
    private lateinit var connectionDevice: ImageView
    private lateinit var connectionGear: ImageView
    private lateinit var connectButton: Button
    private lateinit var recyclerView: RecyclerView

    private var selectedDevice: DeviceDescription? = null

    private val messageListener = object : MessageHandler.MessageListener {
        override fun onMessageReceive(message: BTMessage2) {
            if (message is BTCommandResponse) {
                if (message.isSuccess() && message.resultDescription == "liveview") {
                    val ssid = gear360Service?.gear360Info?.apSSID ?: return
                    val password = gear360Service?.gear360Info?.apPassword ?: return
                    WifiUtils.connectToWifi(this@HomeActivity, ssid, password, true)
                }
            } else if (message is BTDeviceDescriptionUrlMessage) {
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
        recyclerView = findViewById(R.id.recycler)

        setDeviceConnectivityIndicator(false)

        selectedDevice = intent.getSerializableExtra(EXTRA_DEVICE_DESCRIPTION) as DeviceDescription?
        val settings = SettingsHelper(this)
        if (selectedDevice == null)
            selectedDevice = settings.getLastConnectedDevice()

        startGear360Service()

        connectButton = findViewById(R.id.btn_connect)
        connectButton.setOnClickListener {
            connect()
        }

        selectedDevice?.type?.let { connectionGear.setImageResource(ResUtils.getConnectModelIcon(it)) }

        startRecyclerView()
    }

    override fun onGearServiceConnected() {
        setDeviceConnectivityIndicator(true)
    }

    override fun onConnectionStateChanged(state: ConnectionState) {
        setGearConnectivityIndicator(state)
        setDeviceConnectivityIndicator(state != ConnectionState.INVALID)
    }

    private fun startRecyclerView() {
        val dataSet = arrayOf(
            Property(
                ResUtils.getModelIcon(selectedDevice!!.type),
                R.string.btn_camera,
                getString(R.string.btn_camera_description)
            ) {
                gear360Service?.requestLiveView()
            },
            Property(
                R.drawable.ic_baseline_settings_remote_24,
                R.string.btn_remote_control,
                getString(R.string.btn_remote_control_description),
                ActivityAction(RemoteControlActivity::class.java)
            ),
            Property(
                R.drawable.ic_baseline_settings_applications_24,
                R.string.btn_status,
                getString(R.string.btn_status_description),
                ActivityAction(StatusActivity::class.java)
            ),
            Property(
                R.drawable.ic_baseline_memory_24,
                R.string.btn_hardware,
                getString(R.string.btn_hardware_description),
                ActivityAction(HardwareInfoActivity::class.java)
            ),
            Property(
                R.drawable.baseline_chat_24,
                R.string.btn_messages,
                getString(R.string.btn_messages_description),
                ActivityAction(MessagesActivity::class.java)
            ),
            Property(
                R.drawable.ic_baseline_widgets_24,
                R.string.btn_test,
                getString(R.string.btn_test_description),
                ActivityAction(TestActivity::class.java)
            ),
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PropertiesRecyclerAdapter(dataSet)
    }

    private fun connect() {
        if (gear360Service?.connectionState?.value == ConnectionState.CONNECTED) {
            gear360Service?.disconnect()
            return
        }

        selectedDevice?.let {
            Log.d(TAG, "Connect to $it")
            gear360Service?.connect(it.address)
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

        connectButton.isEnabled = when (connectionState) {
            ConnectionState.INVALID -> false
            ConnectionState.CONNECTING -> false
            else -> true
        }

        connectButton.setText(
            when (connectionState) {
                ConnectionState.CONNECTED -> R.string.btn_disconnect
                else -> R.string.btn_connect
            }
        )
    }

    private inner class ActivityAction<T>(private val activity: Class<T>) : Action {
        override fun execute() {
            startActivity(Intent(this@HomeActivity, activity))
        }
    }
}
