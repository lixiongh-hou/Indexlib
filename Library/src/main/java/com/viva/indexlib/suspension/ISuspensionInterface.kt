package com.viva.indexlib.suspension

/**
 * @author 李雄厚
 *
 * 悬停接口
 */
interface ISuspensionInterface {

    /**
     * 是否需要显示悬停title
     */
    fun isShowSuspension(): Boolean

    /**
     * 悬停的title
     */
    fun getSuspensionTag(): String
}