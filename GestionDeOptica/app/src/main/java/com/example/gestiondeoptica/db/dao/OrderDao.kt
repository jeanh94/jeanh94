package com.example.gestiondeoptica.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.gestiondeoptica.db.entity.Order
import com.example.gestiondeoptica.db.model.OrderClassificationCount

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Update
    suspend fun updateOrder(order: Order)

    @Query("SELECT * FROM orders ORDER BY fechaPedido DESC")
    fun getAllOrders(): LiveData<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderById(orderId: Long): LiveData<Order?>

    @Query("SELECT * FROM orders WHERE customerCedula = :cedula ORDER BY fechaPedido DESC")
    fun getOrdersByCustomerCedula(cedula: String): LiveData<List<Order>>

    @Query("SELECT * FROM orders WHERE estadoPedido = :status ORDER BY fechaPedido DESC")
    fun getOrdersByStatus(status: String): LiveData<List<Order>>

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrder(orderId: Long)

    @Query("SELECT clasificacionPedido, COUNT(*) as count FROM orders WHERE estadoPedido = 'Completado' GROUP BY clasificacionPedido")
    fun getCompletedOrderCountsByType(): LiveData<List<OrderClassificationCount>>
}
