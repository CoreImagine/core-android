package com.coreix.test0310

/**
 * Created by user on 3/10/2018.
 */

data class DeviceData(val deviceName:String?,val deviceHardwareAddress: String){
    // fun return Boolean type matching
    override fun equals(other: Any?):Boolean{
        val deviceData = other as DeviceData
        return deviceHardwareAddress == deviceData.deviceHardwareAddress
    }
    // fun hash
    override fun hashCode(): Int{
        return deviceHardwareAddress.hashCode()
    }
}