package io.github.teccheck.gear360app.activity

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.teccheck.gear360app.R

private const val TAG = "HardwareInfoActivity"

class HardwareInfoActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hardware_info)

        setupBackButton()

        recyclerView = findViewById(R.id.recycler_view)

        startGear360Service()
    }

    override fun onGearServiceConnected() {
        startRecyclerView()
    }

    private fun startRecyclerView() {
        val info = gear360Service?.gear360Info ?: return

        Log.d(TAG, "startRecyclerView")

        val modelNameIcon =
            if (info.isCM200()) R.drawable.ic_gear_360_2016
            else R.drawable.ic_gear_360_2017

        val fwTypeName =
            if (info.fwType == 0) getString(R.string.hardware_fw_type_user)
            else getString(R.string.hardware_fw_type_retail)

        val dataSet: Array<Property> = arrayOf(
            Property(
                modelNameIcon,
                R.string.hardware_model_name,
                info.modelName.value
            ),
            Property(
                R.drawable.ic_baseline_memory_24,
                R.string.hardware_board_revision,
                info.boardRevision
            ),
            Property(
                R.drawable.ic_baseline_build_24,
                R.string.hardware_serial_number,
                info.serialNumber
            ),
            Property(
                R.drawable.ic_baseline_numbers_24,
                R.string.hardware_unique_number,
                info.uniqueNumber
            ),
            Property(
                R.drawable.ic_baseline_widgets_24,
                R.string.hardware_model_version,
                "${info.getVersionName()} (${info.getSemanticVersion()})"
            ),
            Property(
                R.drawable.ic_baseline_settings_applications_24,
                R.string.hardware_fw_type,
                "${info.fwType} ($fwTypeName)"
            ),
            Property(
                R.drawable.ic_baseline_looks_24,
                R.string.hardware_channel,
                info.channel.toString()
            ),
            Property(
                R.drawable.ic_baseline_font_download_24,
                R.string.hardware_wifi_direct_mac,
                info.wifiDirectMac
            ),
            Property(
                R.drawable.ic_baseline_network_wifi_24,
                R.string.hardware_ap_ssid,
                info.apSSID
            ),
            Property(
                R.drawable.ic_baseline_password_24,
                R.string.hardware_ap_password,
                info.apPassword
            ),
            Property(
                R.drawable.ic_baseline_font_download_24,
                R.string.hardware_wifi_mac,
                info.wifiMac
            ),
            Property(
                R.drawable.ic_baseline_bluetooth_24,
                R.string.hardware_bt_mac,
                info.btMac
            )
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PropertiesRecyclerAdapter(dataSet)
    }
}