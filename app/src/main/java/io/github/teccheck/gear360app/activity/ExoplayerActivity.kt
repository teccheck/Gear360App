package io.github.teccheck.gear360app.activity

import android.os.Bundle
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.EventLogger
import io.github.teccheck.gear360app.R
import io.github.teccheck.gear360app.player.Extractor

private const val TAG = "ExoplayerActivity"

class ExoplayerActivity : BaseActivity() {

    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exoplayer)

        setupBackButton()

        val playerView = findViewById<StyledPlayerView>(R.id.player_view)
        playerView.setShowBuffering(StyledPlayerView.SHOW_BUFFERING_ALWAYS)

        val mediaSourceFactory = DefaultMediaSourceFactory(this, Extractor.Factory())

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(200, 400, 0, 0)
            .build()

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .build()

        playerView.player = player

        player.addAnalyticsListener(EventLogger())
        player.addMediaItem(MediaItem.fromUri("http://192.168.107.1:7679/livestream_high.avi"))
        player.prepare()
        player.play()
    }

    override fun onDestroy() {
        player.stop()
        player.release()
        super.onDestroy()
    }
}