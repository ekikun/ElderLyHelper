package com.eki.elderlyhelper

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import androidx.core.content.ContextCompat
import com.alibaba.android.arouter.launcher.ARouter
import com.eki.common.utils.AppHelper

class MainActivity : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button:Button = findViewById(R.id.button)
        button.setOnClickListener{
              ARouter.getInstance().build("/magnifier/MagnifierActivity")
                .navigation()

        }
    }


}


