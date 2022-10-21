package com.viva.chek

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnWeChat).setOnClickListener {
            WeChatBarActivity.start(this)
        }
        findViewById<Button>(R.id.btnMain).setOnClickListener {
            MainBarActivity.start(this)
        }
    }
}