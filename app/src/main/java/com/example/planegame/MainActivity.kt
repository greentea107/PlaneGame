package com.example.planegame

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.planegame.bomb.BombManager
import com.example.planegame.bullet.BulletManager
import com.example.planegame.plane.EnemyPlaneManager
import com.example.planegame.plane.PlayerPlane
import com.example.planegame.view.CrossRocker
import com.jeremyliao.liveeventbus.LiveEventBus
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_info.*
import kotlinx.coroutines.*
import java.text.DecimalFormat

/**
 * 本程序所涉及到的素材皆来自网络，仅供学习和演示使用。欢迎相互交流和学习
 * 简书原文：https://www.jianshu.com/p/7efa4b6eb2ab
 *
 * 作者：绿茶
 * 联系方式：
 * QQ：52137124
 * Email: 52137124@qq.com
 */
class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private lateinit var map: BackgroundMap
    private lateinit var playerPlane: PlayerPlane
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onResume() {
        super.onResume()
        hideStatusBar()
    }

    override fun onBackPressed() {
        AppHelper.isRunning = false
        android.os.Process.killProcess(android.os.Process.myPid())
        super.onBackPressed()
    }

    /**
     * 全屏
     */
    private fun hideStatusBar() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            val v = this.window.decorView
            v.systemUiVisibility = View.GONE
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val decorView = window.decorView
            val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            decorView.systemUiVisibility = uiOptions
        }
    }

    private fun initView() {
        ibtnBack.setOnClickListener {
            onBackPressed()
        }
        initPauseButton()
        initGamePad() // 方向键
        initAttackButton() // 开火键
        initScoreCount() // 评分和击落数
        initCollision() // 碰撞数
        initFirePower() // 火力升级
        initBombView() // 爆雷
        initSurfaceView()
    }

    private fun initSurfaceView() {
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                launch(Dispatchers.IO) {
                    while (AppHelper.isRunning) {
                        val canvas = holder.lockCanvas()
                        // 绘制背景
                        map.draw(canvas, width, height)
                        if (!AppHelper.isPause) {
                            // 绘制子弹
                            BulletManager.getInst().drawPlayerBullet(canvas)
                            BulletManager.getInst().drawBossBullet(canvas)
                            // 绘制我方飞机
                            playerPlane.draw(canvas)
                            // 绘制敌方飞机
                            EnemyPlaneManager.getInst().draw(canvas)
                            // 绘制爆炸
                            BombManager.getInst().drawAll(canvas)
                        }
                        canvas?.let { holder.unlockCanvasAndPost(it) }
                    }
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                AppHelper.isRunning = false
                // 资源释放
                map.relase()
                PlayerPlane.getInst().release()
                BulletManager.getInst().release()
                BombManager.getInst().release()
                EnemyPlaneManager.getInst().release()
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                AppHelper.isRunning = true
                AppHelper.widthSurface = surfaceView.width
                AppHelper.heightSurface = surfaceView.height
                AppHelper.bound = Rect(0, 0, surfaceView.width, surfaceView.height)

                map = BackgroundMap(this@MainActivity) // 初始化地图
                playerPlane = PlayerPlane.init() // 初始化玩家飞机
                EnemyPlaneManager.init() // 初始化敌方飞机
                BulletManager.init() // 初始化子弹数据
                BombManager.init() // 初始化爆炸管理
            }

        })
    }

    private fun initPauseButton() {
        vgInfomation.visibility = View.GONE
        btnPause.setOnClickListener {
            AppHelper.isPause = !AppHelper.isPause
            vgInfomation.visibility = if (AppHelper.isPause) View.VISIBLE else View.GONE
        }
        btnJianShu.setOnClickListener { openBrowser(btnJianShu.text.toString().split("\n")[1]) }
        btnQQ.setOnClickListener { openBrowser(btnQQ.text.toString().split("\n")[1]) }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initAttackButton() {
        btnAttack.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_POINTER_DOWN -> {
                    btnAttack.setBackgroundResource(R.drawable.btn_oval_true)
                    PlayerPlane.getInst().attack(true)
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    btnAttack.setBackgroundResource(R.drawable.btn_oval_false)
                    PlayerPlane.getInst().attack(false)
                }

            }
            true
        }
    }


    /**
     * 碰撞数
     */
    private fun initCollision() {
        LiveEventBus.get(AppHelper.COLLI_EVENT, Int::class.java)
            .observe(this, Observer {
                val oldValue = tvCollision.text.toString().toInt()
                val df = DecimalFormat("00000")
                val newValue = df.format(it + oldValue)
                tvCollision.text = newValue
            })
    }

    private fun initBombView() {
        LiveEventBus.get(AppHelper.BOMB_ADD_EVENT, Int::class.java)
            .observe(this, Observer {
                ratBomb.rating++
            })
        LiveEventBus.get(AppHelper.BOMB_RESET_EVENT, Int::class.java)
            .observe(this, Observer {
                ratBomb.rating = it.toFloat()
            })
        btnBomb.setOnClickListener {
            if (ratBomb.rating > 0) {
                EnemyPlaneManager.getInst().fullScreenBomb()
                ratBomb.rating--
            }
        }
    }

    /**
     * 火力强度
     */
    private fun initFirePower() {
        pbPower.max = 20
        pbPower.progress = 0
        LiveEventBus.get(AppHelper.FIRE_EVENT, Int::class.java)
            .observe(this, Observer {
                tvFirePower.text = when (it) {
                    in (0..10) -> "一级"
                    in (11..20) -> "二级"
                    else -> "三级"
                }

                pbPower.progress = it
            })
    }

    /**
     * 总分数和击落数
     */
    private fun initScoreCount() {
        LiveEventBus.get(AppHelper.SCORE_EVENT, Int::class.java)
            .observe(this) {
                val oldScore = tvScore.text.toString().toInt()
                val df = DecimalFormat("00000")
                val newScore = df.format(it + oldScore)
                tvScore.text = newScore
                println(">>>>>>>>>> ${tvScore.text}")
                val oldCount = tvCount.text.toString().toInt()
                val df2 = DecimalFormat("00000")
                val newCount = df2.format(oldCount + 1)
                tvCount.text = newCount
            }
        LiveEventBus.get(AppHelper.SCORE_EVENT, Int::class.java)
            .post(999)
    }

    /**
     * 初始化游戏十字键
     */
    private fun initGamePad() {
        gamePad.setActionListener { _, direction, action ->
            onGamePadKey(action, direction) // 响应方向盘操作
        }
    }

    private var lastDirection = ""
    private fun onGamePadKey(action: Int, direction: String) {
        when (action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (direction != lastDirection) playerPlane.releaseAction()
                when (direction) {
                    CrossRocker.RIGHT -> playerPlane.actionRight()
                    CrossRocker.LEFT -> playerPlane.actionLeft()
                    CrossRocker.TOP -> playerPlane.actionTop()
                    CrossRocker.BOTTOM -> playerPlane.actionBottom()
                    CrossRocker.TOP_RIGHT -> {
                        playerPlane.actionTop();playerPlane.actionRight()
                    }
                    CrossRocker.BOTTOM_RIGHT -> {
                        playerPlane.actionBottom();playerPlane.actionRight()
                    }
                    CrossRocker.BOTTOM_LEFT -> {
                        playerPlane.actionBottom();playerPlane.actionLeft()
                    }
                    CrossRocker.TOP_LEFT -> {
                        playerPlane.actionTop();playerPlane.actionLeft()
                    }
                }
                lastDirection = direction
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                playerPlane.releaseAction()
            }
        }
    }

    /**
     * 调用第三方浏览器打开
     * @param url 要浏览的资源地址
     */
    private fun openBrowser(url: String?) {
        val intent = Intent()
        intent.action = "android.intent.action.VIEW"
        val content_url = Uri.parse(url)
        intent.data = content_url
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity")
        startActivity(intent)
    }
}
