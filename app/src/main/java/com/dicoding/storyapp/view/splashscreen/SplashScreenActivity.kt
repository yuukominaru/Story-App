package com.dicoding.storyapp.view.splashscreen

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.storyapp.R
import com.dicoding.storyapp.view.main.MainActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        supportActionBar!!.hide()

        val splashScreen: LinearLayout = findViewById(R.id.splash_layout)
        splashScreen.alpha = 0f
        splashScreen.animate().setDuration(1500).alpha(1f).withEndAction {
            val openMainPage = Intent(this, MainActivity::class.java)
            startActivity(openMainPage)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}