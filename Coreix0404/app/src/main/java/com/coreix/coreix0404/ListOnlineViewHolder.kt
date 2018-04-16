package com.coreix.coreix0404

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

/**
 * Created by user on 4/4/2018.
 */
class ListOnlineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    var txtEmail: TextView

    internal lateinit var itemClickListener: ItemClickListener

    init {
        txtEmail = itemView.findViewById<View>(R.id.txt_email) as TextView
        itemView.setOnClickListener(this)
    }

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun onClick(view: View) {
        itemClickListener.onClick(view, adapterPosition)
    }
}