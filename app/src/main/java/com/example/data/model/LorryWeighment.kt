package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QualityItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val quality: String = "",
    val quantity: Double = 0.0,
    val unit: String = "BALES"
)

@Entity(tableName = "lorry_weighments")
@JsonClass(generateAdapter = true)
data class LorryWeighment(
    @PrimaryKey
    @Json(name = "gate_pass")
    val gatePass: String = "",

    @Json(name = "date")
    val date: String = "",

    @Json(name = "type")
    val type: String = "IN",

    @Json(name = "lorry_number")
    val lorryNumber: String = "",

    @Json(name = "chalan")
    val chalan: String = "",

    @Json(name = "in_time")
    val inTime: String = "",

    @Json(name = "party")
    val party: String = "",

    @Json(name = "description")
    val description: String = "Jute",

    @Json(name = "department")
    val department: String = "",

    @Json(name = "total_quantity")
    val totalQuantity: Double = 0.0,

    @Json(name = "unit")
    val unit: String = "BALES",

    @Json(name = "gross_weight")
    val grossWeight: Double? = null,

    @Json(name = "mill_gross_weight")
    val millGrossWeight: Double? = null,

    @Json(name = "electric_gross_weight")
    val electricGrossWeight: Double? = null,

    @Json(name = "tare_weight")
    val tareWeight: Double? = null,

    @Json(name = "electric_tare_weight")
    val electricTareWeight: Double? = null,

    @Json(name = "mill_tare_weight")
    val millTareWeight: Double? = null,

    @Json(name = "net_weight")
    val netWeight: Double? = null,

    @Json(name = "mokam")
    val mokam: String = "",

    @Json(name = "marka")
    val marka: String = "",

    @Json(name = "status")
    val status: String = LorryStatus.GATE_ENTRY.name,

    @Json(name = "current_stage")
    val currentStage: String = "Main Gate",

    @Json(name = "unloaded")
    val unloaded: Boolean = false,

    @Json(name = "out_date")
    val outDate: String? = null,

    @Json(name = "out_time")
    val outTime: String? = null,

    @Json(name = "remarks")
    val remarks: String? = null,

    @Json(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @Json(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @Json(name = "is_synced")
    val isSynced: Boolean = true,

    // Serialized Quality Items (JSON list string stored in Room or converted)
    @Json(name = "quality_items_json")
    val qualityItemsJson: String = "[]"
) {
    val effectiveDepartment: String
        get() {
            if (department.isNotBlank() && department != "Select Department") {
                val d = department.trim().lowercase()
                return when {
                    d.contains("store") -> "Store"
                    d.contains("finish") -> "Finish Good"
                    d.contains("other") -> "Other"
                    d.contains("jute") -> "Jute"
                    else -> "Jute"
                }
            }
            if (!remarks.isNullOrBlank() && remarks.contains("Department:", ignoreCase = true)) {
                val deptVal = remarks.substringAfter("Department:").substringBefore("\n").substringBefore("|").trim().lowercase()
                return when {
                    deptVal.contains("store") -> "Store"
                    deptVal.contains("finish") -> "Finish Good"
                    deptVal.contains("other") -> "Other"
                    else -> "Jute"
                }
            }
            val stage = currentStage.lowercase()
            val desc = description.lowercase()
            val stat = status.lowercase()
            return when {
                stat.contains("store") || stage.contains("store") || desc.contains("store") -> "Store"
                stat.contains("finish") || stage.contains("finish") || desc.contains("finish") -> "Finish Good"
                stat.contains("other") || stage.contains("other") || desc.contains("other") -> "Other"
                else -> "Jute"
            }
        }

    val millNetWeight: Double?
        get() = if (millGrossWeight != null && millTareWeight != null) millGrossWeight - millTareWeight else null

    val electricNetWeight: Double?
        get() = if (electricGrossWeight != null && electricTareWeight != null) electricGrossWeight - electricTareWeight else null

    val hasTareRecorded: Boolean
        get() = (tareWeight != null && tareWeight > 0) || (millTareWeight != null && millTareWeight > 0) || (electricTareWeight != null && electricTareWeight > 0)

    val lowestNetWeight: Double?
        get() {
            val mNet = millNetWeight
            val eNet = electricNetWeight
            return when {
                mNet != null && eNet != null && mNet > 0 && eNet > 0 -> kotlin.math.min(mNet, eNet)
                mNet != null && mNet > 0 -> mNet
                eNet != null && eNet > 0 -> eNet
                netWeight != null && netWeight > 0 -> netWeight
                else -> null
            }
        }
}
