package io.github.teccheck.gear360app.activity

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import io.github.teccheck.gear360app.R
import io.github.teccheck.gear360app.service.ConnectionState
import io.github.teccheck.gear360app.utils.DeviceDescription
import io.github.teccheck.gear360app.utils.SettingsHelper

private const val TAG = "ScanActivity"

class ScanActivity : BaseActivity() {
    private lateinit var recyclerView: RecyclerView

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var scanning = false

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (!result.device.name.isNullOrEmpty()) {
                (recyclerView.adapter as BtDeviceAdapter).addDevice(result.device)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = BtDeviceAdapter()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val bluetoothManager = getSystemService(BluetoothManager::class.java)
            this.bluetoothAdapter = bluetoothManager.adapter
        } else {
            this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        }
        this.bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        startGear360Service()
    }

    override fun onResume() {
        super.onResume()
        startDeviceDiscovery()
    }

    override fun onPause() {
        stopDeviceDiscovery()
        super.onPause()
    }

    override fun onConnectionStateChanged(state: ConnectionState) {
        Log.d(TAG, "Connection state: $state")
        if (state == ConnectionState.CONNECTED) {
            val deviceDescription = DeviceDescription(
                gear360Service?.gear360Info?.btMac ?: return,
                gear360Service?.gear360Info?.modelName ?: return
            )

            val settings = SettingsHelper(this)
            settings.addPairedDevice(deviceDescription)
            settings.setLastConnectedDevice(deviceDescription)

            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra(EXTRA_DEVICE_DESCRIPTION, deviceDescription)
            startActivity(intent)
        }
    }

    private fun startDeviceDiscovery() {
        scanning = true
        bluetoothLeScanner.startScan(leScanCallback)
    }

    private fun stopDeviceDiscovery() {
        scanning = false
        bluetoothLeScanner.stopScan(leScanCallback)
    }

    private fun onItemClick(index: Int, bluetoothDevice: BluetoothDevice) {
        gear360Service?.connect(bluetoothDevice.address)
    }

    inner class BtDeviceAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val devices = mutableListOf<BluetoothDevice>()

        fun addDevice(device: BluetoothDevice) {
            if (devices.contains(device))
                return

            devices.add(device)
            notifyItemInserted(devices.size - 1)
        }

        fun clear() {
            val lastIndex = devices.lastIndex
            notifyItemRangeRemoved(0, lastIndex)
            devices.clear()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = layoutInflater.inflate(R.layout.list_entry_device, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val view = holder.itemView
            val device = devices[position]

            val chip = view.findViewById<Chip>(R.id.chip)
            chip.setOnClickListener { this@ScanActivity.onItemClick(position, device) }
            chip.text = device.name
        }

        override fun getItemCount(): Int {
            return devices.size
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}