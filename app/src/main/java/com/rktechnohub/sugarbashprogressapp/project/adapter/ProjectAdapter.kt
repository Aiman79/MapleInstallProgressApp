package com.rktechnohub.sugarbashprogressapp.project.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.rktechnohub.sugarbashprogressapp.R
import com.rktechnohub.sugarbashprogressapp.firebasedb.FirebaseDatabaseOperations
import com.rktechnohub.sugarbashprogressapp.project.fragment.ProjectListFragment
import com.rktechnohub.sugarbashprogressapp.project.model.Project
import com.rktechnohub.sugarbashprogressapp.task.model.TaskModel
import com.rktechnohub.sugarbashprogressapp.utils.AppUtils
import com.rktechnohub.sugarbashprogressapp.utils.ColorCustomizeClass.updateProgress

/**
 * Created by Aiman on 18, May, 2024
 */
class ProjectAdapter(private var mList: MutableList<Project>
) : RecyclerView.Adapter<ProjectViewHolder>() {
    //    private var mList = mutableListOf<ProjectModel>()
    private var requestManager: RequestManager? = null
    private var isDelete = false
    var context: Context? = null

    fun setData(list: List<Project>, requestManager: RequestManager, isDelete: Boolean, context: Context) {
        mList.addAll(list)
        this.requestManager = requestManager
        Log.d("ProjectAdapter", "setData called with list of size ${list.size}")
        this.isDelete = isDelete
        this.context = context

        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.raw_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.tvName.text = mList[position].name
        holder.tvDate.text = "${mList[position].startDate} - ${mList[position].endDate}"
        holder.tvProgress.text = "${mList[position].progress}%"
//        holder.progressBar.progress = mList[position].progress.toInt()
        holder.progressBar.updateProgress(mList[position].progress.toInt(), context!!)

        if (isDelete){
            holder.ivDelete.visibility = View.VISIBLE
        } else {
            holder.ivDelete.visibility = View.GONE
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

        holder.ivIcon.setOnClickListener {
            onItemClickedListener?.onIconClicked(mList[position], position)
        }

        holder.tvIcon.setOnClickListener {
            onItemClickedListener?.onIconClicked(mList[position], position)
        }

        holder.ivMap.setOnClickListener {
            onItemClickedListener?.onMapClicked(mList[position])
        }

        holder.clMain.setOnClickListener {
            onItemClickedListener?.onItemClicked(mList[position])
        }

        holder.ivDelete.setOnClickListener {
            onItemClickedListener?.onItemDelete(mList[position], position)
        }

        holder.ivCopy.setOnClickListener {
            onItemClickedListener?.onItemCopy(mList[position], position)
        }


    }

    private var onItemClickedListener: OnItemClickedListener? = null

    public fun setOnItemClickedListener(onItemClickedListener: OnItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener
    }

    fun removeItem(position: Int){
        mList.removeAt(position)
        notifyDataSetChanged()
    }

    interface OnItemClickedListener {
        fun onItemClicked(project: Project)
        fun onMapClicked(project: Project)
        fun onItemDelete(project: Project, pos: Int)
        fun onItemCopy(project: Project, pos: Int)
        fun onIconClicked(project: Project, pos: Int)
        fun onDragged(list: List<Project>)
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
       /* val list: MutableList<Project> = mutableListOf()
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                mList[i].order = (i + 1).toString()
                mList[i + 1].order = i.toString()
                list.add(mList[i])
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                mList[i].order = (i - 1).toString()
                mList[i - 1].order = i.toString()
                list.add(mList[i])
            }
        }*/

        val movedItem = mList.removeAt(fromPosition)
        mList.add(toPosition, movedItem)
        notifyItemMoved(fromPosition, toPosition)
        onItemClickedListener?.onDragged(mList)
    }
}