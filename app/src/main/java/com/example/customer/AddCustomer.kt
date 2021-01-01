package com.example.customer

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_add_customer.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class AddCustomer : AppCompatActivity() {
    var custName: EditText? = null
    var custNUID: EditText? = null
    var custAccNo: EditText? = null
    val TAG:String = "AddCustomer"

    lateinit var reff: DatabaseReference

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_customer)

        custName = findViewById(R.id.cust_name)
        custNUID = findViewById(R.id.cust_nuid)
        custAccNo = findViewById(R.id.cust_acc)


        Log.i(TAG, "Button Obtained")

        //  Storing Data on save
        saveBtn.setOnClickListener {
            val name = custName!!.text.toString().capitalize(Locale.getDefault())
            val nuid:String = custNUID!!.text.toString()
            val accNo:String = custAccNo!!.text.toString()
            val months = findViewById<EditText>(R.id.custMonth).text.toString()

            if(Validator.checkInput(name, nuid, accNo, months, this)){
                val startDate = System.currentTimeMillis()
                val endDate = startDate + getEndDate(months.toInt())

                storeData(Customer(name, nuid, accNo.toLong(), startDate, endDate))
                Log.i(TAG, "Content Obtained Successfully")
            }
        }

        //  Handling Cancel Button
        val cancelBtn = findViewById<Button>(R.id.cancelBtn)
        cancelBtn.setOnClickListener {
            basicAlert()
        }
    }
    private fun convertLongToDate(date:Long): String{
        val date = Date(date)
        val format = SimpleDateFormat("dd/MM/yyyy")
        return format.format(date)
    }

    private fun storeData(cust_add:Customer){
        reff = FirebaseDatabase.getInstance().reference

        Log.i(TAG, "Cust ID Obtained Successful")
        reff.child(cust_add._acc.toString()).setValue(cust_add).addOnCompleteListener {
            Toast.makeText(this, "Data Stored Successfull", Toast.LENGTH_SHORT).show()
        }
        //  Return to main activity after storing data
        finish()
    }

    val positiveButtonClick = { dialog:DialogInterface, which: Int->
        Toast.makeText(this, "Enter Customer Data", Toast.LENGTH_SHORT).show()
    }
    //  Negavtive bUtton for discarding data
    val negativeButtonClick = { dialog:DialogInterface, which: Int->
        finish()
        Toast.makeText(this, "Data Discarded", Toast.LENGTH_SHORT).show()
    }

    private fun basicAlert(){
        val builder = AlertDialog.Builder(this)

        with(builder){
            setTitle("Android Alert")
            setMessage("Discard Data")
            setPositiveButton("Cancel", DialogInterface.OnClickListener(function = positiveButtonClick))
            setNegativeButton("Discard", DialogInterface.OnClickListener(function = negativeButtonClick))
            show()
        }
    }

    private fun getEndDate(months:Int): Long{
        //  1 Month = 2,592,000,000
        //  1 Day   = 86,400,000
        //            20,200,428
        return (2592000000 * months) + (86400000 * (months - 1))
    }
}