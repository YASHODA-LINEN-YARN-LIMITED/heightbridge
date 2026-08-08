package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhonelinkLock
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.util.ReportExporter
import com.example.util.LocationHelper
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AuditLogEntry
import com.example.data.model.LorryWeighment
import com.example.data.model.UserRole
import com.example.ui.components.AirportQueueBoardCard
import com.example.ui.components.CommandHeaderCard
import com.example.ui.components.DigitalTwinMillMapCard
import com.example.ui.theme.IndustrialBlue
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusPurple
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.AppUser
import com.example.ui.viewmodel.DashboardStats
import com.example.ui.viewmodel.GeoFenceState
import com.example.ui.viewmodel.SecurityState
import com.example.ui.viewmodel.SystemSettingsState

data class AdminActionModule(
    val title: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminDashboard(
    stats: DashboardStats,
    geoFenceState: GeoFenceState,
    securityState: SecurityState = SecurityState(),
    auditLogs: List<AuditLogEntry> = emptyList(),
    allLorries: List<LorryWeighment> = emptyList(),
    appUsers: List<AppUser> = emptyList(),
    systemSettings: SystemSettingsState = SystemSettingsState(),
    brokers: List<String> = emptyList(),
    qualities: List<String> = emptyList(),
    mokams: List<String> = emptyList(),
    markas: List<String> = emptyList(),
    onToggleGeoFenceOverride: () -> Unit,
    onToggleGeofenceEnforcement: (Boolean) -> Unit = {},
    onUpdateGpsLocation: (Double, Double, Float, Boolean, String) -> Unit = { _, _, _, _, _ -> },
    onSyncOffline: () -> Unit = {},
    onExportReport: (String) -> Unit,
    onShowToast: (String) -> Unit,
    onAddUser: (String, UserRole) -> Unit = { _, _ -> },
    onToggleUserStatus: (String) -> Unit = {},
    onDeleteUser: (String) -> Unit = {},
    onAddBroker: (String) -> Unit = {},
    onDeleteBroker: (String) -> Unit = {},
    onAddQuality: (String) -> Unit = {},
    onDeleteQuality: (String) -> Unit = {},
    onAddMokam: (String) -> Unit = {},
    onDeleteMokam: (String) -> Unit = {},
    onAddMarka: (String) -> Unit = {},
    onDeleteMarka: (String) -> Unit = {},
    onSimulateLocation: (Double, Double, Boolean, String) -> Unit = { _, _, _, _ -> },
    onUpdateSettings: (Double, Double, Boolean, Int, Boolean) -> Unit = { _, _, _, _, _ -> },
    onBackupJson: () -> Unit = {},
    onPublishAppUpdate: (Int, String, String, String, Boolean) -> Unit = { _, _, _, _, _ -> },
    onClearAuditLogs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeModuleDialog by remember { mutableStateOf<String?>(null) }

    val modules = listOf(
        AdminActionModule("Reports", Icons.Default.Assessment, StatusBlue),
        AdminActionModule("Analytics", Icons.Default.Analytics, StatusGreen),
        AdminActionModule("Masters", Icons.Default.Category, StatusOrange),
        AdminActionModule("Users", Icons.Default.Group, IndustrialBlue),
        AdminActionModule("Audit Logs", Icons.Default.History, StatusRed),
        AdminActionModule("Backup", Icons.Default.Backup, IndustrialBlue),
        AdminActionModule("Settings", Icons.Default.Settings, Color.Gray)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Banner
            item {
                CommandHeaderCard(stats = stats, onSyncClick = onSyncOffline)
            }

            item {
                DigitalTwinMillMapCard(stats = stats)
            }

            item {
                AirportQueueBoardCard(stats = stats)
            }

            // Security Features Status Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = StatusPurple)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Security & Integrity Shield",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF111827)
                                )
                            }
                            Button(
                                onClick = onSyncOffline,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync Data", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SecurityBadgeItem(
                                icon = Icons.Default.Security,
                                label = "Screenshot & Recording Protection",
                                statusText = "OFF (Allowed)",
                                color = StatusBlue
                            )
                            SecurityBadgeItem(
                                icon = Icons.Default.PhonelinkLock,
                                label = "Device Root Protection Check",
                                statusText = if (securityState.isDeviceRooted) "ROOTED (Warning)" else "PASSED",
                                color = if (securityState.isDeviceRooted) StatusRed else StatusGreen
                            )
                            SecurityBadgeItem(
                                icon = Icons.Default.Timer,
                                label = "Auto Logout Inactivity Guard",
                                statusText = "${systemSettings.inactivityTimeoutMinutes} min timeout",
                                color = StatusBlue
                            )
                            SecurityBadgeItem(
                                icon = Icons.Default.CloudDone,
                                label = "Cloud Sync & Encryption",
                                statusText = "Supabase Connected",
                                color = IndustrialBlue
                            )
                        }
                    }
                }
            }

            // Analytics Overview
            item {
                Column {
                    Text(
                        text = "Analytics Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricChip("Total Entries", stats.todayEntries.toString(), StatusBlue, Modifier.weight(1f))
                        MetricChip("Total Exits", stats.todayExits.toString(), StatusGreen, Modifier.weight(1f))
                        MetricChip("Pending", stats.pendingCount.toString(), StatusOrange, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricChip("Overdue", stats.overdueCount.toString(), StatusRed, Modifier.weight(1f))
                        MetricChip("Avg Turnaround", "${stats.avgTurnaroundHrs} Hrs", IndustrialBlue, Modifier.weight(1f))
                        MetricChip("Vehicles Inside", stats.vehiclesInside.toString(), StatusOrange, Modifier.weight(1f))
                    }
                }
            }

            // GeoFence Settings Card (Google Location Services & Admin Control)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // 1. Primary Admin Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (geoFenceState.isGeofenceEnabled) IndustrialBlue.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = if (geoFenceState.isGeofenceEnabled) IndustrialBlue else Color.Gray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Google Location Services Geofencing",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color(0xFF111827)
                                    )
                                    Text(
                                        text = if (geoFenceState.isGeofenceEnabled) "GEOFENCE ENFORCED (ADMIN ONLY)" else "DISABLED BY ADMIN (Check-in anywhere)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (geoFenceState.isGeofenceEnabled) StatusGreen else Color.Gray
                                    )
                                }
                            }

                            Switch(
                                checked = geoFenceState.isGeofenceEnabled,
                                onCheckedChange = { onToggleGeofenceEnforcement(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = StatusGreen,
                                    checkedTrackColor = StatusGreen.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.testTag("switch_geofence_admin_master")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status Alert Box
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    !geoFenceState.isGeofenceEnabled -> Color(0xFFF1F5F9)
                                    geoFenceState.superAdminOverride -> Color(0xFFFEF3C7)
                                    geoFenceState.isInsideBallyJuteMill -> Color(0xFFDCFCE7)
                                    else -> Color(0xFFFEE2E2)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = when {
                                        !geoFenceState.isGeofenceEnabled -> "Geofencing OFF — Lorry check-in & weighment allowed anywhere (Turned OFF by Admin)."
                                        geoFenceState.superAdminOverride -> "ADMIN BYPASS OVERRIDE ACTIVE — Restrictions bypassed for all lorries."
                                        geoFenceState.isInsideBallyJuteMill -> "IN MILL PREMISES — Lorry is within Bally Jute Mill premises (${geoFenceState.distanceFromMillMeters.toInt()}m from center)."
                                        else -> "OUTSIDE PREMISES RESTRICTED — Lorry is outside Bally Jute Mill (${geoFenceState.distanceFromMillMeters.toInt()}m away). Check-in/out blocked!"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when {
                                        !geoFenceState.isGeofenceEnabled -> Color(0xFF475569)
                                        geoFenceState.superAdminOverride -> Color(0xFF92400E)
                                        geoFenceState.isInsideBallyJuteMill -> Color(0xFF166534)
                                        else -> StatusRed
                                    }
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Location: ${geoFenceState.locationName} • Lat: ${geoFenceState.latitude}, Lng: ${geoFenceState.longitude}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Google Location Services Fetch Action
                        Button(
                            onClick = {
                                LocationHelper.fetchCurrentLocation(
                                    context = context,
                                    radiusMeters = geoFenceState.radiusMeters,
                                    onSuccess = { lat, lng, dist, isInside, locName ->
                                        onUpdateGpsLocation(lat, lng, dist, isInside, locName)
                                    },
                                    onError = { err ->
                                        onShowToast(err)
                                    }
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndustrialBlue),
                            modifier = Modifier.fillMaxWidth().testTag("btn_fetch_google_location")
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fetch Live Location via Google Services")
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Admin Emergency Override Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Admin Emergency Bypass Override",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFF334155)
                                )
                                Text(
                                    text = "Temporarily allow check-in when GPS signal is lost",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Switch(
                                checked = geoFenceState.superAdminOverride,
                                onCheckedChange = { onToggleGeoFenceOverride() },
                                colors = SwitchDefaults.colors(checkedThumbColor = StatusOrange)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Test Coordinate Simulation Buttons
                        Text(
                            text = "Test Device Location Simulation:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onSimulateLocation(22.6500, 88.3400, true, "Bally Jute Mill Gate, Bally, Howrah")
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, StatusGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Inside Bally Mill", color = StatusGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    onSimulateLocation(22.5851, 88.3468, false, "Outside Mill - Howrah Bridge, Kolkata")
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, StatusRed),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Outside Geofence", color = StatusRed, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // System Administration Modules Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "System Administration Modules",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    modules.chunked(2).forEach { rowModules ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowModules.forEach { mod ->
                                Card(
                                    onClick = { activeModuleDialog = mod.title },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(76.dp)
                                        .testTag("admin_mod_${mod.title.lowercase().replace(" ", "_")}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(mod.color.copy(alpha = 0.15f))
                                        ) {
                                            Icon(
                                                imageVector = mod.icon,
                                                contentDescription = null,
                                                tint = mod.color,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = mod.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF111827)
                                            )
                                            Text(
                                                text = "Tap to open",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }
                            }
                            if (rowModules.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Quick Export Reports Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Quick Statement Downloads",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "Export Party-wise, Quality-wise, Broker-wise Excel/PDF statements",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    val file = ReportExporter.generateCsvReport(context, allLorries)
                                    if (file != null) {
                                        onExportReport("Excel / CSV Lorry Ledger")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_export_excel")
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export Excel")
                            }

                            Button(
                                onClick = {
                                    val file = ReportExporter.generatePdfShiftLedger(context, allLorries)
                                    if (file != null) {
                                        onExportReport("PDF Shift Ledger")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_export_pdf")
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export PDF")
                            }
                        }
                    }
                }
            }
        }

        // Active Administrative Module Dialog / Screen
        activeModuleDialog?.let { moduleName ->
            AdminModuleDialog(
                moduleName = moduleName,
                stats = stats,
                allLorries = allLorries,
                appUsers = appUsers,
                auditLogs = auditLogs,
                systemSettings = systemSettings,
                brokers = brokers,
                qualities = qualities,
                mokams = mokams,
                markas = markas,
                geoFenceState = geoFenceState,
                onDismiss = { activeModuleDialog = null },
                onExportReport = onExportReport,
                onAddUser = onAddUser,
                onToggleUserStatus = onToggleUserStatus,
                onDeleteUser = onDeleteUser,
                onAddBroker = onAddBroker,
                onDeleteBroker = onDeleteBroker,
                onAddQuality = onAddQuality,
                onDeleteQuality = onDeleteQuality,
                onAddMokam = onAddMokam,
                onDeleteMokam = onDeleteMokam,
                onAddMarka = onAddMarka,
                onDeleteMarka = onDeleteMarka,
                onUpdateSettings = onUpdateSettings,
                onBackupJson = onBackupJson,
                onPublishAppUpdate = onPublishAppUpdate,
                onClearAuditLogs = onClearAuditLogs,
                onSyncOffline = onSyncOffline
            )
        }
    }
}

@Composable
fun AdminModuleDialog(
    moduleName: String,
    stats: DashboardStats,
    allLorries: List<LorryWeighment>,
    appUsers: List<AppUser>,
    auditLogs: List<AuditLogEntry>,
    systemSettings: SystemSettingsState,
    brokers: List<String>,
    qualities: List<String>,
    mokams: List<String>,
    markas: List<String>,
    geoFenceState: GeoFenceState,
    onDismiss: () -> Unit,
    onExportReport: (String) -> Unit,
    onAddUser: (String, UserRole) -> Unit,
    onToggleUserStatus: (String) -> Unit,
    onDeleteUser: (String) -> Unit,
    onAddBroker: (String) -> Unit,
    onDeleteBroker: (String) -> Unit,
    onAddQuality: (String) -> Unit,
    onDeleteQuality: (String) -> Unit,
    onAddMokam: (String) -> Unit,
    onDeleteMokam: (String) -> Unit,
    onAddMarka: (String) -> Unit,
    onDeleteMarka: (String) -> Unit,
    onUpdateSettings: (Double, Double, Boolean, Int, Boolean) -> Unit,
    onBackupJson: () -> Unit,
    onPublishAppUpdate: (Int, String, String, String, Boolean) -> Unit = { _, _, _, _, _ -> },
    onClearAuditLogs: () -> Unit,
    onSyncOffline: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF8FAFC)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = IndustrialBlue.copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (moduleName) {
                                        "Reports" -> Icons.Default.Assessment
                                        "Analytics" -> Icons.Default.Analytics
                                        "Masters" -> Icons.Default.Category
                                        "Users" -> Icons.Default.Group
                                        "Audit Logs" -> Icons.Default.History
                                        "Backup" -> Icons.Default.Backup
                                        else -> Icons.Default.Settings
                                    },
                                    contentDescription = null,
                                    tint = IndustrialBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = moduleName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Divider(color = Color(0xFFE2E8F0))

                // Module Content Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    when (moduleName) {
                        "Reports" -> ReportsModuleView(allLorries = allLorries, onExportReport = onExportReport)
                        "Analytics" -> AnalyticsModuleView(stats = stats, allLorries = allLorries)
                        "Masters" -> MastersModuleView(
                            brokers = brokers,
                            qualities = qualities,
                            mokams = mokams,
                            markas = markas,
                            onAddBroker = onAddBroker,
                            onDeleteBroker = onDeleteBroker,
                            onAddQuality = onAddQuality,
                            onDeleteQuality = onDeleteQuality,
                            onAddMokam = onAddMokam,
                            onDeleteMokam = onDeleteMokam,
                            onAddMarka = onAddMarka,
                            onDeleteMarka = onDeleteMarka
                        )
                        "Users" -> UsersModuleView(
                            appUsers = appUsers,
                            onAddUser = onAddUser,
                            onToggleUserStatus = onToggleUserStatus,
                            onDeleteUser = onDeleteUser
                        )
                        "Audit Logs" -> AuditLogsModuleView(auditLogs = auditLogs, onClearLogs = onClearAuditLogs)
                        "Backup" -> BackupModuleView(
                            totalLorries = allLorries.size,
                            onSyncOffline = onSyncOffline,
                            onBackupJson = onBackupJson,
                            onPublishAppUpdate = onPublishAppUpdate
                        )
                        "Settings" -> SettingsModuleView(
                            systemSettings = systemSettings,
                            geoFenceState = geoFenceState,
                            onUpdateSettings = onUpdateSettings
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 1. REPORTS MODULE VIEW
// ----------------------------------------------------
@Composable
fun ReportsModuleView(
    allLorries: List<LorryWeighment>,
    onExportReport: (String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedDepartmentFilter by remember { mutableStateOf("All") }

    val filteredList = remember(allLorries, searchQuery, selectedDepartmentFilter) {
        allLorries.filter { lorry ->
            val matchesDept = if (selectedDepartmentFilter == "All") true else lorry.effectiveDepartment.equals(selectedDepartmentFilter, ignoreCase = true)
            val matchesQuery = searchQuery.isEmpty() ||
                    lorry.lorryNumber.contains(searchQuery, ignoreCase = true) ||
                    lorry.gatePass.contains(searchQuery, ignoreCase = true) ||
                    lorry.party.contains(searchQuery, ignoreCase = true)
            matchesDept && matchesQuery
        }
    }

    val totalNetWtKg = filteredList.sumOf { lorry ->
        lorry.lowestNetWeight ?: ((lorry.millGrossWeight ?: lorry.electricGrossWeight ?: lorry.grossWeight ?: 0.0) - (lorry.millTareWeight ?: lorry.electricTareWeight ?: lorry.tareWeight ?: 0.0)).coerceAtLeast(0.0)
    }
    val totalNetMt = Math.round((totalNetWtKg / 1000.0) * 10.0) / 10.0

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Search & Filter Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter by Party, Lorry No, Gate Entry No...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // Department Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("All", "Jute", "Store", "Finish Good", "Other").forEach { dept ->
                androidx.compose.material3.FilterChip(
                    selected = selectedDepartmentFilter == dept,
                    onClick = { selectedDepartmentFilter = dept },
                    label = { Text(dept, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        // Stat Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricChip("Matched Records", filteredList.size.toString(), StatusBlue, Modifier.weight(1f))
            MetricChip("Total Net Weight", "$totalNetMt MT", StatusGreen, Modifier.weight(1f))
        }

        // Export Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val file = ReportExporter.generateCsvReport(context, filteredList)
                    if (file != null) {
                        onExportReport("CSV / Excel Accounting Ledger")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export CSV / Excel")
            }

            Button(
                onClick = {
                    val file = ReportExporter.generatePdfShiftLedger(context, filteredList)
                    if (file != null) {
                        onExportReport("PDF Shift Ledger")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export PDF Ledger")
            }
        }

        // Report Data List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (filteredList.isEmpty()) {
                item {
                    Text(
                        text = "No lorry weighment records found matching criteria.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(filteredList) { lorry ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = lorry.gatePass,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = IndustrialBlue
                                )
                                Text(
                                    text = lorry.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val gateGross = lorry.grossWeight?.toInt()?.toString() ?: "N/A"
                            val millGross = lorry.millGrossWeight?.toInt()?.toString() ?: "N/A"
                            val elecGross = lorry.electricGrossWeight?.toInt()?.toString() ?: "N/A"
                            val elecTare = lorry.electricTareWeight?.toInt()?.toString() ?: "N/A"
                            val millTare = lorry.millTareWeight?.toInt()?.toString() ?: "N/A"
                            val net = (lorry.lowestNetWeight ?: 0.0).toInt()
                            Text(
                                text = "Lorry: ${lorry.lorryNumber} • Party: ${lorry.party.ifBlank { "N/A" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "Gate Gross: $gateGross kg | Mill Gross: $millGross kg | Elec Gross: $elecGross kg",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "Elec Tare: $elecTare kg | Mill Tare: $millTare kg | Net Wt: $net kg",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    val file = ReportExporter.generatePdfWeightReceipt(context, lorry)
                                    if (file != null) {
                                        onExportReport("PDF Weight Receipt for ${lorry.gatePass}")
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, IndustrialBlue),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, tint = IndustrialBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Download / Print PDF Weight Receipt", color = IndustrialBlue, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 2. ANALYTICS MODULE VIEW
// ----------------------------------------------------
@Composable
fun AnalyticsModuleView(
    stats: DashboardStats,
    allLorries: List<LorryWeighment>
) {
    val totalCount = allLorries.size
    val completedCount = allLorries.count { it.status == "COMPLETED" }
    val completionPercent = if (totalCount > 0) (completedCount * 100 / totalCount) else 100

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("System Throughput & Efficiency", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Overall Processing Efficiency", style = MaterialTheme.typography.bodySmall)
                        Text("$completionPercent%", fontWeight = FontWeight.Bold, color = StatusGreen)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = (completionPercent / 100f).coerceIn(0f, 1f),
                        color = StatusGreen,
                        trackColor = Color(0xFFDCFCE7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Average Turnaround Times", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    AnalyticsStageRow("Main Gate -> Mill Weighbridge", "18 Min", StatusBlue)
                    AnalyticsStageRow("Mill Gross -> Unloading Yard", "42 Min", StatusOrange)
                    AnalyticsStageRow("Unloading Yard Dwell Time", "1.2 Hrs", StatusPurple)
                    AnalyticsStageRow("Electric Weighbridge & Gate Exit", "15 Min", StatusGreen)
                }
            }
        }
    }
}

@Composable
fun AnalyticsStageRow(stage: String, duration: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stage, style = MaterialTheme.typography.bodySmall, color = Color(0xFF334155))
        }
        Text(text = duration, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

// ----------------------------------------------------
// 3. MASTERS MODULE VIEW
// ----------------------------------------------------
@Composable
fun MastersModuleView(
    brokers: List<String>,
    qualities: List<String>,
    mokams: List<String>,
    markas: List<String>,
    onAddBroker: (String) -> Unit,
    onDeleteBroker: (String) -> Unit,
    onAddQuality: (String) -> Unit,
    onDeleteQuality: (String) -> Unit,
    onAddMokam: (String) -> Unit,
    onDeleteMokam: (String) -> Unit,
    onAddMarka: (String) -> Unit,
    onDeleteMarka: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var newItemInput by remember { mutableStateOf("") }

    val tabs = listOf("Brokers (${brokers.size})", "Qualities (${qualities.size})", "Mokam (${mokams.size})", "Marka (${markas.size})")

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(title, fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newItemInput,
                onValueChange = { newItemInput = it },
                placeholder = { Text("Add new master item...") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            )

            Button(
                onClick = {
                    if (newItemInput.isNotBlank()) {
                        when (selectedTab) {
                            0 -> onAddBroker(newItemInput)
                            1 -> onAddQuality(newItemInput)
                            2 -> onAddMokam(newItemInput)
                            3 -> onAddMarka(newItemInput)
                        }
                        newItemInput = ""
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndustrialBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
        }

        val currentList = when (selectedTab) {
            0 -> brokers
            1 -> qualities
            2 -> mokams
            else -> markas
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(currentList) { item ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                        IconButton(
                            onClick = {
                                when (selectedTab) {
                                    0 -> onDeleteBroker(item)
                                    1 -> onDeleteQuality(item)
                                    2 -> onDeleteMokam(item)
                                    3 -> onDeleteMarka(item)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 4. USERS MODULE VIEW
// ----------------------------------------------------
@Composable
fun UsersModuleView(
    appUsers: List<AppUser>,
    onAddUser: (String, UserRole) -> Unit,
    onToggleUserStatus: (String) -> Unit,
    onDeleteUser: (String) -> Unit
) {
    var usernameInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.MAIN_GATE) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Add User Form
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Create New Operator Account", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = { usernameInput = it },
                    placeholder = { Text("Username...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (usernameInput.isNotBlank()) {
                                onAddUser(usernameInput, selectedRole)
                                usernameInput = ""
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add User")
                    }
                }
            }
        }

        // Active Users List
        Text("Configured System Users (${appUsers.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(appUsers) { user ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = user.username, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text(text = "Role: ${user.role.title} • ${user.lastLogin}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = user.isActive,
                                onCheckedChange = { onToggleUserStatus(user.username) },
                                colors = SwitchDefaults.colors(checkedThumbColor = StatusGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = { onDeleteUser(user.username) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = StatusRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 5. AUDIT LOGS MODULE VIEW
// ----------------------------------------------------
@Composable
fun AuditLogsModuleView(
    auditLogs: List<AuditLogEntry>,
    onClearLogs: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filteredLogs = remember(auditLogs, query) {
        if (query.isBlank()) auditLogs else auditLogs.filter {
            it.gatePass.contains(query, ignoreCase = true) ||
                    it.userRole.contains(query, ignoreCase = true) ||
                    it.details.contains(query, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search audit trail by Gate Entry No or Role...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Logged System Actions (${filteredLogs.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            OutlinedButton(onClick = onClearLogs, shape = RoundedCornerShape(8.dp)) {
                Text("Reset Logs", color = StatusRed, style = MaterialTheme.typography.labelSmall)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(filteredLogs) { log ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = log.userRole, fontWeight = FontWeight.Bold, color = IndustrialBlue, style = MaterialTheme.typography.labelMedium)
                            Text(text = log.timestamp, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = log.details, style = MaterialTheme.typography.bodySmall, color = Color(0xFF1E293B))
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 6. BACKUP & CLOUD SYNC MODULE VIEW
// ----------------------------------------------------
@Composable
fun BackupModuleView(
    totalLorries: Int,
    onSyncOffline: () -> Unit,
    onBackupJson: () -> Unit,
    onPublishAppUpdate: (versionCode: Int, versionName: String, downloadUrl: String, releaseNotes: String, isMandatory: Boolean) -> Unit = { _, _, _, _, _ -> }
) {
    var updateVersionCode by remember { mutableStateOf("2") }
    var updateVersionName by remember { mutableStateOf("1.1.0") }
    var updateDownloadUrl by remember { mutableStateOf("https://github.com/mis-cell/weight_bridge/raw/main/app-debug.apk") }
    var updateReleaseNotes by remember { mutableStateOf("New Update v1.1.0: Realtime multi-device sync improvements & security updates.") }
    var updateIsMandatory by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // OTA APP UPDATE PUBLISHER CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = StatusBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🚀 Realtime OTA App Update Publisher Engine", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Publish a new app version to Supabase. Every connected mobile device will instantly display the 'Update Available' banner!", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = updateVersionCode,
                        onValueChange = { updateVersionCode = it },
                        label = { Text("Version Code") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = updateVersionName,
                        onValueChange = { updateVersionName = it },
                        label = { Text("Version Name") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = updateDownloadUrl,
                    onValueChange = { updateDownloadUrl = it },
                    label = { Text("GitHub Release / Direct APK Download URL") },
                    placeholder = { Text("e.g. https://github.com/user/repo/releases/download/v1.1.0/app.apk") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "💡 Tip: Push your app to GitHub, create a GitHub Release with the .apk file attached, and paste the download link here!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF0284C7),
                    modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = updateReleaseNotes,
                    onValueChange = { updateReleaseNotes = it },
                    label = { Text("Release Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = updateIsMandatory,
                        onCheckedChange = { updateIsMandatory = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mandatory Update (Force prompt on devices)", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val code = updateVersionCode.toIntOrNull() ?: 2
                        onPublishAppUpdate(code, updateVersionName, updateDownloadUrl, updateReleaseNotes, updateIsMandatory)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Publish, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Push Update To All Mobile Devices Now", fontWeight = FontWeight.Bold)
                }
            }
        }
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = StatusGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Supabase Cloud PostgREST Sync", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Connected Table: public.lorry_weighments", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("Local Records: $totalLorries", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSyncOffline,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sync Local Records to Supabase Cloud")
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Backup, contentDescription = null, tint = IndustrialBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Local Database JSON Backup", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Generate encrypted offline JSON snapshot of all weighments & audit logs.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onBackupJson,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Offline Backup JSON")
                }
            }
        }
    }
}

// ----------------------------------------------------
// 7. SETTINGS MODULE VIEW
// ----------------------------------------------------
@Composable
fun SettingsModuleView(
    systemSettings: SystemSettingsState,
    geoFenceState: GeoFenceState,
    onUpdateSettings: (Double, Double, Boolean, Int, Boolean) -> Unit
) {
    var millOffset by remember { mutableStateOf(systemSettings.millZeroOffsetKg.toString()) }
    var elecOffset by remember { mutableStateOf(systemSettings.electricZeroOffsetKg.toString()) }
    var autoPrint by remember { mutableStateOf(systemSettings.autoPrintThermalReceipt) }
    var timeoutMin by remember { mutableStateOf(systemSettings.inactivityTimeoutMinutes) }
    var allowScreenCapture by remember { mutableStateOf(systemSettings.allowScreenCapture) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Weighbridge Zero-Offset Calibration", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = millOffset,
                        onValueChange = { millOffset = it },
                        label = { Text("Mill WB Offset (Kg)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = elecOffset,
                        onValueChange = { elecOffset = it },
                        label = { Text("Electric WB Offset (Kg)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto-Print Thermal Receipt Slip", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = autoPrint, onCheckedChange = { autoPrint = it }, colors = SwitchDefaults.colors(checkedThumbColor = IndustrialBlue))
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Allow Screen Capture & Screenshots", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "When enabled, screenshots and screen recordings are allowed. When disabled, they are restricted for security.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = allowScreenCapture,
                        onCheckedChange = { allowScreenCapture = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = IndustrialBlue)
                    )
                }
            }
        }

        Button(
            onClick = {
                val millVal = millOffset.toDoubleOrNull() ?: 0.0
                val elecVal = elecOffset.toDoubleOrNull() ?: 0.0
                onUpdateSettings(millVal, elecVal, autoPrint, timeoutMin, allowScreenCapture)
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save System Settings")
        }
    }
}

@Composable
fun SecurityBadgeItem(icon: ImageVector, label: String, statusText: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF334155))
        }
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun MetricChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
