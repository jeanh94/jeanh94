package com.example.gestiondeoptica

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestiondeoptica.db.entity.MonthlyTax
import java.util.*
import kotlin.collections.ArrayList

class TaxAdapter : RecyclerView.Adapter<TaxAdapter.TaxViewHolder>() {

    private var taxes: List<MonthlyTax> = ArrayList()
    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(tax: MonthlyTax)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaxViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tax, parent, false)
        return TaxViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaxViewHolder, position: Int) {
        val currentTax = taxes[position]
        holder.bind(currentTax)
    }

    override fun getItemCount() = taxes.size

    fun submitList(taxList: List<MonthlyTax>) {
        taxes = taxList
        notifyDataSetChanged() // Consider using DiffUtil for better performance
    }

    inner class TaxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTaxType: TextView = itemView.findViewById(R.id.tv_tax_type)
        private val tvTaxPeriod: TextView = itemView.findViewById(R.id.tv_tax_period)
        private val tvTaxAmount: TextView = itemView.findViewById(R.id.tv_tax_amount)
        private val tvTaxStatus: TextView = itemView.findViewById(R.id.tv_tax_status)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(taxes[position])
                }
            }
        }

        fun bind(tax: MonthlyTax) {
            val context = itemView.context
            tvTaxType.text = context.getString(R.string.item_tax_type_prefix) + tax.tipoImpuesto
            tvTaxPeriod.text = context.getString(R.string.item_tax_period_prefix) + "${tax.mes}/${tax.ano}"
            tvTaxAmount.text = context.getString(R.string.item_tax_amount_prefix) + String.format(Locale.US, "%.2f", tax.monto)
            tvTaxStatus.text = context.getString(R.string.item_tax_status_prefix) + tax.estadoImpuesto

            if (tax.estadoImpuesto.equals("Pendiente", ignoreCase = true)) {
                tvTaxStatus.setTextColor(Color.RED)
            } else { // Pagado
                tvTaxStatus.setTextColor(Color.parseColor("#4CAF50")) // Green
            }
        }
    }
}
