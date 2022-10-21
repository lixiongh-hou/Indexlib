package com.viva.indexlib.suspension

import com.viva.indexlib.indexBar.BaseIndexPinyinBean

/**
 * @author 李雄厚
 *
 * 处理数据工具接口
 */
interface IIndexBarDataHelper {

    /**
     * 汉语 -> 拼音
     */
    fun convert(data: List<BaseIndexPinyinBean>): IIndexBarDataHelper

    /**
     * 拼音 -> tag
     */
    fun fillIndexTag(data: List<BaseIndexPinyinBean>): IIndexBarDataHelper

    /**
     * 对源数据进行排序（RecyclerView）
     */
    fun sortSourceDates(date: List<BaseIndexPinyinBean>): IIndexBarDataHelper

    /**
     * 对IndexBar的数据源进行排序(右侧栏),在 [sortSourceDates] 方法后调用
     */
    fun getSortedIndexDates(
        sourceDate: MutableList<out BaseIndexPinyinBean>,
        dates: MutableList<String>
    ): IIndexBarDataHelper
}