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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

private val DarkBackground = Color(0xFF0F0F1A)
private val DarkSurface    = Color(0xFF1A1A2E)
private val DarkCard       = Color(0xFF16213E)
private val AccentPurple   = Color(0xFF7C4DFF)
private val AccentBlue     = Color(0xFF14C7EA)
private val AccentGreen    = Color(0xFF00E676)
private val AccentOrange   = Color(0xFFFF6D00)
private val TextPrimary    = Color(0xFFE0E0E0)
private val TextSecondary  = Color(0xFF9E9E9E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope       = rememberCoroutineScope()
    var visible     by remember { mutableStateOf(false) }

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
                            text = "Accueil",
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

                // ── Carte de bienvenue ───────────────────────
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { -40 })
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(20.dp),
                            colors   = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(AccentPurple, AccentBlue)
                                        )
                                    )
                                    .padding(24.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = "https://randomuser.me/api/portraits/men/1.jpg",
                                            contentDescription = "Photo de profil",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column {
                                        Text(
                                            text = "Bonjour 👋",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Bienvenue",
                                            color = Color.White,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Bonne journée !",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Titre Statistiques ───────────────────────
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(500))
                    ) {
                        Text(
                            text = "Statistiques",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // ── Grille de stats ──────────────────────────
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { 40 })
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    icon     = Icons.Default.Article,
                                    label    = "Posts",
                                    value    = "100",
                                    color    = AccentPurple
                                )
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    icon     = Icons.Default.People,
                                    label    = "Utilisateurs",
                                    value    = "10",
                                    color    = AccentBlue
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    icon     = Icons.Default.Comment,
                                    label    = "Commentaires",
                                    value    = "500",
                                    color    = AccentGreen
                                )
                                StatCard(
                                    modifier = Modifier.weight(1f),
                                    icon     = Icons.Default.ImageSearch,
                                    label    = "Détections",
                                    value    = "24",
                                    color    = AccentOrange
                                )
                            }
                        }
                    }
                }

                // ── Activité récente ─────────────────────────
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(700)) + slideInVertically(initialOffsetY = { 40 })
                    ) {
                        Text(
                            text = "Activité récente",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(800)) + slideInVertically(initialOffsetY = { 40 })
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(16.dp),
                            colors   = CardDefaults.cardColors(containerColor = DarkCard)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                ActivityRow(
                                    icon  = Icons.Default.Login,
                                    text  = "Connexion réussie",
                                    time  = "À l'instant",
                                    color = AccentGreen
                                )
                                Divider(
                                    color    = Color.White.copy(alpha = 0.06f),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                ActivityRow(
                                    icon  = Icons.Default.Article,
                                    text  = "100 posts disponibles",
                                    time  = "jsonplaceholder",
                                    color = AccentPurple
                                )
                                Divider(
                                    color    = Color.White.copy(alpha = 0.06f),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                ActivityRow(
                                    icon  = Icons.Default.People,
                                    text  = "10 utilisateurs chargés",
                                    time  = "jsonplaceholder",
                                    color = AccentBlue
                                )
                            }
                        }
                    }
                }

                // ── Espace bas ───────────────────────────────
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// StatCard
// ─────────────────────────────────────────────────────────────────
@Composable
fun StatCard(
    modifier : Modifier = Modifier,
    icon     : ImageVector,
    label    : String,
    value    : String,
    color    : Color
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = value,
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// ActivityRow
// ─────────────────────────────────────────────────────────────────
@Composable
fun ActivityRow(
    icon  : ImageVector,
    text  : String,
    time  : String,
    color : Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = time,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}