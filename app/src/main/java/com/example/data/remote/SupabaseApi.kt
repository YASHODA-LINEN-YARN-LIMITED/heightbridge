package com.example.data.model.remote

import com.example.data.model.LorryStatus
import com.example.data.model.LorryWeighment
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class BrokerResponse(
    val id: String? = null,
    val brok_name: String? = null
)

@JsonClass(generateAdapter = true)
data class AppUpdateDto(
    @Json(name = "id") val id: Int = 1,
    @Json(name = "version_code") val versionCode: Int = 1,
    @Json(name = "version_name") val versionName: String = "1.0.0",
    @Json(name = "download_url") val downloadUrl: String = "",
    @Json(name = "release_notes") val releaseNotes: String = "",
    @Json(name = "is_mandatory") val isMandatory: Boolean = false,
    @Json(name = "updated_at") val updatedAt: String? = null
)

/**
 * DTO matching the exact column names of the 'lorry_weighments' table in Supabase PostgREST:
 * id, gate_pass, entry_date, type, lorry_no, chalan_no, in_time, party_name,
 * description, quantity, unit, mokam, marka, gate_gross_weight, gate_tare_weight,
 * gate_net_weight, mill_gross_weight, mill_tare_weight, electric_gross_weight,
 * electric_tare_weight, unload_status, status, mill_remarks, out_date, out_time,
 * out_remarks, created_at, updated_at, grade, grade_details
 */
@JsonClass(generateAdapter = true)
data class LorryWeighmentDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "gate_pass") val gatePass: String? = null,
    @Json(name = "entry_date") val entryDate: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "lorry_no") val lorryNo: String? = null,
    @Json(name = "chalan_no") val chalanNo: String? = null,
    @Json(name = "in_time") val inTime: String? = null,
    @Json(name = "party_name") val partyName: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "quantity") val quantity: Any? = null,
    @Json(name = "unit") val unit: String? = null,
    @Json(name = "mokam") val mokam: String? = null,
    @Json(name = "marka") val marka: String? = null,
    @Json(name = "gate_gross_weight") val gateGrossWeight: Any? = null,
    @Json(name = "gate_tare_weight") val gateTareWeight: Any? = null,
    @Json(name = "gate_net_weight") val gateNetWeight: Any? = null,
    @Json(name = "mill_gross_weight") val millGrossWeight: Any? = null,
    @Json(name = "mill_tare_weight") val millTareWeight: Any? = null,
    @Json(name = "electric_gross_weight") val electricGrossWeight: Any? = null,
    @Json(name = "electric_tare_weight") val electricTareWeight: Any? = null,
    @Json(name = "unload_status") val unloadStatus: Any? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "mill_remarks") val millRemarks: String? = null,
    @Json(name = "out_date") val outDate: String? = null,
    @Json(name = "out_time") val outTime: String? = null,
    @Json(name = "out_remarks") val outRemarks: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "grade") val grade: String? = null,
    @Json(name = "grade_details") val gradeDetails: String? = null,
    @Json(name = "department") val department: String? = null
) {
    fun toDomain(): LorryWeighment {
        val createdMillis = parseMillis(createdAt)
        val updatedMillis = parseMillis(updatedAt)

        val qtyVal = safeDouble(quantity) ?: 0.0
        val gateGrossVal = safeDouble(gateGrossWeight)
        val gateTareVal = safeDouble(gateTareWeight)
        val gateNetVal = safeDouble(gateNetWeight)
        val millGrossVal = safeDouble(millGrossWeight)
        val millTareVal = safeDouble(millTareWeight)
        val elecGrossVal = safeDouble(electricGrossWeight)
        val elecTareVal = safeDouble(electricTareWeight)

        val bestGross = gateGrossVal ?: millGrossVal ?: elecGrossVal
        val bestTare = gateTareVal ?: millTareVal ?: elecTareVal
        val bestNet = gateNetVal ?: (if (bestGross != null && bestTare != null) bestGross - bestTare else null)

        val rawDept = department ?: (if (!millRemarks.isNullOrBlank() && millRemarks.contains("Department:")) millRemarks.substringAfter("Department:").substringBefore("\n").trim() else description ?: "Jute")
        val resolvedDept = when {
            rawDept.lowercase().contains("store") -> "Store"
            rawDept.lowercase().contains("finish") -> "Finish Good"
            rawDept.lowercase().contains("other") -> "Other"
            else -> "Jute"
        }

        var resolvedStatus = status ?: LorryStatus.GATE_ENTRY.name
        if (resolvedStatus == LorryStatus.GATE_ENTRY.name) {
            when (resolvedDept) {
                "Store" -> resolvedStatus = LorryStatus.STORE_PENDING.name
                "Finish Good" -> resolvedStatus = LorryStatus.FINISH_GOOD_PENDING.name
                "Other" -> resolvedStatus = LorryStatus.OTHER_PENDING.name
            }
        }
        val stage = LorryStatus.fromString(resolvedStatus).stageName

        val isUnloaded = when (unloadStatus) {
            is Boolean -> unloadStatus
            is String -> unloadStatus.lowercase() == "true" || unloadStatus == "1"
            is Number -> unloadStatus.toInt() == 1
            else -> false
        }

        return LorryWeighment(
            gatePass = gatePass ?: id ?: "",
            date = entryDate ?: "",
            type = type ?: "IN",
            lorryNumber = lorryNo ?: "",
            chalan = chalanNo ?: "",
            inTime = inTime ?: "",
            party = partyName ?: "",
            description = description ?: grade ?: resolvedDept,
            department = resolvedDept,
            totalQuantity = qtyVal,
            unit = unit ?: "BALES",
            grossWeight = gateGrossVal,
            millGrossWeight = millGrossVal,
            electricGrossWeight = elecGrossVal,
            tareWeight = gateTareVal,
            electricTareWeight = elecTareVal,
            millTareWeight = millTareVal,
            netWeight = bestNet,
            mokam = mokam ?: "",
            marka = marka ?: "",
            status = resolvedStatus,
            currentStage = stage,
            unloaded = isUnloaded,
            outDate = outDate,
            outTime = outTime,
            remarks = millRemarks ?: outRemarks,
            createdAt = createdMillis,
            updatedAt = updatedMillis,
            isSynced = true,
            qualityItemsJson = gradeDetails ?: "[]"
        )
    }

    private fun safeDouble(value: Any?): Double? {
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull()
            else -> null
        }
    }

    private fun parseMillis(timeObj: Any?): Long {
        if (timeObj == null) return System.currentTimeMillis()
        if (timeObj is Number) return timeObj.toLong()
        if (timeObj is String) {
            return try {
                val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                format.parse(timeObj)?.time ?: timeObj.toLong()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        }
        return System.currentTimeMillis()
    }
}

