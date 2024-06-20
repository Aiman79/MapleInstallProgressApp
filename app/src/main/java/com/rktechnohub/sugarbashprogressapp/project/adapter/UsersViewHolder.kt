package com.rktechnohub.sugarbashprogressapp.project.adapter

import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.rktechnohub.sugarbashprogressapp.R

/**
 * Created by Aiman on 18, May, 2024
 */
class UsersViewHolder(view: View): RecyclerView.ViewHolder(view) {
    var tvName: AppCompatTextView
    var tvEmail: AppCompatTextView
    var ivAssign: AppCompatImageView
    init {
        tvName = view.findViewById(R.id.tv_name)
        tvEmail = view.findViewById(R.id.tv_email)
        ivAssign = view.findViewById(R.id.iv_assign)
    }
}