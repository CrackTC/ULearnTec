package zip.sora.ulearntec.ui.screen

import android.text.format.DateUtils
import android.view.TextureView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.SubtitleView
import androidx.window.layout.WindowMetrics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import zip.sora.ulearntec.R
import zip.sora.ulearntec.domain.SwipeSeekMode
import zip.sora.ulearntec.domain.model.LiveStatus
import zip.sora.ulearntec.ui.UpdateViewConfiguration
import zip.sora.ulearntec.ui.component.ErrorPane
import zip.sora.ulearntec.ui.component.VerticalSlider
import zip.sora.ulearntec.ui.exclusiveDetectDoubleTapGesture
import zip.sora.ulearntec.ui.exclusiveDetectTransformGestures
import zip.sora.ulearntec.ui.screen.PlayerUiState.Error
import zip.sora.ulearntec.ui.screen.PlayerUiState.PreferenceLoaded
import zip.sora.ulearntec.ui.screen.PlayerUiState.PreferenceLoaded.ResourceLoaded.PlayerCreated
import zip.sora.ulearntec.ui.screen.PlayerUiState.PreferenceLoaded.ResourceLoaded.PlayerCreated.Playing

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.NormalVideoRow(
    players: List<Player>,
    aspectRatios: List<Float>,
    currentPipIndex: Int,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        players.forEachIndexed { index, player ->
            if (index != currentPipIndex)
                AndroidView(
                    modifier = modifier
                        .weight(1.0f, fill = false)
                        .aspectRatio(aspectRatios[index])
                        .sharedElement(
                            rememberSharedContentState(key = player),
                            animatedVisibilityScope
                        ),
                    factory = { context ->
                        TextureView(context).also {
                            player.setVideoTextureView(it)
                        }
                    }
                )
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.PipVideo(
    player: Player,
    aspectRatio: Float,
    offset: Density.() -> IntOffset,
    height: Dp,
    onDoubleTap: () -> Unit,
    onZoom: PointerInputScope.(Float) -> Unit,
    onDrag: PointerInputScope.(Offset) -> Unit,
    onDragFinished: PointerInputScope.() -> Unit,
    pointInputKey: Any?,
    lock: Boolean,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {

    AndroidView(
        factory = { context ->
            TextureView(context).also {
                player.setVideoTextureView(it)
            }
        },
        modifier = modifier
            .absoluteOffset(offset = offset)
            .height(height)
            .aspectRatio(aspectRatio)
            .pointerInput(pointInputKey, lock) {
                if (!lock) exclusiveDetectDoubleTapGesture { onDoubleTap() }
            }
            .pointerInput(pointInputKey, lock) {
                if (!lock) {
                    exclusiveDetectTransformGestures(onGestureFinished = onDragFinished) { _, pan, zoom, _ ->
                        when {
                            zoom != 1.0f -> onZoom(zoom)
                            else -> onDrag(pan)
                        }
                    }
                }
            }
            .sharedElement(
                rememberSharedContentState(key = player),
                animatedVisibilityScope,
                zIndexInOverlay = 1.0f
            )
            .clip(RoundedCornerShape(8.dp))
    )
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PlayerScreen(
    uiState: PlayerUiState,
    windowMetrics: WindowMetrics,
    currentVolumePercent: Float,
    currentBrightnessPercent: Float,
    onBackButtonClicked: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onSeek: (Long) -> Unit,
    onRetry: () -> Unit,
    onSpeed: (Float) -> Unit,
    onVolumeDelta: (Float) -> Unit,
    onBrightnessDelta: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    var lock by rememberSaveable { mutableStateOf(false) }
    var currentPipIndex by rememberSaveable { mutableIntStateOf(-1) }
    var showSubtitle by rememberSaveable { mutableStateOf(false) }
    var showOverlay by rememberSaveable { mutableStateOf(true) }
    var showVolumeBar by remember { mutableStateOf(false) }
    var showBrightnessBar by remember { mutableStateOf(false) }
    var speedingUp by remember { mutableStateOf(false) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekMillis by remember { mutableLongStateOf(0L) }
    var showingModalMenu by rememberSaveable { mutableStateOf(false) }

    val playing by rememberUpdatedState(uiState as? Playing)
    val currentMillis by remember { derivedStateOf { playing?.currentMillis ?: 0L } }
    val totalMillis by remember { derivedStateOf { playing?.totalMillis ?: 0L } }
    val isPlaying by remember { derivedStateOf { playing?.isPlaying } }

    val preferenceLoaded by rememberUpdatedState(uiState as? PreferenceLoaded)
    val gesturePreferences by remember { derivedStateOf { preferenceLoaded?.gesturePreferences } }

    // for LocalContentColor
    Surface {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Gesture overlay
            UpdateViewConfiguration(doubleTapTimeoutMillis = 150) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(WindowInsets.safeGestures.asPaddingValues())
                        .pointerInput(lock) {
                            if (!lock) {
                                detectHorizontalDragGestures(
                                    onDragStart = {
                                        seekMillis = currentMillis
                                        isSeeking = true
                                    },
                                    onDragEnd = {
                                        isSeeking = false
                                        onSeek(seekMillis)
                                    },
                                ) { _, amount ->
                                    val fullDragMillis = gesturePreferences?.let {
                                        when (it.swipeSeekMode) {
                                            SwipeSeekMode.FIXED -> it.swipeSeekFixedMillis
                                            SwipeSeekMode.PERCENT -> (it.swipeSeekPercent * totalMillis).toLong()
                                        }
                                    } ?: return@detectHorizontalDragGestures
                                    val delta =
                                        (amount / windowMetrics.bounds.width()) * fullDragMillis
                                    seekMillis =
                                        (seekMillis + delta.toLong()).coerceIn(0L, totalMillis)
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { showOverlay = true },
                                onPress = {
                                    if (!lock) {
                                        awaitRelease()
                                        if (speedingUp) {
                                            speedingUp = false
                                            onSpeed(1.0f)
                                        }
                                    }
                                },
                                onLongPress = {
                                    if (!lock) {
                                        speedingUp = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onSpeed(
                                            gesturePreferences?.longPressSpeed
                                                ?: return@detectTapGestures
                                        )
                                    }
                                },
                                onDoubleTap = {
                                    if (!lock) {
                                        if (isPlaying == true) onPause()
                                        else onPlay()
                                    }
                                },
                            )
                        },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1.0f)
                            .pointerInput(windowMetrics.bounds.height(), lock) {
                                if (!lock) {
                                    detectVerticalDragGestures(
                                        onDragStart = { showBrightnessBar = true },
                                        onDragEnd = { showBrightnessBar = false }
                                    ) { _, amount ->
                                        onBrightnessDelta(
                                            -amount / windowMetrics.bounds.height() * (gesturePreferences?.swipeBrightnessPercent
                                                ?: return@detectVerticalDragGestures)
                                        )
                                    }
                                }
                            }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1.0f)
                            .pointerInput(windowMetrics.bounds.height(), lock) {
                                if (!lock) {
                                    detectVerticalDragGestures(
                                        onDragStart = { showVolumeBar = true },
                                        onDragEnd = { showVolumeBar = false }
                                    ) { _, amount ->
                                        onVolumeDelta(
                                            -amount / windowMetrics.bounds.height() * (gesturePreferences?.swipeVolumePercent
                                                ?: return@detectVerticalDragGestures)
                                        )
                                    }
                                }
                            }
                    )
                }
            }

            when (uiState) {
                is Playing -> {
                    var heightDp by rememberSaveable { mutableFloatStateOf(192.0f) }
                    val xPx = remember { Animatable(0.0f) }
                    val yPx = remember { Animatable(0.0f) }

                    SharedTransitionLayout {
                        AnimatedContent(currentPipIndex) { pipIndex ->
                            NormalVideoRow(
                                uiState.videoPlayers,
                                uiState.aspectRatios,
                                pipIndex,
                                this
                            )

                            if (pipIndex >= 0) {
                                val player = uiState.videoPlayers[pipIndex]
                                val aspectRatio = uiState.aspectRatios[pipIndex]

                                val coroutineScope = rememberCoroutineScope()

                                PipVideo(
                                    player = player,
                                    aspectRatio = aspectRatio,
                                    offset = { IntOffset(xPx.value.toInt(), yPx.value.toInt()) },
                                    height = heightDp.dp,
                                    animatedVisibilityScope = this,
                                    pointInputKey = windowMetrics,
                                    lock = lock,
                                    onDoubleTap = {
                                        currentPipIndex =
                                            if (currentPipIndex + 1 >= uiState.videoPlayers.size) -1
                                            else currentPipIndex + 1
                                    },
                                    onZoom = {
                                        heightDp = (heightDp * it).coerceIn(72.0f, 256.0f)
                                    },
                                    onDrag = {
                                        coroutineScope.launch {
                                            xPx.snapTo(xPx.value + it.x)
                                            yPx.snapTo(yPx.value + it.y)
                                        }
                                    },
                                    onDragFinished = {
                                        val heightPx = heightDp.dp.roundToPx().toFloat()
                                        val widthPx = aspectRatio * heightPx
                                        val maxXPx =
                                            windowMetrics.bounds.width() - widthPx
                                        val maxYPx =
                                            windowMetrics.bounds.height() - heightPx

                                        val left = xPx.value.coerceIn(0.0f, maxXPx)
                                        val top = yPx.value.coerceIn(0.0f, maxYPx)
                                        val right = maxXPx - left
                                        val bottom = maxYPx - top
                                        val min = minOf(left, top, right, bottom)

                                        with(coroutineScope) {
                                            if (left == min) launch { xPx.animateTo(0.0f) }
                                            else if (right == min) launch { xPx.animateTo(maxXPx) }
                                            if (top == min) launch { yPx.animateTo(0.0f) }
                                            else if (bottom == min) launch { yPx.animateTo(maxYPx) }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = showSubtitle,
                        enter = slideIn { IntOffset(0, it.height) },
                        exit = slideOut { IntOffset(0, it.height) }
                    ) {
                        val raiseDp = remember { Animatable(72.0f) }
                        val shouldRaise = showOverlay && !lock
                        LaunchedEffect(shouldRaise) {
                            if (shouldRaise) raiseDp.animateTo(72.0f)
                            else raiseDp.animateTo(0.0f)
                        }
                        AndroidView(
                            factory = { context -> SubtitleView(context) },
                            update = { it.setCues(uiState.cues) },
                            modifier = Modifier
                                .fillMaxSize()
                                .offset { IntOffset(0, -raiseDp.value.dp.roundToPx()) }
                        )
                    }
                }

                is Error ->
                    ErrorPane(
                        message = uiState.message(LocalContext.current),
                        modifier = Modifier.fillMaxSize()
                    )

                else -> {}
            }

            val sideIndicatorModifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 4.dp)
                .padding(top = 18.dp, bottom = 12.dp)

            // volume indicator
            AnimatedVisibility(
                showVolumeBar,
                modifier = Modifier.align(Alignment.CenterStart),
                enter = slideIn { size -> IntOffset(-size.width, 0) },
                exit = slideOut { size -> IntOffset(-size.width, 0) }
            ) {
                Row {
                    Spacer(modifier = Modifier.width(32.dp))
                    VerticalSlider(
                        value = currentVolumePercent,
                        icon = Icons.AutoMirrored.Rounded.VolumeUp,
                        onValueChange = { },
                        modifier = sideIndicatorModifier
                    )
                }
            }

            // Brightness indicator
            AnimatedVisibility(
                showBrightnessBar,
                modifier = Modifier.align(Alignment.CenterEnd),
                enter = slideIn { size -> IntOffset(size.width, 0) },
                exit = slideOut { size -> IntOffset(size.width, 0) }
            ) {
                Row {
                    VerticalSlider(
                        value = currentBrightnessPercent,
                        icon = Icons.Rounded.BrightnessHigh,
                        onValueChange = { },
                        modifier = sideIndicatorModifier
                    )
                    Spacer(modifier = Modifier.width(32.dp))
                }
            }

            // Player control overlay
            AnimatedVisibility(showOverlay, enter = fadeIn(), exit = fadeOut()) {
                var timeoutResetSignal by remember { mutableStateOf(false) }
                if (playing?.isPlaying == true && !isSeeking && !showingModalMenu
                ) {
                    LaunchedEffect(timeoutResetSignal) {
                        delay(3000)
                        showOverlay = false
                    }
                }

                if (!lock) {
                    Column(
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f))
                            .pointerInput(Unit) {
                                detectTapGestures { showOverlay = false }
                            }
                    ) {
                        TopBar(
                            title = uiState.live?.liveRecordName
                                ?: stringResource(R.string.loading),
                            speedButtonEnabled = uiState.live?.liveStatus == LiveStatus.FINISHED,
                            closedCaptionButtonEnabled = uiState.live?.liveStatus == LiveStatus.FINISHED,
                            currentSpeed = (uiState as? PlayerCreated)?.requestedSpeed
                                ?: 1.0f,
                            onSpeed = {
                                showingModalMenu = false
                                onSpeed(it)
                            },
                            onSpeedButtonClicked = { showingModalMenu = true },
                            onSpeedMenuDismiss = { showingModalMenu = false },
                            onBackButtonClicked = onBackButtonClicked,
                            onLockButtonClicked = { lock = true },
                            onPipButtonClicked = {
                                timeoutResetSignal = !timeoutResetSignal
                                if (uiState is PlayerCreated) {
                                    currentPipIndex =
                                        if (currentPipIndex + 1 >= uiState.videoPlayers.size) -1
                                        else currentPipIndex + 1
                                }
                            },
                            onCcButtonClicked = { showSubtitle = !showSubtitle },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp)
                        )

                        CenterControl(
                            indicatorType = getIndicatorType(uiState),
                            onPlay = {
                                timeoutResetSignal = !timeoutResetSignal
                                onPlay()
                            },
                            onPause = {
                                timeoutResetSignal = !timeoutResetSignal
                                onPause()
                            },
                            onRetry = {
                                timeoutResetSignal = !timeoutResetSignal
                                onRetry()
                            },
                            onRewind = {
                                timeoutResetSignal = !timeoutResetSignal
                                onRewind()
                            },
                            onForward = {
                                timeoutResetSignal = !timeoutResetSignal
                                onForward()
                            }
                        )

                        BottomBar(
                            currentMillis = currentMillis,
                            totalMillis = totalMillis,
                            onSlide = {
                                isSeeking = true
                                seekMillis = it
                            },
                            onSlideFinished = {
                                isSeeking = false
                                onSeek(it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .height(72.dp)
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures { showOverlay = false }
                            }
                    ) {
                        // additionally adding a mask for contrast
                        Box(
                            modifier = Modifier
                                .padding(6.dp)
                                .clip(CircleShape)
                                .wrapContentSize()
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f))
                        ) {
                            OverlayButton(
                                Icons.Outlined.Lock,
                                modifier = Modifier.padding(18.dp),
                                onClick = { lock = false }
                            )
                        }
                    }
                }
            }

            SeekingIndicator(visible = isSeeking, seekMillis = seekMillis)
            SpeedupIndicator(visible = speedingUp)
        }
    }
}

@Composable
fun BoxScope.SpeedupIndicator(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible,
        modifier = modifier.align(Alignment.TopCenter),
        enter = slideIn { size -> IntOffset(0, -size.height) },
        exit = slideOut { size -> IntOffset(0, -size.height) }
    ) {
        Column {
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.surfaceContainer,
                    RoundedCornerShape(32.dp)
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.FastForward,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(28.dp)
                )
            }
        }
    }
}

