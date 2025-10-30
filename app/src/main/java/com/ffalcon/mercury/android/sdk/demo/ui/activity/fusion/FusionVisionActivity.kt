package com.ffalcon.mercury.android.sdk.demo.ui.activity.fusion

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ffalcon.mercury.android.sdk.demo.databinding.ActivityFusionActBinding
import com.ffalcon.mercury.android.sdk.touch.TempleAction
import com.ffalcon.mercury.android.sdk.ui.activity.BaseMirrorActivity
import kotlinx.coroutines.launch

class FusionVisionActivity : BaseMirrorActivity<ActivityFusionActBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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