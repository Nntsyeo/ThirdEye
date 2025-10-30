package com.ffalcon.thirdeye.demo.ui.activity.fusion


import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ffalcon.thirdeye.demo.databinding.ActivityMirrorContainerViewBinding
import com.ffalcon.thirdeye.demo.databinding.ViewContainerMirrorBinding
import com.ffalcon.mercury.android.sdk.touch.TempleAction
import com.ffalcon.mercury.android.sdk.ui.activity.BaseEventActivity
import kotlinx.coroutines.launch

class MirrorContainerViewActivity : BaseEventActivity() {
    private lateinit var binding: ActivityMirrorContainerViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMirrorContainerViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mirrorContainer.bindTo(ViewContainerMirrorBinding::class.java)

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
}