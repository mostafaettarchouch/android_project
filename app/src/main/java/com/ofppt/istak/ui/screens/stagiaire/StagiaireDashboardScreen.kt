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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        com.ofppt.istak.ui.components.ProfileAvatar(
                            name = data.student.prenom,
                            size = 56.dp,
                            onClick = onNavigateToProfile
                        )
                    }

                    // BENTO GRID LAYOUT
                    
                    // Row 1: Access Status Card (Full Width)
                    val isAuthorized = data.student.is_authorized_to_enter
                    val statusColor = if (isAuthorized) Color(0xFF10B981) else Color(0xFFEF4444)
                    val statusBgColor = if (isAuthorized) Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFEF4444).copy(alpha = 0.1f)
                    val statusText = if (isAuthorized) "Accès Autorisé" else "Accès Interdit"
                    val statusDesc = if (isAuthorized) "Vous pouvez entrer en classe." else "Veuillez contacter l'administration."
                    val statusIcon = if (isAuthorized) Icons.Default.CheckCircle else Icons.Default.Block

                    Card(
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = statusBgColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha=0.3f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(statusColor.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                            Column(verticalArrangement = Arrangement.Center) {
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

                    // Row 2: Total Hours (Left) & Sanctions count (Right)
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

                    // Row 3: Justified vs Unjustified (Left & Right)
                    Row(
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
                                Text("Justifiées", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("${data.stats.total_justified}h", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }
                        
                        Card(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
                                Text("Non Justifiées", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("${data.stats.total_unjustified}h", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                            }
                        }
                    }

                    // History Section
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Historique Récent",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (data.absences.isEmpty()) {
                            Text("Aucune absence enregistrée.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            data.absences.take(5).forEach { absence ->
                                ActivityItem(absence)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for FAB
                }
            }
        }
        
        // Chat FAB
        val messageViewModel: com.ofppt.istak.viewmodel.MessageViewModel = hiltViewModel()
        val unreadCount by messageViewModel.unreadCount.collectAsState()
        var showChatDialog by remember { mutableStateOf(false) }

        FloatingActionButton(
            onClick = { showChatDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .shadow(16.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Box {
                Icon(imageVector = Icons.Default.Email, contentDescription = "Messages")
                if (unreadCount > 0) {
                    Badge(modifier = Modifier.align(Alignment.TopEnd)) {
                        Text(text = unreadCount.toString())
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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha=0.2f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Column {
                Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
                Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ActivityItem(absence: com.ofppt.istak.data.model.Absence) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
