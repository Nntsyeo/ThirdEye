package com.ffalcon.mercury.android.sdk.demo.ui.activity.fusion

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ffalcon.mercury.android.sdk.core.make3DEffectForSide
import com.ffalcon.mercury.android.sdk.demo.R
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

class FusionVisionHomeActivity : BaseMirrorActivity<ActivityFusionVisionHomeBinding>() {
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
                    btnFusionActivity,
                    eventHandler = { action ->
                        when (action) {
                            is TempleAction.Click -> {
                                startActivity(
                                    Intent(
                                        this@FusionVisionHomeActivity,
                                        FusionVisionActivity::class.java
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
                                btnFusionActivity,
                                mBindingPair.checkIsLeft(this)
                            )
                        }
                    }
                ),
                FocusInfo(
                    btnFusionView,
                    eventHandler = { action ->
                        when (action) {
                            is TempleAction.Click -> {
                                startActivity(
                                    Intent(
                                        this@FusionVisionHomeActivity,
                                        MirrorContainerViewActivity::class.java
                                    )
                                )
                            }

                            else -> Unit
                        }
                    },
                    focusChangeHandler = { hasFocus ->
                        mBindingPair.updateView {
                            triggerFocus(hasFocus, btnFusionView, mBindingPair.checkIsLeft(this))
                        }
                    }
                ),
                FocusInfo(
                    btnFusionFragment,
                    eventHandler = { action ->
                        when (action) {
                            is TempleAction.Click -> {
                                startActivity(
                                    Intent(
                                        this@FusionVisionHomeActivity,
                                        FragmentDemoActivity::class.java
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
                                btnFusionFragment,
                                mBindingPair.checkIsLeft(this)
                            )
                        }
                    }
                ),
                FocusInfo(
                    btnToast,
                    eventHandler = { action ->
                        when (action) {
                            is TempleAction.Click -> {
                                FToast.show("btnToast click")
                            }

                            else -> Unit
                        }
                    },
                    focusChangeHandler = { hasFocus ->
                        mBindingPair.updateView {
                            triggerFocus(hasFocus, btnToast, mBindingPair.checkIsLeft(this))
                        }
                    }
                ),
                FocusInfo(
                    btnDialog,
                    eventHandler = { action ->
                        when (action) {
                            is TempleAction.Click -> {
                                startActivity(
                                    Intent(
                                        this@FusionVisionHomeActivity,
                                        DialogActivity::class.java
                                    )
                                )
                            }

                            else -> Unit
                        }
                    },
                    focusChangeHandler = { hasFocus ->
                        mBindingPair.updateView {
                            triggerFocus(hasFocus, btnDialog, mBindingPair.checkIsLeft(this))
                        }
                    }
                ),
                FocusInfo(
                    btnPag,
                    eventHandler = { action ->
                        when (action) {
                            is TempleAction.Click -> {
                                startActivity(
                                    Intent(
                                        this@FusionVisionHomeActivity,
                                        PAGActivity::class.java
                                    )
                                )
                            }

                            else -> Unit
                        }
                    },
                    focusChangeHandler = { hasFocus ->
                        mBindingPair.updateView {
                            triggerFocus(hasFocus, btnPag, mBindingPair.checkIsLeft(this))
                        }
                    }
                ),
            )
            focusHolder.currentFocus(mBindingPair.left.btnFusionActivity)
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