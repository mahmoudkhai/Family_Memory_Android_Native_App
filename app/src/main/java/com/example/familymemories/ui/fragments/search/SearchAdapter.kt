package com.example.familymemory.ui.fragments.search

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.HttpDataSource.HttpDataSourceException
import androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.RecyclerView
import com.example.familymemory.data.VideoItem
import com.example.familymemory.databinding.SearchItemBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import kotlinx.coroutines.*

/***
 * The FirebaseRecyclerAdapter class provides an easy-to-use API for binding Firebase data to a RecyclerView
 * and handling changes in real-time. It takes care of fetching data from the Firebase database
 * and displaying it in the RecyclerView,
 * as well as updating the RecyclerView when the data changes.
 */
class SearchAdapter(
    private val options: FirebaseRecyclerOptions<VideoItem?>,
    private val context: Context,
) :
    FirebaseRecyclerAdapter<VideoItem?, SearchAdapter.SearchItemsViewHolder>(options) {

    private var coroutinesJobs: MutableList<Job?> = mutableListOf()


    inner class SearchItemsViewHolder(private val binding: SearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
        fun setVideoData(videoItem: VideoItem) {

            val player = ExoPlayer.Builder(context).build()


            val mediItem = androidx.media3.common.MediaItem.Builder()
                .setUri(videoItem.url)
                .setMimeType(MimeTypes.APPLICATION_MP4)
                .build()
            // Provides one period that loads data from a Uri and extracted using an Extractor.
            // internally, the player needs MediaSource instances to play the content
            //for regular media files.
            val mediaSource = ProgressiveMediaSource.Factory(
                // For fetching data over HTTP and HTTPS
                DefaultDataSource.Factory(context)
            ).createMediaSource(mediItem)

            player.apply {
                playWhenReady = true
                // Adds a media source to the end of the playlist.
//                addMediaSource(mediaSource)
                setMediaSource(mediaSource)
                setMediaItem(mediItem)
                Log.d("Mahmoud", "isLoading = $isLoading ")
                addListener(object : Player.Listener {
                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int,
                    ) {
                        super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                        Log.d(
                            "Mahmoud", "Old Position in sec= ${
                                oldPosition.contentPositionMs * 1000
                            } , New Position = ${newPosition.contentPositionMs * 1000} , " +
                                    " New position in sec ${newPosition.positionMs*1000}"
                        )
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        if (error.cause is HttpDataSourceException) {
                            if (error.cause is InvalidResponseCodeException) {
                                Log.d(
                                    "Mahmoud",
                                    "Error InvalidResponseCodeException ${error.cause?.message}"
                                )
                            } else {
                                Log.d(
                                    "Mahmoud",
                                    "Error HttpDataSourceException ${error.errorCodeName} \n code ${error.errorCode} "
                                )
                            }
                        } else {
                            Log.d(
                                "Mahmoud",
                                "Error cause ${error.cause} \n error code ${error.errorCode} , message ${error.message}"
                            )
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        super.onIsPlayingChanged(isPlaying)
                        if (isPlaying) {
                            binding.videoProgressBar.visibility = View.GONE
                        } else {
                        }
                    }
                })
                prepare()
                play()
            }

            binding.searchVideoView.player = player
            Log.d(
                "Mahmoud",
                "Search Video title ${videoItem.title} Buffered percentage = ${binding.searchVideoView.player!!.bufferedPercentage} "
            )
            Log.d(
                "Mahmoud",
                "Search Video title ${videoItem.title}  total Buffered duration ${binding.searchVideoView.player!!.totalBufferedDuration} "
            )

//            binding.searchVideoView.setVideoURI(Uri.parse(videoItem.url))
//            binding.searchVideoView.setOnPreparedListener { mediaPlayer ->
//                mediaPlayer.setOnBufferingUpdateListener { mediaPlayer, bufferValue ->
//                    Log.d("Mahmoud", "Search Video title ${videoItem.title} Buffered $bufferValue ")
//                }
//                CoroutineScope(Dispatchers.IO).launch {
//                    val retriever = MediaMetadataRetriever()
//                    retriever.setDataSource(videoItem.url, HashMap<String, String>())
//                    val bitmap = retriever.frameAtTime
//
//                    // Set the bitmap as the background image of the VideoView on the main thread
//                    withContext(Dispatchers.Main) {
//                        binding.videoProgressBar.visibility = View.GONE
//                        binding.searchVideoView.background =
//                            BitmapDrawable(context.resources, bitmap)
//                    }
//                }
//                // Start playing the video when the user clicks on the VideoView
//                binding.searchVideoView.setOnTouchListener { view, motionEvent ->
//                    binding.startPlay.visibility = View.GONE
//                    binding.searchVideoView.background = null
//                    if (motionEvent.action == MotionEvent.ACTION_UP) { //which indicates that the user has released their touch on the VideoView.
//                        if (binding.searchVideoView.isPlaying)
//                            binding.searchVideoView.pause()
//                        else
//                            binding.searchVideoView.start()
//                        mediaPlayer.setVolume(0f, 0f)
//                    }
//                    mediaPlayer.start()
//                    val videoRatio =
//                        mediaPlayer.videoWidth / (mediaPlayer.videoHeight).toFloat()
//                    val screenRatio =
//                        binding.searchVideoView.width / binding.searchVideoView.height.toFloat()
//                    val scale = videoRatio / screenRatio
//                    if (scale >= 1f) binding.searchVideoView.scaleX =
//                        scale else binding.searchVideoView.scaleY = 1f / scale
//                    true
//                }
//            }
//            binding.searchVideoView.setOnCompletionListener { mp ->
//                mp.stop()
//
//            }


        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        coroutinesJobs.forEach { it?.cancel() }
//        player.release()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemsViewHolder {
        return SearchItemsViewHolder(
            SearchItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: SearchItemsViewHolder, position: Int, model: VideoItem) {
        holder.setVideoData(model)

    }

}