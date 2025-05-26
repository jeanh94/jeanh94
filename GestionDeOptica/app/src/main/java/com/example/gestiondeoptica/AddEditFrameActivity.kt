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
import com.example.gestiondeoptica.db.dao.FrameDao
import com.example.gestiondeoptica.db.entity.Frame
import kotlinx.coroutines.launch

class AddEditFrameActivity : AppCompatActivity() {

    private lateinit var spinnerTipoMontura: Spinner
    private lateinit var etCantidadStockFrame: EditText
    private lateinit var btnSaveFrame: Button

    private lateinit var frameDao: FrameDao
    private var currentFrameId: Long? = null

    companion object {
        const val EXTRA_FRAME_ID = "com.example.gestiondeoptica.EXTRA_FRAME_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_frame)

        spinnerTipoMontura = findViewById(R.id.spinner_tipo_montura)
        etCantidadStockFrame = findViewById(R.id.et_cantidad_stock_frame)
        btnSaveFrame = findViewById(R.id.btn_save_frame)

        frameDao = AppDatabase.getDatabase(applicationContext).frameDao()

        setupSpinner()

        if (intent.hasExtra(EXTRA_FRAME_ID)) {
            currentFrameId = intent.getLongExtra(EXTRA_FRAME_ID, -1L)
            if (currentFrameId != -1L) {
                title = "Editar Montura"
                loadFrameData(currentFrameId!!)
            }
        } else {
            title = "Agregar Montura Nueva"
        }

        btnSaveFrame.setOnClickListener {
            saveFrame()
        }
    }

    private fun setupSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.tipo_montura_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTipoMontura.adapter = adapter
        }
    }

    private fun loadFrameData(frameId: Long) {
        val frameLiveData: LiveData<Frame?> = frameDao.getFrameById(frameId)
        frameLiveData.observe(this) { frame ->
            frame?.let {
                setSpinnerSelection(spinnerTipoMontura, it.tipoMontura, R.array.tipo_montura_array)
                etCantidadStockFrame.setText(it.cantidadStock.toString())
                frameLiveData.removeObservers(this) // Avoid re-triggering
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

    private fun saveFrame() {
        val tipoMontura = spinnerTipoMontura.selectedItem.toString()
        val cantidadStockStr = etCantidadStockFrame.text.toString().trim()

        if (cantidadStockStr.isEmpty()) {
            Toast.makeText(this, "Cantidad en stock es requerida", Toast.LENGTH_SHORT).show()
            etCantidadStockFrame.error = "Cantidad requerida"
            return
        }

        val cantidadStock = cantidadStockStr.toIntOrNull()
        if (cantidadStock == null || cantidadStock < 0) {
            Toast.makeText(this, "Cantidad en stock inválida", Toast.LENGTH_SHORT).show()
            etCantidadStockFrame.error = "Cantidad inválida"
            return
        }

        val frame = Frame(
            id = currentFrameId ?: 0L, // If editing, use currentFrameId, else 0 for new
            tipoMontura = tipoMontura,
            cantidadStock = cantidadStock
        )

        lifecycleScope.launch {
            if (currentFrameId != null) {
                frameDao.updateFrame(frame)
                Toast.makeText(this@AddEditFrameActivity, "Montura actualizada", Toast.LENGTH_SHORT).show()
            } else {
                frameDao.insertFrame(frame)
                Toast.makeText(this@AddEditFrameActivity, "Montura guardada", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}
