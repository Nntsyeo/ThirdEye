package com.ffalcon.mercury.android.sdk.demo.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.lifecycleScope
import com.ffalcon.mercury.android.sdk.core.BaseScreenHolder
import com.ffalcon.mercury.android.sdk.demo.R
import com.ffalcon.mercury.android.sdk.demo.databinding.FragmentDemoBinding
import com.ffalcon.mercury.android.sdk.ui.toast.FToast
import com.ffalcon.mercury.android.sdk.focus.IFocusable
import com.ffalcon.mercury.android.sdk.focus.releaseFocus
import com.ffalcon.mercury.android.sdk.touch.TempleAction
import com.ffalcon.mercury.android.sdk.touch.TempleActionViewModel
import com.ffalcon.mercury.android.sdk.ui.fragment.BaseMirrorFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive

class FragmentDemo :
    BaseMirrorFragment<FragmentDemoBinding, BaseScreenHolder<FragmentDemoBinding>>(), IFocusable {
    override var hasFocus: Boolean = false
        set(value) {
            field = value
            updateViewBkg()
        }


    override var focusParent: IFocusable? = null

    override fun onCreateView(rootView: View, savedInstanceState: Bundle?) {
        val content = arguments?.getString("content") ?: ""
        mBindingPair.updateView {
            tvContent.text = content
        }
        val templeActionViewModel =
            ViewModelProvider(requireActivity()).get<TempleActionViewModel>()
        lifecycleScope.launchWhenResumed {
            templeActionViewModel.state.collectLatest { action ->
                if (!hasFocus || !isActive || action.consumed) {
                    return@collectLatest
                }
                when (action) {
                    is TempleAction.DoubleClick -> {
                        // consumed the event
                        action.consumed = true
                        releaseFocus()
                    }

                    is TempleAction.Click -> {
                        FToast.show(mBindingPair.left.tvContent.text.toString())
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun getBgColor(content: String): Int {
        if (content.endsWith("1")) {
            return com.ffalcon.mercury.android.sdk.R.color.color_rayneo_theme_1
        } else if (content.endsWith("2")) {
            return com.ffalcon.mercury.android.sdk.R.color.color_rayneo_theme_2
        } else if (content.endsWith("3")) {
            return com.ffalcon.mercury.android.sdk.R.color.color_rayneo_theme_3
        }
        return com.ffalcon.mercury.android.sdk.R.color.color_rayneo_theme_1
    }

    private fun updateViewBkg() {
        mBindingPair.updateView {
            if (hasFocus) {
                tvContent.setBackgroundColor(tvContent.context.getColor(getBgColor(tvContent.text.toString())))
            } else {
                tvContent.setBackgroundColor(tvContent.context.getColor(R.color.teal_700))
            }
        }
    }

    companion object {
        fun newInstance(content: String): FragmentDemo {
            val fragment = FragmentDemo()
            fragment.arguments = Bundle().apply {
                putString("content", content)
            }
            return fragment
        }
    }
}
