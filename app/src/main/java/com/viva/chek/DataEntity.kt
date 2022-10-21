package com.viva.chek

import com.viva.indexlib.indexBar.BaseIndexPinyinBean

/**
 * @author 李雄厚
 *
 *
 */
class DataEntity(
    val name: String = ""
) : BaseIndexPinyinBean() {

    /**
     * 是否是最上面的 不需要被转化成拼音的
     */
    private var isTop: Boolean = true

    /**
     * 是否需要悬停
     */
    private var isSuspension: Boolean = true

    fun isTop(top: Boolean): DataEntity{
        this.isTop = top
        return this
    }

    fun isSuspension(suspension: Boolean): DataEntity{
        this.isSuspension = suspension
        return this
    }

    override fun getTarget(): String {
        if (name.isNotEmpty()) {
            return name
        }
        return "TOP"
    }

    override fun isShowSuspension(): Boolean {
        return isSuspension
    }

    override fun isNeedToPinyin(): Boolean {
        return isTop
    }
}