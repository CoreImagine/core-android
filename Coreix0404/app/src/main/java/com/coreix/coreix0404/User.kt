package com.coreix.coreix0404

/**
 * Created by user on 4/4/2018.
 */
class User {
    private var email : String
    private var status : String

    constructor(){
        this.email = ""
        this.status = ""
    }
    constructor(email : String, status : String){
        this.email = email
        this.status = status
    }

    fun getEmail():String{
        return this.email
    }

    fun getStatus():String{
        return this.status
    }
}