@Composable
fun BoxScope.SeekingIndicator(
    visible: Boolean,
    seekMillis: Long,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible,
        modifier = modifier.align(Alignment.TopCenter),
        enter = slideIn { size -> IntOffset(0, -size.height) },
        exit = slideOut { size -> IntOffset(0, -size.height) }
    ) {
        Column {
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.surfaceContainer,
                    RoundedCornerShape(32.dp)
                )
            ) {
                val text = DateUtils.formatElapsedTime(seekMillis / 1000)
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun TopBar(
    title: String,
    currentSpeed: Float,
    speedButtonEnabled: Boolean,
    closedCaptionButtonEnabled: Boolean,
    onSpeed: (Float) -> Unit,
    onBackButtonClicked: () -> Unit,
    onLockButtonClicked: () -> Unit,
    onSpeedButtonClicked: () -> Unit,
    onSpeedMenuDismiss: () -> Unit,
    onPipButtonClicked: () -> Unit,
    onCcButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = modifier) {
        TitleRow(
            title = title,
            onBackButtonClicked = onBackButtonClicked,
            modifier = Modifier.weight(1.0f)
        )
        SecondaryControlRow(
            currentSpeed = currentSpeed,
            speedButtonEnabled = speedButtonEnabled,
            closedCaptionButtonEnabled = closedCaptionButtonEnabled,
            onSpeed = onSpeed,
            onLockButtonClicked = onLockButtonClicked,
            onPipButtonClicked = onPipButtonClicked,
            onSpeedButtonClicked = onSpeedButtonClicked,
            onSpeedMenuDismiss = onSpeedMenuDismiss,
            onCcButtonClicked = onCcButtonClicked,
            modifier = Modifier.weight(1.0f)
        )
    }
}

