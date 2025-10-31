package com.ffalcon.thirdeye.demo.ui.activity.api

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ffalcon.mercury.android.sdk.api.MobileState
import com.ffalcon.thirdeye.demo.databinding.ActivityApiBinding
import com.ffalcon.mercury.android.sdk.touch.TempleAction
import com.ffalcon.mercury.android.sdk.ui.activity.BaseMirrorActivity
import com.ffalcon.mercury.android.sdk.util.DeviceUtil
import com.ffalcon.mercury.android.sdk.util.FLogger
import com.ffalconxr.mercury.ipc.Launcher
import com.ffalconxr.mercury.ipc.Launcher.OnResponseListener
import com.ffalconxr.mercury.ipc.helpers.GPSIPCHelper
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class APIActivity : BaseMirrorActivity<ActivityApiBinding>() {
    private var mLauncher: Launcher? = null

    // Location permission launcher
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                // Fine location access granted, initialize GPS
                FLogger.i("Location permission granted")
                initGPS()
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                // Coarse location access granted, initialize GPS
                FLogger.i("Coarse location permission granted")
                initGPS()
            }
            else -> {
                // No location access granted
                FLogger.w("Location permission denied")
                mBindingPair.updateView {
                    tvTvLocationInfo.text = "需要位置权限"
                }
            }
        }
    }

    private val response =
        OnResponseListener { response ->
            if (response?.getData() == null) return@OnResponseListener
            try {
                val jo = JSONObject(response.getData())
                if (jo.has("mLatitude") && jo.has("mLongitude") && jo.has("mAltitude")) { //GPS数据
                    val mProvider = jo.getString("mProvider")
                    val mTime = jo.getLong("mTime")
                    val mElapsedRealtimeNanos = jo.getLong("mElapsedRealtimeNanos")
                    val mLatitude = jo.getDouble("mLatitude")
                    val mLongitude = jo.getDouble("mLongitude")
                    val mAltitude = jo.getDouble("mAltitude")
                    val mSpeed = jo.getDouble("mSpeed")
                    val mBearing = jo.getDouble("mBearing")
                    val mHorizontalAccuracyMeters = jo.getDouble("mHorizontalAccuracyMeters")
                    val mVerticalAccuracyMeters = jo.getDouble("mVerticalAccuracyMeters")
                    val mSpeedAccuracyMetersPerSecond =
                        jo.getDouble("mSpeedAccuracyMetersPerSecond")
                    val mBearingAccuracyDegrees = jo.getDouble("mBearingAccuracyDegrees")

                    runOnUiThread {
                        mBindingPair.updateView {
                            // Display location with altitude
                            val locationText = buildString {
                                append("纬度: %.6f\n".format(mLatitude))
                                append("经度: %.6f\n".format(mLongitude))
                                append("海拔: %.1f m".format(mAltitude))
                                if (mVerticalAccuracyMeters > 0) {
                                    append(" (±%.1f m)".format(mVerticalAccuracyMeters))
                                }
                            }
                            tvTvLocationInfo.text = locationText
                        }
                    }
                    FLogger.i(

                        ("======  mProvider:" + mProvider + "  mTime:" + mTime + "  mElapsedRealtimeNanos:" + mElapsedRealtimeNanos
                                + "  mLatitude:" + mLatitude + "  mLongitude:" + mLongitude + "  mAltitude:" + mAltitude + "  mSpeed:" + mSpeed
                                + "  mBearing:" + mBearing + "  mHorizontalAccuracyMeters:" + mHorizontalAccuracyMeters + "  mVerticalAccuracyMeters:" + mVerticalAccuracyMeters
                                + "  mSpeedAccuracyMetersPerSecond:" + mSpeedAccuracyMetersPerSecond + "  mBearingAccuracyDegrees:" + mBearingAccuracyDegrees + "   ======")
                    )
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initEvent()
        getDevicesType()
        if (DeviceUtil.isX3Device()) {
            collectBleStatus()
        }
        checkLocationPermissionAndInit()
    }

    private fun checkLocationPermissionAndInit() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                initGPS()
            }
            else -> {
                // Request location permissions
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun initGPS() {
        mLauncher = Launcher.getInstance(this)
        mLauncher!!.enableLog()//打开log
        mLauncher!!.addOnResponseListener(response)
        GPSIPCHelper.registerGPSInfo(this)
    }

    private fun getDevicesType() {
        mBindingPair.updateView {
            if (DeviceUtil.isX3Device()) {
                tvDevicesType.text = "Rayneo X3"
                tvBle.visibility = View.VISIBLE
                tvBleStatus.visibility = View.VISIBLE
            } else {
                tvDevicesType.text = "Rayneo X2"
                tvBle.visibility = View.GONE
                tvBleStatus.visibility = View.GONE
            }

        }
    }

    private fun initEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                templeActionViewModel.state.collect {
                    when (it) {
                        is TempleAction.DoubleClick -> {
                            finish()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun collectBleStatus() {
        MobileState.isMobileConnected().onEach {
            FLogger.d("isMobileConnected:$it")
            mBindingPair.updateView {
                tvBleStatus.text = if (it) "connect" else "disconnect"
            }
        }.launchIn(lifecycleScope)
    }

    override fun onDestroy() {
        GPSIPCHelper.unRegisterGPSInfo(this)
        super.onDestroy()
    }
}