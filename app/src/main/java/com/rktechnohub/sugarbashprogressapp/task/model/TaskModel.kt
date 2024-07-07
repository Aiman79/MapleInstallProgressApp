package com.rktechnohub.sugarbashprogressapp.task.model

import android.os.Parcel
import android.os.Parcelable
import com.rktechnohub.sugarbashprogressapp.project.model.Project

/**
 * Created by Aiman on 23, May, 2024
 */
class TaskModel : Parcelable{
    var id: String
    var name: String
    var icon: String
    var description: String
    var taskId: String
    var mainTaskId: String
    var completedSubTasks: String
    var userId: String
    var projectId: String
    var progress: String
    var deadline_date: String
    var deadline_time: String
    var subTaskList: MutableList<TaskModel>

    init {
        this.id = ""
        this.name = ""
        this.icon = ""
        this.description = ""
        this.taskId = ""
        this.mainTaskId = ""
        this.completedSubTasks = ""
        this.userId = ""
        this.projectId = ""
        this.progress = ""
        this.deadline_date = ""
        this.deadline_time = ""
        this.subTaskList = mutableListOf()
    }

    constructor(){
        this.id = ""
        this.name = ""
        this.icon = ""
        this.description = ""
        this.taskId = ""
        this.mainTaskId = ""
        this.completedSubTasks = ""
        this.userId = ""
        this.projectId = ""
        this.progress = ""
        this.deadline_date = ""
        this.deadline_time = ""
    }

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    constructor(  id: String,
                  name: String,
                  icon: String,
                  description: String,
                  taskId: String,
                  mainTaskId: String,
                  completedSubTasks: String,
                  userId: String,
                  projectId: String,
                  progress: String,
                  deadline_date: String,
                  deadline_time: String,
    ){
        this.id = id
        this.name = name
        this.icon = icon
        this.description = description
        this.taskId = taskId
        this.mainTaskId = mainTaskId
        this.completedSubTasks = completedSubTasks
        this.userId = userId
        this.projectId = projectId
        this.progress = progress
        this.deadline_date = deadline_date
        this.deadline_time = deadline_time
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, p1: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(icon)
        parcel.writeString(description)
        parcel.writeString(taskId)
        parcel.writeString(mainTaskId)
        parcel.writeString(completedSubTasks)
        parcel.writeString(userId)
        parcel.writeString(projectId)
        parcel.writeString(progress)
        parcel.writeString(deadline_date)
        parcel.writeString(deadline_time)
    }

    companion object CREATOR : Parcelable.Creator<TaskModel> {
        override fun createFromParcel(parcel: Parcel): TaskModel {
            return TaskModel(parcel)
        }

        override fun newArray(size: Int): Array<TaskModel?> {
            return arrayOfNulls(size)
        }
    }
}