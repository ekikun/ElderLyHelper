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
import com.eki.common.utils.Constant

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setStatusBarColor(getColor(com.eki.common.R.color.white))
        val btn_magnifier:Button = findViewById(R.id.btn_magnifier)
        btn_magnifier.setOnClickListener{
              ARouter.getInstance().build(Constant.ROUTER_MAGNIFIER)
                .navigation()
        }
        val btn_schedule:Button = findViewById(R.id.btn_schedule)
        btn_schedule.setOnClickListener {
            ARouter.getInstance().build(Constant.ROUTER_SCHEDULE)
                .navigation()
        }
    }


}


