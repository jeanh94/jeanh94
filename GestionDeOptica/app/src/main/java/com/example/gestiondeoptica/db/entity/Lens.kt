package com.example.gestiondeoptica.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lenses")
data class Lens(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val clasificacionCristal: String, // e.g., "Visión Sencilla", "Bifocal", "Progresivo"
    val cantidadStock: Int
)
