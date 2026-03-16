package com.ofppt.istak.ui.screens.formateur

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ofppt.istak.viewmodel.FormateurUiState
import com.ofppt.istak.viewmodel.FormateurViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormateurDashboardScreen(
    viewModel: FormateurViewModel = hiltViewModel(),
    onNavigateToAbsenceEntry: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val seances by viewModel.seances.collectAsState()
    val students by viewModel.students.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedWeekStart by viewModel.selectedWeekStart.collectAsState()
    val userName by viewModel.userName.collectAsState(initial = "Formateur")

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 1. Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Espace Formateur",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Appel et Suivi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                com.ofppt.istak.ui.components.ProfileAvatar(
                    name = userName,
                    size = 56.dp,
                    onClick = onNavigateToProfile
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Week Selector Card (Glassmorphic look)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { viewModel.onWeekSelected(selectedWeekStart.minusWeeks(1)) }) {
                        Icon(Icons.Default.ArrowLeft, contentDescription = "Prev", tint = MaterialTheme.colorScheme.primary)
                    }
                    Text(
                        text = viewModel.getWeekLabel(selectedWeekStart),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { viewModel.onWeekSelected(selectedWeekStart.plusWeeks(1)) }) {
                        Icon(Icons.Default.ArrowRight, contentDescription = "Next", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Day Selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(6) { i ->
                    val date = selectedWeekStart.plusDays(i.toLong())
                    val isSelected = date == selectedDate
                    val dayName = date.format(DateTimeFormatter.ofPattern("EEE", Locale.FRENCH)).replace(".", "")
                    val dayNum = date.format(DateTimeFormatter.ofPattern("dd"))

                    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                    Card(
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .width(64.dp)
                            .clickable { viewModel.onDateSelected(date) }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dayName.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = contentColor.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dayNum,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Group Selector
            var expandedGroup by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = viewModel.selectedGroup?.nom ?: "Sélectionner un groupe",
                    onValueChange = {},
                    label = { Text("Groupe d'étude") },
                    readOnly = true,
                    trailingIcon = { 
                        Icon(Icons.Default.ArrowDropDown, "Select", Modifier.clickable { expandedGroup = true }, tint = MaterialTheme.colorScheme.primary) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                DropdownMenu(
                    expanded = expandedGroup, 
                    onDismissRequest = { expandedGroup = false },
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
                ) {
                    groups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group.nom, color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                viewModel.onGroupSelected(group)
                                expandedGroup = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Seance and Students
            if (viewModel.selectedGroup == null) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Veuillez sélectionner un groupe", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (seances.isEmpty()) {
                 Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Aucune séance prévue ce jour", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                var expandedSeance by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = viewModel.selectedSeance?.let { "${it.start_time.take(5)} - ${it.end_time.take(5)} (${it.module})" } ?: "Sélectionner une séance",
                        onValueChange = {},
                        label = { Text("Séance / Module") },
                        readOnly = true,
                        trailingIcon = { 
                            Icon(Icons.Default.ArrowDropDown, "Select", Modifier.clickable { expandedSeance = true }, tint = MaterialTheme.colorScheme.primary) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    DropdownMenu(
                        expanded = expandedSeance, 
                        onDismissRequest = { expandedSeance = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        seances.forEach { seance ->
                            DropdownMenuItem(
                                text = { Text("${seance.start_time.take(5)} - ${seance.end_time.take(5)} (${seance.module})", color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    viewModel.onSeanceSelected(seance)
                                    expandedSeance = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                if (students.isNotEmpty() && viewModel.selectedSeance != null) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(students) { student ->
                            val cardBg = when {
                                student.is_blocked || student.isAbsent -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surface
                            }
                            
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleStudentAbsence(student.cef) },
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = student.isAbsent,
                                        onCheckedChange = { viewModel.toggleStudentAbsence(student.cef) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = MaterialTheme.colorScheme.error,
                                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "${student.nom} ${student.prenom}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (student.is_blocked || student.isAbsent) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = student.cef,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        
                                        if (!student.status_message.isNullOrEmpty()) {
                                            Text(
                                                text = student.status_message,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.padding(top = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.submitAbsences() },
                                modifier = Modifier.fillMaxWidth().height(56.dp).shadow(8.dp, RoundedCornerShape(16.dp), spotColor = MaterialTheme.colorScheme.primary),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(16.dp),
                                enabled = uiState !is FormateurUiState.Loading
                            ) {
                                if (uiState is FormateurUiState.Loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                                } else {
                                    Text("Valider l'Appel", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        val context = androidx.compose.ui.platform.LocalContext.current
        LaunchedEffect(uiState) {
            when (uiState) {
                is FormateurUiState.Success -> {
                    android.widget.Toast.makeText(context, (uiState as FormateurUiState.Success).message, android.widget.Toast.LENGTH_LONG).show()
                    viewModel.resetUiState()
                }
                is FormateurUiState.Error -> {
                    android.widget.Toast.makeText(context, (uiState as FormateurUiState.Error).message, android.widget.Toast.LENGTH_LONG).show()
                    viewModel.resetUiState()
                }
                else -> {}
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
