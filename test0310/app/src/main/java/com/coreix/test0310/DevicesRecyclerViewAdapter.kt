package com.coreix.test0310

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Created by user on 3/10/2018.
 */

class DevicesRecyclerViewAdapter(val mDeviceList: List<DeviceData>, val context: Context): RecyclerView.Adapter<DevicesRecyclerViewAdapter.VH>(){


    inner class VH(itemView: View?) : RecyclerView.ViewHolder
}