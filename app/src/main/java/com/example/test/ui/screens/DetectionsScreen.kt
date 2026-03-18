package com.example.test.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.test.ml.ObjectDetectorHelper
import com.example.test.ui.navigation.Routers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Couleurs (définies localement ou importées d'un fichier theme)
private val DarkBackground = Color(0xFF0F0F1A)
private val DarkSurface    = Color(0xFF1A1A2E)
private val AccentPurple   = Color(0xFF7C4DFF)
private val AccentBlue     = Color(0xFF14C7EA)
private val TextPrimary    = Color(0xFFE0E0E0)
private val TextSecondary  = Color(0xFF9E9E9E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionsScreen(navController: NavController) {
    val context = LocalContext.current
    val detector = remember { ObjectDetectorHelper(context) }

    var resultText by remember { mutableStateOf("") }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Transformation du texte en liste de paires (label, score)
    val detections = remember(resultText) {
        resultText.split("\n").mapNotNull { line ->
            val parts = line.split(" : ")
            if (parts.size == 2) {
                val label = parts[0]
                val score = parts[1].removeSuffix("%").toIntOrNull() ?: 0
                label to score
            } else null
        }
    }

    // Lanceur galerie
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isLoading = true
            val inputStream = context.contentResolver.openInputStream(uri)
            val image = BitmapFactory.decodeStream(inputStream)
            bitmap = image
            scope.launch {
                // Petit délai pour voir l'animation de chargement
                delay(500)
                val results = detector.detect(image)
                resultText = results.joinToString("\n") {
                    val label = it.categories[0].label
                    val score = it.categories[0].score
                    "$label : ${(score * 100).toInt()}%"
                }
                isLoading = false
            }
        }
    }

    // Lanceur caméra
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { imageBitmap: Bitmap? ->
        imageBitmap?.let {
            isLoading = true
            bitmap = it
            scope.launch {
                delay(500)
                val results = detector.detect(it)
                resultText = results.joinToString("\n") {
                    val label = it.categories[0].label
                    val score = it.categories[0].score

                    "$label : ${(score * 100).toInt()}%"
                }
                isLoading = false
            }
        }
    }

    // Effacement de l'image et des résultats
    fun clearAll() {
        bitmap = null
        resultText = ""
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerMenu(
                navController = navController,
                onClose = { scope.launch { drawerState.close() } },
                // Sur l'écran de détection, on évite de renaviguer vers la même page
                onDetectionClick = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            containerColor = DarkBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Détection d'objets",
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
                        if (bitmap != null) {
                            IconButton(onClick = { clearAll() }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Effacer",
                                    tint = AccentPurple
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkSurface,
                        titleContentColor = TextPrimary
                    )
                )
            },
            floatingActionButton = {
                Column(

                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp),

                ) {
                    FloatingActionButton(
                        onClick = { cameraLauncher.launch(null) },
                        containerColor = AccentPurple,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Caméra", tint = Color.White)
                    }
                    FloatingActionButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        containerColor = AccentBlue,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Photo, contentDescription = "Galerie", tint = Color.White)
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(DarkBackground)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Affichage de l'image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurface)
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap!!.asImageBitmap(),
                                contentDescription = "Image sélectionnée",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (isLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AccentPurple)
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Aucune image sélectionnée",
                                    color = TextSecondary,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Liste des détections
                    AnimatedVisibility(
                        visible = detections.isNotEmpty() && !isLoading,
                        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(),
                        exit = fadeOut()
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(detections) { detection ->
                                DetectionCard(label = detection.first, score = detection.second)
                            }
                        }
                    }

                    // Message si aucune détection
                    AnimatedVisibility(
                        visible = bitmap != null && !isLoading && detections.isEmpty()
                    ) {
                        Text(
                            text = "Aucun objet détecté",
                            color = TextSecondary,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetectionCard(label: String, score: Int) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Label,
                contentDescription = null,
                tint = AccentPurple,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Confiance",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { score/ 100f },
                    modifier = Modifier.size(48.dp),
                    color = AccentBlue,
                    trackColor = DarkBackground,
                    strokeWidth = 4.dp
                )
                Text(

                    text = "$score%",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}