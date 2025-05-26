package com.example.gestiondeoptica

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestiondeoptica.db.entity.Customer // Assuming you might want to display customer name
import com.example.gestiondeoptica.db.entity.Order
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

data class OrderWithCustomerName(
    val order: Order,
    val customerName: String? // Nullable if customer might not be found or not fetched
)

class OrderAdapter : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    private var ordersWithCustomerNames: List<OrderWithCustomerName> = ArrayList()
    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(order: Order)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val currentOrderWithCustomerName = ordersWithCustomerNames[position]
        holder.bind(currentOrderWithCustomerName)
    }

    override fun getItemCount() = ordersWithCustomerNames.size

    fun submitList(orderList: List<OrderWithCustomerName>) {
        ordersWithCustomerNames = orderList
        notifyDataSetChanged() // Consider DiffUtil for better performance
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCustomerInfo: TextView = itemView.findViewById(R.id.tv_order_customer_info)
        private val tvOrderDate: TextView = itemView.findViewById(R.id.tv_order_date)
        private val tvOrderMaterial: TextView = itemView.findViewById(R.id.tv_order_material)
        private val tvOrderClassification: TextView = itemView.findViewById(R.id.tv_order_classification)
        private val tvOrderPrice: TextView = itemView.findViewById(R.id.tv_order_price)
        private val tvOrderStatus: TextView = itemView.findViewById(R.id.tv_order_status)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(ordersWithCustomerNames[position].order)
                }
            }
        }

        fun bind(orderWithCustomerName: OrderWithCustomerName) {
            val order = orderWithCustomerName.order
            val order = orderWithCustomerName.order
            val customerNameDisplay = orderWithCustomerName.customerName ?: order.customerCedula // Fallback to cedula
            val context = itemView.context

            tvCustomerInfo.text = context.getString(R.string.item_order_customer_prefix) + customerNameDisplay + " (${order.customerCedula})"
            tvOrderDate.text = context.getString(R.string.item_order_date_prefix) + SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(order.fechaPedido))
            tvOrderMaterial.text = context.getString(R.string.item_order_material_prefix) + order.tipoMaterialCristal
            tvOrderClassification.text = context.getString(R.string.item_order_classification_prefix) + order.clasificacionPedido
            tvOrderPrice.text = context.getString(R.string.item_order_price_prefix) + String.format(Locale.US, "%.2f", order.precio)
            tvOrderStatus.text = context.getString(R.string.item_order_status_prefix) + order.estadoPedido

            if (order.estadoPedido.equals("Pendiente", ignoreCase = true)) {
                tvOrderStatus.setTextColor(Color.parseColor("#FFA500")) // Orange for Pending
            } else if (order.estadoPedido.equals("Completado", ignoreCase = true)) {
                tvOrderStatus.setTextColor(Color.parseColor("#4CAF50")) // Green for Completed
            } else {
                tvOrderStatus.setTextColor(Color.BLACK) // Default
            }
        }
    }
}
