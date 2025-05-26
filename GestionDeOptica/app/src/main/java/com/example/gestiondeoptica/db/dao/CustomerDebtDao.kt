package com.example.gestiondeoptica.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.gestiondeoptica.db.entity.CustomerDebt
import com.example.gestiondeoptica.db.entity.DebtPayment

@Dao
interface CustomerDebtDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: CustomerDebt): Long

    @Update
    suspend fun updateDebt(debt: CustomerDebt)

    @Query("SELECT * FROM customer_debts WHERE customerCedula = :cedula ORDER BY fechaCreacion DESC")
    fun getDebtsByCustomer(cedula: String): LiveData<List<CustomerDebt>>

    @Query("SELECT * FROM customer_debts WHERE id = :debtId")
    fun getDebtById(debtId: Long): LiveData<CustomerDebt?>

    @Query("SELECT * FROM customer_debts WHERE estadoDeuda = 'Pendiente'")
    fun getAllPendingDebts(): LiveData<List<CustomerDebt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: DebtPayment)

    @Query("SELECT * FROM debt_payments WHERE debtId = :debtId ORDER BY fechaAbono ASC")
    fun getPaymentsForDebt(debtId: Long): LiveData<List<DebtPayment>>

    @Query("SELECT SUM(montoAbono) FROM debt_payments WHERE debtId = :debtId")
    fun getTotalPaymentsForDebt(debtId: Long): LiveData<Double?>

    @Query("SELECT SUM(d.montoTotal - COALESCE(p.totalPagado, 0.0)) " +
           "FROM customer_debts d " +
           "LEFT JOIN (SELECT debtId, SUM(montoAbono) as totalPagado FROM debt_payments GROUP BY debtId) p ON d.id = p.debtId " +
           "WHERE d.estadoDeuda = 'Pendiente'")
    fun getTotalPendingDebtAmount(): LiveData<Double?>
}
