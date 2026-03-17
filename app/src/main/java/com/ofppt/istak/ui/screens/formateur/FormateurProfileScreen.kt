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

import com.ofppt.istak.ui.theme.neumorphic
import com.ofppt.istak.ui.theme.NeumorphicColors
import androidx.compose.foundation.clickable
import com.ofppt.istak.ui.screens.profile.SocialIconNeumorphic
import com.ofppt.istak.viewmodel.PasswordUpdateState

import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun FormateurProfileScreen(
    viewModel: FormateurProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val logoutState by viewModel.logoutState.collectAsState()
    val darkModePref by viewModel.isDarkMode.collectAsState(initial = null)
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = darkModePref ?: isSystemDark
    
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
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mon Profil",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)
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

                // Neumorphic Profile Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphic(shape = RoundedCornerShape(28.dp), elevation = 6.dp)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .neumorphic(shape = CircleShape, elevation = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(text = user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = user.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .neumorphic(shape = RoundedCornerShape(8.dp), elevation = 1.dp)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Formateur",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Neumorphic Theme Toggle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .neumorphic(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                        .clickable { viewModel.toggleDarkMode(!isDark) }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode, 
                                contentDescription = null, 
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = if (isDark) "Mode Sombre" else "Mode Clair", 
                                fontWeight = FontWeight.Bold, 
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = isDark,
                            onCheckedChange = { viewModel.toggleDarkMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Neumorphic Action Buttons
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .neumorphic(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                        .clickable { showPasswordDialog = true }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Changer le mot de passe", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .neumorphic(
                            shape = RoundedCornerShape(16.dp), 
                            elevation = 4.dp,
                            darkShadowColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        )
                        .clickable(enabled = logoutState !is LogoutState.Loading) { viewModel.logout() }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (logoutState is LogoutState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.error)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Se déconnecter", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Contact Info
                val contactInfo by viewModel.contactInfo.collectAsState(initial = emptyMap())
                if (contactInfo.isNotEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Contact & Support", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val phone = contactInfo["phone"]
                        val email = contactInfo["email"]
                        
                        if (!phone.isNullOrBlank()) {
                            Text(text = "Tél: $phone", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (!email.isNullOrBlank()) {
                            Text(text = "Email: $email", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            if (!contactInfo["facebook"].isNullOrBlank()) {
                                SocialIconNeumorphic(
                                    icon = com.ofppt.istak.ui.theme.SocialIcons.Facebook,
                                    color = Color(0xFF1877F2),
                                    onClick = { uriHandler.openUri(contactInfo["facebook"]!!) }
                                )
                            }
                            if (!contactInfo["instagram"].isNullOrBlank()) {
                                SocialIconNeumorphic(
                                    icon = com.ofppt.istak.ui.theme.SocialIcons.Instagram,
                                    color = Color(0xFFE4405F),
                                    onClick = { uriHandler.openUri(contactInfo["instagram"]!!) }
                                )
                            }
                            if (!contactInfo["whatsapp"].isNullOrBlank()) {
                                SocialIconNeumorphic(
                                    icon = com.ofppt.istak.ui.theme.SocialIcons.WhatsApp,
                                    color = Color(0xFF25D366),
                                    onClick = { uriHandler.openUri(contactInfo["whatsapp"]!!) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPasswordDialog) {
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
        confirmButton = {
            Button(
                onClick = { onSubmit(currentPassword, newPassword, confirmPassword) },
                enabled = updateState !is PasswordUpdateState.Loading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (updateState is PasswordUpdateState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Enregistrer")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        },
        title = { Text("Changer le mot de passe", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Box(modifier = Modifier.fillMaxWidth().neumorphic(shape = RoundedCornerShape(12.dp), elevation = 2.dp)) {
                    TextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        placeholder = { Text("Mot de passe actuel") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().neumorphic(shape = RoundedCornerShape(12.dp), elevation = 2.dp)) {
                    TextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        placeholder = { Text("Nouveau mot de passe") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().neumorphic(shape = RoundedCornerShape(12.dp), elevation = 2.dp)) {
                    TextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("Confirmer le mot de passe") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                if (updateState is PasswordUpdateState.Error) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = (updateState as PasswordUpdateState.Error).message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                if (updateState is PasswordUpdateState.Success) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = (updateState as PasswordUpdateState.Success).message, color = Color(0xFF4CAF50), style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}

