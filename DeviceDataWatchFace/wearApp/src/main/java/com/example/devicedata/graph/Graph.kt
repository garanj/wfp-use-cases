package com.example.devicedata.graph

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.createBitmap

/**
 * Draws a basic graph from the readings from the bluetooth device. Used as a trivial example of
 * visualizing the data that can then be displayed as an image via the complication data source.
 */
fun createGraph(
    width: Int,
    height: Int,
    points: List<Double>
): Bitmap {
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply {
        isAntiAlias = true
    }
    val whiteStrokePaint = Paint(paint).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    val whiteMidStrokePaint = Paint(paint).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    val whiteThickStrokePaint = Paint(paint).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    canvas.drawRoundRect(
        RectF(0f, 0f, width.toFloat(), height.toFloat()),
        5f,
        5f,
        whiteMidStrokePaint
    )

    val hInterval = height.toFloat() / 10
    val wInterval = width.toFloat() / 10

    for (i in 0 until 10) {
        canvas.drawLine(0f, i * hInterval, width.toFloat(), i * hInterval, whiteStrokePaint)
        canvas.drawLine(i * wInterval, 0f, i * wInterval, height.toFloat(), whiteStrokePaint)
    }
    val pInterval = width.toFloat() / points.size
    for (i in 0 until (points.size - 1)) {
        canvas.drawLine(
            i * pInterval,
            (points[i] * height).toFloat(),
            (i + 1) * pInterval,
            (points[i + 1] * height).toFloat(),
            whiteThickStrokePaint
        )
    }

    return bitmap
}
