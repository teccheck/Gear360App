package io.github.teccheck.gear360app.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import io.github.teccheck.gear360app.utils.SettingsHelper

private const val PERMISSION_REQUEST_CODE = 0xACDC

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkPermissions())
            startNextActivity()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        results: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            var success = results.isNotEmpty()

            for (result in results) {
                if (result != PackageManager.PERMISSION_GRANTED)
                    success = false
            }

            if (success)
                startNextActivity()
        }
    }

    private fun startNextActivity() {
        val settings = SettingsHelper(this)
        val lastConnectedDevice = settings.getLastConnectedDeviceAddress()

        if (lastConnectedDevice != null) {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(EXTRA_MAC_ADDRESS, lastConnectedDevice)
            startActivity(intent)
        } else {
            val intent = Intent(this, ScanActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun checkPermissions(): Boolean {
        var success = true
        val requiredPermissions =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mutableListOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } else {
                mutableListOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }

        var i = requiredPermissions.lastIndex
        while (i >= 0) {
            if (ActivityCompat.checkSelfPermission(this, requiredPermissions[i])
                == PackageManager.PERMISSION_GRANTED
            ) {
                requiredPermissions.removeAt(i)
            } else {
                success = false
            }
            i--
        }

        if (!success) {
            ActivityCompat.requestPermissions(
                this, requiredPermissions.toTypedArray(), PERMISSION_REQUEST_CODE
            )
        }

        return success
    }
}