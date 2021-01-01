package com.example.customer

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    companion object{
        var DisplayCustomerList = ArrayList<Customer>()
    }
    var CustomerList = ArrayList<Customer>()
    lateinit var reff: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //  Show progress bar before loading data
        showProgressBar()

        //  If Internet Connectivity is Available then
        if(!isConnected()){
            progressBar.visibility = View.GONE
            connectivityAlert()
        }else{
            //  Get Layout Manger using this context and set orientation
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.VERTICAL

            //  set layout manager for recyclerView's LayoutManager
            recycleViewID.layoutManager = layoutManager

            reff = FirebaseDatabase.getInstance().reference

            //  Overriding ValueEventListner
            val getData = object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(DisplayCustomerList.size == 0 || DisplayCustomerList.size != snapshot.childrenCount.toInt()){
                        Log.i("LoadData", "Loaded Data = " + snapshot.childrenCount)

                        for(i in snapshot.children){
                            val Cname = i.child("_name").value.toString()
                            val Cnuid = i.child("_nuid").value.toString()
                            val Cacc = i.child("_acc").value as Long
                            val CstartDate = i.child("_st_date").value as Long
                            val CendDate = i.child("_en_date").value as Long

                            val custObj = Customer(Cname, Cnuid, Cacc, CstartDate, CendDate)
                            if (custObj !in DisplayCustomerList){
                                DisplayCustomerList.add(custObj)
                            }
                        }
                        //  Storing sorted and Unique data to main Customer List
                        DisplayCustomerList = ArrayList(DisplayCustomerList.sortedBy { it._name })

                        CustomerList.addAll(DisplayCustomerList.filter { it !in CustomerList })

                        Log.i("AfterGettingData", "Result = " + DisplayCustomerList.size)

                        setRecycleViewAdapter(DisplayCustomerList)
                        //  HideProgressBar
                        hideProgressBar()
                    }
                }
            }

            val deleteObj = object : ChildEventListener{
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.i("MainActivity", "Child Added")
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    Log.i("AfterDeleting", "Came here OnChildRemoved")
                    setRecycleViewAdapter(DisplayCustomerList)
                }
            }

            //  Overriding Completed
            reff.addListenerForSingleValueEvent(getData)
            reff.addValueEventListener(getData)
            reff.addChildEventListener(deleteObj)
        }
        //  Else Block Completed here

        //  Handling Add New Customer Button
        add_items.setOnClickListener {
            val intent = Intent(this, AddCustomer::class.java)
            startActivity(intent)
        }
    }

    private fun setRecycleViewAdapter(custList:List<Customer>){
        //  Passing contex and DisplayCustomerList to CustomerAdapter
        val adapter = CustomerAdapter(baseContext, custList)

        //  Setting RecycleAdapter to our Customer Adapter
        recycleViewID.adapter = adapter
    }
    private fun showProgressBar(){
        findViewById<RecyclerView>(R.id.recycleViewID).visibility = View.GONE
        findViewById<FloatingActionButton>(R.id.add_items).visibility = View.GONE
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
    }

    private fun hideProgressBar(){
        findViewById<TextView>(R.id.errorTV).visibility = View.GONE
        findViewById<RecyclerView>(R.id.recycleViewID).visibility = View.VISIBLE
        findViewById<FloatingActionButton>(R.id.add_items).visibility = View.VISIBLE
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val menuItem = menu!!.findItem(R.id.search)

        if(menuItem != null){
            val searchView = menuItem.actionView as SearchView

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {

                    if(query!!.isNotEmpty()){
                        DisplayCustomerList.clear()
                        val submitQuery = query.toLowerCase(Locale.getDefault())

                        CustomerList.forEach {
                            if(it._name.equals(submitQuery, true)){
                                DisplayCustomerList.add(it)
                            }
                        }
                    }else{
                        DisplayCustomerList.clear()
                        DisplayCustomerList.addAll(CustomerList)
                    }
                    recycleViewID.adapter!!.notifyDataSetChanged()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {

                    if(newText!!.isNotEmpty()){
                        DisplayCustomerList.clear()
                        val search = newText.toLowerCase(Locale.getDefault())

                        //  Loop Through all Customer Data for matching Name
                        CustomerList.forEach {
                            if(it._name.startsWith(search, true)){
                                DisplayCustomerList.add(it)
                            }
                        }
                    }else{
                        DisplayCustomerList.clear()
                        DisplayCustomerList.addAll(CustomerList)
                        recycleViewID.adapter!!.notifyDataSetChanged()
                    }
                    recycleViewID.adapter!!.notifyDataSetChanged()
                    return true
                }

            })
        }
        return super.onCreateOptionsMenu(menu)
    }

    //  Check Network

    private fun isConnected() : Boolean{
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connectivityManager.activeNetworkInfo

        return networkInfo != null
    }

    private fun connectivityAlert(){
        val builder = AlertDialog.Builder(this)
        with(builder){
            setTitle("No Internet Connection")
            setMessage("Please Connect to a Network")
            setPositiveButton("connect", DialogInterface.OnClickListener{
                _, _ -> startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            })
            setNegativeButton("cancel", DialogInterface.OnClickListener {
                _, _ ->  showErrorText("No Internet Available")})
            show()
        }
    }

    private fun showErrorText(msg:String){
        val textError = findViewById<TextView>(R.id.errorTV)
        textError.text = msg
        textError.visibility = View.VISIBLE
    }
}