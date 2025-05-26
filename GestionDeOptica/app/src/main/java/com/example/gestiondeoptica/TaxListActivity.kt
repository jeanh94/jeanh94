package com.example.gestiondeoptica

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestiondeoptica.db.AppDatabase
import com.example.gestiondeoptica.db.dao.MonthlyTaxDao
import com.example.gestiondeoptica.db.entity.MonthlyTax
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TaxListActivity : AppCompatActivity() {

    private lateinit var spinnerFilterTaxStatus: Spinner
    private lateinit var btnClearTaxFilter: Button
    private lateinit var rvTaxList: RecyclerView
    private lateinit var fabAddTax: FloatingActionButton

    private lateinit var monthlyTaxDao: MonthlyTaxDao
    private lateinit var taxAdapter: TaxAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tax_list)

        title = getString(R.string.tax_list_screen_title)

        spinnerFilterTaxStatus = findViewById(R.id.spinner_filter_tax_status)
        btnClearTaxFilter = findViewById(R.id.btn_clear_tax_filter)
        rvTaxList = findViewById(R.id.rv_tax_list)
        fabAddTax = findViewById(R.id.fab_add_tax)

        monthlyTaxDao = AppDatabase.getDatabase(applicationContext).monthlyTaxDao()

        setupRecyclerView()
        setupFilterSpinner()
        loadAllTaxes() // Initial load

        fabAddTax.setOnClickListener {
            val intent = Intent(this, AddEditTaxActivity::class.java)
            startActivity(intent)
        }

        btnClearTaxFilter.setOnClickListener {
            spinnerFilterTaxStatus.setSelection(0) // Assuming first item is "Todos" or similar
            loadAllTaxes()
        }
    }

    private fun setupRecyclerView() {
        taxAdapter = TaxAdapter()
        rvTaxList.adapter = taxAdapter
        rvTaxList.layoutManager = LinearLayoutManager(this)

        taxAdapter.setOnItemClickListener(object : TaxAdapter.OnItemClickListener {
            override fun onItemClick(tax: MonthlyTax) {
                val intent = Intent(this@TaxListActivity, AddEditTaxActivity::class.java).apply {
                    putExtra(AddEditTaxActivity.EXTRA_TAX_ID, tax.id)
                }
                startActivity(intent)
            }
        })
    }

    private fun setupFilterSpinner() {
        // Reusing estado_pedido_array for status filtering
        val spinnerAdapterList = resources.getStringArray(R.array.estado_pedido_array).toMutableList()
        spinnerAdapterList.insert(0, getString(R.string.tax_list_filter_all_taxes)) // Add "Todos" option at the beginning

        val arrayAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            spinnerAdapterList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerFilterTaxStatus.adapter = arrayAdapter

        spinnerFilterTaxStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedStatus = parent.getItemAtPosition(position).toString()
                if (selectedStatus == getString(R.string.tax_list_filter_all_taxes)) {
                    loadAllTaxes()
                } else {
                    filterTaxesByStatus(selectedStatus)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadAllTaxes() {
        monthlyTaxDao.getAllTaxes().observe(this) { taxes ->
            taxAdapter.submitList(taxes)
        }
    }

    private fun filterTaxesByStatus(status: String) {
        if (status == "Pendiente") { // Match the exact string from the array
            monthlyTaxDao.getAllPendingTaxes().observe(this) { taxes ->
                taxAdapter.submitList(taxes)
            }
        } else { // For "Pagado" or any other status if added in future
             monthlyTaxDao.getAllTaxes().observe(this) { taxes ->
                val filteredList = taxes.filter { it.estadoImpuesto.equals(status, ignoreCase = true) }
                taxAdapter.submitList(filteredList)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data based on current filter or all if no filter
        val currentFilter = spinnerFilterTaxStatus.selectedItem.toString()
        if (currentFilter == getString(R.string.tax_list_filter_all_taxes) || spinnerFilterTaxStatus.selectedItemPosition == 0) {
            loadAllTaxes()
        } else {
            filterTaxesByStatus(currentFilter)
        }
    }
}
