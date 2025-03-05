package io.github.teccheck.gear360app.activity

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.teccheck.gear360app.R
import io.github.teccheck.gear360app.bluetooth.MessageLog

private const val TAG = "MessagesActivity"

class MessagesActivity : BaseActivity() {
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
        val messageLog = gear360Service?.messageLog ?: return
        val cameraIcon = if (gear360Service?.gear360Info?.isCM200() == true) {
            R.drawable.ic_connect_g360_2016
        } else {
            R.drawable.ic_connect_g360_2017
        }

        Log.d(TAG, "startRecyclerView")

        val dataSet = messageLog.messages.map {
            val icon = if (it.direction == MessageLog.SendDirection.TO_CAMERA) {
                R.drawable.ic_connect_phone
            } else {
                cameraIcon
            }

            val sender = if(it.direction == MessageLog.SendDirection.TO_CAMERA) {
                R.string.sender_phone
            } else {
                R.string.sender_camera
            }

            return@map Property(icon, sender, it.message)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PropertiesRecyclerAdapter(dataSet.toTypedArray())
    }
}