package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.LorryStatus
import com.example.data.model.LorryWeighment
import com.example.ui.theme.IndustrialBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GateOutScreen(
    lorry: LorryWeighment,
    onMarkOutClick: (gatePass: String, remarks: String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val currentTime = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()) }

    var outDateText by remember { mutableStateOf(todayDate) }
    var outTimeText by remember { mutableStateOf(currentTime) }
    var remarksInput by remember { mutableStateOf("Lorry Out - Weight Clearance Passed") }

    val isAlreadyOut = lorry.status == LorryStatus.COMPLETED.name || !lorry.outTime.isNullOrBlank()
    val isDeptVehicle = lorry.remarks?.contains("Department:", ignoreCase = true) == true && !lorry.remarks.contains("Jute", ignoreCase = true)
    val isClearedForOut = isAlreadyOut || lorry.status == LorryStatus.READY_FOR_GATE_EXIT.name || lorry.unloaded || lorry.hasTareRecorded || lorry.status == LorryStatus.ELECTRIC_TARE_DONE.name
    val displayTare = lorry.tareWeight ?: lorry.millTareWeight ?: lorry.electricTareWeight
    val displayGross = lorry.grossWeight ?: lorry.millGrossWeight ?: lorry.electricGrossWeight
    val displayNet = lorry.lowestNetWeight

    val standardInputColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFF111827),
        unfocusedTextColor = Color(0xFF111827),
        focusedBorderColor = IndustrialBlue,
        unfocusedBorderColor = Color(0xFFCCCCCC),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedLabelColor = IndustrialBlue,
        unfocusedLabelColor = Color(0xFF4B5563)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IndustrialBlue)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Main Gate Exit Clearance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lorry.gatePass,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = IndustrialBlue
                            )
                            Icon(
                                imageVector = if (isClearedForOut) Icons.Default.ExitToApp else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isClearedForOut) StatusGreen else StatusRed
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "Lorry Number: ${lorry.lorryNumber}", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        Text(text = "Party Name: ${lorry.party.ifBlank { "N/A" }}", color = Color(0xFF374151))
                        Text(text = "Challan No: ${lorry.chalan.ifBlank { "N/A" }}", color = Color(0xFF374151))
                        Text(text = "Mokam: ${lorry.mokam.ifBlank { "-" }} | Marka: ${lorry.marka.ifBlank { "-" }}", color = Color(0xFF374151))

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isAlreadyOut) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = StatusGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "✓ Lorry Already Marked OUT",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = "Out Date: ${lorry.outDate ?: outDateText}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF111827))
                                    Text(text = "Out Time: ${lorry.outTime ?: outTimeText}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF111827))
                                    Text(text = "Status: Completed (Lorry Out)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = StatusGreen)
                                    if (!lorry.remarks.isNullOrBlank()) {
                                        Text(text = "Exit Remarks: ${lorry.remarks}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4B5563))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        } else if (!isClearedForOut) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = StatusRed,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = "🛑 GATE EXIT RESTRICTED: Vehicle has not received Department / Tare Weight Clearance. Cannot allow vehicle OUT without Department Clearance or Tare Weight!",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusRed
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        } else {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = null,
                                        tint = StatusGreen,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = "✅ CLEARANCE VERIFIED: Vehicle cleared for Main Gate Exit!",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusGreen
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Detailed Mill & Electric Weighment Breakdown Table
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "⚖️ Weighment & Net Weight Comparison",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = IndustrialBlue
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Mill Weighment Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Mill Gross", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(text = "${lorry.millGrossWeight?.toInt() ?: "---"} kg", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Mill Tare", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(text = "${lorry.millTareWeight?.toInt() ?: "---"} kg", fontWeight = FontWeight.Bold, color = if (lorry.millTareWeight != null) Color(0xFF111827) else StatusRed)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Mill Net Wt", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(text = "${lorry.millNetWeight?.toInt() ?: "---"} kg", fontWeight = FontWeight.Bold, color = IndustrialBlue)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Electric Weighment Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Elec Gross", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(text = "${lorry.electricGrossWeight?.toInt() ?: "---"} kg", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Elec Tare", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(text = "${lorry.electricTareWeight?.toInt() ?: "---"} kg", fontWeight = FontWeight.Bold, color = if (lorry.electricTareWeight != null) Color(0xFF111827) else StatusRed)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Elec Net Wt", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(text = "${lorry.electricNetWeight?.toInt() ?: "---"} kg", fontWeight = FontWeight.Bold, color = IndustrialBlue)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Final Gate Summary Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Gate Gross (Challan)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(text = "${lorry.grossWeight?.toInt() ?: "---"} kg", fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Recorded Tare Wt", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(text = "${displayTare?.toInt() ?: "NOT RECORDED"} kg", fontWeight = FontWeight.Bold, color = if (isClearedForOut) Color(0xFF111827) else StatusRed)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Billed Net Wt (Lowest)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(text = "${displayNet?.toInt() ?: "---"} kg", fontWeight = FontWeight.Bold, color = StatusGreen)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (!isAlreadyOut) {
                            OutlinedTextField(
                                value = outDateText,
                                onValueChange = { outDateText = it },
                                label = { Text("Out Date") },
                                trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp),
                                colors = standardInputColors,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = outTimeText,
                                onValueChange = { outTimeText = it },
                                label = { Text("Out Time") },
                                trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp),
                                colors = standardInputColors,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = remarksInput,
                                onValueChange = { remarksInput = it },
                                label = { Text("Gate Exit Remarks") },
                                shape = RoundedCornerShape(12.dp),
                                colors = standardInputColors,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        Button(
                            onClick = {
                                if (!isAlreadyOut && isClearedForOut) {
                                    onMarkOutClick(lorry.gatePass, remarksInput)
                                }
                            },
                            enabled = !isAlreadyOut && isClearedForOut,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IndustrialBlue,
                                disabledContainerColor = if (isAlreadyOut) StatusGreen else Color(0xFFD1D5DB),
                                disabledContentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_mark_lorry_out")
                        ) {
                            Text(
                                text = when {
                                    isAlreadyOut -> "✓ Lorry Already Marked OUT"
                                    isClearedForOut -> "Clear & Mark Lorry OUT"
                                    else -> "Gate Out Locked (Missing Clearance)"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
