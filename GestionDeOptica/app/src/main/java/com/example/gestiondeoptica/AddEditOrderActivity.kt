package com.example.gestiondeoptica

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.example.gestiondeoptica.db.AppDatabase
import com.example.gestiondeoptica.db.dao.CustomerDao
import com.example.gestiondeoptica.db.dao.OrderDao
import com.example.gestiondeoptica.db.entity.Customer
import com.example.gestiondeoptica.db.entity.Order
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddEditOrderActivity : AppCompatActivity() {

    private lateinit var actvCustomerCedula: AutoCompleteTextView
    private lateinit var customerInputLayout: TextInputLayout
    private lateinit var spinnerTipoMaterialCristal: Spinner
    private lateinit var spinnerClasificacionPedido: Spinner
    private lateinit var btnFechaPedido: Button
    private lateinit var tvSelectedFechaPedido: TextView
    private lateinit var etPrecioPedido: EditText
    private lateinit var spinnerEstadoPedido: Spinner
    private lateinit var etOdEsferaPedido: EditText
    private lateinit var etOdCilindroPedido: EditText
    private lateinit var etOdEjePedido: EditText
    private lateinit var etOdAddPedido: EditText
    private lateinit var etOiEsferaPedido: EditText
    private lateinit var etOiCilindroPedido: EditText
    private lateinit var etOiEjePedido: EditText
    private lateinit var etOiAddPedido: EditText
    private lateinit var btnSaveOrder: Button

    private lateinit var customerDao: CustomerDao
    private lateinit var orderDao: OrderDao
    private var allCustomers: List<Customer> = emptyList()
    private var selectedCustomer: Customer? = null
    private var selectedDateInMillis: Long = System.currentTimeMillis()
    private var currentOrderId: Long? = null

    companion object {
        const val EXTRA_ORDER_ID = "com.example.gestiondeoptica.EXTRA_ORDER_ID"
        const val EXTRA_CUSTOMER_CEDULA_PREFILL = "com.example.gestiondeoptica.EXTRA_CUSTOMER_CEDULA_PREFILL"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_order)

        customerDao = AppDatabase.getDatabase(applicationContext).customerDao()
        orderDao = AppDatabase.getDatabase(applicationContext).orderDao()

        initializeViews()
        setupSpinners()
        setupCustomerAutoComplete()
        setupDatePicker()

        if (intent.hasExtra(EXTRA_ORDER_ID)) {
            currentOrderId = intent.getLongExtra(EXTRA_ORDER_ID, -1L)
            if (currentOrderId != -1L) {
                title = getString(R.string.add_edit_order_title_edit)
                actvCustomerCedula.isEnabled = false // Disallow changing customer in edit mode
                customerInputLayout.isEnabled = false
                loadOrderData(currentOrderId!!)
            }
        } else {
            title = getString(R.string.add_edit_order_title_add)
            // If a cedula is passed (e.g. from Customer history screen), prefill it
            if (intent.hasExtra(EXTRA_CUSTOMER_CEDULA_PREFILL)) {
                val prefillCedula = intent.getStringExtra(EXTRA_CUSTOMER_CEDULA_PREFILL)
                actvCustomerCedula.setText(prefillCedula, false)
                // Find the customer and prefill formula if available
                lifecycleScope.launch {
                    val customer = allCustomers.find { it.cedula == prefillCedula }
                    customer?.let {
                        selectedCustomer = it
                        prefillFormulaFromCustomer(it)
                        actvCustomerCedula.isEnabled = false // Also disable if prefilled
                        customerInputLayout.isEnabled = false
                    }
                }
            }
            updateDateDisplay(selectedDateInMillis) // Set initial date for new orders
        }

        btnSaveOrder.setOnClickListener {
            saveOrder()
        }
    }

    private fun initializeViews() {
        customerInputLayout = findViewById(R.id.actv_customer_cedula_layout) // Assuming you add an ID to TextInputLayout
        actvCustomerCedula = findViewById(R.id.actv_customer_cedula)
        spinnerTipoMaterialCristal = findViewById(R.id.spinner_tipo_material_cristal)
        spinnerClasificacionPedido = findViewById(R.id.spinner_clasificacion_pedido)
        btnFechaPedido = findViewById(R.id.btn_fecha_pedido)
        tvSelectedFechaPedido = findViewById(R.id.tv_selected_fecha_pedido)
        etPrecioPedido = findViewById(R.id.et_precio_pedido)
        spinnerEstadoPedido = findViewById(R.id.spinner_estado_pedido)
        etOdEsferaPedido = findViewById(R.id.et_od_esfera_pedido)
        etOdCilindroPedido = findViewById(R.id.et_od_cilindro_pedido)
        etOdEjePedido = findViewById(R.id.et_od_eje_pedido)
        etOdAddPedido = findViewById(R.id.et_od_add_pedido)
        etOiEsferaPedido = findViewById(R.id.et_oi_esfera_pedido)
        etOiCilindroPedido = findViewById(R.id.et_oi_cilindro_pedido)
        etOiEjePedido = findViewById(R.id.et_oi_eje_pedido)
        etOiAddPedido = findViewById(R.id.et_oi_add_pedido)
        btnSaveOrder = findViewById(R.id.btn_save_order)
    }

    private fun setupSpinners() {
        ArrayAdapter.createFromResource(
            this, R.array.tipo_material_cristal_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTipoMaterialCristal.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this, R.array.clasificacion_pedido_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerClasificacionPedido.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this, R.array.estado_pedido_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerEstadoPedido.adapter = adapter
        }
    }

    private fun setupCustomerAutoComplete() {
        customerDao.getAllCustomers().observe(this) { customers ->
            allCustomers = customers
            val customerRepresentations = customers.map { "${it.nombre} ${it.apellido} - ${it.cedula}" }
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, customerRepresentations)
            actvCustomerCedula.setAdapter(adapter)
        }

        actvCustomerCedula.setOnItemClickListener { parent, _, position, _ ->
            val selectedRepresentation = parent.getItemAtPosition(position) as String
            val cedula = selectedRepresentation.substringAfterLast("-").trim()
            selectedCustomer = allCustomers.find { it.cedula == cedula }
            selectedCustomer?.let {
                prefillFormulaFromCustomer(it)
            }
        }
    }

    private fun prefillFormulaFromCustomer(customer: Customer) {
        etOdEsferaPedido.setText(customer.odEsfera ?: "")
        etOdCilindroPedido.setText(customer.odCilindro ?: "")
        etOdEjePedido.setText(customer.odEje ?: "")
        etOdAddPedido.setText(customer.odAdd ?: "")
        etOiEsferaPedido.setText(customer.oiEsfera ?: "")
        etOiCilindroPedido.setText(customer.oiCilindro ?: "")
        etOiEjePedido.setText(customer.oiEje ?: "")
        etOiAddPedido.setText(customer.oiAdd ?: "")
    }

    private fun setupDatePicker() {
        btnFechaPedido.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDateInMillis

            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth)
                    selectedDateInMillis = selectedCalendar.timeInMillis
                    updateDateDisplay(selectedDateInMillis)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    private fun updateDateDisplay(millis: Long) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvSelectedFechaPedido.text = sdf.format(Date(millis))
    }

    private fun loadOrderData(orderId: Long) {
        val orderLiveData: LiveData<Order?> = orderDao.getOrderById(orderId)
        orderLiveData.observe(this) { order ->
            order?.let {
                actvCustomerCedula.setText(it.customerCedula, false)
                // Fetch the specific customer to ensure 'selectedCustomer' is set for saving
                lifecycleScope.launch {
                     val customer = allCustomers.find { c -> c.cedula == it.customerCedula}
                     selectedCustomer = customer
                }

                setSpinnerSelection(spinnerTipoMaterialCristal, it.tipoMaterialCristal, R.array.tipo_material_cristal_array)
                setSpinnerSelection(spinnerClasificacionPedido, it.clasificacionPedido, R.array.clasificacion_pedido_array)
                setSpinnerSelection(spinnerEstadoPedido, it.estadoPedido, R.array.estado_pedido_array)

                selectedDateInMillis = it.fechaPedido
                updateDateDisplay(it.fechaPedido)
                etPrecioPedido.setText(it.precio.toString())

                etOdEsferaPedido.setText(it.odEsfera ?: "")
                etOdCilindroPedido.setText(it.odCilindro ?: "")
                etOdEjePedido.setText(it.odEje ?: "")
                etOdAddPedido.setText(it.odAdd ?: "")
                etOiEsferaPedido.setText(it.oiEsfera ?: "")
                etOiCilindroPedido.setText(it.oiCilindro ?: "")
                etOiEjePedido.setText(it.oiEje ?: "")
                etOiAddPedido.setText(it.oiAdd ?: "")

                orderLiveData.removeObservers(this) // Avoid re-triggering
            }
        }
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String?, arrayResId: Int) {
        value?.let {
            val adapter = spinner.adapter as ArrayAdapter<String>
            val position = adapter.getPosition(it)
            if (position >= 0) {
                spinner.setSelection(position)
            } else {
                 // If value not in array (e.g. old data), try to find by resource array comparison
                val array = resources.getStringArray(arrayResId)
                val idx = array.indexOf(it)
                if(idx >=0) spinner.setSelection(idx)
            }
        }
    }

    private fun saveOrder() {
        if (selectedCustomer == null && currentOrderId == null) { // Only require selection if new and not prefilled
            Toast.makeText(this, getString(R.string.add_edit_order_toast_select_customer), Toast.LENGTH_SHORT).show()
            actvCustomerCedula.error = getString(R.string.add_edit_order_error_customer_required)
            return
        }
        val cedulaToSave = selectedCustomer?.cedula ?: actvCustomerCedula.text.toString().substringAfterLast("-").trim()
        if (cedulaToSave.isEmpty()){
             Toast.makeText(this, getString(R.string.add_edit_order_toast_customer_cedula_empty), Toast.LENGTH_SHORT).show()
             actvCustomerCedula.error = getString(R.string.add_edit_order_error_cedula_required)
             return
        }


        val precioStr = etPrecioPedido.text.toString().trim()
        if (precioStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.add_edit_order_toast_enter_price), Toast.LENGTH_SHORT).show()
            etPrecioPedido.error = getString(R.string.add_edit_order_error_price_required)
            return
        }
        val precio = precioStr.toDoubleOrNull()
        if (precio == null) {
            Toast.makeText(this, getString(R.string.add_edit_order_toast_invalid_price), Toast.LENGTH_SHORT).show()
            etPrecioPedido.error = getString(R.string.add_edit_order_error_invalid_price)
            return
        }

        val order = Order(
            id = currentOrderId ?: 0L, // If editing, use currentOrderId, else 0 for new
            customerCedula = cedulaToSave,
            tipoMaterialCristal = spinnerTipoMaterialCristal.selectedItem.toString(),
            clasificacionPedido = spinnerClasificacionPedido.selectedItem.toString(),
            fechaPedido = selectedDateInMillis,
            precio = precio,
            estadoPedido = spinnerEstadoPedido.selectedItem.toString(),
            odEsfera = etOdEsferaPedido.text.toString().trim().takeIf { it.isNotEmpty() },
            odCilindro = etOdCilindroPedido.text.toString().trim().takeIf { it.isNotEmpty() },
            odEje = etOdEjePedido.text.toString().trim().takeIf { it.isNotEmpty() },
            odAdd = etOdAddPedido.text.toString().trim().takeIf { it.isNotEmpty() },
            oiEsfera = etOiEsferaPedido.text.toString().trim().takeIf { it.isNotEmpty() },
            oiCilindro = etOiCilindroPedido.text.toString().trim().takeIf { it.isNotEmpty() },
            oiEje = etOiEjePedido.text.toString().trim().takeIf { it.isNotEmpty() },
            oiAdd = etOiAddPedido.text.toString().trim().takeIf { it.isNotEmpty() }
        )

        lifecycleScope.launch {
            val isNewOrder = currentOrderId == null
            if (isNewOrder) {
                orderDao.insertOrder(order)
                Toast.makeText(this@AddEditOrderActivity, getString(R.string.add_edit_order_toast_saved), Toast.LENGTH_SHORT).show()
            } else {
                orderDao.updateOrder(order)
                Toast.makeText(this@AddEditOrderActivity, getString(R.string.add_edit_order_toast_updated), Toast.LENGTH_SHORT).show()
            }

            if (order.estadoPedido.equals("Completado", ignoreCase = true)) {
                // Show specific Toast for completed order
                val message = getString(R.string.add_edit_order_toast_order_completed_prefix) +
                              order.id +
                              getString(R.string.add_edit_order_toast_order_completed_suffix)
                Toast.makeText(this@AddEditOrderActivity, message, Toast.LENGTH_LONG).show()
            }
            finish()
        }
    }
}
