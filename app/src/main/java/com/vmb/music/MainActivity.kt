package com.vmb.music

import android.content.ComponentName
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.common.util.concurrent.ListenableFuture
import com.vmb.music.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val tracks = mutableListOf<Track>()
    private lateinit var adapter: TrackAdapter
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val picker = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isEmpty()) return@registerForActivityResult

        uris.forEach { uri ->
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) { }

            val uriText = uri.toString()
            if (tracks.none { it.uri == uriText }) {
                tracks += Track(uriText, displayName(uri))
            }
        }
        persistLibrary()
        refreshUi()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.bg)

        loadLibrary()
        adapter = TrackAdapter(tracks) { index -> playIndex(index) }
        binding.trackList.layoutManager = LinearLayoutManager(this)
        binding.trackList.adapter = adapter

        binding.importButton.setOnClickListener { picker.launch(arrayOf("audio/mpeg", "audio/*")) }
        binding.emptyImportButton.setOnClickListener { picker.launch(arrayOf("audio/mpeg", "audio/*")) }
        binding.playPause.setOnClickListener {
            controller?.let { if (it.isPlaying) it.pause() else it.play() }
        }
        binding.next.setOnClickListener { controller?.seekToNextMediaItem() }
        binding.previous.setOnClickListener { controller?.seekToPreviousMediaItem() }

        connectController()
        refreshUi()
    }

    private fun connectController() {
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, token).buildAsync()
        controllerFuture?.addListener({
            try {
                controller = controllerFuture?.get()
                controller?.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) = updatePlayerUi()
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = updatePlayerUi()
                    override fun onPlaybackStateChanged(playbackState: Int) = updatePlayerUi()
                })
                updatePlayerUi()
            } catch (_: Exception) { }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun playIndex(index: Int) {
        val c = controller ?: return
        val items = tracks.map {
            MediaItem.Builder()
                .setUri(Uri.parse(it.uri))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(it.title)
                        .setArtist("VMB Music")
                        .build()
                )
                .build()
        }
        c.setMediaItems(items, index, 0L)
        c.prepare()
        c.play()
    }

    private fun updatePlayerUi() {
        runOnUiThread {
            val c = controller
            val item = c?.currentMediaItem
            val title = item?.mediaMetadata?.title?.toString()
            binding.miniPlayer.visibility = if (item == null) View.GONE else View.VISIBLE
            binding.nowPlayingTitle.text = title ?: "Nenhuma música"
            binding.nowPlayingSubtitle.text = if (c?.isPlaying == true) "Tocando agora" else "Pausado"
            binding.playPause.setImageResource(
                if (c?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
        }
    }

    private fun refreshUi() {
        if (::adapter.isInitialized) adapter.refresh()
        val empty = tracks.isEmpty()
        binding.emptyState.visibility = if (empty) View.VISIBLE else View.GONE
        binding.trackList.visibility = if (empty) View.GONE else View.VISIBLE
        binding.libraryCount.text = "${tracks.size} música${if (tracks.size == 1) "" else "s"}"
    }

    private fun displayName(uri: Uri): String {
        var name = "Música MP3"
        val cursor: Cursor? = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = it.getString(idx)
            }
        }
        return name.removeSuffix(".mp3").removeSuffix(".MP3")
    }

    private fun persistLibrary() {
        val encoded = tracks.map { "${it.uri}\u0001${it.title}" }.toSet()
        getSharedPreferences("vmb_music", MODE_PRIVATE)
            .edit().putStringSet("library", encoded).apply()
    }

    private fun loadLibrary() {
        val stored = getSharedPreferences("vmb_music", MODE_PRIVATE)
            .getStringSet("library", emptySet()) ?: emptySet()
        tracks.clear()
        stored.forEach { value ->
            val parts = value.split("\u0001", limit = 2)
            if (parts.size == 2) tracks += Track(parts[0], parts[1])
        }
        tracks.sortBy { it.title.lowercase() }
    }

    override fun onDestroy() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
        super.onDestroy()
    }
}
