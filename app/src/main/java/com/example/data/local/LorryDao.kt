package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LorryWeighment
import kotlinx.coroutines.flow.Flow

@Dao
interface LorryDao {
    @Query("SELECT * FROM lorry_weighments ORDER BY createdAt DESC")
    fun getAllLorries(): Flow<List<LorryWeighment>>

    @Query("SELECT * FROM lorry_weighments ORDER BY createdAt DESC")
    suspend fun getAllLorriesList(): List<LorryWeighment>

    @Query("SELECT * FROM lorry_weighments WHERE gatePass = :gatePass LIMIT 1")
    suspend fun getLorryByGatePass(gatePass: String): LorryWeighment?

    @Query("SELECT * FROM lorry_weighments WHERE status != 'COMPLETED' ORDER BY createdAt DESC")
    fun getPendingLorries(): Flow<List<LorryWeighment>>

    @Query("SELECT * FROM lorry_weighments WHERE status = 'COMPLETED' ORDER BY updatedAt DESC")
    fun getCompletedLorries(): Flow<List<LorryWeighment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLorry(lorry: LorryWeighment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLorries(lorries: List<LorryWeighment>)

    @Update
    suspend fun updateLorry(lorry: LorryWeighment)

    @Query("DELETE FROM lorry_weighments WHERE gatePass = :gatePass")
    suspend fun deleteLorryByGatePass(gatePass: String)

    @Query("DELETE FROM lorry_weighments")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM lorry_weighments")
    suspend fun getCount(): Int
}
