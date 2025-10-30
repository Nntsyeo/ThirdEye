package com.ffalcon.mercury.android.sdk.demo.ui.activity.tp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ffalcon.mercury.android.sdk.core.make3DEffectForSide
import com.ffalcon.mercury.android.sdk.demo.R
import com.ffalcon.mercury.android.sdk.demo.databinding.ActivityTpEventBinding
import com.ffalcon.mercury.android.sdk.demo.ui.activity.api.APIActivity
import com.ffalcon.mercury.android.sdk.demo.ui.activity.camera.CameraActivity
import com.ffalcon.mercury.android.sdk.demo.ui.activity.fusion.FusionVisionHomeActivity
import com.ffalcon.mercury.android.sdk.demo.ui.activity.recycle.RecycleViewHomeActivity
import com.ffalcon.mercury.android.sdk.focus.reqFocus
import com.ffalcon.mercury.android.sdk.touch.TempleAction
import com.ffalcon.mercury.android.sdk.ui.activity.BaseMirrorActivity
import com.ffalcon.mercury.android.sdk.ui.toast.FToast
import com.ffalcon.mercury.android.sdk.ui.util.FixPosFocusTracker
import com.ffalcon.mercury.android.sdk.ui.util.FocusHolder
import com.ffalcon.mercury.android.sdk.ui.util.FocusInfo
import com.ffalcon.mercury.android.sdk.util.FLogger
import kotlinx.coroutines.launch

class TPEventActivity : BaseMirrorActivity<ActivityTpEventBinding>() {
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
                    btnEvent,
                    eventHandler = { action -> handleAction(action) },
                    focusChangeHandler = { hasFocus ->
                        mBindingPair.updateView {
                            triggerFocus(hasFocus, btnEvent, mBindingPair.checkIsLeft(this))
                        }
                    }
                )
            )
            focusHolder.currentFocus(mBindingPair.left.btnEvent)
        }
        fixPosFocusTracker = FixPosFocusTracker(focusHolder).apply {
            focusObj.reqFocus()
        }
    }

    private fun handleAction(action: TempleAction) {
        when (action) {
            is TempleAction.LongClick -> {
                FToast.show("LongClick")
            }

            is TempleAction.Click -> {
                FToast.show("Click")
            }

            is TempleAction.DoubleClick -> {
                FToast.show("DoubleClick")
                finish()
            }

            is TempleAction.TripleClick -> {
                FToast.show("TripleClick")
            }

            is TempleAction.SlideBackward -> {
                FToast.show("SlideBackward")
            }

            is TempleAction.SlideForward -> {
                FToast.show("SlideForward")
            }

            is TempleAction.SlideUpwards -> {
                FToast.show("SlideUpwards")
            }

            is TempleAction.SlideDownwards -> {
                FToast.show("SlideDownwards")
            }


            else -> Unit
        }
    }

    private fun initEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                templeActionViewModel.state.collect {
                    fixPosFocusTracker?.handleFocusTargetEvent(it)
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