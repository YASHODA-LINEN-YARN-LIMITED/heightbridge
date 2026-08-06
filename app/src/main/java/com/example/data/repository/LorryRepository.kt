package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.LorryDao
import com.example.data.model.LorryWeighment
import com.example.data.model.MasterDataLists
import com.example.data.model.remote.AppUpdateDto
import com.example.data.model.remote.toDto
import com.example.data.remote.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LorryRepository(context: Context) {
    private val lorryDao: LorryDao = AppDatabase.getDatabase(context).lorryDao()
    private val supabaseApi = SupabaseClient.api

    val allLorries: Flow<List<LorryWeighment>> = lorryDao.getAllLorries()
    val pendingLorries: Flow<List<LorryWeighment>> = lorryDao.getPendingLorries()
    val completedLorries: Flow<List<LorryWeighment>> = lorryDao.getCompletedLorries()

    suspend fun allLorriesList(): List<LorryWeighment> = withContext(Dispatchers.IO) {
        lorryDao.getAllLorriesList()
    }

    private suspend fun syncToSupabase(lorry: LorryWeighment): Boolean {
        return try {
            val dto = lorry.toDto()
            val resp = supabaseApi.upsertLorry(lorry = dto)
            if (resp.isSuccessful) {
                true
            } else {
                Log.w("LorryRepository", "Upsert returned HTTP ${resp.code()}, trying PATCH update fallback...")
                val updateResp = supabaseApi.updateLorry(gatePassQuery = "eq.${lorry.gatePass}", lorry = dto)
                if (updateResp.isSuccessful) {
                    true
                } else {
                    Log.e("LorryRepository", "Supabase sync error: Upsert HTTP ${resp.code()} / Patch HTTP ${updateResp.code()} - ${resp.errorBody()?.string()}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("LorryRepository", "Supabase sync exception for ${lorry.gatePass}: ${e.localizedMessage}")
            false
        }
    }

    suspend fun pushLocalToRemote() = withContext(Dispatchers.IO) {
        try {
            val localList = lorryDao.getAllLorriesList()
            for (lorry in localList) {
                if (!lorry.isSynced) {
                    val synced = syncToSupabase(lorry)
                    if (synced) {
                        lorryDao.insertLorry(lorry.copy(isSynced = true))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LorryRepository", "Failed to query local lorries: ${e.localizedMessage}")
        }
    }

    suspend fun refreshFromRemote(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // First push any local unsynced entries to Supabase
            pushLocalToRemote()

            val response = supabaseApi.getLorries()
            if (response.isSuccessful) {
                val dtos = response.body() ?: emptyList()
                val remoteLorries = dtos.map { it.toDomain() }.filter { it.gatePass.isNotBlank() }
                if (remoteLorries.isNotEmpty()) {
                    lorryDao.insertLorries(remoteLorries)
                }

                // Remove local entries that were deleted on Supabase to keep all devices 100% in sync
                val remoteGatePasses = remoteLorries.map { it.gatePass }.toSet()
                val localLorries = lorryDao.getAllLorriesList()
                for (local in localLorries) {
                    if (local.isSynced && !remoteGatePasses.contains(local.gatePass)) {
                        lorryDao.deleteLorryByGatePass(local.gatePass)
                    }
                }

                Result.success(Unit)
            } else {
                Log.e("LorryRepository", "Failed to fetch from Supabase: ${response.code()} ${response.message()}")
                Result.failure(Exception("Supabase HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("LorryRepository", "Error syncing with Supabase: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun saveGateEntry(lorry: LorryWeighment): Result<LorryWeighment> = withContext(Dispatchers.IO) {
        try {
            // Save locally first
            lorryDao.insertLorry(lorry)
            // Push to Supabase immediately
            val synced = syncToSupabase(lorry)
            val finalLorry = lorry.copy(isSynced = synced)
            if (synced) {
                lorryDao.insertLorry(finalLorry)
            }
            Result.success(finalLorry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLorry(lorry: LorryWeighment): Result<LorryWeighment> = withContext(Dispatchers.IO) {
        try {
            val updated = lorry.copy(updatedAt = System.currentTimeMillis())
            lorryDao.insertLorry(updated)
            val synced = syncToSupabase(updated)
            val finalLorry = updated.copy(isSynced = synced)
            if (synced) {
                lorryDao.insertLorry(finalLorry)
            }
            Result.success(finalLorry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteLorry(gatePass: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            lorryDao.deleteLorryByGatePass(gatePass)
            try {
                supabaseApi.deleteLorry("eq.$gatePass")
            } catch (e: Exception) {
                Log.e("LorryRepository", "Supabase delete deferred: ${e.localizedMessage}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchBrokers(): List<String> = withContext(Dispatchers.IO) {
        try {
            val response = supabaseApi.getBrokers()
            if (response.isSuccessful) {
                val brokers = response.body()?.mapNotNull { it.brok_name?.trim() }?.filter { it.isNotEmpty() }
                if (!brokers.isNullOrEmpty()) {
                    return@withContext brokers.distinct().sorted()
                }
            }
        } catch (e: Exception) {
            Log.e("LorryRepository", "Error fetching brokers from Supabase: ${e.localizedMessage}")
        }
        MasterDataLists.DEFAULT_BROKERS
    }

    suspend fun cleanUpDummyData() = withContext(Dispatchers.IO) {
        val dummyPasses = listOf("GP-20260801-00124", "GP-20260801-00123", "GP-20260801-00122", "GP-20260731-00098")
        for (pass in dummyPasses) {
            lorryDao.deleteLorryByGatePass(pass)
        }
    }

    suspend fun clearAllLocalData() = withContext(Dispatchers.IO) {
        lorryDao.deleteAll()
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        cleanUpDummyData()
    }

    suspend fun getAppUpdateInfo(): AppUpdateDto? = withContext(Dispatchers.IO) {
        try {
            val response = supabaseApi.getAppUpdateInfo()
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                return@withContext response.body()?.firstOrNull()
            }
        } catch (e: Exception) {
            Log.e("LorryRepository", "Error fetching app update info from Supabase: ${e.localizedMessage}")
        }
        null
    }

    suspend fun publishAppUpdate(update: AppUpdateDto): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = supabaseApi.publishAppUpdate(update)
            return@withContext response.isSuccessful
        } catch (e: Exception) {
            Log.e("LorryRepository", "Error publishing app update to Supabase: ${e.localizedMessage}")
            return@withContext false
        }
    }
}
