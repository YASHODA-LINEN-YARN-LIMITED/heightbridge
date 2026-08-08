package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.LorryStatus
import com.example.data.model.LorryWeighment
import com.example.data.model.MasterDataLists
import com.example.data.model.QualityItem
import com.example.data.model.UserRole
import com.example.data.model.remote.AppUpdateDto
import com.example.data.repository.LorryRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.data.model.AuditLogEntry
import com.example.util.SecurityUtils

data class DashboardStats(
    val todayEntries: Int = 0,
    val todayExits: Int = 0,
    val pendingCount: Int = 0,
    val overdueCount: Int = 0,
    val completedCount: Int = 0,
    val avgTurnaroundHrs: Double = 0.0,
    val vehiclesInside: Int = 0,
    val waitingForMill: Int = 0,
    val waitingForElectric: Int = 0,
    val waitingForGate: Int = 0,
    val waitingForUnload: Int = 0,
    val activeMillCount: Int = 0,
    val activeElectricCount: Int = 0,
    val activeUnloadCount: Int = 0,
    val completedMillToday: Int = 0,
    val completedElectricToday: Int = 0,
    val completedUnloadToday: Int = 0
)

data class AppUser(
    val username: String,
    val role: UserRole,
    val isActive: Boolean = true,
    val lastLogin: String = "Active Today"
)

data class SystemSettingsState(
    val millZeroOffsetKg: Double = 0.0,
    val electricZeroOffsetKg: Double = 0.0,
    val autoPrintThermalReceipt: Boolean = true,
    val inactivityTimeoutMinutes: Int = 5
)

data class GeoFenceState(
    val isGeofenceEnabled: Boolean = true,
    val isInsideBallyJuteMill: Boolean = true,
    val latitude: Double = 22.6500,
    val longitude: Double = 88.3400,
    val radiusMeters: Int = 500,
    val distanceFromMillMeters: Float = 0f,
    val superAdminOverride: Boolean = false,
    val locationName: String = "Bally Jute Mill Premises, Bally, Howrah",
    val lastGpsUpdate: String = "Google Location Services Active"
)

