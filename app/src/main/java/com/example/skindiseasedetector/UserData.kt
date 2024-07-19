package com.example.skindiseasedetector

import com.google.firebase.Timestamp

data class UserData(
    var diagnosis:String? = null,
    var cause:String? = null,
    var symptoms:String? = null,
    var treatment:String? = null,
    var date:String? = null,
    var imageUrl:String?=null,
    var timestamp: Long = 0L

)
