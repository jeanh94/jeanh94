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
import com.example.gestiondeoptica.db.dao.LensDao
import com.example.gestiondeoptica.db.entity.Lens
import kotlinx.coroutines.launch

class AddEditLensActivity : AppCompatActivity() {

    private lateinit var spinnerClasificacionCristal: Spinner
    private lateinit var etCantidadStockLens: EditText
    private lateinit var btnSaveLens: Button

    private lateinit var lensDao: LensDao
    private var currentLensId: Long? = null

    companion object {
        const val EXTRA_LENS_ID = "com.example.gestiondeoptica.EXTRA_LENS_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_lens)

        spinnerClasificacionCristal = findViewById(R.id.spinner_clasificacion_cristal)
        etCantidadStockLens = findViewById(R.id.et_cantidad_stock_lens)
        btnSaveLens = findViewById(R.id.btn_save_lens)

        lensDao = AppDatabase.getDatabase(applicationContext).lensDao()

        setupSpinner()

        if (intent.hasExtra(EXTRA_LENS_ID)) {
            currentLensId = intent.getLongExtra(EXTRA_LENS_ID, -1L)
            if (currentLensId != -1L) {
                title = getString(R.string.add_edit_lens_title_edit)
                loadLensData(currentLensId!!)
            }
        } else {
            title = getString(R.string.add_edit_lens_title_add)
        }

        btnSaveLens.setOnClickListener {
            saveLens()
        }
    }

    private fun setupSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.clasificacion_pedido_array, // Using existing array as requested
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerClasificacionCristal.adapter = adapter
        }
    }

    private fun loadLensData(lensId: Long) {
        val lensLiveData: LiveData<Lens?> = lensDao.getLensById(lensId)
        lensLiveData.observe(this) { lens ->
            lens?.let {
                setSpinnerSelection(spinnerClasificacionCristal, it.clasificacionCristal, R.array.clasificacion_pedido_array)
                etCantidadStockLens.setText(it.cantidadStock.toString())
                lensLiveData.removeObservers(this) // Avoid re-triggering
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

    private fun saveLens() {
        val clasificacionCristal = spinnerClasificacionCristal.selectedItem.toString()
        val cantidadStockStr = etCantidadStockLens.text.toString().trim()

        if (cantidadStockStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.add_edit_frame_toast_stock_required), Toast.LENGTH_SHORT).show()
            etCantidadStockLens.error = getString(R.string.add_edit_frame_error_stock_required)
            return
        }

        val cantidadStock = cantidadStockStr.toIntOrNull()
        if (cantidadStock == null || cantidadStock < 0) {
            Toast.makeText(this, getString(R.string.add_edit_frame_toast_stock_invalid), Toast.LENGTH_SHORT).show()
            etCantidadStockLens.error = getString(R.string.add_edit_frame_error_stock_invalid)
            return
        }

        val lens = Lens(
            id = currentLensId ?: 0L, // If editing, use currentLensId, else 0 for new
            clasificacionCristal = clasificacionCristal,
            cantidadStock = cantidadStock
        )

        lifecycleScope.launch {
            if (currentLensId != null) {
                lensDao.updateLens(lens)
                Toast.makeText(this@AddEditLensActivity, getString(R.string.add_edit_lens_toast_updated), Toast.LENGTH_SHORT).show()
            } else {
                lensDao.insertLens(lens)
                Toast.makeText(this@AddEditLensActivity, getString(R.string.add_edit_lens_toast_saved), Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}
