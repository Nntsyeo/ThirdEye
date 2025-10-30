package com.ffalcon.mercury.android.sdk.demo.ui.activity.api

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ffalcon.mercury.android.sdk.demo.databinding.ActivityImuBinding
import com.ffalcon.mercury.android.sdk.touch.TempleAction
import com.ffalcon.mercury.android.sdk.ui.activity.BaseMirrorActivity
import kotlinx.coroutines.launch


class IMUActivity : BaseMirrorActivity<ActivityImuBinding>(), SensorEventListener {
    // 传感器管理器和传感器对象
    private lateinit var sensorManager: SensorManager
    private var accelerometerSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initIMU()
        initEvent()
    }

    private fun initIMU() {
        // 1. 获取 SensorManager 系统服务
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // 2. 获取默认的传感器实例
        // 使用 TYPE_ACCELEROMETER 获取加速度计
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        // 使用 TYPE_GYROSCOPE 获取陀螺仪
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        // 使用 TYPE_MAGNETIC_FIELD 获取磁力计
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        mBindingPair.updateView {
            // 检查设备是否支持这些传感器
            if (accelerometerSensor == null) {
                tvAccelerometer.text = "加速度计不可用"
            }
            if (gyroscopeSensor == null) {
                tvGyroscope.text = "陀螺仪不可用"
            }
            if (magnetometerSensor == null) {
                tvMagnetometer.text = "磁力计不可用"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 3. 注册传感器监听器
        // 参数：监听器、传感器对象、采样延迟（微秒）
        accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscopeSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        magnetometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }


    override fun onPause() {
        super.onPause()
        // 4. 非常重要！在暂停时注销监听器以节省电量
        sensorManager.unregisterListener(this)
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

    // 当传感器数据变化时回调
    override fun onSensorChanged(event: SensorEvent) {
        mBindingPair.updateView {
            // event.values 是一个浮点数数组，包含X, Y, Z轴的数据
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    tvAccelerometer.text =
                        "加速度计:\nX: %.2f m/s²\nY: %.2f m/s²\nZ: %.2f m/s²".format(x, y, z)
                }

                Sensor.TYPE_GYROSCOPE -> {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    tvGyroscope.text =
                        "陀螺仪:\nX: %.2f rad/s\nY: %.2f rad/s\nZ: %.2f rad/s".format(x, y, z)
                }

                Sensor.TYPE_MAGNETIC_FIELD -> {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    tvMagnetometer.text =
                        "磁力计:\nX: %.2f μT\nY: %.2f μT\nZ: %.2f μT".format(x, y, z)
                }
            }
        }
    }

    // 当传感器精度变化时回调（通常不需要处理）
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 可以在这里处理精度变化，例如从低精度变为高精度
    }

}