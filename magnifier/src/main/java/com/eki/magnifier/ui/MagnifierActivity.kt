package com.eki.magnifier.ui

import android.os.Bundle
import com.eki.common.base.BaseActivity
import com.eki.magnifier.R
import com.eki.magnifier.databinding.ActivityMagnifierBinding
import com.iflytek.cloud.RecognizerResult

class MagnifierActivity : BaseActivity<ActivityMagnifierBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_magnifier)
    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun execute(results: RecognizerResult?) {

    }

    override fun getLayoutId() = R.layout.activity_magnifier
}