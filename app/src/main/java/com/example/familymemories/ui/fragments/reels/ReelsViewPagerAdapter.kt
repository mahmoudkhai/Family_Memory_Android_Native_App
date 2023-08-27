package com.example.familymemory.ui.fragments.reels

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.upstream.DefaultAllocator
import androidx.recyclerview.widget.RecyclerView
import com.example.familymemory.data.VideoItem
import com.example.familymemory.databinding.VideoItemBinding
import com.example.familymemory.ui.fragments.AddVideo
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseError

class ReelsViewPagerAdapter(
    private val options: FirebaseRecyclerOptions<VideoItem?>,
    private val subscriber: AddVideo,
    private var player: ExoPlayer,
    private val context: Context,
) : FirebaseRecyclerAdapter<VideoItem?, ReelsViewPagerAdapter.PlayerViewHolder>(options) {

    override fun onError(error: DatabaseError) {
        super.onError(error)
        subscriber.showErrorLayout(true)
        Log.d(
            "Mahmoud",
            "ON ERROR CALLED  msg = ${error.message} \n details ${error.details}"
        )
    }

    override fun onViewDetachedFromWindow(holder: PlayerViewHolder) {
        super.onViewDetachedFromWindow(holder)
//        holder.apply {
//            binding.playerView.visibility = View.GONE
//            videoPlayer.stop()
//            holder.binding.playerView.player = null
//            videoPlayer.clearMediaItems()
//            videoPlayer.release()
//        }
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
//        holder.binding.playerView.visibility = View.GONE
        holder.videoPlayer.stop()
        holder.binding.playerView.player = null
        holder.videoPlayer.release()
        holder.videoPlayer.clearMediaItems()
    }

    /**
     * If the getItemViewType() method returns the same value for two different data items,
     * then the same view holder will be reused to display both data items,
     * and the onViewRecycled() method will be called when the view holder is recycled.
     * If the getItemViewType() method returns a different value for two different data items,
     * then a new view holder will be created to display each data item,
     * and the onViewRecycled() method will not be called.
     */
    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
//        Log.d(
//            "Mahmoud",
//            "getItemViewType $position"
//        )

    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    inner class PlayerViewHolder(val binding: VideoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val videoPlayer = player

        init {
            Log.d("Mahmoud", "NEW Holder created ")
            binding.playerView.apply {
                player = videoPlayer
                //Sets whether the playback controls are automatically shown when playback starts, pauses, ends, or fails.
                controllerAutoShow = true
                setShowFastForwardButton(false)
                setShowPreviousButton(false)
                setShowNextButton(false)
                setKeepContentOnPlayerReset(true)
                setShowShuffleButton(false)
            }
        }

    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun setBufferSize() {
        val bufferSize = 64 * 1024 // 64 KB
        val defaultAllocator = DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE)
        defaultAllocator.setTargetBufferSize(bufferSize)
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int, videoItem: VideoItem) {
        holder.binding.videoTitle.text = videoItem.title
        holder.videoPlayer.apply {
            //VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            setMediaSource(createMediaSource(videoItem.url!!))
            prepare()
        }
        holder.videoPlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                subscriber.showErrorLayout(true)
                Log.d(
                    "Mahmoud",
                    "onPlayerError called ${error.cause} \n error code ${error.errorCode} , message ${error.message}"
                )
                holder.binding.apply {
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
                val bufferPercent = holder.videoPlayer.bufferedPercentage
                val bufferDuration = holder.videoPlayer.totalBufferedDuration
                if (playbackState == Player.STATE_ENDED) {
                    Log.d(
                        "Mahmoud", "STATE ENDED , " +
                                "Buffered percentage = $bufferPercent for video ${videoItem.title}"
                    )
                } else if (playbackState == Player.STATE_IDLE) {
                    Log.d(
                        "Mahmoud",
                        "STATE IDLE ,Buffered percentage = $bufferPercent for video ${videoItem.title}"
                    )
                } else if (playbackState == Player.STATE_READY) {
                    Log.d(
                        "Mahmoud", "STATE READY Buffered percentage = $bufferPercent\n" +
                                "total buffer Duration $bufferDuration for " +
                                "${videoItem.title}"
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
                                    "total buffer duration = ${holder.videoPlayer.totalBufferedDuration}"
                        )
                    } else if (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_REMOTE) {
                        Log.d("Mahmoud", "reason = PLAY_WHEN_READY_CHANGE_REASON_REMOTE")
                    }
                } else {
                    Log.d(
                        "Mahmoud", "Play when ready = false " +
                                "\n total buffer duration = ${holder.videoPlayer.totalBufferedDuration}"
                    )
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    Log.d("Mahmoud", "${videoItem.title} isPlaying  = true")
                    // this changes playWhenReady to true
                    holder.videoPlayer.play()
                } else {
                    Log.d("Mahmoud", "${videoItem.title} isPlaying  = false")
                    // changes playWhenReady to false
                    holder.videoPlayer.pause()
                }
            }

            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                if (events.containsAny(Player.EVENT_PLAY_WHEN_READY_CHANGED)) {
                    Log.d("Mahmoud", "Event PLAY_WHEN_READY_CHANGED")

                }
            }
        })

        holder.binding.addVideofab.setOnClickListener {
            subscriber.navigateToUploadVideoFragment()
        }
        holder.binding.downloadVideofab.setOnClickListener {
            subscriber.downloadVideo(videoItem.url!!)
        }
        holder.binding.deleteVideofab.setOnClickListener {
            subscriber.deleteVideo(videoUrl = videoItem.url!!, videoItem.id!!)
        }
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