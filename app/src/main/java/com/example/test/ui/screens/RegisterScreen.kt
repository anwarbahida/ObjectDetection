package com.example.test.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.test.data.network.RetrofitInstance
import com.example.test.ui.navigation.Routers
import kotlinx.coroutines.launch

// ─────────────────────────────────────────
// Couleurs Dark Mode (identiques)
// ─────────────────────────────────────────
private val DarkBackground = Color(0xFF0F0F1A)
private val DarkSurface    = Color(0xFF1A1A2E)
private val DarkCard       = Color(0xFF16213E)
private val AccentPurple   = Color(0xFF7C4DFF)
private val AccentBlue     = Color(0xFF00B4D8)
private val AccentGreen    = Color(0xFF00E676)
private val TextPrimary    = Color(0xFFE0E0E0)
private val TextSecondary  = Color(0xFF9E9E9E)

@Composable
fun RegisterScreen(navController: NavController) {

    var name        by remember { mutableStateOf("") }
    var username    by remember { mutableStateOf("") }
    var email       by remember { mutableStateOf("") }
    var phone       by remember { mutableStateOf("") }
    var website     by remember { mutableStateOf("") }
    var street      by remember { mutableStateOf("") }
    var suite       by remember { mutableStateOf("") }
    var city        by remember { mutableStateOf("") }
    var zipcode     by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var isLoading   by remember { mutableStateOf(false) }
    var buttonClicked by remember { mutableStateOf(false) }
    var formVisible   by remember { mutableStateOf(false) }

    val scope             = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { formVisible = true }

    Scaffold(
        containerColor = DarkBackground,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color(0xFF0A2A1A),
                        contentColor = AccentGreen,
                        shape = RoundedCornerShape(13.dp)
                    )
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(32.dp))

                // ── Header ───────────────────────────────────
                AnimatedVisibility(
                    visible = formVisible,
                    enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { -20 })
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(AccentPurple, AccentBlue)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Créer un compte",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Remplissez les informations ci-dessous",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ── Card Infos personnelles ───────────────────
                AnimatedVisibility(
                    visible = formVisible,
                    enter = fadeIn(tween(400, delayMillis = 100)) +
                            slideInVertically(initialOffsetY = { 60 }, animationSpec = tween(400, delayMillis = 100))
                ) {
                    RegisterCard(title = "Informations personnelles", icon = Icons.Default.Person) {

                        DarkTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Nom complet",
                            icon = Icons.Default.Person,
                            delay = 100
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DarkTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = "Nom d'utilisateur",
                            icon = Icons.Default.AccountCircle,
                            delay = 150
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DarkTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email",
                            icon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email,
                            delay = 200
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DarkTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = "Téléphone",
                            icon = Icons.Default.Phone,
                            keyboardType = KeyboardType.Phone,
                            delay = 250
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DarkTextField(
                            value = website,
                            onValueChange = { website = it },
                            label = "Site web",
                            icon = Icons.Default.Web,
                            delay = 300
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Card Adresse ──────────────────────────────
                AnimatedVisibility(
                    visible = formVisible,
                    enter = fadeIn(tween(400, delayMillis = 200)) +
                            slideInVertically(initialOffsetY = { 60 }, animationSpec = tween(400, delayMillis = 200))
                ) {
                    RegisterCard(title = "Adresse", icon = Icons.Default.Home) {

                        DarkTextField(
                            value = street,
                            onValueChange = { street = it },
                            label = "Rue",
                            icon = Icons.Default.LocationOn,
                            delay = 100
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DarkTextField(
                            value = suite,
                            onValueChange = { suite = it },
                            label = "Appartement / Suite",
                            icon = Icons.Default.MeetingRoom,
                            delay = 150
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DarkTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = "Ville",
                                icon = Icons.Default.LocationCity,
                                modifier = Modifier.weight(1f),
                                delay = 200
                            )
                            DarkTextField(
                                value = zipcode,
                                onValueChange = { zipcode = it },
                                label = "Code postal",
                                icon = Icons.Default.Pin,
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(1f),
                                delay = 250
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Card Entreprise ───────────────────────────
                AnimatedVisibility(
                    visible = formVisible,
                    enter = fadeIn(tween(400, delayMillis = 300)) +
                            slideInVertically(initialOffsetY = { 60 }, animationSpec = tween(400, delayMillis = 300))
                ) {
                    RegisterCard(title = "Entreprise", icon = Icons.Default.Business) {
                        DarkTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = "Nom de l'entreprise",
                            icon = Icons.Default.Business,
                            delay = 100
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ── Bouton S'inscrire ─────────────────────────
                AnimatedVisibility(
                    visible = formVisible,
                    enter = fadeIn(tween(300, delayMillis = 400)) + scaleIn(initialScale = 0.8f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                if (name.isBlank() || email.isBlank() || username.isBlank()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Veuillez remplir les champs obligatoires")
                                    }
                                    return@Button
                                }
                                buttonClicked = !buttonClicked
                                isLoading = true
                                scope.launch {
                                    try {
                                        val users = RetrofitInstance.api.getUsers()
                                        val alreadyExists = users.any {
                                            it.email == email || it.username == username
                                        }
                                        if (alreadyExists) {
                                            snackbarHostState.showSnackbar("Email ou username déjà utilisé")
                                        } else {
                                            snackbarHostState.showSnackbar("Compte créé avec succès !")
                                            navController.navigate(Routers.LOGIN) {
                                                popUpTo(Routers.REGISTER) { inclusive = true }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Erreur réseau : ${e.message}")
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .scale(if (buttonClicked) 0.97f else 1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(0.dp),
                            enabled = !isLoading
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (!isLoading)
                                            Brush.horizontalGradient(listOf(AccentPurple, AccentBlue))
                                        else
                                            Brush.horizontalGradient(listOf(AccentPurple.copy(alpha = 0.5f), AccentBlue.copy(alpha = 0.5f))),
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.HowToReg,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "S'inscrire",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // ── Divider ──────────────────────────
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Divider(modifier = Modifier.weight(1f), color = TextSecondary.copy(alpha = 0.3f))
                            Text("  ou  ", color = TextSecondary, fontSize = 12.sp)
                            Divider(modifier = Modifier.weight(1f), color = TextSecondary.copy(alpha = 0.3f))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // ── Bouton Login ──────────────────────
                        OutlinedButton(
                            onClick = { navController.navigate(Routers.LOGIN) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(AccentPurple, AccentBlue)
                                )
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Login,
                                    contentDescription = null,
                                    tint = AccentPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Déjà un compte ? Se connecter",
                                    color = AccentPurple,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Composant Card de section
// ─────────────────────────────────────────────────────────────────
@Composable
fun RegisterCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Titre de section avec icône
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(listOf(AccentPurple, AccentBlue))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Composant TextField Dark
// ─────────────────────────────────────────────────────────────────
@Composable
fun DarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text,
    delay: Int = 0
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label, color = TextSecondary, fontSize = 12.sp)
        },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(18.dp))
        },
        singleLine = true,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentPurple,
            unfocusedBorderColor = DarkSurface,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = AccentPurple,
            focusedContainerColor = DarkSurface,
            unfocusedContainerColor = DarkSurface,
            focusedLabelColor = AccentPurple,
            unfocusedLabelColor = TextSecondary
        )
    )
}
