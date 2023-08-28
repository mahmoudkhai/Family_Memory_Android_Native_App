package com.example.familymemory.ui.fragments.reels

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.common.*
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.RecyclerView
import com.example.familymemory.data.VideoItem
import com.example.familymemory.databinding.VideoItemBinding
import com.example.familymemory.ui.fragments.AddVideo
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseError

class ReelsAdapter(
    private val options: FirebaseRecyclerOptions<VideoItem?>,
    private val subscriber: AddVideo,
    private var videoPlayer: ExoPlayer,
    private val context: Context,
) : FirebaseRecyclerAdapter<VideoItem?, ReelsAdapter.PlayerViewHolder>(options) {

    override fun onError(error: DatabaseError) {
        super.onError(error)
        subscriber.showErrorLayout(true)
        Log.d(
            "Mahmoud",
            "ON ERROR CALLED  msg = ${error.message} \n details ${error.details}"
        )
    }

    override fun onViewRecycled(holder: PlayerViewHolder) {
        Log.d(
            "Mahmoud",
            "onViewRecycled called"
        )
        super.onViewRecycled(holder)
        /**
         * Setting holder.playerView.visibility = View.GONE is used to hide the PlayerView instance
         * in the view holder when it is being recycled.
         * By default, the PlayerView instance is set to VISIBLE in the view holder,
         * which means that it will still be visible on the screen
         * even if the view holder is no longer being used to display data.
         * This can cause unexpected behavior if the view holder is reused for a different data item,
         * because the PlayerView instance may still be visible even though it is not playing any media.
         */
//        val position = holder.absoluteAdapterPosition
//        holder.binding.playerView.visibility = View.GONE
//        videoPlayer.stop()
//         holder.binding.playerView.player = null
//        videoPlayer.clearMediaItems()
    }

    /**
     * If the getItemViewType() method returns the same value for two different data items,
     * then the same view holder will be reused to display both data items,
     * and the onViewRecycled() method will be called when the view holder is recycled.
     * If the getItemViewType() method returns a different value for two different data items,
     * then a new view holder will be created to display each data item,
     * and the onViewRecycled() method will not be called.
     */

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    inner class PlayerViewHolder(val binding: VideoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setExoPlayer(title: String?, url: String?, id: String, position: Int) {
            binding.apply {
                videoTitle.text = title!!
//                playerView.player = videoPlayer
            }

            videoPlayer.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    subscriber.showErrorLayout(true)
                    Log.d(
                        "Mahmoud",
                        "onPlayerError called ${error.cause} \n error code ${error.errorCode} , message ${error.message}"
                    )
                    binding.apply {
                        if (error.cause is HttpDataSource.HttpDataSourceException) {
                            if (error.cause is HttpDataSource.InvalidResponseCodeException) {
                                Log.d(
                                    "Mahmoud",
                                    "Error InvalidResponseCodeException ${error.cause?.message}"
                                )
                            } else {
                                Log.d(
                                    "Mahmoud",
                                    "Error HttpDataSourceException ${error.errorCodeName} \n  cause ${error.cause} code ${error.errorCode} "
                                )
                            }
                        } else {
                            Log.d(
                                "Mahmoud",
                                "Error cause ${error.cause} \n error code ${error.errorCode} , message ${error.message}"
                            )
                        }
                    }
                }

                // دى والى تحتها زى بعض
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    val bufferPercent = videoPlayer.bufferedPercentage
                    val bufferDuration = videoPlayer.totalBufferedDuration
                    if (playbackState == Player.STATE_ENDED) {
                        Log.d(
                            "Mahmoud", "STATE ENDED , " +
                                    "Buffered percentage = $bufferPercent for video ${title}"
                        )
                    } else if (playbackState == Player.STATE_IDLE) {
                        Log.d(
                            "Mahmoud",
                            "STATE IDLE ,Buffered percentage = $bufferPercent for video ${title}"
                        )
                    } else if (playbackState == Player.STATE_READY) {
                        Log.d(
                            "Mahmoud", "STATE READY Buffered percentage = $bufferPercent\n" +
                                    "total buffer Duration $bufferDuration for " +
                                    "${title}"
                        )
                    }
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    super.onPlayWhenReadyChanged(playWhenReady, reason)
                    if (playWhenReady) {
                        Log.d("Mahmoud", "Play when ready = true")
                        if (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM) {
                            Log.d(
                                "Mahmoud",
                                "reason = PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM \n " +
                                        "total buffer duration = ${videoPlayer.totalBufferedDuration}"
                            )
                        } else if (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_REMOTE) {
                            Log.d("Mahmoud", "reason = PLAY_WHEN_READY_CHANGE_REASON_REMOTE")
                        }
                    } else {
                        Log.d(
                            "Mahmoud", "Play when ready = false " +
                                    "\n total buffer duration = ${videoPlayer.totalBufferedDuration}"
                        )
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    if (isPlaying) {
                        Log.d("Mahmoud", "${title} isPlaying  = true")
                        // this changes playWhenReady to true
                        videoPlayer.play()
                    } else {
                        Log.d("Mahmoud", "${title} isPlaying  = false")
                        // changes playWhenReady to false
                        videoPlayer.pause()
                    }
                }

                override fun onEvents(player: Player, events: Player.Events) {
                    super.onEvents(player, events)
                    if (events.containsAny(Player.EVENT_PLAY_WHEN_READY_CHANGED)) {
                        Log.d("Mahmoud", "Event PLAY_WHEN_READY_CHANGED")

                    }
                }
            })
            binding.addVideofab.setOnClickListener {
                subscriber.navigateToUploadVideoFragment()
            }
            binding.downloadVideofab.setOnClickListener {
                subscriber.downloadVideo(url!!)
            }
            binding.deleteVideofab.setOnClickListener {
                subscriber.deleteVideo(videoUrl = url!!, id)
            }
            videoPlayer.apply {
                //VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                setMediaSource(createMediaSource(url!!))
                playWhenReady = false
                prepare()
            }
        }


        init {
            Log.d("Mahmoud", "NEW Holder created ")
            binding.playerView.apply {
                //Sets whether the playback controls are automatically shown when playback starts, pauses, ends, or fails.
                controllerAutoShow = true
                setShowFastForwardButton(false)
                setShowPreviousButton(false)
                setShowNextButton(false)
                setKeepContentOnPlayerReset(true)
                setShowShuffleButton(false)
                binding.playerView.player = videoPlayer
            }
        }

    }


    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int, videoItem: VideoItem) {

        holder.setExoPlayer(videoItem.title, videoItem.url, videoItem.id!! , position)

    }


    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun createMediaSource(url: String) =
        ProgressiveMediaSource.Factory(
            // For fetching data over HTTP and HTTPS
            //The DefaultDataSource.Factory layer adds in support for non-http(s) sources such as local files.
            DefaultDataSource.Factory(context)
        ).createMediaSource(
            createMediaItem(url)
        )

    private fun createMediaItem(url: String) = MediaItem.Builder()
        .setUri(Uri.parse(url))
        .setMimeType(MimeTypes.APPLICATION_MP4)
        .build()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        return PlayerViewHolder(
            VideoItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

}