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

import com.ofppt.istak.ui.theme.neumorphic
import com.ofppt.istak.ui.theme.NeumorphicColors

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
                
                Box(modifier = Modifier.neumorphic(shape = CircleShape, elevation = 4.dp).padding(4.dp)) {
                    com.ofppt.istak.ui.components.ProfileAvatar(
                        name = userName,
                        size = 52.dp,
                        onClick = onNavigateToProfile
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Week Selector with Neumorphism
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphic(shape = RoundedCornerShape(20.dp), elevation = 4.dp)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .neumorphic(shape = CircleShape, elevation = 2.dp)
                            .clickable { viewModel.onWeekSelected(selectedWeekStart.minusWeeks(1)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowLeft, contentDescription = "Prev", tint = MaterialTheme.colorScheme.primary)
                    }
                    
                    Text(
                        text = viewModel.getWeekLabel(selectedWeekStart),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .neumorphic(shape = CircleShape, elevation = 2.dp)
                            .clickable { viewModel.onWeekSelected(selectedWeekStart.plusWeeks(1)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ArrowRight, contentDescription = "Next", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Day Selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
            ) {
                items(6) { i ->
                    val date = selectedWeekStart.plusDays(i.toLong())
                    val isSelected = date == selectedDate
                    val dayName = date.format(DateTimeFormatter.ofPattern("EEE", Locale.FRENCH)).replace(".", "")
                    val dayNum = date.format(DateTimeFormatter.ofPattern("dd"))

                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .neumorphic(
                                shape = RoundedCornerShape(16.dp), 
                                elevation = if (isSelected) 0.dp else 4.dp,
                                isPressed = isSelected
                            )
                            .clickable { viewModel.onDateSelected(date) }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dayName.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dayNum,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Group Selector
            var expandedGroup by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphic(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                        .clickable { expandedGroup = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Groupe d'étude", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = viewModel.selectedGroup?.nom ?: "Sélectionner un groupe",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(Icons.Default.ArrowDropDown, "Select", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                
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
            if (viewModel.selectedGroup != null) {
                var expandedSeance by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphic(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                            .clickable { expandedSeance = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Séance / Module", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = viewModel.selectedSeance?.let { "${it.start_time.take(5)} - ${it.end_time.take(5)} (${it.module})" } ?: "Sélectionner une séance",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, "Select", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    
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
                
                Spacer(modifier = Modifier.height(20.dp))

                if (students.isNotEmpty() && viewModel.selectedSeance != null) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
                    ) {
                        items(students) { student ->
                            val isAbsent = student.isAbsent
                            val isBlocked = student.is_blocked
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .neumorphic(
                                        shape = RoundedCornerShape(20.dp), 
                                        elevation = 4.dp,
                                        darkShadowColor = if (isAbsent || isBlocked) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) else NeumorphicColors.darkShadow()
                                    )
                                    .clickable { viewModel.toggleStudentAbsence(student.cef) }
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isAbsent,
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
                                            color = if (isBlocked || isAbsent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
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
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(top = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .neumorphic(shape = RoundedCornerShape(16.dp), elevation = 6.dp)
                                    .clickable(enabled = uiState !is FormateurUiState.Loading) {
                                        viewModel.submitAbsences()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState is FormateurUiState.Loading) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Valider l'Appel", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Veuillez sélectionner un groupe", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
