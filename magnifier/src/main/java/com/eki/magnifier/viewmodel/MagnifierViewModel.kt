package com.eki.magnifier.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.eki.common.utils.JsonParser
import com.iflytek.cloud.RecognizerResult
class MagnifierViewModel:ViewModel(){


    val orderLiveData:MutableLiveData<String> =  MutableLiveData()

    private val TAG = "MAGNIFIERVIEWMODEL"

    fun encodeOrder(result: RecognizerResult?):String{
        val text = JsonParser.parseIatResult(result?.resultString)
        var order = ""
        if(text.contains("大")){
            order = "大"
        }else if(text.contains("小")){
            order = "小"
        }else if(text.contains("回")||text.contains("退")){
            order = "返回"
        }else order = "不匹配"
        Log.d(TAG, order)
        return order
    }
}