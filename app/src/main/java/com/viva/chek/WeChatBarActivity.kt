package com.viva.chek

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.viva.indexlib.CustomLinearLayoutManager
import com.viva.indexlib.SuspensionDecoration
import com.viva.indexlib.WeChatIndexBar

/**
 * @author 李雄厚
 *
 *
 */
class WeChatBarActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, WeChatBarActivity::class.java))
        }
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var indexBar: WeChatIndexBar
    private val manager by lazy { CustomLinearLayoutManager(this) }
    private val adapter by lazy { Adapter(this) }
    private lateinit var decoration: SuspensionDecoration
    private var dataSource = mutableListOf<DataEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_we_chat_bar)
        title = "仿微信"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.recyclerView)
        indexBar = findViewById(R.id.contactIndexBar)

        recyclerView.layoutManager = manager
        recyclerView.adapter = adapter

        dataSource.add(
            DataEntity("我是头部，不参加悬停1")
                .isTop(false)
                .isSuspension(false)
                .setBaseIndexTag(
                    "↑"
                ) as DataEntity
        )
        dataSource.add(
            DataEntity("我是头部，不参加悬停2")
                .isTop(false)
                .isSuspension(false)
                .setBaseIndexTag(
                    "↑"
                ) as DataEntity
        )
        dataSource.add(
            DataEntity("我是头部，不参加悬停3")
                .isTop(false)
                .isSuspension(false)
                .setBaseIndexTag(
                    "↑"
                ) as DataEntity
        )

        dataSource.addAll(surname.map {
            val data = DataEntity(it)
            data
        })

        decoration = SuspensionDecoration(this, dataSource)
            .setBottomLine(0F)
            .setTitleFont(40F)
            .setTitleHeight(this, 30)
            .setTitleFontSize(this, 12)
            .setBottomLineColor(Color.parseColor("#EDEDED"))
            .setTitleBgColor(Color.parseColor("#EDEDED"))
            .setTitleFontColor(Color.parseColor("#666666"))
            .isTitleFontBold(true)
        recyclerView.addItemDecoration(decoration)
        indexBar.setNeedRealIndex(false)
            .setLayoutManager(manager)


        indexBar.setSourceDates(dataSource).invalidate()
        decoration.setDataList(dataSource)
        adapter.setData(dataSource)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}