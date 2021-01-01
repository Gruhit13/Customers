package com.example.customer

import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_display_customer.*
import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*

class DisplayCustomer : AppCompatActivity() {
    lateinit var displayName: EditText
    lateinit var displayNuid: EditText
    lateinit var displayAccNo: EditText

    //  Setting clipboard manager for copying NUID and Account Number
    lateinit var clipboardManager: ClipboardManager

    @RequiresApi(Build.VERSION_CODES.N)
    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_customer)

        val cust_selected: Customer? = intent.getParcelableExtra("cust_selected")
        displayName = findViewById(R.id.dispName)
        displayNuid = findViewById(R.id.dispNuid)
        displayAccNo = findViewById(R.id.dispAccNo)

        //  Initializing ClipBoardManager
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        cust_selected?.let {
            Toast.makeText(this, "Customer Selected:- " + cust_selected._name, Toast.LENGTH_SHORT).show()
            displayContent(cust_selected)
        }

        //  Disabling Edit mode and enabling it on EditButtonClick
        disableEditMode()

        //  Handling Deletion on Data
        val deleteData = findViewById<Button>(R.id.deleteBtn)

        deleteData.setOnClickListener {
            confirmDelete()
        }

        //  Handling Edit data
        val editBtn = findViewById<Button>(R.id.editBtn)

        editBtn.setOnClickListener {
            enableEditMode()
            Toast.makeText(this, "Editor mode on", Toast.LENGTH_SHORT).show()
        }

        //  Handling Save after edit
        val saveBtn = findViewById<Button>(R.id.dispSaveBtn)

        saveBtn.setOnClickListener {
            val name = displayName.text.toString().capitalize(Locale.getDefault())
            val nuid:String = displayNuid.text.toString()
            val accNo:String = displayAccNo.text.toString()
            val months = findViewById<EditText>(R.id.dispMonth).text.toString()

            if(Validator.checkInput(name, nuid, accNo, months, this)){
                val startDate = System.currentTimeMillis()
                val endDate = startDate + getEndDate(months.toInt())

                storeData(Customer(name, nuid, accNo.toLong(), startDate, endDate))
            }
        }

        //  Handling Cancel Button
        val cancelBtn = findViewById<Button>(R.id.dispCancelBtn)
        cancelBtn.setOnClickListener {
            basicAlert()
        }

        //  Handling Copy Nuid
        val copyNuid = findViewById<ImageButton>(R.id.copyNuid)
        copyNuid.setOnClickListener {
            copyContent(displayNuid)
        }

        //  Handling Copy Account Number
        val copyAccNo = findViewById<ImageButton>(R.id.copyAccNo)
        copyAccNo.setOnClickListener {
            copyContent(displayAccNo)
        }
    }

    //  Enabling edit mode
    private fun enableEditMode(){
        displayName.isEnabled = true
        displayNuid.isEnabled = true
        displayAccNo.isEnabled = true

        findViewById<LinearLayout>(R.id.displayLL).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.dispDatesLL).visibility = View.GONE
        findViewById<LinearLayout>(R.id.dispMonthLL).visibility = View.VISIBLE
    }
    private fun disableEditMode(){
        displayName.isEnabled = false
        displayNuid.isEnabled = false
        displayAccNo.isEnabled = false
        findViewById<EditText>(R.id.dispStartDate).isEnabled = false
        findViewById<EditText>(R.id.dispEndDate).isEnabled = false
    }
    //  Stroing Data
    private fun storeData(cust_add:Customer){
        val reff = FirebaseDatabase.getInstance().reference

        reff.child(cust_add._acc.toString()).setValue(cust_add).addOnCompleteListener {
            Toast.makeText(this, "Data Edited Successfull", Toast.LENGTH_SHORT).show()
        }
        //  Return to main activity after storing data
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }

    //  Displaying Content
    private fun displayContent(cust: Customer){
        displayName.setText(cust._name)
        displayNuid.setText(cust._nuid)
        displayAccNo.setText(cust._acc.toString())
        findViewById<EditText>(R.id.dispStartDate).setText(convertLongToDate(cust._st_date))
        findViewById<EditText>(R.id.dispEndDate).setText(convertLongToDate(cust._en_date))
        val isOnline = Customer.ifOnline(cust._st_date, cust._en_date)
        findViewById<TextView>(R.id.dispStatus).text = if(isOnline) "ACTIVE" else "IN-ACTIVE"
    }

    //  Function for postitve and negative buttons
    val positiveButtonClick = { _:DialogInterface, which: Int->
        Toast.makeText(this, "Change Customer Data", Toast.LENGTH_SHORT).show()
    }
    //  Negavtive bUtton for discarding data
    val negativeButtonClick = { _:DialogInterface, which: Int->
        finish()
        Toast.makeText(this, "Changes Discarded", Toast.LENGTH_SHORT).show()
    }

    //  Basic alert on press of cancel button
    private fun basicAlert(){
        val builder = AlertDialog.Builder(this)

        with(builder){
            setTitle("Android Alert")
            setMessage("Changed will be Discarded!!")
            setPositiveButton("Cancel", DialogInterface.OnClickListener(function = positiveButtonClick))
            setNegativeButton("Discard", DialogInterface.OnClickListener(function = negativeButtonClick))
            show()
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    private fun confirmDelete(){
        val dialogBuilder = AlertDialog.Builder(this)
        with(dialogBuilder){
            setTitle("Data Deletetion")
            setMessage("Confirm Delete this Customer ?")
            setPositiveButton("Confirm", DialogInterface.OnClickListener {
                dialog, which ->  deleteUser(findViewById<TextView>(R.id.dispAccNo).text.toString())
            })
            setNegativeButton("Cancel", DialogInterface.OnClickListener {
                dialog, id -> dialog.cancel()
            })
            show()
        }
    }

    //  Delete Specified User
    @RequiresApi(Build.VERSION_CODES.N)
    private fun deleteUser(delChildId: String){
        Log.i("DisplayCustomer", "Customer Deleted")
        val reff = FirebaseDatabase.getInstance().reference
        MainActivity.DisplayCustomerList.removeIf { cust -> cust._acc.toString().equals(delChildId) }
        Log.i("DisplayCustomer", "Item Deleted Size:- " + MainActivity.DisplayCustomerList.size)
        reff.child(delChildId).removeValue()
        finish()
        Toast.makeText(this, "Data Deleted Successfully!!!", Toast.LENGTH_SHORT).show()
    }

    //  Copy Content of specified EditTextBlock
    private fun copyContent(editTextToCopy: EditText){
        clipboardManager.text = editTextToCopy.text.toString()
        Toast.makeText(this, "Content Copied", Toast.LENGTH_SHORT).show()
    }

    //  Convert Long formated string to specified Date format
    private fun convertLongToDate(date:Long): String{
        val date = Date(date)
        val format = SimpleDateFormat("dd/MM/yyyy")
        return format.format(date)
    }

    //  Add number of months to current date to set endDate
    private fun getEndDate(months:Int): Long{
        //  1 Month = 2,592,000,000
        //  1 Day   = 86,400,000
        //            20,200,428
        return (2592000000 * months) + (86400000 * (months - 1))
    }
}
