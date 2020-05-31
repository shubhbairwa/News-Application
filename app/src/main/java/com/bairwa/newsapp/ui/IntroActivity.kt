package com.bairwa.newsapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bairwa.newsapp.R

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_intro)
supportActionBar?.hide()

//    Thread(Runnable {
//        Thread.sleep(3000)
//        Intent(applicationContext,NewsActivity::class.java).also {
//            startActivity(it)
//        }
//    })

        //getSupportActionBar().hide();
//
        val splash: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(3000)
                    val intent = Intent(this@IntroActivity, NewsActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                }
            }
        }
        splash.start()

    }
}