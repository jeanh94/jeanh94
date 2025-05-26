package com.example.gestiondeoptica

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestiondeoptica.db.AppDatabase
import com.example.gestiondeoptica.db.dao.CustomerDebtDao
import com.example.gestiondeoptica.db.entity.CustomerDebt
import com.example.gestiondeoptica.db.entity.DebtPayment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DebtDetailsActivity : AppCompatActivity() {

    private lateinit var tvDebtId: TextView
    private lateinit var tvCustomerCedula: TextView
    private lateinit var tvMontoTotal: TextView
    private lateinit var tvFechaCreacion: TextView
    private lateinit var tvEstadoDeuda: TextView
    private lateinit var tvSaldoPendiente: TextView
    private lateinit var rvDebtPayments: RecyclerView
    private lateinit var etAbonoMonto: EditText
    private lateinit var btnAddAbono: Button

    private lateinit var customerDebtDao: CustomerDebtDao
    private lateinit var paymentAdapter: PaymentAdapter
    private var currentDebt: CustomerDebt? = null
    private var debtId: Long = -1L
    private var totalPaid: Double = 0.0

    companion object {
        const val EXTRA_DEBT_ID = "com.example.gestiondeoptica.EXTRA_DEBT_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debt_details)

        title = getString(R.string.debt_details_screen_title)

        tvDebtId = findViewById(R.id.tv_details_debt_id)
        tvCustomerCedula = findViewById(R.id.tv_details_customer_cedula)
        tvMontoTotal = findViewById(R.id.tv_details_monto_total)
        tvFechaCreacion = findViewById(R.id.tv_details_fecha_creacion)
        tvEstadoDeuda = findViewById(R.id.tv_details_estado_deuda)
        tvSaldoPendiente = findViewById(R.id.tv_details_saldo_pendiente)
        rvDebtPayments = findViewById(R.id.rv_debt_payments)
        etAbonoMonto = findViewById(R.id.et_abono_monto)
        btnAddAbono = findViewById(R.id.btn_add_abono)

        customerDebtDao = AppDatabase.getDatabase(applicationContext).customerDebtDao()
        setupRecyclerView()

        debtId = intent.getLongExtra(EXTRA_DEBT_ID, -1L)
        if (debtId == -1L) {
            Toast.makeText(this, getString(R.string.debt_details_error_debt_id_not_found), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        loadDebtDetails()
        loadPayments()
        observeTotalPayments()

        btnAddAbono.setOnClickListener {
            addAbono()
        }
    }

    private fun setupRecyclerView() {
        paymentAdapter = PaymentAdapter()
        rvDebtPayments.adapter = paymentAdapter
        rvDebtPayments.layoutManager = LinearLayoutManager(this)
    }

    private fun loadDebtDetails() {
        customerDebtDao.getDebtById(debtId).observe(this, Observer { debt ->
            currentDebt = debt
            debt?.let {
                tvDebtId.text = getString(R.string.debt_details_label_debt_id_prefix) + it.id
                tvCustomerCedula.text = getString(R.string.debt_details_label_customer_cedula_prefix) + it.customerCedula
                tvMontoTotal.text = getString(R.string.debt_details_label_total_amount_prefix) + "$${String.format(Locale.US, "%.2f", it.montoTotal)}"
                tvFechaCreacion.text = getString(R.string.debt_details_label_creation_date_prefix) + SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it.fechaCreacion))
                tvEstadoDeuda.text = getString(R.string.debt_details_label_status_prefix) + it.estadoDeuda

                // Style tvEstadoDeuda
                if (it.estadoDeuda.equals("Pagada", ignoreCase = true)) {
                    tvEstadoDeuda.setTextColor(resources.getColor(android.R.color.holo_green_dark, theme))
                } else {
                    tvEstadoDeuda.setTextColor(resources.getColor(android.R.color.holo_red_dark, theme))
                }

                updateSaldoPendiente() // Update saldo when debt details or payments change
                // Disable payment input if debt is paid
                if (it.estadoDeuda.equals("Pagada", ignoreCase = true)) {
                    etAbonoMonto.isEnabled = false
                    btnAddAbono.isEnabled = false
                    etAbonoMonto.hint = getString(R.string.debt_details_hint_debt_paid)
                } else {
                    etAbonoMonto.isEnabled = true
                    btnAddAbono.isEnabled = true
                    etAbonoMonto.hint = getString(R.string.debt_details_hint_payment_amount)
                }
            }
        })
    }

    private fun loadPayments() {
        customerDebtDao.getPaymentsForDebt(debtId).observe(this, Observer { payments ->
            paymentAdapter.submitList(payments)
            // Total paid is observed separately to simplify LiveData handling here
        })
    }

    private fun observeTotalPayments() {
        customerDebtDao.getTotalPaymentsForDebt(debtId).observe(this, Observer { paidAmount ->
            totalPaid = paidAmount ?: 0.0
            updateSaldoPendiente()
        })
    }

    private fun updateSaldoPendiente() {
        currentDebt?.let {
            val saldo = it.montoTotal - totalPaid
            tvSaldoPendiente.text = getString(R.string.debt_details_label_pending_balance_prefix) + "$${String.format(Locale.US, "%.2f", saldo)}"
            if (saldo <= 0 && it.estadoDeuda == "Pendiente") {
                tvSaldoPendiente.setTextColor(resources.getColor(android.R.color.holo_green_dark, theme))
            } else if (it.estadoDeuda == "Pagada") {
                 tvSaldoPendiente.setTextColor(resources.getColor(android.R.color.holo_green_dark, theme))
            }
            else {
                tvSaldoPendiente.setTextColor(resources.getColor(android.R.color.holo_red_dark, theme))
            }
        }
    }

    private fun addAbono() {
        val montoAbonoStr = etAbonoMonto.text.toString().trim()
        if (montoAbonoStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.debt_details_toast_enter_payment_amount), Toast.LENGTH_SHORT).show()
            etAbonoMonto.error = getString(R.string.debt_details_error_payment_amount_required)
            return
        }

        val montoAbono = montoAbonoStr.toDoubleOrNull()
        if (montoAbono == null || montoAbono <= 0) {
            Toast.makeText(this, getString(R.string.debt_details_toast_invalid_payment_amount), Toast.LENGTH_SHORT).show()
            etAbonoMonto.error = getString(R.string.debt_details_error_invalid_payment_amount)
            return
        }

        currentDebt?.let { debt ->
            if (montoAbono > (debt.montoTotal - totalPaid)) {
                Toast.makeText(this, getString(R.string.debt_details_toast_payment_exceeds_balance), Toast.LENGTH_LONG).show()
                etAbonoMonto.error = getString(R.string.debt_details_error_payment_exceeds_balance)
                return
            }

            val newPayment = DebtPayment(
                debtId = debt.id,
                fechaAbono = System.currentTimeMillis(),
                montoAbono = montoAbono
            )

            lifecycleScope.launch {
                customerDebtDao.insertPayment(newPayment)
                etAbonoMonto.text.clear() // Clear input field
                Toast.makeText(this@DebtDetailsActivity, getString(R.string.debt_details_toast_payment_registered), Toast.LENGTH_SHORT).show()

                // Recalculate total paid after new payment (LiveData will trigger updateSaldoPendiente)
                // Check if debt is fully paid
                val newTotalPaid = totalPaid + montoAbono // Simulate new total for check
                if (debt.montoTotal - newTotalPaid <= 0) {
                    if (debt.estadoDeuda == "Pendiente") {
                        val updatedDebt = debt.copy(estadoDeuda = "Pagada")
                        customerDebtDao.updateDebt(updatedDebt)
                        // UI for disabling payment fields will be updated by the LiveData observer on loadDebtDetails
                    }
                }
            }
        } ?: run {
            Toast.makeText(this, getString(R.string.debt_details_toast_error_loading_debt), Toast.LENGTH_SHORT).show()
        }
    }
}
