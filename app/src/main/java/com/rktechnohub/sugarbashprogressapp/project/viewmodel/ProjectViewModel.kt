package com.rktechnohub.sugarbashprogressapp.project.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rktechnohub.sugarbashprogressapp.authentication.model.SessionManager
import com.rktechnohub.sugarbashprogressapp.firebasedb.FirebaseDatabaseOperations
import com.rktechnohub.sugarbashprogressapp.project.model.Project
import com.rktechnohub.sugarbashprogressapp.utils.AppUtils

/**
 * Created by Aiman on 18, May, 2024
 */
class ProjectViewModel(context: Context) : ViewModel() {
    private val _items = MutableLiveData<List<Project>>()
    val items: LiveData<List<Project>>
        get() = _items

//    private val disposables = CompositeDisposable()


    /*init {
        val fbOp = FirebaseDatabaseOperations()
        fbOp.addOnDataChangedListener(object : FirebaseDatabaseOperations.DataChangedListener{
            override fun dataRecieved() {
                clear()
                _items.value = fbOp.projectList
            }

            override fun canceled() {
                Toast.makeText(context, "Please try again", Toast.LENGTH_SHORT).show()
            }

        })

        val session = SessionManager(context)
        when(session.getRole()){
            AppUtils.roleSuperAdmin.toString() -> {
                fbOp.getAllProjectsForSuperAdmin()
            }
            AppUtils.roleAdmin.toString() -> {
                fbOp.getAllProjectsForAdmin(session.getUId())
            }
            AppUtils.roleEmployee.toString() -> {
                fbOp.getAllProjectsForEmployee(session.getUId())
            }
            AppUtils.roleClient.toString() -> {
                fbOp.getAllProjectsForClient(session.getUId())
            }
        }
    }*/

   /* fun getData(context: Context){
        val fbOp = FirebaseDatabaseOperations()
        fbOp.addOnDataChangedListener(object : FirebaseDatabaseOperations.DataChangedListener{
            override fun dataRecieved() {
                clear()
                _items.value = fbOp.projectList
            }

            override fun canceled() {
                Toast.makeText(context, "Please try again", Toast.LENGTH_SHORT).show()
            }

        })

        val session = SessionManager(context)
        when(session.getRole()){
            AppUtils.roleSuperAdmin.toString() -> {
                fbOp.getAllProjectsForSuperAdmin()
            }
            AppUtils.roleAdmin.toString() -> {
                fbOp.getAllProjectsForAdmin(session.getUId())
            }
            AppUtils.roleEmployee.toString() -> {
                fbOp.getAllProjectsForEmployee(session.getUId())
            }
            AppUtils.roleClient.toString() -> {
                fbOp.getAllProjectsForClient(session.getUId())
            }
            //TODO for all
        }
    }*/

    fun updateData(list: List<Project>){
        clear()
        _items.value = list
    }

    fun clear() {
        _items.value = emptyList()
    }

}