package com.viva.indexlib.indexBar

import android.util.Log
import com.github.promeg.pinyinhelper.Pinyin
import com.viva.indexlib.suspension.IIndexBarDataHelper
import java.util.Collections.sort
import java.util.Locale

/**
 * @author 李雄厚
 *
 * 处理数据
 */
class IndexBarDataHelperImpl : IIndexBarDataHelper {

    /**
     * 如果需要，
     * 字符 -> 拼音，
     *
     * @param data
     */
    override fun convert(data: List<BaseIndexPinyinBean>): IIndexBarDataHelper {
        if (data.isEmpty()) {
            return this
        }
        val size = data.size
        for (i in 0 until size) {
            val indexPinyinBean = data[i]
//            val pySb = StringBuilder()
            //add by zhangxutong 2016 11 10 如果不是top 才转拼音，否则不用转了
            if (indexPinyinBean.isNeedToPinyin()) {
                val target = indexPinyinBean.getTarget()//取出需要被拼音化的字段
//                //遍历target的每个char得到它的全拼音
//                for (i1 in target.indices) {
//                    //利用TinyPinyin将char转成拼音
//                    //查看源码，方法内 如果char为汉字，则返回大写拼音
//                    //如果c不是汉字，则返回String.valueOf(c)
//                    pySb.append(Pinyin.toPinyin(target[i1]).uppercase(Locale.ROOT))
//                }
                //转换成大写字母，如果存在小写字母也进行转换，不然小写字母会被归类到#里面
                indexPinyinBean.setBaseIndexPinyin(Pinyin.toPinyin(target.uppercase(Locale.ROOT), ""))//设置城市名全拼音
            }
        }
        return this
    }

    /**
     * 如果需要取出，
     * 则取出首字母 -> tag,或者特殊字母 "#".
     * 否则，用户已经实现设置好
     *
     * @param data
     */
    override fun fillIndexTag(data: List<BaseIndexPinyinBean>): IIndexBarDataHelper {
        if (data.isEmpty()) {
            return this
        }
        val size = data.size
        for (i in 0 until size) {
            val indexPinyinBean = data[i]
            if (indexPinyinBean.isNeedToPinyin()) {
                //以下代码设置城市拼音首字母
                val tagString = indexPinyinBean.baseIndexPinyin[0].toString()
                if (tagString.matches("[A-Z]".toRegex())) {//如果是A-Z字母开头
                    indexPinyinBean.setBaseIndexTag(tagString)
                } else {//特殊字母这里统一用#处理
                    indexPinyinBean.setBaseIndexTag("#")
                }
            }
        }
        return this
    }

    /**
     * 对源数据进行排序
     */
    override fun sortSourceDates(date: List<BaseIndexPinyinBean>): IIndexBarDataHelper {
        if (date.isEmpty()) {
            return this
        }
        convert(date)
        fillIndexTag(date)
        //对数据源进行排序
        sort(
            date
        ) { lhs, rhs ->
            if (!lhs.isNeedToPinyin()) {
                0
            } else if (!rhs.isNeedToPinyin()) {
                0
            } else if (lhs.baseIndexTag == "#" && rhs.baseIndexTag != "#") {
                1
            } else if (lhs.baseIndexTag != "#" && rhs.baseIndexTag == "#") {
                -1
            } else {
                lhs.baseIndexPinyin.compareTo(rhs.baseIndexPinyin)
            }
        }
        return this
    }

    override fun getSortedIndexDates(
        sourceDate: MutableList<out BaseIndexPinyinBean>,
        dates: MutableList<String>
    ): IIndexBarDataHelper {
        if (sourceDate.isEmpty()) {
            return this
        }
        dates.clear()
        //按数据源来 此时sourceData已经有序
        val size = sourceDate.size
        var baseIndexTag: String
        for (i in 0 until size) {
            baseIndexTag = sourceDate[i].baseIndexTag
            if (!dates.contains(baseIndexTag)) {//则判断是否已经将这个索引添加进去，若没有则添加
                dates.add(baseIndexTag)
            }
        }
        return this
    }
}