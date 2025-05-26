package com.example.gestiondeoptica

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.gestiondeoptica.db.AppDatabase
import com.example.gestiondeoptica.db.dao.OrderDao
import com.example.gestiondeoptica.db.model.OrderClassificationCount
import java.util.*

class OrderStatsActivity : AppCompatActivity() {

    private lateinit var tvStatsTitle: TextView
    private lateinit var tvStatsTotalCompletedOrders: TextView
    private lateinit var tvStatsVisionSencilla: TextView
    private lateinit var tvStatsBifocales: TextView
    private lateinit var tvStatsProgresivos: TextView

    private lateinit var orderDao: OrderDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_stats)

        title = getString(R.string.order_stats_screen_title) // Set activity title

        tvStatsTitle = findViewById(R.id.tv_stats_title)
        tvStatsTotalCompletedOrders = findViewById(R.id.tv_stats_total_completed_orders)
        tvStatsVisionSencilla = findViewById(R.id.tv_stats_vision_sencilla)
        tvStatsBifocales = findViewById(R.id.tv_stats_bifocales)
        tvStatsProgresivos = findViewById(R.id.tv_stats_progresivos)

        orderDao = AppDatabase.getDatabase(applicationContext).orderDao()

        loadOrderStats()
    }

    private fun loadOrderStats() {
        orderDao.getCompletedOrderCountsByType().observe(this) { stats ->
            var totalCompletedOrders = 0
            var countVisionSencilla = 0
            var countBifocales = 0
            var countProgresivos = 0

            stats.forEach { stat ->
                totalCompletedOrders += stat.count
                when (stat.clasificacionPedido) {
                    "Visión Sencilla" -> countVisionSencilla = stat.count
                    "Bifocales" -> countBifocales = stat.count
                    "Progresivos" -> countProgresivos = stat.count
                }
            }

            tvStatsTotalCompletedOrders.text = getString(R.string.order_stats_total_completed_orders_prefix) + totalCompletedOrders

            val percentageVisionSencilla = if (totalCompletedOrders > 0) (countVisionSencilla.toDouble() / totalCompletedOrders) * 100 else 0.0
            val percentageBifocales = if (totalCompletedOrders > 0) (countBifocales.toDouble() / totalCompletedOrders) * 100 else 0.0
            val percentageProgresivos = if (totalCompletedOrders > 0) (countProgresivos.toDouble() / totalCompletedOrders) * 100 else 0.0

            tvStatsVisionSencilla.text = String.format(Locale.US, getString(R.string.order_stats_vision_sencilla_format), countVisionSencilla, percentageVisionSencilla)
            tvStatsBifocales.text = String.format(Locale.US, getString(R.string.order_stats_bifocales_format), countBifocales, percentageBifocales)
            tvStatsProgresivos.text = String.format(Locale.US, getString(R.string.order_stats_progresivos_format), countProgresivos, percentageProgresivos)
        }
    }
}
