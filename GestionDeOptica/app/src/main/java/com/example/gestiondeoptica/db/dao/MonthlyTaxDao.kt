package com.example.gestiondeoptica.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.gestiondeoptica.db.entity.MonthlyTax

@Dao
interface MonthlyTaxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTax(tax: MonthlyTax)

    @Update
    suspend fun updateTax(tax: MonthlyTax)

    @Query("SELECT * FROM monthly_taxes ORDER BY ano DESC, mes DESC")
    fun getAllTaxes(): LiveData<List<MonthlyTax>>

    @Query("SELECT * FROM monthly_taxes WHERE id = :taxId")
    fun getTaxById(taxId: Long): LiveData<MonthlyTax?>

    @Query("SELECT * FROM monthly_taxes WHERE estadoImpuesto = 'Pendiente' ORDER BY ano DESC, mes DESC")
    fun getAllPendingTaxes(): LiveData<List<MonthlyTax>>
}
