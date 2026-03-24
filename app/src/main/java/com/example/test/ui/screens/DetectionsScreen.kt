package com.example.test.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.test.ml.ObjectDetectorHelper
import com.example.test.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.Executors


private val DarkBackground = Color(0xFF0F0F1A)
private val DarkSurface    = Color(0xFF1A1A2E)
private val AccentPurple   = Color(0xFF7C4DFF)
private val AccentBlue     = Color(0xFF14C7EA)
private val AccentGreen    = Color(0xFF00E676)
private val TextPrimary    = Color(0xFFE0E0E0)
private val TextSecondary  = Color(0xFF9E9E9E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionsScreen(navController: NavController) {

    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val detector       = remember { ObjectDetectorHelper(context) }
    val scope          = rememberCoroutineScope()
    val drawerState    = rememberDrawerState(DrawerValue.Closed)

    var detections  by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var isSpeaking  by remember { mutableStateOf(false) }

    // ✅ Garde-fous anti-spam vocal
    var lastSpokenLabel by remember { mutableStateOf("") }
    var lastSpokenTime  by remember { mutableStateOf(0L) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // ── TextToSpeech ─────────────────────────────────
    val tts = remember {
        var instance: TextToSpeech? = null
        instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                instance?.language = Locale.FRENCH
                instance?.setSpeechRate(0.85f)  // ✅ vitesse naturelle
                instance?.setPitch(1.0f)
            }
        }
        instance
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // ── Executor caméra ──────────────────────────────
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    fun speakIfNew(label: String, score: Int) {
        val now = System.currentTimeMillis()

        // Protection 1 — TTS encore en train de parler
        if (tts?.isSpeaking == true) return

        // Protection 2 — même objet dans les 5 dernières secondes
        if (label == lastSpokenLabel && now - lastSpokenTime < 5000) return

        lastSpokenLabel = label
        lastSpokenTime  = now
        isSpeaking      = true

        val text: String

        if (label == "person") {
            text = "Attention : une personne a été détectée devant vous."

            // Lecture du son d'alerte
            val mediaPlayer = MediaPlayer.create(context, R.raw.alert)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { mp -> mp.release() }

        } else {
            text = "J'ai détecté : $label"
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "live_detection")

        scope.launch {
            delay(4000) // Protection 3 — cooldown 4s avant de reparler
            isSpeaking = false
        }
    }

    // ── Permission launcher ──────────────────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    // ── UI ───────────────────────────────────────────
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerMenu(
                navController    = navController,
                onClose          = { scope.launch { drawerState.close() } },
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
                            text       = "Détection en direct",
                            color      = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector    = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint           = AccentPurple
                            )
                        }
                    },
                    actions = {
                        // ✅ Indicateur vocal animé
                        AnimatedVisibility(
                            visible = isSpeaking,
                            enter   = fadeIn() + scaleIn(),
                            exit    = fadeOut() + scaleOut()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(AccentBlue.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector    = Icons.Default.VolumeUp,
                                    contentDescription = "Parle",
                                    tint           = AccentBlue,
                                    modifier       = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text      = "Parle...",
                                    color     = AccentBlue,
                                    fontSize  = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Aperçu caméra ────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurface)
                ) {
                    if (hasCameraPermission) {

                        // ✅ CameraX Preview + ImageAnalysis
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx)
                                val cameraProviderFuture =
                                    ProcessCameraProvider.getInstance(ctx)

                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()

                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }

                                    val imageAnalyzer = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(
                                            ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                                        )
                                        .build()
                                        .also { analysis ->
                                            analysis.setAnalyzer(cameraExecutor) { imageProxy ->

                                                // ✅ Ignorer si TTS parle encore
                                                if (tts?.isSpeaking == true) {
                                                    imageProxy.close()
                                                    return@setAnalyzer
                                                }

                                                // ✅ Max 1 analyse par seconde
                                                val now = System.currentTimeMillis()
                                                if (now - lastSpokenTime < 1000) {
                                                    imageProxy.close()
                                                    return@setAnalyzer
                                                }

                                                val bitmap = imageProxy.toBitmap()

                                                try {
                                                    val results = detector.detect(bitmap)

                                                    if (results.isNotEmpty()) {
                                                        val topLabel = results[0].categories[0].label
                                                        val topScore = (results[0].categories[0].score * 100).toInt()

                                                        detections = results.map {
                                                            it.categories[0].label to
                                                                    (it.categories[0].score * 100).toInt()
                                                        }

                                                        speakIfNew(topLabel, topScore)

                                                    } else {
                                                        detections = emptyList()
                                                    }

                                                } catch (e: Exception) {
                                                    Log.e("Detection", "Erreur : ${e.message}")
                                                } finally {
                                                    imageProxy.close()
                                                }
                                            }
                                        }

                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageAnalyzer
                                        )
                                    } catch (e: Exception) {
                                        Log.e("CameraX", "Erreur bind : ${e.message}")
                                    }

                                }, ContextCompat.getMainExecutor(ctx))

                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // ✅ Badge EN DIRECT
                        Box(
                            modifier = Modifier
                                .padding(12.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Red.copy(alpha = 0.85f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .align(Alignment.TopStart)
                        ) {
                            Text(
                                text       = "● EN DIRECT",
                                color      = Color.White,
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // ✅ Badge nombre d'objets détectés
                        androidx.compose.animation.AnimatedVisibility(
                            visible = detections.isNotEmpty(),
                            enter   = fadeIn() + slideInVertically { it },
                            exit    = fadeOut() + slideOutVertically { it },
                            modifier = Modifier.align(Alignment.BottomStart)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(AccentPurple.copy(alpha = 0.9f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text       = "✔ ${detections.size} objet(s) détecté(s)",
                                    color      = Color.White,
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // ✅ Indicateur vocal sur la vidéo
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isSpeaking,
                            enter   = fadeIn() + slideInVertically { -it },
                            exit    = fadeOut() + slideOutVertically { -it },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(AccentBlue.copy(alpha = 0.9f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector    = Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint           = Color.White,
                                        modifier       = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text      = "Parle...",
                                        color     = Color.White,
                                        fontSize  = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                    } else {

                        // ✅ Écran demande de permission
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector    = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint           = TextSecondary,
                                modifier       = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text     = "Permission caméra requise",
                                color    = TextSecondary,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentPurple
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector    = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint           = Color.White,
                                    modifier       = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Autoriser la caméra", color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Liste détections ─────────────────────────
                AnimatedVisibility(
                    visible = detections.isNotEmpty(),
                    enter   = fadeIn(tween(300)) + slideInVertically(),
                    exit    = fadeOut(tween(300))
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(detections) { detection ->
                            DetectionCard(
                                label = detection.first,
                                score = detection.second
                            )
                        }
                    }
                }

                // ── Message si rien détecté ──────────────────
                AnimatedVisibility(
                    visible = detections.isEmpty() && hasCameraPermission,
                    enter   = fadeIn(tween(300)),
                    exit    = fadeOut(tween(300))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector    = Icons.Default.Search,
                            contentDescription = null,
                            tint           = TextSecondary,
                            modifier       = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text     = "Pointez la caméra vers un objet...",
                            color    = TextSecondary,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// DetectionCard
// ─────────────────────────────────────────────────────────────────
@Composable
fun DetectionCard(label: String, score: Int) {

    val progressColor = when {
        score >= 80 -> AccentGreen
        score >= 50 -> AccentBlue
        else        -> AccentPurple
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = DarkSurface),
        shape    = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ Icône colorée selon le score
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(progressColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector    = Icons.Default.Label,
                    contentDescription = null,
                    tint           = progressColor,
                    modifier       = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = label,
                    color      = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
                Text(
                    text     = "Confiance",
                    color    = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // ✅ Cercle de progression coloré selon le score
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress    = { score / 100f },
                    modifier    = Modifier.size(52.dp),
                    color       = progressColor,
                    trackColor  = DarkBackground,
                    strokeWidth = 4.dp
                )
                Text(
                    text       = "$score%",
                    color      = TextPrimary,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}