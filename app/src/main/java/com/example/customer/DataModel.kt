package com.example.customer

import android.annotation.SuppressLint
import android.os.Parcelable
import android.os.Parcel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

data class Customer(var _name: String, var _nuid:String, var _acc:Long, var _st_date:Long, var _en_date:Long) : Parcelable{
    companion object CREATOR : Parcelable.Creator<Customer> {

        override fun createFromParcel(parcel: Parcel): Customer {
            return Customer(parcel)
        }

        override fun newArray(size: Int): Array<Customer?> {
            return arrayOfNulls(size)
        }

        fun ifOnline(startDate:Long, endDate:Long): Boolean{
            return Calendar.getInstance().timeInMillis in startDate..endDate
        }
    }

    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(_name)
        parcel.writeString(_nuid)
        parcel.writeLong(_acc)
        parcel.writeLong(_st_date)
        parcel.writeLong(_en_date)
    }

    override fun describeContents(): Int {
        return 0
    }

}