package com.eki.elderlyhelper

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.text.format.Time
import androidx.appcompat.app.AppCompatActivity

class SplashActivity:AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed(Runnable()
            {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent);
                finish()
        }, 3000)
    }

}