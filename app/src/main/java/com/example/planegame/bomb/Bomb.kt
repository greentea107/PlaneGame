package com.example.planegame.bomb

import android.graphics.*
import com.example.planegame.BitmapCache
import com.example.planegame.MyApp

/**
 * 爆炸效果
 */
class Bomb {
    private var bmpBombList = mutableListOf<Bitmap>()
    private var frame = 0
    private var bmpX = 0f
    private var bmpY = 0f
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 100f // 爆炸半径
    private var paintOval = Paint()
    var isPlaying = false

    init {
        // 从assets里读取图片文件到列表
        repeat(6) {
            bmpBombList.add(BitmapCache.loadBitmap("bomb_enemy_$it.png"))
        }
    }

    fun draw(canvas: Canvas) {
        // 绘制冲击波效果
        if (frame > 0) {
            paintOval.let {
                it.alpha = 255 / (frame + 1)
                val gradient = RadialGradient(
                    centerX, centerY, (frame * radius / 2) + 1,
                    intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, Color.WHITE), null,
                    Shader.TileMode.CLAMP
                )
                it.setShader(gradient)
            }
            canvas.drawCircle(centerX, centerY, frame * radius / 2, paintOval)
        }
        // 绘制爆炸图片
        val src = Rect(0, 0, bmpBombList[frame].width, bmpBombList[frame].height)
        val dst = Rect(
            bmpX.toInt(), bmpY.toInt(),
            (radius * 2 + bmpX).toInt(),
            (radius * 2 + bmpY).toInt()
        )
        canvas.drawBitmap(bmpBombList[frame], src, dst, null)
    }

    /**
     * 开始播放爆炸
     */
    fun play(x: Float, y: Float, radius: Float) {
        isPlaying = true
        frame = 0
        this.centerX = x
        this.centerY = y
        this.bmpX = x - radius
        this.bmpY = y - radius
    }

    fun loopFrame() {
        frame++
        if (frame >= bmpBombList.size) {
            frame = 0
            isPlaying = false
        }
    }
}