package com.example.gestiondeoptica.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.gestiondeoptica.db.entity.Lens

@Dao
interface LensDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLens(lens: Lens)

    @Update
    suspend fun updateLens(lens: Lens)

    @Query("SELECT * FROM lenses ORDER BY clasificacionCristal ASC")
    fun getAllLenses(): LiveData<List<Lens>>

    @Query("SELECT * FROM lenses WHERE id = :lensId")
    fun getLensById(lensId: Long): LiveData<Lens?>

    @Query("DELETE FROM lenses WHERE id = :lensId")
    suspend fun deleteLens(lensId: Long)

    @Query("SELECT * FROM lenses WHERE cantidadStock <= :minStockThreshold ORDER BY cantidadStock ASC")
    fun getLowStockLenses(minStockThreshold: Int): LiveData<List<Lens>>
}
