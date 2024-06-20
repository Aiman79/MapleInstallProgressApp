package com.rktechnohub.sugarbashprogressapp.task.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rktechnohub.sugarbashprogressapp.authentication.model.SessionManager
import com.rktechnohub.sugarbashprogressapp.firebasedb.FirebaseDatabaseOperations
import com.rktechnohub.sugarbashprogressapp.project.model.Project
import com.rktechnohub.sugarbashprogressapp.task.model.TaskModel
import com.rktechnohub.sugarbashprogressapp.utils.AppUtils

/**
 * Created by Aiman on 18, May, 2024
 */
class TaskViewModel(context: Context, projectId: String, flag: String) : ViewModel() {
    private val _items = MutableLiveData<MutableList<TaskModel>>()
    val items: LiveData<MutableList<TaskModel>>
        get() = _items

    //    private val disposables = CompositeDisposable()
    suspend fun getDataPr(context: Context, projectId: String, flag: String) {
        val fbOp = FirebaseDatabaseOperations()
        val session = SessionManager(context)
        _items.value = when(flag){
            "p" -> fbOp.getTaskByProjectIdCoroutine(projectId, true,  session.getOrderListTask()).toMutableList()
            "u" -> fbOp.getTaskByUserId(session.getUId(), session.getOrderListTask()).toMutableList()
            "t" -> fbOp.getTaskByMainTaskId(projectId, session.getOrderListTask()).toMutableList()
            else -> {
                fbOp.getTaskByUserIdDate(session.getUId(), session.getOrderListTask()).toMutableList()
            }
        }
    }

    /*fun getData(context: Context, projectId: String, flag: String) {
        val fbOp = FirebaseDatabaseOperations()
        fbOp.addOnDataChangedListener(object : FirebaseDatabaseOperations.DataChangedListener {
            override fun dataRecieved() {
                clear()
                when (flag) {
                    "t" -> {
                        _items.value = fbOp.subtaskList
                    }

                    else -> {
                        _items.value = fbOp.taskList
                    }
                }
            }

            override fun canceled() {
                Toast.makeText(context, "Please try again", Toast.LENGTH_SHORT).show()
            }

        })

        val session = SessionManager(context)
        when (flag) {
            "p" -> {
                fbOp.getTaskByProjectId(projectId)
            }

            "u" -> {
                fbOp.getTaskByUserId(session.getUId())
            }

            "t" -> {
                fbOp.getTaskByMainTaskId(projectId)
            }

            "today" -> {
                fbOp.getTaskByUserIdDate(session.getUId())
            }
        }
    }*/

    fun clear() {
        _items.value = mutableListOf()
    }

}