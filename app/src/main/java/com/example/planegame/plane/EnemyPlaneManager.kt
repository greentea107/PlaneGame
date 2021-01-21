package com.example.planegame.plane

import android.graphics.*
import com.example.planegame.AppHelper
import com.example.planegame.MathUtils
import com.example.planegame.bomb.BombManager
import com.example.planegame.bullet.BulletManager
import com.jeremyliao.liveeventbus.LiveEventBus
import java.lang.Exception
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 敌机资源管理
 */
class EnemyPlaneManager private constructor() {
    private val enemyList = mutableListOf<EnemyPlane>()
    private lateinit var executor: ExecutorService

    private fun init() {
        executor = Executors.newSingleThreadExecutor()
        executor.submit {
            while (AppHelper.isRunning) {
                if (AppHelper.isPause) continue
                buildEnemyPlane() // 生成敌机
                motionPlane() // 对敌机列表遍历，作运动处理
                sendBullet() // 让指定的飞机发射子弹
                Thread.sleep(Holder.DELAY) // 这个延时能决定飞机的移动的快慢
            }
        }
    }

    private object Holder {
        const val ENEMY_MAX = 12 // 最大敌机数量
        const val DELAY = 2L
        val instance = EnemyPlaneManager()
    }

    companion object {
        fun getInst() = Holder.instance
        fun init() = getInst().apply { this.init() }

        fun buildEnemyPlane() {
            getInst().obtain()
        }
    }

    fun release() {
        executor.shutdown()
    }

    /**
     * 初始化飞机（工厂化生产）
     */
    fun obtain() {
        var plane = enemyList.find { !it.flagUsed }
        if (plane == null && enemyList.size < Holder.ENEMY_MAX) {
            plane = EnemyPlane()
            enemyList += plane
        }
        // 随机生成敌机类型
        var enemyType = Random().nextInt(11)
        // 控制生成大飞机的总数
        val count = enemyList.count { it.type in (6..10) && it.flagUsed }
        if (count >= 5) enemyType -= 5
        plane?.let {
            it.flagUsed = true
            when (enemyType) {
                0 -> it.buildPlane("ep0.png", 1.0f, 1)
                1 -> it.buildPlane("ep1.png", 1.1f, 1)
                2 -> it.buildPlane("ep2.png", 1.5f, 1, isTrack = true)
                3 -> it.buildPlane("ep3.png", 1.2f, 2, isTrack = true)
                4 -> it.buildPlane("ep4.png", 0.8f, 2)
                5 -> it.buildPlane("ep5.png", 0.8f, 1, 3)
                6 -> it.buildPlane("ep6.png", 0.3f, 5, 20, "up")
                7 -> it.buildPlane("ep7.png", 0.4f, 4, 10)
                8 -> it.buildPlane("ep8.png", 0.2f, 5, 12, "up")
                9 -> it.buildPlane("ep9.png", 0.35f, 8, 20)
                10 -> it.buildPlane("ep10.png", 0.37f, 9, 20)
            }
            it.type = enemyType
            // 随机设置敌机的起始位置
            val x = Random().nextInt(AppHelper.widthSurface - it.getPlaneImage().width).toFloat()
            val y = if (enemyType in arrayOf(6, 8)) {
                it.getPlaneImage().height * 2f + AppHelper.heightSurface
            } else {
                -it.getPlaneImage().height * 2f
            }
            it.setLocation(x, y)
        }
    }

    private var lastMillis = 0L
    private fun sendBullet() {
        enemyList.filter { it.flagUsed && it.type in (6 until 10) }
            .forEach {
                if ((System.currentTimeMillis() - lastMillis) > 1000) {
                    if (it.y > 10) {
                        BulletManager.sendBossBullet(
                            it.x.toInt(), it.y.toInt(),
                            it.getPlaneImage().width,
                            it.getPlaneImage().height
                        )
                    }
                    lastMillis = System.currentTimeMillis()
                }
            }
    }

