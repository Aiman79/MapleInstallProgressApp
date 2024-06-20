package com.rktechnohub.sugarbashprogressapp.authentication.model

import android.provider.ContactsContract.CommonDataKinds.Email

/**
 * Created by Aiman on 17, May, 2024
 */
class User{
    var uid: String
    var name: String
    var email: String
    var role: String

    init {
        this.name = ""
        this.uid = ""
        this. email = ""
        this.role = ""
    }

    constructor(){
        this.name = ""
        this.uid = ""
        this. email = ""
        this.role = ""
    }

    constructor(uid: String, name: String, email: String, role: String){
        this.name = name
        this.uid = uid
        this. email = email
        this.role = role
    }
}