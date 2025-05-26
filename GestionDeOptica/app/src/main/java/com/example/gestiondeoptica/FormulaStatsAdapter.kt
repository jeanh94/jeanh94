package com.example.gestiondeoptica

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

data class FormulaStat(
    val formulaKey: String,
    var count: Int,
    val odEsfera: String?,
    val odCilindro: String?,
    val odEje: String?,
    val odAdd: String?,
    val oiEsfera: String?,
    val oiCilindro: String?,
    val oiEje: String?,
    val oiAdd: String?
)

class FormulaStatsAdapter : RecyclerView.Adapter<FormulaStatsAdapter.FormulaStatViewHolder>() {

    private var formulaStats: List<FormulaStat> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormulaStatViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_formula_stat, parent, false)
        return FormulaStatViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FormulaStatViewHolder, position: Int) {
        val currentStat = formulaStats[position]
        holder.bind(currentStat)
    }

    override fun getItemCount() = formulaStats.size

    fun submitList(statList: List<FormulaStat>) {
        formulaStats = statList
        notifyDataSetChanged() // Consider using DiffUtil for better performance
    }

    inner class FormulaStatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFormulaString: TextView = itemView.findViewById(R.id.tv_formula_string)
        private val tvFormulaCount: TextView = itemView.findViewById(R.id.tv_formula_count)

        fun bind(stat: FormulaStat) {
            val context = itemView.context
            val na = context.getString(R.string.item_formula_na)

            val odStr = context.getString(R.string.item_formula_od_prefix) +
                        context.getString(R.string.item_formula_esf_prefix) + (stat.odEsfera ?: na) +
                        context.getString(R.string.item_formula_cil_prefix) + (stat.odCilindro ?: na) +
                        context.getString(R.string.item_formula_eje_prefix) + (stat.odEje ?: na) +
                        context.getString(R.string.item_formula_add_prefix) + (stat.odAdd ?: na)

            val oiStr = context.getString(R.string.item_formula_oi_prefix) +
                        context.getString(R.string.item_formula_esf_prefix) + (stat.oiEsfera ?: na) +
                        context.getString(R.string.item_formula_cil_prefix) + (stat.oiCilindro ?: na) +
                        context.getString(R.string.item_formula_eje_prefix) + (stat.oiEje ?: na) +
                        context.getString(R.string.item_formula_add_prefix) + (stat.oiAdd ?: na)

            tvFormulaString.text = odStr + context.getString(R.string.item_formula_separator) + oiStr
            tvFormulaCount.text = context.getString(R.string.item_formula_requests_prefix) + stat.count
        }
    }
}
