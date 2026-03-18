package com.example.test.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.test.ui.viewmodel.DetectionViewModel
import kotlinx.coroutines.launch

private val DarkBackground = Color(0xFF0F0F1A)
private val DarkSurface    = Color(0xFF1A1A2E)
private val AccentPurple   = Color(0xFF7C4DFF)
private val AccentBlue     = Color(0xFF14C7EA)
private val TextPrimary    = Color(0xFFE0E0E0)
private val TextSecondary  = Color(0xFF9E9E9E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionScreen(
    navController: NavController,         // ← ajouté
    onClose: () -> Unit = {},
    viewModel: DetectionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedBitmap  by remember { mutableStateOf<Bitmap?>(null) }
    var showCamera      by remember { mutableStateOf(false) }
    var showDetection   by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(context.contentResolver, it)
                )
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            }
            selectedBitmap = bitmap
            viewModel.detectObjects(bitmap)
        }
    }

    // Caméra plein écran
    if (showCamera) {
        CameraCapture(
            onImageCaptured = { bitmap ->
                selectedBitmap = bitmap
                viewModel.detectObjects(bitmap)
                showCamera = false
            },
            onDismiss = { showCamera = false }
        )
        return
    }

    // Detection depuis un autre DrawerItem
    if (showDetection) {
        DetectionScreen(
            navController = navController,
            onClose = { showDetection = false }
        )
        return
    }

    // ── Ecran principal avec Drawer ──────────────

        Scaffold(
            containerColor = DarkBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Détection d'objets",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        // ← Bouton burger pour ouvrir le Drawer
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                Icons.Default.Menu,
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
                                "AB",
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
                    .padding(16.dp)
                    .background(DarkBackground)
            ) {
                // Boutons source
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showCamera = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentPurple
                        )
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Caméra")
                    }
                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ImageSearch, contentDescription = null,
                            tint = AccentBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Galerie", color = AccentBlue)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Aperçu image
                selectedBitmap?.let { bmp ->
                    androidx.compose.foundation.Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Image analysée",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // États
                when (val state = uiState) {
                    is DetectionViewModel.UiState.Idle -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Sélectionne une image\npour détecter des objets",
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    is DetectionViewModel.UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = AccentPurple)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Analyse en cours avec Gemini...",
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    is DetectionViewModel.UiState.Success -> {
                        Text(
                            "${state.objects.size} objet(s) détecté(s)",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.objects) { obj ->
                                DetectedObjectCard(obj)
                            }
                        }
                    }

                    is DetectionViewModel.UiState.Error -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Erreur : ${state.message}",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }

@Composable
fun DetectedObjectCard(obj: DetectionViewModel.DetectedObject) {
    val confidenceColor = when (obj.confidence.lowercase()) {
        "haute"   -> Color(0xFF2E7D32)
        "moyenne" -> Color(0xFFF57F17)
        else      -> Color(0xFFC62828)
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(obj.name, style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFE0E0E0))
                if (obj.description.isNotEmpty()) {
                    Text(obj.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9E9E9E))
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = confidenceColor.copy(alpha = 0.12f)
            ) {
                Text(
                    obj.confidence,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = confidenceColor
                )
            }
        }
    }
}