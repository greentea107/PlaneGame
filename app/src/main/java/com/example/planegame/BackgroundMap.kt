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
    }

    fun draw(canvas: Canvas?, surfaceWidth: Int, surfaceHeight: Int) {
        if (canvas == null) return
        if (srcHeight == 0) {
            // 按Surface的高度转换成和原图等比例的高度
            srcHeight = bmpMap.width * surfaceHeight / surfaceWidth
            offy = bmpMap.height - srcHeight
        }
        // 原画中要显示的局部区域
        val srcRect = Rect(0, 0 + offy, bmpMap.width, srcHeight + offy)
        // 将原画的局部区域充满Surface的区域
        if (dstRect.isEmpty)
            dstRect = Rect(0, 0, surfaceWidth, surfaceHeight)
        canvas.drawBitmap(bmpMap, srcRect, dstRect, null)

        offy -= 1 // 原画的偏移

        // 当原画显示到顶部时拼接原画的尾部
        if (offy < 0) {
            val srcRect2 = Rect(0, bmpMap.height - (offy * -1), bmpMap.width, bmpMap.height)
            val dstRect2 = Rect(0, 0, surfaceWidth, surfaceWidth * srcRect2.height() / srcRect2.width())
            canvas.drawBitmap(bmpMap, srcRect2, dstRect2, null)

            if (offy <= -srcHeight)
                offy = bmpMap.height - srcHeight
        }
    }

    fun relase() {
        bmpMap.recycle()
    }
}