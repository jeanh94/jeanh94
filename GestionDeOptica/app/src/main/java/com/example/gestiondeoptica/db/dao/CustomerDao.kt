package com.example.gestiondeoptica.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.gestiondeoptica.db.entity.Customer

@Dao
interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("SELECT * FROM customers ORDER BY apellido, nombre ASC")
    fun getAllCustomers(): LiveData<List<Customer>>

    @Query("SELECT * FROM customers WHERE cedula = :cedula")
    fun getCustomerByCedula(cedula: String): LiveData<Customer?>

    @Query("DELETE FROM customers WHERE cedula = :cedula")
    suspend fun deleteCustomer(cedula: String)
}
