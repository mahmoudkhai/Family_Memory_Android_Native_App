package com.example.familymemory.ui.fragments

interface AddVideo {
    fun navigateToUploadVideoFragment()
    fun deleteVideo(videoUrl: String , videoId: String?)
    fun downloadVideo(videoUrl: String)
    fun showErrorLayout(error: Boolean = true)
}