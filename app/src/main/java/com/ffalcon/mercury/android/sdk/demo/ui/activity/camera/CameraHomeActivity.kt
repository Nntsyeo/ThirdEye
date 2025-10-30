package com.ffalcon.mercury.android.sdk.demo.ui.activity.camera

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ffalcon.mercury.android.sdk.core.make3DEffectForSide
import com.ffalcon.mercury.android.sdk.demo.R
import com.ffalcon.mercury.android.sdk.demo.databinding.ActivityApiHomeBinding
import com.ffalcon.mercury.android.sdk.demo.databinding.ActivityCameraHomeBinding
import com.ffalcon.mercury.android.sdk.demo.databinding.ActivityFusionVisionHomeBinding
import com.ffalcon.mercury.android.sdk.demo.ui.activity.DialogActivity
import com.ffalcon.mercury.android.sdk.demo.ui.activity.FragmentDemoActivity
import com.ffalcon.mercury.android.sdk.touch.TempleAction
import com.ffalcon.mercury.android.sdk.ui.activity.BaseMirrorActivity
import com.ffalcon.mercury.android.sdk.ui.toast.FToast
import com.ffalcon.mercury.android.sdk.ui.util.FixPosFocusTracker
import com.ffalcon.mercury.android.sdk.ui.util.FocusHolder
import com.ffalcon.mercury.android.sdk.ui.util.FocusInfo
import kotlinx.coroutines.launch

class CameraHomeActivity : BaseMirrorActivity<ActivityCameraHomeBinding>() {
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
                    btnCamera,
                    eventHandler = { action ->
                        when (action) {
                            is TempleAction.Click -> {
                                startActivity(
                                    Intent(
                                        this@CameraHomeActivity,
                                        CameraActivity::class.java
                                    )
                                )
                            }

                            else -> Unit
                        }
                    },
                    focusChangeHandler = { hasFocus ->
                        mBindingPair.updateView {
                            triggerFocus(
                                hasFocus,
                                btnCamera,
                                mBindingPair.checkIsLeft(this)
                            )
                        }
                    }
                ),
                FocusInfo(
                    btnVga,
                    eventHandler = { action ->
                        when (action) {
                            is TempleAction.Click -> {
                                startActivity(
                                    Intent(
                                        this@CameraHomeActivity,
                                        CameraActivity::class.java
                                    ).apply {
                                        putExtra("isVGA", true)
                                    }
                                )
                            }

                            else -> Unit
                        }
                    },
                    focusChangeHandler = { hasFocus ->
                        mBindingPair.updateView {
                            triggerFocus(hasFocus, btnVga, mBindingPair.checkIsLeft(this))
                        }
                    }
                ),
            )
            focusHolder.currentFocus(mBindingPair.left.btnCamera)
        }
        fixPosFocusTracker = FixPosFocusTracker(focusHolder).apply {
            // focusObj.reqFocus()
            focusObj.hasFocus = true
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
}