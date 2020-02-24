package com.example.planegame.plane

import android.graphics.*
import android.util.Log
import com.example.planegame.AppHelper
import com.example.planegame.BitmapCache
import com.example.planegame.MathUtils

open class Plane {
    var x = 0f
    var y = 0f
    protected var speed = 1f
    var hp = 1
    private var isEnableOut = false // 是否允许飞出边界
    //    private var bound = Rect()
    private var bmpPlane: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

    /**
     * 根据文件名加载飞机的位图，加载后的位图会放在map集合中缓存起来，以便下回取出
     */
    open fun setPlaneImage(img: String) {
        this.bmpPlane = BitmapCache.loadBitmap(img)
    }

    fun getPlaneImage(): Bitmap {
        return bmpPlane
    }

    fun setLocation(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun setEnableOut(isEnableOut: Boolean) {
        this.isEnableOut = isEnableOut
    }

    fun moveLeft() {
        this.x -= this.speed
        if (this.x <= 0) this.x = 0f
    }

    fun moveRight() {
        this.x += this.speed
        if (this.x >= (AppHelper.bound.right - bmpPlane.width)) {
            this.x = (AppHelper.bound.right - bmpPlane.width).toFloat()
        }
    }

    fun moveTop(): Boolean {
        this.y -= this.speed
        val topSide =
            if (isEnableOut) AppHelper.bound.top - bmpPlane.height else AppHelper.bound.top
        if (this.y <= topSide) {
            this.y = topSide.toFloat()
            return false
        }
        return true
    }

    fun moveBottom(): Boolean {
        this.y += this.speed
        val bottomSide =
            if (isEnableOut) AppHelper.bound.bottom else AppHelper.bound.bottom - bmpPlane.height
        if (this.y > bottomSide) {
            this.y = bottomSide.toFloat()
            return false
        }
        return true
    }

    open fun draw(canvas: Canvas?) {
        if (canvas == null) return
        canvas.drawBitmap(bmpPlane, x, y, null)
    }
}