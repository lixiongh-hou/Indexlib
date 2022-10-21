package com.viva.indexlib

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.viva.indexlib.suspension.ISuspensionInterface

/**
 * @author 李雄厚
 *
 * @features ***
 */
class SuspensionDecoration(context: Context, dataList: MutableList<out ISuspensionInterface>) :
    RecyclerView.ItemDecoration() {
    /**
     * title字体大小
     */
    private var mTitleFontSize = 0
    private var mDataList: MutableList<out ISuspensionInterface> = ArrayList()
    private val mPaint by lazy { Paint() }
    private val mBounds by lazy { Rect() }
    private var mTitleHeight = 0
    private var mHeaderViewCount = 0
    private var leftBottomLine = 0F
    private var leftTitleFont = 0F
    private var isBold = false

    private var colorTitleBg = Color.parseColor("#EDEDED")
    private var colorTitleBottomLine = Color.parseColor("#EDEDED")
    private var colorTitleFont = Color.parseColor("#666666")

    init {
        this.mDataList = dataList
        mTitleHeight =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                50F,
                context.resources.displayMetrics
            )
                .toInt()
        mTitleFontSize =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                12F,
                context.resources.displayMetrics
            )
                .toInt()
        mPaint.textSize = mTitleFontSize.toFloat()
        mPaint.isAntiAlias = true
    }

    fun setDataList(mDataList: MutableList<out ISuspensionInterface>): SuspensionDecoration {
        this.mDataList = mDataList
        return this
    }

    fun setTitleHeight(context: Context, height: Int): SuspensionDecoration {
        mTitleHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            height.toFloat(),
            context.resources.displayMetrics
        ).toInt()
        return this
    }

    fun setTitleFontSize(context: Context, size: Int): SuspensionDecoration {
        mTitleFontSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            size.toFloat(),
            context.resources.displayMetrics
        ).toInt()
        return this
    }

    fun setBottomLine(left: Float): SuspensionDecoration {
        leftBottomLine = left
        return this
    }

    fun setTitleFont(left: Float): SuspensionDecoration {
        leftTitleFont = left
        return this
    }

    fun setTitleBgColor(color: Int): SuspensionDecoration {
        colorTitleBg = color
        return this
    }

    fun setBottomLineColor(color: Int): SuspensionDecoration {
        colorTitleBottomLine = color
        return this
    }

    fun setTitleFontColor(color: Int): SuspensionDecoration {
        colorTitleFont = color
        return this
    }

    fun isTitleFontBold(isBold: Boolean): SuspensionDecoration {
        this.isBold = isBold
        return this
    }


    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                .layoutParams as RecyclerView.LayoutParams
            var position = params.viewLayoutPosition
            position -= mHeaderViewCount
            //pos为1，size为1，1>0? true
            if (mDataList.isEmpty() || position > mDataList.size - 1 || position < 0 || !mDataList[position].isShowSuspension()) {
                continue  //越界
            }
            //我记得Rv的item position在重置时可能为-1.保险点判断一下吧
            if (position > -1) {
                if (position == 0) {
                    //等于0肯定要有title的
                    drawTitleArea(c, left, right, child, params, position)
                } else { //其他的通过判断
                    if (mDataList[position].getSuspensionTag() != mDataList[position - 1].getSuspensionTag()) {
                        //不为空 且跟前一个tag不一样了，说明是新的分类，也要title
                        drawTitleArea(c, left, right, child, params, position)
                    }
                }
            }
        }
    }

    private fun drawTitleArea(
        c: Canvas,
        left: Int,
        right: Int,
        child: View,
        params: RecyclerView.LayoutParams,
        position: Int
    ) {
        //最先调用，绘制在最下层
        mPaint.color = colorTitleBg
        c.drawRect(
            left.toFloat(),
            (child.top - params.topMargin - mTitleHeight).toFloat(),
            right.toFloat(),
            (child.top - params.topMargin).toFloat(),
            mPaint
        )
        mPaint.color = colorTitleBottomLine
        c.drawRect(
            left.toFloat() + leftBottomLine,
            (child.top - params.topMargin - 1).toFloat(),
            right.toFloat() - 120,
            (child.top - params.topMargin).toFloat(),
            mPaint
        )
        mPaint.color = colorTitleFont
        mPaint.getTextBounds(
            mDataList[position].getSuspensionTag(),
            0,
            mDataList[position].getSuspensionTag().length,
            mBounds
        )
        mPaint.isFakeBoldText = isBold
        c.drawText(
            mDataList[position].getSuspensionTag(),
            (child.paddingLeft + leftTitleFont),
            (child.top - params.topMargin - (mTitleHeight / 2 - mBounds.height() / 2)).toFloat(),
            mPaint
        )
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        //最后调用 绘制在最上层
        var pos = (parent.layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
        pos -= mHeaderViewCount
        //pos为1，size为1，1>0? true
        if (mDataList.isEmpty() || pos > mDataList.size - 1 || pos < 0 || !mDataList[pos].isShowSuspension()) {
            return  //越界
        }
        val tag: String = mDataList[pos].getSuspensionTag()
        //View child = parent.getChildAt(pos);
        //出现一个奇怪的bug，有时候child为空，所以将 child = parent.getChildAt(i)。-》 parent.findViewHolderForLayoutPosition(pos).itemView
        val child = parent.findViewHolderForLayoutPosition(pos + mHeaderViewCount)!!.itemView
        var flag = false //定义一个flag，Canvas是否位移过的标志
        if (pos + 1 < mDataList.size) {//防止数组越界（一般情况不会出现）
            //当前第一个可见的Item的tag，不等于其后一个item的tag，说明悬浮的View要切换了
            if (tag != mDataList[pos + 1].getSuspensionTag()) {
                //当第一个可见的item在屏幕中还剩的高度小于title区域的高度时，我们也该开始做悬浮Title的“交换动画”
                if (child.height + child.top < mTitleHeight) {
                    //每次绘制前 保存当前Canvas状态，
                    c.save()
                    flag = true

                    //一种头部折叠起来的视效，个人觉得也还不错~
                    //可与123行 c.drawRect 比较，只有bottom参数不一样，由于 child.getHeight() + child.getTop() < mTitleHeight，所以绘制区域是在不断的减小，有种折叠起来的感觉
                    //c.clipRect(parent.getPaddingLeft(), parent.getPaddingTop(), parent.getRight() - parent.getPaddingRight(), parent.getPaddingTop() + child.getHeight() + child.getTop());

                    //类似饿了么点餐时,商品列表的悬停头部切换“动画效果”
                    //上滑时，将canvas上移 （y为负数） ,所以后面canvas 画出来的Rect和Text都上移了，有种切换的“动画”感觉
                    c.translate(0f, (child.height + child.top - mTitleHeight).toFloat())
                }
            }
        }
        mPaint.color = colorTitleBg
        c.drawRect(
            parent.paddingLeft.toFloat(),
            parent.paddingTop.toFloat(),
            (parent.right - parent.paddingRight).toFloat(),
            (parent.paddingTop + mTitleHeight).toFloat(),
            mPaint
        )

        mPaint.color = colorTitleBottomLine
        c.drawRect(
            parent.paddingLeft.toFloat() + leftBottomLine,
            (parent.paddingTop + mTitleHeight - 1).toFloat(),
            (parent.right - parent.paddingRight - 120).toFloat(),
            (parent.paddingTop + mTitleHeight).toFloat(),
            mPaint
        )

        mPaint.color = colorTitleFont
        mPaint.getTextBounds(tag, 0, tag.length, mBounds)
        mPaint.isFakeBoldText = isBold
        c.drawText(
            tag, (child.paddingLeft + leftTitleFont), (
                    parent.paddingTop + mTitleHeight - (mTitleHeight / 2 - mBounds.height() / 2)).toFloat(),
            mPaint
        )

        if (flag) c.restore() //恢复画布到之前保存的状态


    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        //super里会先设置0 0 0 0
        super.getItemOffsets(outRect, view, parent, state)
        var position = (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
        position -= mHeaderViewCount
        //pos为1，size为1，1>0? true
        if (mDataList.isEmpty() || position > mDataList.size - 1) {
            return  //越界
        }
        //我记得Rv的item position在重置时可能为-1.保险点判断一下吧
        if (position > -1) {
            val titleCategoryInterface: ISuspensionInterface = mDataList[position]
            //等于0肯定要有title的,
            // 2016 11 07 add 考虑到headerView 等于0 也不应该有title
            // 2016 11 10 add 通过接口里的isShowSuspension() 方法，先过滤掉不想显示悬停的item
            if (titleCategoryInterface.isShowSuspension()) {
                if (position == 0) {
                    outRect[0, mTitleHeight, 0] = 0
                } else {
                    //其他的通过判断
                    if (titleCategoryInterface.getSuspensionTag() != mDataList[position - 1].getSuspensionTag()) {
                        //不为空 且跟前一个tag不一样了，说明是新的分类，也要title
                        outRect[0, mTitleHeight, 0] = 0
                    }
                }
            }
        }
    }
}