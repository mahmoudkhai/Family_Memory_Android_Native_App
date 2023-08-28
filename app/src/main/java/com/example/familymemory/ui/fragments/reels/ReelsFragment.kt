package com.example.familymemory.ui.fragments.reels

import android.Manifest
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.example.familymemory.R
import com.example.familymemory.data.VideoItem
import com.example.familymemory.databinding.FragmentReelsBinding
import com.example.familymemory.ui.fragments.AddVideo
import com.example.familymemory.util.NetworkStatus.networkStateFlow
import com.example.familymemory.util.SnapToCenterSmoothScroller
import com.example.familymemory.util.SpeedyLinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.tasks.await
import java.io.IOException


class ReelsFragment : Fragment(), AddVideo {
    private lateinit var binding: FragmentReelsBinding
    private lateinit var navController: NavController
    private var adapter: ReelsAdapter? = null
    private lateinit var firebaseDatabaseRef: DatabaseReference
    private lateinit var storageRef: FirebaseStorage
    val REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 1
    private lateinit var scope: CoroutineScope
    private lateinit var player: ExoPlayer


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentReelsBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()
        storageRef = FirebaseStorage.getInstance()
        firebaseDatabaseRef = Firebase.database.getReference("videos")

        player = ExoPlayer.Builder(requireContext()).setLoadControl(createLoadControl()).build()

        binding.recyclerView.apply {
            val layoutManager =
                SpeedyLinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            this.layoutManager = layoutManager
            // to scroll smoothly to the nearest next item.
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val snapHelper = recyclerView.onFlingListener as SnapHelper
                        val snapView = snapHelper.findSnapView(layoutManager)
                        val targetPosition = snapView?.let {
                            layoutManager.getPosition(it)
                        } ?: RecyclerView.NO_POSITION

                        val smoothScroller = SnapToCenterSmoothScroller(recyclerView.context)
                        smoothScroller.targetPosition = targetPosition
                        layoutManager.startSmoothScroll(smoothScroller)
                    }
                }
            })
        }

        lifecycleScope.launch {
            requireContext().networkStateFlow().collectLatest { isNetworkConnected ->
                if (isNetworkConnected) {
                    showErrorLayout(false)
                    val options = FirebaseRecyclerOptions.Builder<VideoItem>()
                        .setQuery(firebaseDatabaseRef, VideoItem::class.java)
                        .build()
                    // initializing the adapter.
                    adapter = ReelsAdapter(
                        options, this@ReelsFragment, player, requireContext()
                    )
                    adapter?.startListening()
                    // attaching adapter to recycler view
                    binding.recyclerView.apply {
                        adapter = this@ReelsFragment.adapter
                    }

                    Log.d(
                        "Mahmoud",
                        "OPTIONS =  ${options}"
                    )
                } else {
                    showErrorLayout(true)
                }
            }
        }


        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerView)

        binding.searchTV.setOnClickListener {
            navController.navigate(R.id.searchFragment)
        }
    }

    @androidx.media3.common.util.UnstableApi
    private fun createLoadControl(): DefaultLoadControl {
        return DefaultLoadControl.Builder()
            .setTargetBufferBytes(
                6 * 1024 * 1024
            )
            .setBufferDurationsMs(
                1 * 1000,  // minimum buffer size
                1 * 1000,          // maximum buffer size
                1 * 1000,          // buffer for playback duration
                1 * 1000           // buffer for playback after rebuffering
            )
            .build()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        player.apply {
            stop()
            clearMediaItems()
            release()
        }
    }


    private fun requestWriteExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission granted

            } else {
                // permission denied
                Toast.makeText(
                    requireContext(),
                    "You can't download video without permission",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        adapter?.stopListening()
        super.onStop()
    }

    override fun navigateToUploadVideoFragment() {
        navController.navigate(R.id.uploadVideoFragment)
    }

    override fun deleteVideo(videoUrl: String, videoId: String?) {

        AlertDialog.Builder(requireContext()).setTitle("Delete")
            .setMessage("Are you sure to Delete this video ?")
            .setPositiveButton("Yes") { dialog, which ->
                scope = CoroutineScope(Dispatchers.IO)
                scope.launch {
                    confirmDeleteVideo(videoUrl, videoId!!)
                    cancel()
                }
            }
            .setNegativeButton("No") { dialog, which -> dialog.dismiss() }
            .show()
    }

    private suspend fun confirmDeleteVideo(videoUrl: String, videoId: String): Boolean {
        return try {
            // delete from firebase database
            storageRef.getReferenceFromUrl(videoUrl).delete().await()
            firebaseDatabaseRef.child(videoId).removeValue().await()
            true
        } catch (e: IOException) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireActivity(),
                    "No Internet connection",
                    Toast.LENGTH_LONG
                ).show()
                false
            }
        } catch (e: java.lang.Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireActivity(),
                    "An Error Occurred while removing: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
        }
    }

    override fun downloadVideo(videoUrl: String) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            val videoRef = storageRef.getReferenceFromUrl(videoUrl)
            videoRef.metadata.addOnSuccessListener { videoData ->
                val fileName = videoData.name
                val filetype = videoData.contentType // MP4
                val fileDirectory = Environment.DIRECTORY_DOWNLOADS // save video in downloads

                val downloadManager =
                    requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                val uri = Uri.parse(videoUrl)

                //download request + notification when downloaded
                val request = DownloadManager.Request(uri).setDestinationInExternalPublicDir(
                    fileDirectory.toString(), "fileName.mp4"
                )
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                downloadManager.enqueue(request)
            }.addOnFailureListener {
                Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
            }
        } else {
            requestWriteExternalStoragePermission()
        }
    }

    override fun showErrorLayout(error: Boolean) {
        if (error) {
            Log.d(
                "Mahmoud", "Showing Error }"
            )
            binding.recyclerView.visibility = View.GONE
            binding.searchTV.visibility = View.GONE
            binding.includedErrorLayout.noWifiImg.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.searchTV.visibility = View.VISIBLE
            binding.includedErrorLayout.noWifiImg.visibility = View.GONE
        }
    }
}