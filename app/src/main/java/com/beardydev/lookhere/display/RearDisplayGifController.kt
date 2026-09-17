package com.beardydev.lookhere.display

import android.content.Context
import android.graphics.Color
import android.os.Binder
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.window.core.ExperimentalWindowApi
import androidx.window.area.WindowAreaCapability
import androidx.window.area.WindowAreaController
import androidx.window.area.WindowAreaInfo
import androidx.window.area.WindowAreaPresentationSessionCallback
import androidx.window.area.WindowAreaSessionPresenter
import com.beardydev.lookhere.domain.model.SelectedGif
import com.beardydev.lookhere.domain.usecase.ObserveCoverScreenGifUseCase
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Renders the selected GIF on the Fold's cover screen while the camera screen
 * is active.
 *
 * A raw DisplayManager/Presentation lookup DOES find the cover screen as a
 * Display with FLAG_PRESENTATION -- but on real hardware, in normal unfolded
 * posture the OEM's device-state layout keeps that display disabled/off; a
 * plain Presentation targeting it silently shows nothing. The actual
 * mechanism that works is Jetpack WindowManager's "present content on window
 * area" API: calling presentContentOnWindowArea() is what triggers the
 * vendor's transition into the simultaneous dual-display device state.
 * (Confirmed on-device: `adb shell dumpsys display`'s DeviceStateToLayoutMap
 * shows the cover screen is OFF in the "open" state and only ON in the
 * "dual"/"rear_dual" states, which this API is what actually requests.)
 */
@OptIn(ExperimentalWindowApi::class)
class RearDisplayGifController(
    private val activity: ComponentActivity,
    private val observeCoverScreenGif: ObserveCoverScreenGifUseCase,
) {
    private val windowAreaController = WindowAreaController.getOrCreate()
    private val mainExecutor = ContextCompat.getMainExecutor(activity)

    private var session: WindowAreaSessionPresenter? = null
    private var imageView: ImageView? = null
    private var sessionRequestInFlight = false
    private var latestGif: SelectedGif? = null
    private var job: Job? = null

    // Selfie mode shows the GIF in-frame on the main screen instead (there's no
    // one else to show the cover screen to), so the cover-screen session needs
    // to be suppressible without tearing down the whole controller.
    private val suppressed = MutableStateFlow(false)

    fun setSuppressed(value: Boolean) {
        suppressed.value = value
    }

    // Whether the device can currently show content on a second screen at all --
    // false when folded shut (only the cover screen is visible) or on a plain
    // single-display phone. Used to default fresh camera sessions into selfie
    // mode, where the GIF plays in-frame instead of on a screen no one can see.
    private val _isDualDisplayAvailable = MutableStateFlow(false)
    val isDualDisplayAvailable: StateFlow<Boolean> = _isDualDisplayAvailable.asStateFlow()

    fun start(scope: CoroutineScope) {
        job = scope.launch {
            combine(
                windowAreaController.windowAreaInfos,
                observeCoverScreenGif(suppressed),
            ) { infos, gif -> infos to gif }
                .collect { (infos, gif) ->
                    latestGif = gif

                    val rearFacingInfo = infos.firstOrNull { it.type == WindowAreaInfo.Type.TYPE_REAR_FACING }
                    val status = rearFacingInfo
                        ?.getCapability(WindowAreaCapability.Operation.OPERATION_PRESENT_ON_AREA)
                        ?.status

                    _isDualDisplayAvailable.value =
                        status == WindowAreaCapability.Status.WINDOW_AREA_STATUS_AVAILABLE ||
                            status == WindowAreaCapability.Status.WINDOW_AREA_STATUS_ACTIVE

                    // gif is already null when suppressed (selfie mode plays it
                    // in-frame on the main screen instead) -- no separate branch needed.
                    when {
                        gif == null -> closeSession()
                        status == WindowAreaCapability.Status.WINDOW_AREA_STATUS_AVAILABLE ->
                            requestSession(rearFacingInfo.token)

                        status == WindowAreaCapability.Status.WINDOW_AREA_STATUS_ACTIVE ->
                            renderGif(gif)

                        else -> closeSession()
                    }
                }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        closeSession()
    }

    private fun requestSession(token: Binder) {
        if (sessionRequestInFlight || session != null) return
        sessionRequestInFlight = true

        windowAreaController.presentContentOnWindowArea(
            token,
            activity,
            mainExecutor,
            object : WindowAreaPresentationSessionCallback {
                override fun onSessionStarted(session: WindowAreaSessionPresenter) {
                    Log.d("LookHereRearDisplay", "onSessionStarted")
                    sessionRequestInFlight = false
                    this@RearDisplayGifController.session = session

                    val view = createImageView(session.context)
                    imageView = view
                    session.setContentView(FrameLayout(session.context).apply { addView(view) })

                    latestGif?.let { renderGif(it) }
                }

                override fun onSessionEnded(t: Throwable?) {
                    Log.d("LookHereRearDisplay", "onSessionEnded", t)
                    sessionRequestInFlight = false
                    session = null
                    imageView = null
                }

                override fun onContainerVisibilityChanged(isVisible: Boolean) = Unit
            },
        )
    }

    private fun closeSession() {
        imageView?.let { Glide.with(it).clear(it) }
        session?.close()
        session = null
        imageView = null
    }

    private fun renderGif(gif: SelectedGif) {
        val view = imageView ?: return
        // fitCenter, not centerCrop: a GIF's aspect ratio rarely matches the
        // cover screen's, and cropping was cutting off content. Letterboxing
        // (black bars) shows the whole GIF instead.
        val requestOptions = RequestOptions().fitCenter()
        when (gif) {
            is SelectedGif.FromSearch ->
                Glide.with(view).asGif().load(gif.fullUrl).apply(requestOptions).into(view)

            is SelectedGif.FromDevice ->
                Glide.with(view).asGif().load(File(gif.filePath)).apply(requestOptions).into(view)
        }
    }

    private fun createImageView(context: Context): ImageView =
        ImageView(context).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.BLACK)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            )
        }
}
