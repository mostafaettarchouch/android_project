package com.ofppt.istak.ui.screens.formateur

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ofppt.istak.viewmodel.FormateurScheduleViewModel
import com.ofppt.istak.viewmodel.ScheduleUiState
import com.ofppt.istak.ui.screens.stagiaire.DayGridItem

@Composable
fun FormateurScheduleScreen(
    viewModel: FormateurScheduleViewModel = hiltViewModel()
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

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(data.schedule) { daySchedule ->
                        DayGridItem(daySchedule)
                    }
                }
            }
        }
    }
}}
