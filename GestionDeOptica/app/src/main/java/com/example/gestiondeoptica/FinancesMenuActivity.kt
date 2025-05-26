package com.example.gestiondeoptica

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class FinancesMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finances_menu)

        title = getString(R.string.finances_menu_screen_title)

        val btnManageCustomerDebts = findViewById<Button>(R.id.btn_menu_manage_customer_debts)
        val btnManageMonthlyTaxes = findViewById<Button>(R.id.btn_menu_manage_monthly_taxes)

        btnManageCustomerDebts.setOnClickListener {
            startActivity(Intent(this, CustomerDebtListActivity::class.java))
        }

        btnManageMonthlyTaxes.setOnClickListener {
            // Intent to MonthlyTaxListActivity
            // Toast.makeText(this, "Gestión de Impuestos Mensuales (Próximamente)", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, TaxListActivity::class.java))
        }

        val btnViewSalesStats = findViewById<Button>(R.id.btn_menu_view_sales_stats)
        btnViewSalesStats.setOnClickListener {
            startActivity(Intent(this, SalesStatsActivity::class.java))
        }
    }
}