@Composable
private fun TitleRow(
    title: String,
    onBackButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        OverlayButton(
            Icons.AutoMirrored.Filled.ArrowBack,
            modifier = Modifier.padding(18.dp),
            onClick = onBackButtonClicked
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SecondaryControlRow(
    currentSpeed: Float,
    speedButtonEnabled: Boolean,
    closedCaptionButtonEnabled: Boolean,
    onLockButtonClicked: () -> Unit,
    onPipButtonClicked: () -> Unit,
    onSpeedButtonClicked: () -> Unit,
    onSpeedMenuDismiss: () -> Unit,
    onSpeed: (Float) -> Unit,
    onCcButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
        modifier = modifier
    ) {
        val buttonModifier = Modifier.padding(18.dp)
        var speedSelectorExpanded by rememberSaveable { mutableStateOf(false) }

        OverlayButton(
            Icons.Outlined.Lock,
            modifier = buttonModifier,
            onClick = onLockButtonClicked
        )
        OverlayButton(
            Icons.Outlined.PictureInPictureAlt,
            modifier = buttonModifier,
            onClick = onPipButtonClicked
        )
        if (speedButtonEnabled)
            Box {
                OverlayButton(
                    Icons.Outlined.Speed,
                    modifier = buttonModifier,
                    onClick = {
                        speedSelectorExpanded = true
                        onSpeedButtonClicked()
                    }
                )
                DropdownMenu(
                    expanded = speedSelectorExpanded,
                    onDismissRequest = {
                        speedSelectorExpanded = false
                        onSpeedMenuDismiss()
                    }) {
                    listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f).forEach {
                        DropdownMenuItem(
                            text = { Text(text = "${it}x") },
                            onClick = {
                                speedSelectorExpanded = false
                                onSpeed(it)
                            },
                            leadingIcon = {
                                RadioButton(
                                    selected = currentSpeed == it,
                                    onClick = {
                                        speedSelectorExpanded = false
                                        onSpeed(it)
                                    }
                                )
                            }
                        )
                    }
                }
            }
        if (closedCaptionButtonEnabled) {
            OverlayButton(
                Icons.Outlined.ClosedCaption,
                modifier = buttonModifier,
                onClick = onCcButtonClicked
            )
        }
    }
}

