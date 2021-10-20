package com.eki.common.utils

import android.content.Context
import android.widget.Toast

object ToastUtils{
    private var time: Long = 0
    private var oldMsg: String? = null

    fun show(msg: String) {
        if (msg != oldMsg) {
            create(msg)
            time = System.currentTimeMillis()
        } else {
            if (System.currentTimeMillis() - time > 2000) {
                create(msg)
                time = System.currentTimeMillis()
            }
        }
        oldMsg = msg
    }

    private fun create(massage: String) {
        val context: Context? = AppHelper.mContext
        val toast = Toast.makeText(context,massage, Toast.LENGTH_SHORT)
        //设置显示时间
        toast.show()
    }
}