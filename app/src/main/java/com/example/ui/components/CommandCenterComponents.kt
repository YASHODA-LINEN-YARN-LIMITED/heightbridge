package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.data.model.LorryStatus
import com.example.data.model.LorryWeighment
import com.example.ui.theme.IndustrialBlue
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusPurple
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.DashboardStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AI Natural Language Command Search Bar
 */
@Composable
fun AiCommandSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onVoiceSearchClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showVoiceDialog by remember { mutableStateOf(false) }
    val quickFilters = listOf("TD5 Waiting", "Overdue >24h", "Mill Pending", "Party: Jute India", "Completed Today")

    Column(modifier = modifier.fillMaxWidth()) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    onQueryChange("Show TD5 lorries waiting for unloading")
                }) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Ask AI Suggestions",
                        tint = StatusPurple,
                        modifier = Modifier.size(22.dp)
                    )
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_ai_command_bar")
                )
                IconButton(onClick = {
                    showVoiceDialog = true
                    onVoiceSearchClick?.invoke()
                }) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = IndustrialBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(quickFilters) { filter ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (query == filter) IndustrialBlue else Color(0xFFEFF6FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.clickable { onQueryChange(if (query == filter) "" else filter) }
                ) {
                    Text(
                        text = "⚡ $filter",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (query == filter) Color.White else Color(0xFF1E40AF),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    if (showVoiceDialog) {
        VoiceCommandModalDialog(
            onDismiss = { showVoiceDialog = false },
            onVoiceResult = { voiceQuery ->
                onQueryChange(voiceQuery)
                showVoiceDialog = false
            }
        )
    }
}

@Composable
fun VoiceCommandModalDialog(
    onDismiss: () -> Unit,
    onVoiceResult: (String) -> Unit
) {
    var isListening by remember { mutableStateOf(true) }
    var recognizedText by remember { mutableStateOf("Listening for operational commands...") }
    val sampleVoiceCommands = listOf(
        "Show TD5 lorries waiting for unloading",
        "Find WB39 vehicles overdue > 24 hours",
        "Show Mill Bridge pending queue",
        "Filter Party: Jute India",
        "Show completed entries today"
    )

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = IndustrialBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Voice Assistant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pulsing Mic Visualizer
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .background(color = Color(0xFFEFF6FF), shape = CircleShape)
                        .border(2.dp, IndustrialBlue, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Active Listening",
                        tint = IndustrialBlue,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isListening) "🎙️ Listening... Speak your query" else "Recognized Command:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = IndustrialBlue
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "\"$recognizedText\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Or tap a sample voice command:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    sampleVoiceCommands.forEach { cmd ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    recognizedText = cmd
                                    onVoiceResult(cmd)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = IndustrialBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(cmd, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (recognizedText.startsWith("Listening")) {
                            val randomCmd = sampleVoiceCommands.random()
                            onVoiceResult(randomCmd)
                        } else {
                            onVoiceResult(recognizedText)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Voice Query", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Command Center Header Stats Card with Live Target & Efficiency
 */
@Composable
fun CommandHeaderCard(
    stats: DashboardStats,
    onSyncClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val liveTimeStr = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()) }
    val todayTarget = if (stats.todayEntries > 0) Math.max(stats.todayEntries, 20) else 0
    val progress = if (todayTarget > 0) (stats.todayEntries.toFloat() / todayTarget).coerceIn(0f, 1f) else 0f
    val efficiency = if (stats.todayEntries > 0) {
        ((stats.todayExits.toDouble() / stats.todayEntries) * 100).coerceAtMost(100.0).toInt()
    } else if (stats.completedCount > 0) {
        100
    } else {
        0
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Status Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WbCloudy, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "⛅ Bally, Howrah", color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelMedium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1E293B)) {
                        Text(
                            text = "🟢 Live Cloud Sync • $liveTimeStr",
                            color = Color(0xFF4ADE80),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Processing Target & Efficiency Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Today's Processing Target", color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = if (todayTarget > 0) "${stats.todayEntries} / $todayTarget Lorries Processed" else "${stats.todayEntries} Lorries Processed Today", color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                }
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF166534)) {
                    Text(
                        text = "⚡ $efficiency% Efficiency",
                        color = Color(0xFF86EFAC),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                color = Color(0xFF3B82F6),
                trackColor = Color(0xFF334155),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color.Transparent, RoundedCornerShape(4.dp))
            )
        }
    }
}

