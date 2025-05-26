package com.example.gestiondeoptica.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customer_debts",
    foreignKeys = [ForeignKey(
        entity = Customer::class,
        parentColumns = ["cedula"],
        childColumns = ["customerCedula"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["customerCedula"])]
)
data class CustomerDebt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val customerCedula: String,
    val montoTotal: Double,
    val fechaCreacion: Long, // Timestamp
    val estadoDeuda: String // e.g., "Pendiente", "Pagada"
)
