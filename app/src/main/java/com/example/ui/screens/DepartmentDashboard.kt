package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LorryStatus
import com.example.data.model.LorryWeighment
import com.example.data.model.UserRole
import com.example.ui.components.SearchableDropdown
import com.example.ui.theme.IndustrialBlue
import com.example.ui.theme.StatusGreen

@Composable
fun DepartmentDashboard(
    departmentRole: UserRole,
    lorries: List<LorryWeighment>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSubmitDepartmentAction: (gatePass: String, loadUnloadStatus: String, remarks: String, clearForExit: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val deptName = when (departmentRole) {
        UserRole.STORE -> "Store"
        UserRole.FINISH_GOOD -> "Finish Good"
        UserRole.OTHER -> "Other"
        else -> departmentRole.title
    }

    val deptThemeColor = when (departmentRole) {
        UserRole.STORE -> Color(0xFF0284C7)
        UserRole.FINISH_GOOD -> Color(0xFF059669)
        UserRole.OTHER -> Color(0xFFD97706)
        else -> IndustrialBlue
    }

    // Filter lorries strictly relevant to this department
    val deptLorries = remember(lorries, deptName, searchQuery) {
        lorries.filter { lorry ->
            val matchesDept = lorry.effectiveDepartment.equals(deptName, ignoreCase = true)

            val matchesSearch = searchQuery.isBlank() ||
                    lorry.lorryNumber.contains(searchQuery, ignoreCase = true) ||
                    lorry.gatePass.contains(searchQuery, ignoreCase = true) ||
                    lorry.party.contains(searchQuery, ignoreCase = true) ||
                    lorry.chalan.contains(searchQuery, ignoreCase = true)

            matchesDept && matchesSearch && lorry.status != LorryStatus.COMPLETED.name && lorry.outTime.isNullOrEmpty()
        }
    }

    var selectedGatePass by remember { mutableStateOf("") }
    val selectedLorry = deptLorries.find { it.gatePass == selectedGatePass } ?: deptLorries.firstOrNull()

    var dialogLorry by remember { mutableStateOf<LorryWeighment?>(null) }

    var loadUnloadStatus by remember { mutableStateOf("Unloaded") }
    var departmentRemarks by remember { mutableStateOf("") }
    var clearForMainGateOut by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Card(
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = deptThemeColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (departmentRole == UserRole.STORE) Icons.Default.Store else Icons.Default.Inventory,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "$deptName Department Dashboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Active Vehicles & Load/Unload Operations",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Table Format Section for Lorries
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📋 $deptName Active Lorries Table",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = deptThemeColor,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = deptThemeColor.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "${deptLorries.size} Vehicles",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = deptThemeColor,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))

                        if (deptLorries.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No pending lorries found for $deptName department",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        } else {
                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(deptThemeColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Lorry No.", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = deptThemeColor, modifier = Modifier.weight(1.3f))
                                Text("Gate Pass", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = deptThemeColor, modifier = Modifier.weight(1.5f))
                                Text("Party / Category", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = deptThemeColor, modifier = Modifier.weight(1.6f))
                                Text("In Time", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = deptThemeColor, modifier = Modifier.weight(1.1f))
                                Text("Action", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = deptThemeColor, modifier = Modifier.weight(0.9f))
                            }

                            Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                            deptLorries.forEachIndexed { index, lorry ->
                                val isSelected = selectedLorry?.gatePass == lorry.gatePass
                                val rowBg = if (isSelected) deptThemeColor.copy(alpha = 0.12f) else if (index % 2 == 0) Color(0xFFFAFAFA) else Color.White

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(rowBg, shape = RoundedCornerShape(4.dp))
                                        .clickable {
                                            selectedGatePass = lorry.gatePass
                                            dialogLorry = lorry
                                        }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = lorry.lorryNumber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) deptThemeColor else Color(0xFF0F172A),
                                        modifier = Modifier.weight(1.3f)
                                    )
                                    Text(
                                        text = lorry.gatePass,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF334155),
                                        modifier = Modifier.weight(1.5f)
                                    )
                                    Text(
                                        text = if (lorry.party.isNotBlank()) lorry.party else lorry.description,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        color = Color(0xFF475569),
                                        modifier = Modifier.weight(1.6f)
                                    )
                                    Text(
                                        text = lorry.inTime.ifBlank { "N/A" },
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        modifier = Modifier.weight(1.1f)
                                    )
                                    Box(modifier = Modifier.weight(0.9f)) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = deptThemeColor),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.clickable {
                                                selectedGatePass = lorry.gatePass
                                                dialogLorry = lorry
                                            }
                                        ) {
                                            Text(
                                                text = "View",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                            }
                        }
                    }
                }
            }

            // Vehicle Selection Dropdown Section
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Select Vehicle to Process",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = deptThemeColor
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (deptLorries.isEmpty()) {
                            Text(
                                text = "No active vehicles available for $deptName department.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        } else {
                            val optionsList = deptLorries.map { "${it.lorryNumber} (${it.gatePass} - ${it.party.ifBlank { "No Party" }})" }
                            val currentSelectedStr = selectedLorry?.let { "${it.lorryNumber} (${it.gatePass} - ${it.party.ifBlank { "No Party" }})" } ?: ""

                            SearchableDropdown(
                                label = "Select Vehicle Number ($deptName)",
                                options = optionsList,
                                selectedOption = currentSelectedStr,
                                onOptionSelected = { selected ->
                                    val gatePassPart = selected.substringAfter("(").substringBefore(" -")
                                    selectedGatePass = gatePassPart
                                }
                            )
                        }
                    }
                }
            }

            // Vehicle Details & Department Action Section
            selectedLorry?.let { lorry ->
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = lorry.lorryNumber,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = deptThemeColor
                                    )
                                    Text(
                                        text = "Gate Entry No: ${lorry.gatePass} • In Time: ${lorry.inTime}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = lorry.status,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = deptThemeColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Summary Grid
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Party / Vendor:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(lorry.party.ifBlank { "N/A" }, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                if (lorry.effectiveDepartment.equals("Jute", ignoreCase = true)) {
                                    Column {
                                        Text("Chalan No:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text(lorry.chalan.ifBlank { "N/A" }, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                } else {
                                    Column {
                                        Text("Quantity:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text("${lorry.totalQuantity} ${lorry.unit.ifBlank { "Units" }}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                                Column {
                                    Text("Category:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(lorry.description.ifBlank { "General Material" }, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Load / Unload Status",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = deptThemeColor
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf("Unloaded", "Loaded", "In Progress").forEach { statusOption ->
                                    FilterChip(
                                        selected = loadUnloadStatus == statusOption,
                                        onClick = { loadUnloadStatus = statusOption },
                                        label = { Text(statusOption, fontWeight = FontWeight.SemiBold) },
                                        leadingIcon = if (loadUnloadStatus == statusOption) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = deptThemeColor,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "$deptName Remarks",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = deptThemeColor
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = departmentRemarks,
                                onValueChange = { departmentRemarks = it },
                                label = { Text("$deptName Loading / Unloading Remarks") },
                                placeholder = { Text("e.g., Goods verified, unloaded at bay 2, quantity accurate.") },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = deptThemeColor,
                                    focusedLabelColor = deptThemeColor
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = clearForMainGateOut,
                                        onCheckedChange = { clearForMainGateOut = it },
                                        colors = CheckboxDefaults.colors(checkedColor = StatusGreen)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Lorry Yes Clear For Out Main Gate",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF166534)
                                        )
                                        Text(
                                            text = "Marking this option authorizes Main Gate to allow vehicle exit.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    onSubmitDepartmentAction(
                                        lorry.gatePass,
                                        loadUnloadStatus,
                                        departmentRemarks,
                                        clearForMainGateOut
                                    )
                                    departmentRemarks = ""
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = deptThemeColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("btn_submit_dept_action")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Submit & Clear Lorry for Gate Exit",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Full Details Modal Dialog when a row is clicked
    dialogLorry?.let { lorry ->
        AlertDialog(
            onDismissRequest = { dialogLorry = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = deptThemeColor, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Lorry Details: ${lorry.lorryNumber}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text(text = "Gate Entry No: ${lorry.gatePass}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Divider(color = Color(0xFFE2E8F0))
                    DetailRow("Vehicle Number", lorry.lorryNumber)
                    DetailRow("Gate Entry Pass", lorry.gatePass)
                    DetailRow("Department", lorry.effectiveDepartment)
                    DetailRow("Current Stage", lorry.currentStage)
                    DetailRow("Status", lorry.status)
                    DetailRow("Entry Date", lorry.date)
                    DetailRow("In Time", lorry.inTime)
                    DetailRow("Party / Vendor", lorry.party.ifBlank { "N/A" })
                    DetailRow("Category / Desc", lorry.description)
                    DetailRow("Quantity", "${lorry.totalQuantity} ${lorry.unit}")
                    
                    if (lorry.effectiveDepartment.equals("Jute", ignoreCase = true)) {
                        if (lorry.chalan.isNotBlank()) {
                            DetailRow("Chalan Number", lorry.chalan)
                        }
                        if (lorry.millGrossWeight != null && lorry.millGrossWeight > 0) {
                            DetailRow("Mill Gross Weight", "${lorry.millGrossWeight} MT")
                        }
                        if (lorry.millTareWeight != null && lorry.millTareWeight > 0) {
                            DetailRow("Mill Tare Weight", "${lorry.millTareWeight} MT")
                        }
                        if (lorry.electricGrossWeight != null && lorry.electricGrossWeight > 0) {
                            DetailRow("Electric Gross Wt", "${lorry.electricGrossWeight} MT")
                        }
                        if (lorry.electricTareWeight != null && lorry.electricTareWeight > 0) {
                            DetailRow("Electric Tare Wt", "${lorry.electricTareWeight} MT")
                        }
                    }
                    if (!lorry.remarks.isNullOrBlank()) {
                        DetailRow("Remarks", lorry.remarks)
                    }
                    Divider(color = Color(0xFFE2E8F0))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedGatePass = lorry.gatePass
                        dialogLorry = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = deptThemeColor)
                ) {
                    Text("Select for Processing")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { dialogLorry = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF64748B))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
    }
}