private enum class IndicatorType {
    LOADING,
    PLAY,
    PAUSE,
    RETRY
}

private fun getIndicatorType(uiState: PlayerUiState) =
    when (uiState) {
        is Playing -> {
            if (uiState.isPlaying) IndicatorType.PAUSE else IndicatorType.PLAY
        }

        is Error -> IndicatorType.RETRY

        else -> {
            IndicatorType.LOADING
        }
    }

@Composable
private fun CenterControl(
    indicatorType: IndicatorType,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onRetry: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        val buttonModifier = Modifier
            .padding(12.dp)
            .size(40.dp)

//      OverlayButton(Icons.Rounded.SkipPrevious, modifier = buttonModifier) { }
        OverlayButton(Icons.Rounded.FastRewind, modifier = buttonModifier, onClick = onRewind)
        AnimatedContent(indicatorType) {
            val indicatorModifier = Modifier
                .padding(8.dp)
                .size(48.dp)
            when (it) {
                IndicatorType.LOADING -> CircularProgressIndicator(
                    modifier = indicatorModifier,
                )

                IndicatorType.PLAY -> OverlayButton(
                    icon = Icons.Rounded.PlayArrow,
                    modifier = indicatorModifier,
                    onClick = onPlay
                )

                IndicatorType.PAUSE -> OverlayButton(
                    icon = Icons.Rounded.Pause,
                    modifier = indicatorModifier,
                    onClick = onPause
                )

                IndicatorType.RETRY -> OverlayButton(
                    icon = Icons.Rounded.Refresh,
                    modifier = indicatorModifier,
                    onClick = onRetry
                )
            }
        }
        OverlayButton(Icons.Rounded.FastForward, modifier = buttonModifier, onClick = onForward)
//      OverlayButton(Icons.Rounded.SkipNext, modifier = buttonModifier) { }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomBar(
    currentMillis: Long,
    totalMillis: Long,
    onSlide: (Long) -> Unit,
    onSlideFinished: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSliding by remember { mutableStateOf(false) }
    var displayValue by remember { mutableFloatStateOf(0f) }
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        val text = "${DateUtils.formatElapsedTime(currentMillis / 1000)} - ${
            DateUtils.formatElapsedTime(totalMillis / 1000)
        }"

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
        )

        val value = if (isSliding) {
            displayValue
        } else if (totalMillis == 0L) {
            0f
        } else {
            currentMillis / totalMillis.toFloat()
        }

        Slider(
            value = value,
            onValueChange = {
                isSliding = true
                displayValue = it
                onSlide((totalMillis * it).toLong())
            },
            onValueChangeFinished = {
                isSliding = false
                onSlideFinished((totalMillis * displayValue).toLong())
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    colors = SliderDefaults.colors(),
                    enabled = true,
                    sliderState = sliderState,
                    thumbTrackGapSize = 3.dp,
                    modifier = Modifier.height(12.dp)
                )
            },
            modifier = Modifier.height(24.dp)
        )
    }
}

@Composable
private fun OverlayButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick, role = Role.Button) then modifier
    )
}

