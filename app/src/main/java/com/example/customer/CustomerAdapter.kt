package com.example.customer

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.cust_list.view.*

class CustomerAdapter(val context: Context, val custList: List<Customer>): RecyclerView.Adapter<CustomerAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.cust_list, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        //  Just return the Number of customer
        return custList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val customer = custList[position]
        holder.setData(customer)
    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var cust_selected: Customer? = null

        init {
            itemView.setOnClickListener {
                val intent = Intent(context, DisplayCustomer::class.java)
                intent.action = Intent.ACTION_SEND
                intent.putExtra("cust_selected", this.cust_selected)
                context.startActivity(intent)
            }
        }

        fun setData(customer: Customer){
            itemView.nameChar.text = customer._name[0].toUpperCase().toString()
            itemView.name.text = customer._name
            itemView.nuid.text = customer._nuid.toString()
            itemView.accNo.text = customer._acc.toString()
            val isOnline = Customer.ifOnline(customer._st_date, customer._en_date)
            itemView.status.text = if(isOnline) "ACTIVE" else "IN-ACTIVE"

            this.cust_selected = customer
        }
    }
}
