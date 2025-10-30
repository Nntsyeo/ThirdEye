package com.ffalcon.mercury.android.sdk.demo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ffalcon.mercury.android.sdk.core.make3DEffectForSide
import com.ffalcon.mercury.android.sdk.demo.databinding.LayoutDemoHomeBinding
import com.ffalcon.mercury.android.sdk.demo.ui.activity.DialogActivity
import com.ffalcon.mercury.android.sdk.demo.ui.activity.api.APIActivity
import com.ffalcon.mercury.android.sdk.demo.ui.activity.api.APIHomeActivity
import com.ffalcon.mercury.android.sdk.demo.ui.activity.camera.CameraActivity
import com.ffalcon.mercury.android.sdk.demo.ui.activity.camera.CameraHomeActivity
import com.ffalcon.mercury.android.sdk.demo.ui.activity.fusion.FusionVisionHomeActivity
import com.ffalcon.mercury.android.sdk.demo.ui.activity.player.VideoPlayActivity
import com.ffalcon.mercury.android.sdk.demo.ui.activity.recycle.RecycleViewHomeActivity
import com.ffalcon.mercury.android.sdk.demo.ui.activity.tp.TPEventActivity
import com.ffalcon.mercury.android.sdk.focus.reqFocus
import com.ffalcon.mercury.android.sdk.touch.TempleAction
import com.ffalcon.mercury.android.sdk.ui.activity.BaseMirrorActivity
import com.ffalcon.mercury.android.sdk.ui.util.FixPosFocusTracker
import com.ffalcon.mercury.android.sdk.ui.util.FocusHolder
import com.ffalcon.mercury.android.sdk.ui.util.FocusInfo
import com.ffalcon.mercury.android.sdk.util.FLogger
import kotlinx.coroutines.launch


class DemoHomeActivity : BaseMirrorActivity<LayoutDemoHomeBinding>() {
    private var fixPosFocusTracker: FixPosFocusTracker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFocusTarget()
        initEvent()
    }

    private fun initFocusTarget() {
        val focusHolder = FocusHolder(false)
        mBindingPair.setLeft {
            focusHolder.addFocusTarget(
                FocusInfo(
                    btnFusionVision,
                    eventHandler = { action ->
                        when (action) {
                            is TempleAction.Click -> {
                                startActivity(
                                    Intent(
                                        this@DemoHomeActivity,
                                        FusionVisionHomeActivity::class.java
                                    )
                                )
                            }

                            else -> Unit
                        }
                    },
                    focusChangeHandler = { hasFocus ->
                        mBindingPair.updateView {
                            triggerFocus(hasFocus, btnFusionVision, mBindingPair.checkIsLeft(this))
                        }
                    }
                ),
                FocusInfo(
                    btnRecycleView,
                    eventHandler = { action ->
                        when (action) {
                            is TempleAction.Click -> {
                                startActivity(
                                    Intent(
                                        this@DemoHomeActivity,
                                        RecycleViewHomeActivity::class.java
                                    )
                                )
                            }

                            else -> Unit
                        }
                    },
                    focusChangeHandler = { hasFocus ->
                        mBindingPair.updateView {
                            triggerFocus(hasFocus, btnRecycleView, mBindingPair.checkIsLeft(this))
                        }
                    }
                ),
                FocusInfo(
                    btnApi,
                    eventHandler = { action ->
                        when (action) {
                            is TempleAction.Click -> {
                                startActivity(
                                    Intent(
                                        this@DemoHomeActivity,
                                        APIHomeActivity::class.java
                                    )
                                )
                            }

                            else -> Unit
                        }
                    },
                    focusChangeHandler = { hasFocus ->
                        mBindingPair.updateView {
                            triggerFocus(hasFocus, btnApi, mBindingPair.checkIsLeft(this))
                        }
                    }
                ),
                FocusInfo(
                    btnCamera,
                    eventHandler = { action ->
                        when (action) {
                            is TempleAction.Click -> {

                                // 检查权限
                                if (ContextCompat.checkSelfPermission(
                                        this@DemoHomeActivity,
                                        Manifest.permission.CAMERA
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    // 请求权限
                                    ActivityCompat.requestPermissions(
                                        this@DemoHomeActivity,
                                        arrayOf(Manifest.permission.CAMERA),
                                        1
                                    )
                                } else {
                                    // 权限已授予，可以使用相机
                                    FLogger.d("Camera permission already granted")

                                    startActivity(
                                        Intent(
                                            this@DemoHomeActivity,
                                            CameraHomeActivity::class.java
                                        )
                                    )
                                }


                            }

                            else -> Unit
                        }
                    },
                    focusChangeHandler = { hasFocus ->
                        mBindingPair.updateView {
                            triggerFocus(hasFocus, btnCamera, mBindingPair.checkIsLeft(this))
                        }
                    }
                ),
                FocusInfo(
                    btnEvent,
                    eventHandler = { action ->
                        when (action) {
                            is TempleAction.Click -> {

                                startActivity(
                                    Intent(
                                        this@DemoHomeActivity,
                                        TPEventActivity::class.java
                                    )
                                )


                            }

                            else -> Unit
                        }
                    },
                    focusChangeHandler = { hasFocus ->
                        mBindingPair.updateView {
                            triggerFocus(hasFocus, btnEvent, mBindingPair.checkIsLeft(this))
                        }
                    }
                ),
                FocusInfo(
                    btnPlayer,
                    eventHandler = { action ->
                        when (action) {
                            is TempleAction.Click -> {
                                startActivity(
                                    Intent(
                                        this@DemoHomeActivity,
                                        VideoPlayActivity::class.java
                                    )
                                )
                            }

                            else -> Unit
                        }
                    },
                    focusChangeHandler = { hasFocus ->
                        mBindingPair.updateView {
                            triggerFocus(hasFocus, btnPlayer, mBindingPair.checkIsLeft(this))
                        }
                    }
                ),
            )
            focusHolder.currentFocus(mBindingPair.left.btnFusionVision)
        }
        fixPosFocusTracker = FixPosFocusTracker(focusHolder).apply {
            focusObj.reqFocus()
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

                        else -> fixPosFocusTracker?.handleFocusTargetEvent(it)
                    }
                }
            }
        }
    }

    private fun triggerFocus(hasFocus: Boolean, view: View, isLeft: Boolean) {
        view.setBackgroundColor(getColor(if (hasFocus) com.ffalcon.mercury.android.sdk.R.color.color_rayneo_theme_0 else R.color.black))
        // 3D效果
        make3DEffectForSide(view, isLeft, hasFocus)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 权限被授予
            FLogger.d("Camera permission granted")
            startActivity(
                Intent(
                    this@DemoHomeActivity,
                    CameraHomeActivity::class.java
                )
            )
        } else {
            // 权限被拒绝
            FLogger.d("Camera permission denied")
        }
    }
}