package com.example.familymemory.util

import android.content.Context
import androidx.recyclerview.widget.LinearSmoothScroller

class SnapToCenterSmoothScroller(context: Context) : LinearSmoothScroller(context) {
    override fun getVerticalSnapPreference(): Int {
        return SNAP_TO_START // Adjust this based on your desired snapping behavior
    }

    override fun getHorizontalSnapPreference(): Int {
        return SNAP_TO_START // Adjust this based on your desired snapping behavior
    }
}