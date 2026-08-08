package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MasterDataLists
import com.example.data.model.QualityItem
import com.example.ui.components.SearchableDropdown
import com.example.ui.theme.IndustrialBlue
import com.example.ui.theme.StatusGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NewGateEntryScreen(
    generatedGatePass: String,
    brokersList: List<String>,
    qualitiesList: List<String>,
    mokamsList: List<String>,
    markasList: List<String>,
    onSaveClick: (
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
        department: String
    ) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val currentTime = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()) }

    var dateText by remember { mutableStateOf(todayDate) }
    var lorryNumber by remember { mutableStateOf("") }
    var selectedDepartment by remember { mutableStateOf("Select Department") }
    var materialDescription by remember { mutableStateOf("") }
    var inTimeText by remember { mutableStateOf(currentTime) }
    var remarksText by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IndustrialBlue)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Main Gate Vehicle Entry",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Step 1 of 1 • Vehicle Inflow & Gate Entry Registration",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(8.dp)
                    .background(
                        color = Color(0xFF10B981),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "1. Main Gate Vehicle Inflow Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IndustrialBlue
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🔒 Main Gate Policy: Record Vehicle Inflow & Issue Gate Entry No. Once submitted, vehicle entry data is locked and cannot be edited by operators until logout or Super Admin authorization.",
                                style = MaterialTheme.typography.bodySmall,
                                color = IndustrialBlue,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Date Selection with Dropdown & Refresh
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = dateText,
                                onValueChange = { dateText = it },
                                label = { Text("Date (yyyy-MM-dd)") },
                                trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp),
                                colors = standardInputColors,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    dateText = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IndustrialBlue),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
                            ) {
                                Text("Today", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = "IN",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Type") },
                            shape = RoundedCornerShape(12.dp),
                            colors = standardInputColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = lorryNumber,
                            onValueChange = {
                                if (it.length <= 15) lorryNumber = it.uppercase()
                            },
                            label = { Text("Vehicle Number") },
                            leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = standardInputColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_lorry_number")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // In Time Selection with Time Dropdown & Live Refresh
                        val timeDropdownOptions = listOf(
                            "CURRENT TIME (NOW)",
                            "06:00 AM", "07:00 AM", "08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM",
                            "12:00 PM", "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM",
                            "06:00 PM", "07:00 PM", "08:00 PM", "09:00 PM", "10:00 PM"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                com.example.ui.components.SearchableDropdown(
                                    label = "In Time Dropdown / Select",
                                    options = timeDropdownOptions,
                                    selectedOption = inTimeText,
                                    onOptionSelected = { selected ->
                                        if (selected == "CURRENT TIME (NOW)") {
                                            inTimeText = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                                        } else {
                                            inTimeText = selected
                                        }
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    inTimeText = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IndustrialBlue),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Now", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Current In-Time: $inTimeText (Click 'Now' to refresh if form was open 5-10 min)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SearchableDropdown(
                            label = "Select Department",
                            options = MasterDataLists.DEPARTMENTS,
                            selectedOption = selectedDepartment,
                            onOptionSelected = { selectedDepartment = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = materialDescription,
                            onValueChange = { materialDescription = it },
                            label = { Text("Material / Category Description") },
                            placeholder = { Text("e.g. Raw Jute, Machine Store Parts, Finished Bales") },
                            shape = RoundedCornerShape(12.dp),
                            colors = standardInputColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = generatedGatePass,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gate Entry No. (Auto Generated)") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF111827),
                                unfocusedTextColor = Color(0xFF111827),
                                focusedBorderColor = IndustrialBlue,
                                unfocusedContainerColor = Color(0xFFF3F4F6)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = remarksText,
                            onValueChange = { remarksText = it },
                            label = { Text("Gate Entry Remarks / Material Tag") },
                            shape = RoundedCornerShape(12.dp),
                            colors = standardInputColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Bottom Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val isValidDepartment = selectedDepartment.isNotBlank() && selectedDepartment != "Select Department"
            Button(
                onClick = {
                    if (lorryNumber.isNotBlank() && isValidDepartment) {
                        showConfirmDialog = true
                    }
                },
                enabled = lorryNumber.isNotBlank() && isValidDepartment,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndustrialBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_gate_entry_next_save")
            ) {
                val buttonLabel = when (selectedDepartment) {
                    "Jute" -> "Issue Gate Entry & Send to Mill Weighbridge"
                    "Store" -> "Issue Gate Entry & Send to Store"
                    "Finish Good" -> "Issue Gate Entry & Send to Finish Good"
                    "Other" -> "Issue Gate Entry & Send to Other Department"
                    else -> "Select Department"
                }
                Text(
                    text = buttonLabel,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = {
                    Text(
                        text = "Confirm Gate Entry Routing",
                        fontWeight = FontWeight.Bold,
                        color = IndustrialBlue
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Please verify the vehicle routing details before issuing the Gate Entry:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Gate Entry No: $generatedGatePass",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Vehicle No: ${lorryNumber.trim().uppercase()}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = IndustrialBlue
                                )
                                Text(
                                    text = "Department: $selectedDepartment",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusGreen
                                )
                                val destinationWorkflow = when (selectedDepartment) {
                                    "Jute" -> "Mill Weighbridge -> Electric Weighbridge -> Jute Yard"
                                    "Store" -> "Store Department Queue"
                                    "Finish Good" -> "Finish Good Department Queue"
                                    "Other" -> "Other Department Queue"
                                    else -> selectedDepartment
                                }
                                Text(
                                    text = "Routing to: $destinationWorkflow",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF4B5563)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            val currentLorryNo = lorryNumber.trim().uppercase()
                            val desc = if (materialDescription.isNotBlank()) materialDescription.trim() else selectedDepartment
                            onSaveClick(
                                currentLorryNo,
                                "", // Chalan
                                "", // Party
                                desc,
                                0.0,
                                "BALES",
                                null,
                                null,
                                emptyList(),
                                "",
                                "",
                                selectedDepartment
                            )
                            // Reset all entry form fields to blank after submission
                            lorryNumber = ""
                            selectedDepartment = ""
                            materialDescription = ""
                            remarksText = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialBlue)
                    ) {
                        Text("Confirm & Issue Gate Entry")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
