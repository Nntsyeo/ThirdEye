package com.ffalcon.mercury.android.sdk.demo.ui.activity.player

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ffalcon.mercury.android.sdk.demo.R
import com.ffalcon.mercury.android.sdk.demo.databinding.ActivityVideoPlayBinding
import com.ffalcon.mercury.android.sdk.touch.TempleAction
import com.ffalcon.mercury.android.sdk.ui.activity.BaseEventActivity
import kotlinx.coroutines.launch

class VideoPlayActivity : BaseEventActivity() {
    private lateinit var binding: ActivityVideoPlayBinding
    private var mPlayer: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textureView.surfaceTextureListener = object : SimpleSurfaceTextureListener() {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                try {
                    mPlayer =
                        MediaPlayer.create(
                            this@VideoPlayActivity,
                            R.raw.rayneo
                        )?.apply {
                            setSurface(Surface(surface))
                            setVolume(0.6f, 0.6f)
                            isLooping = true
                            start()
                        }
                } catch (ignored: Exception) {
                }
            }
        }

        binding.textureView.let { leftTexture ->
            binding.mirrorView.setSource(leftTexture)
            binding.mirrorView.startMirroring()
        }




        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                templeActionViewModel.state.collect {
                    when (it) {
                        is TempleAction.Click -> {
                            mPlayer?.apply {
                                if (isPlaying) {
                                    pause()
                                } else {
                                    start()
                                }
                            }
                        }

                        is TempleAction.DoubleClick -> {
                            finish()
                        }

                        else -> Unit
                    }
                }
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        binding.mirrorView.stopMirroring()
        mPlayer?.release()
    }

}


open class SimpleSurfaceTextureListener : TextureView.SurfaceTextureListener {
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

}