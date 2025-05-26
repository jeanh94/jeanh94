package com.example.gestiondeoptica

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestiondeoptica.db.entity.Lens

class LensAdapter : RecyclerView.Adapter<LensAdapter.LensViewHolder>() {

    private var lenses: List<Lens> = ArrayList()
    private var listener: OnItemClickListener? = null
    private val lowStockThreshold = 10

    interface OnItemClickListener {
        fun onItemClick(lens: Lens)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LensViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lens, parent, false)
        return LensViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LensViewHolder, position: Int) {
        val currentLens = lenses[position]
        holder.bind(currentLens)
    }

    override fun getItemCount() = lenses.size

    fun submitList(lensList: List<Lens>) {
        lenses = lensList
        notifyDataSetChanged() // Consider using DiffUtil for better performance
    }

    inner class LensViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLensClassification: TextView = itemView.findViewById(R.id.tv_lens_classification)
        private val tvLensStock: TextView = itemView.findViewById(R.id.tv_lens_stock)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(lenses[position])
                }
            }
        }

        fun bind(lens: Lens) {
            val context = itemView.context
            tvLensClassification.text = context.getString(R.string.item_lens_classification_prefix) + lens.clasificacionCristal
            tvLensStock.text = context.getString(R.string.item_frame_stock_prefix) + lens.cantidadStock // Reusing from frame

            if (lens.cantidadStock <= lowStockThreshold) {
                tvLensStock.setTextColor(Color.RED)
            } else {
                tvLensStock.setTextColor(Color.BLACK) // Or your default text color
            }
        }
    }
}
