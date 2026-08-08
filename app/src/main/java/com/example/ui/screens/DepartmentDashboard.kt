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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Store
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import com.example.ui.theme.StatusOrange

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
                            text = "Manage $deptName Vehicles • Load/Unload Status & Clear for Main Gate Out",
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
                            text = "1. Select $deptName Vehicle",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = deptThemeColor
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (deptLorries.isEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "No pending lorries found for $deptName department.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        } else {
                            val optionsList = deptLorries.map { "${it.lorryNumber} (${it.gatePass} - ${it.party.ifBlank { "No Party" }})" }
                            val currentSelectedStr = selectedLorry?.let { "${it.lorryNumber} (${it.gatePass} - ${it.party.ifBlank { "No Party" }})" } ?: ""

                            SearchableDropdown(
                                label = "Select Vehicle Number Dropdown ($deptName)",
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
                                    Text("Party:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(lorry.party.ifBlank { "N/A" }, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Column {
                                    Text("Chalan No:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(lorry.chalan.ifBlank { "N/A" }, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Column {
                                    Text("Category:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(lorry.description, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "2. Department Load / Unload Status",
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
                                text = "3. $deptName Person Remarks",
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
}
