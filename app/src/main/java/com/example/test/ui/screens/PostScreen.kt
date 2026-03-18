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
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.ErrorOutline
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.test.data.model.Post
import com.example.test.ui.viewmodel.PostViewModel
import kotlinx.coroutines.launch

private val DarkBackground  = Color(0xFF0F0F1A)
private val DarkSurface     = Color(0xFF1A1A2E)
private val DarkCard        = Color(0xFF16213E)
private val AccentPurple    = Color(0xFF7C4DFF)
private val AccentBlue      = Color(0xFF14C7EA)
private val TextPrimary     = Color(0xFFE0E0E0)
private val TextSecondary   = Color(0xFF9E9E9E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(navController: NavController) {

    val viewModel: PostViewModel = viewModel()
    val posts     = viewModel.posts
    val isLoading = viewModel.isLoading
    val error     = viewModel.errorMessage

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { }
    ) {

        Scaffold(
            containerColor = DarkBackground,

            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Posts",
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
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = AccentPurple
                        )
                    }

                    error != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color.Red
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = error,
                                color = Color.Red
                            )
                        }
                    }

                    else -> {

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            itemsIndexed(posts) { index, post ->

                                AnimatedVisibility(
                                    visible = visible,
                                    enter = fadeIn(tween(300)) +
                                            slideInVertically(initialOffsetY = { 40 })
                                ) {

                                    PostCard(post)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(post: Post) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.Article,
                    contentDescription = null,
                    tint = AccentPurple
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Post #${post.id}",
                    color = AccentPurple,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = post.title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.body,
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}