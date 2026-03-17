package com.ofppt.istak.ui.screens.stagiaire

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ofppt.istak.viewmodel.StagiaireUiState
import com.ofppt.istak.viewmodel.StagiaireViewModel

import com.ofppt.istak.ui.theme.neumorphic
import com.ofppt.istak.ui.theme.NeumorphicColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StagiaireDashboardScreen(
    viewModel: StagiaireViewModel = hiltViewModel(),
    onNavigateToSchedule: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            when (uiState) {
                is StagiaireUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is StagiaireUiState.Error -> {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = (uiState as StagiaireUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is StagiaireUiState.Success -> {
                    val data = (uiState as StagiaireUiState.Success).data

                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Bonjour, ${data.student.prenom}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable(enabled = data.groups.size > 1) { expanded = true }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Groupe: ${data.student.groupe_nom}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (data.groups.size > 1) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Switch Group",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                ) {
                                    data.groups.forEach { group ->
                                        DropdownMenuItem(
                                            text = { Text(group.nom, color = MaterialTheme.colorScheme.onSurface) },
                                            onClick = {
                                                viewModel.switchGroup(group.id)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        Box(modifier = Modifier.neumorphic(shape = CircleShape, elevation = 4.dp).padding(4.dp)) {
                            com.ofppt.istak.ui.components.ProfileAvatar(
                                name = data.student.prenom,
                                size = 52.dp,
                                onClick = onNavigateToProfile
                            )
                        }
                    }

                    // Neumorphic Access Status Card
                    val isAuthorized = data.student.is_authorized_to_enter
                    val statusColor = if (isAuthorized) Color(0xFF10B981) else Color(0xFFEF4444)
                    val statusText = if (isAuthorized) "Accès Autorisé" else "Accès Interdit"
                    val statusDesc = if (isAuthorized) "Vous pouvez entrer en classe." else "Veuillez contacter l'administration."
                    val statusIcon = if (isAuthorized) Icons.Default.CheckCircle else Icons.Default.Block

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphic(shape = RoundedCornerShape(28.dp), elevation = 6.dp)
                            .padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .neumorphic(shape = CircleShape, elevation = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                            Column {
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                                Text(
                                    text = statusDesc,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Row 2: Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        BentoCard(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            title = "Total Heures",
                            value = "${data.stats.total_hours}h",
                            icon = Icons.Default.Schedule,
                            color = MaterialTheme.colorScheme.primary
                        )
                        BentoCard(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            title = "Sanctions",
                            value = "${data.sanctions.size}",
                            icon = Icons.Default.Warning,
                            color = if (data.sanctions.isNotEmpty()) Color(0xFFF43F5E) else MaterialTheme.colorScheme.tertiary
                        )
                    }

                    // Row 3: Justified vs Unjustified
                    Row(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .neumorphic(shape = RoundedCornerShape(24.dp), elevation = 4.dp)
                                .padding(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Text("Justifiées", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${data.stats.total_justified}h", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .neumorphic(shape = RoundedCornerShape(24.dp), elevation = 4.dp)
                                .padding(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Text("Non Justifiées", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${data.stats.total_unjustified}h", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                            }
                        }
                    }

                    // History Section
                    Text(
                        text = "Historique Récent",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (data.absences.isEmpty()) {
                            Text("Aucune absence enregistrée.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            data.absences.take(5).forEach { absence ->
                                ActivityItem(absence)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
        
        // Chat FAB with Neumorphism
        val messageViewModel: com.ofppt.istak.viewmodel.MessageViewModel = hiltViewModel()
        val unreadCount by messageViewModel.unreadCount.collectAsState()
        var showChatDialog by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp)
                .neumorphic(shape = CircleShape, elevation = 6.dp)
                .clickable { showChatDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Box {
                Icon(imageVector = Icons.Default.Email, contentDescription = "Messages", tint = MaterialTheme.colorScheme.primary)
                if (unreadCount > 0) {
                    Badge(
                        modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp),
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Text(text = unreadCount.toString(), color = Color.White)
                    }
                }
            }
        }

        if (showChatDialog) {
            com.ofppt.istak.ui.components.ChatDialog(
                viewModel = messageViewModel,
                onDismiss = { showChatDialog = false }
            )
        }
    }
}

@Composable
fun BentoCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Box(
        modifier = modifier
            .neumorphic(shape = RoundedCornerShape(24.dp), elevation = 4.dp)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .neumorphic(shape = RoundedCornerShape(10.dp), elevation = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
                Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ActivityItem(absence: com.ofppt.istak.data.model.Absence) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neumorphic(shape = RoundedCornerShape(20.dp), elevation = 3.dp)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = formatDate(absence.date_absence),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val sessions = mutableListOf<String>()
                if (absence.seance_1) sessions.add("S1")
                if (absence.seance_2) sessions.add("S2")
                if (absence.seance_3) sessions.add("S3")
                if (absence.seance_4) sessions.add("S4")
                
                Text(
                    text = sessions.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            val hours = listOf(absence.seance_1, absence.seance_2, absence.seance_3, absence.seance_4).count { it } * 2.5
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${hours}h",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (absence.motif != null) {
                    Text("Justifié", color = Color(0xFF10B981), style = MaterialTheme.typography.labelSmall)
                } else {
                    Text("Non justifié", color = Color(0xFFEF4444), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}


fun formatDate(dateStr: String): String {
    return try {
        val date = java.time.LocalDate.parse(dateStr.take(10))
        date.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy", java.util.Locale.getDefault()))
    } catch (e: Exception) {
        dateStr
    }
}
