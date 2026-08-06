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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.data.model.LorryStatus
import com.example.data.model.LorryWeighment
import com.example.data.model.UserRole
import com.example.ui.components.SearchableDropdown
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusPurple

@Composable
fun ElectricWeightmentDashboard(
    lorries: List<LorryWeighment>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSubmitElectricGross: (gatePass: String, grossWeight: Double) -> Unit,
    onMarkUnloaded: (gatePass: String) -> Unit,
    onSubmitElectricTare: (gatePass: String, tareWeight: Double) -> Unit,
    onSubmitElectricWeights: ((gatePass: String, grossWeight: Double?, tareWeight: Double?) -> Unit)? = null,
    currentUserRole: com.example.data.model.UserRole? = null,
    modifier: Modifier = Modifier
) {
    var selectedLorry by remember { mutableStateOf<LorryWeighment?>(null) }
    var vehicleNoInput by remember { mutableStateOf("") }
    var gatePassInput by remember { mutableStateOf("") }
    var eleGrossInput by remember { mutableStateOf("") }
    var eleTareInput by remember { mutableStateOf("") }

    fun loadLorry(lorry: LorryWeighment) {
        selectedLorry = lorry
        gatePassInput = lorry.gatePass
        vehicleNoInput = lorry.lorryNumber
        eleGrossInput = if ((lorry.electricGrossWeight ?: 0.0) > 0) lorry.electricGrossWeight?.toInt()?.toString() ?: "" else ""
        eleTareInput = if ((lorry.electricTareWeight ?: 0.0) > 0) lorry.electricTareWeight?.toInt()?.toString() ?: "" else ""
    }

    LaunchedEffect(lorries) {
        selectedLorry?.let { current ->
            val updated = lorries.find { it.gatePass == current.gatePass }
            if (updated != null) {
                selectedLorry = updated
                if ((updated.electricGrossWeight ?: 0.0) > 0) {
                    eleGrossInput = updated.electricGrossWeight?.toInt()?.toString() ?: ""
                }
                if ((updated.electricTareWeight ?: 0.0) > 0) {
                    eleTareInput = updated.electricTareWeight?.toInt()?.toString() ?: ""
                }
            }
        }
    }

    fun resetForm() {
        selectedLorry = null
        gatePassInput = ""
        vehicleNoInput = ""
        eleGrossInput = ""
        eleTareInput = ""
    }

    val standardInputColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFF111827),
        unfocusedTextColor = Color(0xFF111827),
        focusedBorderColor = StatusPurple,
        unfocusedBorderColor = Color(0xFFCBD5E1),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedLabelColor = StatusPurple,
        unfocusedLabelColor = Color(0xFF475569),
        disabledBorderColor = Color(0xFFE2E8F0),
        disabledContainerColor = Color(0xFFF8FAFC),
        disabledTextColor = Color(0xFF94A3B8),
        disabledLabelColor = Color(0xFF94A3B8)
    )

    val pendingLorries = remember(lorries) {
        lorries.filter { it.status != LorryStatus.COMPLETED.name }
    }

    // Gross weight condition: Check if gross weight exists or is currently entered
    val recordedGross = selectedLorry?.electricGrossWeight ?: selectedLorry?.grossWeight ?: 0.0
    val currentEnteredGross = eleGrossInput.toDoubleOrNull() ?: 0.0
    val isGrossRecorded = recordedGross > 0 || currentEnteredGross > 0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StatusPurple)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Electric Weighbridge Station",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Electric Gross & Tare Weighment",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFF3E8FF)
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Section 1: Weighment Entry Form
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Electric Weighbridge Form",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusPurple
                                )

                                if (selectedLorry != null || vehicleNoInput.isNotBlank()) {
                                    Button(
                                        onClick = { resetForm() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Clear", color = Color(0xFF475569), fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Vehicle Dropdown Select (All Active & Pending Vehicles)
                            val vehicleOptions = remember(pendingLorries) {
                                pendingLorries.map { "${it.lorryNumber} (${it.gatePass})" }
                            }

                            SearchableDropdown(
                                label = "Select Vehicle (${pendingLorries.size} Pending)",
                                options = vehicleOptions,
                                selectedOption = if (vehicleNoInput.isNotBlank() && gatePassInput.isNotBlank()) "$vehicleNoInput ($gatePassInput)" else vehicleNoInput,
                                onOptionSelected = { selected ->
                                    val matched = pendingLorries.find {
                                        "${it.lorryNumber} (${it.gatePass})" == selected || it.lorryNumber.equals(selected, ignoreCase = true) || selected.contains(it.gatePass)
                                    } ?: lorries.find {
                                        "${it.lorryNumber} (${it.gatePass})" == selected || it.lorryNumber.equals(selected, ignoreCase = true) || selected.contains(it.gatePass)
                                    }

                                    if (matched != null) {
                                        loadLorry(matched)
                                    } else {
                                        vehicleNoInput = selected
                                    }
                                }
                            )

                            if (selectedLorry != null) {
                                val lorry = selectedLorry!!
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFAF5FF), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "Vehicle: ${lorry.lorryNumber} | Pass: ${lorry.gatePass}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusPurple
                                        )
                                        Text(
                                            text = "Party: ${lorry.party.ifBlank { "N/A" }} | Chalan: ${lorry.chalan.ifBlank { "N/A" }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                }
                            }

                            val isGrossInModel = (selectedLorry?.electricGrossWeight ?: 0.0) > 0
                            val isTareInModel = (selectedLorry?.electricTareWeight ?: 0.0) > 0
                            val isSuperAdminRole = currentUserRole == UserRole.SUPER_ADMIN

                            val canEditGross = !isGrossInModel || isSuperAdminRole
                            val canEditTare = !isTareInModel || isSuperAdminRole

                            Spacer(modifier = Modifier.height(10.dp))

                            // Step 1: Gross Weight Input
                            Column {
                                OutlinedTextField(
                                    value = eleGrossInput,
                                    onValueChange = { eleGrossInput = it },
                                    label = { Text("Electric Gross Wt (kg)") },
                                    enabled = canEditGross,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = standardInputColors,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (isGrossInModel && !isSuperAdminRole) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "🔒 Gross Wt Recorded (${selectedLorry!!.electricGrossWeight?.toInt()} kg) • Locked until session reset or Admin",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Step 2: Tare Weight Input (ACTIVE ONLY AFTER GROSS WEIGHT IS RECORDED / ENTERED)
                            if (!isGrossRecorded && !isGrossInModel) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFEF3C7), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.padding(end = 6.dp)
                                        )
                                        Text(
                                            text = "Tare Weight is active after Gross Weight entry",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                }
                            } else {
                                Column {
                                    OutlinedTextField(
                                        value = eleTareInput,
                                        onValueChange = { eleTareInput = it },
                                        label = { Text("Electric Tare Wt (kg)") },
                                        enabled = canEditTare,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = standardInputColors,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    if (isTareInModel && !isSuperAdminRole) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "🔒 Tare Wt Recorded (${selectedLorry!!.electricTareWeight?.toInt()} kg) • Locked until session reset or Admin",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD97706)
                                        )
                                    }

                                    val eg = eleGrossInput.toDoubleOrNull() ?: recordedGross
                                    val et = eleTareInput.toDoubleOrNull() ?: 0.0
                                    if (eg > 0 && et > 0) {
                                        val net = (eg - et).coerceAtLeast(0.0)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Calculated Net Weight: ${net.toInt()} kg",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (selectedLorry != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { onMarkUnloaded(selectedLorry!!.gatePass) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (selectedLorry!!.unloaded) "Status: UNLOADED" else "Mark Unloaded",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            Button(
                                onClick = {
                                    val matchedLorry = selectedLorry ?: lorries.find {
                                        (gatePassInput.isNotBlank() && it.gatePass.equals(gatePassInput.trim(), ignoreCase = true)) ||
                                        (vehicleNoInput.isNotBlank() && it.lorryNumber.equals(vehicleNoInput.trim(), ignoreCase = true) && it.status != LorryStatus.COMPLETED.name)
                                    }
                                    val pass = gatePassInput.ifBlank { matchedLorry?.gatePass ?: vehicleNoInput }
                                    val eg = eleGrossInput.toDoubleOrNull()
                                    val et = eleTareInput.toDoubleOrNull()

                                    if (pass.isNotBlank()) {
                                        if (onSubmitElectricWeights != null) {
                                            onSubmitElectricWeights(pass, eg, et)
                                        } else {
                                            if (eg != null && eg > 0) {
                                                onSubmitElectricGross(pass, eg)
                                            }
                                            if (et != null && et > 0 && isGrossRecorded) {
                                                onSubmitElectricTare(pass, et)
                                            }
                                        }
                                        resetForm()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusPurple),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("submit_electric_weights_btn")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isGrossRecorded && eleTareInput.isNotBlank()) "Submit Electric Tare Weight" else "Submit Electric Gross Weight",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // Section 2: Queue Header & Search
                item {
                    Text(
                        text = "Pending Electric Weighbridge Queue (${lorries.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Search Vehicle No, Pass...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = StatusPurple) },
                        shape = RoundedCornerShape(10.dp),
                        colors = standardInputColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ele_search_input")
                    )
                }

                if (lorries.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No lorries currently in queue.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                items(lorries) { lorry ->
                    val statusEnum = LorryStatus.fromString(lorry.status)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${lorry.lorryNumber} (${lorry.gatePass})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Party: ${lorry.party.ifBlank { "N/A" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4B5563),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                StatusBadge(status = statusEnum)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { loadLorry(lorry) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusPurple)
                            ) {
                                Text("Weigh", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}
