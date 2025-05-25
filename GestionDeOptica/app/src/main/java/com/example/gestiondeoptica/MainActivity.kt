package com.example.gestiondeoptica

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

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
            Toast.makeText(this, "Gestión de Pedidos clicked", Toast.LENGTH_SHORT).show()
            // Intent intent = new Intent(MainActivity.this, OrderListActivity.class);
            // startActivity(intent);
        }

        btnManageInventory.setOnClickListener {
            Toast.makeText(this, "Gestión de Inventario clicked", Toast.LENGTH_SHORT).show()
            // Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
            // startActivity(intent);
        }

        btnManageFinances.setOnClickListener {
            Toast.makeText(this, "Gestión de Deudas e Impuestos clicked", Toast.LENGTH_SHORT).show()
            // Intent intent = new Intent(MainActivity.this, FinancesActivity.class);
            // startActivity(intent);
        }
    }
}
