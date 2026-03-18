package com.example.test.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.test.ui.navigation.Routers
import com.example.test.ui.viewmodel.HomeViewModel
import com.example.test.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch

private val DarkBackground = Color(0xFF0F0F1A)
private val DarkSurface    = Color(0xFF1A1A2E)
private val AccentPurple   = Color(0xFF7C4DFF)
private val AccentBlue     = Color(0xFF14C7EA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {

   val drawerState = rememberDrawerState(DrawerValue.Closed)
   val scope       = rememberCoroutineScope()

   val userViewModel: UserViewModel = viewModel()
   val user by userViewModel.user.collectAsStateWithLifecycle()

   val homeViewModel: HomeViewModel = viewModel()
   val users = homeViewModel.users

   var visible by remember { mutableStateOf(false) }
   LaunchedEffect(Unit) { visible = true }

   ModalNavigationDrawer(
      drawerState = drawerState,
      drawerContent = {
         DrawerMenu(
            navController    = navController,
            onClose          = { scope.launch { drawerState.close() } },
            onDetectionClick = { navController.navigate(Routers.DETECTION) }
         )
      }
   ) {
      Scaffold(
         topBar = {
            TopAppBar(
               title = {
                  Text(
                     text = "Profile",
                     color = Color.White,
                     fontWeight = FontWeight.Bold,
                     fontSize = 20.sp
                  )
               },
               navigationIcon = {
                  IconButton(
                     onClick = { scope.launch { drawerState.open() } }
                  ) {
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
                        text = user.name.firstOrNull()?.toString() ?: "?",
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

         Column(
            modifier = Modifier
               .fillMaxSize()
               .padding(paddingValues)
               .background(DarkBackground)
         ) {

            // ── Card 1 — Profil connecté ─────────
            Card(
               colors = CardDefaults.cardColors(
                  containerColor = Color(0xFF16213E)
               ),
               modifier = Modifier
                  .fillMaxWidth()
                  .weight(1f),
               shape = RoundedCornerShape(0.dp)
            ) {
               Row(
                  modifier = Modifier
                     .padding(16.dp)
                     .fillMaxSize(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
               ) {
                  Column(
                     horizontalAlignment = Alignment.CenterHorizontally,
                     verticalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                     // Avatar avec initiale
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
                        Text(
                           text = user.name.firstOrNull()?.toString() ?: "?",
                           color = Color.White,
                           fontSize = 32.sp,
                           fontWeight = FontWeight.Bold
                        )
                     }

                     Text(
                        text = user.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                     )
                     Text(
                        text = "@${user.username}",
                        color = AccentBlue,
                        fontSize = 13.sp
                     )
                     Text(
                        text = user.email,
                        color = Color(0xFF9E9E9E),
                        fontSize = 12.sp
                     )
                     Text(
                        text = user.phone,
                        color = Color(0xFF9E9E9E),
                        fontSize = 12.sp
                     )
                     Text(
                        text = "${user.city} · ${user.company}",
                        color = Color(0xFF9E9E9E),
                        fontSize = 12.sp
                     )
                  }
               }
            }

            /*/ ── Card 2 — Liste Users ─────────────
            Card(
               modifier = Modifier
                  .fillMaxWidth()
                  .weight(2f),
               shape = RoundedCornerShape(0.dp),
               colors = CardDefaults.cardColors(
                  containerColor = Color(0xFF1A1A2E),
                  contentColor   = Color.White
               )
            ) {
               LazyColumn(
                  modifier = Modifier
                     .fillMaxSize()
                     .padding(horizontal = 16.dp),
                  verticalArrangement = Arrangement.spacedBy(12.dp),
                  contentPadding = PaddingValues(vertical = 16.dp)
               ) {
                  itemsIndexed(users) { index, userItem ->  // ← renommé userItem
                     AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(300 + index * 80)) +
                                slideInVertically(
                                   initialOffsetY = { 60 },
                                   animationSpec = tween(300 + index * 80)
                                )
                     ) {
                        UserCard(user = userItem, index = index)  // ← userItem
                     }
                  }
               }
            }*/
         }
      }
   }
}