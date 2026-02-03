package com.example.electionapp.ui.map.components

import android.content.Context
import android.graphics.Color
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer

class CustomRadiusMarkerClusterer(
    context: Context
) : RadiusMarkerClusterer(context) {

    init {
        val density = context.resources.displayMetrics.density
        mTextPaint.color = Color.WHITE
        mTextPaint.textSize = 18f * density
        mTextPaint.isFakeBoldText = true
    }
}
