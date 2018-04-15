package com.coreix.coreix0404

class Tracking {
    private var email : String
    private var uid : String
    private var lat : String
    private var lng : String

    constructor(){
        this.email = "user@gmail.com"
        this.uid = "0550193"
        this.lat = "23.535"
        this.lng = "124.649"
    }

    constructor(email: String,uid: String,lat: String,lng: String){
        this.email = email
        this.uid = uid
        this.lat = lat
        this.lng = lng
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
    fun setLocation(lat:String, lng:String){
        this.lat = lat
        this.lng = lng
    }
    fun getLatitude():String{
        return this.lat
    }
    fun getLongitude():String{
        return this.lng
    }
}