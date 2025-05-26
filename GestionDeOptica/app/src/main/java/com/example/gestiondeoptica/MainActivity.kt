package com.example.gestiondeoptica

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.view.Menu
import android.view.MenuItem
import android.content.Intent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnManageCustomers = findViewById<Button>(R.id.btn_manage_customers)
        val btnManageOrders = findViewById<Button>(R.id.btn_manage_orders)
        val btnManageInventory = findViewById<Button>(R.id.btn_manage_inventory)
        val btnManageFinances = findViewById<Button>(R.id.btn_manage_finances)

        btnManageCustomers.setOnClickListener {
            // Toast.makeText(this, "Gestión de Clientes clicked", Toast.LENGTH_SHORT).show()
            android.content.Intent(this, CustomerListActivity::class.java).also { startActivity(it) }
        }

        btnManageOrders.setOnClickListener {
            // Toast.makeText(this, "Gestión de Pedidos clicked", Toast.LENGTH_SHORT).show()
            android.content.Intent(this, OrderListActivity::class.java).also { startActivity(it) }
        }

        btnManageInventory.setOnClickListener {
            // Toast.makeText(this, "Gestión de Inventario clicked", Toast.LENGTH_SHORT).show()
            android.content.Intent(this, InventoryMenuActivity::class.java).also { startActivity(it) }
        }

        btnManageFinances.setOnClickListener {
            // Toast.makeText(this, "Gestión de Deudas e Impuestos clicked", Toast.LENGTH_SHORT).show()
            android.content.Intent(this, FinancesMenuActivity::class.java).also { startActivity(it) }
        }

        // Initialize DAOs
        val customerDebtDao = AppDatabase.getDatabase(applicationContext).customerDebtDao()
        val monthlyTaxDao = AppDatabase.getDatabase(applicationContext).monthlyTaxDao()

        // Initialize TextViews for financial summary
        val tvTotalPendingDebts = findViewById<android.widget.TextView>(R.id.tv_total_pending_debts)
        val tvTotalPendingTaxes = findViewById<android.widget.TextView>(R.id.tv_total_pending_taxes)

        // Observe total pending debts
        // Using the new DAO method getTotalPendingDebtAmount()
        customerDebtDao.getTotalPendingDebtAmount().observe(this) { total ->
            val totalAmount = total ?: 0.0
            tvTotalPendingDebts.text = getString(R.string.main_total_pending_debts_label_prefix) + " $${String.format(java.util.Locale.US, "%.2f", totalAmount)}"
        }

        // Observe total pending taxes
        monthlyTaxDao.getAllPendingTaxes().observe(this) { taxes ->
            val totalTaxAmount = taxes.sumOf { it.monto }
            tvTotalPendingTaxes.text = getString(R.string.main_total_pending_taxes_label_prefix) + " $${String.format(java.util.Locale.US, "%.2f", totalTaxAmount)}"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
