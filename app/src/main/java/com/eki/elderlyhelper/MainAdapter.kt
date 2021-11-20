package com.eki.elderlyhelper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.imageview.ShapeableImageView

/**
 *  主界面GridLayout RecyclerView 的 Adapter
 */
class MainAdapter(context: Context):RecyclerView.Adapter<MainAdapter.MyViewHolder>(){

    var dataList = mutableListOf<MainData>()

    interface setonClickedLisentener{
        fun onItemClicked(function:String)
    }

    private val listener = context as setonClickedLisentener


    fun getData(dataList:List<MainData>) {
        this.dataList = dataList as MutableList<MainData>
    }

    inner class MyViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_main_layout,parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = dataList.get(position)
        val tv = holder.itemView.findViewById<TextView>(R.id.tv_func)
        val img = holder.itemView.findViewById<ShapeableImageView>(R.id.image_icon)
        tv.text = item.name
        img.load(item.drawable)
        holder.itemView.setOnClickListener {
            listener.onItemClicked(item.function)
        }
    }

    override fun getItemCount() = dataList.size
}