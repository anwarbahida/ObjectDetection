package com.example.test.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.test.R
import com.example.test.data.network.RetrofitInstance
import com.example.test.ui.navigation.Routers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Context
import android.provider.Settings.Global.*
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.test.ui.viewmodel.UserViewModel


private val DarkBackground = Color(0xFF0F0F1A)
private val DarkSurface    = Color(0xFF1A1A2E)
private val DarkCard       = Color(0xFF16213E)
private val AccentPurple   = Color(0xFF7C4DFF)
private val AccentBlue     = Color(0xFF00B4D8)
private val TextPrimary    = Color(0xFFE0E0E0)
private val TextSecondary  = Color(0xFF9E9E9E)

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userViewModel: UserViewModel
) {
    var email          by remember { mutableStateOf("") }
    var password       by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var buttonClicked  by remember { mutableStateOf(false) }
    var formVisible    by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    LaunchedEffect(Unit) { formVisible = true }

    Scaffold(
        containerColor = DarkBackground ,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color(0xFFFFFFFF),
                        contentColor = Color.Red,
                        shape = RoundedCornerShape(13.dp),
                    )
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Carousel ─────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxSize()
//                        .height(260.dp)
                ) {
                    ImagePagerCarouselAutoScroll(
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay gradient bas du carousel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        DarkBackground.copy(alpha = 0.3f)
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ── Titre ─────────────────────────────────────
                AnimatedVisibility(
                    visible = formVisible,
                    enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { -20 })
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        // Logo / Avatar animé
                        Box(
                            modifier = Modifier
                                .size(84.dp),
//                                .clip(CircleShape),

                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = R.drawable.b3g,
                                contentDescription = "Photo de profil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Connectez-vous pour continuer",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Card Formulaire ───────────────────────────
                AnimatedVisibility(
                    visible = formVisible,
                    enter = slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn(tween(500))
                ) {
                    val floatingAnimation = rememberInfiniteTransition()
                    val floatingOffset by floatingAnimation.animateFloat(
                        initialValue = 0f,
                        targetValue = (-4).dp.value,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offset(y = floatingOffset.dp),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {

                            // ── Champ Email ──────────────────
                            AnimatedVisibility(
                                visible = formVisible,
                                enter = fadeIn(tween(300, delayMillis = 100)) +
                                        slideInHorizontally(initialOffsetX = { -40 })
                            ) {
                                Column {
                                    Text(
                                        text = "Email ou Username",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        placeholder = { Text("ex: Sincere@april.biz", color = TextSecondary.copy(alpha = 0.5f)) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Email,
                                                null,
                                                tint = AccentPurple
                                            )
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentPurple,
                                            unfocusedBorderColor = DarkSurface,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            cursorColor = AccentPurple,
                                            focusedContainerColor = DarkSurface,
                                            unfocusedContainerColor = DarkSurface
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // ── Champ Password ───────────────
                            AnimatedVisibility(
                                visible = formVisible,
                                enter = fadeIn(tween(300, delayMillis = 200)) +
                                        slideInHorizontally(initialOffsetX = { -40 })
                            ) {
                                Column {
                                    Text(
                                        text = "Mot de passe",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = { password = it },
                                        placeholder = { Text("*********", color = TextSecondary.copy(alpha = 0.5f)) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Lock,
                                                null,
                                                tint = AccentPurple
                                            )
                                        },
                                        trailingIcon = {
                                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                Icon(
                                                    imageVector = if (passwordVisible)
                                                        Icons.Default.Visibility
                                                    else Icons.Default.VisibilityOff,
                                                    contentDescription = null,
                                                    tint = if (passwordVisible) AccentPurple else TextSecondary
                                                )
                                            }
                                        },
                                        visualTransformation = if (passwordVisible)
                                            VisualTransformation.None
                                        else PasswordVisualTransformation(),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentPurple,
                                            unfocusedBorderColor = DarkSurface,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            cursorColor = AccentPurple,
                                            focusedContainerColor = DarkSurface,
                                            unfocusedContainerColor = DarkSurface
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            // ── Bouton Connexion ─────────────
                            AnimatedVisibility(
                                visible = formVisible,
                                enter = fadeIn(tween(300, delayMillis = 300)) +
                                        scaleIn(initialScale = 0.8f)
                            ) {
                                Button(
                                    onClick = {
                                        buttonClicked = !buttonClicked
                                        scope.launch {
                                            try {
                                                val users = RetrofitInstance.api.getUsers()
                                                val matchedUser = users.firstOrNull { user ->
                                                    (user.email == email || user.username == email)
                                                            && user.name == password
                                                }

                                                if (matchedUser != null) {
                                                    // ✅ Sauvegarder AVANT de naviguer
                                                    val prefs = context.getSharedPreferences(
                                                        "user_prefs",
                                                        Context.MODE_PRIVATE
                                                    )
                                                    prefs.edit().apply {
                                                        putInt("user_id", matchedUser.id)
                                                        putString("name", matchedUser.name)
                                                        putString("email", matchedUser.email)
                                                        putString("username", matchedUser.username)
                                                        putString("phone", matchedUser.phone)
                                                        putString("website", matchedUser.website)
                                                        putString(
                                                            "company",
                                                            matchedUser.company.name
                                                        ) // adapter selon votre modèle
                                                        putString("city", matchedUser.address.city)
                                                    }.apply()

                                                    // ✅ Naviguer UNE SEULE FOIS, après sauvegarde
                                                    navController.navigate(Routers.HOME) {
                                                        popUpTo(Routers.LOGIN) {
                                                        inclusive = true
                                                        } // évite le retour arrière
                                                    }
                                                } else {
                                                    snackbarHostState.showSnackbar("Identifiants incorrects")
                                                }

                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                snackbarHostState.showSnackbar("Erreur : Vérifiez votre connexion Internet")
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .scale(if (buttonClicked) 0.97f else 1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent
                                    ),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(AccentPurple, AccentBlue)
                                                ),
                                                shape = RoundedCornerShape(14.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Login,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Se connecter",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // ── Divider ──────────────────────
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Divider(
                                    modifier = Modifier.weight(1f),
                                    color = TextSecondary.copy(alpha = 0.3f)
                                )
                                Text(
                                    "  ou  ",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Divider(
                                    modifier = Modifier.weight(1f),
                                    color = TextSecondary.copy(alpha = 0.3f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // ── Bouton Register ──────────────
                            AnimatedVisibility(
                                visible = formVisible,
                                enter = fadeIn(tween(300, delayMillis = 400)) +
                                        scaleIn(initialScale = 0.8f)
                            ) {
                                OutlinedButton(
                                    onClick = { navController.navigate(Routers.REGISTER) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(AccentPurple, AccentBlue)
                                        )
                                    )
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.PersonAdd,
                                            contentDescription = null,
                                            tint = AccentPurple,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Créer un compte",
                                            color = AccentPurple,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// Carousel (inchangé)
// ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImagePagerCarouselAutoScroll(modifier: Modifier = Modifier) {
    val images = listOf(
        R.drawable.img1,
        R.drawable.img2,
        R.drawable.img3,
        R.drawable.img4
    )

    val pagerState = rememberPagerState(pageCount = { images.size })
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    LaunchedEffect(pagerState, isDragged) {
        if (!isDragged) {
            while (true) {
                delay(2000)
                val nextPage = (pagerState.currentPage + 1) % images.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Image(
                painter = painterResource(id = images[page]),
                contentDescription = "Image $page",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)              // ✅ hauteur fixe identique pour toutes
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
                contentScale = ContentScale.Crop // ✅ recadre sans déformer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        PageIndicator(
            pageCount = images.size,
            currentPage = pagerState.currentPage
        )
    }
}

@Composable
fun PageIndicator(pageCount: Int, currentPage: Int, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) 34.dp else 18.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) AccentPurple else Color.White
            )
            Box(
                modifier = Modifier
                    .padding(horizontal =6.dp)
                    .height(10.dp)
                    .width(width)          // ✅ largeur animée au lieu de size
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}
