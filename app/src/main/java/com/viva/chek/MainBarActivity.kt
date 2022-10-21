package com.viva.chek

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.viva.indexlib.CustomLinearLayoutManager
import com.viva.indexlib.IndexBar
import com.viva.indexlib.SuspensionDecoration
import com.viva.indexlib.WeChatIndexBar

/**
 * @author 李雄厚
 *
 *
 */
class MainBarActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, MainBarActivity::class.java))
        }
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var indexBar: IndexBar
    private lateinit var sideBarHint: AppCompatTextView
    private val manager by lazy { CustomLinearLayoutManager(this) }
    private val adapter by lazy { Adapter(this) }
    private lateinit var decoration: SuspensionDecoration
    private var dataSource = mutableListOf<DataEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_bar)
        title = "常规"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.recyclerView)
        indexBar = findViewById(R.id.contactIndexBar)
        sideBarHint = findViewById(R.id.contactTvSideBarHint)

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
        recyclerView.addItemDecoration(decoration)
        indexBar.setPressedShowTextView(sideBarHint)
            .setNeedRealIndex(false)
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