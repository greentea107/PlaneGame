package com.example.planegame

import android.graphics.Point
import android.graphics.Rect
import kotlin.math.*

object MathUtils {
    /**
     * 判断两个矩形是否相交
     */
    fun cross(r1: Rect, r2: Rect): Boolean {
        val zx = abs(r1.left + r1.right - r2.left - r2.right)
        val x = abs(r1.left - r1.right) + abs(r2.left - r2.right)
        val zy = abs(r1.top + r1.bottom - r2.top - r2.bottom)
        val y = abs(r1.top - r1.bottom) + abs(r2.top - r2.bottom)
        return zx <= x && zy <= y
    }

    /**
     * 根据两个坐标获取角度
     */
    fun getAngle(p1: Point, p2: Point): Double {
        val tem = atan(((p2.y - p1.y) / (p2.x - p1.x)).toDouble())
        return tem * (180 / Math.PI)
    }

    /**
     * 坐标按角度旋转
     */
    fun coordinateRotation(x: Float, y: Float, degree: Int): Point {
        var x1 = x * cos(degree * Math.PI / 180) + y * sin(degree * Math.PI / 180);
        x1 = ((x1 * 100).roundToInt() / 100).toDouble()
        var y1 =
            -x * sin(degree * Math.PI / 180) + y * cos(degree * Math.PI / 180);
        y1 = ((y1 * 100).roundToInt() / 100).toDouble()
        val relativeOriginPoint = Point(x1.toInt(), y1.toInt())
        return relativeOriginPoint
    }
}