package com.rktechnohub.sugarbashprogressapp.authentication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.rktechnohub.sugarbashprogressapp.R


/**
 * Created by Aiman on 19, May, 2024
 */
class CustomSpinnerAdapter(val mContext: Context, val tvId: Int, val data: Array<String>):
    ArrayAdapter<String>(mContext, tvId, data) {

    fun getAdapterContext(): Context {
        return mContext
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    fun getCustomView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val row: View = inflater.inflate(R.layout.raw_spinner_color, parent, false)
        val label = row.findViewById<View>(R.id.tv_role) as TextView
        when(position){
            0-> label.setBackgroundColor(ContextCompat.getColor(getAdapterContext(), R.color.blue_light))
//            1-> label.setBackgroundColor(ContextCompat.getColor(getAdapterContext(), R.color.cyan))
            1-> label.setBackgroundColor(ContextCompat.getColor(getAdapterContext(), R.color.colorAccent))
            2-> label.setBackgroundColor(ContextCompat.getColor(getAdapterContext(), R.color.colorPrimary))
            3-> label.setBackgroundColor(ContextCompat.getColor(getAdapterContext(), R.color.colorSecondary))
        }
        label.setText(data[position])
        return row
    }
}