package com.example.test.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

// ── Couleurs UI ───────────────────────────────────────────────────
private val DarkBackground = Color(0xFF0F0F1A)
private val DarkSurface    = Color(0xFF1A1A2E)
private val DarkCard       = Color(0xFF16213E)
private val AccentPurple   = Color(0xFF7C4DFF)
private val TextPrimary    = Color(0xFFE0E0E0)
private val TextSecondary  = Color(0xFF9E9E9E)

// ── Couleurs de dessin disponibles ───────────────────────────────
private val DrawColors = listOf(
    Color(0xFFFF4444),  // Rouge
    Color(0xFF00E676),  // Vert
    Color(0xFF2979FF),  // Bleu
    Color(0xFFFFD600),  // Jaune
    Color(0xFFFF6D00),  // Orange
    Color(0xFFE040FB),  // Violet
    Color(0xFF00BCD4),  // Cyan
    Color(0xFFFFFFFF),  // Blanc
)

// ── Modèle d'un trait dessiné ─────────────────────────────────────
data class DrawPath(
    val points: List<Offset>,
    val color : Color,
    val width : Float = 8f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FingerDrawScreen(navController: NavController) {

    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope          = rememberCoroutineScope()
    val drawerState    = rememberDrawerState(DrawerValue.Closed)

    // ── États dessin ─────────────────────────────────
    var selectedColor   by remember { mutableStateOf(DrawColors[0]) }
    var brushSize       by remember { mutableStateOf(8f) }
    var paths           by remember { mutableStateOf<List<DrawPath>>(emptyList()) }
    var currentPoints   by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var isDrawing       by remember { mutableStateOf(false) }
    var isFistDetected  by remember { mutableStateOf(false) }
    var fingerPosition  by remember { mutableStateOf<Offset?>(null) }
    var handDetected    by remember { mutableStateOf(false) }

    // ── Dimensions caméra (pour normaliser les coords) ─
    var canvasWidth  by remember { mutableStateOf(1f) }
    var canvasHeight by remember { mutableStateOf(1f) }

    // ── Permission caméra ────────────────────────────
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hasCameraPermission = it }

    // ── Executor caméra ──────────────────────────────
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) { onDispose { cameraExecutor.shutdown() } }

    // ── MediaPipe HandLandmarker ──────────────────────
    val handLandmarker = remember {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("hand_landmarker.task")
                .build()
            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setNumHands(1)
                .setRunningMode(RunningMode.IMAGE)
                .build()
            HandLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            Log.e("HandLandmarker", "Erreur init : ${e.message}")
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose { handLandmarker?.close() }
    }

    // ── Détection poing fermé ────────────────────────
    // Un poing fermé = tous les doigts repliés
    // On compare la position Y du bout vs la base de chaque doigt
    fun isFistClosed(result: HandLandmarkerResult): Boolean {
        if (result.landmarks().isEmpty()) return false
        val landmarks = result.landmarks()[0]

        // Indices : bout des doigts = 8,12,16,20 | bases = 5,9,13,17
        val fingerTips  = listOf(8, 12, 16, 20)
        val fingerBases = listOf(5,  9, 13, 17)

        var closedCount = 0
        for (i in fingerTips.indices) {
            val tip  = landmarks[fingerTips[i]]
            val base = landmarks[fingerBases[i]]
            // Si le bout du doigt est EN DESSOUS de la base → doigt replié
            if (tip.y() > base.y()) closedCount++
        }
        return closedCount >= 3 // 3 doigts repliés = poing
    }

    // ── Récupérer position index ─────────────────────
    // Landmark 8 = bout de l'index
    fun getIndexFingerTip(
        result: HandLandmarkerResult,
        width: Float,
        height: Float
    ): Offset? {
        if (result.landmarks().isEmpty()) return null
        val tip = result.landmarks()[0][8]
        // MediaPipe retourne des coords normalisées [0,1]
        // On miroir X car la caméra frontale est inversée
        return Offset(
            x = (1f - tip.x()) * width,
            y = tip.y() * height
        )
    }

    // ── UI ────────────────────────────────────────────
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
                            text       = "Dessin par doigt",
                            color      = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = AccentPurple)
                        }
                    },
                    actions = {
                        // ✅ Bouton effacer tout
                        IconButton(onClick = {
                            paths       = emptyList()
                            currentPoints = emptyList()
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Effacer",
                                tint = Color.Red
                            )
                        }
                        // ✅ Bouton annuler dernier trait
                        IconButton(onClick = {
                            if (paths.isNotEmpty()) {
                                paths = paths.dropLast(1)
                            }
                        }) {
                            Icon(
                                Icons.Default.Undo,
                                contentDescription = "Annuler",
                                tint = TextSecondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
                )
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(DarkBackground)
            ) {

                // ── Zone caméra + Canvas dessin ──────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (hasCameraPermission) {

                        // ✅ Caméra FRONTALE pour le dessin
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
                                        .setOutputImageFormat(
                                            ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
                                        )
                                        .build()
                                        .also { analysis ->
                                            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                                try {
                                                    // Convertir en Bitmap
                                                    val bitmap = Bitmap.createBitmap(
                                                        imageProxy.width,
                                                        imageProxy.height,
                                                        Bitmap.Config.ARGB_8888
                                                    )
                                                    imageProxy.use {
                                                        val plane = it.planes[0]
                                                        bitmap.copyPixelsFromBuffer(plane.buffer)
                                                    }

                                                    // Rotation selon orientation
                                                    val matrix = Matrix().apply {
                                                        postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                                                    }
                                                    val rotatedBitmap = Bitmap.createBitmap(
                                                        bitmap, 0, 0,
                                                        bitmap.width, bitmap.height,
                                                        matrix, true
                                                    )

                                                    // ✅ Analyser avec MediaPipe
                                                    val mpImage = BitmapImageBuilder(rotatedBitmap).build()
                                                    val result  = handLandmarker?.detect(mpImage)

                                                    if (result != null && result.landmarks().isNotEmpty()) {
                                                        handDetected = true

                                                        // Vérifier poing fermé
                                                        val fist = isFistClosed(result)
                                                        isFistDetected = fist

                                                        if (fist) {
                                                            // ✅ Poing = effacer tout
                                                            paths         = emptyList()
                                                            currentPoints = emptyList()
                                                            isDrawing     = false
                                                            fingerPosition = null
                                                        } else {
                                                            // ✅ Index levé = dessiner
                                                            val pos = getIndexFingerTip(
                                                                result,
                                                                canvasWidth,
                                                                canvasHeight
                                                            )
                                                            if (pos != null) {
                                                                fingerPosition = pos
                                                                currentPoints  = currentPoints + pos
                                                                isDrawing      = true
                                                            }
                                                        }
                                                    } else {
                                                        // Aucune main — sauvegarder le trait en cours
                                                        handDetected = false
                                                        if (currentPoints.size > 1) {
                                                            paths = paths + DrawPath(
                                                                points = currentPoints,
                                                                color  = selectedColor,
                                                                width  = brushSize
                                                            )
                                                        }
                                                        currentPoints  = emptyList()
                                                        isDrawing      = false
                                                        fingerPosition = null
                                                    }

                                                } catch (e: Exception) {
                                                    Log.e("FingerDraw", "Erreur : ${e.message}")
                                                } finally {
                                                    imageProxy.close()
                                                }
                                            }
                                        }

                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            // ✅ Caméra FRONTALE
                                            CameraSelector.DEFAULT_FRONT_CAMERA,
                                            preview,
                                            imageAnalyzer
                                        )
                                    } catch (e: Exception) {
                                        Log.e("CameraX", "Erreur : ${e.message}")
                                    }

                                }, ContextCompat.getMainExecutor(ctx))

                                previewView
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .onGloballyPositioned { coords ->
                                    canvasWidth  = coords.size.width.toFloat()
                                    canvasHeight = coords.size.height.toFloat()
                                }
                        )

                        // ✅ Canvas transparent par-dessus la caméra
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            canvasWidth  = size.width
                            canvasHeight = size.height

                            // Dessiner tous les traits sauvegardés
                            paths.forEach { drawPath ->
                                if (drawPath.points.size > 1) {
                                    val path = Path().apply {
                                        moveTo(drawPath.points[0].x, drawPath.points[0].y)
                                        for (i in 1 until drawPath.points.size) {
                                            val prev = drawPath.points[i - 1]
                                            val curr = drawPath.points[i]
                                            // Courbe lisse entre les points
                                            quadraticBezierTo(
                                                prev.x, prev.y,
                                                (prev.x + curr.x) / 2f,
                                                (prev.y + curr.y) / 2f
                                            )
                                        }
                                    }
                                    drawPath(
                                        path  = path,
                                        color = drawPath.color,
                                        style = Stroke(
                                            width     = drawPath.width,
                                            cap       = StrokeCap.Round,
                                            join      = StrokeJoin.Round
                                        )
                                    )
                                }
                            }

                            // ✅ Dessiner le trait en cours
                            if (currentPoints.size > 1) {
                                val path = Path().apply {
                                    moveTo(currentPoints[0].x, currentPoints[0].y)
                                    for (i in 1 until currentPoints.size) {
                                        val prev = currentPoints[i - 1]
                                        val curr = currentPoints[i]
                                        quadraticBezierTo(
                                            prev.x, prev.y,
                                            (prev.x + curr.x) / 2f,
                                            (prev.y + curr.y) / 2f
                                        )
                                    }
                                }
                                drawPath(
                                    path  = path,
                                    color = selectedColor,
                                    style = Stroke(
                                        width = brushSize,
                                        cap   = StrokeCap.Round,
                                        join  = StrokeJoin.Round
                                    )
                                )
                            }

                            // ✅ Point curseur sur le bout du doigt
                            fingerPosition?.let { pos ->
                                drawCircle(
                                    color  = selectedColor,
                                    radius = brushSize / 2f + 4f,
                                    center = pos
                                )
                                drawCircle(
                                    color  = Color.White,
                                    radius = brushSize / 2f + 6f,
                                    center = pos,
                                    style  = Stroke(width = 2f)
                                )
                            }
                        }

                        // ✅ Badge état main
                        Box(
                            modifier = Modifier
                                .padding(12.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    when {
                                        isFistDetected -> Color.Red.copy(alpha = 0.85f)
                                        isDrawing      -> selectedColor.copy(alpha = 0.85f)
                                        handDetected   -> Color.Gray.copy(alpha = 0.7f)
                                        else           -> Color.Black.copy(alpha = 0.5f)
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .align(Alignment.TopStart)
                        ) {
                            Text(
                                text = when {
                                    isFistDetected -> "✊ Effacement..."
                                    isDrawing      -> "✏ Dessin en cours"
                                    handDetected   -> "✋ Main détectée"
                                    else           -> "👁 Montrez votre main"
                                },
                                color      = Color.White,
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                    } else {
                        // ✅ Écran permission
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint     = TextSecondary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Permission caméra requise", color = TextSecondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors  = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                            ) {
                                Text("Autoriser", color = Color.White)
                            }
                        }
                    }
                }

                // ── Barre outils couleurs + taille ───────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ✅ Palette de couleurs
                    Row(
                        modifier             = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment    = Alignment.CenterVertically
                    ) {
                        DrawColors.forEach { color ->
                            val isSelected = color == selectedColor
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 38.dp else 30.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (isSelected) Modifier.border(
                                            width = 3.dp,
                                            color = Color.White,
                                            shape = CircleShape
                                        ) else Modifier
                                    )
                                    .clickable { selectedColor = color }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ✅ Slider taille du pinceau
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint     = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Slider(
                            value         = brushSize,
                            onValueChange = { brushSize = it },
                            valueRange    = 4f..24f,
                            modifier      = Modifier.weight(1f).padding(horizontal = 8.dp),
                            colors        = SliderDefaults.colors(
                                thumbColor       = selectedColor,
                                activeTrackColor = selectedColor
                            )
                        )
                        Box(
                            modifier = Modifier
                                .size(brushSize.dp.coerceIn(8.dp, 24.dp))
                                .clip(CircleShape)
                                .background(selectedColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // ✅ Légende gestes
                    Row(
                        modifier             = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text     = "☝ Index = Dessiner",
                            color    = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text     = "✊ Poing = Effacer tout",
                            color    = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}