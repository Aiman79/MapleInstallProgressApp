package com.rktechnohub.sugarbashprogressapp.project.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

/**
 * Created by Aiman on 18 May 2024
 */
class Project : Parcelable{
    var id: String
    var name: String
    var icon: String
    var startDate: String
    var endDate: String
    var daysLeft: String
    var target: String
    var description: String
    var taskId: String
    var adminId: String
    var employeeId: String
    var clientId: String
    var progress: String
    var mapLink: String
    var order: String

    init {
        this.id = ""
        this.name = ""
        this.icon = ""
        this.startDate = ""
        this.endDate = ""
        this.daysLeft = ""
        this.target = ""
        this.description = ""
        this.taskId = ""
        this.adminId = ""
        this.employeeId = ""
        this.clientId = ""
        this.progress = ""
        this.mapLink = ""
        this.order = ""
    }

    constructor(){
        this.id = ""
        this.name = ""
        this.icon = ""
        this.startDate = ""
        this.endDate = ""
        this.daysLeft = ""
        this.target = ""
        this.description = ""
        this.taskId = ""
        this.adminId = ""
        this.employeeId = ""
        this.mapLink = ""
        this.order = ""
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
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    constructor(  id: String,
                  name: String,
                  icon: String,
                  startDate: String,
                  endDate: String,
                  daysLeft: String,
                  target: String,
                  description: String,
                  taskId: String,
                  adminId: String,
                  employeeId: String,
                  clientId: String,
                  progress: String,
                  mapLink: String,
                  order: String,
        ){
        this.id = id
        this.name = name
        this.icon = icon
        this.startDate = startDate
        this.endDate = endDate
        this.daysLeft = daysLeft
        this.target = target
        this.description = description
        this.taskId = taskId
        this.adminId = adminId
        this.employeeId = employeeId
        this.clientId = clientId
        this.progress = progress
        this.mapLink = mapLink
        this.order = order
    }

  /*  companion object {
        fun fromMap(map: Map<String, Any>): Project {
            return Project(
                map["id"] as String,
                map["name"] as String,
                map["icon"] as String,
                map["startDate"] as String,
                map["endDate"] as String,
                map["target"] as String,
                map["description"] as String,
                map["taskId"] as String,
                map["adminId"] as String,
                map["employeeId"] as String,
                map["clientId"] as String,
                map["progress"] as String,
            )
        }
    }*/

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, p1: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(icon)
        parcel.writeString(startDate)
        parcel.writeString(endDate)
        parcel.writeString(daysLeft)
        parcel.writeString(target)
        parcel.writeString(description)
        parcel.writeString(taskId)
        parcel.writeString(adminId)
        parcel.writeString(employeeId)
        parcel.writeString(clientId)
        parcel.writeString(progress)
        parcel.writeString(mapLink)
        parcel.writeString(order)
    }

    companion object CREATOR : Parcelable.Creator<Project> {
        override fun createFromParcel(parcel: Parcel): Project {
            return Project(parcel)
        }

        override fun newArray(size: Int): Array<Project?> {
            return arrayOfNulls(size)
        }
    }
}