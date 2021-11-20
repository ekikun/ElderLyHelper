package com.eki.textocr.Bean

data class TextBean(
    val words_result:List<Sentence>,
    val words_result_num:Int,
    val direction: Int,
    val log_id:Long
)