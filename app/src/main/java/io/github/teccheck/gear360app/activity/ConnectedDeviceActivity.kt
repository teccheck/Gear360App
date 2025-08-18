package io.github.teccheck.gear360app.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import io.github.teccheck.gear360app.R
import io.github.teccheck.gear360app.utils.DeviceDescription
import io.github.teccheck.gear360app.utils.SettingsHelper

class ConnectedDeviceActivity : BaseActivity() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connected_device)

        val settings = SettingsHelper(this)
        val devices = settings.getPairedDevices()
        Log.d("Con", "Devices: $devices")

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = BtDeviceAdapter(devices)

        startGear360Service()
    }

    private fun onItemClick(index: Int, device: DeviceDescription) {
        gear360Service?.connect(device)
        finish()
    }

    inner class BtDeviceAdapter(val devices: List<DeviceDescription>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = layoutInflater.inflate(R.layout.list_entry_device, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val view = holder.itemView
            val device = devices[position]

            val chip = view.findViewById<Chip>(R.id.chip)
            chip.setOnClickListener { this@ConnectedDeviceActivity.onItemClick(position, device) }
            chip.text = device.name
        }

        override fun getItemCount(): Int {
            return devices.size
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}