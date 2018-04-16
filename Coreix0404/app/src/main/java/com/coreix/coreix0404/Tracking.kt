package com.coreix.coreix0404

class Tracking {
    private lateinit var email : String
    private lateinit var uid : String
    private lateinit var latitude : String
    private lateinit var longitude : String

    constructor(){
    }

    constructor(email: String,uid: String,lat: String,lng: String){
        this.email = email
        this.uid = uid
        this.latitude = lat
        this.longitude = lng
    }

    fun getEmail():String{
        return email
    }
    fun setEmail(email:String){
        this.email = email
    }
    fun getUid():String{
        return uid
    }
    fun getUid(uid:String){
        this.uid = uid
    }
    fun setLat(lat:String){
        this.latitude = lat
    }
    fun setLng(lng:String){
        this.longitude = lng
    }
    fun getLatitude():String{
        return this.latitude
    }
    fun getLongitude():String{
        return this.longitude
    }
}