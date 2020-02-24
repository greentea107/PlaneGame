package com.example.planegame

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect

/**
 * 背景卷轴
 */
class BackgroundMap(context: Context) {
    private var bmpMap: Bitmap
    private var srcHeight = 0
    private var dstRect: Rect = Rect()
    private var offy = 0

    init {
        val inputStream = context.assets.open("map.jpg")
        bmpMap = BitmapFactory.decodeStream(inputStream)
        offy = bmpMap.height
    }

    fun draw(canvas: Canvas?, width: Int, height: Int) {
        if (canvas == null) return
        if (srcHeight == 0) {
            srcHeight = bmpMap.width * height / width
            offy = bmpMap.height - srcHeight
        }
        if ((dstRect.right - dstRect.left) == 0)
            dstRect = Rect(0, 0, width, height)

        val srcRect = Rect(0, 0 + offy, bmpMap.width, srcHeight + offy)
        canvas.drawBitmap(bmpMap, srcRect, dstRect, null)

        offy -= 1
        if (offy < 0) {
            val srcRect2 = Rect(0, bmpMap.height - (offy * -1), bmpMap.width, bmpMap.height)
            val dstRect2 = Rect(0, 0, width, width * srcRect2.height() / srcRect2.width())
            canvas.drawBitmap(bmpMap, srcRect2, dstRect2, null)

            if (offy <= -srcHeight)
                offy = bmpMap.height - srcHeight
        }

    }

    fun relase() {
        bmpMap.recycle()
    }
}