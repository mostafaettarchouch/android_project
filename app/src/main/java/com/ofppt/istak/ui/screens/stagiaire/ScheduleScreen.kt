package com.ofppt.istak.ui.screens.stagiaire

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ofppt.istak.data.model.DaySchedule
import com.ofppt.istak.data.model.Session
import com.ofppt.istak.viewmodel.ScheduleUiState
import com.ofppt.istak.viewmodel.ScheduleViewModel

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Mon Emploi du Temps",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

        when (uiState) {
            is ScheduleUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ScheduleUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = (uiState as ScheduleUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is ScheduleUiState.Success -> {
                val data = (uiState as ScheduleUiState.Success).data
                val currentWeekStart by viewModel.currentWeekStart.collectAsState()

                // Week Navigation
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(24.dp),
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
                        IconButton(onClick = { viewModel.previousWeek() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Previous Week", tint = MaterialTheme.colorScheme.primary)
                        }

                        Text(
                            text = "Semaine du ${currentWeekStart.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM", java.util.Locale.getDefault()))}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(onClick = { viewModel.nextWeek() }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Next Week", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha=0.2f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        var expanded by remember { mutableStateOf(false) }

                        if (!data.available_groups.isNullOrEmpty() && data.available_groups.size > 1) {
                            Box {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { expanded = true }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Groupe: ${data.group_name}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Group",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                ) {
                                    data.available_groups.forEach { group ->
                                        DropdownMenuItem(
                                            text = { Text(group.nom, color = MaterialTheme.colorScheme.onSurface) },
                                            onClick = {
                                                viewModel.onGroupSelected(group.id.toString())
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Groupe: ${data.group_name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Semaine du: ${data.week_start}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                ScheduleList(schedule = data.schedule)
            }
        }
    }
}
}

@Composable
fun ScheduleList(schedule: List<DaySchedule>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(schedule) { daySchedule ->
            DayGridItem(daySchedule)
        }
    }
}

@Composable
fun DayGridItem(daySchedule: DaySchedule) {
    Column {
        // Day Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.size(width = 6.dp, height = 24.dp)
            ) {}
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${daySchedule.day} ${daySchedule.date}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Grid of Sessions (2x2)
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: S1 & S2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val s1 = daySchedule.sessions.find { it.creneau_id == 1 }
                val s2 = daySchedule.sessions.find { it.creneau_id == 2 }

                Box(modifier = Modifier.weight(1f)) {
                    SessionCard(session = s1, label = "S1", time = "08:30 - 11:00")
                }
                Box(modifier = Modifier.weight(1f)) {
                    SessionCard(session = s2, label = "S2", time = "11:00 - 13:30")
                }
            }

            // Row 2: S3 & S4
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val s3 = daySchedule.sessions.find { it.creneau_id == 3 }
                val s4 = daySchedule.sessions.find { it.creneau_id == 4 }

                Box(modifier = Modifier.weight(1f)) {
                    SessionCard(session = s3, label = "S3", time = "13:30 - 16:00")
                }
                Box(modifier = Modifier.weight(1f)) {
                    SessionCard(session = s4, label = "S4", time = "16:00 - 18:30")
                }
            }
        }
    }
}

@Composable
fun SessionCard(session: Session?, label: String, time: String) {
    val isOccupied = session != null && session.module != null
    val containerColor = if (isOccupied) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val borderColor = if (isOccupied) MaterialTheme.colorScheme.outlineVariant else Color.Transparent

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp) // Fixed height for uniformity
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOccupied) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Label + Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            if (isOccupied) {
                Column {
                    Text(
                        text = session?.module ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = session?.salle ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = session?.formateur ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "-",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
