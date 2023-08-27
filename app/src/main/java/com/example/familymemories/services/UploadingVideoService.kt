package com.example.familymemory.services

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.familymemory.R
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*

class UploadingVideoService : Service() {
    private val CHANNEL_ID = "running channel"
    val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var videoUploadingProgress: ProgressNotificationBuilder

    //
    override fun onBind(p0: Intent?): IBinder?  //IBinder , one active component and multiple instances connect to this component
    {
        return null
    }

    // commands that activity can send to the service
    // example when we start the service
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.toString() -> {
                val videoTitle = intent.getStringExtra("videoTitle").toString()
                val videoUri = intent.data
                start(videoUri, videoTitle)
            }
            Actions.STOP.toString() -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)

    }

    // if we wanna make this foreground service it must come with persisance notification
    private fun start(videoUri: Uri?, viedoTitle: String) {
        val notificationTitle = "Uploading $viedoTitle"
        ContextCompat.getSystemService(applicationContext, NotificationManager::class.java)?.let {
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationTitle)
                .build()// create the same notification again with the new content , and call startForeGround fun afterwards
            // id = refers to specefic notifaction so if you want to update notification
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationId =
                System.currentTimeMillis().toInt() // to generate a unique id for the notification
            videoUploadingProgress =
                ProgressNotificationBuilder(applicationContext, CHANNEL_ID, notificationId)
//            startForeground(1, notification)// 0 won't work

            scope.launch {
                Log.d("HODA", "NEW scope created")
                uploadVideoToFirebaseStorage(videoUri, viedoTitle, notificationId)
            }

        }
    }

    enum class Actions {
        START,
        STOP,
    }

    private suspend fun uploadVideoToFirebaseStorage(
        videoUri: Uri?,
        viedoTitle: String,
        notificationId: Int,
    ) {
        var timeStamp = "" + System.currentTimeMillis()
        val filePathAndName = "Videos/video_$timeStamp"
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(videoUri!!).addOnProgressListener { taskSnapshot ->
            val progress =
                (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            videoUploadingProgress.showNotification("Uploading $viedoTitle ", " ", progress)

        }.addOnSuccessListener { taskSnapShot ->
            val uriTaskUrl = taskSnapShot.storage.downloadUrl
            while (!uriTaskUrl.isSuccessful) {
            }
            val downloadUri = uriTaskUrl.result
            if (uriTaskUrl.isSuccessful) {
                val hashMap = HashMap<String, String>()
                hashMap.put("id", timeStamp)
                hashMap.put("title", viedoTitle.trim())
                hashMap.put("url", downloadUri.toString())

                val dbRef = FirebaseDatabase.getInstance().getReference("videos")
                dbRef.child(timeStamp)
                    .setValue(hashMap)
                    .addOnSuccessListener {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                applicationContext,
                                "Video Uploaded Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            videoUploadingProgress.showNotification(
                                "Video Uploaded",
                                "video uploaded successfully",
                                notificationId
                            )
                            videoUploadingProgress.dismissNotification()
                            stopSelf()
                            cancel()
                        }
                    }.addOnFailureListener {
                        CoroutineScope(Dispatchers.Main).launch {
                            Log.d("HODA", "failed to upload video \n ${it.message} ")
                            Toast.makeText(
                                applicationContext,
                                "An Error Occurred !${it.message}",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            videoUploadingProgress.showNotification(
                                "Uploading Failed",
                                "Failed to upload video",
                                notificationId
                            )

                            stopSelf()
                            cancel()
                        }
                    }.addOnFailureListener {
                        CoroutineScope(Dispatchers.Main).launch {
                            Log.d("HODA", "URI taskURL isn't successfun \n ${it.message} ")
                            // uploading to storage failed
                            Toast.makeText(
                                applicationContext,
                                "Error Uploading To Storage ${it.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            videoUploadingProgress.showNotification(
                                "Uploading Failed",
                                it.message.toString(),
                                notificationId
                            )
                            stopSelf()
                            cancel()
                        }
                    }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}