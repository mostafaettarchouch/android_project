package com.ofppt.istak.ui.screens.formateur

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ofppt.istak.viewmodel.FormateurProfileViewModel
import com.ofppt.istak.viewmodel.LogoutState
import com.ofppt.istak.viewmodel.ProfileUiState
import com.ofppt.istak.ui.screens.profile.ChangePasswordDialog

@Composable
fun FormateurProfileScreen(
    viewModel: FormateurProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val logoutState by viewModel.logoutState.collectAsState()
    var showPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(logoutState) {
        if (logoutState is LogoutState.Success) {
            onLogout()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mon Profil",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        when (uiState) {
            is ProfileUiState.Loading -> {
                CircularProgressIndicator()
            }
            is ProfileUiState.Error -> {
                Text(text = (uiState as ProfileUiState.Error).message, color = MaterialTheme.colorScheme.error)
            }
            is ProfileUiState.Success -> {
                val user = (uiState as ProfileUiState.Success).user

                // Profile Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(88.dp)
                        ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.padding(20.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = user.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha=0.1f)
                        ) {
                            Text(
                                text = "Formateur",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Button(
                    onClick = { showPasswordDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Changer le mot de passe", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    if (logoutState is LogoutState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.error)
                    } else {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Se déconnecter", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Contact Info
                val contactInfo by viewModel.contactInfo.collectAsState(initial = emptyMap())
                if (contactInfo.isNotEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Contact & Support", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val phone = contactInfo["phone"]
                        val email = contactInfo["email"]
                        
                        if (!phone.isNullOrBlank()) {
                            Text(text = "Tél: $phone", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (!email.isNullOrBlank()) {
                            Text(text = "Email: $email", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (!contactInfo["facebook"].isNullOrBlank()) {
                                IconButton(onClick = { uriHandler.openUri(contactInfo["facebook"]!!) }) {
                                    Icon(
                                        imageVector = com.ofppt.istak.ui.theme.SocialIcons.Facebook,
                                        contentDescription = "Facebook",
                                        tint = Color(0xFF1877F2),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            if (!contactInfo["instagram"].isNullOrBlank()) {
                                IconButton(onClick = { uriHandler.openUri(contactInfo["instagram"]!!) }) {
                                    Icon(
                                        imageVector = com.ofppt.istak.ui.theme.SocialIcons.Instagram,
                                        contentDescription = "Instagram",
                                        tint = Color(0xFFE4405F),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            if (!contactInfo["whatsapp"].isNullOrBlank()) {
                                IconButton(onClick = { uriHandler.openUri(contactInfo["whatsapp"]!!) }) {
                                    Icon(
                                        imageVector = com.ofppt.istak.ui.theme.SocialIcons.WhatsApp,
                                        contentDescription = "WhatsApp",
                                        tint = Color(0xFF25D366),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPasswordDialog) {
        // We need to cast viewModel to ProfileViewModel or make ChangePasswordDialog generic.
        // Since ChangePasswordDialog expects ProfileViewModel, we might need to duplicate it or make it accept a lambda/interface.
        // For simplicity, I'll duplicate the dialog logic here or update ChangePasswordDialog to be more flexible.
        // Actually, I can just copy the Dialog code into this file to avoid dependency issues or refactoring.
        
        ChangePasswordDialogInternal(
            onDismiss = { 
                showPasswordDialog = false 
                viewModel.resetPasswordState()
            },
            onSubmit = { current, new, confirm ->
                viewModel.updatePassword(current, new, confirm)
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun ChangePasswordDialogInternal(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit,
    viewModel: FormateurProfileViewModel
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val updateState by viewModel.passwordUpdateState.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Changer le mot de passe") },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Mot de passe actuel") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nouveau mot de passe") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmer le mot de passe") },
                    singleLine = true
                )
                
                // Error/Success messages...
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(currentPassword, newPassword, confirmPassword) }) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
