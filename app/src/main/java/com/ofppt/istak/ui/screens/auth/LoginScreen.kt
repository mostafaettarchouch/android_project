package com.ofppt.istak.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ofppt.istak.ui.theme.NeumorphicColors
import com.ofppt.istak.ui.theme.neumorphic
import com.ofppt.istak.viewmodel.LoginState
import com.ofppt.istak.viewmodel.LoginViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

// Generate a random 5-character string
fun generateCaptchaString(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (1..5).map { chars.random() }.joinToString("")
}

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var captchaInput by remember { mutableStateOf("") }
    var captchaCode by remember { mutableStateOf(generateCaptchaString()) }
    var captchaError by remember { mutableStateOf(false) }

    val loginState by viewModel.loginState.collectAsState()

    // Entrance Animation setup
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(800)) + slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(800)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo with Neumorphism
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .neumorphic(shape = CircleShape, elevation = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(50.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "ISTAK OFPPT",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Portail d'Authentification",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Neumorphic Form Container
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphic(shape = RoundedCornerShape(28.dp), elevation = 10.dp)
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Neumorphic Input for Email
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphic(shape = RoundedCornerShape(16.dp), elevation = 4.dp, isPressed = false)
                    ) {
                        TextField(
                            value = email,
                            onValueChange = { email = it; captchaError = false },
                            placeholder = { Text("Nom d'utilisateur / CEF") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Neumorphic Input for Password
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphic(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                    ) {
                        TextField(
                            value = password,
                            onValueChange = { password = it; captchaError = false },
                            placeholder = { Text("Mot de passe") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Advanced Captcha Box
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .neumorphic(shape = RoundedCornerShape(16.dp), elevation = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val onBgColor = MaterialTheme.colorScheme.onBackground
                            
                            // Captcha Text
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                captchaCode.forEach { char ->
                                    Text(
                                        text = char.toString(),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 4.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .neumorphic(shape = CircleShape, elevation = 4.dp)
                                .clickable {
                                    captchaCode = generateCaptchaString()
                                    captchaInput = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphic(shape = RoundedCornerShape(16.dp), elevation = 4.dp)
                    ) {
                        TextField(
                            value = captchaInput,
                            onValueChange = { captchaInput = it; captchaError = false },
                            placeholder = { Text("Code de sécurité") },
                            isError = captchaError,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                    
                    AnimatedVisibility(visible = captchaError) {
                        Text(
                            "Code CAPTCHA incorrect", 
                            color = MaterialTheme.colorScheme.error, 
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp).align(Alignment.Start)
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Neumorphic Action Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .neumorphic(
                                shape = RoundedCornerShape(16.dp), 
                                elevation = 6.dp,
                                lightShadowColor = if (loginState !is LoginState.Loading) NeumorphicColors.lightShadow() else Color.Transparent,
                                darkShadowColor = if (loginState !is LoginState.Loading) NeumorphicColors.darkShadow() else Color.Transparent
                            )
                            .clickable(enabled = loginState !is LoginState.Loading) {
                                if (captchaInput.equals(captchaCode, ignoreCase = true)) {
                                    captchaError = false
                                    viewModel.login(email, password)
                                } else {
                                    captchaError = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (loginState is LoginState.Loading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                "Se Connecter", 
                                fontSize = 18.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    AnimatedVisibility(visible = loginState is LoginState.Error) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = (loginState as? LoginState.Error)?.message ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            val role = (loginState as LoginState.Success).user.role
            onLoginSuccess(role)
        }
    }
}
