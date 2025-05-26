package com.example.gestiondeoptica.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "debt_payments",
    foreignKeys = [ForeignKey(
        entity = CustomerDebt::class,
        parentColumns = ["id"],
        childColumns = ["debtId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["debtId"])]
)
data class DebtPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val debtId: Long,
    val fechaAbono: Long, // Timestamp
    val montoAbono: Double
)
