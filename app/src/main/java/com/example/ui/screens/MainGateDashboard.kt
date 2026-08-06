package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LorryWeighment
import com.example.data.model.UserRole
import com.example.ui.components.AirportQueueBoardCard
import com.example.ui.components.AiCommandSearchBar
import com.example.ui.components.CommandHeaderCard
import com.example.ui.components.DigitalTwinMillMapCard
import com.example.ui.components.IncidentReportDialog
import com.example.ui.components.KanbanBoardView
import com.example.ui.components.LorryItemCard
import com.example.ui.components.ShiftHandoverDialog
import com.example.ui.components.SmartQuickActionFab
import com.example.ui.theme.IndustrialBlue
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.DashboardStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainGateDashboard(
    stats: DashboardStats,
    lorries: List<LorryWeighment>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onNewEntryClick: () -> Unit,
    onViewAllPendingClick: () -> Unit,
    onLorryClick: (LorryWeighment) -> Unit,
    onDaysInsideText: (Long) -> String,
    currentUserRole: UserRole? = null,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showHandoverDialog by remember { mutableStateOf(false) }
    var showIncidentDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Command Header Card
            item {
                CommandHeaderCard(stats = stats)
            }

            // AI Natural Search Command Bar
            item {
                AiCommandSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchChange
                )
            }

            // Tab Navigation Selector
            item {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = IndustrialBlue
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("📊 Overview Map", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("🛫 Live Board", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = { Text("📋 Kanban Queue", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }

            // Tab Contents
            if (selectedTabIndex == 0) {
                // Digital Twin Map & Stats Cards
                item {
                    DigitalTwinMillMapCard(
                        stats = stats,
                        onNodeClick = { node -> onSearchChange(node) }
                    )
                }

                item {
                    // Grid of 4 Stat Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Today's Entry",
                            value = stats.todayEntries.toString(),
                            icon = Icons.Default.LocalShipping,
                            bgColor = StatusBlue,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Pending Queue",
                            value = stats.pendingCount.toString(),
                            icon = Icons.Default.PendingActions,
                            bgColor = StatusOrange,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Completed",
                            value = stats.completedCount.toString(),
                            icon = Icons.Default.CheckCircle,
                            bgColor = StatusGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Overdue (>24h)",
                            value = stats.overdueCount.toString(),
                            icon = Icons.Default.Warning,
                            bgColor = StatusRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else if (selectedTabIndex == 1) {
                // Airport Display Queue Board
                item {
                    AirportQueueBoardCard(stats = stats)
                }
            } else if (selectedTabIndex == 2) {
                // Kanban View
                item {
                    KanbanBoardView(
                        lorries = lorries,
                        onLorryClick = onLorryClick
                    )
                }
            }

            // Pending Lorries List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Real-Time Lorries Queue (${lorries.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onViewAllPendingClick) {
                        Text("View All (${stats.pendingCount})", color = IndustrialBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // List of Pending Lorries
            if (lorries.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "🎉 No Queue Bottleneck",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "All lorries are progressing smoothly",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(lorries.take(10)) { lorry ->
                    LorryItemCard(
                        lorry = lorry,
                        daysInsideText = onDaysInsideText(lorry.createdAt),
                        currentUserRole = currentUserRole,
                        onActionClick = { onLorryClick(lorry) }
                    )
                }
            }
        }

        // Smart FAB Speed Dial
        SmartQuickActionFab(
            onNewEntryClick = onNewEntryClick,
            onScanQrClick = { onSearchChange("BJM-2026") },
            onShiftHandoverClick = { showHandoverDialog = true },
            onReportIncidentClick = { showIncidentDialog = true },
            onSyncClick = { onViewAllPendingClick() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )

        // Dialog Modals
        if (showHandoverDialog) {
            ShiftHandoverDialog(
                onDismiss = { showHandoverDialog = false },
                onSubmit = { notes -> showHandoverDialog = false }
            )
        }

        if (showIncidentDialog) {
            IncidentReportDialog(
                onDismiss = { showIncidentDialog = false },
                onSubmit = { category, details -> showIncidentDialog = false }
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.height(96.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

