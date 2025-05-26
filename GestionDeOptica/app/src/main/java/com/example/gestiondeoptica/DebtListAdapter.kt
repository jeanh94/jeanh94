package com.example.gestiondeoptica

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestiondeoptica.db.entity.CustomerDebt
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

data class DebtWithDetails(
    val debt: CustomerDebt,
    val customerName: String?, // Nullable if customer might not be found
    val totalPaid: Double
) {
    val saldoPendiente: Double
        get() = debt.montoTotal - totalPaid
}

class DebtListAdapter : RecyclerView.Adapter<DebtListAdapter.DebtViewHolder>() {

    private var debtsWithDetails: List<DebtWithDetails> = ArrayList()
    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(debt: CustomerDebt)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer_debt, parent, false)
        return DebtViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DebtViewHolder, position: Int) {
        val currentDebtWithDetails = debtsWithDetails[position]
        holder.bind(currentDebtWithDetails)
    }

    override fun getItemCount() = debtsWithDetails.size

    fun submitList(debtList: List<DebtWithDetails>) {
        debtsWithDetails = debtList
        notifyDataSetChanged() // Consider DiffUtil
    }

    inner class DebtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCustomerInfo: TextView = itemView.findViewById(R.id.tv_debt_customer_info)
        private val tvMontoTotal: TextView = itemView.findViewById(R.id.tv_debt_monto_total)
        private val tvFechaCreacion: TextView = itemView.findViewById(R.id.tv_debt_fecha_creacion)
        private val tvEstado: TextView = itemView.findViewById(R.id.tv_debt_estado)
        private val tvSaldoPendiente: TextView = itemView.findViewById(R.id.tv_debt_saldo_pendiente)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(debtsWithDetails[position].debt)
                }
            }
        }

        fun bind(debtWithDetails: DebtWithDetails) {
            val debt = debtWithDetails.debt
            val debt = debtWithDetails.debt
            val customerNameDisplay = debtWithDetails.customerName ?: debt.customerCedula
            val context = itemView.context

            tvCustomerInfo.text = context.getString(R.string.item_customer_debt_customer_prefix) + customerNameDisplay + context.getString(R.string.item_customer_debt_debt_id_middle) + debt.id
            tvMontoTotal.text = context.getString(R.string.item_customer_debt_total_amount_prefix) + String.format(Locale.US, "%.2f", debt.montoTotal)
            tvFechaCreacion.text = context.getString(R.string.item_customer_debt_creation_date_prefix) + SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(debt.fechaCreacion))
            tvEstado.text = context.getString(R.string.item_customer_debt_status_prefix) + debt.estadoDeuda
            tvSaldoPendiente.text = context.getString(R.string.item_customer_debt_pending_balance_prefix) + String.format(Locale.US, "%.2f", debtWithDetails.saldoPendiente)

            if (debt.estadoDeuda.equals("Pendiente", ignoreCase = true)) {
                tvEstado.setTextColor(Color.RED)
                tvSaldoPendiente.setTextColor(Color.RED)
            } else { // Pagada
                tvEstado.setTextColor(Color.parseColor("#4CAF50")) // Green
                tvSaldoPendiente.setTextColor(Color.parseColor("#4CAF50"))
            }
        }
    }
}
