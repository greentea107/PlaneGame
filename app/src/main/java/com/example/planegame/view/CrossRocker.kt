package com.example.planegame.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.withRotation
import com.example.planegame.R
import kotlin.math.cos
import kotlin.math.sin

/**
 * 自定义圆形方向盘控件，此控件主要处理上下左右的事件
 * 方向盘以圆形中心等分16个区域，区域从0-15，
 * 上：0和15
 * 上右：1和2
 * 右：3和4
 * 右下：5和6
 * 下：7和8
 * 左下：9和10
 * 左：11和12
 * 左上：13和14
 * 点击方向盘时通过getPartition来判断触点属于哪个分区，以此为判断需要响应的方向键
 * 使用时需要实现PartitionEventListener接口，通过接口参数获取具体响应的分区号
 */
class CrossRocker : View {
    companion object {
        private const val PARTITION = 16
        private const val CENTER_PART_SIZE_RATIO = 0.28f
    }

    private val paint = Paint()
    private var currWidth = -1
    private var currHeight = -1
    private var currRadius = -1.0
    private var centerX = -1f
    private var centerY = -1f
    private var touched = false
    private var touchedX = -1f
    private var touchedY = -1f
    private var lastPartition = -1

    private var vectorX = DoubleArray(PARTITION + 1)
    private var vectorY = DoubleArray(PARTITION + 1)
    private var partListener: PartitionEventListener? = null

