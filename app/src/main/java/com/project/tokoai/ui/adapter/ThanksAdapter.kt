package com.project.tokoai.ui.adapter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.project.tokoai.R
import android.content.Intent
import android.os.Handler
import com.project.tokoai.ui.history.HistoryActivity

class ThanksAdapter : AppCompatActivity() {
   private val splashDelay: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.thanks_cart)
      Handler().postDelayed({
           finish()
        }, splashDelay)
    }
}