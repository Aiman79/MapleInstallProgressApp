package com.rktechnohub.sugarbashprogressapp.project.adapter

import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.rktechnohub.sugarbashprogressapp.R

/**
 * Created by Aiman on 18, May, 2024
 */
class ProjectViewHolder(view: View): RecyclerView.ViewHolder(view) {
    var tvName: AppCompatTextView
    var tvIcon: AppCompatTextView
    var ivIcon: AppCompatImageView
    var ivMap: AppCompatImageView
    var ivDelete: AppCompatImageView
    var ivCopy: AppCompatImageView
    var tvDate: AppCompatTextView
    var tvProgress: AppCompatTextView
    var progressBar: ProgressBar
    var clMain: ConstraintLayout
    init {
        tvName = view.findViewById(R.id.tv_name)
        tvIcon = view.findViewById(R.id.tv_icon)
        ivIcon = view.findViewById(R.id.iv_icon)
        tvDate = view.findViewById(R.id.tv_date)
        ivMap = view.findViewById(R.id.iv_map)
        ivDelete = view.findViewById(R.id.iv_delete)
        ivCopy = view.findViewById(R.id.iv_copy)
        tvProgress = view.findViewById(R.id.tv_progress)
        progressBar = view.findViewById(R.id.progress_bar)
        clMain = view.findViewById(R.id.cl_main)
    }
}