package com.coreix.coreix0404

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

/**
 * Created by user on 4/4/2018.
 */
class ListOnlineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    private lateinit var mItemClickListener : ItemClickListener

    override fun onClick(v: View?) {

    }


    var txtEmail : TextView = itemView.findViewById(R.id.txt_email)


}