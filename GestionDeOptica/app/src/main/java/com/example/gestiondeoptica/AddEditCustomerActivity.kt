package com.example.gestiondeoptica

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.example.gestiondeoptica.db.AppDatabase
import com.example.gestiondeoptica.db.dao.CustomerDao
import com.example.gestiondeoptica.db.entity.Customer
import kotlinx.coroutines.launch

class AddEditCustomerActivity : AppCompatActivity() {

    private lateinit var etCedula: EditText
    private lateinit var etNombre: EditText
    private lateinit var etApellido: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etDireccion: EditText
    private lateinit var etOdEsfera: EditText
    private lateinit var etOdCilindro: EditText
    private lateinit var etOdEje: EditText
    private lateinit var etOdAdd: EditText
    private lateinit var etOiEsfera: EditText
    private lateinit var etOiCilindro: EditText
    private lateinit var etOiEje: EditText
    private lateinit var etOiAdd: EditText
    private lateinit var btnSaveCustomer: Button
    private lateinit var btnViewPurchaseHistory: Button // Added

    private lateinit var customerDao: CustomerDao
    private var currentCustomerCedula: String? = null

    companion object {
        const val EXTRA_CUSTOMER_CEDULA = "com.example.gestiondeoptica.EXTRA_CUSTOMER_CEDULA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_customer)

        customerDao = AppDatabase.getDatabase(applicationContext).customerDao()

        etCedula = findViewById(R.id.et_cedula)
        etNombre = findViewById(R.id.et_nombre)
        etApellido = findViewById(R.id.et_apellido)
        etTelefono = findViewById(R.id.et_telefono)
        etDireccion = findViewById(R.id.et_direccion)
        etOdEsfera = findViewById(R.id.et_od_esfera)
        etOdCilindro = findViewById(R.id.et_od_cilindro)
        etOdEje = findViewById(R.id.et_od_eje)
        etOdAdd = findViewById(R.id.et_od_add)
        etOiEsfera = findViewById(R.id.et_oi_esfera)
        etOiCilindro = findViewById(R.id.et_oi_cilindro)
        etOiEje = findViewById(R.id.et_oi_eje)
        etOiAdd = findViewById(R.id.et_oi_add)
        btnSaveCustomer = findViewById(R.id.btn_save_customer)
        btnViewPurchaseHistory = findViewById(R.id.btn_view_purchase_history) // Added

        if (intent.hasExtra(EXTRA_CUSTOMER_CEDULA)) {
            currentCustomerCedula = intent.getStringExtra(EXTRA_CUSTOMER_CEDULA)
            title = "Editar Cliente" // Change activity title
            etCedula.isFocusable = false
            etCedula.isClickable = false
            btnViewPurchaseHistory.visibility = android.view.View.VISIBLE // Show button in edit mode

            currentCustomerCedula?.let { cedula ->
                val customerLiveData: LiveData<Customer?> = customerDao.getCustomerByCedula(cedula)
                customerLiveData.observe(this) { customer ->
                    customer?.let {
                        populateFields(it)
                        // Important to remove observer after first load to prevent issues on config change or re-observe
                        customerLiveData.removeObservers(this)
                    }
                }
            }
        } else {
            title = "Agregar Cliente Nuevo" // Change activity title
            btnViewPurchaseHistory.visibility = android.view.View.GONE // Hide button in add mode
        }

        btnSaveCustomer.setOnClickListener {
            saveCustomer()
        }

        btnViewPurchaseHistory.setOnClickListener { // Added
            Toast.makeText(this, "El historial de compras se implementará próximamente.", Toast.LENGTH_LONG).show()
        }
    }

    private fun populateFields(customer: Customer) {
        etCedula.setText(customer.cedula)
        etNombre.setText(customer.nombre)
        etApellido.setText(customer.apellido)
        etTelefono.setText(customer.telefono)
        etDireccion.setText(customer.direccion)
        etOdEsfera.setText(customer.odEsfera ?: "")
        etOdCilindro.setText(customer.odCilindro ?: "")
        etOdEje.setText(customer.odEje ?: "")
        etOdAdd.setText(customer.odAdd ?: "")
        etOiEsfera.setText(customer.oiEsfera ?: "")
        etOiCilindro.setText(customer.oiCilindro ?: "")
        etOiEje.setText(customer.oiEje ?: "")
        etOiAdd.setText(customer.oiAdd ?: "")
    }

    private fun saveCustomer() {
        val cedula = etCedula.text.toString().trim()
        val nombre = etNombre.text.toString().trim()
        val apellido = etApellido.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val direccion = etDireccion.text.toString().trim()

        if (cedula.isEmpty()) {
            Toast.makeText(this, "Cédula es obligatoria", Toast.LENGTH_SHORT).show()
            etCedula.error = "Cédula es obligatoria"
            return
        }
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Nombre es obligatorio", Toast.LENGTH_SHORT).show()
            etNombre.error = "Nombre es obligatorio"
            return
        }
        if (apellido.isEmpty()) {
            Toast.makeText(this, "Apellido es obligatorio", Toast.LENGTH_SHORT).show()
            etApellido.error = "Apellido es obligatorio"
            return
        }
         if (telefono.isEmpty()) {
            Toast.makeText(this, "Teléfono es obligatorio", Toast.LENGTH_SHORT).show()
            etTelefono.error = "Teléfono es obligatorio"
            return
        }
        if (direccion.isEmpty()) {
            Toast.makeText(this, "Dirección es obligatoria", Toast.LENGTH_SHORT).show()
            etDireccion.error = "Dirección es obligatoria"
            return
        }


        val customer = Customer(
            cedula = cedula,
            nombre = nombre,
            apellido = apellido,
            telefono = telefono,
            direccion = direccion,
            odEsfera = etOdEsfera.text.toString().trim().takeIf { it.isNotEmpty() },
            odCilindro = etOdCilindro.text.toString().trim().takeIf { it.isNotEmpty() },
            odEje = etOdEje.text.toString().trim().takeIf { it.isNotEmpty() },
            odAdd = etOdAdd.text.toString().trim().takeIf { it.isNotEmpty() },
            oiEsfera = etOiEsfera.text.toString().trim().takeIf { it.isNotEmpty() },
            oiCilindro = etOiCilindro.text.toString().trim().takeIf { it.isNotEmpty() },
            oiEje = etOiEje.text.toString().trim().takeIf { it.isNotEmpty() },
            oiAdd = etOiAdd.text.toString().trim().takeIf { it.isNotEmpty() }
        )

        lifecycleScope.launch {
            if (currentCustomerCedula != null) { // Edit mode
                customerDao.updateCustomer(customer)
                Toast.makeText(this@AddEditCustomerActivity, "Cliente actualizado", Toast.LENGTH_SHORT).show()
            } else { // Add mode
                // Check if customer with this cedula already exists, even though we use REPLACE
                // It's good UX to inform the user.
                val existingCustomer = customerDao.getCustomerByCedula(cedula).value // This might need to be observed or run in a different way if not directly accessible
                if (existingCustomer != null) {
                     Toast.makeText(this@AddEditCustomerActivity, "Un cliente con esta cédula ya existe. Actualizando datos.", Toast.LENGTH_LONG).show()
                }
                customerDao.insertCustomer(customer) // Due to OnConflictStrategy.REPLACE, this will update if exists
                Toast.makeText(this@AddEditCustomerActivity, "Cliente guardado", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}
