package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.SystemUpdate
import com.example.data.model.remote.AppUpdateDto
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.LorryWeighment
import com.example.data.model.UserRole
import com.example.ui.screens.DepartmentDashboard
import com.example.ui.screens.ElectricWeightmentDashboard
import com.example.ui.screens.GateOutScreen
import com.example.ui.screens.GeoFenceRestrictionDialog
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainGateDashboard
import com.example.ui.screens.MillWeightmentDashboard
import com.example.ui.screens.NewGateEntryScreen
import com.example.ui.screens.PendingLorriesScreen
import com.example.ui.screens.SuperAdminDashboard
import com.example.ui.theme.BallyWeighbridgeTheme
import com.example.ui.theme.IndustrialBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusPurple
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set FLAG_SECURE to prevent screenshots and screen recording for security
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        enableEdgeToEdge()

        setContent {
            BallyWeighbridgeTheme {
                BallyWeighbridgeApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Enforce FLAG_SECURE on resume to block screenshots and recording
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BallyWeighbridgeApp(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUserRole by viewModel.currentUserRole.collectAsState()
    val geoFenceState by viewModel.geoFenceState.collectAsState()
    val securityState by viewModel.securityState.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()

    val filteredLorries by viewModel.filteredLorries.collectAsState()
    val allLorries by viewModel.allLorriesFlow.collectAsState(initial = emptyList())
    val stats by viewModel.dashboardStats.collectAsState()

    val appUsers by viewModel.appUsersList.collectAsState()
    val systemSettings by viewModel.systemSettings.collectAsState()

    val brokersList by viewModel.brokersList.collectAsState()
    val qualitiesList by viewModel.qualitiesList.collectAsState()
    val mokamsList by viewModel.mokamsList.collectAsState()
    val markasList by viewModel.markasList.collectAsState()

    val toastMsg by viewModel.toastMessage.collectAsState()
    val availableAppUpdate by viewModel.availableAppUpdate.collectAsState()

    // Periodic check for auto logout due to inactivity
    LaunchedEffect(currentUserRole) {
        while (currentUserRole != null) {
            kotlinx.coroutines.delay(60000L) // Check every 1 minute
            viewModel.checkInactivityAndAutoLogout(maxInactiveMinutes = 5)
        }
    }

    // Handle toast messages
    LaunchedEffect(toastMsg) {
        toastMsg?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    if (currentUserRole == null) {
        LoginScreen(
            onLoginClick = { role, pass ->
                viewModel.login(role, pass)
            }
        )
    } else {
        key(currentUserRole) {
            val roleNavController = rememberNavController()
            var currentNavRoute by remember { mutableStateOf("dashboard") }
            var activeGateOutLorry by remember { mutableStateOf<LorryWeighment?>(null) }
            var showGeoFenceDialog by remember { mutableStateOf(false) }

            val roleThemeColor = when (currentUserRole) {
                UserRole.MAIN_GATE -> IndustrialBlue
                UserRole.MILL_WEIGHTMENT -> StatusGreen
                UserRole.ELECTRIC_WEIGHTMENT -> StatusPurple
                UserRole.STORE -> Color(0xFF0284C7)
                UserRole.FINISH_GOOD -> Color(0xFF059669)
                UserRole.OTHER -> Color(0xFFD97706)
                UserRole.SUPER_ADMIN -> StatusRed
                else -> IndustrialBlue
            }

            val rolePendingCount = remember(allLorries, currentUserRole) {
                val pendingList = allLorries.filter { it.status != com.example.data.model.LorryStatus.COMPLETED.name && it.outTime.isNullOrEmpty() }
                when (currentUserRole) {
                    UserRole.MILL_WEIGHTMENT, UserRole.ELECTRIC_WEIGHTMENT -> {
                        pendingList.count { it.effectiveDepartment.equals("Jute", ignoreCase = true) }
                    }
                    UserRole.STORE -> {
                        pendingList.count { it.effectiveDepartment.equals("Store", ignoreCase = true) }
                    }
                    UserRole.FINISH_GOOD -> {
                        pendingList.count { it.effectiveDepartment.equals("Finish Good", ignoreCase = true) }
                    }
                    UserRole.OTHER -> {
                        pendingList.count { it.effectiveDepartment.equals("Other", ignoreCase = true) }
                    }
                    else -> pendingList.size
                }
            }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Bally Jute Mill",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = "Role: ${currentUserRole?.title}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.checkForAppUpdatesManually() },
                            modifier = Modifier.testTag("top_app_bar_check_updates")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = "Check for OTA Updates",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.testTag("top_app_bar_logout")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = roleThemeColor
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentNavRoute == "dashboard",
                        onClick = {
                            currentNavRoute = "dashboard"
                            roleNavController.navigate("dashboard") { launchSingleTop = true }
                        },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = {
                            Text(
                                when (currentUserRole) {
                                    UserRole.MILL_WEIGHTMENT -> "Mill Wt"
                                    UserRole.ELECTRIC_WEIGHTMENT -> "Electric Wt"
                                    UserRole.STORE -> "Store"
                                    UserRole.FINISH_GOOD -> "Finish Good"
                                    UserRole.OTHER -> "Other Dept"
                                    else -> "Dashboard"
                                }
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = roleThemeColor)
                    )

                    if (currentUserRole == UserRole.MAIN_GATE || currentUserRole == UserRole.SUPER_ADMIN) {
                        NavigationBarItem(
                            selected = currentNavRoute == "new_entry",
                            onClick = {
                                currentNavRoute = "new_entry"
                                roleNavController.navigate("new_entry") { launchSingleTop = true }
                            },
                            icon = { Icon(Icons.Default.AddCircle, contentDescription = "New Entry") },
                            label = { Text("Gate Entry") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = roleThemeColor)
                        )
                    }

                    if (currentUserRole == UserRole.SUPER_ADMIN) {
                        NavigationBarItem(
                            selected = currentNavRoute == "mill_weight",
                            onClick = {
                                currentNavRoute = "mill_weight"
                                roleNavController.navigate("mill_weight") { launchSingleTop = true }
                            },
                            icon = { Icon(Icons.Default.Scale, contentDescription = "Mill") },
                            label = { Text("Mill Wt") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = roleThemeColor)
                        )
                        NavigationBarItem(
                            selected = currentNavRoute == "electric_weight",
                            onClick = {
                                currentNavRoute = "electric_weight"
                                roleNavController.navigate("electric_weight") { launchSingleTop = true }
                            },
                            icon = { Icon(Icons.Default.ElectricBolt, contentDescription = "Electric") },
                            label = { Text("Electric Wt") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = roleThemeColor)
                        )
                    }

                    NavigationBarItem(
                        selected = currentNavRoute == "pending",
                        onClick = {
                            currentNavRoute = "pending"
                            roleNavController.navigate("pending") { launchSingleTop = true }
                        },
                        icon = { Icon(Icons.Default.FormatListNumbered, contentDescription = "Pending Lorries") },
                        label = { Text("Pending ($rolePendingCount)") },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = roleThemeColor)
                    )

                    if (currentUserRole == UserRole.SUPER_ADMIN) {
                        NavigationBarItem(
                            selected = currentNavRoute == "admin",
                            onClick = {
                                currentNavRoute = "admin"
                                roleNavController.navigate("admin") { launchSingleTop = true }
                            },
                            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                            label = { Text("Admin") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = roleThemeColor)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    availableAppUpdate?.let { update: AppUpdateDto ->
                        OtaUpdateBannerCard(
                            updateDto = update,
                            onDismiss = { viewModel.dismissUpdateBanner() }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        NavHost(
                            navController = roleNavController,
                            startDestination = "dashboard"
                        ) {
                    composable("dashboard") {
                        when (currentUserRole) {
                            UserRole.MAIN_GATE -> MainGateDashboard(
                                stats = stats,
                                lorries = filteredLorries.filter { it.status != com.example.data.model.LorryStatus.COMPLETED.name && it.outTime.isNullOrEmpty() },
                                searchQuery = searchQuery,
                                onSearchChange = { viewModel.searchQuery.value = it },
                                onNewEntryClick = {
                                    currentNavRoute = "new_entry"
                                    roleNavController.navigate("new_entry")
                                },
                                onViewAllPendingClick = {
                                    currentNavRoute = "pending"
                                    roleNavController.navigate("pending")
                                },
                                onLorryClick = { lorry ->
                                    val statusEnum = com.example.data.model.LorryStatus.fromString(lorry.status)
                                    val isAllowed = statusEnum == com.example.data.model.LorryStatus.READY_FOR_GATE_EXIT ||
                                            statusEnum == com.example.data.model.LorryStatus.COMPLETED ||
                                            statusEnum == com.example.data.model.LorryStatus.ELECTRIC_TARE_DONE ||
                                            lorry.hasTareRecorded || lorry.unloaded || !lorry.outTime.isNullOrEmpty()
                                    if (isAllowed) {
                                        activeGateOutLorry = lorry
                                        roleNavController.navigate("gate_out")
                                    } else {
                                        viewModel.showToast("Lorry is currently at stage: ${lorry.currentStage}. Gate Exit is permitted when status is Ready for Gate Exit.")
                                    }
                                },
                                onDaysInsideText = { viewModel.getDaysInsideText(it) },
                                currentUserRole = currentUserRole
                            )

                            UserRole.MILL_WEIGHTMENT -> MillWeightmentDashboard(
                                lorries = allLorries.filter { it.effectiveDepartment.equals("Jute", ignoreCase = true) && it.status != com.example.data.model.LorryStatus.COMPLETED.name && it.outTime.isNullOrEmpty() },
                                searchQuery = searchQuery,
                                onSearchChange = { viewModel.searchQuery.value = it },
                                onSubmitGrossWeight = { pass, w, party, chalan, mokam, marka, desc, tareWt, totalQty, unit, qualityItems, lorryNum, chalanGross ->
                                    viewModel.submitMillGrossWeight(pass, w, party, chalan, mokam, marka, desc, tareWt, totalQty, unit, qualityItems, lorryNum, chalanGross)
                                },
                                onSubmitTareWeight = { pass, w -> viewModel.submitMillTareWeight(pass, w) },
                                currentUserRole = currentUserRole
                            )

                            UserRole.ELECTRIC_WEIGHTMENT -> ElectricWeightmentDashboard(
                                lorries = allLorries.filter { it.effectiveDepartment.equals("Jute", ignoreCase = true) && it.status != com.example.data.model.LorryStatus.COMPLETED.name && it.outTime.isNullOrEmpty() },
                                searchQuery = searchQuery,
                                onSearchChange = { viewModel.searchQuery.value = it },
                                onSubmitElectricGross = { pass, w -> viewModel.submitElectricGrossWeight(pass, w) },
                                onMarkUnloaded = { pass -> viewModel.markUnloaded(pass) },
                                onSubmitElectricTare = { pass, w -> viewModel.submitElectricTareWeight(pass, w) },
                                onSubmitElectricWeights = { pass, eg, et -> viewModel.submitElectricWeights(pass, eg, et) },
                                currentUserRole = currentUserRole
                            )

                            UserRole.STORE, UserRole.FINISH_GOOD, UserRole.OTHER -> DepartmentDashboard(
                                departmentRole = currentUserRole!!,
                                lorries = allLorries.filter { lorry ->
                                    val targetDept = when (currentUserRole) {
                                        UserRole.STORE -> "Store"
                                        UserRole.FINISH_GOOD -> "Finish Good"
                                        UserRole.OTHER -> "Other"
                                        else -> ""
                                    }
                                    lorry.effectiveDepartment.equals(targetDept, ignoreCase = true)
                                },
                                searchQuery = searchQuery,
                                onSearchChange = { viewModel.searchQuery.value = it },
                                onSubmitDepartmentAction = { gatePass, loadUnloadStatus, remarks, clearForExit ->
                                    viewModel.submitDepartmentProcessing(gatePass, loadUnloadStatus, remarks, clearForExit)
                                }
                            )

                            UserRole.SUPER_ADMIN -> SuperAdminDashboard(
                                stats = stats,
                                geoFenceState = geoFenceState,
                                securityState = securityState,
                                auditLogs = auditLogs,
                                allLorries = allLorries,
                                appUsers = appUsers,
                                systemSettings = systemSettings,
                                brokers = brokersList,
                                qualities = qualitiesList,
                                mokams = mokamsList,
                                markas = markasList,
                                onToggleGeoFenceOverride = { viewModel.toggleGeoFenceOverride() },
                                onToggleGeofenceEnforcement = { enabled -> viewModel.toggleGeofenceEnforcement(enabled) },
                                onUpdateGpsLocation = { lat, lng, dist, isInside, name -> viewModel.updateGoogleLocationServicesGps(lat, lng, dist, isInside, name) },
                                onSyncOffline = { viewModel.syncOfflineData() },
                                onExportReport = { format ->
                                    viewModel.showToast("$format Report exported successfully to Downloads folder")
                                },
                                onShowToast = { viewModel.showToast(it) },
                                onAddUser = { name, role -> viewModel.addUser(name, role) },
                                onToggleUserStatus = { name -> viewModel.toggleUserStatus(name) },
                                onDeleteUser = { name -> viewModel.deleteUser(name) },
                                onAddBroker = { name -> viewModel.addBroker(name) },
                                onDeleteBroker = { name -> viewModel.deleteBroker(name) },
                                onAddQuality = { name -> viewModel.addQuality(name) },
                                onDeleteQuality = { name -> viewModel.deleteQuality(name) },
                                onAddMokam = { name -> viewModel.addMokam(name) },
                                onDeleteMokam = { name -> viewModel.deleteMokam(name) },
                                onAddMarka = { name -> viewModel.addMarka(name) },
                                onDeleteMarka = { name -> viewModel.deleteMarka(name) },
                                onSimulateLocation = { lat, lng, inMill, name -> viewModel.simulateGpsLocation(lat, lng, inMill, name) },
                                onUpdateSettings = { mill, elec, print, timeout -> viewModel.updateSettings(mill, elec, print, timeout) },
                                onBackupJson = { viewModel.exportDatabaseBackupJson() },
                                onPublishAppUpdate = { code, name, url, notes, mandatory -> viewModel.publishAppUpdate(code, name, url, notes, mandatory) },
                                onClearAuditLogs = { viewModel.clearAuditLogs() }
                            )

                            else -> MainGateDashboard(
                                stats = stats,
                                lorries = filteredLorries.filter { it.status != com.example.data.model.LorryStatus.COMPLETED.name && it.outTime.isNullOrEmpty() },
                                searchQuery = searchQuery,
                                onSearchChange = { viewModel.searchQuery.value = it },
                                onNewEntryClick = { roleNavController.navigate("new_entry") },
                                onViewAllPendingClick = { roleNavController.navigate("pending") },
                                onLorryClick = { lorry ->
                                    val statusEnum = com.example.data.model.LorryStatus.fromString(lorry.status)
                                    val isAllowed = statusEnum == com.example.data.model.LorryStatus.READY_FOR_GATE_EXIT ||
                                            statusEnum == com.example.data.model.LorryStatus.COMPLETED ||
                                            statusEnum == com.example.data.model.LorryStatus.ELECTRIC_TARE_DONE ||
                                            lorry.hasTareRecorded || lorry.unloaded || !lorry.outTime.isNullOrEmpty()
                                    if (isAllowed) {
                                        activeGateOutLorry = lorry
                                        roleNavController.navigate("gate_out")
                                    } else {
                                        viewModel.showToast("Lorry is currently at stage: ${lorry.currentStage}. Gate Exit is permitted when status is Ready for Gate Exit.")
                                    }
                                },
                                onDaysInsideText = { viewModel.getDaysInsideText(it) },
                                currentUserRole = currentUserRole
                            )
                        }
                    }

                    composable("new_entry") {
                        if (currentUserRole == UserRole.MAIN_GATE || currentUserRole == UserRole.SUPER_ADMIN) {
                            NewGateEntryScreen(
                                generatedGatePass = viewModel.generateGatePassNumber(),
                                brokersList = brokersList,
                                qualitiesList = qualitiesList,
                                mokamsList = mokamsList,
                                markasList = markasList,
                                onSaveClick = { lorryNo, chalan, party, desc, qty, unit, gross, tare, items, mokam, marka, dept, customGatePass ->
                                    viewModel.saveGateEntry(lorryNo, chalan, party, desc, qty, unit, gross, tare, items, mokam, marka, dept, customGatePass)
                                    currentNavRoute = "dashboard"
                                    roleNavController.popBackStack("dashboard", false)
                                },
                                onBackClick = {
                                    roleNavController.popBackStack()
                                }
                            )
                        } else {
                            AccessDeniedCard(
                                role = currentUserRole,
                                requiredRole = "Main Gate or Super Admin",
                                onGoHome = {
                                    currentNavRoute = "dashboard"
                                    roleNavController.navigate("dashboard")
                                }
                            )
                        }
                    }

                    composable("mill_weight") {
                        if (currentUserRole == UserRole.MILL_WEIGHTMENT || currentUserRole == UserRole.SUPER_ADMIN) {
                            MillWeightmentDashboard(
                                lorries = allLorries.filter { it.effectiveDepartment.equals("Jute", ignoreCase = true) && it.status != com.example.data.model.LorryStatus.COMPLETED.name && it.outTime.isNullOrEmpty() },
                                searchQuery = searchQuery,
                                onSearchChange = { viewModel.searchQuery.value = it },
                                onSubmitGrossWeight = { pass, w, party, chalan, mokam, marka, desc, tareWt, totalQty, unit, qualityItems, lorryNum, chalanGross ->
                                    viewModel.submitMillGrossWeight(pass, w, party, chalan, mokam, marka, desc, tareWt, totalQty, unit, qualityItems, lorryNum, chalanGross)
                                },
                                onSubmitTareWeight = { pass, w -> viewModel.submitMillTareWeight(pass, w) },
                                currentUserRole = currentUserRole
                            )
                        } else {
                            AccessDeniedCard(
                                role = currentUserRole,
                                requiredRole = "Mill Weightment or Super Admin",
                                onGoHome = {
                                    currentNavRoute = "dashboard"
                                    roleNavController.navigate("dashboard")
                                }
                            )
                        }
                    }

                    composable("electric_weight") {
                        if (currentUserRole == UserRole.ELECTRIC_WEIGHTMENT || currentUserRole == UserRole.SUPER_ADMIN) {
                            ElectricWeightmentDashboard(
                                lorries = allLorries.filter { it.effectiveDepartment.equals("Jute", ignoreCase = true) && it.status != com.example.data.model.LorryStatus.COMPLETED.name && it.outTime.isNullOrEmpty() },
                                searchQuery = searchQuery,
                                onSearchChange = { viewModel.searchQuery.value = it },
                                onSubmitElectricGross = { pass, w -> viewModel.submitElectricGrossWeight(pass, w) },
                                onMarkUnloaded = { pass -> viewModel.markUnloaded(pass) },
                                onSubmitElectricTare = { pass, w -> viewModel.submitElectricTareWeight(pass, w) },
                                onSubmitElectricWeights = { pass, eg, et -> viewModel.submitElectricWeights(pass, eg, et) },
                                currentUserRole = currentUserRole
                            )
                        } else {
                            AccessDeniedCard(
                                role = currentUserRole,
                                requiredRole = "Electric Weightment or Super Admin",
                                onGoHome = {
                                    currentNavRoute = "dashboard"
                                    roleNavController.navigate("dashboard")
                                }
                            )
                        }
                    }

                    composable("pending") {
                        val roleFilteredLorries = remember(filteredLorries, currentUserRole) {
                            when (currentUserRole) {
                                UserRole.MILL_WEIGHTMENT, UserRole.ELECTRIC_WEIGHTMENT -> {
                                    filteredLorries.filter { it.effectiveDepartment.equals("Jute", ignoreCase = true) }
                                }
                                UserRole.STORE -> {
                                    filteredLorries.filter { it.effectiveDepartment.equals("Store", ignoreCase = true) }
                                }
                                UserRole.FINISH_GOOD -> {
                                    filteredLorries.filter { it.effectiveDepartment.equals("Finish Good", ignoreCase = true) }
                                }
                                UserRole.OTHER -> {
                                    filteredLorries.filter { it.effectiveDepartment.equals("Other", ignoreCase = true) }
                                }
                                else -> filteredLorries
                            }
                        }
                        PendingLorriesScreen(
                            lorries = roleFilteredLorries,
                            searchQuery = searchQuery,
                            onSearchChange = { viewModel.searchQuery.value = it },
                            activeFilter = filterStatus,
                            onFilterChange = { viewModel.filterStatus.value = it },
                            currentUserRole = currentUserRole,
                            onLorryClick = { lorry ->
                                when (currentUserRole) {
                                    UserRole.MILL_WEIGHTMENT -> {
                                        currentNavRoute = "dashboard"
                                        roleNavController.navigate("dashboard")
                                    }
                                    UserRole.ELECTRIC_WEIGHTMENT -> {
                                        currentNavRoute = "dashboard"
                                        roleNavController.navigate("dashboard")
                                    }
                                    UserRole.MAIN_GATE -> {
                                        val statusEnum = com.example.data.model.LorryStatus.fromString(lorry.status)
                                        val isAllowed = statusEnum == com.example.data.model.LorryStatus.READY_FOR_GATE_EXIT ||
                                                statusEnum == com.example.data.model.LorryStatus.COMPLETED ||
                                                statusEnum == com.example.data.model.LorryStatus.ELECTRIC_TARE_DONE ||
                                                lorry.hasTareRecorded || lorry.unloaded || !lorry.outTime.isNullOrEmpty()
                                        if (isAllowed) {
                                            activeGateOutLorry = lorry
                                            roleNavController.navigate("gate_out")
                                        } else {
                                            viewModel.showToast("Lorry is currently at stage: ${lorry.currentStage}. Gate Exit clearance is permitted when status is Ready for Gate Exit.")
                                        }
                                    }
                                    UserRole.SUPER_ADMIN -> {
                                        val statusEnum = com.example.data.model.LorryStatus.fromString(lorry.status)
                                        val isAllowed = statusEnum == com.example.data.model.LorryStatus.READY_FOR_GATE_EXIT ||
                                                statusEnum == com.example.data.model.LorryStatus.COMPLETED ||
                                                statusEnum == com.example.data.model.LorryStatus.ELECTRIC_TARE_DONE ||
                                                lorry.hasTareRecorded || lorry.unloaded || !lorry.outTime.isNullOrEmpty()
                                        if (isAllowed) {
                                            activeGateOutLorry = lorry
                                            roleNavController.navigate("gate_out")
                                        } else if (statusEnum == com.example.data.model.LorryStatus.ELECTRIC_GROSS_DONE || statusEnum == com.example.data.model.LorryStatus.WAITING_FOR_UNLOADING) {
                                            currentNavRoute = "electric_weight"
                                            roleNavController.navigate("electric_weight")
                                        } else {
                                            currentNavRoute = "mill_weight"
                                            roleNavController.navigate("mill_weight")
                                        }
                                    }
                                    else -> {}
                                }
                            },
                            onDeleteLorry = { pass -> viewModel.deleteLorry(pass) },
                            onDaysInsideText = { viewModel.getDaysInsideText(it) }
                        )
                    }

                    composable("gate_out") {
                        if (currentUserRole == UserRole.MAIN_GATE || currentUserRole == UserRole.SUPER_ADMIN) {
                            activeGateOutLorry?.let { selectedLorry ->
                                val liveLorry = allLorries.find { it.gatePass == selectedLorry.gatePass || (it.lorryNumber.isNotBlank() && it.lorryNumber.equals(selectedLorry.lorryNumber, ignoreCase = true)) } ?: selectedLorry
                                GateOutScreen(
                                    lorry = liveLorry,
                                    onMarkOutClick = { pass, remarks ->
                                        viewModel.markGateOut(pass, remarks)
                                        roleNavController.popBackStack()
                                    },
                                    onBackClick = {
                                        roleNavController.popBackStack()
                                    }
                                )
                            }
                        } else {
                            AccessDeniedCard(
                                role = currentUserRole,
                                requiredRole = "Main Gate or Super Admin",
                                onGoHome = {
                                    currentNavRoute = "dashboard"
                                    roleNavController.navigate("dashboard")
                                }
                            )
                        }
                    }

                    composable("admin") {
                        if (currentUserRole == UserRole.SUPER_ADMIN) {
                            SuperAdminDashboard(
                                stats = stats,
                                geoFenceState = geoFenceState,
                                securityState = securityState,
                                auditLogs = auditLogs,
                                allLorries = allLorries,
                                appUsers = appUsers,
                                systemSettings = systemSettings,
                                brokers = brokersList,
                                qualities = qualitiesList,
                                mokams = mokamsList,
                                markas = markasList,
                                onToggleGeoFenceOverride = { viewModel.toggleGeoFenceOverride() },
                                onToggleGeofenceEnforcement = { enabled -> viewModel.toggleGeofenceEnforcement(enabled) },
                                onUpdateGpsLocation = { lat, lng, dist, isInside, name -> viewModel.updateGoogleLocationServicesGps(lat, lng, dist, isInside, name) },
                                onSyncOffline = { viewModel.syncOfflineData() },
                                onExportReport = { format ->
                                    viewModel.showToast("$format Report exported successfully to Downloads folder")
                                },
                                onShowToast = { viewModel.showToast(it) },
                                onAddUser = { name, role -> viewModel.addUser(name, role) },
                                onToggleUserStatus = { name -> viewModel.toggleUserStatus(name) },
                                onDeleteUser = { name -> viewModel.deleteUser(name) },
                                onAddBroker = { name -> viewModel.addBroker(name) },
                                onDeleteBroker = { name -> viewModel.deleteBroker(name) },
                                onAddQuality = { name -> viewModel.addQuality(name) },
                                onDeleteQuality = { name -> viewModel.deleteQuality(name) },
                                onAddMokam = { name -> viewModel.addMokam(name) },
                                onDeleteMokam = { name -> viewModel.deleteMokam(name) },
                                onAddMarka = { name -> viewModel.addMarka(name) },
                                onDeleteMarka = { name -> viewModel.deleteMarka(name) },
                                onSimulateLocation = { lat, lng, inMill, name -> viewModel.simulateGpsLocation(lat, lng, inMill, name) },
                                onUpdateSettings = { mill, elec, print, timeout -> viewModel.updateSettings(mill, elec, print, timeout) },
                                onBackupJson = { viewModel.exportDatabaseBackupJson() },
                                onPublishAppUpdate = { code, name, url, notes, mandatory -> viewModel.publishAppUpdate(code, name, url, notes, mandatory) },
                                onClearAuditLogs = { viewModel.clearAuditLogs() }
                            )
                        } else {
                            AccessDeniedCard(
                                role = currentUserRole,
                                requiredRole = "Super Admin",
                                onGoHome = {
                                    currentNavRoute = "dashboard"
                                    roleNavController.navigate("dashboard")
                                }
                            )
                        }
                    }
                }
            }
        }

                if (showGeoFenceDialog) {
                    GeoFenceRestrictionDialog(
                        onSuperAdminOverrideClick = {
                            viewModel.toggleGeoFenceOverride()
                            showGeoFenceDialog = false
                        },
                        onDismissRequest = { showGeoFenceDialog = false }
                    )
                }
            }
        }
    }
}
}

@Composable
fun AccessDeniedCard(
    role: UserRole?,
    requiredRole: String,
    onGoHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Access Denied",
                    modifier = Modifier.size(64.dp),
                    tint = StatusRed
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Access Restricted",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your current role (${role?.title ?: "Unknown"}) does not have permission to access this section. Required role: $requiredRole.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onGoHome,
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Return to Dashboard", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun OtaUpdateBannerCard(
    updateDto: AppUpdateDto,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = {
            if (!updateDto.isMandatory) {
                onDismiss()
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = null,
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "New Software Update Available",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Version: v${updateDto.versionName} (Build ${updateDto.versionCode})",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF64748B)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (updateDto.isMandatory) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "⚠️ Mandatory Update Required",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                if (!updateDto.releaseNotes.isNullOrEmpty()) {
                    Text(
                        text = "Release Notes:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = updateDto.releaseNotes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF334155)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rawUrl = updateDto.downloadUrl.trim()
                    val targetUrl = if (rawUrl.isNotEmpty()) rawUrl else "https://github.com/mis-cell/weight_bridge/raw/main/app-debug.apk"
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open download link: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Update Now", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (!updateDto.isMandatory) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Later")
                }
            }
        }
    )
}
