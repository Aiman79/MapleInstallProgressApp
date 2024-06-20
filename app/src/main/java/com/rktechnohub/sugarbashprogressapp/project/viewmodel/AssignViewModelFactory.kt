package com.rktechnohub.sugarbashprogressapp.project.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Created by Aiman on 22, May, 2024
 */
class AssignViewModelFactory (private val context: Context, private val role: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AssignViewModel(context, role) as T
    }
}