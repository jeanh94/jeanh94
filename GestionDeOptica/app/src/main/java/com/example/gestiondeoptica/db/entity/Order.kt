package com.example.gestiondeoptica.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    foreignKeys = [ForeignKey(
        entity = Customer::class,
        parentColumns = ["cedula"],
        childColumns = ["customerCedula"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["customerCedula"])]
)
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val customerCedula: String,
    val tipoMaterialCristal: String,
    val fechaPedido: Long,
    val precio: Double,
    val estadoPedido: String,
    val clasificacionPedido: String,
    val odEsfera: String?,
    val odCilindro: String?,
    val odEje: String?,
    val odAdd: String?,
    val oiEsfera: String?,
    val oiCilindro: String?,
    val oiEje: String?,
    val oiAdd: String?
)
