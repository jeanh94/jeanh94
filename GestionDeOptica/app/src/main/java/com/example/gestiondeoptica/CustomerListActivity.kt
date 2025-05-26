package com.example.gestiondeoptica

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestiondeoptica.db.AppDatabase
import com.example.gestiondeoptica.db.dao.CustomerDao
import com.example.gestiondeoptica.db.entity.Customer
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CustomerListActivity : AppCompatActivity() {

    private lateinit var etSearchCedula: EditText
    private lateinit var btnSearchCustomer: Button
    private lateinit var btnShowAllCustomers: Button
    private lateinit var rvCustomerList: RecyclerView
    private lateinit var fabAddCustomer: FloatingActionButton

    private lateinit var customerDao: CustomerDao
    private lateinit var customerAdapter: CustomerAdapter

    private var allCustomersObserver: ((List<Customer>) -> Unit)? = null
    private var searchedCustomerObserver: ((Customer?) -> Unit)? = null
    private var currentAllCustomersLiveData: LiveData<List<Customer>>? = null
    private var currentSearchedCustomerLiveData: LiveData<Customer?>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_list)

        etSearchCedula = findViewById(R.id.et_search_cedula)
        btnSearchCustomer = findViewById(R.id.btn_search_customer)
        btnShowAllCustomers = findViewById(R.id.btn_show_all_customers)
        rvCustomerList = findViewById(R.id.rv_customer_list)
        fabAddCustomer = findViewById(R.id.fab_add_customer)

        customerDao = AppDatabase.getDatabase(applicationContext).customerDao()
        setupRecyclerView()
        loadAllCustomers()

        fabAddCustomer.setOnClickListener {
            val intent = Intent(this, AddEditCustomerActivity::class.java)
            startActivity(intent)
        }

        btnSearchCustomer.setOnClickListener {
            val searchText = etSearchCedula.text.toString().trim()
            if (searchText.isNotEmpty()) {
                searchCustomerByCedula(searchText)
            } else {
                Toast.makeText(this, getString(R.string.customer_list_toast_enter_cedula_to_search), Toast.LENGTH_SHORT).show()
            }
        }

        btnShowAllCustomers.setOnClickListener {
            loadAllCustomers()
        }
    }

    private fun setupRecyclerView() {
        customerAdapter = CustomerAdapter()
        rvCustomerList.adapter = customerAdapter
        rvCustomerList.layoutManager = LinearLayoutManager(this)

        customerAdapter.setOnItemClickListener(object : CustomerAdapter.OnItemClickListener {
            override fun onItemClick(customer: Customer) {
                val intent = Intent(this@CustomerListActivity, AddEditCustomerActivity::class.java).apply {
                    putExtra(AddEditCustomerActivity.EXTRA_CUSTOMER_CEDULA, customer.cedula)
                }
                startActivity(intent)
            }
        })
    }

    private fun loadAllCustomers() {
        // Remove previous observer if any
        currentSearchedCustomerLiveData?.removeObservers(this)
        allCustomersObserver?.let { currentAllCustomersLiveData?.removeObserver(it) }


        currentAllCustomersLiveData = customerDao.getAllCustomers()
        allCustomersObserver = { customers ->
            customerAdapter.submitList(customers)
        }
        currentAllCustomersLiveData?.observe(this, allCustomersObserver!!)
    }

    private fun searchCustomerByCedula(cedula: String) {
        // Remove previous observer if any
        currentAllCustomersLiveData?.removeObservers(this)
        searchedCustomerObserver?.let { currentSearchedCustomerLiveData?.removeObserver(it) }

        currentSearchedCustomerLiveData = customerDao.getCustomerByCedula(cedula)
        searchedCustomerObserver = { customer ->
            if (customer != null) {
                customerAdapter.submitList(listOf(customer))
            } else {
                customerAdapter.submitList(emptyList())
                Toast.makeText(this, getString(R.string.customer_list_toast_customer_not_found), Toast.LENGTH_SHORT).show()
            }
        }
        currentSearchedCustomerLiveData?.observe(this, searchedCustomerObserver!!)
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when activity resumes, e.g., after adding/editing a customer
        if (etSearchCedula.text.toString().trim().isEmpty()){
            loadAllCustomers()
        } else {
            // If there was a search query, re-execute it or simply load all.
            // For simplicity here, loading all. Or you could store last search and re-apply.
             loadAllCustomers() // or searchCustomerByCedula(etSearchCedula.text.toString().trim()) if you want to maintain search
        }
    }

     override fun onDestroy() {
        super.onDestroy()
        // Clean up observers to prevent memory leaks
        allCustomersObserver?.let { currentAllCustomersLiveData?.removeObserver(it) }
        searchedCustomerObserver?.let { currentSearchedCustomerLiveData?.removeObserver(it) }
    }
}
