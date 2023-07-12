/*==================================================================================
https://qiita.com/inuko/items/408937f72b1ab861d613
https://developer.android.com/training/camerax/architecture?hl=ja#custom-life
===================================================================================*/

package jp.ac.thers.s.hayshi.signlanguagetranslator.media_pipe

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.camera.core.CameraProvider // 追加

class CustomLifecycle : LifecycleOwner {
    private val lifecycleRegistry: LifecycleRegistry

    init {
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.markState(Lifecycle.State.CREATED)
    }

    fun doOnResume() {
        lifecycleRegistry.markState(Lifecycle.State.RESUMED)
    }

    fun doStart() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    fun doPause() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}