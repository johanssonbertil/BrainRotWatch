package com.fishforandre.brainrotwatch

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.fishforandre.brainrotwatch.data.AuthRepository
import com.fishforandre.brainrotwatch.ui.theme.AppBackgroundGradient
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

val LogoTurquoise = Color(0xFF00E5FF)
val LogoPurple = Color(0xFF9C27B0)
val FinitoGreen = Color(0xFF00E676)
val DarkButtonContainer = Color(0xFF1E293B)

val LogoGradient = Brush.verticalGradient(
    colors = listOf(LogoTurquoise, LogoPurple)
)

@Composable
fun BrainRotLogoIcon(
    modifier: Modifier = Modifier.size(80.dp)
) {
    val surfaceColor = MaterialTheme.colorScheme.background

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val strokeWidth = 3.5.dp.toPx()

        // 1. Phone Body Outline
        val phoneWidth = width * 0.52f
        val phoneHeight = height * 0.92f
        val phoneLeft = (width - phoneWidth) / 2f
        val phoneTop = (height - phoneHeight) / 2f
        val phoneCornerRadius = 14.dp.toPx()

        drawRoundRect(
            brush = LogoGradient,
            topLeft = Offset(phoneLeft, phoneTop),
            size = Size(phoneWidth, phoneHeight),
            cornerRadius = CornerRadius(phoneCornerRadius, phoneCornerRadius),
            style = Stroke(width = strokeWidth)
        )

        // Phone Notch
        val notchWidth = phoneWidth * 0.35f
        val notchHeight = 4.dp.toPx()
        drawRoundRect(
            brush = LogoGradient,
            topLeft = Offset((width - notchWidth) / 2f, phoneTop + 5.dp.toPx()),
            size = Size(notchWidth, notchHeight),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )

        // Phone Home Indicator
        val homeBarWidth = phoneWidth * 0.30f
        val homeBarHeight = 3.dp.toPx()
        drawRoundRect(
            brush = LogoGradient,
            topLeft = Offset((width - homeBarWidth) / 2f, (phoneTop + phoneHeight) - 8.dp.toPx()),
            size = Size(homeBarWidth, homeBarHeight),
            cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
        )

        // 2. Stylized Eye Outer Shape
        val eyeCenter = Offset(width / 2f, height / 2f)
        val eyeWidth = width * 0.96f
        val eyeHeight = height * 0.50f
        val eyeLeft = (width - eyeWidth) / 2f

        val eyePath = Path().apply {
            moveTo(eyeLeft, eyeCenter.y)
            cubicTo(
                eyeLeft + eyeWidth * 0.25f, eyeCenter.y - eyeHeight * 0.75f,
                eyeLeft + eyeWidth * 0.75f, eyeCenter.y - eyeHeight * 0.75f,
                eyeLeft + eyeWidth, eyeCenter.y
            )
            cubicTo(
                eyeLeft + eyeWidth * 0.75f, eyeCenter.y + eyeHeight * 0.75f,
                eyeLeft + eyeWidth * 0.25f, eyeCenter.y + eyeHeight * 0.75f,
                eyeLeft, eyeCenter.y
            )
            close()
        }

        drawPath(
            path = eyePath,
            brush = LogoGradient
        )

        // 3. Eye Sclera / Inner Circle Cutout
        val irisRadius = eyeHeight * 0.42f
        drawCircle(
            color = surfaceColor,
            radius = irisRadius,
            center = eyeCenter
        )

        // 4. Inner Pupil Circle (Gradient)
        val pupilRadius = irisRadius * 0.72f
        drawCircle(
            brush = LogoGradient,
            radius = pupilRadius,
            center = eyeCenter
        )

        // 5. Catchlight Highlight Cutout (Top-Right of Pupil)
        val highlightCenter = Offset(
            eyeCenter.x + pupilRadius * 0.35f,
            eyeCenter.y - pupilRadius * 0.35f
        )
        drawCircle(
            color = surfaceColor,
            radius = pupilRadius * 0.28f,
            center = highlightCenter
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(
    authRepository: AuthRepository,
    onSignedIn: (UserProfile) -> Unit,
    onNavigateToDeviceSetup: () -> Unit = {},
    initialPage: Int = 0,
) {
    val pagerState = rememberPagerState(initialPage = initialPage) { 4 }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible

    // Automatically clear focus (and unhighlight text field) when the soft keyboard is hidden
    LaunchedEffect(isImeVisible) {
        if (!isImeVisible) {
            focusManager.clearFocus()
        }
    }

    val pageAnimationSpec = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)

    var nameText by rememberSaveable { mutableStateOf("") }
    var avatarUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var avatarBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = AppBackgroundGradient))
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                beyondBoundsPageCount = 3,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> OnboardingWelcomePage(
                        onNext = {
                            scope.launch {
                                pagerState.animateScrollToPage(1, animationSpec = pageAnimationSpec)
                            }
                        }
                    )

                    1 -> OnboardingIntroPage(
                        onNext = {
                            scope.launch {
                                pagerState.animateScrollToPage(2, animationSpec = pageAnimationSpec)
                            }
                        }
                    )

                    2 -> OnboardingUsageAccessPage(
                        onNext = {
                            scope.launch {
                                pagerState.animateScrollToPage(3, animationSpec = pageAnimationSpec)
                            }
                        }
                    )

                    3 -> ProfileSetupPage(
                        authRepository = authRepository,
                        nameText = nameText,
                        onNameTextChange = { nameText = it },
                        avatarBitmap = avatarBitmap,
                        onAvatarBitmapChange = { avatarBitmap = it },
                        avatarUri = avatarUri,
                        onAvatarUriChange = { avatarUri = it },
                        onSignedIn = onSignedIn,
                        showBackArrow = false,
                        onBack = {
                            scope.launch {
                                pagerState.animateScrollToPage(2, animationSpec = pageAnimationSpec)
                            }
                        }
                    )
                }
            }

            // Page Indicator Dots
            Row(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .width(if (isSelected) 24.dp else 8.dp)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) LogoTurquoise.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingWelcomePage(
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Image(
                painter = painterResource(id = R.drawable.final_logo_icon_only),
                contentDescription = "BrainRotWatch Logo Icon",
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.final_logo_text_only),
                contentDescription = "BrainRotWatch Text",
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(32.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Let Social Pressure Guide Your Scrolling Habits.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        Button(
            onClick = onNext,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkButtonContainer,
                contentColor = Color.White
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Let's get started",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = LogoTurquoise,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun OnboardingIntroPage(
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "With BrainRot Watch, you can see your friends' screen time and they can see yours.",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "This way, we are held responsible for our doom scroll.",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )
        }

        Button(
            onClick = onNext,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkButtonContainer,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Got it",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun OnboardingUsageAccessPage(
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasUsageAccess by remember { mutableStateOf(DeviceUsageRepository.hasUsageAccess(context)) }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsageAccess = DeviceUsageRepository.hasUsageAccess(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        if (hasUsageAccess) FinitoGreen.copy(alpha = 0.15f)
                        else LogoTurquoise.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasUsageAccess) Icons.Outlined.CheckCircle else Icons.Default.Analytics,
                    contentDescription = null,
                    tint = if (hasUsageAccess) FinitoGreen else LogoTurquoise,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (hasUsageAccess) "Usage Access Granted!" else "Usage Access Required",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (hasUsageAccess)
                    "You're all set! BrainRotWatch can now measure your daily screen time and sync with your friends."
                else
                    "To measure screen time, BrainRotWatch requires Usage Access permission on this device.\n\nAfter pressing the button below, select BrainRotWatch from the list and grant access.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        if (hasUsageAccess) {
            Button(
                onClick = onNext,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkButtonContainer,
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = LogoTurquoise,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                },
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkButtonContainer,
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = LogoTurquoise,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Allow users usage access",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSetupPage(
    authRepository: AuthRepository,
    nameText: String,
    onNameTextChange: (String) -> Unit,
    avatarBitmap: Bitmap?,
    onAvatarBitmapChange: (Bitmap?) -> Unit,
    avatarUri: Uri?,
    onAvatarUriChange: (Uri?) -> Unit,
    onSignedIn: (UserProfile) -> Unit,
    showBackArrow: Boolean = false,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var tempUncroppedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showCropDialog by remember { mutableStateOf(false) }

    var savedCropScale by remember { mutableFloatStateOf(1f) }
    var savedCropOffset by remember { mutableStateOf(Offset.Zero) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            val loadedBitmap = loadBitmapFromUri(context, cameraImageUri!!)
            if (loadedBitmap != null) {
                tempUncroppedBitmap = loadedBitmap
                savedCropScale = 1f
                savedCropOffset = Offset.Zero
                showCropDialog = true
            }
        }
    }

    val cameraPreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            tempUncroppedBitmap = bitmap
            savedCropScale = 1f
            savedCropOffset = Offset.Zero
            showCropDialog = true
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val loadedBitmap = loadBitmapFromUri(context, uri)
            if (loadedBitmap != null) {
                tempUncroppedBitmap = loadedBitmap
                savedCropScale = 1f
                savedCropOffset = Offset.Zero
                showCropDialog = true
            }
        }
    }

    if (showCropDialog && tempUncroppedBitmap != null) {
        FaceCropDialog(
            sourceBitmap = tempUncroppedBitmap!!,
            initialScale = savedCropScale,
            initialOffset = savedCropOffset,
            onCropped = { croppedBitmap, scale, offset ->
                onAvatarBitmapChange(croppedBitmap)
                onAvatarUriChange(saveBitmapToLocalFile(context, croppedBitmap))
                savedCropScale = scale
                savedCropOffset = offset
                showCropDialog = false
            },
            onDismiss = {
                showCropDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (showBackArrow) Arrangement.Start else Arrangement.Center
            ) {
                if (showBackArrow) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "Create Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .border(2.5.dp, LogoTurquoise.copy(alpha = 0.5f), CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                    .clickable(enabled = tempUncroppedBitmap != null || avatarBitmap != null) {
                        if (tempUncroppedBitmap == null && avatarBitmap != null) {
                            tempUncroppedBitmap = avatarBitmap
                        }
                        showCropDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                if (avatarBitmap != null) {
                    Image(
                        bitmap = avatarBitmap!!.asImageBitmap(),
                        contentDescription = "Face Photo - Tap to recrop",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Add face photo",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Face Photo",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        try {
                            val uri = createCameraImageUri(context)
                            cameraImageUri = uri
                            cameraLauncher.launch(uri)
                        } catch (_: Exception) {
                            try {
                                cameraPreviewLauncher.launch(null)
                            } catch (_: Exception) {
                                errorMessage = "Could not open camera."
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkButtonContainer,
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Take Photo",
                            tint = LogoTurquoise,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Take Photo",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoLibrary,
                            contentDescription = "Select Photo",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Select Photo",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = nameText,
                onValueChange = onNameTextChange,
                label = { Text("Enter your full name") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LogoTurquoise,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f),
                    focusedLabelColor = LogoTurquoise,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp)
        ) {
            Text(
                text = "Social accountability depends on authentic representation. For this reason, please use your real name and a clear photo of your face.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(12.dp))
            } else {
                Button(
                    onClick = {
                        if (nameText.isNotBlank()) {
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val profile = authRepository.registerDeviceAccount(
                                    displayName = nameText.trim(),
                                    avatarUrl = avatarUri?.toString()
                                )
                                isLoading = false
                                if (profile != null) {
                                    onSignedIn(profile)
                                } else {
                                    errorMessage = "Failed to create profile. Please try again."
                                }
                            }
                        }
                    },
                    enabled = nameText.isNotBlank(),
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkButtonContainer,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.30f)
                    )
                ) {
                    Text(
                        text = "Start BrainRotWatch",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun FaceCropDialog(
    sourceBitmap: Bitmap,
    initialScale: Float = 1f,
    initialOffset: Offset = Offset.Zero,
    onCropped: (Bitmap, Float, Offset) -> Unit,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    val viewportDp = 260.dp
    val viewportPx = with(density) { viewportDp.toPx() }

    val srcWidth = sourceBitmap.width.toFloat()
    val srcHeight = sourceBitmap.height.toFloat()

    val baseScale = Math.max(viewportPx / srcWidth, viewportPx / srcHeight)

    var userScale by remember { mutableFloatStateOf(initialScale) }
    var offset by remember { mutableStateOf(initialOffset) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(srcWidth, srcHeight) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (userScale * zoom).coerceIn(1f, 5f)
                        userScale = newScale

                        val currentTotalScale = baseScale * newScale
                        val scaledWidth = srcWidth * currentTotalScale
                        val scaledHeight = srcHeight * currentTotalScale

                        val maxPanX = Math.max(0f, (scaledWidth - viewportPx) / 2f)
                        val maxPanY = Math.max(0f, (scaledHeight - viewportPx) / 2f)

                        offset = Offset(
                            x = (offset.x + pan.x).coerceIn(-maxPanX, maxPanX),
                            y = (offset.y + pan.y).coerceIn(-maxPanY, maxPanY)
                        )
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val totalScale = baseScale * userScale
                val renderedW = srcWidth * totalScale
                val renderedH = srcHeight * totalScale

                val canvasCenter = Offset(size.width / 2f, size.height / 2f)
                val photoLeft = canvasCenter.x - (renderedW / 2f) + offset.x
                val photoTop = canvasCenter.y - (renderedH / 2f) + offset.y

                drawImage(
                    image = sourceBitmap.asImageBitmap(),
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(sourceBitmap.width, sourceBitmap.height),
                    dstOffset = IntOffset(photoLeft.toInt(), photoTop.toInt()),
                    dstSize = IntSize(renderedW.toInt(), renderedH.toInt()),
                    filterQuality = FilterQuality.High
                )

                val maskPath = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRect(Rect(0f, 0f, size.width, size.height))
                    addOval(Rect(center = canvasCenter, radius = viewportPx / 2f))
                }
                drawPath(maskPath, color = Color.Black.copy(alpha = 0.60f))

                drawCircle(
                    color = LogoTurquoise,
                    radius = viewportPx / 2f,
                    center = canvasCenter,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.95f),
                                Color.Black.copy(alpha = 0.80f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(top = 40.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Center Your Face",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pinch to zoom and drag anywhere to position your face inside the circle",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.80f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .padding(top = 32.dp, bottom = 36.dp, start = 24.dp, end = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.18f),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val cropped = cropBitmapToCircle(sourceBitmap, userScale, offset, viewportPx)
                            onCropped(cropped, userScale, offset)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkButtonContainer,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Done!", fontWeight = FontWeight.Bold, color = LogoTurquoise)
                    }
                }
            }
        }
    }
}

private fun cropBitmapToCircle(
    source: Bitmap,
    userScale: Float,
    offset: Offset,
    viewportPx: Float
): Bitmap {
    val w = source.width.toFloat()
    val h = source.height.toFloat()

    val baseScale = Math.max(viewportPx / w, viewportPx / h)
    val totalScale = baseScale * userScale

    val srcCropWidth = viewportPx / totalScale
    val srcCropHeight = viewportPx / totalScale

    val srcCenterX = (w / 2f) - (offset.x / totalScale)
    val srcCenterY = (h / 2f) - (offset.y / totalScale)

    val maxLeft = Math.max(0f, w - srcCropWidth)
    val maxTop = Math.max(0f, h - srcCropHeight)

    val left = (srcCenterX - (srcCropWidth / 2f)).coerceIn(0f, maxLeft)
    val top = (srcCenterY - (srcCropHeight / 2f)).coerceIn(0f, maxTop)

    val srcRect = android.graphics.Rect(
        left.toInt(),
        top.toInt(),
        (left + srcCropWidth).toInt().coerceAtMost(source.width),
        (top + srcCropHeight).toInt().coerceAtMost(source.height)
    )

    val outputSize = 512
    val output = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(output)
    val destRect = android.graphics.Rect(0, 0, outputSize, outputSize)

    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }

    canvas.drawBitmap(source, srcRect, destRect, paint)
    return output
}

private fun createCameraImageUri(context: Context): Uri {
    val file = File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

@Composable
fun DeviceSetupScreen(
    authRepository: AuthRepository,
    onBack: () -> Unit,
    onSignedIn: (UserProfile) -> Unit,
) {
    var nameText by rememberSaveable { mutableStateOf("") }
    var avatarUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var avatarBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = AppBackgroundGradient))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(vertical = 12.dp)
    ) {
        ProfileSetupPage(
            authRepository = authRepository,
            nameText = nameText,
            onNameTextChange = { nameText = it },
            avatarBitmap = avatarBitmap,
            onAvatarBitmapChange = { avatarBitmap = it },
            avatarUri = avatarUri,
            onAvatarUriChange = { avatarUri = it },
            onSignedIn = onSignedIn,
            showBackArrow = true,
            onBack = onBack
        )
    }
}

fun saveBitmapToLocalFile(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val file = File(context.filesDir, "device_avatar_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        Uri.fromFile(file)
    } catch (_: Exception) {
        null
    }
}

fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    } catch (_: Exception) {
        null
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    AuthScreen(
        authRepository = com.fishforandre.brainrotwatch.data.SupabaseAuthRepository(),
        onSignedIn = {}
    )
}
