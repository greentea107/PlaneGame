package com.example.planegame

import android.graphics.PointF
import android.graphics.Rect
import android.util.LruCache
import androidx.annotation.NonNull
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
     * 对COS和SIN的结果进行缓存，如果某个角度已经计算过则直接取缓存，不用每次都计算
     * 缓存的KEY为保留两位小数的角度值，所以从缓存取值有精度不准的问题
     */
    private val cosCach = LruCache<String, Double>(360)
    private val sinCache = LruCache<String, Double>(360)

    /**
     * @param isCache 是否使用缓存
     */
    private fun getCosByRadians(@NonNull angle: Double, isCache: Boolean = true): Double {
        //将Double类型的角度值保留两位小数后转String类型，用于缓存的KEY值
        val keyAngle = String.format("%.2f", angle)
        return if (isCache) {
            // 有缓存取缓存，没缓存就计算后再缓存
            if (cosCach[keyAngle] != null)
                cosCach[keyAngle]
            else {
                val result = cos(Math.toRadians(angle))
                cosCach.put(keyAngle, result)
                result
            }
        } else {
            // 不用缓存直接计算
            cos(Math.toRadians(angle))
        }
    }

    /**
     * @param isCache 是否使用缓存
     */
    private fun getSinByRadians(@NonNull angle: Double, isCache: Boolean = true): Double {
        //将Double类型的角度值保留两位小数后转String类型，用于缓存的KEY值
        val keyAngle = String.format("%.2f", angle)
        return if (isCache) {
            // 有缓存取缓存，没缓存就计算后再缓存
            if (sinCache[keyAngle] != null)
                sinCache[keyAngle]
            else {
                val result = sin(Math.toRadians(angle))
                sinCache.put(keyAngle, result)
                result
            }
        } else {
            // 不用缓存直接计算
            sin(Math.toRadians(angle))
        }
    }

    /**
     * 根据半径、角度计算对应的坐标
     * 角度以三点钟方向为0度，顺时针方向增加
     * @param ptOrgini 原点坐标，默认为0,0
     */
    fun getCoordsByAngle(radius: Float, angle: Double, ptOrgini: PointF = PointF()): PointF {
        val x = ptOrgini.x + radius * getCosByRadians(angle)
        val y = ptOrgini.y + radius * getSinByRadians(angle)
        return PointF(x.toFloat(), y.toFloat())
    }
}