package com.example.gestiondeoptica

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestiondeoptica.db.entity.Customer

class CustomerAdapter : RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder>() {

    private var customers: List<Customer> = ArrayList()
    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(customer: Customer)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer, parent, false)
        return CustomerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val currentCustomer = customers[position]
        holder.bind(currentCustomer)
    }

    override fun getItemCount() = customers.size

    fun submitList(customerList: List<Customer>) {
        customers = customerList
        notifyDataSetChanged() // Consider using DiffUtil for better performance
    }

    inner class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCustomerName: TextView = itemView.findViewById(R.id.tv_customer_name)
        private val tvCustomerCedula: TextView = itemView.findViewById(R.id.tv_customer_cedula)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(customers[position])
                }
            }
        }

        fun bind(customer: Customer) {
            tvCustomerName.text = "${customer.apellido}, ${customer.nombre}"
            // Ensure context is available, typically from itemView.context
            tvCustomerCedula.text = itemView.context.getString(R.string.item_customer_cedula_prefix) + customer.cedula
        }
    }
}
