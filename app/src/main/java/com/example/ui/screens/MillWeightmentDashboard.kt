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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.MasterDataLists
import com.example.data.model.QualityItem
import com.example.data.model.UserRole
import com.example.ui.components.SearchableDropdown
import com.example.ui.components.StatusBadge
import com.example.ui.theme.IndustrialBlue
import com.example.ui.theme.StatusRed
import com.squareup.moshi.Moshi

@Composable
fun MillWeightmentDashboard(
    lorries: List<LorryWeighment>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSubmitGrossWeight: (
        gatePass: String,
        grossWeight: Double,
        party: String,
        chalan: String,
        mokam: String,
        marka: String,
        description: String,
        tareWeight: Double?,
        totalQuantity: Double,
        unit: String,
        qualityItems: List<QualityItem>,
        lorryNumber: String,
        chalanGrossWeight: Double?
    ) -> Unit,
    onSubmitTareWeight: (gatePass: String, tareWeight: Double) -> Unit,
    currentUserRole: UserRole? = null,
    modifier: Modifier = Modifier
) {
    var selectedLorry by remember { mutableStateOf<LorryWeighment?>(null) }
    var isEditingForm by remember { mutableStateOf(false) }

    var vehicleNoInput by remember { mutableStateOf("") }
    var gatePassInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var partyInput by remember { mutableStateOf("") }
    var challanInput by remember { mutableStateOf("") }
    var chalanGrossWtInput by remember { mutableStateOf("") }
    var mokamInput by remember { mutableStateOf("") }
    var markaInput by remember { mutableStateOf("") }
    var millGrossWeightInput by remember { mutableStateOf("") }
    var millTareWeightInput by remember { mutableStateOf("") }

    val qualityItems = remember { mutableStateListOf<QualityItem>() }

    val moshi = remember { Moshi.Builder().build() }
    val qualityAdapter = remember(moshi) {
        moshi.adapter<List<QualityItem>>(
            com.squareup.moshi.Types.newParameterizedType(List::class.java, QualityItem::class.java)
        )
    }

    fun loadLorryIntoForm(lorry: LorryWeighment) {
        selectedLorry = lorry
        gatePassInput = lorry.gatePass
        vehicleNoInput = lorry.lorryNumber
        partyInput = lorry.party
        challanInput = lorry.chalan
        descriptionInput = lorry.description
        chalanGrossWtInput = if ((lorry.grossWeight ?: 0.0) > 0) lorry.grossWeight?.toInt().toString() else ""
        mokamInput = lorry.mokam
        markaInput = lorry.marka
        millGrossWeightInput = if ((lorry.millGrossWeight ?: 0.0) > 0) lorry.millGrossWeight?.toInt().toString() else ""
        millTareWeightInput = if ((lorry.millTareWeight ?: 0.0) > 0) lorry.millTareWeight?.toInt().toString() else ""

        qualityItems.clear()
        val parsed = try {
            qualityAdapter.fromJson(lorry.qualityItemsJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        if (parsed.isNotEmpty()) {
            qualityItems.addAll(parsed)
        } else {
            qualityItems.add(
                QualityItem(
                    quality = "",
                    quantity = lorry.totalQuantity,
                    unit = lorry.unit
                )
            )
        }
        isEditingForm = true
    }

    LaunchedEffect(lorries) {
        selectedLorry?.let { current ->
            val updated = lorries.find { it.gatePass == current.gatePass }
            if (updated != null) {
                selectedLorry = updated
                chalanGrossWtInput = if ((updated.grossWeight ?: 0.0) > 0) updated.grossWeight?.toInt().toString() else ""
                if ((updated.millGrossWeight ?: 0.0) > 0) {
                    millGrossWeightInput = updated.millGrossWeight?.toInt().toString() ?: ""
                }
                if ((updated.millTareWeight ?: 0.0) > 0) {
                    millTareWeightInput = updated.millTareWeight?.toInt().toString() ?: ""
                }
            }
        }
    }

    fun resetForm() {
        selectedLorry = null
        gatePassInput = ""
        vehicleNoInput = ""
        partyInput = ""
        challanInput = ""
        descriptionInput = ""
        chalanGrossWtInput = ""
        mokamInput = ""
        markaInput = ""
        millGrossWeightInput = ""
        millTareWeightInput = ""
        qualityItems.clear()
        qualityItems.add(QualityItem(quality = "", quantity = 0.0, unit = ""))
        isEditingForm = false
    }

    if (qualityItems.isEmpty()) {
        qualityItems.add(QualityItem(quality = "", quantity = 0.0, unit = ""))
    }

    val standardInputColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFF111827),
        unfocusedTextColor = Color(0xFF111827),
        focusedBorderColor = IndustrialBlue,
        unfocusedBorderColor = Color(0xFFCBD5E1),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedLabelColor = IndustrialBlue,
        unfocusedLabelColor = Color(0xFF475569)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IndustrialBlue)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditingForm) {
                    IconButton(onClick = { isEditingForm = false }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                } else {
                    Icon(imageVector = Icons.Default.Scale, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEditingForm) "Mill Weighment & Chalan Form" else "Pending Gate Lorries Queue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isEditingForm) "Vehicle: ${vehicleNoInput.ifBlank { "New" }} (${gatePassInput.ifBlank { "N/A" }})" else "${lorries.size} Lorries waiting from Gate Entry",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE2E8F0)
                    )
                }

                if (!isEditingForm) {
                    Button(
                        onClick = {
                            resetForm()
                            isEditingForm = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("+ Direct Entry", color = IndustrialBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            if (!isEditingForm) {
                val displayJuteLorries = remember(lorries, searchQuery) {
                    lorries.filter { lorry ->
                        lorry.effectiveDepartment == "Jute" &&
                        lorry.status != LorryStatus.COMPLETED.name &&
                        lorry.outTime.isNullOrEmpty() &&
                        (searchQuery.isBlank() ||
                         lorry.lorryNumber.contains(searchQuery, ignoreCase = true) ||
                         lorry.gatePass.contains(searchQuery, ignoreCase = true) ||
                         lorry.party.contains(searchQuery, ignoreCase = true))
                    }
                }

                // DASHBOARD VIEW: SHOW PENDING JUTE LORRIES FROM GATE
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            placeholder = { Text("Search Vehicle No, Pass, Party...", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IndustrialBlue) },
                            shape = RoundedCornerShape(10.dp),
                            colors = standardInputColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mill_search_input")
                        )
                    }

                    if (displayJuteLorries.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No pending Jute lorries in Mill queue.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            resetForm()
                                            isEditingForm = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialBlue)
                                    ) {
                                        Text("Open Direct Weighment Form", color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    items(displayJuteLorries) { lorry ->
                        val statusEnum = LorryStatus.fromString(lorry.status)
                        Card(
                            shape = RoundedCornerShape(12.dp),
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = lorry.lorryNumber,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF111827)
                                        )
                                        Text(
                                            text = "Gate Entry No: ${lorry.gatePass} | Entry Time: ${lorry.inTime.ifBlank { "N/A" }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF475569)
                                        )
                                    }

                                    StatusBadge(status = statusEnum)
                                }

                                if (lorry.party.isNotBlank() || lorry.chalan.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Party: ${lorry.party.ifBlank { "N/A" }} | Chalan: ${lorry.chalan.ifBlank { "N/A" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                val mGross = lorry.millGrossWeight?.toInt()?.toString() ?: "---"
                                val eGross = lorry.electricGrossWeight?.toInt()?.toString() ?: "---"
                                val eTare = lorry.electricTareWeight?.toInt()?.toString() ?: "---"
                                val mTare = lorry.millTareWeight?.toInt()?.toString() ?: "---"
                                Text(
                                    text = "Mill Gross: $mGross kg | Elec Gross: $eGross kg | Elec Tare: $eTare kg | Mill Tare: $mTare kg",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                val buttonLabel = when {
                                    lorry.millGrossWeight == null -> "Stage 1: Enter Mill Gross Weight"
                                    lorry.electricTareWeight == null -> "Stage 2/3: In Electric Weighbridge Stage"
                                    lorry.millTareWeight == null -> "Stage 4: Enter Mill Tare Weight"
                                    else -> "View / Edit Mill Weighment Record"
                                }

                                Button(
                                    onClick = { loadLorryIntoForm(lorry) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialBlue),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Scale, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(buttonLabel, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                // FORM VIEW: PARTY CHALAN & MILL GROSS WEIGHMENT FORM
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsBus,
                                            contentDescription = null,
                                            tint = IndustrialBlue
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Party Chalan Details",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = IndustrialBlue
                                        )
                                    }

                                    Button(
                                        onClick = { isEditingForm = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Close Form", color = Color(0xFF475569), fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Vehicle Dropdown Select
                                val juteLorries = remember(lorries) {
                                    lorries.filter { it.effectiveDepartment == "Jute" && it.status != LorryStatus.COMPLETED.name && it.outTime.isNullOrEmpty() }
                                }
                                val vehicleOptions = remember(juteLorries) {
                                    juteLorries.map { "${it.lorryNumber} (${it.gatePass})" }
                                }

                                SearchableDropdown(
                                    label = "Vehicle / Gate Entry",
                                    options = vehicleOptions,
                                    selectedOption = if (vehicleNoInput.isNotBlank() && gatePassInput.isNotBlank()) "$vehicleNoInput ($gatePassInput)" else vehicleNoInput,
                                    onOptionSelected = { selected ->
                                        val matchedLorry = juteLorries.find {
                                            "${it.lorryNumber} (${it.gatePass})" == selected || it.lorryNumber.equals(selected, ignoreCase = true)
                                        }
                                        if (matchedLorry != null) {
                                            loadLorryIntoForm(matchedLorry)
                                        } else {
                                            vehicleNoInput = selected
                                        }
                                    }
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Description & Party Name (Stacked vertically)
                                OutlinedTextField(
                                    value = descriptionInput,
                                    onValueChange = { descriptionInput = it },
                                    label = { Text("Item Description") },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = standardInputColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                SearchableDropdown(
                                    label = "Party Name",
                                    options = MasterDataLists.PARTIES,
                                    selectedOption = partyInput,
                                    onOptionSelected = { partyInput = it },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Challan No & Challan Gross Weight (Stacked vertically)
                                OutlinedTextField(
                                    value = challanInput,
                                    onValueChange = { challanInput = it },
                                    label = { Text("Challan No.") },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = standardInputColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = chalanGrossWtInput,
                                    onValueChange = { chalanGrossWtInput = it },
                                    label = { Text("Challan Gross Wt (kg)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = standardInputColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                Divider(color = Color(0xFFE2E8F0))
                                Spacer(modifier = Modifier.height(10.dp))

                                // Grade Details Section (Quality, Quantity & Unit)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Grade Details (Quality & Qty)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = IndustrialBlue
                                    )

                                    Button(
                                        onClick = {
                                            qualityItems.add(QualityItem(quality = "", quantity = 0.0, unit = ""))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = IndustrialBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Grade", color = Color.White, fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                qualityItems.forEachIndexed { index, item ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Grade #${index + 1}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF334155)
                                            )
                                            if (qualityItems.size > 1) {
                                                IconButton(
                                                    onClick = { qualityItems.removeAt(index) },
                                                    modifier = Modifier.height(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusRed)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        SearchableDropdown(
                                            label = "Quality Grade",
                                            options = MasterDataLists.QUALITIES,
                                            selectedOption = item.quality,
                                            onOptionSelected = { selectedQual ->
                                                qualityItems[index] = item.copy(quality = selectedQual)
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        OutlinedTextField(
                                            value = if (item.quantity > 0) item.quantity.toString() else "",
                                            onValueChange = { qStr ->
                                                val qVal = qStr.toDoubleOrNull() ?: 0.0
                                                qualityItems[index] = item.copy(quantity = qVal)
                                            },
                                            label = { Text("Quantity") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = standardInputColors,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        SearchableDropdown(
                                            label = "Unit",
                                            options = MasterDataLists.UNITS,
                                            selectedOption = item.unit,
                                            onOptionSelected = { selectedUnit ->
                                                qualityItems[index] = item.copy(unit = selectedUnit)
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Mokam & Marka (Stacked vertically)
                                SearchableDropdown(
                                    label = "Mokam",
                                    options = MasterDataLists.MOKAMS,
                                    selectedOption = mokamInput,
                                    onOptionSelected = { mokamInput = it },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                SearchableDropdown(
                                    label = "Marka",
                                    options = MasterDataLists.MARKAS,
                                    selectedOption = markaInput,
                                    onOptionSelected = { markaInput = it },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // MILL GROSS & TARE WEIGHMENT CARD
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, IndustrialBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Scale, contentDescription = null, tint = IndustrialBlue)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Mill Weighment (Gross & Tare)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = IndustrialBlue
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val isMillGrossInModel = (selectedLorry?.millGrossWeight ?: 0.0) > 0
                                val isMillTareInModel = (selectedLorry?.millTareWeight ?: 0.0) > 0
                                val isSuperAdminRole = currentUserRole == UserRole.SUPER_ADMIN

                                val canEditMillGross = !isMillGrossInModel || isSuperAdminRole
                                val canEditMillTare = !isMillTareInModel || isSuperAdminRole

                                OutlinedTextField(
                                    value = millGrossWeightInput,
                                    onValueChange = { millGrossWeightInput = it },
                                    label = { Text("Mill Gross Weight (kg)") },
                                    enabled = canEditMillGross,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = standardInputColors,
                                    leadingIcon = {
                                        Icon(Icons.Default.Scale, contentDescription = null, tint = IndustrialBlue)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (isMillGrossInModel && !isSuperAdminRole) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "🔒 Mill Gross Weight Recorded (${selectedLorry!!.millGrossWeight?.toInt()} kg) • Locked",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (!isMillGrossInModel && !isSuperAdminRole) {
                                    // STAGE 1: Mill Gross Stage - DO NOT ASK FOR MILL TARE YET
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = "ℹ️ Stage 1 of 2 in Mill Weighbridge: Enter Mill Gross Weight. Mill Tare Weight will be recorded in Stage 4 after Electric Weighbridge.",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF1E40AF)
                                        )
                                    }
                                } else {
                                    // STAGE 4: Mill Tare Stage
                                    OutlinedTextField(
                                        value = millTareWeightInput,
                                        onValueChange = { millTareWeightInput = it },
                                        label = { Text("Mill Tare Weight (Empty Lorry) (kg)") },
                                        placeholder = { Text("Enter empty lorry weight after unloading") },
                                        enabled = canEditMillTare,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = standardInputColors,
                                        leadingIcon = {
                                            Icon(Icons.Default.Scale, contentDescription = null, tint = IndustrialBlue)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    if (isMillTareInModel && !isSuperAdminRole) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "🔒 Mill Tare Weight Recorded (${selectedLorry!!.millTareWeight?.toInt()} kg) • Locked",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD97706)
                                        )
                                    }

                                    val grossVal = millGrossWeightInput.toDoubleOrNull() ?: (selectedLorry?.millGrossWeight ?: 0.0)
                                    val tareVal = millTareWeightInput.toDoubleOrNull() ?: 0.0
                                    if (grossVal > 0 && tareVal > 0) {
                                        val netVal = (grossVal - tareVal).coerceAtLeast(0.0)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF0FDF4), RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Calculated Mill Net Weight:",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF166534)
                                                )
                                                Text(
                                                    text = "${netVal.toInt()} kg",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF15803D)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        val millGross = millGrossWeightInput.toDoubleOrNull() ?: (selectedLorry?.millGrossWeight ?: 0.0)
                                        val millTare = millTareWeightInput.toDoubleOrNull()
                                        val chalanGross = chalanGrossWtInput.toDoubleOrNull()
                                        val totalQtySum = qualityItems.sumOf { it.quantity }
                                        val primaryUnit = qualityItems.firstOrNull()?.unit ?: "BALES"
                                        val primaryQuality = qualityItems.joinToString(", ") { it.quality }.ifBlank { descriptionInput }

                                        if (isMillGrossInModel && millTare != null && millTare > 0) {
                                            onSubmitTareWeight(gatePassInput, millTare)
                                        } else {
                                            onSubmitGrossWeight(
                                                gatePassInput,
                                                millGross,
                                                partyInput,
                                                challanInput,
                                                mokamInput,
                                                markaInput,
                                                primaryQuality,
                                                millTare,
                                                totalQtySum,
                                                primaryUnit,
                                                qualityItems.toList(),
                                                vehicleNoInput,
                                                chalanGross
                                            )
                                        }

                                        resetForm()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialBlue),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("submit_mill_weight_button")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isMillGrossInModel) "Submit Mill Tare Weight (Stage 4)" else "Submit Mill Gross Weight (Stage 1)",
                                        color = Color.White,
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
}
