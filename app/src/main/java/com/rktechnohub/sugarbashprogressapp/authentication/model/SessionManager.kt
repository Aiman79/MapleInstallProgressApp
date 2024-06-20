package com.rktechnohub.sugarbashprogressapp.authentication.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rktechnohub.sugarbashprogressapp.R
import com.rktechnohub.sugarbashprogressapp.project.model.Project
import java.lang.reflect.Type


class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences
    private val KEY_IS_LOGGED_IN = "is_logged_in"
    private val KEY_GID = "gid"
    private val KEY_NAME = "name"
    private val KEY_EMAIL = "email"
    private val KEY_ROLE = "role"
    private val KEY_ORDER_LIST_PROJECT = "order_list_project"
    private val KEY_ORDER_LIST_TASK = "order_list_task"


    init {
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), 0)
    }

    fun setIsLoggedIn(isLoggedIn: Boolean){
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun getIsLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)!!
    }

    fun setUId(id: String?){
        sharedPreferences.edit().putString(KEY_GID, id).apply()
    }

    fun getUId(): String {
        return sharedPreferences.getString(KEY_GID, "")!!
    }

    fun setName(name: String?){
        sharedPreferences.edit().putString(KEY_NAME, name).apply()
    }

    fun getName(): String {
        return sharedPreferences.getString(KEY_NAME, "")!!
    }

    fun setEmail(email: String?){
        sharedPreferences.edit().putString(KEY_EMAIL, email).apply()
    }

    fun getEmail(): String {
        return sharedPreferences.getString(KEY_EMAIL, "")!!
    }

    fun setRole(role: String?){
        sharedPreferences.edit().putString(KEY_ROLE, role).apply()
    }

    fun getRole(): String {
        return sharedPreferences.getString(KEY_ROLE, "")!!
    }

    fun setSession(user: User){
        setName(user.name)
        setEmail(user.email)
        setUId(user.uid)
        setRole(user.role)
    }

    fun getUser(): User {

        return User(
            getUId(),
            getName(),
            getEmail(),
            getRole()
        )
    }

    fun setOrderList(orderProjectList: List<OrderClass>){
        var gson: Gson = Gson()
        var json: String = gson.toJson(orderProjectList)

        sharedPreferences.edit().putString(KEY_ORDER_LIST_PROJECT, json).apply()
    }

    fun getOrderList(): List<OrderClass>{
       try{
           val json = sharedPreferences.getString(KEY_ORDER_LIST_PROJECT, "")!!

           var gson: Gson = Gson()
           val type: Type = object : TypeToken<List<OrderClass?>?>() {}.type
           val dataList: List<OrderClass> = gson.fromJson(json, type)

           Log.e("Order list", dataList.toString())
           return dataList
       } catch (e: Exception){
           return emptyList()
       }
    }

    fun setOrderListTask(orderTaskList: List<OrderClass>){
        var gson: Gson = Gson()
        var json: String = gson.toJson(orderTaskList)

        sharedPreferences.edit().putString(KEY_ORDER_LIST_TASK, json).apply()
    }

    fun getOrderListTask(): List<OrderClass>{
       try{
           val json = sharedPreferences.getString(KEY_ORDER_LIST_TASK, "")!!

           var gson: Gson = Gson()
           val type: Type = object : TypeToken<List<OrderClass?>?>() {}.type
           val dataList: List<OrderClass> = gson.fromJson(json, type)

           Log.e("Order list", dataList.toString())
           return dataList
       } catch (e: Exception){
           return emptyList()
       }
    }


    fun logout(){
        sharedPreferences.edit().clear().apply()
    }
}