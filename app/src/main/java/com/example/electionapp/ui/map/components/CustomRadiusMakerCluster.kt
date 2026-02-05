package com.example.electionapp.ui.map.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.bonuspack.clustering.StaticCluster
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * A custom clusterer that changes color based on the density of markers.
 * - Green: Low density (< 10)
 * - Orange: Medium density (10 - 29)
 * - Red: High density (30+)
 */
class CustomRadiusMarkerCluster(context: Context, private val mapView: MapView) : RadiusMarkerClusterer(context) {

    init {
        // Fix: Use the setter method instead of accessing the field directly
        setRadius(100)
    }

    override fun buildClusterMarker(cluster: StaticCluster, mapView: MapView): Marker {
        val m = Marker(mapView)
        m.position = cluster.position
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

        // Color logic based on cluster size
        val clusterColor = when {
            cluster.size < 10 -> Color.parseColor("#4CAF50") // Green
            cluster.size < 30 -> Color.parseColor("#FF9800") // Orange
            else -> Color.parseColor("#F44336")              // Red
        }

        val bitmap = createClusterBitmap(cluster.size.toString(), clusterColor)
        m.icon = BitmapDrawable(mapView.context.resources, bitmap)

        m.setOnMarkerClickListener { _, _ ->
            mapView.controller.animateTo(cluster.position)
            mapView.controller.zoomIn()
            true
        }

        return m
    }

    private fun createClusterBitmap(text: String, color: Int): Bitmap {
        val density = mapView.context.resources.displayMetrics.density
        val size = (44 * density).toInt()
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background circle
        paint.color = color
        canvas.drawCircle(size / 2f, size / 2f, size / 2.1f, paint)

        // White ring
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2 * density
        canvas.drawCircle(size / 2f, size / 2f, size / 2.1f, paint)

        // Text
        paint.style = Paint.Style.FILL
        paint.textSize = 14f * density
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textAlign = Paint.Align.CENTER

        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val textY = (size / 2f) + (bounds.height() / 2f)
        canvas.drawText(text, size / 2f, textY, paint)

        return bitmap
    }
}