    /**
     * 飞行的运动循环
     */
    private fun motionPlane() {
        enemyList.filter { it.flagUsed }.forEach {
            if (it.isTrack) {
                val offset = it.getPlaneSpeed() / 2
                if ((PlayerPlane.getInst().x - it.x) < 0)
                    it.offsetLeft(offset)
                else
                    it.offsetRight(offset)
            }
            collision(it) // 碰撞检测
            if (it.direction == "down")
                if (!it.moveBottom()) it.flagUsed = false
            if (it.direction == "up")
                if (!it.moveTop()) it.flagUsed = false
        }
    }

    /**
     * 碰撞检测
     */
    private fun collision(ep: EnemyPlane) {
        // 获取敌机矩形
        val eRect = ep.getRect()
        // 获取玩家矩形
        val pRect = PlayerPlane.getInst().getRect()
        // 检测是否和玩家飞机碰撞
        if (MathUtils.cross(eRect, pRect)) {
            // 玩家被击中
            PlayerPlane.hit()
            // 敌机置为空闲
            ep.flagUsed = false
            // 敌机爆炸
            BombManager.getInst().obtain(
                eRect.centerX().toFloat(),
                eRect.centerY().toFloat(),
                ep.getPlaneImage().width / 2.toFloat()
            )
            // 通过消息总线发给主界面更新计分控件
            LiveEventBus.get(AppHelper.COLLI_EVENT, Int::class.java).post(1)
        }
    }

    fun draw(canvas: Canvas?) {
        enemyList.filter { it.flagUsed }.forEach {
            canvas?.drawBitmap(it.getPlaneImage(), it.x, it.y, null)
        }
    }

    /**
     * 判断子弹是否击中飞机
     * @return 返回值表示飞机是否中弹
     */
    fun isHit(bulletPoint: Rect, bulletLevel: Int): Point? {
        enemyList.filter { it.flagUsed }.forEach {
            // 获取飞机的矩形
            val px = it.x.toInt()
            val py = it.y.toInt()
            val width = it.getPlaneImage().width
            val height = it.getPlaneImage().height
            val right = width + px
            val bottom = height + py
            // 计算矩形框内缩1/5的比例
            val scaleW = width / 5
            val scaleH = height / 5
            val planeRect = Rect(px + scaleW, py + scaleH, right - scaleW, bottom - scaleH)

            if (MathUtils.cross(planeRect, bulletPoint)) {
                it.hp -= bulletLevel // 根据子弹等级减血
                // 敌机血降为0，标记对象为false，可被再次复用
                if (it.hp <= 0) {
                    it.flagUsed = false
                    // 根据分数给子弹升级
                    when (PlayerPlane.hitCount++) {
                        in (0..10) -> BulletManager.level = 1
                        in (10..20) -> BulletManager.level = 2
                        else -> {
                            BulletManager.level = 3
                            // 击落一定数量的飞机就奖励一次爆雷
                            if ((PlayerPlane.hitCount % 100) == 0) {
                                LiveEventBus.get(AppHelper.BOMB_ADD_EVENT, Int::class.java).post(1)
                            }
                        }
                    }
                    LiveEventBus.get(AppHelper.FIRE_EVENT, Int::class.java)
                        .post(PlayerPlane.hitCount)
                    // 更新界面上的分数
                    LiveEventBus.get(AppHelper.SCORE_EVENT, Int::class.java).post(it.score)
                    // 子弹击中，返回爆炸中心坐标
                    return Point(px + (width / 2), py + (height / 2))
                }
                // 子弹击中，需要回收子弹资源
                return Point(-1, -1)
            }
        }
        // 子弹未击中
        return null
    }

    /**
     * 全屏炸弹
     */
    fun fullScreenBomb() {
        // 清飞机
        enemyList.filter { it.flagUsed }.forEach {
            BombManager.getInst().obtain(
                it.x, it.y,
                (it.getPlaneImage().width / 2).toFloat()
            )
            // 爆雷击落的飞机只算1分
            LiveEventBus.get(AppHelper.SCORE_EVENT, Int::class.java).post(1)
            it.flagUsed = false
        }
        // 清子弹
        BulletManager.getInst().clearBullet()
    }
}