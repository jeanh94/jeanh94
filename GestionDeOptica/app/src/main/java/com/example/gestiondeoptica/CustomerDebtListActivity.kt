package com.example.gestiondeoptica

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestiondeoptica.db.AppDatabase
import com.example.gestiondeoptica.db.dao.CustomerDao
import com.example.gestiondeoptica.db.dao.CustomerDebtDao
import com.example.gestiondeoptica.db.entity.Customer
import com.example.gestiondeoptica.db.entity.CustomerDebt
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CustomerDebtListActivity : AppCompatActivity() {

    private lateinit var rvCustomerDebtList: RecyclerView
    private lateinit var fabAddDebt: FloatingActionButton

    private lateinit var customerDebtDao: CustomerDebtDao
    private lateinit var customerDao: CustomerDao
    private lateinit var debtListAdapter: DebtListAdapter

    private val combinedDebtData = MediatorLiveData<List<DebtWithDetails>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_debt_list)

        title = getString(R.string.customer_debt_list_screen_title)

        rvCustomerDebtList = findViewById(R.id.rv_customer_debt_list)
        fabAddDebt = findViewById(R.id.fab_add_debt)

        customerDebtDao = AppDatabase.getDatabase(applicationContext).customerDebtDao()
        customerDao = AppDatabase.getDatabase(applicationContext).customerDao()

        setupRecyclerView()
        loadAllPendingDebts() // Initially load pending debts

        fabAddDebt.setOnClickListener {
            val intent = Intent(this, AddEditDebtActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        debtListAdapter = DebtListAdapter()
        rvCustomerDebtList.adapter = debtListAdapter
        rvCustomerDebtList.layoutManager = LinearLayoutManager(this)

        debtListAdapter.setOnItemClickListener(object : DebtListAdapter.OnItemClickListener {
            override fun onItemClick(debt: CustomerDebt) {
                val intent = Intent(this@CustomerDebtListActivity, DebtDetailsActivity::class.java).apply {
                    putExtra(DebtDetailsActivity.EXTRA_DEBT_ID, debt.id)
                }
                startActivity(intent)
            }
        })
    }

    private fun loadAllPendingDebts() {
        val allDebtsLiveData = customerDebtDao.getAllPendingDebts() // Or getAllDebts() if you want to show all
        val allCustomersLiveData = customerDao.getAllCustomers()

        combinedDebtData.addSource(allDebtsLiveData) { debts ->
            combineDebtAndCustomerData(debts, allCustomersLiveData.value)
        }
        combinedDebtData.addSource(allCustomersLiveData) { customers ->
            combineDebtAndCustomerData(allDebtsLiveData.value, customers)
        }

        combinedDebtData.observe(this, Observer { debtWithDetailsList ->
            debtListAdapter.submitList(debtWithDetailsList)
        })
    }

    private fun combineDebtAndCustomerData(debts: List<CustomerDebt>?, customers: List<Customer>?) {
        if (debts == null || customers == null) return

        val customerMap = customers.associateBy { it.cedula }
        val resultList = mutableListOf<DebtWithDetails>()

        for (debt in debts) {
            // Asynchronously fetch total paid for each debt
            // This is a simplification. For many debts, this would be inefficient.
            // A better approach might involve a more complex LiveData structure or ViewModel.
            customerDebtDao.getTotalPaymentsForDebt(debt.id).observe(this, object : Observer<Double?> {
                override fun onChanged(totalPaid: Double?) {
                    // Remove observer to prevent multiple triggers for the same debt from this specific call
                    customerDebtDao.getTotalPaymentsForDebt(debt.id).removeObserver(this)

                    val customerName = customerMap[debt.customerCedula]?.let { "${it.nombre} ${it.apellido}" }
                    resultList.removeAll { it.debt.id == debt.id } // Remove old entry if it exists
                    resultList.add(DebtWithDetails(debt, customerName, totalPaid ?: 0.0))

                    // Sort or re-evaluate the list if necessary before posting
                    // For simplicity, posting the updated list.
                    // This can lead to multiple updates to the adapter.
                    // A more robust solution would collect all results before updating.
                    combinedDebtData.postValue(ArrayList(resultList)) // Post a new list to trigger update
                }
            })
        }
        // Initial empty list or list without payment details before async calls complete
        if (resultList.isEmpty() && debts.isNotEmpty()){
             val initialResultList = debts.map { debt ->
                val customerName = customerMap[debt.customerCedula]?.let { "${it.nombre} ${it.apellido}" }
                DebtWithDetails(debt, customerName, 0.0) // Assume 0 paid initially
            }
            debtListAdapter.submitList(initialResultList) // Show initial list quickly
        }

    }


    override fun onResume() {
        super.onResume()
        // Data will be refreshed by LiveData automatically in most cases
        // However, the complex combination might need a nudge or re-observation if data sources change.
        // For now, relying on LiveData's nature.
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up observers from combinedDebtData if necessary, though LiveData handles lifecycle.
    }
}
