package com.example.planegame.bomb

import android.graphics.Canvas
import com.example.planegame.AppHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 爆炸管理
 */
class BombManager private constructor() {
    private val bombList = mutableListOf<Bomb>()
    private lateinit var executors: ExecutorService

    private object Holder {
        const val DELAY = 80L
        val instance = BombManager()
    }

    companion object {
        fun getInst() = Holder.instance
        fun init() = getInst().apply { init() }
    }

    private fun init() {
        executors = Executors.newSingleThreadExecutor()
        executors.submit {
            while (AppHelper.isRunning) {
                if (AppHelper.isPause) continue
                bombList.filter { it.isPlaying }
                    .forEach { it.loopFrame() }
                Thread.sleep(Holder.DELAY)
            }
        }
    }

    fun release() {
        executors.shutdown()
    }

    /**
     * 生成一个对象
     * @param x：爆炸中心X
     * @param y: 爆炸中心Y
     * @param radius:爆炸半径
     */
    fun obtain(x: Float, y: Float, radius: Float = 100f) {
        var bomb = bombList.find { !it.isPlaying }
        if (bomb == null) {
            bomb = Bomb()
            bombList += bomb
        }
        bomb.play(x, y, radius)
    }

    fun drawAll(canvas: Canvas?) {
        if (canvas == null) return
        bombList.filter { it.isPlaying }.forEach { it.draw(canvas) }
    }

}