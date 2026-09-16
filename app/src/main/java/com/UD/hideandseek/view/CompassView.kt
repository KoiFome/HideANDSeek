package com.UD.hideandseek.view


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.system.Os.close
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.ui.graphics.Path
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.UD.hideandseek.ECS.TempState
import com.UD.hideandseek.R
import kotlin.math.*
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import kotlin.io.path.Path
import kotlin.io.path.moveTo


class CompassView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? =null
) : View(context, attrs){

    private val mokouBitMap: Bitmap =
        AppCompatResources.getDrawable(context, R.drawable.hidden_mokou)!!
            .toBitmap()

    private val arrowBitMap: Bitmap =
        AppCompatResources.getDrawable(context, R.drawable.ic_arrow)!!
            .toBitmap()


    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.Dial)
    }

    private val letterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.TextGame)
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val conePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // ── Capa estática cacheada ──────────────────────────────────────
    private var staticLayer: Bitmap? = null

    // ── Estado que empuja el Activity ───────────────────────────────
    var azimuth: Float = 0f
        set(v) { field = v; invalidate() }

    var targetAzimuth: Float = 0f
        set(v) { field = v; invalidate() }

    var temperature: TempState = TempState.FRIO
        set(v) { field = v; invalidate() }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            staticLayer?.recycle()
            staticLayer = buildStaticLayer(w, h)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        staticLayer?.recycle()
        staticLayer = null
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        staticLayer?.let{ canvas.drawBitmap(it, 0f,0f,null) }

        val cx = width / 2f
        val cy = height /2f
        val r = min(cx,cy) * 0.9f

        //calcula la posicion de la flecha

        val relativeAngle = targetAzimuth - azimuth

        val rad = Math.toRadians((relativeAngle - 90f).toDouble())
        val tipX = cx + r * 0.85f * cos(rad).toFloat()
        val tipY = cy + r * 0.85f * sin(rad).toFloat()

        //Dial/meter

        //Direction Arrow of the Dial

        val stateColor = when(temperature) {
            TempState.CALIENTE -> ContextCompat.getColor(context, R.color.HotState)
            TempState.TIBIO -> ContextCompat.getColor(context, R.color.MildState)
            TempState.FRIO -> ContextCompat.getColor(context, R.color.ColdState)
        }

        drawCone(canvas, cx, cy, r, relativeAngle, stateColor)

        glowPaint.color = stateColor
        glowPaint.alpha = 90

        //Drawing the Arrow...
        canvas.drawCircle(tipX,tipY,60f,glowPaint)

        //Drawing the movement of the Arrow

        canvas.withRotation(relativeAngle, tipX, tipY) {
            drawBitmap(
                arrowBitMap,
                tipX - arrowBitMap.width /2f,
                tipY - arrowBitMap.height /2f,
                null
            )
        }

        //Draw Mokou in the screen
        if(temperature == TempState.CALIENTE){
            val scale = 1.2f
            canvas.withScale(scale, scale, cx, cy) {
                drawBitmap(
                    mokouBitMap,
                    cx - mokouBitMap.width / 2f,
                    cy - mokouBitMap.height / 2f,
                    null
                )
            }
        }

    }

    private fun drawCone(
        canvas: Canvas, cx: Float, cy: Float, r: Float,
        relativeAngle: Float, color: Int
    ) {
        // 1. Keep angles as Float using kotlin.math.toRadians if available, or cast to Float
        val halfWidthRad = Math.toRadians(14.0).toFloat()
        val baseRad = Math.toRadians((relativeAngle - 90f).toDouble()).toFloat()

        // 2. Use kotlin.math.cos and sin directly with Float parameters
        val x1 = cx + r * 0.7f * cos(baseRad)
        val y1 = cy + r * 0.7f * sin(baseRad)
        val x2 = cx + r * 0.7f * cos(baseRad)
        val y2 = cy + r * 0.7f * sin(baseRad)

        // Note: If you want to keep the offset (baseRad - halfWidthRad), do it like this:
        // val x1 = cx + r * 0.7f * cos(baseRad - halfWidthRad)

        val path = android.graphics.Path().apply {
            moveTo(cx, cy)
            lineTo(x1, y1)
            lineTo(x2, y2)
            close()
        }

        conePaint.color = color
        conePaint.alpha = 70

        canvas.drawPath(path, conePaint)
    }


    private fun buildStaticLayer (w: Int, h: Int): Bitmap{
        val bmp = createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val cx = w /2f
        val cy = h /2f
        val r = min(cx,cy) * 0.9f

        c.drawCircle(cx,cy,r,ringPaint)
        ringPaint.strokeWidth = r * 0.045f
        letterPaint.textSize = r * 0.16f

        val offset = r * 0.82f
        val baselineShift = letterPaint.textSize * 0.35f
        c.drawText("N", cx, cy - offset + baselineShift, letterPaint)
        c.drawText("S", cx, cy + offset + baselineShift, letterPaint)
        c.drawText("E", cx + offset, cy + baselineShift, letterPaint)
        c.drawText("O", cx - offset, cy + baselineShift, letterPaint)

        return bmp
    }


}