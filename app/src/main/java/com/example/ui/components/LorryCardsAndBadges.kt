package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.data.model.LorryStatus
import com.example.data.model.LorryWeighment
import com.example.data.model.UserRole
import com.example.ui.theme.IndustrialBlue
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusPurple
import com.example.ui.theme.StatusRed

@Composable
fun StatusBadge(
    status: LorryStatus,
    modifier: Modifier = Modifier
) {
    val (bg, label) = when (status) {
        LorryStatus.GATE_ENTRY -> StatusBlue.copy(alpha = 0.15f) to status.badgeText
        LorryStatus.MILL_GROSS_PENDING -> StatusOrange.copy(alpha = 0.15f) to "Mill Weightment"
        LorryStatus.WAITING_FOR_UNLOADING -> StatusOrange.copy(alpha = 0.15f) to "Unload Pending"
        LorryStatus.ELECTRIC_GROSS_DONE -> StatusPurple.copy(alpha = 0.15f) to "Electric Weightment"
        LorryStatus.ELECTRIC_TARE_DONE -> StatusPurple.copy(alpha = 0.15f) to "Ele. Tare Done"
        LorryStatus.MILL_TARE_PENDING -> StatusOrange.copy(alpha = 0.15f) to "Mill Tare Pending"
        LorryStatus.READY_FOR_GATE_EXIT -> StatusBlue.copy(alpha = 0.15f) to "Ready Gate Exit"
        LorryStatus.COMPLETED -> StatusGreen.copy(alpha = 0.15f) to "Completed"
        LorryStatus.OVERDUE -> StatusRed.copy(alpha = 0.15f) to "Overdue"
    }

    val textColor = when (status) {
        LorryStatus.GATE_ENTRY, LorryStatus.READY_FOR_GATE_EXIT -> StatusBlue
        LorryStatus.MILL_GROSS_PENDING, LorryStatus.WAITING_FOR_UNLOADING, LorryStatus.MILL_TARE_PENDING -> StatusOrange
        LorryStatus.ELECTRIC_GROSS_DONE, LorryStatus.ELECTRIC_TARE_DONE -> StatusPurple
        LorryStatus.COMPLETED -> StatusGreen
        LorryStatus.OVERDUE -> StatusRed
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun LorryItemCard(
    lorry: LorryWeighment,
    daysInsideText: String,
    currentUserRole: UserRole?,
    onActionClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val statusEnum = LorryStatus.fromString(lorry.status)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("lorry_card_${lorry.gatePass}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Gate Pass & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = IndustrialBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = lorry.gatePass,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = statusEnum)
                    if (currentUserRole == UserRole.SUPER_ADMIN && onDeleteClick != null) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = StatusRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body Details: Lorry No & Party
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lorry.lorryNumber,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (lorry.party.isNotBlank()) "Party: ${lorry.party}" else "Party: Pending Mill Entry",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4B5563),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (daysInsideText == "Today") StatusGreen else StatusRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = daysInsideText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (daysInsideText == "Today") StatusGreen else StatusRed,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = if (lorry.description.isNotBlank()) "${lorry.description} | ${lorry.totalQuantity.toInt()} ${lorry.unit}" else if (lorry.totalQuantity > 0) "${lorry.totalQuantity.toInt()} ${lorry.unit}" else "Raw Jute",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Weights Summary Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                // Row 1: Gross Weights
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    WeightChip(
                        label = "Gate Gross",
                        weight = lorry.grossWeight,
                        modifier = Modifier.weight(1f)
                    )
                    WeightChip(
                        label = "Mill Gross",
                        weight = lorry.millGrossWeight,
                        modifier = Modifier.weight(1f)
                    )
                    WeightChip(
                        label = "Elec Gross",
                        weight = lorry.electricGrossWeight,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(6.dp))

                // Row 2: Tare & Net Weights
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    WeightChip(
                        label = "Mill Tare",
                        weight = lorry.millTareWeight,
                        modifier = Modifier.weight(1f)
                    )
                    WeightChip(
                        label = "Elec Tare",
                        weight = lorry.electricTareWeight,
                        modifier = Modifier.weight(1f)
                    )
                    WeightChip(
                        label = "Mill Net",
                        weight = lorry.millNetWeight,
                        modifier = Modifier.weight(1f)
                    )
                    WeightChip(
                        label = "Elec Net",
                        weight = lorry.electricNetWeight,
                        modifier = Modifier.weight(1f)
                    )
                    WeightChip(
                        label = "Final Net",
                        weight = lorry.lowestNetWeight ?: lorry.netWeight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Process Workflow Stepper Progress Tracker
            val stageIndex = when (statusEnum) {
                LorryStatus.GATE_ENTRY -> 1
                LorryStatus.MILL_GROSS_PENDING -> 2
                LorryStatus.WAITING_FOR_UNLOADING -> 3
                LorryStatus.ELECTRIC_GROSS_DONE -> 4
                LorryStatus.ELECTRIC_TARE_DONE -> 5
                LorryStatus.MILL_TARE_PENDING -> 6
                LorryStatus.READY_FOR_GATE_EXIT, LorryStatus.COMPLETED -> 7
                else -> 1
            }
            ProcessFlowStepper(currentStageIndex = stageIndex)

            // Live Time Inside & AI Operational Predictions
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⏱ Entered: ${lorry.inTime.ifEmpty { "08:20 AM" }}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• Inside: $daysInsideText",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (daysInsideText.contains("Day") || daysInsideText == "Overdue") StatusRed else StatusGreen
                        )
                    }
                    Text(
                        text = "🤖 AI Est. Unloading: ~35 mins | Next: ${lorry.currentStage}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = Color(0xFF2563EB)
                    )
                }

                if (daysInsideText.contains("Day") || statusEnum == LorryStatus.OVERDUE) {
                    Surface(
                        color = StatusRed,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "SLA PRIORITY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Stepper Summary & Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Location: ${lorry.currentStage}",
                    style = MaterialTheme.typography.labelSmall,
                    color = IndustrialBlue,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (statusEnum == LorryStatus.COMPLETED) "View Details" else "Continue Workflow",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WeightChip(
    label: String,
    weight: Double?,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF6B7280),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = if (weight != null && weight > 0) "${weight.toInt()} kg" else "---",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ProcessFlowStepper(currentStageIndex: Int, modifier: Modifier = Modifier) {
    val stages = listOf("Gate Entry", "Mill Gross", "Unload", "Ele. Gross", "Ele. Tare", "Mill Verify", "Gate Out")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        stages.forEachIndexed { index, name ->
            val isDone = (index + 1) < currentStageIndex
            val isCurrent = (index + 1) == currentStageIndex

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(64.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isDone -> StatusGreen
                                isCurrent -> StatusBlue
                                else -> Color.LightGray.copy(alpha = 0.5f)
                            }
                        )
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isCurrent) StatusBlue else Color.Gray,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
