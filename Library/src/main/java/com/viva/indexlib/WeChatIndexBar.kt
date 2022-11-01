package com.viva.indexlib

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import com.viva.indexlib.indexBar.BaseIndexPinyinBean
import com.viva.indexlib.indexBar.IndexBarDataHelperImpl
import com.viva.indexlib.suspension.IIndexBarDataHelper
import java.util.ArrayList

/**
 * @author 李雄厚
 *
 * 仿微信IndexBar
 */
class WeChatIndexBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * 背景色
     */
    private var mBackgroundColor = Color.parseColor("#F9F9F9")

    /**
     * 字体颜色
     */
    private var mTextColor = Color.parseColor("#969696")

    /**
     * 字体大小
     */
    private var mTextSize = 0

    /**
     * 选中字体颜色
     */
    private var mSelectTextColor = Color.parseColor("#FFFFFF")

    /**
     * 选中字体大小
     */
    private var mSelectTextSize = 0

    /**
     * 提示字体颜色
     */
    private var mHintTextColor = Color.parseColor("#FFFFFF")

    /**
     * 提示字体大小
     */
    private var mHintTextSize = 0

    /**
     * 提示圆半径
     */
    private var mHintCircleRadius = 0

    /**
     * 提示圆颜色
     */
    private var mHintCircleColor = Color.parseColor("#bef9b81b")

    /**
     * 波浪半径
     */
    private var mWaveRadius = 0

    /**
     * 整体上下边距
     */
    private var mContentPadding = 0

    /**
     * 整体的左右边距
     */
    private var mBarPadding = 0

    /**
     * 整体宽度
     */
    private var mBarWidth = 0

    private val mTextPaint by lazy { TextPaint() }
    private var mSlideBarRect: RectF? = null
    private val mPaint by lazy { Paint() }
    private val mWavePaint by lazy { Paint() }
    private var mSelect = 0
    private var mPreSelect = 0
    private var mNewSelect = 0
    private var mLetters: List<String>? = null
    private var mRatioAnimator: ValueAnimator? = null
    private var mAnimationRatio = 0F
    private var letterChange: ((String?) -> Unit)? = null

    /**
     * Adapter的数据源
     */
    private var mSourceDates: List<BaseIndexPinyinBean>? = null

    /**
     * 源数据 已经有序？
     */
    var isSourceDatesAlreadySorted = false

    /**
     * 以下是帮助类
     */
    private var mDataHelper: IIndexBarDataHelper? = null

    /**
     * Rv的布局管理器
     */
    private var mLayoutManager: CustomLinearLayoutManager? = null

    /**
     * 是否需要根据实际的数据来生成索引数据源（例如 只有 A B C 三种tag，那么索引栏就 A B C 三项）
     */
    private var isNeedRealIndex = false

    var headerViewCount = 0

    private var mBitmap: Bitmap? = null

    init {
        initAttribute(attrs, defStyleAttr)
        initData()
    }

    private fun initAttribute(attrs: AttributeSet?, defStyleAttr: Int) {
        val typeArray =
            context.obtainStyledAttributes(attrs, R.styleable.WeChatIndexBar, defStyleAttr, 0)
        mBackgroundColor = typeArray.getColor(
            R.styleable.WeChatIndexBar_backgroundColor,
            mBackgroundColor
        )
        mTextColor = typeArray.getColor(
            R.styleable.WeChatIndexBar_textColor,
            mTextColor
        )
        mTextSize = typeArray.getDimensionPixelOffset(
            R.styleable.WeChatIndexBar_textSize,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 10F,
                resources.displayMetrics
            ).toInt()
        )
        mSelectTextColor = typeArray.getColor(
            R.styleable.WeChatIndexBar_selectTextColor,
            mSelectTextColor
        )
        mSelectTextSize = typeArray.getDimensionPixelOffset(
            R.styleable.WeChatIndexBar_selectTextSize,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 10F,
                resources.displayMetrics
            ).toInt()
        )
        mHintTextColor = typeArray.getColor(
            R.styleable.WeChatIndexBar_hintTextColor,
            mHintTextColor
        )
        mHintTextSize = typeArray.getDimensionPixelOffset(
            R.styleable.WeChatIndexBar_hintTextSize,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 28F,
                resources.displayMetrics
            ).toInt()
        )
        mHintCircleRadius = typeArray.getDimensionPixelOffset(
            R.styleable.WeChatIndexBar_hintCircleRadius,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 28F,
                resources.displayMetrics
            ).toInt()
        )
        mHintCircleColor = typeArray.getColor(
            R.styleable.WeChatIndexBar_hintCircleColor,
            mHintCircleColor
        )
        mWaveRadius = typeArray.getDimensionPixelOffset(
            R.styleable.WeChatIndexBar_waveRadius,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 20F,
                resources.displayMetrics
            ).toInt()
        )
        mContentPadding = typeArray.getDimensionPixelOffset(
            R.styleable.WeChatIndexBar_contentPadding,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 2F,
                resources.displayMetrics
            ).toInt()
        )
        mBarPadding = typeArray.getDimensionPixelOffset(
            R.styleable.WeChatIndexBar_barPadding,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 6F,
                resources.displayMetrics
            ).toInt()
        )
        mBarWidth = typeArray.getDimensionPixelOffset(
            R.styleable.WeChatIndexBar_barWidth,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 0F,
                resources.displayMetrics
            ).toInt()
        )
        if (mBarWidth == 0) {
            mBarWidth = 2 * mTextSize
        }
        typeArray.recycle()
    }

    private fun initData() {
        mTextPaint.isAntiAlias = true
        mTextPaint.isFakeBoldText = true
        mPaint.isAntiAlias = true
        mWavePaint.isAntiAlias = true
        mSelect = -1
        mBitmap = BitmapFactory.decodeResource(resources, R.drawable.water_drop_icon)
        letterChange = { letter ->
            //滑动Rv
            if (mLayoutManager != null) {
                val position = getPosByTag(letter ?: "")
                if (position != -1) {
                    mLayoutManager!!.scrollToPositionWithOffset(position, 0)
                }
            }
        }
        mDataHelper = IndexBarDataHelperImpl()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mSlideBarRect == null) {
            mSlideBarRect = RectF()
        }
        val contentLeft = measuredWidth - mBarWidth - mBarPadding.toFloat()
        val contentRight = measuredWidth - mBarPadding.toFloat()
        val contentTop = mBarPadding.toFloat()
        val contentBottom = (measuredHeight - mBarPadding).toFloat()
        mSlideBarRect?.set(contentLeft, contentTop, contentRight, contentBottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //绘制slide bar 上字母列表
        drawLetters(canvas)
        //绘制选中时的提示信息(圆＋文字)
        drawHint(canvas)
        //绘制选中的slide bar上的那个文字
        drawSelect(canvas)
    }

    /**
     * 绘制slide bar 上字母列表
     */
    private fun drawLetters(canvas: Canvas) {
        //绘制圆角矩形
        mPaint.style = Paint.Style.FILL
        mPaint.color = mBackgroundColor
        canvas.drawRoundRect(mSlideBarRect!!, mBarWidth / 2.0f, mBarWidth / 2.0f, mPaint)
        //顺序绘制文字
        val itemHeight =
            (mSlideBarRect!!.bottom - mSlideBarRect!!.top - mContentPadding * 2) / mLetters!!.size
        for (i in mLetters!!.indices) {
            val baseLine = getTextBaseLineByCenter(
                mSlideBarRect!!.top + mContentPadding + itemHeight * i + itemHeight / 2,
                mTextPaint,
                mTextSize
            )
            mTextPaint.color = mTextColor
            mTextPaint.textSize = mTextSize.toFloat()
            mTextPaint.textAlign = Paint.Align.CENTER

            val pointX =
                mSlideBarRect!!.left + (mSlideBarRect!!.right - mSlideBarRect!!.left) / 2.0f
            canvas.drawText(mLetters!![i], pointX, baseLine, mTextPaint)
        }
    }

    /**
     * 绘制选中时的提示信息(圆＋文字)
     */
    private fun drawHint(canvas: Canvas) {
        val itemHeight =
            (mSlideBarRect!!.bottom - mSlideBarRect!!.top - mContentPadding * 2) / mLetters!!.size
        //选择文字的Y轴位置
        val baseLine = getTextBaseLineByCenter(
            mSlideBarRect!!.top + mContentPadding + itemHeight * mSelect + itemHeight / 2,
            mTextPaint,
            mTextSize
        )
        //选择文字的X轴位置  图片下载大小是138
        val circleCenterX =
            measuredWidth + 138 - (2.0f * mWaveRadius + 2.0f * 138) * mAnimationRatio
        //选择文字的X轴位置
        val textCenterX =
            measuredWidth + mHintCircleRadius - (2.0f * mWaveRadius + 2.0f * mHintCircleRadius) * mAnimationRatio
        if (mAnimationRatio >= 0.9f && mSelect != -1) {
            mWavePaint.style = Paint.Style.FILL
            mWavePaint.color = mHintCircleColor
            canvas.drawBitmap(mBitmap!!, circleCenterX - 50, baseLine - 138 / 2, mWavePaint)
            val target = mLetters!![mSelect]
            mTextPaint.color = mHintTextColor
            mTextPaint.textSize = mHintTextSize.toFloat()
            mTextPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(target, textCenterX - 50, baseLine + mHintTextSize / 3, mTextPaint)
        }
    }

    /***
     * 绘制选中的slide bar上的那个文字
     */
    private fun drawSelect(canvas: Canvas) {
        if (mSelect != -1) {
            val itemHeight =
                (mSlideBarRect!!.bottom - mSlideBarRect!!.top - mContentPadding * 2) / mLetters!!.size
            //选择文字的Y轴位置
            val baseLine = getTextBaseLineByCenter(
                mSlideBarRect!!.top + mContentPadding + itemHeight * mSelect + itemHeight / 2,
                mTextPaint,
                mTextSize
            )
            //选择文字的X轴位置
            val pointX =
                mSlideBarRect!!.left + (mSlideBarRect!!.right - mSlideBarRect!!.left) / 2.0f

            mWavePaint.style = Paint.Style.FILL
            mWavePaint.color = Color.parseColor("#70C160")
            canvas.drawCircle(
                pointX,
                baseLine - mTextSize / 3,
                mTextSize.toFloat() - mTextSize.toFloat() / 3,
                mWavePaint
            )

            mTextPaint.color = mSelectTextColor
            mTextPaint.textSize = mSelectTextSize.toFloat()
            mTextPaint.textAlign = Paint.Align.CENTER

            canvas.drawText(mLetters!![mSelect], pointX, baseLine, mTextPaint)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val y = event.y
        val x = event.x
        mPreSelect = mSelect
        mNewSelect = (y / (mSlideBarRect!!.bottom - mSlideBarRect!!.top) * mLetters!!.size).toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //保证down的时候在bar区域才相应事件
                if (x < mSlideBarRect!!.left || y < mSlideBarRect!!.top || y > mSlideBarRect!!.bottom) {
                    return false
                }
                startAnimator(1.0F)
            }
            MotionEvent.ACTION_MOVE -> {
                if (mPreSelect != mNewSelect && mNewSelect >= 0 && mNewSelect < mLetters!!.size) {
                    mSelect = mNewSelect
                    letterChange?.invoke(mLetters!![mSelect])
                }
                invalidate()
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                startAnimator(0F)
                mSelect = -1
            }
        }
        return true
    }

    private fun startAnimator(value: Float) {
        if (mRatioAnimator == null) {
            mRatioAnimator = ValueAnimator()
        }
        mRatioAnimator?.cancel()
        mRatioAnimator?.setFloatValues(value)
        mRatioAnimator?.addUpdateListener {
            mAnimationRatio = it.animatedValue as Float
            //球弹到位的时候，并且点击的位置变了，即点击的时候显示当前选择位置
            if (mAnimationRatio == 1f && mPreSelect != mNewSelect) {
                if (mNewSelect >= 0 && mNewSelect < mLetters!!.size) {
                    mSelect = mNewSelect
                    letterChange?.invoke((mLetters!![mNewSelect]))
                }
            }
            invalidate()
        }
        mRatioAnimator?.duration = 50
        mRatioAnimator?.start()
    }

    /**
     * 显示当前已有索引还是全部索引数据初始化
     */
    private fun initIndexDates(){
        mLetters = if (isNeedRealIndex) {
            ArrayList()
        } else {
            listOf(*context.resources.getStringArray(R.array.slide_bar_value_list)
            )
        }
    }

    /**
     * 一定要在设置数据源[setSourceDates]之前调用
     * 显示当前已有索引还是全部索引
     * @param needRealIndex
     * @return
     */
    fun setNeedRealIndex(needRealIndex: Boolean): WeChatIndexBar {
        isNeedRealIndex = needRealIndex
        initIndexDates()
        return this
    }

    /**
     * 设置数据源
     */
    fun setSourceDates(mSourceDates: List<BaseIndexPinyinBean>): WeChatIndexBar {
        this.mSourceDates = mSourceDates
        //对数据源进行初始化
        initSourceDates()
        return this
    }

    /**
     * 设置Rv的布局管理器
     */
    fun setLayoutManager(mLayoutManager: CustomLinearLayoutManager): WeChatIndexBar {
        this.mLayoutManager = mLayoutManager
        return this
    }

    /**
     * 源数据 是否已经有序
     *
     * @param sourceDatesAlreadySorted
     * @return
     */
    fun setSourceDatesAlreadySorted(sourceDatesAlreadySorted: Boolean): WeChatIndexBar {
        isSourceDatesAlreadySorted = sourceDatesAlreadySorted
        return this
    }

    /**
     * 设置HeaderView的Count
     *
     * @param headerViewCount
     * @return
     */
    fun setHeaderViewCount(headerViewCount: Int): WeChatIndexBar{
        this.headerViewCount = headerViewCount
        return this
    }

    /**
     * 初始化原始数据源，并取出索引数据源
     *
     * @return
     */
    private fun initSourceDates() {
        // 解决源数据为空 或者size为0的情况,
        if (null == mSourceDates || mSourceDates!!.isEmpty()) {
            return
        }
        if (!isSourceDatesAlreadySorted) {
            //排序sourceDates
            mDataHelper?.sortSourceDates(mSourceDates!!)
        } else {
            //汉语->拼音
            mDataHelper?.convert(mSourceDates!!)
            //拼音->tag
            mDataHelper?.fillIndexTag(mSourceDates!!)
        }
        if (isNeedRealIndex) {
            mDataHelper!!.getSortedIndexDates(mSourceDates as MutableList<out BaseIndexPinyinBean>,
                mLetters as MutableList<String>
            )
        }
    }

    /**
     * 根据传入的pos返回tag
     *
     * @param tag
     * @return
     */
    fun getPosByTag(tag: String): Int {
        // 解决源数据为空 或者size为0的情况,
        if (null == mSourceDates || mSourceDates!!.isEmpty()) {
            return -1
        }
        if (TextUtils.isEmpty(tag)) {
            return -1
        }
        for (i in mSourceDates!!.indices) {
            if (tag == mSourceDates!![i].baseIndexTag) {
                return i + headerViewCount
            }
        }
        return -1
    }

    /**
     * 给定文字的center获取文字的base line
     */
    private fun getTextBaseLineByCenter(center: Float, paint: TextPaint, size: Int): Float {
        paint.textSize = size.toFloat()
        val fontMetrics: Paint.FontMetrics = paint.fontMetrics
        val height: Float = fontMetrics.bottom - fontMetrics.top
        return center + height / 2 - fontMetrics.bottom
    }
}