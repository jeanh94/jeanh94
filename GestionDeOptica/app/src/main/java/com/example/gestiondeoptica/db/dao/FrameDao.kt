package com.example.gestiondeoptica.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.gestiondeoptica.db.entity.Frame

@Dao
interface FrameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFrame(frame: Frame)

    @Update
    suspend fun updateFrame(frame: Frame)

    @Query("SELECT * FROM frames ORDER BY tipoMontura ASC")
    fun getAllFrames(): LiveData<List<Frame>>

    @Query("SELECT * FROM frames WHERE id = :frameId")
    fun getFrameById(frameId: Long): LiveData<Frame?>

    @Query("DELETE FROM frames WHERE id = :frameId")
    suspend fun deleteFrame(frameId: Long)

    @Query("SELECT * FROM frames WHERE cantidadStock <= :minStockThreshold ORDER BY cantidadStock ASC")
    fun getLowStockFrames(minStockThreshold: Int): LiveData<List<Frame>>
}
