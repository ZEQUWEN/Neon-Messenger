package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MyProfileScreen(viewModel: AppViewModel, navController: NavController) {
    val activeAccount = LocalActiveAccount.current ?: return
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val headerHeightDp = 380.dp
    val headerHeightPx = with(density) { headerHeightDp.toPx() }
    
    var overscrollOffset by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (overscrollOffset > 0f && available.y < 0) {
                    val consumed = available.y.coerceAtLeast(-overscrollOffset)
                    overscrollOffset += consumed
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y > 0) {
                    overscrollOffset += available.y
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (overscrollOffset > 0f) {
                    overscrollOffset = 0f
                }
                return Velocity.Zero
            }
        }
    }

    val animatedOverscroll by animateFloatAsState(targetValue = overscrollOffset, label = "overscroll")
    
    var showEditDateDialog by remember { mutableStateOf(false) }
    var showChangeNumberDialog by remember { mutableStateOf(false) }
    var showAvatarViewer by remember { mutableStateOf(false) }
    val avatars = remember(activeAccount.id) {
        listOf(
            activeAccount.profilePicUrl.takeIf { it.isNotEmpty() } ?: "https://picsum.photos/seed/${activeAccount.id}/800",
            "https://picsum.photos/seed/${activeAccount.id}_1/800",
            "https://picsum.photos/seed/${activeAccount.id}_2/800"
        )
    }
    
    var selectedTab by remember { mutableStateOf(0) }

    if (showAvatarViewer) {
        AvatarViewerDialog(
            avatars = avatars,
            initialPage = 0,
            onDismiss = { showAvatarViewer = false }
        )
    }

    val imageLoader = remember {
        coil.ImageLoader.Builder(context)
            .components {
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(coil.decode.ImageDecoderDecoder.Factory())
                } else {
                    add(coil.decode.GifDecoder.Factory())
                }
            }
            .build()
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .nestedScroll(nestedScrollConnection)
    ) {
        // --- Header Image and Title ---
        val scrollOffset = listState.firstVisibleItemScrollOffset.toFloat()
        val firstItemIndex = listState.firstVisibleItemIndex
        
        val actualScroll = if (firstItemIndex == 0) scrollOffset else headerHeightPx
        val collapseFraction = (actualScroll / headerHeightPx).coerceIn(0f, 1f)
        
        val scale = 1f + (animatedOverscroll / 1000f)
        val translationY = if (animatedOverscroll > 0f) animatedOverscroll / 2f else -actualScroll * 0.5f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeightDp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.translationY = translationY
                }
                .clickable { showAvatarViewer = true }
        ) {
            val imageUrl = avatars.first()
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = "Avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Gradient overlay at bottom of image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.9f)),
                            startY = headerHeightPx * 0.5f
                        )
                    )
            )
            
            // Name and Status
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .graphicsLayer {
                        alpha = 1f - collapseFraction
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = activeAccount.displayName,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (activeAccount.customStatus.isNotEmpty()) {
                    Text(
                        text = activeAccount.customStatus,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "в сети",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )
            }
        }

        // --- Main Content ---
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.height(headerHeightDp - 20.dp))
            }
            
            // Action Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
                    ) { uri ->
                        if (uri != null) {
                            viewModel.updateProfile(activeAccount.id, activeAccount.username, activeAccount.displayName, activeAccount.bio, uri.toString(), activeAccount.customStatus)
                        }
                    }

                    ProfileActionButton(
                        icon = Icons.Filled.PhotoCamera,
                        text = "Выбрать фото",
                        onClick = { launcher.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    )
                    ProfileActionButton(
                        icon = Icons.Filled.Edit,
                        text = "Изменить",
                        onClick = { navController.navigate("settings/general") }
                    )
                    ProfileActionButton(
                        icon = Icons.Filled.Settings,
                        text = "Настройки",
                        onClick = { navController.navigate("settings") }
                    )
                }
            }

            // Info Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        InfoItem(
                            title = "+7 (922) 669-26-82", // Example hardcoded or from account
                            subtitle = "Телефон",
                            onClick = { clipboardManager.setText(AnnotatedString("+79226692682")) },
                            menuItems = { closeMenu ->
                                DropdownMenuItem(text = { Text("Копировать") }, onClick = { clipboardManager.setText(AnnotatedString("+79226692682")); closeMenu() })
                                DropdownMenuItem(text = { Text("Изменить номер") }, onClick = { showChangeNumberDialog = true; closeMenu() })
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        InfoItem(
                            title = activeAccount.bio.takeIf { it.isNotBlank() } ?: "✨Занимаюсь дизайном карточек товаров и вайбкодингом, это моё хобби✨",
                            subtitle = "О себе",
                            onClick = { },
                            menuItems = { closeMenu ->
                                DropdownMenuItem(text = { Text("Копировать") }, onClick = { clipboardManager.setText(AnnotatedString(activeAccount.bio)); closeMenu() })
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        InfoItem(
                            title = if (activeAccount.username.startsWith("@")) activeAccount.username else "@${activeAccount.username}",
                            subtitle = "Имя пользователя",
                            onClick = { },
                            menuItems = { closeMenu ->
                                DropdownMenuItem(text = { Text("Копировать") }, onClick = { clipboardManager.setText(AnnotatedString(activeAccount.username)); closeMenu() })
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        InfoItem(
                            title = "21 июн. 2005 (21 год)", // Example
                            subtitle = "День рождения",
                            onClick = { },
                            menuItems = { closeMenu ->
                                DropdownMenuItem(text = { Text("Копировать") }, onClick = { clipboardManager.setText(AnnotatedString("21 июн. 2005")); closeMenu() })
                                DropdownMenuItem(text = { Text("Изменить дату") }, onClick = { showEditDateDialog = true; closeMenu() })
                                DropdownMenuItem(text = { Text("Удалить", color = Color.Red) }, onClick = { closeMenu() })
                            }
                        )
                    }
                }
            }

            // Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Публикации") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Архив публикаций") }
                    )
                }
            }
            
            // Publications Empty State
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, bottom = 120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Публикаций пока нет...",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Публикуйте фотографии и видео в\nсвоём профиле",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { /* Add publication */ },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Добавить")
                    }
                }
            }
        }

        // --- Top App Bar ---
        val showTopBar = collapseFraction > 0.8f
        AnimatedVisibility(
            visible = showTopBar,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val imageUrl = activeAccount.profilePicUrl.takeIf { it.isNotEmpty() } ?: "https://picsum.photos/seed/${activeAccount.id}/100"
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(imageUrl).build(),
                            imageLoader = imageLoader,
                            contentDescription = "Mini Avatar",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(activeAccount.displayName, style = MaterialTheme.typography.titleMedium)
                            Text("в сети", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* more */ }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
        
        // Back button always visible if top bar is hidden
        if (!showTopBar) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(top = 8.dp, start = 8.dp)
                    .statusBarsPadding()
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }

        // Dialogs
        if (showChangeNumberDialog) {
            AlertDialog(
                onDismissRequest = { showChangeNumberDialog = false },
                title = { Text("Сменить номер") },
                text = { Text("Здесь Вы можете сменить номер телефона. Ваш аккаунт и все данные будут перенесены на новый номер.") },
                confirmButton = {
                    Button(onClick = { showChangeNumberDialog = false }) { Text("Сменить номер") }
                },
                dismissButton = {
                    TextButton(onClick = { showChangeNumberDialog = false }) { Text("Отмена") }
                }
            )
        }
        if (showEditDateDialog) {
            AlertDialog(
                onDismissRequest = { showEditDateDialog = false },
                title = { Text("День рождения") },
                text = { Text("Укажите свой день рождения.") },
                confirmButton = {
                    Button(onClick = { showEditDateDialog = false }) { Text("Сохранить") }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDateDialog = false }) { Text("Отмена") }
                }
            )
        }
    }
}

@Composable
fun ProfileActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .width(80.dp)
    ) {
        Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(4.dp))
        Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfoItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    menuItems: @Composable (closeMenu: () -> Unit) -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            menuItems { showMenu = false }
        }
    }
}
