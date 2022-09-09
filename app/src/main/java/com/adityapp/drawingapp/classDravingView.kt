package com.adityapp.drawingapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class ClassDrawingView(context : Context, attr : AttributeSet) : View(context, attr) {
    // Variable Declaration
    private var mDrawingPath : CustomPath? = null
    private var mCanvasBitmap : Bitmap? = null
    private var mDrawPaint : Paint? = null
    private var mCanvasPaint : Paint? = null
    private var mBrushSize : Float = 0.toFloat()
    private var color = Color.BLACK
    private var canvas :  Canvas? = null
    private val mPaths = ArrayList<CustomPath>()
    private val mUndoPaths = ArrayList<CustomPath>()

    // Initialization of above variables
    init {
        setUpDrawing()
    }       // init block is the first to be executed when the class is instantiated

    fun onClickUndo(){
        if(mPaths.size>0){
            mUndoPaths.add(mPaths.removeAt(mPaths.size-1))
            invalidate()
        }
    }

    private fun setUpDrawing(){
        mDrawPaint = Paint()
        mDrawingPath = CustomPath(color,mBrushSize)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        //mBrushSize = 20.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }
    // change canvas to canvas? if fails
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!,0f,0f, mCanvasPaint)

        for(path in mPaths){
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas.drawPath(path, mDrawPaint!!)
        }

        if(!mDrawingPath!!.isEmpty){
            mDrawPaint!!.strokeWidth = mDrawingPath!!.brushThickness
            mDrawPaint!!.color = mDrawingPath!!.color
        canvas.drawPath(mDrawingPath!!, mDrawPaint!!)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                mDrawingPath!!.color = color
                mDrawingPath!!.brushThickness = mBrushSize

                mDrawingPath!!.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawingPath!!.moveTo(touchX,touchY)
                    }
                }
            }
            MotionEvent.ACTION_MOVE ->{
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawingPath!!.lineTo(touchX,touchY)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawingPath!!)
                mDrawingPath = CustomPath(color, mBrushSize)

            }
            else -> return false
        }
        invalidate()

        return true
    }

    fun brushSizeSetter(newSize : Float){
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, resources.displayMetrics)
        mDrawPaint!!.strokeWidth = mBrushSize
    }
    fun setColor(newColor: String){
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color
    }


    internal inner class CustomPath(var color: Int,
                                    var brushThickness: Float) : Path(){

    }


}