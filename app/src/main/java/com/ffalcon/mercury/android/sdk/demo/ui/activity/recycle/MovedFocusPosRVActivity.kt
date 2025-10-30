package com.ffalcon.mercury.android.sdk.demo.ui.activity.recycle

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ffalcon.mercury.android.sdk.core.ViewPair
import com.ffalcon.mercury.android.sdk.demo.databinding.LayoutRecyclerviewMovedFocusBinding
import com.ffalcon.mercury.android.sdk.demo.ui.adapter.MovedFocusPosAdapter
import com.ffalcon.mercury.android.sdk.demo.ui.entity.contactList
import com.ffalcon.mercury.android.sdk.ui.toast.FToast
import com.ffalcon.mercury.android.sdk.ui.util.RecyclerViewFocusTracker
import com.ffalcon.mercury.android.sdk.touch.TempleAction
import com.ffalcon.mercury.android.sdk.touch.TempleActionViewModel
import com.ffalcon.mercury.android.sdk.ui.activity.BaseMirrorActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive

/**
 * RecyclerView with fixed focus position
 */
class MovedFocusPosRVActivity : BaseMirrorActivity<LayoutRecyclerviewMovedFocusBinding>() {
    private lateinit var favoriteTracker: RecyclerViewFocusTracker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        favoriteTracker = RecyclerViewFocusTracker(
            ViewPair(mBindingPair.left.recyclerView, mBindingPair.right.recyclerView),
            ignoreDelta = 70
        )
        initView()
        initEvent()
        favoriteTracker.focusObj.hasFocus = true
    }

    private fun initEvent() {
        // 监听原始事件，实现跟手效果

        lifecycleScope.launchWhenResumed {
            val templeActionViewModel =
                ViewModelProvider(this@MovedFocusPosRVActivity).get<TempleActionViewModel>()
            templeActionViewModel.state.collectLatest {
                if (!favoriteTracker.focusObj.hasFocus || !this.isActive || it.consumed) {
                    return@collectLatest
                }
                favoriteTracker.handleActionEvent(it) { action ->
                    when (action) {
                        is TempleAction.DoubleClick -> {
                            finish()
                        }
                        is TempleAction.Click -> {
                            if (!action.consumed) {
                                (mBindingPair.left.recyclerView.adapter as MovedFocusPosAdapter)
                                    .getCurrentData()?.apply {
                                        FToast.show(displayName)
                                    }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun initView() {
        val mPair = mBindingPair
        mPair.updateView {
            val isLeft = mPair.checkIsLeft(this)
            recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = MovedFocusPosAdapter(context, isLeft, favoriteTracker).apply {
                    setData(contactList())
                }
                itemAnimator = null
            }
            favoriteTracker.setCurrentSelectPos(0)
        }
    }
}