fun LorryWeighment.toDto(): LorryWeighmentDto {
    val bestTare = tareWeight ?: millTareWeight ?: electricTareWeight
    val bestGross = grossWeight ?: millGrossWeight ?: electricGrossWeight

    val millNet = if (millGrossWeight != null && millTareWeight != null) millGrossWeight - millTareWeight else null
    val elecNet = if (electricGrossWeight != null && electricTareWeight != null) electricGrossWeight - electricTareWeight else null
    val calculatedNet = when {
        millNet != null && elecNet != null && millNet > 0 && elecNet > 0 -> kotlin.math.min(millNet, elecNet)
        millNet != null && millNet > 0 -> millNet
        elecNet != null && elecNet > 0 -> elecNet
        netWeight != null && netWeight > 0 -> netWeight
        bestGross != null && bestTare != null -> bestGross - bestTare
        else -> null
    }

    val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
        timeZone = java.util.TimeZone.getTimeZone("UTC")
    }

    val createdIso = isoFormat.format(java.util.Date(if (createdAt > 0) createdAt else System.currentTimeMillis()))
    val updatedIso = isoFormat.format(java.util.Date(if (updatedAt > 0) updatedAt else System.currentTimeMillis()))

    return LorryWeighmentDto(
        id = null,
        gatePass = gatePass,
        entryDate = date,
        type = type,
        lorryNo = lorryNumber,
        chalanNo = chalan,
        inTime = inTime,
        partyName = party,
        description = description,
        quantity = totalQuantity,
        unit = unit,
        mokam = mokam,
        marka = marka,
        gateGrossWeight = grossWeight,
        gateTareWeight = tareWeight,
        gateNetWeight = calculatedNet,
        millGrossWeight = millGrossWeight,
        millTareWeight = millTareWeight,
        electricGrossWeight = electricGrossWeight,
        electricTareWeight = electricTareWeight,
        unloadStatus = unloaded.toString(),
        status = status,
        millRemarks = remarks,
        outDate = outDate,
        outTime = outTime,
        outRemarks = remarks,
        createdAt = createdIso,
        updatedAt = updatedIso,
        grade = description,
        gradeDetails = qualityItemsJson,
        department = if (department.isNotBlank()) department else if (!remarks.isNullOrEmpty() && remarks.startsWith("Department: ")) remarks.removePrefix("Department: ") else description
    )
}

interface SupabaseApi {
    @GET("lorry_weighments")
    suspend fun getLorries(
        @Query("select") select: String = "*",
        @Query("order") order: String = "created_at.desc"
    ): Response<List<LorryWeighmentDto>>

    @Headers("Prefer: resolution=merge-duplicates,return=representation")
    @POST("lorry_weighments")
    suspend fun upsertLorry(
        @Query("on_conflict") onConflict: String = "gate_pass",
        @Body lorry: LorryWeighmentDto
    ): Response<List<LorryWeighmentDto>>

    @Headers("Prefer: return=representation")
    @PATCH("lorry_weighments")
    suspend fun updateLorry(
        @Query("gate_pass") gatePassQuery: String,
        @Body lorry: LorryWeighmentDto
    ): Response<List<LorryWeighmentDto>>

    @DELETE("lorry_weighments")
    suspend fun deleteLorry(
        @Query("gate_pass") gatePassQuery: String
    ): Response<Unit>

    @GET("broker_master")
    suspend fun getBrokers(
        @Query("select") select: String = "brok_name"
    ): Response<List<BrokerResponse>>

    @GET("app_updates")
    suspend fun getAppUpdateInfo(
        @Query("select") select: String = "*",
        @Query("id") idQuery: String = "eq.1"
    ): Response<List<AppUpdateDto>>

    @Headers("Prefer: resolution=merge-duplicates")
    @POST("app_updates")
    suspend fun publishAppUpdate(
        @Body update: AppUpdateDto
    ): Response<Unit>
}
