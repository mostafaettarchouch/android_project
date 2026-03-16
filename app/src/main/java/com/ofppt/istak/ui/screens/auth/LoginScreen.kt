package com.ofppt.istak.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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

    // Animation states for floating background elements
    val infiniteTransition = rememberInfiniteTransition(label = "bg_float")
    val offsetY1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "circle1"
    )
    val offsetY2 by infiniteTransition.animateFloat(
        initialValue = 50f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "circle2"
    )

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
        // Floating Background Elements to simulate Glassmorphism depth
        Box(
            modifier = Modifier
                .offset(x = (-50).dp, y = (-50 + offsetY1).dp)
                .size(250.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                .blur(80.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = (50 + offsetY2).dp)
                .size(300.dp)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), CircleShape)
                .blur(100.dp)
        )

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(800)) + slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(800)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            ),
                            shape = CircleShape
                        )
                        .shadow(8.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
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

                Spacer(modifier = Modifier.height(32.dp))

                // Glassmorphic Card Form
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; captchaError = false },
                            label = { Text("Nom d'utilisateur / CEF") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; captchaError = false },
                            label = { Text("Mot de passe") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Advanced Captcha Box
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                val canvasPrimary = MaterialTheme.colorScheme.primary
                                val canvasSecondary = MaterialTheme.colorScheme.secondary
                                val onBgColor = MaterialTheme.colorScheme.onBackground

                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val canvasWidth = size.width
                                    val canvasHeight = size.height
                                    
                                    // 1. Draw Noise Dots
                                    repeat(150) {
                                        drawCircle(
                                            color = onBgColor.copy(alpha = 0.15f),
                                            radius = Random.nextFloat() * 3f,
                                            center = Offset(
                                                x = Random.nextFloat() * canvasWidth,
                                                y = Random.nextFloat() * canvasHeight
                                            )
                                        )
                                    }

                                    // 2. Draw Bezier Interference Lines
                                    repeat(4) {
                                        val path = Path().apply {
                                            moveTo(0f, Random.nextFloat() * canvasHeight)
                                            quadraticBezierTo(
                                                canvasWidth / 2, Random.nextFloat() * canvasHeight,
                                                canvasWidth, Random.nextFloat() * canvasHeight
                                            )
                                        }
                                        drawPath(
                                            path = path,
                                            color = listOf(canvasPrimary, canvasSecondary).random().copy(alpha = 0.4f),
                                            style = Stroke(width = Random.nextFloat() * 3f + 1f)
                                        )
                                    }
                                }

                                // Captcha Text with slight individual character offset/rotation simulation
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    captchaCode.forEach { char ->
                                        Text(
                                            text = char.toString(),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 4.sp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                            modifier = Modifier.offset(
                                                x = (Random.nextFloat() * 4 - 2).dp,
                                                y = (Random.nextFloat() * 8 - 4).dp
                                            )
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            IconButton(
                                onClick = { 
                                    captchaCode = generateCaptchaString()
                                    captchaInput = ""
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh Captcha", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = captchaInput,
                            onValueChange = { captchaInput = it; captchaError = false },
                            label = { Text("Code de sécurité") },
                            isError = captchaError,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        AnimatedVisibility(visible = captchaError) {
                            Text(
                                "Code CAPTCHA incorrect", 
                                color = MaterialTheme.colorScheme.error, 
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp).align(Alignment.Start)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                if (captchaInput.equals(captchaCode, ignoreCase = true)) {
                                    captchaError = false
                                    viewModel.login(email, password)
                                } else {
                                    captchaError = true
                                    // Optionally logic to regenerate on multiple fails
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(0.dp),
                            enabled = loginState !is LoginState.Loading
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (loginState is LoginState.Loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                                } else {
                                    Text("Se Connecter", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
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
    }

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            val role = (loginState as LoginState.Success).user.role
            onLoginSuccess(role)
        }
    }
}
