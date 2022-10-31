package com.viva.indexlib

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.viva.indexlib.indexBar.BaseIndexPinyinBean
import com.viva.indexlib.indexBar.IndexBarDataHelperImpl
import com.viva.indexlib.suspension.IIndexBarDataHelper
import kotlin.collections.ArrayList

/**
 * @author 李雄厚
 *
 *
 */
class IndexBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * 是否需要根据实际的数据来生成索引数据源（例如 只有 A B C 三种tag，那么索引栏就 A B C 三项）
     */
    private var isNeedRealIndex = false

    /**
     * 索引数据源
     */
    private var mIndexDataList = ArrayList<String>()

    /**
     * view高宽
     */
    private var mWidth = 0
    private var mHeight = 0

    /**
     * 每个index区域的高度
     */
    private var mGapHeight = 0

    private val mPaint by lazy { Paint() }
    private val indexBounds by lazy { Rect() }

    /**
     * 汉语->拼音，拼音->tag
     */
    private var mDataHelper: IIndexBarDataHelper

    /**
     * 用于特写显示正在被触摸的index
     */
    private var mPressedShowTextView: AppCompatTextView? = null

    /**
     * 源数据 已经有序？
     */
    var isSourceDataAlreadySorted = false

    /**
     * Adapter的数据源
     */
    private var mSourceDataList: MutableList<out BaseIndexPinyinBean> = ArrayList()

    private var mLayoutManager: CustomLinearLayoutManager? = null
    var headerViewCount = 0
    private var mOnIndexPressedListener: IndexPressedListener? = null


    fun setHeaderViewCount(headerViewCount: Int): IndexBar {
        this.headerViewCount = headerViewCount
        return this
    }


    fun setSourceDataAlreadySorted(sourceDataAlreadySorted: Boolean): IndexBar {
        isSourceDataAlreadySorted = sourceDataAlreadySorted
        return this
    }

    fun getDataHelper(): IIndexBarDataHelper = mDataHelper

    fun setDataHelper(dataHelper: IIndexBarDataHelper): IndexBar {
        mDataHelper = dataHelper
        return this
    }

    init {
        //默认的TextSize
        var textSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16F, resources.displayMetrics)
        val typedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.IndexBar, defStyleAttr, 0)
        val n = typedArray.indexCount
        for (i in 0 until n) {
            val attr = typedArray.getIndex(i)
            //modify 2016 09 07 :如果引用成AndroidLib 资源都不是常量，无法使用switch case
            if (attr == R.styleable.IndexBar_indexBarTextSize) {
                textSize = typedArray.getDimensionPixelSize(attr, textSize.toInt()).toFloat()
            }
        }
        typedArray.recycle()

        initIndexDataList()

        mPaint.isAntiAlias = true
        mPaint.isFakeBoldText = true
        mPaint.textSize = textSize
        mPaint.color = Color.parseColor("#000000")

        setOnIndexPressedListener {
            onIndexPressed { _, text ->
                mPressedShowTextView?.isVisible = true
                mPressedShowTextView?.text = text

                mLayoutManager?.let {
                    val position = getPosByTag(text)
                    it.scrollToPositionWithOffset(position, 0)
                }
            }

            onMotionEventEnd {
                mPressedShowTextView?.isVisible = false
            }
        }


        mDataHelper = IndexBarDataHelperImpl()

    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //取出宽高的MeasureSpec  Mode 和Size
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)
        //最终测量出来的宽高
        var measureWidth = 0
        var measureHeight = 0

        //得到合适宽度：
        var index: String //每个要绘制的index内容
        for (i in mIndexDataList.indices) {
            index = mIndexDataList[i]
            mPaint.getTextBounds(index, 0, index.length, indexBounds) //测量计算文字所在矩形，可以得到宽高
            measureWidth = indexBounds.width().coerceAtLeast(measureWidth) //循环结束后，得到index的最大宽度
            measureHeight =
                indexBounds.height().coerceAtLeast(measureHeight) //循环结束后，得到index的最大高度，然后*size
        }
        measureHeight *= mIndexDataList.size
        when (wMode) {
            MeasureSpec.EXACTLY -> measureWidth = wSize
            MeasureSpec.AT_MOST -> measureWidth =
                measureWidth.coerceAtMost(wSize) //wSize此时是父控件能给子View分配的最大空间
            MeasureSpec.UNSPECIFIED -> {
            }
        }

        //得到合适的高度：
        when (hMode) {
            MeasureSpec.EXACTLY -> measureHeight = hSize
            MeasureSpec.AT_MOST -> measureHeight =
                measureHeight.coerceAtMost(hSize) //wSize此时是父控件能给子View分配的最大空间
            MeasureSpec.UNSPECIFIED -> {
            }
        }

        setMeasuredDimension(measureWidth, measureHeight)

    }

    override fun onDraw(canvas: Canvas?) {
        val t = paddingTop //top的基准点(支持padding)
        var index: String //每个要绘制的index内容
        for (i in mIndexDataList.indices) {
            index = mIndexDataList[i]
            //获得画笔的FontMetrics，用来计算baseLine。因为drawText的y坐标，代表的是绘制的文字的baseLine的位置
            val fontMetrics = mPaint.fontMetrics
            //计算出在每格index区域，竖直居中的baseLine值
            val baseline = ((mGapHeight - fontMetrics.bottom - fontMetrics.top) / 2).toInt()
            //调用drawText，居中显示绘制index
            canvas?.drawText(
                index, mWidth / 2 - mPaint.measureText(index) / 2,
                (t + mGapHeight * i + baseline).toFloat(), mPaint
            )

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                val y = event.y
                //通过计算判断落点在哪个区域：
                var pressI = ((y - paddingTop) / mGapHeight).toInt()
                //边界处理（在手指move时，有可能已经移出边界，防止越界）
                if (pressI < 0) {
                    pressI = 0
                } else if (pressI >= mIndexDataList.size) {
                    pressI = mIndexDataList.size - 1
                }
                //回调监听器
                if (pressI > -1 && pressI < mIndexDataList.size) {
                    mOnIndexPressedListener?.onIndexPressed(pressI, mIndexDataList[pressI])
                }
            }
            MotionEvent.ACTION_UP -> {
                //回调监听器
                mOnIndexPressedListener?.onMotionEventEnd()
            }
        }
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        //解决源数据为空 或者size为0的情况,
        if (mIndexDataList.isEmpty()) {
            return
        }
        computeGapHeight()
    }

    private fun setOnIndexPressedListener(listener: Builder.() -> Unit) {
        this.mOnIndexPressedListener = Builder().also(listener)
    }

    /**
     * 显示当前被按下的index的TextView
     */
    fun setPressedShowTextView(mPressedShowTextView: AppCompatTextView): IndexBar {
        this.mPressedShowTextView = mPressedShowTextView
        return this
    }

    fun setLayoutManager(mLayoutManager: CustomLinearLayoutManager): IndexBar {
        this.mLayoutManager = mLayoutManager
        return this
    }

    /**
     * 一定要在设置数据源 {@link #setSourceDataList(List)} 之前调用
     */
    fun setNeedRealIndex(needRealIndex: Boolean): IndexBar {
        this.isNeedRealIndex = needRealIndex
        initIndexDataList()
        return this
    }

    private fun initIndexDataList() {
        mIndexDataList = if (isNeedRealIndex) {
            ArrayList()
        } else {
            arrayListOf(
                *context.resources.getStringArray(R.array.slide_bar_value_list)
            )
        }
    }

    fun setSourceDates(mSourceDataList: MutableList<out BaseIndexPinyinBean>): IndexBar {
        this.mSourceDataList = mSourceDataList
        initSourceDataList()
        return this
    }

    /**
     * 初始化原始数据源，并取出索引数据源
     */
    private fun initSourceDataList() {
        // 解决源数据为空 或者size为0的情况,
        if (mSourceDataList.isEmpty()) {
            return
        }
        if (!isSourceDataAlreadySorted) {
            //排序sourceDataList
            mDataHelper.sortSourceDates(mSourceDataList)
        } else {
            //汉语->拼音
            mDataHelper.convert(mSourceDataList)
            //拼音->tag
            mDataHelper.fillIndexTag(mSourceDataList)
        }
        if (isNeedRealIndex) {
            mDataHelper.getSortedIndexDates(mSourceDataList, mIndexDataList)
            computeGapHeight()
        }
    }

    /**
     * 以下情况调用：
     * 1 在数据源改变
     * 2 控件size改变时
     * 计算gapHeight
     */
    private fun computeGapHeight() {
        mGapHeight = (mHeight - paddingTop - paddingBottom) / mIndexDataList.size
    }

    private fun getPosByTag(tag: String): Int {
        // 解决源数据为空 或者size为0的情况,
        if (mSourceDataList.isEmpty()) {
            return -1
        }
        if (tag.isEmpty()) {
            return -1
        }
        for (i in mSourceDataList.indices) {
            if (tag == mSourceDataList[i].baseIndexTag) {
                return i + headerViewCount
            }
        }
        return -1
    }


    interface IndexPressedListener {
        /**
         * 当某个Index被按下
         */
        fun onIndexPressed(index: Int, text: String)

        /**
         *当触摸事件结束（UP CANCEL）
         */
        fun onMotionEventEnd()
    }

    class Builder : IndexPressedListener {

        private lateinit var indexPressed: (index: Int, text: String) -> Unit
        private lateinit var motionEventEnd: () -> Unit
        fun onIndexPressed(listener: (index: Int, text: String) -> Unit) {
            this.indexPressed = listener
        }

        fun onMotionEventEnd(listener: () -> Unit) {
            this.motionEventEnd = listener
        }


        override fun onIndexPressed(index: Int, text: String) {
            indexPressed.invoke(index, text)
        }

        override fun onMotionEventEnd() {
            motionEventEnd.invoke()
        }

    }
}