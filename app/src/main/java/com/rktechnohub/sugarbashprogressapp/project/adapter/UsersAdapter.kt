package com.rktechnohub.sugarbashprogressapp.project.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rktechnohub.sugarbashprogressapp.R
import com.rktechnohub.sugarbashprogressapp.authentication.model.User
import com.rktechnohub.sugarbashprogressapp.project.model.Project

/**
 * Created by Aiman on 18, May, 2024
 */
class UsersAdapter(private var mList :List<User>) : RecyclerView.Adapter<UsersViewHolder>(){
//    private var mList = mutableListOf<ProjectModel>()

    fun setData(list: List<User>) {
        mList = list
        Log.d("ProjectAdapter", "setData called with list of size ${list.size}")

        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.raw_users, parent, false)
        return UsersViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        holder.tvName.text = mList[position].name
        holder.tvEmail.text = mList[position].email

        holder.ivAssign.setOnClickListener{
            onItemClickedListener?.onItemClicked(mList[position])
        }
    }

    private var onItemClickedListener: OnItemClickedListener? = null

    public fun setOnItemClickedListener(onItemClickedListener: OnItemClickedListener){
        this.onItemClickedListener = onItemClickedListener
    }

    interface OnItemClickedListener{
        fun onItemClicked(user: User)
    }
}