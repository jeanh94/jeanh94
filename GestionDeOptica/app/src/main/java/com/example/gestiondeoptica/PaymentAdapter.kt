package com.example.gestiondeoptica

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestiondeoptica.db.entity.DebtPayment
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PaymentAdapter : RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    private var payments: List<DebtPayment> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_debt_payment, parent, false)
        return PaymentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val currentPayment = payments[position]
        holder.bind(currentPayment)
    }

    override fun getItemCount() = payments.size

    fun submitList(paymentList: List<DebtPayment>) {
        payments = paymentList
        notifyDataSetChanged() // Consider DiffUtil for better performance
    }

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPaymentFecha: TextView = itemView.findViewById(R.id.tv_payment_fecha)
        private val tvPaymentMonto: TextView = itemView.findViewById(R.id.tv_payment_monto)

        fun bind(payment: DebtPayment) {
            val context = itemView.context
            tvPaymentFecha.text = context.getString(R.string.item_debt_payment_date_prefix) + SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(payment.fechaAbono))
            tvPaymentMonto.text = context.getString(R.string.item_debt_payment_amount_prefix) + String.format(Locale.US, "%.2f", payment.montoAbono)
        }
    }
}
