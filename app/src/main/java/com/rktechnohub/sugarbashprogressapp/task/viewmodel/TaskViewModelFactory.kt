package com.rktechnohub.sugarbashprogressapp.task.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Created by Aiman on 22, May, 2024
 */
class TaskViewModelFactory (private val context: Context, private val projectId: String
                            , private val flag: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TaskViewModel(context, projectId, flag) as T
    }
}