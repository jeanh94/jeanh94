package com.example.gestiondeoptica.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "frames")
data class Frame(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val tipoMontura: String, // e.g., "Pasta", "Metal", "Niños", "Lentes de sol"
    val cantidadStock: Int
)
