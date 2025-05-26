package com.example.gestiondeoptica

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.example.gestiondeoptica.db.AppDatabase
import com.example.gestiondeoptica.db.dao.MonthlyTaxDao
import com.example.gestiondeoptica.db.entity.MonthlyTax
import kotlinx.coroutines.launch
import java.util.Calendar

class AddEditTaxActivity : AppCompatActivity() {

    private lateinit var spinnerTipoImpuesto: Spinner
    private lateinit var etTaxMonto: EditText
    private lateinit var etTaxMes: EditText
    private lateinit var etTaxAno: EditText
    private lateinit var spinnerEstadoImpuesto: Spinner
    private lateinit var btnSaveTax: Button

    private lateinit var monthlyTaxDao: MonthlyTaxDao
    private var currentTaxId: Long? = null

    companion object {
        const val EXTRA_TAX_ID = "com.example.gestiondeoptica.EXTRA_TAX_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_tax)

        spinnerTipoImpuesto = findViewById(R.id.spinner_tipo_impuesto)
        etTaxMonto = findViewById(R.id.et_tax_monto)
        etTaxMes = findViewById(R.id.et_tax_mes)
        etTaxAno = findViewById(R.id.et_tax_ano)
        spinnerEstadoImpuesto = findViewById(R.id.spinner_estado_impuesto)
        btnSaveTax = findViewById(R.id.btn_save_tax)

        monthlyTaxDao = AppDatabase.getDatabase(applicationContext).monthlyTaxDao()

        setupSpinners()

        if (intent.hasExtra(EXTRA_TAX_ID)) {
            currentTaxId = intent.getLongExtra(EXTRA_TAX_ID, -1L)
            if (currentTaxId != -1L) {
                title = "Editar Impuesto"
                loadTaxData(currentTaxId!!)
            }
        } else {
            title = "Agregar Impuesto Nuevo"
        }

        btnSaveTax.setOnClickListener {
            saveTax()
        }
    }

    private fun setupSpinners() {
        ArrayAdapter.createFromResource(
            this, R.array.tipo_impuesto_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTipoImpuesto.adapter = adapter
        }

        // Reusing estado_pedido_array for estado_impuesto_array
        ArrayAdapter.createFromResource(
            this, R.array.estado_pedido_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerEstadoImpuesto.adapter = adapter
        }
    }

    private fun loadTaxData(taxId: Long) {
        val taxLiveData: LiveData<MonthlyTax?> = monthlyTaxDao.getTaxById(taxId)
        taxLiveData.observe(this) { tax ->
            tax?.let {
                setSpinnerSelection(spinnerTipoImpuesto, it.tipoImpuesto, R.array.tipo_impuesto_array)
                etTaxMonto.setText(it.monto.toString())
                etTaxMes.setText(it.mes.toString())
                etTaxAno.setText(it.ano.toString())
                setSpinnerSelection(spinnerEstadoImpuesto, it.estadoImpuesto, R.array.estado_pedido_array)
                taxLiveData.removeObservers(this) // Avoid re-triggering
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
                 val array = resources.getStringArray(arrayResId)
                val idx = array.indexOf(it)
                if(idx >=0) spinner.setSelection(idx)
            }
        }
    }

    private fun saveTax() {
        val tipoImpuesto = spinnerTipoImpuesto.selectedItem.toString()
        val montoStr = etTaxMonto.text.toString().trim()
        val mesStr = etTaxMes.text.toString().trim()
        val anoStr = etTaxAno.text.toString().trim()
        val estadoImpuesto = spinnerEstadoImpuesto.selectedItem.toString()

        if (montoStr.isEmpty()) {
            Toast.makeText(this, "Monto es requerido", Toast.LENGTH_SHORT).show()
            etTaxMonto.error = "Requerido"
            return
        }
        val monto = montoStr.toDoubleOrNull()
        if (monto == null || monto <= 0) {
            Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show()
            etTaxMonto.error = "Inválido"
            return
        }

        if (mesStr.isEmpty()) {
            Toast.makeText(this, "Mes es requerido", Toast.LENGTH_SHORT).show()
            etTaxMes.error = "Requerido"
            return
        }
        val mes = mesStr.toIntOrNull()
        if (mes == null || mes !in 1..12) {
            Toast.makeText(this, "Mes inválido (1-12)", Toast.LENGTH_SHORT).show()
            etTaxMes.error = "Inválido"
            return
        }

        if (anoStr.isEmpty()) {
            Toast.makeText(this, "Año es requerido", Toast.LENGTH_SHORT).show()
            etTaxAno.error = "Requerido"
            return
        }
        val ano = anoStr.toIntOrNull()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        if (ano == null || ano < 2000 || ano > currentYear + 5) { // Basic year validation
            Toast.makeText(this, "Año inválido", Toast.LENGTH_SHORT).show()
            etTaxAno.error = "Inválido"
            return
        }

        val tax = MonthlyTax(
            id = currentTaxId ?: 0L,
            tipoImpuesto = tipoImpuesto,
            monto = monto,
            mes = mes,
            ano = ano,
            estadoImpuesto = estadoImpuesto
        )

        lifecycleScope.launch {
            if (currentTaxId != null) {
                monthlyTaxDao.updateTax(tax)
                Toast.makeText(this@AddEditTaxActivity, "Impuesto actualizado", Toast.LENGTH_SHORT).show()
            } else {
                monthlyTaxDao.insertTax(tax)
                Toast.makeText(this@AddEditTaxActivity, "Impuesto guardado", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}
