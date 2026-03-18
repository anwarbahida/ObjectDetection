package com.example.test.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.test.data.model.User
import com.example.test.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

private val DarkBackground  = Color(0xFF0F0F1A)
private val DarkSurface     = Color(0xFF1A1A2E)
private val DarkCard        = Color(0xFF16213E)
private val AccentPurple    = Color(0xFF7C4DFF)
private val AccentBlue      = Color(0xFF14C7EA)
private val AccentGreen     = Color(0xFF00E676)
private val AccentOrange    = Color(0xFFFFAE3F)
private val TextPrimary     = Color(0xFFE0E0E0)
private val TextSecondary   = Color(0xFF9E9E9E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val viewModel: HomeViewModel = viewModel()
    val users     = viewModel.users
    val isLoading = viewModel.isLoading
    val error     = viewModel.errorMessage

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    var visible         by remember { mutableStateOf(false) }
    var showDetection   by remember { mutableStateOf(false) }  // ← ici dans HomeScreen

    LaunchedEffect(Unit) { visible = true }

    // ── DetectionScreen par-dessus tout ─────────────
    if (showDetection) {
        DetectionScreen(
            navController=navController,
            onClose = { showDetection = false }
        )
        return   // ← on n'affiche pas le reste
    }

    // ── Ecran principal ──────────────────────────────
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerMenu(                          // ← nouveau fichier séparé
                navController      = navController,
                onClose            = { scope.launch { drawerState.close() } },
                onDetectionClick   = { showDetection = true }
            )
        }
    ) {
        Scaffold(
            containerColor = DarkBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Utilisateurs",
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
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(AccentPurple, AccentBlue)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkSurface
                    )
                )
            }
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(DarkBackground)
            ) {
                when {
                    isLoading -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = AccentPurple)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Chargement...", color = TextSecondary)
                        }
                    }
                    error != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error,
                                color = Color.Red,
                                fontSize = 14.sp
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            itemsIndexed(users) { index, user ->
                                AnimatedVisibility(
                                    visible = visible,
                                    enter = fadeIn(tween(300 + index * 80)) +
                                            slideInVertically(
                                                initialOffsetY = { 60 },
                                                animationSpec = tween(300 + index * 80)
                                            )
                                ) {
                                    UserCard(user = user, index = index)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// UserCard
// ─────────────────────────────────────────────────────────────────
@Composable
fun UserCard(user: User, index: Int) {
    val accentColors = listOf(AccentPurple, AccentBlue, AccentGreen, AccentOrange)
    val accent = accentColors[index % accentColors.size]

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(accent, accent.copy(alpha = 0.6f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.first().toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(user.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("@${user.username}", color = accent, fontSize = 13.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accent.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("#${user.id}", color = accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color.White.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(12.dp))
            UserDetailRow(Icons.Default.Email,     user.email,              accent)
            Spacer(modifier = Modifier.height(6.dp))
            UserDetailRow(Icons.Default.Phone,     user.phone,              accent)
            Spacer(modifier = Modifier.height(6.dp))
            UserDetailRow(Icons.Default.Language,  user.website,            accent)
            Spacer(modifier = Modifier.height(6.dp))
            UserDetailRow(Icons.Default.Business,  user.company.name,       accent)
            Spacer(modifier = Modifier.height(6.dp))
            UserDetailRow(Icons.Default.LocationOn,"${user.address.city}, ${user.address.street}", accent)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// UserDetailRow
// ─────────────────────────────────────────────────────────────────
@Composable
fun UserDetailRow(icon: ImageVector, value: String, accent: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = value, color = TextSecondary, fontSize = 13.sp, maxLines = 1)
    }
}