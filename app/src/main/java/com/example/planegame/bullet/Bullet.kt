package com.example.planegame.bullet

import android.graphics.*
import com.example.planegame.AppHelper
import com.example.planegame.BitmapCache
import com.example.planegame.MathUtils
import com.example.planegame.bomb.BombManager
import com.example.planegame.plane.EnemyPlaneManager
import com.example.planegame.plane.PlayerPlane

class Bullet {
    private lateinit var bmpBullet: Bitmap
    private var level = 1 // 子弹等级(范围1,2,3)
    private var x = 0f
    private var y = 0f
    private var speed = 3f
    private var direction = "up"
    var flagFree = true // 闲置标记，子弹飞出边界就算闲置，对象会被重置使用

    fun setBitmap(imgUrl: String) {
        this.bmpBullet = BitmapCache.loadBitmap(imgUrl)
    }

    fun getBitmap() = bmpBullet

    fun buildBmpLevel(level: Int) {
        this.level = level
        setBitmap("myBullet$level.png")
    }

    fun getDirection() = this.direction
    fun setDirection(direction: String) {
        this.direction = direction
    }

    fun getY() = this.y
    fun getX() = this.x
    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bmpBullet, x, y, null)
    }


    fun moveLeft() {
        x -= speed
        flagFree = (x <= 0)
    }

    fun moveRight() {
        x += speed
        flagFree = x >= AppHelper.bound.right
    }

    fun moveTop() {
        y -= speed
        flagFree = (y <= -bmpBullet.height)
    }

    fun moveBottom() {
        y += speed
        flagFree = (y >= AppHelper.bound.bottom)
    }

    fun setLocation(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    /**
     * 判断子弹是否击中敌机
     */
    fun hitEnemy() {
        val left = x.toInt()
        val right = (x + bmpBullet.width).toInt()
        val top = y.toInt()
        val bottom = (y + bmpBullet.height).toInt()

        val result = EnemyPlaneManager.getInst().isHit(Rect(left, top, right, bottom), level)
        if (result == null) return
        else {
            flagFree = true
            if ((result.x + result.y) >= 0) {
                BombManager.getInst().obtain(result.x.toFloat(), result.y.toFloat())
            }
        }
    }

    /**
     * 判断子弹是否击中玩家
     */
    fun hitPlayer() {
        val left = x.toInt()
        val right = (x + bmpBullet.width).toInt()
        val top = y.toInt()
        val bottom = (y + bmpBullet.height).toInt()
        val bulletRect = Rect(left, top, right, bottom)

        val isHit = MathUtils.cross(bulletRect, PlayerPlane.getInst().getRect())
        if (isHit) {
            flagFree = true
            PlayerPlane.hit()
        }
    }

    override fun toString(): String {
        return "${hashCode()} $x $y"
    }
}