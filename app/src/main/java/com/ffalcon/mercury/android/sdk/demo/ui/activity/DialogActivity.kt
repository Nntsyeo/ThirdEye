package com.ffalcon.mercury.android.sdk.demo.ui.activity

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ffalcon.mercury.android.sdk.core.make3DEffectForSide
import com.ffalcon.mercury.android.sdk.demo.R
import com.ffalcon.mercury.android.sdk.demo.databinding.DialogTestBinding
import com.ffalcon.mercury.android.sdk.demo.databinding.LayoutDialogBinding
import com.ffalcon.mercury.android.sdk.ui.dialog.FDialog
import com.ffalcon.mercury.android.sdk.ui.dialog.FocusTracker
import com.ffalcon.mercury.android.sdk.ui.dialog.TrackInfo
import com.ffalcon.mercury.android.sdk.ui.toast.FToast
import com.ffalcon.mercury.android.sdk.touch.TempleAction
import com.ffalcon.mercury.android.sdk.ui.activity.BaseMirrorActivity
import com.ffalcon.mercury.android.sdk.util.FLogger
import kotlinx.coroutines.launch

class DialogActivity : BaseMirrorActivity<LayoutDialogBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initEvent()
    }

    private fun initEvent() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                templeActionViewModel.state.collect {
                    FLogger.i("DemoActivity", "action = $it")
                    when (it) {
                        is TempleAction.DoubleClick -> {
                            finish()
                        }
                        is TempleAction.Click -> {
                            showDialog()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun showDialog() {
        FDialog.Builder<DialogTestBinding>(this)
            .setCancelable(true)
            .setCanceledOnTouchOutside(true)
            .setOnShowListener {

            }
            .setOnDismissListener {

            }
            .setContentView(DialogTestBinding::class.java, initViewBlock = { pair, dialog ->
                //TODO update your ui
            })
            .apply {
                // 处理焦点切换逻辑和焦点对象事件响应
                val pair = mPair
                val btnOk = TrackInfo(pair.left.btnOk, eventHandler = { action, dialog ->
                    when (action) {
                        is TempleAction.Click -> {
                            FToast.show("Click Confirm")
                            dialog.dismiss()
                        }
                        else -> {
                            FLogger.i("btnOk action = $action")
                        }
                    }
                }, focusChangeHandler = { hasFocus ->
                    pair.updateView {
                        triggerFocus(hasFocus, btnOk, pair.checkIsLeft(this))
                    }
                })
                val btnCancel = TrackInfo(pair.left.btnCancel, eventHandler = { action, dialog ->
                    when (action) {
                        is TempleAction.Click -> {
                            FToast.show("Click ")
                            dialog.dismiss()
                        }
                        else -> {
                            FLogger.i("btnCancel action = $action")
                        }
                    }
                }, focusChangeHandler = { hasFocus ->
                    pair.updateView {
                        triggerFocus(hasFocus, btnCancel, pair.checkIsLeft(this))
                    }
                })
                // 设置焦点无限循环,添加切换焦点的对象
                val tracker = FocusTracker(true).apply {
                    addFocusTarget(btnOk, btnCancel)
                }
                // 设置默认焦点位
                tracker.currentFocus(pair.left.btnCancel)
                setFocusTracker(tracker)
            }
            .setEventHandler { action, dialog ->
                when (action) {
                    is TempleAction.DoubleClick -> {
                        dialog.dismiss()
                    }
                    else -> {}
                }
            }
            .build()
            .show()
    }

    private fun triggerFocus(hasFocus: Boolean, view: View, isLeft: Boolean) {
        view.setBackgroundColor(getColor(if (hasFocus) R.color.teal_700 else R.color.black))
        make3DEffectForSide(view, isLeft, hasFocus)
    }
}