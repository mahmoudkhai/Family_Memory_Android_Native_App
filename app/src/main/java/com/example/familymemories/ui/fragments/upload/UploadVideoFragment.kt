package com.example.familymemory.ui.fragments.upload

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import com.example.familymemory.databinding.FragmentUploadVideoBinding
import com.example.familymemory.services.UploadingVideoService

class UploadVideoFragment : Fragment() {

    private lateinit var binding: FragmentUploadVideoBinding
    private lateinit var mediaController: MediaController
    private lateinit var progressDialog: ProgressDialog
    private var videoUri: Uri? = null
    final val PICK_GELLARY_VIDEO_CODE = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentUploadVideoBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Medai Controller
        mediaController = MediaController(requireContext())
        mediaController.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(mediaController)

        progressDialog = ProgressDialog(requireContext())
        progressDialog.apply {
            setTitle("Please Wait")
            setMessage("Uploading Video ...")
            setCanceledOnTouchOutside(false)
        }

//        var storageRef = storage.reference

        binding.uploadVideofab.setOnClickListener {
            pickVideoFromStorage()
        }
        binding.uploadVideo.setOnClickListener {
            if (binding.videoTitle.text.toString().isNullOrEmpty()) {
                binding.videoTitle.error = "Please enter video title"
            } else {
                Log.d("HODA", "creating intent ")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (videoUri != null) {
                        Intent(requireContext(), UploadingVideoService::class.java).also {
                            it.action = UploadingVideoService.Actions.START.toString()
                            it.putExtra("videoTitle", binding.videoTitle.text.toString())
                            it.data = videoUri
                            Log.d("HODA", "intent created")

                            requireContext().startService(it)
                        }
                    }
                }
            }
        }
    }


    private fun pickVideoFromStorage() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.type = "video/*"
        startActivityForResult(pickIntent, PICK_GELLARY_VIDEO_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        videoUri = data?.data
        binding.videoView.apply {
            visibility = View.VISIBLE
            setVideoURI(videoUri)
            requestFocus()
            start()
        }
    }

}