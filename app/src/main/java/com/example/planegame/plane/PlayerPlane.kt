package com.example.planegame.plane

import android.graphics.*
import com.example.planegame.AppHelper
import com.example.planegame.bomb.BombManager
import com.example.planegame.bullet.BulletManager
import com.jeremyliao.liveeventbus.LiveEventBus
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 玩家飞机
 */
class PlayerPlane private constructor() : Plane() {
    private lateinit var executors: ExecutorService
    private var isLeft = false
    private var isRight = false
    private var isTop = false
    private var isBottom = false
    private var isAttack = false
    private var isEntrance = false
    private var startMillis = 0L

    private object Holder {
        val instance = PlayerPlane()
        const val SAFE_TIME = 5000L //无敌时间
    }

    companion object {
        private const val DELAY_MOTION = 2L
        private const val DELAY_BULLET = 100L
        var hitCount = 0 // 累加连接击落数，用于升级子弹
        fun getInst() = Holder.instance
        fun init(): PlayerPlane {
            getInst().let {
                it.init()
                it.reset()
            }
            return getInst()
        }

        /**
         * 玩家被击落
         */
        fun hit() {
            if (getInst().isSafe()) return
            // 玩家飞机爆炸
            BombManager.getInst().obtain(
                getInst().getCenter().x.toFloat(),
                getInst().getCenter().x.toFloat(),
                getInst().getSize().right / 2.toFloat()
            )
            getInst().reset()
        }
    }

    private fun init() {
        setPlaneImage("mine.png") // 加载玩家飞机的位图
        executors = Executors.newFixedThreadPool(2)
        // 飞行运行协程
        executors.submit {
            while (AppHelper.isRunning) {
                if (AppHelper.isPause) continue
                if (isEntrance) { // 玩家飞机是否处于入场阶段
                    entrance()
                } else {
                    if (isRight) moveRight()
                    if (isLeft) moveLeft()
                    if (isTop) moveTop()
                    if (isBottom) moveBottom()
                }
                // 延时-运动间隔，没有这个间隔的话会出现瞬移
                Thread.sleep(DELAY_MOTION)
            }
        }
        // 子弹协程：主要是控制子弹的发射间隔不至于变成一条密集的线
        executors.submit {
            while (AppHelper.isRunning) {
                if (AppHelper.isPause) continue
                if (isAttack && !isEntrance)
                    BulletManager.sendPlayerBullet(
                        x.toInt(), y.toInt(),
                        getPlaneImage().width
                    )
                // 延时-子弹射击间隔
                Thread.sleep(DELAY_BULLET)
            }
        }
    }

    /**
     * 重置玩家
     */
    fun reset() {
        startMillis = System.currentTimeMillis()
        // 连接击落数和火力，爆雷重置
        hitCount = 0
        BulletManager.level = 1
        LiveEventBus.get(AppHelper.FIRE_EVENT, Int::class.java).post(hitCount)
        LiveEventBus.get(AppHelper.BOMB_RESET_EVENT, Int::class.java).post(3)
        // 玩家飞机的初始位置
        val x = (AppHelper.widthSurface / 2).toFloat()
        val y = AppHelper.heightSurface.toFloat()
        getInst().setLocation(x, y)
        isEntrance = true
    }

    /**
     * 玩家入场，此方法会在线程中循环调用。
     * 玩家飞机由屏幕底部飞机底部三分之一处为止
     * 入场时不能操作，不能开火
     */
    private fun entrance() {
        if (isEntrance) {
            val beginY = AppHelper.heightSurface - (AppHelper.heightSurface / 3)
            val endY = AppHelper.heightSurface
            if (y.toInt() in (beginY..endY)) {
                moveTop()
            } else {
                isEntrance = false
            }
        }
    }

    /**
     * 判断是否处于保持时间内（无敌）
     */
    fun isSafe() = (System.currentTimeMillis() - startMillis) < Holder.SAFE_TIME

    fun actionRight() {
        isRight = true
    }

    fun actionLeft() {
        isLeft = true
    }

    fun actionTop() {
        isTop = true
    }

    fun actionBottom() {
        isBottom = true
    }

    fun releaseAction() {
        isRight = false
        isLeft = false
        isTop = false
        isBottom = false
    }

    fun release() {
        executors.shutdown()
    }

    fun attack(isAttack: Boolean) {
        this.isAttack = isAttack
    }

    fun getCenter(): Point {
        val cx = getPlaneImage().width / 2 + x
        val cy = getPlaneImage().height / 2 + y
        return Point(cx.toInt(), cy.toInt())
    }

    fun getSize() = Rect(0, 0, getPlaneImage().width, getPlaneImage().height)

    /**
     * 获取玩家位于屏幕上的矩形
     */
    fun getRect(): Rect {
        val pr = getPlaneImage().width + x.toInt()
        val pb = getPlaneImage().height + y.toInt()
        val scaleWidth = getPlaneImage().width / 5
        val scaleHeight = getPlaneImage().height / 5
        return Rect(
            x.toInt() + scaleWidth,
            y.toInt() + scaleHeight,
            pr - scaleWidth,
            pb - scaleHeight
        )
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        if (isSafe()) { // 飞机处于无敌时间内就
            drawSafeOval(canvas)
        }
    }

    private val paintOval = Paint()
    /**
     * 在飞机的外围画圆，表示飞机处于保护中
     */
    private fun drawSafeOval(canvas: Canvas?) {
        val cx = getCenter().x.toFloat()
        val cy = getCenter().y.toFloat()
        val radius = getSize().right / 2
        paintOval.let {
            val gradient = RadialGradient(
                cx, cy, radius * 1.5f + 0.1f,
                intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, Color.WHITE), null,
                Shader.TileMode.CLAMP
            )
            it.alpha = 100
            it.setShader(gradient)
        }
        canvas?.drawCircle(cx, cy, radius * 1.5f, paintOval)
    }
}