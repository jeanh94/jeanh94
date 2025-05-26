package com.example.gestiondeoptica

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gestiondeoptica.db.entity.Frame

class FrameAdapter : RecyclerView.Adapter<FrameAdapter.FrameViewHolder>() {

    private var frames: List<Frame> = ArrayList()
    private var listener: OnItemClickListener? = null
    private val lowStockThreshold = 10

    interface OnItemClickListener {
        fun onItemClick(frame: Frame)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FrameViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_frame, parent, false)
        return FrameViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FrameViewHolder, position: Int) {
        val currentFrame = frames[position]
        holder.bind(currentFrame)
    }

    override fun getItemCount() = frames.size

    fun submitList(frameList: List<Frame>) {
        frames = frameList
        notifyDataSetChanged() // Consider using DiffUtil for better performance
    }

    inner class FrameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFrameType: TextView = itemView.findViewById(R.id.tv_frame_type)
        private val tvFrameStock: TextView = itemView.findViewById(R.id.tv_frame_stock)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(frames[position])
                }
            }
        }

        fun bind(frame: Frame) {
            val context = itemView.context
            tvFrameType.text = context.getString(R.string.item_frame_type_prefix) + frame.tipoMontura
            tvFrameStock.text = context.getString(R.string.item_frame_stock_prefix) + frame.cantidadStock

            if (frame.cantidadStock <= lowStockThreshold) {
                tvFrameStock.setTextColor(Color.RED)
            } else {
                tvFrameStock.setTextColor(Color.BLACK) // Or your default text color
            }
        }
    }
}
