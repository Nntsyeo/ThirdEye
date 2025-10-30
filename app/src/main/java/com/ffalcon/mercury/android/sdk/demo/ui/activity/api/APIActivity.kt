package com.ffalcon.mercury.android.sdk.demo.ui.activity.api

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ffalcon.mercury.android.sdk.api.MobileState
import com.ffalcon.mercury.android.sdk.demo.databinding.ActivityApiBinding
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
                    runOnUiThread {
                        mBindingPair.updateView {
                            tvTvLocationInfo.text = "$mLatitude,$mLongitude"
                        }
                    }
                    val mAltitude = jo.getDouble("mAltitude")
                    val mSpeed = jo.getDouble("mSpeed")
                    val mBearing = jo.getDouble("mBearing")
                    val mHorizontalAccuracyMeters = jo.getDouble("mHorizontalAccuracyMeters")
                    val mVerticalAccuracyMeters = jo.getDouble("mVerticalAccuracyMeters")
                    val mSpeedAccuracyMetersPerSecond =
                        jo.getDouble("mSpeedAccuracyMetersPerSecond")
                    val mBearingAccuracyDegrees = jo.getDouble("mBearingAccuracyDegrees")
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
        initGPS()
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