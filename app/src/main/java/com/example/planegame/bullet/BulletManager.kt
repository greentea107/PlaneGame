package com.example.planegame.bullet

import android.graphics.Canvas
import com.example.planegame.AppHelper
import com.example.planegame.plane.PlayerPlane
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BulletManager private constructor() {
    private val playerBullets = mutableListOf<Bullet>()
    private val enemyBullets = mutableListOf<Bullet>()
    private lateinit var executors: ExecutorService

    private object Holder {
        val instance = BulletManager()
    }

    companion object {
        var level = 1
        fun getInst() = Holder.instance
        fun init() = getInst().apply { init() }
        fun sendPlayerBullet(x: Int, y: Int, width: Int) {
            getInst().obtainPlayerBullet(x, y, width)
        }

        fun sendBossBullet(x: Int, y: Int, width: Int, height: Int) {
            getInst().obtainBossBullet(x, y, width, height)
        }
    }

    private fun init() {
        // 我方子弹的运动循环
        executors = Executors.newFixedThreadPool(2)
        executors.submit {
            while (AppHelper.isRunning) {
                if (AppHelper.isPause) continue
                playerBullets.filter { !it.flagFree }.forEach {
                    it.moveTop()
                    it.hitEnemy()
                }
                // 运动延时
                Thread.sleep(1)
            }
        }
        // 处理敌方子弹的线程
        executors.submit {
            // 敌方子弹的运动循环
            while (AppHelper.isRunning) {
                if (AppHelper.isPause) continue
                enemyBullets.filter { !it.flagFree }.forEach {
                    when (it.getDirection()) {
                        "down" -> it.moveBottom()
                        "up" -> it.moveTop()
                    }
                    it.hitPlayer()
                }
                // 运动延时
                Thread.sleep(5)
            }
        }
    }

    fun release() {
        executors.shutdown()
    }

    /**
     * 生成玩家子弹对象
     * @param width:玩家飞机的宽度
     */
    fun obtainPlayerBullet(x: Int, y: Int, width: Int) {
        var bullet = playerBullets.find { it.flagFree }
        if (bullet == null) {
            bullet = Bullet()
            playerBullets += bullet
        }
        bullet.buildBmpLevel(level)
        bullet.flagFree = false
        // 计算子弹位于飞机的中间位置
        val centerX = x + (width / 2) - (bullet.getBitmap().width / 2).toFloat()
        bullet.setLocation(centerX, y.toFloat())
    }

    fun drawPlayerBullet(canvas: Canvas?) {
        if (canvas == null) return
        playerBullets.filter { !it.flagFree }.forEach { it.draw(canvas) }
    }

    fun drawBossBullet(canvas: Canvas?) {
        if (canvas == null) return
        enemyBullets.filter { !it.flagFree }.forEach { it.draw(canvas) }
    }

    /**
     * 生成敌方子弹对象
     * @param width:敌机的宽度
     * @param height:敌机的高度
     */
    fun obtainBossBullet(x: Int, y: Int, width: Int, height: Int) {
        var bullet = enemyBullets.find { it.flagFree }
        if (bullet == null) {
            bullet = Bullet().apply {
                this.setBitmap("BossBullet.png")
            }
            enemyBullets += bullet
        }
        bullet.let {
            // 设置子弹的起始位置，位于飞机的中央
            val centerX = x + (width / 2) - (it.getBitmap().width / 2).toFloat()
            val centerY = y + (height / 2) - (it.getBitmap().height / 2).toFloat()
            it.setLocation(centerX, centerY)
            it.flagFree = false
            it.setDirection(getPlayerDirection(it))
        }
    }

    private fun getPlayerDirection(bullet: Bullet): String {
        val py = PlayerPlane.getInst().getCenter().y
        val by = bullet.getY()
        return if ((py - by) > 0) "down" else "up"
    }

    fun clearBullet() {
        enemyBullets.forEach { it.flagFree = true }
    }
}