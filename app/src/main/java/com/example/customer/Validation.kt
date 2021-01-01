package com.example.customer

import android.content.Context
import android.widget.Toast

object Validator{
    fun checkInput(name:String, nuid:String, accNo:String, months:String, context: Context) : Boolean{
        if(name.isEmpty()){
            Toast.makeText(context, "Enter Name", Toast.LENGTH_SHORT).show()
            return false
        } else if(nuid.isEmpty()){
            Toast.makeText(context, "Enter NUID Number", Toast.LENGTH_SHORT).show()
            return false
        } else if(accNo.isEmpty()){
            Toast.makeText(context, "Enter Account Number", Toast.LENGTH_SHORT).show()
            return false
        } else if(months.isEmpty()){
            Toast.makeText(context, "Enter Months", Toast.LENGTH_SHORT).show()
            return false
        } else{
            return true
        }
    }
}