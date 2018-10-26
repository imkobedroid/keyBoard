package loan.moerlong.com.customkeyboard.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import loan.moerlong.com.customkeyboard.R


class CustomKeyboardView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : KeyboardView(context, attrs, defStyleAttr) {

    private var mKeyBoard: Keyboard? = null

    init {
        mKeyBoard = this.keyboard
    }

    @SuppressLint("DrawAllocation", "PrivateResource")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas!!)
        val keyboard = keyboard ?: return
        val keys = keyboard.keys
        if (keys != null && keys.size > 0) {
            val paint = Paint()
            paint.textAlign = Paint.Align.CENTER
            val font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.typeface = font
            paint.isAntiAlias = true
            for (key in keys) {
                if (key.codes[0] == -4 && key.label == "完成") {
                    val dr = context.resources.getDrawable(R.drawable.keyboard_yellow)
                    dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height)
                    dr.draw(canvas)
                } else {
                    if (key.codes[0] != -5) {
                        val dr = context.resources.getDrawable(R.drawable.keyboard_whilat)
                        dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height)
                        dr.draw(canvas)
                    }
                }

                if (key.label != null) {
                    if (key.codes[0] == -4) {
                        paint.textSize = (24 * 2).toFloat()
                    } else {
                        paint.textSize = (30 * 2).toFloat()
                    }
                    if (key.codes[0] == -4 && key.label == "完成") {
                        paint.color = ContextCompat.getColor(context, R.color.white)
                    } else if (key.codes[0] == -4 && key.label == "隐藏键盘") {
                        paint.color = ContextCompat.getColor(context, R.color.black)
                        paint.typeface = Typeface.SANS_SERIF
                    } else {
                        paint.color = ContextCompat.getColor(context, R.color.black)
                    }
                    val rect = Rect(key.x, key.y, key.x + key.width, key.y + key.height)
                    val fontMetrics = paint.fontMetricsInt
                    val baseline = (rect.bottom + rect.top - fontMetrics.bottom - fontMetrics.top) / 2
                    // 下面这行是实现水平居中，drawText对应改为传入targetRect.centerX()
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText(key.label.toString(), rect.centerX().toFloat(), baseline.toFloat(), paint)
                }
            }
        }
    }
}