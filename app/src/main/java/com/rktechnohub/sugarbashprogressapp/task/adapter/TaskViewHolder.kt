package com.rktechnohub.sugarbashprogressapp.task.adapter

import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.rktechnohub.sugarbashprogressapp.R

/**
 * Created by Aiman on 18, May, 2024
 */
class TaskViewHolder(view: View): RecyclerView.ViewHolder(view) {
    var tvName: AppCompatTextView
    var tvIcon: AppCompatTextView
    var tvSubTasks: AppCompatTextView
    var tvProgress: AppCompatTextView
    var progressBar: ProgressBar
    var ivDelete: AppCompatImageView
    var ivIcon: AppCompatImageView
    init {
        tvName = view.findViewById(R.id.tv_name)
        tvIcon = view.findViewById(R.id.tv_icon)
        tvSubTasks = view.findViewById(R.id.tv_subtasks)
        tvProgress = view.findViewById(R.id.tv_progress)
        progressBar = view.findViewById(R.id.progress_bar)
        ivDelete = view.findViewById(R.id.iv_delete)
        ivIcon = view.findViewById(R.id.iv_icon)
    }
}