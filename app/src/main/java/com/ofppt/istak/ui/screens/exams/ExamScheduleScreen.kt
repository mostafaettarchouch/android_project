package com.ofppt.istak.ui.screens.exams

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ofppt.istak.data.model.DaySchedule
import com.ofppt.istak.data.model.Session
import com.ofppt.istak.viewmodel.ExamScheduleViewModel
import com.ofppt.istak.viewmodel.ScheduleUiState

@Composable
fun ExamScheduleScreen(
    viewModel: ExamScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Calendrier des Examens",
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

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha=0.2f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Groupe: ${data.group_name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Semaine du: ${data.week_start}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Week Navigation (Only if multiple weeks)
                if (!data.available_weeks.isNullOrEmpty() && data.available_weeks.size > 1) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
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

                            // Format date
                            val dateStr = try {
                                java.time.LocalDate.parse(data.week_start).format(java.time.format.DateTimeFormatter.ofPattern("dd MMM", java.util.Locale.getDefault()))
                            } catch (e: Exception) { data.week_start }

                            Text(
                                text = "Semaine du $dateStr",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            IconButton(onClick = { viewModel.nextWeek() }) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Next Week", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                ExamScheduleList(schedule = data.schedule)
            }
            }
        }
    }
}

@Composable
fun ExamScheduleList(schedule: List<DaySchedule>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(schedule) { daySchedule ->
            DayExamGridItem(daySchedule)
        }
    }
}

@Composable
fun DayExamGridItem(daySchedule: DaySchedule) {
    Column {
        // Day Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.error, // Red for Exams
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
                    ExamSessionCard(session = s1, label = "S1", time = "08:30 - 11:00")
                }
                Box(modifier = Modifier.weight(1f)) {
                    ExamSessionCard(session = s2, label = "S2", time = "11:00 - 13:30")
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
                    ExamSessionCard(session = s3, label = "S3", time = "13:30 - 16:00")
                }
                Box(modifier = Modifier.weight(1f)) {
                    ExamSessionCard(session = s4, label = "S4", time = "16:00 - 18:30")
                }
            }
        }
    }
}

@Composable
fun ExamSessionCard(session: Session?, label: String, time: String) {
    val isExam = session != null && session.module != null
    // Red accent for exams
    val containerColor =
        if (isExam) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = 0.5f
        )
    val borderColor = if (isExam) MaterialTheme.colorScheme.error else Color.Transparent
    val textColor =
        if (isExam) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val labelColor =
        if (isExam) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .border(1.dp, borderColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExam) 2.dp else 0.dp)
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
                    color = labelColor
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            if (isExam) {
                Column {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "Examen",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = session?.module ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = session?.salle ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
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
