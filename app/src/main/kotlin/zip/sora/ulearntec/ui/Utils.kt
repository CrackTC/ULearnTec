package zip.sora.ulearntec.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.coroutineScope
import kotlin.math.PI
import kotlin.math.abs

@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }
}

@Composable
fun ForceLandscape() {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val activity = context as Activity
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        onDispose {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
}

// https://stackoverflow.com/a/77039810/11846878
@Composable
fun HideSystemBars() {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val window = context.findActivity()?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        insetsController.apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            insetsController.apply {
                show(WindowInsetsCompat.Type.statusBars())
                show(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

// https://stackoverflow.com/a/73885080/11846878
@Composable
fun UpdateViewConfiguration(
    longPressTimeoutMillis: Long? = null,
    doubleTapTimeoutMillis: Long? = null,
    doubleTapMinTimeMillis: Long? = null,
    touchSlop: Float? = null,
    content: @Composable () -> Unit
) {
    fun ViewConfiguration.updateViewConfiguration() = object : ViewConfiguration {
        override val longPressTimeoutMillis
            get() = longPressTimeoutMillis ?: this@updateViewConfiguration.longPressTimeoutMillis

        override val doubleTapTimeoutMillis
            get() = doubleTapTimeoutMillis ?: this@updateViewConfiguration.doubleTapTimeoutMillis

        override val doubleTapMinTimeMillis
            get() =
                doubleTapMinTimeMillis ?: this@updateViewConfiguration.doubleTapMinTimeMillis

        override val touchSlop: Float
            get() = touchSlop ?: this@updateViewConfiguration.touchSlop
    }

    CompositionLocalProvider(
        LocalViewConfiguration provides LocalViewConfiguration.current.updateViewConfiguration()
    ) {
        content()
    }
}

@Composable
fun OverrideBrightness(brightness: Float) {
    val context = LocalContext.current
    DisposableEffect(brightness) {
        setBrightness(context, brightness)
        onDispose {
            setBrightness(context)
        }
    }
}

fun setBrightness(
    context: Context,
    value: Float = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
) {
    val activity = context as? Activity ?: return
    val layoutParams = activity.window.attributes
    layoutParams.screenBrightness = value
    activity.window.attributes = layoutParams
}

// Similar to detectTransformGestures, but immediately consume all changes, making any other
// slop-awaiters in main/final pass to cancel.
suspend fun PointerInputScope.exclusiveDetectTransformGestures(
    panZoomLock: Boolean = false,
    onGestureFinished: PointerInputScope.() -> Unit,
    onGesture: (centroid: Offset, pan: Offset, zoom: Float, rotation: Float) -> Unit
) {
    awaitEachGesture {
        var rotation = 0f
        var zoom = 1f
        var pan = Offset.Zero
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop
        var lockedToPanZoom = false

        awaitFirstDown(requireUnconsumed = false).consume()
        do {
            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
            val zoomChange = event.calculateZoom()
            val rotationChange = event.calculateRotation()
            val panChange = event.calculatePan()

            event.changes.fastForEach {
                if (it.positionChanged()) {
                    it.consume()
                }
            }

            if (!pastTouchSlop) {
                zoom *= zoomChange
                rotation += rotationChange
                pan += panChange

                val centroidSize = event.calculateCentroidSize(useCurrent = false)
                val zoomMotion = abs(1 - zoom) * centroidSize
                val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
                val panMotion = pan.getDistance()

                if (zoomMotion > touchSlop ||
                    rotationMotion > touchSlop ||
                    panMotion > touchSlop
                ) {
                    pastTouchSlop = true
                    lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
                }
            }

            if (pastTouchSlop) {
                val centroid = event.calculateCentroid(useCurrent = false)
                val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
                if (effectiveRotation != 0f ||
                    zoomChange != 1f ||
                    panChange != Offset.Zero
                ) {
                    onGesture(centroid, panChange, zoomChange, effectiveRotation)
                }
            }
        } while (event.changes.fastAny { it.pressed })

        onGestureFinished()
    }
}

private suspend fun AwaitPointerEventScope.exclusiveAwaitSecondDown(
    firstUp: PointerInputChange
): PointerInputChange? = withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
    val minUptime = firstUp.uptimeMillis + viewConfiguration.doubleTapMinTimeMillis
    var change: PointerInputChange
    // The second tap doesn't count if it happens before DoubleTapMinTime of the first tap
    do {
        change = awaitFirstDown(pass = PointerEventPass.Initial)
    } while (change.uptimeMillis < minUptime)
    change
}

suspend fun PointerInputScope.exclusiveDetectDoubleTapGesture(
    onDoubleTap: ((Offset) -> Unit)
) = coroutineScope {
    awaitEachGesture {
        awaitFirstDown(pass = PointerEventPass.Initial).consume()

        // wait for first tap up or long press
        val upOrCancel = waitForUpOrCancellation(pass = PointerEventPass.Initial)

        if (upOrCancel != null) {
            upOrCancel.consume()

            // tap was successful.
            // check for second tap
            val secondDown = exclusiveAwaitSecondDown(upOrCancel)

            if (secondDown != null) {
                secondDown.consume()

                // Second tap down detected
                val secondUp = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                if (secondUp != null) {
                    secondUp.consume()
                    onDoubleTap(secondUp.position)
                }
            }
        }
    }
}
