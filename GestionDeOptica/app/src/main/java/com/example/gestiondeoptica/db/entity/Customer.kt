package com.example.gestiondeoptica.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey
    val cedula: String,
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val direccion: String,
    val odEsfera: String?,
    val odCilindro: String?,
    val odEje: String?,
    val odAdd: String?,
    val oiEsfera: String?,
    val oiCilindro: String?,
    val oiEje: String?,
    val oiAdd: String?
)
