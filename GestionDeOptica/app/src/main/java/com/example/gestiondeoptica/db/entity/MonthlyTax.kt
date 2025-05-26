package com.example.gestiondeoptica.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_taxes")
data class MonthlyTax(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val tipoImpuesto: String, // e.g., "IVA", "IVSS", "Alquiler Local"
    val mes: Int, // 1-12
    val ano: Int, // Year, e.g., 2023
    val monto: Double,
    val estadoImpuesto: String // e.g., "Pendiente", "Pagado"
)
