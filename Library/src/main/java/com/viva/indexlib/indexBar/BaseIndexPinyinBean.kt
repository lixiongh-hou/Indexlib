package com.viva.indexlib.indexBar

/**
 * @author 李雄厚
 *
 *
 */
abstract class BaseIndexPinyinBean : BaseIndexBean() {

    /**
     * 名字的拼音
     *
     * 如 [李**] 取 Li**
     */
    var baseIndexPinyin = ""

    fun setBaseIndexPinyin(baseIndexPinyin: String): BaseIndexPinyinBean {
        this.baseIndexPinyin = baseIndexPinyin
        return this
    }

    /**
     * 顶部是否需要被转化成拼音，类似微信头部那种就不需要 美团的也不需要, 默认需要
     */
    open fun isNeedToPinyin(): Boolean {
        return true
    }

    /**
     * 需要转化成拼音的目标字段
     */
    abstract fun getTarget(): String
}