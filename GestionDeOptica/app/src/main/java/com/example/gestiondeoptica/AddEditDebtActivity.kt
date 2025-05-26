package com.example.gestiondeoptica

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.example.gestiondeoptica.db.AppDatabase
import com.example.gestiondeoptica.db.dao.CustomerDao
import com.example.gestiondeoptica.db.dao.CustomerDebtDao
import com.example.gestiondeoptica.db.entity.Customer
import com.example.gestiondeoptica.db.entity.CustomerDebt
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class AddEditDebtActivity : AppCompatActivity() {

    private lateinit var actvCustomerCedula: AutoCompleteTextView
    private lateinit var customerInputLayout: TextInputLayout
    private lateinit var etMontoTotal: EditText
    private lateinit var btnSaveDebt: Button

    private lateinit var customerDao: CustomerDao
    private lateinit var customerDebtDao: CustomerDebtDao
    private var allCustomers: List<Customer> = emptyList()
    private var selectedCustomerCedula: String? = null
    private var currentDebtId: Long? = null

    companion object {
        const val EXTRA_DEBT_ID = "com.example.gestiondeoptica.EXTRA_DEBT_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_debt)

        customerInputLayout = findViewById(R.id.actv_debt_customer_cedula_layout)
        actvCustomerCedula = findViewById(R.id.actv_debt_customer_cedula)
        etMontoTotal = findViewById(R.id.et_debt_monto_total)
        btnSaveDebt = findViewById(R.id.btn_save_debt)

        customerDao = AppDatabase.getDatabase(applicationContext).customerDao()
        customerDebtDao = AppDatabase.getDatabase(applicationContext).customerDebtDao()

        setupCustomerAutoComplete()

        if (intent.hasExtra(EXTRA_DEBT_ID)) {
            currentDebtId = intent.getLongExtra(EXTRA_DEBT_ID, -1L)
            if (currentDebtId != -1L) {
                title = getString(R.string.add_edit_debt_title_edit)
                customerInputLayout.isEnabled = false // Disable customer selection in edit mode
                actvCustomerCedula.isEnabled = false
                loadDebtData(currentDebtId!!)
            }
        } else {
            title = getString(R.string.add_edit_debt_title_add)
        }

        btnSaveDebt.setOnClickListener {
            saveDebt()
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
            selectedCustomerCedula = selectedRepresentation.substringAfterLast("-").trim()
        }
    }

    private fun loadDebtData(debtId: Long) {
        val debtLiveData: LiveData<CustomerDebt?> = customerDebtDao.getDebtById(debtId)
        debtLiveData.observe(this) { debt ->
            debt?.let {
                // Find the customer representation to set in AutoCompleteTextView
                val customer = allCustomers.find { c -> c.cedula == it.customerCedula }
                val customerRepresentation = customer?.let { c -> "${c.nombre} ${c.apellido} - ${c.cedula}" } ?: it.customerCedula
                actvCustomerCedula.setText(customerRepresentation, false)
                selectedCustomerCedula = it.customerCedula // Ensure cedula is set for saving
                etMontoTotal.setText(it.montoTotal.toString())
                debtLiveData.removeObservers(this) // Avoid re-triggering
            }
        }
    }

    private fun saveDebt() {
        val cedula = selectedCustomerCedula ?: actvCustomerCedula.text.toString().substringAfterLast("-").trim()

        if (cedula.isEmpty() || allCustomers.none { it.cedula == cedula } && currentDebtId == null) {
            Toast.makeText(this, getString(R.string.add_edit_debt_toast_select_valid_customer), Toast.LENGTH_SHORT).show()
            actvCustomerCedula.error = getString(R.string.add_edit_order_error_customer_required)
            return
        }

        val montoTotalStr = etMontoTotal.text.toString().trim()
        if (montoTotalStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.add_edit_debt_toast_total_amount_required), Toast.LENGTH_SHORT).show()
            etMontoTotal.error = getString(R.string.add_edit_debt_error_total_amount_required)
            return
        }

        val montoTotal = montoTotalStr.toDoubleOrNull()
        if (montoTotal == null || montoTotal <= 0) {
            Toast.makeText(this, getString(R.string.add_edit_debt_toast_total_amount_invalid), Toast.LENGTH_SHORT).show()
            etMontoTotal.error = getString(R.string.add_edit_debt_error_total_amount_invalid)
            return
        }

        val debt = CustomerDebt(
            id = currentDebtId ?: 0L,
            customerCedula = cedula,
            montoTotal = montoTotal,
            fechaCreacion = if (currentDebtId == null) System.currentTimeMillis() else {
                 // Keep original creation date when editing
                 customerDebtDao.getDebtById(currentDebtId!!).value?.fechaCreacion ?: System.currentTimeMillis()
            },
            estadoDeuda = "Pendiente" // For new debts, or if editing, it's assumed we might reset or re-evaluate later
        )

        lifecycleScope.launch {
            if (currentDebtId != null) {
                customerDebtDao.updateDebt(debt)
                Toast.makeText(this@AddEditDebtActivity, getString(R.string.add_edit_debt_toast_updated), Toast.LENGTH_SHORT).show()
            } else {
                customerDebtDao.insertDebt(debt)
                Toast.makeText(this@AddEditDebtActivity, getString(R.string.add_edit_debt_toast_saved), Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}
