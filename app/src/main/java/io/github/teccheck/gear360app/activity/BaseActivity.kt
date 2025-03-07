package io.github.teccheck.gear360app.activity

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.github.teccheck.gear360app.service.Gear360Service

abstract class BaseActivity : AppCompatActivity() {

    protected var gear360Service: Gear360Service? = null

    private val gearServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            gear360Service = (service as Gear360Service.LocalBinder).getService()
            gear360Service?.setCallback(gear360ServiceCallback)
            onGearServiceConnected()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            gear360Service = null
            onGearServiceDisconnected()
        }
    }

    private val gear360ServiceCallback = object : Gear360Service.Callback {
        override fun onSAMStarted() {
            this@BaseActivity.onSAMStarted()
        }

        override fun onDeviceConnected() {
            this@BaseActivity.onDeviceConnected()
        }

        override fun onDeviceDisconnected() {
            this@BaseActivity.onDeviceDisconnected()
        }
    }

    protected open fun onGearServiceConnected() {}
    protected open fun onGearServiceDisconnected() {}
    protected open fun onSAMStarted() {}
    protected open fun onDeviceConnected() {}
    protected open fun onDeviceDisconnected() {}

    protected fun startGear360Service(): Boolean {
        val intent = Intent(this, Gear360Service::class.java)
        return bindService(intent, gearServiceConnection, BIND_AUTO_CREATE)
    }

    protected fun setupBackButton() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            onBackPressed()

        return super.onOptionsItemSelected(item)
    }
}
