package zip.sora.ulearntec.playback

import android.content.Context
import android.os.Handler
import androidx.annotation.OptIn
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.MediaClock
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.metadata.MetadataOutput
import androidx.media3.exoplayer.text.TextOutput
import androidx.media3.exoplayer.video.MediaCodecVideoRenderer
import androidx.media3.exoplayer.video.VideoRendererEventListener

@OptIn(UnstableApi::class)
class ClockSyncedVideoRendererFactory(
    private val context: Context,
    private val getPosition: () -> Long,
) : RenderersFactory {
    override fun createRenderers(
        eventHandler: Handler,
        videoRendererEventListener: VideoRendererEventListener,
        audioRendererEventListener: AudioRendererEventListener,
        textRendererOutput: TextOutput,
        metadataRendererOutput: MetadataOutput
    ): Array<Renderer> = arrayOf(
        object : MediaCodecVideoRenderer(
            Builder(context).apply {
                setMediaCodecSelector(MediaCodecSelector.DEFAULT)
                setAllowedJoiningTimeMs(0)
                setEventHandler(eventHandler)
                setEventListener(videoRendererEventListener)
                setMaxDroppedFramesToNotify(0)
            }
        ) {
            override fun getMediaClock(): MediaClock {
                return object : MediaClock {
                    override fun getPositionUs() = getPosition()

                    private var playbackParameters: PlaybackParameters =
                        PlaybackParameters(1.0f, 1.0f)

                    override fun setPlaybackParameters(params: PlaybackParameters) {
                        playbackParameters = params
                    }

                    override fun getPlaybackParameters() = playbackParameters
                }
            }
        }
    )
}