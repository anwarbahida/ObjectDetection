package com.example.test.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.test.ui.navigation.Routers
import org.intellij.lang.annotations.JdkConstants

private val DarkSurface  = Color(0xFF1A1A2E)
private val AccentPurple = Color(0xFF7C4DFF)
private val AccentBlue   = Color(0xFF14C7EA)
private val AccentGreen  = Color(0xFF00E676)
private val TextPrimary  = Color(0xFFE0E0E0)

@Composable
fun DrawerMenu(
    navController: NavController,
    onClose: () -> Unit,
    onDetectionClick: () -> Unit = {      // ← valeur par défaut simple
        navController.navigate(Routers.DETECTION)
    }
) {
    ModalDrawerSheet(
        drawerContainerColor = DarkSurface,
        modifier = Modifier.width(250.dp)
    ) {

        // ── Header ──────────────────────────────────
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,

                ) {
                    AsyncImage(
                        model = "https://randomuser.me/api/portraits/men/1.jpg",
                        contentDescription = "Photo de profil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "B3G",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Api : jsonplaceholder.typicode.com",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Items ────────────────────────────────────
        DrawerItem(
            icon    = Icons.Default.Home,
            label   = "Accueil",
            color   = AccentPurple,
            onClick = {
                onClose()
                navController.navigate(Routers.HOME) {
                    popUpTo(Routers.HOME) { inclusive = true }
                }
            }
        )
        DrawerItem(
            icon    = Icons.Default.People,
            label   = "Utilisateurs",
            color   = AccentBlue,
            onClick = { onClose() }
        )
        DrawerItem(
            icon    = Icons.Default.ImageSearch,
            label   = "Détecter un Objet",
            color   = AccentBlue,
            onClick = {
                onClose()
                navController.navigate(Routers.DETECTION)
//                onDetectionClick()
            }
        )

        DrawerItem(
            icon    = Icons.Default.DocumentScanner,
            label   = "Détecter un Objet (Local)",
            color   = AccentBlue,
            onClick = {
                onClose()
                navController.navigate(Routers.DETECTIONS)
            }
        )

        DrawerItem(
            icon    = Icons.Default.Person,
            label   = "Profile",
            color   = AccentGreen,
            onClick = {
                onClose()
                navController.navigate(Routers.PROFILE)
            }
        )

        DrawerItem(
            icon    = Icons.Default.Article,
            label   = "Posts",
            color   = AccentGreen,
            onClick = {
                onClose()
                navController.navigate(Routers.POSTS)
            }
        )

        DrawerItem(
            icon    = Icons.Default.Info,
            label   = "À propos",
            color   = AccentGreen,
            onClick = {
                onClose()
                navController.navigate(Routers.REGISTER)
            }
        )

        Divider(
            color    = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        DrawerItem(
            icon    = Icons.Default.Logout,
            label   = "Déconnexion",
            color   = Color.Red,
            onClick = {
                onClose()
                navController.navigate(Routers.LOGIN) {
                    popUpTo(Routers.HOME) { inclusive = true }
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// DrawerItem
// ─────────────────────────────────────────────────────────────────
@Composable
fun DrawerItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color
            )
        },
        label = {
            Text(
                text = label,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent
        )
    )
}