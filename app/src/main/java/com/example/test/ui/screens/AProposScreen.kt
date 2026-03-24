package com.example.test.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

private val DarkBackground = Color(0xFF0F0F1A)
private val DarkSurface    = Color(0xFF1A1A2E)
private val DarkCard       = Color(0xFF16213E)
private val AccentPurple   = Color(0xFF7C4DFF)
private val AccentBlue     = Color(0xFF14C7EA)
private val AccentGreen    = Color(0xFF00E676)
private val TextPrimary    = Color(0xFFE0E0E0)
private val TextSecondary  = Color(0xFF9E9E9E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AProposScreen(navController: NavController) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope       = rememberCoroutineScope()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerMenu(
                navController = navController,
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            containerColor = DarkBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "À propos",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = AccentPurple
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkSurface
                    )
                )
            }
        ) { paddingValues ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(DarkBackground)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── Logo ──────────────────────────────
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { -40 })
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(AccentPurple, AccentBlue)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(44.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Detection Objects",
                                color = TextPrimary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Version 1.0.0",
                                color = AccentPurple,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // ── Infos sur l'app ──────────────────────────
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(500)) + slideInVertically(initialOffsetY = { 40 })
                    ) {
                        SectionCard(title = "L'application", icon = Icons.Default.Apps, color = AccentPurple) {
                            InfoRow(label = "Nom",         value = "Detection Objects")
                            InfoRow(label = "Version",     value = "1.0.0")
                            InfoRow(label = "Plateforme",  value = "Android")
                            InfoRow(label = "API utilisée",value = "jsonplaceholder.typicode.com")
                            InfoRow(label = "Langue",      value = "Français / Kotlin")
                            InfoRow(label = "Framework",      value = "Jetpack Compose")

                        }
                    }
                }

                // ── Technologies ─────────────────────────────
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { 40 })
                    ) {
                        SectionCard(title = "Technologies", icon = Icons.Default.Code, color = AccentBlue) {
                            TechBadgeRow(
                                techs = listOf(
                                    "Jetpack Compose" to AccentPurple,
                                    "Kotlin"          to AccentBlue,
                                    "Retrofit"        to AccentGreen,
                                    "ViewModel"       to Color(0xFFFF6D00),
                                    "StateFlow"       to Color(0xFFE91E63),
                                    "Navigation"      to AccentGreen,
                                    "SharedPrefs"     to AccentPurple,
                                    "CameraX"         to Color(0xFF9D4101),
                                    "TextToSpeech"    to Color(0xFFC21206),
                                )
                            )
                        }
                    }
                }

                // ── Équipe / Développeur ─────────────────────
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(700)) + slideInVertically(initialOffsetY = { 40 })
                    ) {
                        SectionCard(title = "Développeur", icon = Icons.Default.Person, color = AccentGreen) {
                            InfoRow(label = "Nom",      value = "Anwar Bahida")
                            InfoRow(label = "Rôle",     value = "Développeur Mobile")
                            InfoRow(label = "Stack",    value = "Android / Kotlin")
                            InfoRow(label = "Contact",  value = "anwar.bahida@b3g.com")
                        }
                    }
                }

                // ── Footer ───────────────────────────────────
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(800))
                    ) {
                        Text(
                            text = "©2026 : by Bahida Anwar",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// SectionCard — carte avec titre et icône
// ─────────────────────────────────────────────────────────────────
@Composable
fun SectionCard(
    title  : String,
    icon   : ImageVector,
    color  : Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Titre de section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector    = icon,
                        contentDescription = null,
                        tint           = color,
                        modifier       = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text       = title,
                    color      = color,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
            }

            Divider(
                color    = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// InfoRow — ligne label : valeur
// ─────────────────────────────────────────────────────────────────
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 14.sp)
        Text(text = value, color = TextPrimary,   fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

// ─────────────────────────────────────────────────────────────────
// TechBadgeRow — badges colorés pour les technologies
// ─────────────────────────────────────────────────────────────────
@Composable
fun TechBadgeRow(techs: List<Pair<String, Color>>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        techs.forEach { (name, color) ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(color.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text       = name,
                    color      = color,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}