package io.github.teccheck.gear360app.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.teccheck.gear360app.R
import io.github.teccheck.gear360app.service.Gear360Status

private const val TAG = "StatusActivity"

class StatusActivity : BaseActivity() {
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
        gear360Service?.gear360Status?.observe(this, this::updateRecyclerView)
    }

    private fun updateRecyclerView(status: Gear360Status) {
        val dataSet: Array<Property> = arrayOf(
            Property(
                R.drawable.baseline_battery_std_24,
                R.string.status_battery,
                status.battery.toString()
            ),
            Property(
                R.drawable.baseline_battery_std_24,
                R.string.status_battery_state,
                status.batteryState.toString()
            ),
            Property(
                R.drawable.ic_baseline_memory_24,
                R.string.status_total_memory,
                status.totalMemory.toString()
            ),
            Property(
                R.drawable.ic_baseline_memory_24,
                R.string.status_free_memory,
                status.freeMemory.toString()
            ),
            Property(
                R.drawable.ic_baseline_memory_24,
                R.string.status_used_memory,
                status.usedMemory.toString()
            ),
            Property(
                R.drawable.ic_baseline_videocam_24,
                R.string.status_record_state,
                status.recordState?.toString() ?: "---"
            ),
            Property(
                R.drawable.ic_baseline_photo_camera_24,
                R.string.status_capture_state,
                status.captureState?.toString() ?: "---"
            ),
            Property(
                R.drawable.ic_baseline_build_24,
                R.string.status_auto_poweroff,
                status.autoPowerOff ?: "---"
            ),
            Property(
                R.drawable.ic_baseline_videocam_24,
                R.string.status_recordable_time,
                status.recordableTime.toString()
            ),
            Property(
                R.drawable.ic_baseline_photo_camera_24,
                R.string.status_capturable_count,
                status.capturableCount.toString()
            ),
        )

        (recyclerView.adapter as PropertiesRecyclerAdapter).updateDataSet(dataSet)
    }

    private fun startRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PropertiesRecyclerAdapter(arrayOf())
    }
}