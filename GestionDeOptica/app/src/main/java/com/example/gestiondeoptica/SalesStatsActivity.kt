package com.example.gestiondeoptica

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestiondeoptica.db.AppDatabase
import com.example.gestiondeoptica.db.dao.OrderDao
import com.example.gestiondeoptica.db.entity.Order

class SalesStatsActivity : AppCompatActivity() {

    private lateinit var rvFormulaStats: RecyclerView
    private lateinit var orderDao: OrderDao
    private lateinit var formulaStatsAdapter: FormulaStatsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_stats)
        title = "Estadísticas de Ventas"

        rvFormulaStats = findViewById(R.id.rv_formula_stats)
        orderDao = AppDatabase.getDatabase(applicationContext).orderDao()

        setupRecyclerView()
        loadAndProcessOrderData()
    }

    private fun setupRecyclerView() {
        formulaStatsAdapter = FormulaStatsAdapter()
        rvFormulaStats.adapter = formulaStatsAdapter
        rvFormulaStats.layoutManager = LinearLayoutManager(this)
    }

    private fun loadAndProcessOrderData() {
        orderDao.getAllOrders().observe(this) { orders ->
            val formulaMap = mutableMapOf<String, FormulaStat>()

            orders.forEach { order ->
                val formulaKey = createFormulaKey(order)
                val stat = formulaMap.getOrPut(formulaKey) {
                    FormulaStat(
                        formulaKey = formulaKey,
                        count = 0,
                        odEsfera = order.odEsfera,
                        odCilindro = order.odCilindro,
                        odEje = order.odEje,
                        odAdd = order.odAdd,
                        oiEsfera = order.oiEsfera,
                        oiCilindro = order.oiCilindro,
                        oiEje = order.oiEje,
                        oiAdd = order.oiAdd
                    )
                }
                stat.count++
            }

            val sortedStats = formulaMap.values.sortedByDescending { it.count }
            formulaStatsAdapter.submitList(sortedStats)
        }
    }

    private fun createFormulaKey(order: Order): String {
        return listOf(
            order.odEsfera ?: "N/A",
            order.odCilindro ?: "N/A",
            order.odEje ?: "N/A",
            order.odAdd ?: "N/A",
            order.oiEsfera ?: "N/A",
            order.oiCilindro ?: "N/A",
            order.oiEje ?: "N/A",
            order.oiAdd ?: "N/A"
        ).joinToString("|")
    }
}
