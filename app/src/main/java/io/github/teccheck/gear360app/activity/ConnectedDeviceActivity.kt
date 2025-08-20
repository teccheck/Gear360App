package io.github.teccheck.gear360app.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.teccheck.gear360app.R
import io.github.teccheck.gear360app.utils.DeviceDescription
import io.github.teccheck.gear360app.utils.ResUtils
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
            val view = layoutInflater.inflate(R.layout.list_entry_hardware, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val view = holder.itemView
            val device = devices[position]

            val name = view.findViewById<TextView>(R.id.name)
            val address = view.findViewById<TextView>(R.id.value)
            val icon = view.findViewById<ImageView>(R.id.icon)

            name.text = device.name
            address.text = device.address
            icon.setImageResource(ResUtils.getModelIcon(device.type))

            view.setOnClickListener { this@ConnectedDeviceActivity.onItemClick(position, device) }
        }

        override fun getItemCount(): Int {
            return devices.size
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}