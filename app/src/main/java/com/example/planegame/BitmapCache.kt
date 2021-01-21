package com.example.planegame

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.util.LruCache
import androidx.annotation.NonNull

/**
 * 位图缓存
 */
object BitmapCache {
    private const val SCALE = 1080 / 720
    private val mapCache = LruCache<String, Bitmap>(32)

    fun loadBitmap(@NonNull imgUrl: String): Bitmap {
        // 如果位图没有缓存过的话，加载位图并缩放大小到合适的比例
        return if (mapCache[imgUrl] == null) {
            // 加载位图
            val bmp = BitmapFactory.decodeStream(MyApp.context.assets.open(imgUrl))
            // 对位图按比例缩放
            val bmpTemp = Bitmap.createBitmap(
                bmp.width * SCALE, bmp.height * SCALE,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bmpTemp)
            canvas.drawBitmap(
                bmp,
                Rect(0, 0, bmp.width, bmp.height),
                Rect(0, 0, bmpTemp.width, bmpTemp.height), null
            )
            // 缓存位图
            mapCache.put(imgUrl, bmpTemp)
            bmpTemp
        } else
            mapCache[imgUrl]!!
    }
}