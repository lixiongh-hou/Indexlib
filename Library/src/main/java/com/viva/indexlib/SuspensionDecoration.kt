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
 * isShowSuspension == true  : 有头部 + 可悬停
 * isShowSuspension == false : 有头部 + 不悬停
 */
class SuspensionDecoration(
    context: Context,
    dataList: MutableList<out ISuspensionInterface>
) : RecyclerView.ItemDecoration() {

    private var mDataList = dataList
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mBounds = Rect()

    private var mTitleHeight: Int
    private var mTitleFontSize: Int
    private var mHeaderViewCount = 0

    private var leftBottomLine = 0f
    private var leftTitleFont = 0f
    private var isBold = false

    private var colorTitleBg = Color.parseColor("#EDEDED")
    private var colorTitleBottomLine = Color.parseColor("#EDEDED")
    private var colorTitleFont = Color.parseColor("#666666")

    init {
        mTitleHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            50f,
            context.resources.displayMetrics
        ).toInt()

        mTitleFontSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            12f,
            context.resources.displayMetrics
        ).toInt()

        mPaint.textSize = mTitleFontSize.toFloat()
    }

    /* ---------------- 对外配置 ---------------- */

    fun setDataList(list: MutableList<out ISuspensionInterface>): SuspensionDecoration {
        mDataList = list
        return this
    }

    fun setTitleHeight(context: Context, heightDp: Int): SuspensionDecoration {
        mTitleHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            heightDp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
        return this
    }

    fun setTitleFontSize(context: Context, sizeSp: Int): SuspensionDecoration {
        mTitleFontSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sizeSp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
        mPaint.textSize = mTitleFontSize.toFloat()
        return this
    }

    fun setBottomLine(left: Float) = apply { leftBottomLine = left }
    fun setTitleFont(left: Float) = apply { leftTitleFont = left }
    fun setTitleBgColor(color: Int) = apply { colorTitleBg = color }
    fun setBottomLineColor(color: Int) = apply { colorTitleBottomLine = color }
    fun setTitleFontColor(color: Int) = apply { colorTitleFont = color }
    fun isTitleFontBold(bold: Boolean) = apply { isBold = bold }

    /* ---------------- 普通分组头绘制 ---------------- */

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val position = params.viewLayoutPosition - mHeaderViewCount
            if (position > -1) {
                if (position !in mDataList.indices) continue

                if (needDrawTitle(position)) {
                    drawTitleArea(c, left, right, child, params, position)
                }
            }

        }
    }

    private fun needDrawTitle(position: Int): Boolean {
        if (position == 0) return true
        return mDataList[position].getSuspensionTag() !=
                mDataList[position - 1].getSuspensionTag()
    }

    private fun drawTitleArea(
        c: Canvas,
        left: Int,
        right: Int,
        child: View,
        params: RecyclerView.LayoutParams,
        position: Int
    ) {
        // 背景
        mPaint.color = colorTitleBg
        c.drawRect(
            left.toFloat(),
            (child.top - params.topMargin - mTitleHeight).toFloat(),
            right.toFloat(),
            (child.top - params.topMargin).toFloat(),
            mPaint
        )

        // 底部分割线
        mPaint.color = colorTitleBottomLine
        c.drawRect(
            left + leftBottomLine,
            (child.top - params.topMargin - 1).toFloat(),
            right - 120f,
            (child.top - params.topMargin).toFloat(),
            mPaint
        )

        // 文本
        val tag = mDataList[position].getSuspensionTag()
        mPaint.color = colorTitleFont
        mPaint.isFakeBoldText = isBold
        mPaint.getTextBounds(tag, 0, tag.length, mBounds)

        c.drawText(
            tag,
            child.paddingLeft + leftTitleFont,
            (child.top - params.topMargin -
                    (mTitleHeight / 2 - mBounds.height() / 2)).toFloat(),
            mPaint
        )
    }

    /* ---------------- 悬浮头绘制（仅 true） ---------------- */

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val lm = parent.layoutManager as? LinearLayoutManager ?: return
        val pos = lm.findFirstVisibleItemPosition() - mHeaderViewCount
        if (pos !in mDataList.indices) return

        val cur = mDataList[pos]

        // ❗false：有头部但不悬停
        if (!cur.isShowSuspension()) return

        val tag = cur.getSuspensionTag()
        val child =
            parent.findViewHolderForLayoutPosition(pos + mHeaderViewCount)?.itemView ?: return

        var translate = false

        if (pos + 1 < mDataList.size) {
            val next = mDataList[pos + 1]
            if (next.isShowSuspension()
                && tag != next.getSuspensionTag()
                && child.height + child.top < mTitleHeight
            ) {
                c.save()
                translate = true
                c.translate(0f, (child.height + child.top - mTitleHeight).toFloat())
            }
        }

        // 悬浮背景
        mPaint.color = colorTitleBg
        c.drawRect(
            parent.paddingLeft.toFloat(),
            parent.paddingTop.toFloat(),
            (parent.width - parent.paddingRight).toFloat(),
            (parent.paddingTop + mTitleHeight).toFloat(),
            mPaint
        )

        // 底线
        mPaint.color = colorTitleBottomLine
        c.drawRect(
            parent.paddingLeft + leftBottomLine,
            (parent.paddingTop + mTitleHeight - 1).toFloat(),
            parent.width - parent.paddingRight - 120f,
            (parent.paddingTop + mTitleHeight).toFloat(),
            mPaint
        )

        // 文本
        mPaint.color = colorTitleFont
        mPaint.isFakeBoldText = isBold
        mPaint.getTextBounds(tag, 0, tag.length, mBounds)

        c.drawText(
            tag,
            child.paddingLeft + leftTitleFont,
            (parent.paddingTop + mTitleHeight -
                    (mTitleHeight / 2 - mBounds.height() / 2)).toFloat(),
            mPaint
        )

        if (translate) c.restore()
    }

    /* ---------------- Item 偏移 ---------------- */

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position =
            (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition - mHeaderViewCount

        if (position !in mDataList.indices) return

        if (needDrawTitle(position)) {
            outRect.top = mTitleHeight
        }
    }
}
