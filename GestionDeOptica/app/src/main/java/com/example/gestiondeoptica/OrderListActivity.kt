package com.example.gestiondeoptica

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestiondeoptica.db.AppDatabase
import com.example.gestiondeoptica.db.dao.CustomerDao
import com.example.gestiondeoptica.db.dao.OrderDao
import com.example.gestiondeoptica.db.entity.Customer
import com.example.gestiondeoptica.db.entity.Order
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OrderListActivity : AppCompatActivity() {

    private lateinit var spinnerFilterOrderStatus: Spinner
    private lateinit var btnClearFilter: Button
    private lateinit var rvOrderList: RecyclerView
    private lateinit var fabAddOrder: FloatingActionButton

    private lateinit var orderDao: OrderDao
    private lateinit var customerDao: CustomerDao
    private lateinit var orderAdapter: OrderAdapter

    private var currentOrderListLiveData: LiveData<List<Order>>? = null
    private val combinedOrderData = MediatorLiveData<List<OrderWithCustomerName>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_list)

        spinnerFilterOrderStatus = findViewById(R.id.spinner_filter_order_status)
        btnClearFilter = findViewById(R.id.btn_clear_filter)
        rvOrderList = findViewById(R.id.rv_order_list)
        fabAddOrder = findViewById(R.id.fab_add_order)

        orderDao = AppDatabase.getDatabase(applicationContext).orderDao()
        customerDao = AppDatabase.getDatabase(applicationContext).customerDao()

        setupRecyclerView()
        setupFilterSpinner()
        loadAllOrders() // Initial load

        fabAddOrder.setOnClickListener {
            val intent = Intent(this, AddEditOrderActivity::class.java)
            startActivity(intent)
        }

        btnClearFilter.setOnClickListener {
            spinnerFilterOrderStatus.setSelection(0) // Assuming first item is "Todos" or similar
            loadAllOrders()
        }
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter()
        rvOrderList.adapter = orderAdapter
        rvOrderList.layoutManager = LinearLayoutManager(this)

        orderAdapter.setOnItemClickListener(object : OrderAdapter.OnItemClickListener {
            override fun onItemClick(order: Order) {
                val intent = Intent(this@OrderListActivity, AddEditOrderActivity::class.java).apply {
                    putExtra(AddEditOrderActivity.EXTRA_ORDER_ID, order.id)
                    putExtra(AddEditOrderActivity.EXTRA_CUSTOMER_CEDULA_PREFILL, order.customerCedula)
                }
                startActivity(intent)
            }
        })
    }
     private fun setupFilterSpinner() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        val spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.estado_pedido_array, // Re-use the array from AddEditOrder
            android.R.layout.simple_spinner_item
        ).toMutableList()

        // Add "Todos" at the beginning
        spinnerAdapter.insert(getString(R.string.order_list_filter_all_orders), 0)


        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerAdapter).apply {
             setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerFilterOrderStatus.adapter = arrayAdapter


        spinnerFilterOrderStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedStatus = parent.getItemAtPosition(position).toString()
                if (selectedStatus == getString(R.string.order_list_filter_all_orders)) {
                    loadAllOrders()
                } else {
                    filterOrdersByStatus(selectedStatus)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun processOrdersWithCustomerNames(orders: List<Order>) {
        // For each order, fetch customer name. This is inefficient but for simplicity.
        // A better approach uses a ViewModel and Repository pattern, or a JOIN query.
        val resultList = mutableListOf<OrderWithCustomerName>()
        // This part needs to be async or handled carefully to avoid blocking UI
        // For LiveData, this kind of transformation is tricky without complex LiveData chains or Coroutines.

        // Simplified: Observe all customers once, then map.
        customerDao.getAllCustomers().observe(this, object : androidx.lifecycle.Observer<List<Customer>> {
            override fun onChanged(customers: List<Customer>) {
                customerDao.getAllCustomers().removeObserver(this) // Observe only once for this transformation
                val customerMap = customers.associateBy { it.cedula }
                resultList.clear()
                for (order in orders) {
                    val customerName = customerMap[order.customerCedula]?.let { "${it.nombre} ${it.apellido}" } ?: order.customerCedula
                    resultList.add(OrderWithCustomerName(order, customerName))
                }
                combinedOrderData.postValue(resultList)
            }
        })
    }


    private fun loadAllOrders() {
        currentOrderListLiveData?.removeObservers(this)
        currentOrderListLiveData = orderDao.getAllOrders()
        currentOrderListLiveData?.observe(this) { orders ->
            processOrdersWithCustomerNames(orders)
        }

        combinedOrderData.observe(this) { ordersWithNames ->
            orderAdapter.submitList(ordersWithNames)
        }
    }

    private fun filterOrdersByStatus(status: String) {
        currentOrderListLiveData?.removeObservers(this) // Remove previous observers
        currentOrderListLiveData = orderDao.getOrdersByStatus(status)
        currentOrderListLiveData?.observe(this) { orders ->
             processOrdersWithCustomerNames(orders)
        }
         combinedOrderData.observe(this) { ordersWithNames ->
            orderAdapter.submitList(ordersWithNames)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data based on current filter or all if no filter
        val currentFilter = spinnerFilterOrderStatus.selectedItem.toString()
        if (currentFilter == getString(R.string.order_list_filter_all_orders) || spinnerFilterOrderStatus.selectedItemPosition == 0) {
            loadAllOrders()
        } else {
            filterOrdersByStatus(currentFilter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up observers
        currentOrderListLiveData?.removeObservers(this)
        combinedOrderData.removeObservers(this)
    }
}