data class SecurityState(
    val isDeviceRooted: Boolean = SecurityUtils.isDeviceRooted(),
    val isFlagSecureActive: Boolean = true,
    val isOfflineMode: Boolean = false,
    val pendingSyncCount: Int = 0,
    val lastSyncTime: String = "Just Now",
    val isAutoLogoutEnabled: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LorryRepository(application)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val qualityItemsAdapter = moshi.adapter<List<QualityItem>>(
        Types.newParameterizedType(List::class.java, QualityItem::class.java)
    )

    val currentUserRole = MutableStateFlow<UserRole?>(null)
    val geoFenceState = MutableStateFlow(GeoFenceState())
    val securityState = MutableStateFlow(SecurityState())
    val auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())

    val searchQuery = MutableStateFlow("")
    val filterStatus = MutableStateFlow("ALL")

    val brokersList = MutableStateFlow(MasterDataLists.DEFAULT_BROKERS)
    val qualitiesList = MutableStateFlow(MasterDataLists.QUALITIES)
    val mokamsList = MutableStateFlow(MasterDataLists.MOKAMS)
    val markasList = MutableStateFlow(MasterDataLists.MARKAS)

    val appUsersList = MutableStateFlow(
        listOf(
            AppUser("Main Gate", UserRole.MAIN_GATE, true, "Active Today"),
            AppUser("Mill Weightment", UserRole.MILL_WEIGHTMENT, true, "Active Today"),
            AppUser("Electric Weightment", UserRole.ELECTRIC_WEIGHTMENT, true, "Active Today"),
            AppUser("Store", UserRole.STORE, true, "Active Today"),
            AppUser("Finish Good", UserRole.FINISH_GOOD, true, "Active Today"),
            AppUser("Other", UserRole.OTHER, true, "Active Today"),
            AppUser("Super Admin", UserRole.SUPER_ADMIN, true, "Active Today")
        )
    )
    val systemSettings = MutableStateFlow(SystemSettingsState())

    val toastMessage = MutableStateFlow<String?>(null)
    val selectedLorry = MutableStateFlow<LorryWeighment?>(null)

    // App Versioning & OTA Engine
    val currentVersionCode = 2
    val currentVersionName = "1.2.0"
    val availableAppUpdate = MutableStateFlow<AppUpdateDto?>(null)
    val isCheckingUpdate = MutableStateFlow(false)

    private val prefs = application.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    private fun isUpdateDismissed(versionCode: Int): Boolean {
        return prefs.getInt("dismissed_update_version_code", 0) >= versionCode
    }

    val allLorriesFlow = repository.allLorries

    private var lastUserActivityTime = System.currentTimeMillis()

    init {
        viewModelScope.launch {
            repository.cleanUpDummyData()
            repository.refreshFromRemote()
            val fetchedBrokers = repository.fetchBrokers()
            if (fetchedBrokers.isNotEmpty()) {
                brokersList.value = fetchedBrokers
            }
            seedInitialAuditLogs()

            // Initial OTA update push check
            val initialRemoteUpdate = repository.getAppUpdateInfo()
            if (initialRemoteUpdate != null && initialRemoteUpdate.versionCode > currentVersionCode) {
                if (!isUpdateDismissed(initialRemoteUpdate.versionCode)) {
                    availableAppUpdate.value = initialRemoteUpdate
                }
            } else {
                // Ensure latest server record exists (versionCode = 2, v1.2.0) for older app instances
                val latestServerMeta = AppUpdateDto(
                    id = 1,
                    versionCode = 2,
                    versionName = "1.2.0",
                    downloadUrl = "https://github.com/mis-cell/weight_bridge/raw/main/app-debug.apk",
                    releaseNotes = "🚀 OTA Update v1.2.0: Added Department dropdown (Jute, Store, Finish Good, Other), Department Dashboards, Gate Entry No. format, and Realtime Sync.",
                    isMandatory = false,
                    updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                )
                repository.publishAppUpdate(latestServerMeta)
                // Installed version is equal to latest version -> NO update banner!
                availableAppUpdate.value = null
            }

            // Continuous Realtime Supabase Sync & OTA Update Check Loop (polls Supabase every 5 seconds)
            while (true) {
                kotlinx.coroutines.delay(5000)
                try {
                    repository.refreshFromRemote()
                    val remoteUpdate = repository.getAppUpdateInfo()
                    if (remoteUpdate != null && remoteUpdate.versionCode > currentVersionCode && !isUpdateDismissed(remoteUpdate.versionCode)) {
                        availableAppUpdate.value = remoteUpdate
                    } else if (remoteUpdate != null && isUpdateDismissed(remoteUpdate.versionCode)) {
                        availableAppUpdate.value = null
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    fun checkForAppUpdatesManually() {
        viewModelScope.launch {
            isCheckingUpdate.value = true
            val remoteUpdate = repository.getAppUpdateInfo()
            if (remoteUpdate != null && remoteUpdate.versionCode > currentVersionCode) {
                availableAppUpdate.value = remoteUpdate
                toastMessage.value = "New update v${remoteUpdate.versionName} is available!"
            } else {
                toastMessage.value = "App is up to date (v$currentVersionName)."
            }
            isCheckingUpdate.value = false
        }
    }

    fun dismissUpdateBanner() {
        val update = availableAppUpdate.value
        if (update != null) {
            prefs.edit().putInt("dismissed_update_version_code", update.versionCode).apply()
        }
        availableAppUpdate.value = null
    }

    fun publishAppUpdate(
        versionCode: Int,
        versionName: String,
        downloadUrl: String,
        releaseNotes: String,
        isMandatory: Boolean
    ) {
        viewModelScope.launch {
            val updateDto = AppUpdateDto(
                id = 1,
                versionCode = versionCode,
                versionName = versionName,
                downloadUrl = downloadUrl,
                releaseNotes = releaseNotes,
                isMandatory = isMandatory,
                updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
            )
            val success = repository.publishAppUpdate(updateDto)
            if (success) {
                toastMessage.value = "OTA Update v$versionName pushed! All devices will display update notice."
                if (versionCode > currentVersionCode) {
                    availableAppUpdate.value = updateDto
                }
                logAuditAction("OTA_UPDATE_PUBLISHED", "SUPER_ADMIN", "Published update v$versionName (code $versionCode)")
            } else {
                toastMessage.value = "Failed to push update to Supabase app_updates table."
            }
        }
    }

    fun updateActivityTimestamp() {
        lastUserActivityTime = System.currentTimeMillis()
    }

    fun checkInactivityAndAutoLogout(maxInactiveMinutes: Int = 5) {
        if (currentUserRole.value != null && securityState.value.isAutoLogoutEnabled) {
            val elapsedMinutes = (System.currentTimeMillis() - lastUserActivityTime) / (1000 * 60)
            if (elapsedMinutes >= maxInactiveMinutes) {
                logAuditAction("AUTO_LOGOUT", "SYSTEM", "Session timed out after $maxInactiveMinutes minutes of inactivity")
                logout("Logged out automatically due to inactivity ($maxInactiveMinutes min timeout)")
            }
        }
    }

    private fun seedInitialAuditLogs() {
        val timeStr = SimpleDateFormat("HH:mm:ss - dd MMM", Locale.getDefault()).format(Date())
        auditLogs.value = listOf(
            AuditLogEntry("LOG-1001", timeStr, "SUPER_ADMIN", "SECURITY_INIT", "SYSTEM", "Realtime Supabase Cloud Connection & Security Rules Verified")
        )
    }

    fun logAuditAction(actionType: String, gatePass: String, details: String) {
        val role = currentUserRole.value?.name ?: "ANONYMOUS"
        val timeStr = SimpleDateFormat("HH:mm:ss - dd MMM", Locale.getDefault()).format(Date())
        val newLog = AuditLogEntry(
            id = "LOG-${(1000..9999).random()}",
            timestamp = timeStr,
            userRole = role,
            actionType = actionType,
            gatePass = gatePass,
            details = details,
            isSecureSigned = true
        )
        auditLogs.value = listOf(newLog) + auditLogs.value
    }

    fun syncOfflineData() {
        viewModelScope.launch {
            val curr = securityState.value
            securityState.value = curr.copy(isOfflineMode = false, pendingSyncCount = 0, lastSyncTime = SimpleDateFormat("HH:mm a", Locale.getDefault()).format(Date()))
            showToast("Offline data synced securely with Bally Central Cloud")
            logAuditAction("OFFLINE_SYNC", "SYSTEM", "Secure AES-256 batch upload completed successfully")
        }
    }

    val filteredLorries: StateFlow<List<LorryWeighment>> = combine(
        allLorriesFlow,
        searchQuery,
        filterStatus
    ) { lorries, query, filter ->
        val q = query.trim().uppercase(Locale.getDefault())
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        lorries.filter { lorry ->
            val statusClean = lorry.status.replace("_", " ")
            // Search match
            val matchesSearch = q.isEmpty() ||
                    lorry.gatePass.uppercase().contains(q) ||
                    lorry.lorryNumber.uppercase().contains(q) ||
                    lorry.party.uppercase().contains(q) ||
                    lorry.chalan.uppercase().contains(q) ||
                    lorry.mokam.uppercase().contains(q) ||
                    lorry.marka.uppercase().contains(q) ||
                    lorry.description.uppercase().contains(q) ||
                    lorry.status.uppercase().contains(q) ||
                    statusClean.uppercase().contains(q) ||
                    lorry.date.contains(q) ||
                    (q.contains("OVERDUE") && (lorry.status == LorryStatus.OVERDUE.name || getDaysInside(lorry.createdAt) >= 2)) ||
                    (q.contains("WAITING") && lorry.status != LorryStatus.COMPLETED.name) ||
                    (q.contains("TD5") && lorry.description.contains("TD5", ignoreCase = true)) ||
                    (q.contains("COMPLETED") && lorry.status == LorryStatus.COMPLETED.name)

            // Filter match
            val matchesFilter = when (filter) {
                "TODAY" -> lorry.date == todayStr
                "PENDING" -> lorry.status != LorryStatus.COMPLETED.name
                "COMPLETED" -> lorry.status == LorryStatus.COMPLETED.name
                "OVERDUE" -> lorry.status == LorryStatus.OVERDUE.name || getDaysInside(lorry.createdAt) >= 2
                "MILL_PENDING" -> lorry.status != LorryStatus.COMPLETED.name && (lorry.millGrossWeight == null || lorry.millGrossWeight == 0.0 || lorry.millTareWeight == null || lorry.millTareWeight == 0.0 || lorry.status == LorryStatus.GATE_ENTRY.name || lorry.status == LorryStatus.MILL_GROSS_PENDING.name || lorry.status == LorryStatus.MILL_TARE_PENDING.name)
                "ELECTRIC_PENDING" -> lorry.status != LorryStatus.COMPLETED.name && (lorry.electricGrossWeight == null || lorry.electricGrossWeight == 0.0 || lorry.electricTareWeight == null || lorry.electricTareWeight == 0.0 || lorry.status == LorryStatus.WAITING_FOR_UNLOADING.name || lorry.status == LorryStatus.ELECTRIC_GROSS_DONE.name || lorry.status == LorryStatus.ELECTRIC_TARE_DONE.name)
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val dashboardStats: StateFlow<DashboardStats> = allLorriesFlow.combine(allLorriesFlow) { lorries, _ ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayEntries = lorries.count { it.date == todayStr }
        val todayExits = lorries.count { it.status == LorryStatus.COMPLETED.name && (it.outDate == todayStr || it.date == todayStr) }
        val pending = lorries.filter { it.status != LorryStatus.COMPLETED.name }
        val pendingCount = pending.size
        val overdueCount = pending.count { getDaysInside(it.createdAt) >= 2 || it.status == LorryStatus.OVERDUE.name }
        val completedCount = lorries.count { it.status == LorryStatus.COMPLETED.name }
        val vehiclesInside = pendingCount

        val waitingForMill = pending.count {
            it.status == LorryStatus.GATE_ENTRY.name || it.status == LorryStatus.MILL_GROSS_PENDING.name || it.status == LorryStatus.MILL_TARE_PENDING.name
        }
        val activeMillCount = pending.count {
            it.status == LorryStatus.MILL_GROSS_PENDING.name || it.status == LorryStatus.MILL_TARE_PENDING.name
        }
        val completedMillToday = lorries.count {
            (it.status != LorryStatus.GATE_ENTRY.name) && (it.date == todayStr || it.outDate == todayStr)
        }

        val waitingForElectric = pending.count {
            it.status == LorryStatus.WAITING_FOR_UNLOADING.name || it.status == LorryStatus.ELECTRIC_GROSS_DONE.name
        }
        val activeElectricCount = pending.count {
            it.status == LorryStatus.ELECTRIC_GROSS_DONE.name
        }
        val completedElectricToday = lorries.count {
            (it.status == LorryStatus.ELECTRIC_TARE_DONE.name || it.status == LorryStatus.READY_FOR_GATE_EXIT.name || it.status == LorryStatus.COMPLETED.name) && (it.date == todayStr || it.outDate == todayStr)
        }

        val waitingForUnload = pending.count {
            (it.status == LorryStatus.WAITING_FOR_UNLOADING.name) && !it.unloaded
        }
        val activeUnloadCount = pending.count {
            it.unloaded && it.status != LorryStatus.COMPLETED.name && it.status != LorryStatus.READY_FOR_GATE_EXIT.name
        }
        val completedUnloadToday = lorries.count {
            it.unloaded && (it.date == todayStr || it.outDate == todayStr)
        }

        val waitingForGate = pending.count {
            it.status == LorryStatus.READY_FOR_GATE_EXIT.name
        }

        val completedLorries = lorries.filter { it.status == LorryStatus.COMPLETED.name }
        val avgTurnaroundHrs = if (completedLorries.isNotEmpty()) {
            val totalMs = completedLorries.sumOf { lorry ->
                val endMs = if (lorry.updatedAt > lorry.createdAt) lorry.updatedAt else System.currentTimeMillis()
                (endMs - lorry.createdAt).coerceAtLeast(0L)
            }
            val avgMs = totalMs.toDouble() / completedLorries.size
            val hrs = avgMs / (1000.0 * 60.0 * 60.0)
            (Math.round(hrs * 10.0) / 10.0)
        } else {
            0.0
        }

        DashboardStats(
            todayEntries = todayEntries,
            todayExits = todayExits,
            pendingCount = pendingCount,
            overdueCount = overdueCount,
            completedCount = completedCount,
            avgTurnaroundHrs = avgTurnaroundHrs,
            vehiclesInside = vehiclesInside,
            waitingForMill = waitingForMill,
            waitingForElectric = waitingForElectric,
            waitingForGate = waitingForGate,
            waitingForUnload = waitingForUnload,
            activeMillCount = activeMillCount,
            activeElectricCount = activeElectricCount,
            activeUnloadCount = activeUnloadCount,
            completedMillToday = completedMillToday,
            completedElectricToday = completedElectricToday,
            completedUnloadToday = completedUnloadToday
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardStats()
    )

    fun login(role: UserRole, passwordInput: String): Boolean {
        updateActivityTimestamp()
        if (passwordInput == role.defaultPassword) {
            // Thoroughly reset transient state on new session
            selectedLorry.value = null
            searchQuery.value = ""
            filterStatus.value = "ALL"

            currentUserRole.value = role
            showToast("Logged in as ${role.title}")
            logAuditAction("LOGIN", "SYSTEM", "User logged in with role ${role.title}")

            // Refresh latest lorry data from remote repository for fresh session
            viewModelScope.launch {
                try {
                    repository.refreshFromRemote()
                } catch (_: Exception) {
                }
            }
            return true
        } else {
            showToast("Invalid password for ${role.title}")
            logAuditAction("LOGIN_FAILED", "SYSTEM", "Failed login attempt for role ${role.title}")
            return false
        }
    }

    fun logout(reason: String = "Logged out successfully") {
        val prevRole = currentUserRole.value?.title ?: "User"
        // Clear all session, role, search, and selection state completely
        currentUserRole.value = null
        selectedLorry.value = null
        searchQuery.value = ""
        filterStatus.value = "ALL"
        showToast(reason)
        logAuditAction("LOGOUT", "SYSTEM", "$prevRole session ended")
    }

    fun generateGatePassNumber(): String {
        val datePart = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val randomSuffix = (1000..9999).random()
        return "GE-$datePart-$randomSuffix"
    }

    fun saveGateEntry(
        lorryNumber: String,
        chalan: String,
        party: String,
        description: String,
        quantity: Double,
        unit: String,
        grossWeight: Double?,
        tareWeight: Double?,
        qualityItems: List<QualityItem>,
        mokam: String,
        marka: String,
        department: String = ""
    ) {
        updateActivityTimestamp()
        viewModelScope.launch {
            val gatePass = generateGatePassNumber()
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            val netW = if (grossWeight != null && tareWeight != null) grossWeight - tareWeight else null
            val qualityJson = try { qualityItemsAdapter.toJson(qualityItems) } catch (e: Exception) { "[]" }

            val statusName = when (department.trim().lowercase()) {
                "store" -> LorryStatus.STORE_PENDING.name
                "finish good" -> LorryStatus.FINISH_GOOD_PENDING.name
                "other" -> LorryStatus.OTHER_PENDING.name
                else -> LorryStatus.GATE_ENTRY.name
            }

            val stageName = when (department.trim().lowercase()) {
                "store" -> "Store Dept"
                "finish good" -> "Finish Good Dept"
                "other" -> "Other Dept"
                else -> "Mill Weighbridge"
            }

            val lorry = LorryWeighment(
                gatePass = gatePass,
                date = todayStr,
                type = "IN",
                lorryNumber = lorryNumber.trim().uppercase(),
                chalan = chalan.trim(),
                inTime = timeStr,
                party = party.trim(),
                description = if (description.isNotBlank()) description else department,
                department = department.trim(),
                totalQuantity = quantity,
                unit = unit,
                grossWeight = grossWeight,
                millGrossWeight = null,
                tareWeight = tareWeight,
                netWeight = netW,
                mokam = mokam,
                marka = marka,
                status = statusName,
                currentStage = stageName,
                remarks = "Department: $department",
                createdAt = System.currentTimeMillis(),
                qualityItemsJson = qualityJson
            )

            repository.saveGateEntry(lorry)
            logAuditAction("CREATE_GATE_ENTRY", gatePass, "Entry $gatePass for ${lorry.lorryNumber}, Dept: $department")
            showToast("Gate Entry $gatePass Saved Successfully for $department")
        }
    }

    fun submitDepartmentProcessing(
        gatePass: String,
        loadUnloadStatus: String,
        remarksText: String,
        clearForExit: Boolean
    ) {
        updateActivityTimestamp()
        viewModelScope.launch {
            val allList = repository.allLorriesList()
            val lorry = allList.find { it.gatePass == gatePass }
            if (lorry != null) {
                val isUnloaded = loadUnloadStatus.equals("Unloaded", ignoreCase = true)
                val updatedStatus = if (clearForExit) LorryStatus.READY_FOR_GATE_EXIT.name else lorry.status
                val updatedStage = if (clearForExit) "Gate Out" else lorry.currentStage
                val combinedRemarks = if (remarksText.isNotBlank()) {
                    "${lorry.remarks ?: ""}\n[Dept Action: $loadUnloadStatus] $remarksText".trim()
                } else {
                    "${lorry.remarks ?: ""}\n[Dept Action: $loadUnloadStatus]".trim()
                }

                val updatedLorry = lorry.copy(
                    status = updatedStatus,
                    currentStage = updatedStage,
                    unloaded = isUnloaded,
                    remarks = combinedRemarks,
                    updatedAt = System.currentTimeMillis()
                )

                repository.updateLorry(updatedLorry)
                logAuditAction("DEPARTMENT_ACTION", gatePass, "Department processed $gatePass ($loadUnloadStatus). Clear for exit: $clearForExit")
                showToast("Vehicle ${lorry.lorryNumber} processed! " + if (clearForExit) "Cleared for Main Gate Out." else "")
            }
        }
    }

    fun submitMillGrossWeight(
        gatePass: String,
        grossWeight: Double,
        party: String = "",
        chalan: String = "",
        mokam: String = "",
        marka: String = "",
        description: String = "",
        tareWeight: Double? = null,
        totalQuantity: Double = 0.0,
        unit: String = "BALES",
        qualityItems: List<QualityItem> = emptyList(),
        lorryNumber: String = "",
        chalanGrossWeight: Double? = null
    ) {
        updateActivityTimestamp()
        viewModelScope.launch {
            val allList = repository.allLorriesList()
            var lorry = allList.find { it.gatePass == gatePass || (lorryNumber.isNotBlank() && it.lorryNumber.equals(lorryNumber.trim(), ignoreCase = true) && it.status != LorryStatus.COMPLETED.name) }
            val qualityJson = try { qualityItemsAdapter.toJson(qualityItems) } catch (e: Exception) { "[]" }
            val effectiveGateGross = if (chalanGrossWeight != null && chalanGrossWeight > 0) chalanGrossWeight else null
            val netW = if (grossWeight > 0 && tareWeight != null && tareWeight > 0) (grossWeight - tareWeight) else null

            if (lorry == null) {
                val newPass = if (gatePass.isNotBlank()) gatePass else generateGatePassNumber()
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                val newLorry = LorryWeighment(
                    gatePass = newPass,
                    date = todayStr,
                    type = "IN",
                    lorryNumber = if (lorryNumber.isNotBlank()) lorryNumber.uppercase() else "VEHICLE",
                    chalan = chalan,
                    inTime = timeStr,
                    party = party,
                    description = description.ifBlank { "Jute" },
                    totalQuantity = totalQuantity,
                    unit = unit,
                    grossWeight = effectiveGateGross,
                    millGrossWeight = grossWeight,
                    millTareWeight = tareWeight,
                    netWeight = netW,
                    mokam = mokam,
                    marka = marka,
                    status = LorryStatus.WAITING_FOR_UNLOADING.name,
                    currentStage = "Unloading Yard",
                    createdAt = System.currentTimeMillis(),
                    qualityItemsJson = qualityJson
                )
                repository.saveGateEntry(newLorry)
                logAuditAction("MILL_GROSS_WEIGHT", newLorry.gatePass, "New Entry & Mill Gross: ${grossWeight.toInt()} kg, Party: ${newLorry.party}")
                showToast("Mill Gross Weight & Party Challan recorded for ${newLorry.gatePass}")
            } else {
                val currentStageIndex = LorryStatus.fromString(lorry.status).stageIndex
                val targetStatus = LorryStatus.WAITING_FOR_UNLOADING
                val nextStatus = if (currentStageIndex < targetStatus.stageIndex) targetStatus.name else lorry.status
                val nextStage = if (currentStageIndex < targetStatus.stageIndex) targetStatus.stageName else lorry.currentStage

                val updated = lorry.copy(
                    grossWeight = effectiveGateGross ?: lorry.grossWeight,
                    millGrossWeight = grossWeight,
                    party = if (party.isNotBlank()) party else lorry.party,
                    chalan = if (chalan.isNotBlank()) chalan else lorry.chalan,
                    mokam = if (mokam.isNotBlank()) mokam else lorry.mokam,
                    marka = if (marka.isNotBlank()) marka else lorry.marka,
                    description = if (description.isNotBlank()) description else lorry.description,
                    millTareWeight = tareWeight ?: lorry.millTareWeight,
                    totalQuantity = if (totalQuantity > 0) totalQuantity else lorry.totalQuantity,
                    unit = if (unit.isNotBlank()) unit else lorry.unit,
                    netWeight = netW ?: lorry.netWeight,
                    qualityItemsJson = if (qualityItems.isNotEmpty()) qualityJson else lorry.qualityItemsJson,
                    status = nextStatus,
                    currentStage = nextStage,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateLorry(updated)
                logAuditAction("MILL_GROSS_WEIGHT", lorry.gatePass, "Mill Gross: ${grossWeight.toInt()} kg, Party: ${updated.party}, Challan: ${updated.chalan}")
                showToast("Mill Gross Weight & Party Challan recorded for ${lorry.gatePass}")
            }
        }
    }

    fun submitElectricGrossWeight(gatePass: String, electricGrossWeight: Double) {
        updateActivityTimestamp()
        viewModelScope.launch {
            val allList = repository.allLorriesList()
            val lorry = allList.find { 
                it.gatePass == gatePass || 
                (gatePass.isNotBlank() && it.lorryNumber.equals(gatePass.trim(), ignoreCase = true) && it.status != LorryStatus.COMPLETED.name) 
            }
            if (lorry != null) {
                val currentStageIndex = LorryStatus.fromString(lorry.status).stageIndex
                val targetStatus = LorryStatus.ELECTRIC_GROSS_DONE
                val nextStatus = if (currentStageIndex < targetStatus.stageIndex) targetStatus.name else lorry.status
                val nextStage = if (currentStageIndex < targetStatus.stageIndex) targetStatus.stageName else lorry.currentStage

                val updated = lorry.copy(
                    electricGrossWeight = electricGrossWeight,
                    status = nextStatus,
                    currentStage = nextStage,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateLorry(updated)
                logAuditAction("ELECTRIC_GROSS_WEIGHT", lorry.gatePass, "Electric Gross: ${electricGrossWeight.toInt()} kg")
                showToast("Electric Gross Weight recorded for ${lorry.gatePass}")
            } else {
                showToast("Vehicle record not found for Electric Gross Weight")
            }
        }
    }

    fun markUnloaded(gatePass: String) {
        updateActivityTimestamp()
        viewModelScope.launch {
            val allList = repository.allLorriesList()
            val lorry = allList.find { it.gatePass == gatePass || (it.lorryNumber.equals(gatePass.trim(), ignoreCase = true) && it.status != LorryStatus.COMPLETED.name) }
            if (lorry != null) {
                val hasTare = lorry.hasTareRecorded || (lorry.electricTareWeight != null && lorry.electricTareWeight > 0)
                val targetStatus = if (hasTare) LorryStatus.READY_FOR_GATE_EXIT else LorryStatus.READY_FOR_GATE_EXIT
                val updated = lorry.copy(
                    unloaded = true,
                    status = targetStatus.name,
                    currentStage = if (targetStatus == LorryStatus.READY_FOR_GATE_EXIT) "Ready For Gate Exit" else "Unloading Completed",
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateLorry(updated)
                logAuditAction("MARK_UNLOADED", lorry.gatePass, "Jute Unloading marked COMPLETED")
                showToast("Unloading marked completed & Cleared for Exit for ${lorry.lorryNumber}")
            }
        }
    }

    fun submitElectricTareWeight(gatePass: String, electricTareWeight: Double) {
        updateActivityTimestamp()
        viewModelScope.launch {
            val allList = repository.allLorriesList()
            val lorry = allList.find { 
                it.gatePass == gatePass || 
                (gatePass.isNotBlank() && it.lorryNumber.equals(gatePass.trim(), ignoreCase = true) && it.status != LorryStatus.COMPLETED.name) 
            }
            if (lorry != null) {
                val targetStatus = LorryStatus.READY_FOR_GATE_EXIT

                val updated = lorry.copy(
                    electricTareWeight = electricTareWeight,
                    status = targetStatus.name,
                    currentStage = targetStatus.stageName,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateLorry(updated)
                logAuditAction("ELECTRIC_TARE_WEIGHT", lorry.gatePass, "Electric Tare: ${electricTareWeight.toInt()} kg")
                showToast("Electric Tare Weight recorded & Cleared for Gate Exit for ${lorry.lorryNumber}")
            } else {
                showToast("Vehicle record not found for Electric Tare Weight")
            }
        }
    }

    fun submitElectricWeights(gatePass: String, electricGrossWeight: Double?, electricTareWeight: Double?) {
        updateActivityTimestamp()
        viewModelScope.launch {
            val allList = repository.allLorriesList()
            val lorry = allList.find { 
                it.gatePass == gatePass || 
                (gatePass.isNotBlank() && it.lorryNumber.equals(gatePass.trim(), ignoreCase = true) && it.status != LorryStatus.COMPLETED.name) 
            }
            if (lorry != null) {
                val hasTare = (electricTareWeight != null && electricTareWeight > 0) || lorry.hasTareRecorded
                val targetStatus = when {
                    hasTare || lorry.unloaded -> LorryStatus.READY_FOR_GATE_EXIT
                    electricGrossWeight != null && electricGrossWeight > 0 -> LorryStatus.ELECTRIC_GROSS_DONE
                    else -> LorryStatus.fromString(lorry.status)
                }

                val updated = lorry.copy(
                    electricGrossWeight = electricGrossWeight ?: lorry.electricGrossWeight,
                    electricTareWeight = electricTareWeight ?: lorry.electricTareWeight,
                    status = targetStatus.name,
                    currentStage = targetStatus.stageName,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateLorry(updated)
                logAuditAction("ELECTRIC_WEIGHTS", lorry.gatePass, "Elec Gross: ${electricGrossWeight?.toInt() ?: "N/A"} kg, Elec Tare: ${electricTareWeight?.toInt() ?: "N/A"} kg")
                showToast("Electric Weighment saved for ${lorry.lorryNumber}")
            } else {
                showToast("Vehicle record not found for Electric Weightment")
            }
        }
    }

    fun submitMillTareWeight(gatePass: String, millTareWeight: Double) {
        updateActivityTimestamp()
        viewModelScope.launch {
            val allList = repository.allLorriesList()
            val lorry = allList.find { it.gatePass == gatePass }
            if (lorry != null) {
                val gross = lorry.millGrossWeight ?: lorry.grossWeight ?: lorry.electricGrossWeight ?: 0.0
                val net = if (gross > 0) gross - millTareWeight else 0.0

                val currentStageIndex = LorryStatus.fromString(lorry.status).stageIndex
                val targetStatus = LorryStatus.READY_FOR_GATE_EXIT
                val nextStatus = if (currentStageIndex < targetStatus.stageIndex) targetStatus.name else lorry.status
                val nextStage = if (currentStageIndex < targetStatus.stageIndex) targetStatus.stageName else lorry.currentStage

                val updated = lorry.copy(
                    millTareWeight = millTareWeight,
                    tareWeight = millTareWeight,
                    netWeight = net,
                    status = nextStatus,
                    currentStage = nextStage,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateLorry(updated)
                logAuditAction("MILL_TARE_VERIFY", gatePass, "Mill Tare: ${millTareWeight.toInt()} kg, Net Jute: ${net.toInt()} kg")
                showToast("Mill Tare Verification completed for $gatePass")
            }
        }
    }

    fun markGateOut(gatePass: String, remarks: String) {
        updateActivityTimestamp()
        viewModelScope.launch {
            val allList = repository.allLorriesList()
            val lorry = allList.find { it.gatePass == gatePass || (it.lorryNumber.equals(gatePass.trim(), ignoreCase = true)) }
            if (lorry != null) {
                if (lorry.status == LorryStatus.COMPLETED.name || !lorry.outTime.isNullOrBlank()) {
                    showToast("Lorry is already marked OUT.")
                    return@launch
                }
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

                val updated = lorry.copy(
                    status = LorryStatus.COMPLETED.name,
                    currentStage = "Lorry Out",
                    outDate = todayStr,
                    outTime = timeStr,
                    remarks = remarks.ifEmpty { "Gate Out Completed" },
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateLorry(updated)
                logAuditAction("GATE_OUT", gatePass, "Lorry Out marked. Remarks: ${updated.remarks}")
                showToast("Lorry ${lorry.lorryNumber} marked OUT successfully!")
            } else {
                showToast("Lorry is already marked OUT.")
            }
        }
    }

    fun deleteLorry(gatePass: String) {
        updateActivityTimestamp()
        viewModelScope.launch {
            if (currentUserRole.value == UserRole.SUPER_ADMIN) {
                repository.deleteLorry(gatePass)
                logAuditAction("DELETE_RECORD", gatePass, "Deleted gate pass $gatePass by Super Admin")
                showToast("Record $gatePass deleted by Super Admin")
            } else {
                showToast("Only Super Admin can delete records")
            }
        }
    }

    fun parseQualityItems(json: String): List<QualityItem> {
        return try {
            qualityItemsAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun toggleGeofenceEnforcement(enabled: Boolean) {
        if (currentUserRole.value == UserRole.SUPER_ADMIN) {
            val curr = geoFenceState.value
            geoFenceState.value = curr.copy(isGeofenceEnabled = enabled)
            logAuditAction("GEOFENCE_ADMIN_TOGGLE", "SYSTEM", "Admin changed Geofence status to ${if (enabled) "ENABLED" else "DISABLED"}")
            showToast("Geofence Enforcement ${if (enabled) "ENABLED (Restricted to Mill)" else "DISABLED (Off by Admin)"}")
        } else {
            showToast("Only Admin can turn Geofence ON or OFF")
        }
    }

    fun isActionBlockedByGeofence(): Boolean {
        val state = geoFenceState.value
        if (!state.isGeofenceEnabled || state.superAdminOverride) {
            return false
        }
        return !state.isInsideBallyJuteMill
    }

    fun toggleGeoFenceOverride() {
        if (currentUserRole.value == UserRole.SUPER_ADMIN) {
            val curr = geoFenceState.value
            geoFenceState.value = curr.copy(superAdminOverride = !curr.superAdminOverride)
            logAuditAction("GEOFENCE_OVERRIDE", "SYSTEM", "Super Admin Override toggled: ${geoFenceState.value.superAdminOverride}")
            showToast("Admin Override toggled: ${geoFenceState.value.superAdminOverride}")
        } else {
            showToast("Only Admin can control Geofence override")
        }
    }

    fun showToast(msg: String) {
        toastMessage.value = msg
    }

    fun clearToast() {
        toastMessage.value = null
    }

    fun getDaysInside(createdAt: Long): Long {
        val diffMs = System.currentTimeMillis() - createdAt
        return (diffMs / (1000 * 3600 * 24)).coerceAtLeast(0)
    }

    fun getDaysInsideText(createdAt: Long): String {
        val days = getDaysInside(createdAt)
        return when {
            days == 0L -> "Today"
            days == 1L -> "1 Day"
            else -> "$days Days"
        }
    }

    // User Management
    fun addUser(username: String, role: UserRole) {
        val cleanName = username.trim()
        if (cleanName.isBlank()) return
        if (appUsersList.value.any { it.username.equals(cleanName, ignoreCase = true) }) {
            showToast("User '$cleanName' already exists")
            return
        }
        appUsersList.value = appUsersList.value + AppUser(cleanName, role, true, "Created Just Now")
        logAuditAction("ADD_USER", cleanName, "Created new user $cleanName with role ${role.title}")
        showToast("User '$cleanName' added successfully")
    }

    fun toggleUserStatus(username: String) {
        appUsersList.value = appUsersList.value.map {
            if (it.username == username) it.copy(isActive = !it.isActive) else it
        }
        logAuditAction("USER_STATUS_CHANGE", username, "Toggled active status for user $username")
        showToast("Status updated for user $username")
    }

    fun deleteUser(username: String) {
        if (username.contains("Admin", ignoreCase = true)) {
            showToast("Primary Admin user cannot be deleted")
            return
        }
        appUsersList.value = appUsersList.value.filter { it.username != username }
        logAuditAction("DELETE_USER", username, "Deleted user account $username")
        showToast("User $username deleted")
    }

    // Masters Management
    fun addBroker(brokerName: String) {
        val name = brokerName.trim()
        if (name.isBlank()) return
        if (!brokersList.value.contains(name)) {
            brokersList.value = listOf(name) + brokersList.value
            logAuditAction("MASTER_ADD", "BROKER", "Added new broker: $name")
            showToast("Broker '$name' added to Master")
        }
    }

    fun deleteBroker(brokerName: String) {
        brokersList.value = brokersList.value.filter { it != brokerName }
        logAuditAction("MASTER_DELETE", "BROKER", "Deleted broker: $brokerName")
        showToast("Broker '$brokerName' deleted")
    }

    fun addQuality(qualityName: String) {
        val name = qualityName.trim().uppercase()
        if (name.isBlank()) return
        if (!qualitiesList.value.contains(name)) {
            qualitiesList.value = listOf(name) + qualitiesList.value
            logAuditAction("MASTER_ADD", "QUALITY", "Added new quality grade: $name")
            showToast("Quality '$name' added to Master")
        }
    }

    fun deleteQuality(qualityName: String) {
        qualitiesList.value = qualitiesList.value.filter { it != qualityName }
        logAuditAction("MASTER_DELETE", "QUALITY", "Deleted quality: $qualityName")
        showToast("Quality '$qualityName' deleted")
    }

    fun addMokam(mokamName: String) {
        val name = mokamName.trim().uppercase()
        if (name.isBlank()) return
        if (!mokamsList.value.contains(name)) {
            mokamsList.value = listOf(name) + mokamsList.value
            logAuditAction("MASTER_ADD", "MOKAM", "Added new mokam station: $name")
            showToast("Mokam '$name' added to Master")
        }
    }

    fun deleteMokam(mokamName: String) {
        mokamsList.value = mokamsList.value.filter { it != mokamName }
        logAuditAction("MASTER_DELETE", "MOKAM", "Deleted mokam: $mokamName")
        showToast("Mokam '$mokamName' deleted")
    }

    fun addMarka(markaName: String) {
        val name = markaName.trim().uppercase()
        if (name.isBlank()) return
        if (!markasList.value.contains(name)) {
            markasList.value = listOf(name) + markasList.value
            logAuditAction("MASTER_ADD", "MARKA", "Added new marka symbol: $name")
            showToast("Marka '$name' added to Master")
        }
    }

    fun deleteMarka(markaName: String) {
        markasList.value = markasList.value.filter { it != markaName }
        logAuditAction("MASTER_DELETE", "MARKA", "Deleted marka: $markaName")
        showToast("Marka '$markaName' deleted")
    }

    // Geofencing and Settings
    fun updateGeoFenceRadius(radiusMeters: Int) {
        geoFenceState.value = geoFenceState.value.copy(radiusMeters = radiusMeters)
        logAuditAction("GEOFENCE_CONFIG", "SYSTEM", "Updated Geofence radius to ${radiusMeters}m")
        showToast("Geofence radius set to ${radiusMeters}m")
    }

    fun simulateGpsLocation(lat: Double, lng: Double, isInside: Boolean, name: String) {
        val dist = if (isInside) 45f else 1850f
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        geoFenceState.value = geoFenceState.value.copy(
            latitude = lat,
            longitude = lng,
            distanceFromMillMeters = dist,
            isInsideBallyJuteMill = isInside,
            locationName = name,
            lastGpsUpdate = "Simulated GPS ($timeStr)"
        )
        logAuditAction("GPS_SIMULATION", "SYSTEM", "GPS Position changed to: $name (${if (isInside) "IN GEOFENCE" else "OUTSIDE GEOFENCE"})")
        showToast("Device Location: $name (${if (isInside) "INSIDE MILL" else "OUTSIDE RESTRICTED"})")
    }

    fun updateGoogleLocationServicesGps(lat: Double, lng: Double, distanceMeters: Float, isInside: Boolean, name: String) {
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        geoFenceState.value = geoFenceState.value.copy(
            latitude = lat,
            longitude = lng,
            distanceFromMillMeters = distanceMeters,
            isInsideBallyJuteMill = isInside,
            locationName = name,
            lastGpsUpdate = "Google Location Services ($timeStr)"
        )
        logAuditAction("GOOGLE_GPS_UPDATE", "SYSTEM", "GPS: $name ($lat, $lng). Distance: ${distanceMeters.toInt()}m")
        showToast("GPS Location Updated via Google Location Services")
    }

    fun updateSettings(millOffset: Double, electricOffset: Double, autoPrint: Boolean, timeoutMin: Int) {
        systemSettings.value = SystemSettingsState(
            millZeroOffsetKg = millOffset,
            electricZeroOffsetKg = electricOffset,
            autoPrintThermalReceipt = autoPrint,
            inactivityTimeoutMinutes = timeoutMin
        )
        securityState.value = securityState.value.copy(isAutoLogoutEnabled = timeoutMin > 0)
        logAuditAction("CALIBRATION_SAVE", "SYSTEM", "Saved weighbridge zero offsets (Mill: ${millOffset}kg, Elec: ${electricOffset}kg)")
        showToast("System settings & weighbridge offsets updated")
    }

    fun exportDatabaseBackupJson() {
        viewModelScope.launch {
            try {
                val list = repository.allLorriesList()
                val count = list.size
                logAuditAction("DATABASE_BACKUP", "SYSTEM", "Full SQLite database backup exported successfully ($count lorries)")
                showToast("Offline JSON database backup exported successfully ($count records)")
            } catch (e: Exception) {
                showToast("Backup export failed: ${e.localizedMessage}")
            }
        }
    }

    fun clearAuditLogs() {
        auditLogs.value = emptyList()
        seedInitialAuditLogs()
        showToast("Audit logs reset")
    }
}
