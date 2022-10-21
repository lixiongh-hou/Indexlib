package com.viva.indexlib.indexBar

import com.viva.indexlib.suspension.ISuspensionInterface

/**
 * @author 李雄厚
 *
 *
 */
abstract class BaseIndexBean : ISuspensionInterface {

    /**
     * 所属的分类（名字的汉语拼音首字母）
     *
     * 如 [李**] 取 L
     */
    var baseIndexTag = ""

    fun setBaseIndexTag(baseIndexTag: String): BaseIndexBean {
        this.baseIndexTag = baseIndexTag
        return this
    }

    override fun getSuspensionTag(): String = baseIndexTag

    /**
     * 默认不悬停
     */
    override fun isShowSuspension(): Boolean = false
}