/**
 * Digital Twin Mill Map - Visual representation of Bally Jute Mill
 */
@Composable
fun DigitalTwinMillMapCard(
    stats: DashboardStats,
    onNodeClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = IndustrialBlue)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Digital Twin Mill Map",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }
                Text(
                    text = "Live Live Nodes",
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Schematic Nodes Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MapNodeItem(title = "Gate", icon = Icons.Default.LocalShipping, count = stats.waitingForGate, color = StatusBlue) { onNodeClick("Gate") }
                Text(text = "➔", color = Color.Gray, fontWeight = FontWeight.Bold)
                MapNodeItem(title = "Mill Bridge", icon = Icons.Default.Scale, count = stats.waitingForMill, color = StatusOrange) { onNodeClick("Mill Bridge") }
                Text(text = "➔", color = Color.Gray, fontWeight = FontWeight.Bold)
                MapNodeItem(title = "Electric", icon = Icons.Default.ElectricMeter, count = stats.waitingForElectric, color = StatusGreen) { onNodeClick("Electric Bridge") }
                Text(text = "➔", color = Color.Gray, fontWeight = FontWeight.Bold)
                MapNodeItem(title = "Unloading", icon = Icons.Default.LocalShipping, count = stats.waitingForUnload, color = StatusPurple) { onNodeClick("Unload Yard") }
            }
        }
    }
}

@Composable
fun MapNodeItem(title: String, icon: ImageVector, count: Int, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.15f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Color(0xFF334155))
        Surface(shape = RoundedCornerShape(8.dp), color = color) {
            Text(
                text = "$count Lorries",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

/**
 * Airport Style Live Queue Display Board
 */
@Composable
fun AirportQueueBoardCard(
    stats: DashboardStats,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AIRPORT DISPLAY QUEUE BOARD",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFF59E0B),
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "LIVE QUEUE VELOCITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QueueBoardRow("MILL WEIGHBRIDGE", waiting = stats.waitingForMill, active = stats.activeMillCount, completed = stats.completedMillToday, color = Color(0xFFF97316))
                QueueBoardRow("ELECTRIC WEIGHBRIDGE", waiting = stats.waitingForElectric, active = stats.activeElectricCount, completed = stats.completedElectricToday, color = Color(0xFF22C55E))
                QueueBoardRow("UNLOADING YARD", waiting = stats.waitingForUnload, active = stats.activeUnloadCount, completed = stats.completedUnloadToday, color = Color(0xFFA855F7))
            }
        }
    }
}

