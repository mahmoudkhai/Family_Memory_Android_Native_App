package com.example.familymemory.ui.main

import android.Manifest
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Rational
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.familymemory.R
import com.example.familymemory.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    private val isPipSupported: Boolean by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        } else {
            false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotificationPermission()
        }else {
            Log.d("HODA", "< than Terrm ")
        }


        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

//        binding.bottomNavigation.setupWithNavController(navController)


    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPostNotificationPermission() {
        Log.d("HODA", "requestPostNotificationPermission: ")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatedPipParams(): PictureInPictureParams? {
        val rect = Rect(20, 20, 20, 20)
        return PictureInPictureParams.Builder()
            .setSourceRectHint(rect)
            .setAspectRatio(Rational(21, 9))
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

//        if (!isPipSupported) return else {
//            updatedPipParams()?.let { params ->
//                enterPictureInPictureMode(
//                    params
//                )
//                binding.bottomNavigation.visibility = View.GONE
//            }
//        }
    }
    /***
     *This method is called when the activity's picture-in-picture mode changes,
     *  either by entering or exiting picture-in-picture mode.
     */
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onPictureInPictureModeChanged(
//        isInPictureInPictureMode: Boolean,
//        newConfig: Configuration,
//    ) {
//        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
//        if (!isInPictureInPictureMode) {
////            binding.bottomNavigation.visibility = View.VISIBLE
//        }
//    }


}