    private var isShowPartitionLine = false // 是否显示分区线
    private var isShowHotsport = true // 是否显示点击热点
    private var isShowAxisArrow = false // 是否显示上下左右方向的箭头
    private var isShowAngleArrow = false // 是否显示夹角方向的箭头
    private var resPadBackground = -1
    private var colorBackground = Color.TRANSPARENT
    private var colorPadLine = Color.WHITE
    private var colorHotspot = Color.CYAN
    private var colorArrowDark = Color.GRAY
    private var colorArrowLight = Color.WHITE
    fun setPartitionEventListener(listener: PartitionEventListener?) {
        partListener = listener
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context?) : super(context) {
        init(context, null)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init(context: Context?, attrs: AttributeSet?) {
        if (attrs != null) {
            context?.obtainStyledAttributes(attrs, R.styleable.CrossRocker)?.let {
                resPadBackground =
                    it.getResourceId(R.styleable.CrossRocker_padBackground, resPadBackground)
                colorBackground =
                    it.getColor(R.styleable.CrossRocker_padBackgroundColor, colorBackground)
                colorArrowDark = it.getColor(R.styleable.CrossRocker_arrowDark, colorArrowDark)
                colorArrowLight = it.getColor(R.styleable.CrossRocker_arrowLight, colorArrowLight)
                colorHotspot = it.getColor(R.styleable.CrossRocker_padHotSportColor, colorHotspot)
                isShowHotsport = it.getBoolean(R.styleable.CrossRocker_showHotSport, isShowHotsport)
                isShowPartitionLine =
                    it.getBoolean(R.styleable.CrossRocker_showPartitionLine, isShowPartitionLine)
                isShowAxisArrow =
                    it.getBoolean(R.styleable.CrossRocker_showAxisArrow, isShowAxisArrow)
                isShowAngleArrow =
                    it.getBoolean(R.styleable.CrossRocker_showAngleArrow, isShowAngleArrow)
            }
        }
        setPartition()
        setOnTouchListener { v, event ->
            this.onTouch(event)
            true
        }
    }

    fun setShowPartitionLine(show: Boolean) {
        isShowPartitionLine = show
    }

    fun setColorPadLine(color: Int) {
        colorPadLine = color
    }

    fun setHotSportColor(color: Int) {
        colorHotspot = color
    }

    /**
     * 设置方向分区
     * 将圆形等比例划分成n个分区
     */
    private fun setPartition() {
        val deg = 2 * Math.PI / PARTITION
        val cos = cos(-deg)
        val sin = sin(-deg)
        vectorX[PARTITION] = 0.0
        vectorX[0] = vectorX[PARTITION]
        vectorY[PARTITION] = currRadius
        vectorY[0] = vectorY[PARTITION]
        for (i in 1 until PARTITION + 1) {
            vectorX[i] = vectorX[i - 1] * cos - vectorY[i - 1] * sin
            vectorY[i] = vectorX[i - 1] * sin + vectorY[i - 1] * cos
        }
    }

    private fun cross(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return x1 * y2 - x2 * y1
    }

    /**
     * 判断坐标在方向分区
     */
    private fun getPartition(x: Float, y: Float): Int {
        if (x <= -1.0f && y <= -1.0f) return -1
        val vx = (x - centerX).toDouble()
        var vy = (y - centerY).toDouble()
        vy *= -1.0
        if (vx * vx + vy * vy < CENTER_PART_SIZE_RATIO * CENTER_PART_SIZE_RATIO * currRadius * currRadius / 4) return 0
        var left = 0
        var right = PARTITION
        while (right - left > 1) {
            val mid = (left + right) / 2
            if (cross(vectorX[left], vectorY[left], vx, vy) <= 0
                && cross(vectorX[mid], vectorY[mid], vx, vy) >= 0
            ) {
                right = mid
            } else {
                left = mid
            }
        }
        return left
    }

    private fun onTouch(evt: MotionEvent): Boolean {
        val action = evt.actionMasked
        val x = evt.x
        val y = evt.y
        lastPartition = getPartition(x, y)
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                touchedX = x
                touchedY = y
                touched = true
                this.postInvalidate()
                partListener?.onPartitionEvent(this, action, lastPartition)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                touchedX = -1f
                touchedY = -1f
                touched = false
                this.postInvalidate()
                partListener?.onPartitionEvent(this, action, lastPartition)
            }
            MotionEvent.ACTION_MOVE -> {
                touchedX = x
                touchedY = y
                this.postInvalidate()
                partListener?.onPartitionEvent(this, action, lastPartition)
            }
        }
        return true
    }

    public override fun onDraw(canvas: Canvas) {
        val width = this.width
        val height = this.height
        val radius = (if (width < height) width else height) / 2.0
        if (width != currWidth || height != currHeight) {
            centerX = (width / 2).toFloat()
            centerY = (height / 2).toFloat()
            currWidth = width
            currHeight = height
            currRadius = radius
            setPartition()
        }
        // 绘制圆盘背景色
        drawBackground(canvas, width, height, radius)
        // 绘制分区线
        if (isShowPartitionLine) {
            drawPartitionLine(canvas)
        }
        if (isShowAxisArrow) {
            drawArrow(canvas, radius, intArrayOf(15, 0), 0f)
            drawArrow(canvas, radius, intArrayOf(3, 4), 90f)
            drawArrow(canvas, radius, intArrayOf(7, 8), 180f)
            drawArrow(canvas, radius, intArrayOf(11, 12), 270f)
        }
        if (isShowAngleArrow) {
            drawArrow(canvas, radius, intArrayOf(1, 2), 45f)
            drawArrow(canvas, radius, intArrayOf(5, 6), 135f)
            drawArrow(canvas, radius, intArrayOf(9, 10), 225f)
            drawArrow(canvas, radius, intArrayOf(13, 14), 315f)
        }
        // 绘制按下时的热点
        if (touched && isShowHotsport) {
            paint.color = colorHotspot
            paint.style = Paint.Style.FILL
            canvas.drawCircle(touchedX, touchedY, (radius / 5f).toFloat(), paint)
        }
    }

    private fun drawBackground(canvas: Canvas, width: Int, height: Int, radius: Double) {
        if (resPadBackground != -1) {
            val bmp = BitmapFactory.decodeResource(context.resources, resPadBackground)
            if (bmp != null)
                canvas.drawBitmap(bmp, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), null)
        } else {
            paint.color = colorBackground
            canvas.drawCircle(centerX, centerY, radius.toFloat(), paint)
        }
    }

    /**
     * 绘制箭头
     * @param radius 圆盘半径
     * @param part   需要响应的分区
     * @param degree 绘制的箭头指向的角度
     */
    private fun drawArrow(canvas: Canvas, radius: Double, part: IntArray, degree: Float) {
        val tempMask = paint.maskFilter
        if (touched && lastPartition in part) {
            paint.color = colorArrowLight
            paint.style = Paint.Style.FILL
            paint.maskFilter = BlurMaskFilter(35f, BlurMaskFilter.Blur.SOLID)
        } else {
            paint.color = colorArrowDark
            paint.style = Paint.Style.FILL
        }
        val offset = (radius / 10).toFloat()
        val size = radius / 4
        val path = Path()
        path.moveTo(centerX, (centerY - radius + offset).toFloat())
        path.lineTo(
            (centerX + size / 2).toFloat(),
            (centerY - radius + size + offset).toFloat()
        )
        path.lineTo((centerX - (size / 2)).toFloat(), (centerY - radius + size + offset).toFloat())
        path.close()
        canvas.withRotation(degree, centerX, centerY) {
            this.drawPath(path, paint)
        }
        paint.maskFilter = tempMask
    }

    /**
     * 绘制分区线，主要是方便观察触屏时的落点
     */
    private fun drawPartitionLine(canvas: Canvas) {
        paint.color = colorPadLine
        repeat(PARTITION) { i ->
            canvas.drawLine(
                centerX, centerY,
                vectorX[i].toFloat() + centerX,
                (-vectorY[i]).toFloat() + centerY,
                paint
            )
        }
    }

    interface PartitionEventListener {
        fun onPartitionEvent(v: View?, action: Int, part: Int)
    }

}