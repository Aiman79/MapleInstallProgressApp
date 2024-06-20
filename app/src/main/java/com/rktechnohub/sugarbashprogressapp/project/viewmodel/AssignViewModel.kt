package com.rktechnohub.sugarbashprogressapp.project.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rktechnohub.sugarbashprogressapp.authentication.model.SessionManager
import com.rktechnohub.sugarbashprogressapp.authentication.model.User
import com.rktechnohub.sugarbashprogressapp.firebasedb.FirebaseDatabaseOperations
import com.rktechnohub.sugarbashprogressapp.project.model.Project
import com.rktechnohub.sugarbashprogressapp.utils.AppUtils

/**
 * Created by Aiman on 18, May, 2024
 */
class AssignViewModel(context: Context, role: String) : ViewModel() {
    private val _items = MutableLiveData<List<User>>()
    val items: LiveData<List<User>>
        get() = _items

//    private val disposables = CompositeDisposable()


    init {
        /*val fbOp = FirebaseDatabaseOperations()
        fbOp.addOnDataChangedListener(object : FirebaseDatabaseOperations.DataChangedListener{
            override fun dataRecieved() {
                clear()
                _items.value = fbOp.userList
            }

            override fun canceled() {
                Toast.makeText(context, "Please try again", Toast.LENGTH_SHORT).show()
            }

        })

        fbOp.getAllUsersByRole(role, )*/

    }

    fun getData(role: String, context: Context, isAdmin: Boolean, project: Project){
        val fbOp = FirebaseDatabaseOperations()
        fbOp.addOnDataChangedListener(object : FirebaseDatabaseOperations.DataChangedListener{
            override fun dataRecieved() {
                clear()
                _items.value = fbOp.userList
            }

            override fun canceled() {
                Toast.makeText(context, "Please try again", Toast.LENGTH_SHORT).show()
            }

        })

        fbOp.getAllUsersByRole(role, isAdmin, project)
    }

    fun clear() {
        _items.value = emptyList()
    }

}