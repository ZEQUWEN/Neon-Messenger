package com.example.ui
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun SettingsListItem(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SettingsSimpleItem(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMenuScreen(viewModel: AppViewModel, navController: NavController) {
    val activeAccount = LocalActiveAccount.current ?: return
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Filled.MoreVert, "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Profile section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("settings/profile") }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val imageUrl = activeAccount.profilePicUrl.ifEmpty { "https://picsum.photos/seed/${activeAccount.id}/100" }
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(64.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(activeAccount.displayName, style = MaterialTheme.typography.titleLarge)
                    Text(if(activeAccount.username.startsWith("@")) activeAccount.username else "@${activeAccount.username}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(activeAccount.bio.take(30) + if(activeAccount.bio.length > 30) "..." else "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            HorizontalDivider()
            
            // Settings Categories
            Text("Settings", modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            
            SettingsListItem(
                icon = { Icon(Icons.Filled.Person, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) },
                title = "Profile",
                subtitle = "Name, bio, avatar",
                onClick = { navController.navigate("settings/profile") }
            )
            
            SettingsListItem(
                icon = { Icon(Icons.Filled.Settings, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) },
                title = "General Settings",
                subtitle = "Language, notifications",
                onClick = { navController.navigate("settings/general") }
            )
            
            SettingsListItem(
                icon = { Icon(Icons.Filled.Palette, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) },
                title = "Colors",
                subtitle = "Custom colors",
                onClick = { navController.navigate("settings/colors") }
            )

            SettingsListItem(
                icon = { Icon(Icons.Filled.ColorLens, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) },
                title = "Themes",
                subtitle = "Appearance, dark mode",
                onClick = { navController.navigate("settings/themes") }
            )
            
            SettingsListItem(
                icon = { Icon(Icons.Filled.Security, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) },
                title = "Privacy and Security",
                subtitle = "Passcode, 2-step verification",
                onClick = { navController.navigate("settings/security") }
            )
            
            SettingsListItem(
                icon = { Icon(Icons.Filled.Devices, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) },
                title = "Devices",
                subtitle = "Active sessions",
                onClick = { navController.navigate("settings/devices") }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Logout
            SettingsListItem(
                icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = MaterialTheme.colorScheme.error) },
                title = "Log Out",
                subtitle = null,
                onClick = { 
                    viewModel.logout()
                    navController.popBackStack()
                }
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

// Added from replacement_profile.txt
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsProfileScreen(viewModel: AppViewModel, navController: NavController) {
    val activeAccount = com.example.ui.LocalActiveAccount.current ?: return
    
    var username by remember { mutableStateOf(activeAccount.username) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var displayName by remember { mutableStateOf(activeAccount.displayName) }
    var bio by remember { mutableStateOf(activeAccount.bio) }
    var customStatus by remember { mutableStateOf(activeAccount.customStatus) }
    var profilePicUrl by remember { mutableStateOf(activeAccount.profilePicUrl) }
    
    val avatars = remember(profilePicUrl) {
        listOf(
            profilePicUrl.ifEmpty { "https://picsum.photos/seed/${activeAccount.id}/400" },
            "https://picsum.photos/seed/${activeAccount.id}_1/400",
            "https://picsum.photos/seed/${activeAccount.id}_2/400"
        )
    }
    val pagerState = rememberPagerState(pageCount = { avatars.size })
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("Profile Management") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
        
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.size(160.dp)) {
                val context = androidx.compose.ui.platform.LocalContext.current
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
                
                Box(modifier = Modifier.fillMaxSize().clip(CircleShape).border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize().clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage((pagerState.currentPage + 1) % avatars.size)
                            }
                        }
                    ) { page ->
                        coil.compose.AsyncImage(
                            model = coil.request.ImageRequest.Builder(context)
                                .data(avatars[page])
                                .crossfade(true)
                                .build(),
                            imageLoader = imageLoader,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                    
                    // Pager Indicators
                    Row(
                        Modifier
                            .height(20.dp)
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(avatars.size) { iteration ->
                            val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .height(4.dp)
                                    .width(if (pagerState.currentPage == iteration) 16.dp else 8.dp)
                            )
                        }
                    }
                }
                
                val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
                ) { uri ->
                    if (uri != null) {
                        profilePicUrl = uri.toString()
                    }
                }
                
                IconButton(
                    onClick = { 
                        launcher.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).size(42.dp)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Change Photo", tint = Color.White, modifier = Modifier.padding(8.dp))
                }
            }
            
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    if (it.length < 5 && it.isNotEmpty()) {
                        usernameError = "Username must be at least 5 characters"
                    } else if (!it.matches(Regex("^[a-zA-Z0-9_]+$")) && it.isNotEmpty()) {
                        usernameError = "Invalid characters"
                    } else if (it == "admin" || it == "system") {
                        usernameError = "Username is already taken"
                    } else {
                        usernameError = null
                    }
                },
                label = { Text("@username") },
                modifier = Modifier.fillMaxWidth(),
                isError = usernameError != null,
                supportingText = { usernameError?.let { Text(it) } }
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = customStatus,
                onValueChange = { customStatus = it },
                label = { Text("Custom Status") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    if (usernameError == null) {
                        viewModel.updateProfile(activeAccount.id, username, displayName, bio, profilePicUrl, customStatus)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}

// Added from replacement.txt
// calculatePasswordStrength removed because it is in Utils.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoStepVerificationScreen(navController: NavController) {
    var password by remember { mutableStateOf("") }
    val strength = remember(password) { calculatePasswordStrength(password) }
    
    val strengthColor = when (strength) {
        0 -> Color.Gray
        1, 2 -> Color(0xFFFF1744) // Neon Red
        3 -> Color(0xFFFFEA00) // Neon Yellow
        4, 5 -> Color(0xFF00E676) // Neon Green
        else -> Color.Gray
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = strength / 5f,
        animationSpec = tween(300)
    )
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)).imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = { Text("Cloud Password") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text("🙈", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Create a password", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Enter password") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Password Strength",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = when (strength) {
                        0 -> ""
                        1, 2 -> "Weak"
                        3 -> "Fair"
                        4, 5 -> "Strong"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = strengthColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color.DarkGray, CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = animatedProgress)
                        .height(6.dp)
                        .background(strengthColor, CircleShape)
                        .border(1.dp, strengthColor, CircleShape)
                        .shadow(
                            elevation = if (strength > 0) 8.dp else 0.dp,
                            shape = CircleShape,
                            ambientColor = strengthColor,
                            spotColor = strengthColor
                        )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSecurityScreen(viewModel: AppViewModel, navController: NavController) {
    var deleteAccountDialogVisible by remember { mutableStateOf(false) }
    var immediateDeleteDialogVisible by remember { mutableStateOf(false) }
    val activeAccount = LocalActiveAccount.current ?: return
    var deleteAccountValue by remember { mutableStateOf("6 months") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy and Security") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            Text("Security", modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            
            SettingsListItem(icon = { Icon(Icons.Filled.Lock, null) }, title = "Passcode Lock", subtitle = "Off", onClick = { navController.navigate("settings/passcode") })
            SettingsListItem(icon = { Icon(Icons.Filled.VpnKey, null) }, title = "Two-Step Verification", subtitle = "Off", onClick = { navController.navigate("settings/two_step") })
            SettingsListItem(icon = { Icon(Icons.Filled.Email, null) }, title = "Login Email", subtitle = "None", onClick = { navController.navigate("settings/email") })
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text("Privacy", modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            
            SettingsListItem(icon = { Icon(Icons.Filled.Block, null) }, title = "Blocked Users", subtitle = "0 users", onClick = { navController.navigate("settings/blocked_users") })
            SettingsSimpleItem(title = "Phone Number", value = "Nobody", onClick = { navController.navigate("settings/privacy/Phone Number") })
            SettingsSimpleItem(title = "Last Seen & Online", value = "Everybody", onClick = { navController.navigate("settings/privacy/Last Seen") })
            SettingsSimpleItem(title = "Profile Photos", value = "Everybody", onClick = { navController.navigate("settings/privacy/Profile Photos") })
            SettingsSimpleItem(title = "Calls", value = "Everybody", onClick = { navController.navigate("settings/privacy/Calls") })
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text("Delete My Account", modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            SettingsSimpleItem(title = "If away for", value = deleteAccountValue, onClick = { deleteAccountDialogVisible = true })
            
            Text(
                text = "Delete Account Now",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { immediateDeleteDialogVisible = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            )
        }
        
        if (immediateDeleteDialogVisible) {
            AlertDialog(
                onDismissRequest = { immediateDeleteDialogVisible = false },
                title = { Text("Delete Account") },
                text = { Text("Are you sure you want to delete your account? This action will wipe all local data and initiate the server-side deletion process. This cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteAccount(activeAccount.id) {
                                immediateDeleteDialogVisible = false
                            }
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { immediateDeleteDialogVisible = false }) { Text("Cancel") }
                }
            )
        }
        
        if (deleteAccountDialogVisible) {
            AlertDialog(
                onDismissRequest = { deleteAccountDialogVisible = false },
                title = { Text("Self-Destruct if inactive for...") },
                text = {
                    Column {
                        listOf("1 month", "3 months", "6 months", "1 year").forEach { option ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { 
                                    deleteAccountValue = option
                                    deleteAccountDialogVisible = false
                                }.padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = deleteAccountValue == option, onClick = null)
                                Spacer(Modifier.width(8.dp))
                                Text(option)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { deleteAccountDialogVisible = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsGeneralScreen(viewModel: AppViewModel, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("General Settings") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            Text("Settings", modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            
            var notificationsEnabled by remember { mutableStateOf(true) }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Notifications", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
            }
            
            SettingsSimpleItem("Language", "English", onClick = {})
            SettingsSimpleItem("Chat Background", "Default", onClick = {})
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsColorsScreen(viewModel: AppViewModel, navController: NavController) {
    val colors = listOf(
        Pair(Color(0xFFBB86FC), "Purple"),
        Pair(Color(0xFF6200EE), "Deep Purple"),
        Pair(Color(0xFF03DAC5), "Teal"),
        Pair(Color(0xFF00C853), "Green"),
        Pair(Color(0xFFFFD600), "Yellow"),
        Pair(Color(0xFFFF3D00), "Orange"),
        Pair(Color(0xFFD50000), "Red"),
        Pair(Color(0xFF2962FF), "Blue")
    )
    
    val currentPrimary by viewModel.customPrimaryColor.collectAsState()
    val isAutoThemeEnabled by viewModel.isAutoThemeEnabled.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Colors") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    TextButton(onClick = { viewModel.resetTheme() }) {
                        Text("Reset")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Auto Theme Switcher", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = isAutoThemeEnabled,
                    onCheckedChange = { viewModel.setAutoThemeEnabled(it) }
                )
            }
            
            HorizontalDivider()
            Text("Primary Color", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.labelLarge)
            
            LazyVerticalGrid(
                columns = GridCells.Adaptive(60.dp),
                modifier = Modifier.padding(16.dp).height(200.dp)
            ) {
                items(colors) { colorPair ->
                    val colorValue = colorPair.first.value.toLong()
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(colorPair.first)
                            .border(
                                3.dp,
                                if (currentPrimary == colorValue) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                CircleShape
                            )
                            .clickable { viewModel.setCustomPrimaryColor(colorValue) }
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsThemesScreen(viewModel: AppViewModel, navController: androidx.navigation.NavController) {
    val currentTheme by viewModel.theme.collectAsState()
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = AppTheme.values().indexOf(currentTheme).let { if (it >= 0) it else 0 },
        pageCount = { AppTheme.values().size }
    )
    
    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { androidx.compose.material3.Text("Themes", color = androidx.compose.ui.graphics.Color.White) },
                navigationIcon = { androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = androidx.compose.ui.graphics.Color.White) } },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        },
        containerColor = androidx.compose.ui.graphics.Color.Black
    ) { padding ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val theme = AppTheme.values()[page]
                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                    // Preview Canvas
                    when (theme) {
                        AppTheme.NEON_SNOWFLAKES -> NeonSnowflakesBackground(isBatterySaver = false, opacity = 1f)
                        AppTheme.NEON_CHERRY_BLOSSOM -> NeonCherryBlossomBackground(isBatterySaver = false, opacity = 1f)
                        AppTheme.NEON_CONFETTI -> NeonConfettiBackground(isBatterySaver = false, opacity = 1f)
                        AppTheme.NEON_MOON -> NeonMoonBackground(opacity = 1f)
                        AppTheme.NEON_ROOM_FOG -> NeonRoomFogBackground(opacity = 1f)
                        AppTheme.DEFAULT -> ElegantDarkBackground(opacity = 1f)
                    }
                    
                    // Theme Name Label
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.Text(
                            text = theme.name.replace("_", " "),
                            style = androidx.compose.material3.MaterialTheme.typography.displayMedium,
                            color = androidx.compose.ui.graphics.Color.White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
                        if (currentTheme == theme) {
                            androidx.compose.material3.Text("Current Theme", color = androidx.compose.material3.MaterialTheme.colorScheme.primary, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                        } else {
                            androidx.compose.material3.Button(onClick = { viewModel.switchTheme(theme) }) {
                                androidx.compose.material3.Text("Apply Theme")
                            }
                        }
                    }
                }
            }
            
            // Pager Indicators
            androidx.compose.foundation.layout.Row(
                Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .align(androidx.compose.ui.Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                repeat(AppTheme.values().size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f)
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(color)
                            .height(8.dp)
                            .width(if (pagerState.currentPage == iteration) 24.dp else 8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasscodeLockScreen(navController: NavController) {
    var passcode by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Passcode Lock") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(32.dp))
            Icon(Icons.Filled.Lock, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("Enter a passcode", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(
                value = passcode,
                onValueChange = { passcode = it },
                label = { Text("Passcode") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginEmailScreen(viewModel: AppViewModel, navController: NavController) {
    var email by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login Email") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEmailScreen(viewModel: AppViewModel, navController: NavController) {
    var code by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Email") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Verification Code") },
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Devices") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            SettingsListItem(icon = { Icon(Icons.Filled.PhoneAndroid, null) }, title = "Current Session", subtitle = "Android", onClick = {})
            Button(onClick = {}, modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text("Terminate All Other Sessions")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedUsersScreen(viewModel: AppViewModel, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blocked Users") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No blocked users")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingScreen(navController: NavController, settingName: String) {
    var selectedOption by remember { mutableStateOf("Everybody") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(settingName) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text("Who can see my $settingName?", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            
            listOf("Everybody", "My Contacts", "Nobody").forEach { option ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { selectedOption = option }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(option)
                    RadioButton(selected = selectedOption == option, onClick = null)
                }
            }
        }
    }
}
