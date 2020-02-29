package com.example.planegame.plane

import android.graphics.Rect

class EnemyPlane : Plane() {
    var flagUsed = false // 占用标记，飞出屏幕后设为false，对象可被重复使用
    var direction = "down" // 敌机的飞行方向
    var score = 1 // 敌机的分数
    var isTrack = false // 是否会根据玩家的位置进行左右偏移
    var type = 1

    init {
        setEnableOut(true) // 敌机可以飞机屏幕边界再消失
    }

    /**
     * 飞机的构造方法
     * @param imgUrl：飞机图片
     * @param speed：速度
     * @param score：分数
     * @param hp：血量
     * @param direction：飞机的飞行方向
     * @param isTrack：是否会根据玩家飞机的位置做偏移飞行
     */
    fun buildPlane(
        imgUrl: String,
        speed: Float,
        score: Int = 1,
        hp: Int = 1,
        direction: String = "down",
        isTrack: Boolean = false
    ) {
        setPlaneImage(imgUrl)
        this.speed = speed
        this.score = score
        this.hp = hp
        this.direction = direction
        this.isTrack = isTrack
    }

    fun offsetLeft(offset: Float) {
        this.x -= offset
    }

    fun offsetRight(offset: Float) {
        this.x += offset
    }

    fun getPlaneSpeed() = this.speed

    /**
     * 获取飞机位于屏幕上的坐标
     */
    fun getRect(): Rect {
        val right = x.toInt() + getPlaneImage().width
        val bottom = y.toInt() + getPlaneImage().height
        // 矩形框内缩1/5，减少碰撞的难度
        val sw = getPlaneImage().width / 5
        val sh = getPlaneImage().height / 5
        return Rect(x.toInt() + sw, y.toInt() + sh, right - sw, bottom - sh)
    }
}