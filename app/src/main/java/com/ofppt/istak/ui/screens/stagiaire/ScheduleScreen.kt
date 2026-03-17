package com.ofppt.istak.ui.screens.stagiaire

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

import com.ofppt.istak.ui.theme.neumorphic
import com.ofppt.istak.ui.theme.NeumorphicColors

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
                modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
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

                // Neumorphic Week Navigation
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
                                .clickable { viewModel.previousWeek() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Prev", tint = MaterialTheme.colorScheme.primary)
                        }

                        Text(
                            text = "Semaine du ${currentWeekStart.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM", java.util.Locale.getDefault()))}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .neumorphic(shape = CircleShape, elevation = 2.dp)
                                .clickable { viewModel.nextWeek() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Neumorphic Group Selection
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphic(shape = RoundedCornerShape(24.dp), elevation = 6.dp)
                        .padding(20.dp)
                ) {
                    Column {
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

                Spacer(modifier = Modifier.height(24.dp))

                ScheduleList(schedule = data.schedule)
            }
        }
    }
}
}

@Composable
fun ScheduleList(schedule: List<DaySchedule>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(32.dp),
        contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
    ) {
        items(schedule) { daySchedule ->
            DayGridItem(daySchedule)
        }
    }
}

@Composable
fun DayGridItem(daySchedule: DaySchedule) {
    Column {
        // Day Header with Neumorphism
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 6.dp, height = 24.dp)
                    .neumorphic(shape = RoundedCornerShape(3.dp), elevation = 1.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${daySchedule.day} ${daySchedule.date}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Grid of Sessions
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .neumorphic(
                shape = RoundedCornerShape(20.dp), 
                elevation = if (isOccupied) 4.dp else 2.dp,
                isPressed = !isOccupied
            )
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOccupied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = time.split(" - ").first(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 10.sp
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
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .neumorphic(shape = RoundedCornerShape(6.dp), elevation = 1.dp)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = session?.salle ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = session?.formateur?.split(" ")?.lastOrNull() ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 9.sp
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Libre",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