@Composable
fun QueueBoardRow(location: String, waiting: Int, active: Int, completed: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.2f)) {
            Text(text = location, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
            Text(text = "Working: $active In-Progress", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8), fontSize = 10.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF451A03)) {
                Text(text = "$waiting WAITING", color = Color(0xFFFDBA74), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF064E3B)) {
                Text(text = "$completed DONE", color = Color(0xFF6EE7B7), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}

/**
 * Expandable Smart Quick Actions FAB Speed Dial
 */
@Composable
fun SmartQuickActionFab(
    onNewEntryClick: () -> Unit,
    onScanQrClick: () -> Unit,
    onShiftHandoverClick: () -> Unit,
    onReportIncidentClick: () -> Unit,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FabActionPill(icon = Icons.Default.NoteAdd, label = "New Gate Entry", color = IndustrialBlue) {
                    expanded = false
                    onNewEntryClick()
                }
                FabActionPill(icon = Icons.Default.QrCodeScanner, label = "Scan QR / OCR", color = StatusPurple) {
                    expanded = false
                    onScanQrClick()
                }
                FabActionPill(icon = Icons.Default.CloudSync, label = "Sync Offline Data", color = StatusGreen) {
                    expanded = false
                    onSyncClick()
                }
                FabActionPill(icon = Icons.Default.AddAlert, label = "Shift Handover", color = StatusBlue) {
                    expanded = false
                    onShiftHandoverClick()
                }
                FabActionPill(icon = Icons.Default.ReportProblem, label = "Report Incident", color = StatusRed) {
                    expanded = false
                    onReportIncidentClick()
                }
            }
        }

        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = IndustrialBlue,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Add else Icons.Default.Add,
                contentDescription = "Quick Actions",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun FabActionPill(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Surface(
            shape = CircleShape,
            color = color,
            shadowElevation = 4.dp,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/**
 * Shift Handover Dialog
 */
@Composable
fun ShiftHandoverDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Shift Handover Notes", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Leave notes for the incoming shift operator regarding pending weighments or bottleneck lorries:", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (notes.isNotBlank()) onSubmit(notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndustrialBlue)
            ) {
                Text("Submit Handover")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Incident Reporting Dialog
 */
@Composable
fun IncidentReportDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var category by remember { mutableStateOf("Weight Mismatch") }
    var details by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Operational Incident", fontWeight = FontWeight.Bold, color = StatusRed) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Log weighbridge discrepancy, damaged jute bales, or vehicle breakdown:", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Incident Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Incident Details & Gate Entry No.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (details.isNotBlank()) onSubmit(category, details)
                },
                colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
            ) {
                Text("Log Incident")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Interactive Kanban Process View
 */
@Composable
fun KanbanBoardView(
    lorries: List<LorryWeighment>,
    onLorryClick: (LorryWeighment) -> Unit,
    modifier: Modifier = Modifier
) {
    val gateEntries = lorries.filter { it.status == LorryStatus.GATE_ENTRY.name }
    val millPending = lorries.filter { it.status == LorryStatus.MILL_GROSS_PENDING.name }
    val unloadingYard = lorries.filter { it.status == LorryStatus.WAITING_FOR_UNLOADING.name }
    val electricPending = lorries.filter { it.status == LorryStatus.ELECTRIC_GROSS_DONE.name || it.status == LorryStatus.ELECTRIC_TARE_DONE.name }
    val gateOutReady = lorries.filter { it.status == LorryStatus.READY_FOR_GATE_EXIT.name || it.status == LorryStatus.COMPLETED.name }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        item { KanbanColumn("🚪 Gate Entry (${gateEntries.size})", gateEntries, StatusBlue, onLorryClick) }
        item { KanbanColumn("⚖️ Mill Bridge (${millPending.size})", millPending, StatusOrange, onLorryClick) }
        item { KanbanColumn("🌾 Unloading (${unloadingYard.size})", unloadingYard, StatusPurple, onLorryClick) }
        item { KanbanColumn("⚡ Electric (${electricPending.size})", electricPending, StatusGreen, onLorryClick) }
        item { KanbanColumn("🚩 Gate Out (${gateOutReady.size})", gateOutReady, IndustrialBlue, onLorryClick) }
    }
}

@Composable
fun KanbanColumn(
    title: String,
    lorries: List<LorryWeighment>,
    color: Color,
    onLorryClick: (LorryWeighment) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = Modifier.width(260.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(10.dp))
            if (lorries.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Text(
                        text = "🎉 No Queue",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    lorries.take(6).forEach { lorry ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLorryClick(lorry) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = lorry.lorryNumber,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = IndustrialBlue
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = color.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = lorry.description.ifEmpty { "JUTE" },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = color,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = lorry.party,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Pass: ${lorry.gatePass}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
