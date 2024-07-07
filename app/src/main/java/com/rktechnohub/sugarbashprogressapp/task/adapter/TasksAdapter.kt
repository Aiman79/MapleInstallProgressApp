package com.rktechnohub.sugarbashprogressapp.task.adapter

import android.content.Context
import android.icu.text.Transliterator.Position
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.rktechnohub.sugarbashprogressapp.R
import com.rktechnohub.sugarbashprogressapp.project.model.Project
import com.rktechnohub.sugarbashprogressapp.task.model.TaskModel
import com.rktechnohub.sugarbashprogressapp.utils.AppUtils
import com.rktechnohub.sugarbashprogressapp.utils.ColorCustomizeClass.updateProgress

/**
 * Created by Aiman on 18, May, 2024
 */
class TasksAdapter(private var mList :MutableList<TaskModel>) : RecyclerView.Adapter<TaskViewHolder>(){
//    private var mList = mutableListOf<ProjectModel>()
    private var requestManager: RequestManager? = null
    var context: Context? = null

    fun setData(list: MutableList<TaskModel>, requestManager: RequestManager, context: Context) {
        mList = list
        this.requestManager = requestManager
        Log.d("ProjectAdapter", "setData called with list of size ${list.size}")
        this.context = context

        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.raw_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.tvName.text = mList[position].name
        holder.tvProgress.text = "${mList[position].progress}%"
//        holder.progressBar.progress = mList[position].progress.toInt()
        holder.progressBar.updateProgress(mList[position].progress.toInt(), context!!)

        if (mList[position].taskId.isNullOrEmpty()){
            holder.tvSubTasks.text = "0 of 0 subtasks"
        } else {
            val ids = mList[position].taskId.split(",")

            val comp = if(mList[position].completedSubTasks.isNullOrEmpty()){
                "0"
            } else {
                mList[position].completedSubTasks
            }
            holder.tvSubTasks.text = "$comp of ${ids.size} subtasks"
        }

        val isEmoji = AppUtils.checkIfEmoji(mList[position].icon)
        if (isEmoji) {
            holder.tvIcon.visibility = View.VISIBLE
            holder.ivIcon.visibility = View.GONE
            holder.tvIcon.text = mList[position].icon
        } else {
            holder.tvIcon.visibility = View.GONE
            holder.ivIcon.visibility = View.VISIBLE
            requestManager?.load(mList[position].icon)
                ?.skipMemoryCache(true)
                ?.into(holder.ivIcon)
        }

        holder.ivDelete.setOnClickListener {
            onItemClickedListener?.onItemDelete(mList[position], position)
        }

        holder.itemView.setOnClickListener{
            onItemClickedListener?.onItemClicked(mList[position])
        }
    }

    fun removeItem(position: Int){
        mList.removeAt(position)
        notifyDataSetChanged()
    }

    private var onItemClickedListener: OnItemClickedListener? = null

    public fun setOnItemClickedListener(onItemClickedListener: OnItemClickedListener){
        this.onItemClickedListener = onItemClickedListener
    }

    interface OnItemClickedListener{
        fun onItemClicked(task: TaskModel)
        fun onItemDelete(task: TaskModel, pos: Int)
        fun onDragged(list: MutableList<TaskModel>)
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val movedItem = mList.removeAt(fromPosition)
        mList.add(toPosition, movedItem)
        notifyItemMoved(fromPosition, toPosition)
        onItemClickedListener?.onDragged(mList)
    }
}