package com.example.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.Date
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(viewModel: AppViewModel, chatId: String, navController: NavController) {
    val chats by viewModel.chats.collectAsState()
    val chat = chats.find { it.id == chatId } ?: return
    
    val messages by viewModel.getMessages(chatId).collectAsState(initial = emptyList())
    val groupMembers by if (chat.isGroup) viewModel.getGroupMembers(chatId).collectAsState(initial = emptyList()) else remember { mutableStateOf(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    val activeAccount = LocalActiveAccount.current
    
    var showDisappearingDialog by remember { mutableStateOf(false) }
    var expiresIn by remember { mutableStateOf<Long?>(null) }
    
    var showReactionDialogFor by remember { mutableStateOf<String?>(null) }
    
    var isRecording by remember { mutableStateOf(false) }
    
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val typingChats by viewModel.typingChats.collectAsState()
    val isTyping = typingChats.contains(chatId)
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = remember(context) {
        var currentContext = context
        while (currentContext is android.content.ContextWrapper) {
            if (currentContext is android.app.Activity) {
                break
            }
            currentContext = currentContext.baseContext
        }
        currentContext as? android.app.Activity
    }
    
    DisposableEffect(chat.isSecret) {
        if (chat.isSecret) {
            activity?.window?.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        onDispose {
            if (chat.isSecret) {
                activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
    
    LaunchedEffect(chatId) {
        viewModel.markMessagesAsRead(chatId, activeAccount?.id ?: "")
        val draft = viewModel.getDraft(chatId)
        if (draft != null) {
            inputText = draft.text
        }
    }

    DisposableEffect(chatId) {
        onDispose {
            viewModel.saveDraft(chatId, inputText)
        }
    }
    
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showSignDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!chat.isGroup && !chat.isChannel) {
                                    navController.navigate("profile/${chat.id}")
                                }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(chat.title)
                        val statusText = when {
                            isTyping -> "typing..."
                            connectionStatus == ConnectionStatus.ONLINE -> "Online"
                            connectionStatus == ConnectionStatus.OFFLINE -> "offline"
                            else -> "Connecting to relay..."
                        }
                        Text(
                            text = statusText, 
                            style = MaterialTheme.typography.labelSmall, 
                            color = if(isTyping) MaterialTheme.colorScheme.primary else if(connectionStatus == ConnectionStatus.ONLINE) Color.Green else Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("call/${chat.id}?isVideo=false") }) {
                        Icon(Icons.Filled.Call, contentDescription = "Voice Call")
                    }
                    IconButton(onClick = { navController.navigate("call/${chat.id}?isVideo=true") }) {
                        Icon(Icons.Filled.VideoCall, contentDescription = "Video Call")
                    }
                    if (chat.isGroup) {
                        IconButton(onClick = { navController.navigate("group_admin/${chat.id}") }) {
                            Icon(Icons.Filled.AdminPanelSettings, contentDescription = "Admin Dashboard")
                        }
                    }
                    IconButton(onClick = { showDisappearingDialog = true }) {
                        Icon(Icons.Filled.Timer, contentDescription = "Disappearing Messages")
                    }
                    
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        if (!chat.isGroup && !chat.isChannel) {
                            DropdownMenuItem(
                                text = { Text("Delete Chat", color = MaterialTheme.colorScheme.error) },
                                onClick = { 
                                    expanded = false
                                    viewModel.deleteChat(chatId)
                                    navController.popBackStack()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear History") },
                                onClick = { 
                                    expanded = false
                                    viewModel.clearHistory(chatId)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Block User", color = MaterialTheme.colorScheme.error) },
                                onClick = { 
                                    expanded = false
                                    viewModel.blockUser(chatId)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Export History") },
                            onClick = { 
                                expanded = false
                                viewModel.exportMessageHistory(chatId)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
            )
        },
        bottomBar = {
            if (chat.isBlocked) {
                Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), color = Color.Transparent) {
                    Text(
                        "You blocked this user.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var attachmentMenuExpanded by remember { mutableStateOf(false) }
                    var botMenuExpanded by remember { mutableStateOf(false) }
                    
                    if (chat.isBot || chat.title.contains("BotFather", ignoreCase = true)) {
                        Box {
                            IconButton(onClick = { botMenuExpanded = true }) {
                                Icon(Icons.Filled.Terminal, contentDescription = "Bot Commands", tint = MaterialTheme.colorScheme.primary)
                            }
                            DropdownMenu(
                                expanded = botMenuExpanded,
                                onDismissRequest = { botMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("/start - start/refresh bot") },
                                    onClick = {
                                        botMenuExpanded = false
                                        viewModel.sendMessage(chatId, activeAccount?.id ?: "", "/start", null, expiresIn)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("/newbot - create a new bot") },
                                    onClick = {
                                        botMenuExpanded = false
                                        viewModel.sendMessage(chatId, activeAccount?.id ?: "", "/newbot", null, expiresIn)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("/description - bot description") },
                                    onClick = {
                                        botMenuExpanded = false
                                        viewModel.sendMessage(chatId, activeAccount?.id ?: "", "/description", null, expiresIn)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("/mybots - show a list of all bots") },
                                    onClick = {
                                        botMenuExpanded = false
                                        viewModel.sendMessage(chatId, activeAccount?.id ?: "", "/mybots", null, expiresIn)
                                    }
                                )
                            }
                        }
                    }

                    Box {
                        IconButton(onClick = { attachmentMenuExpanded = true }) {
                            Icon(Icons.Filled.AttachFile, contentDescription = "Attach")
                        }
                        DropdownMenu(
                            expanded = attachmentMenuExpanded,
                            onDismissRequest = { attachmentMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Camera") },
                                onClick = { 
                                    attachmentMenuExpanded = false
                                    viewModel.sendMessage(chatId, activeAccount?.id ?: "", "📷 Sent a photo", null, expiresIn)
                                },
                                leadingIcon = { Icon(Icons.Filled.CameraAlt, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Document") },
                                onClick = { 
                                    attachmentMenuExpanded = false
                                    showSignDialog = true
                                },
                                leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Location") },
                                onClick = { 
                                    attachmentMenuExpanded = false
                                    viewModel.sendMessage(chatId, activeAccount?.id ?: "", "📍 Location: 37.4221° N, 122.0841° W", null, expiresIn)
                                },
                                leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Message...") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                    if (inputText.isNotBlank()) {
                        IconButton(onClick = {
                            viewModel.sendMessage(chatId, activeAccount?.id ?: "", inputText, null, expiresIn)
                            inputText = ""
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        IconButton(
                            onClick = { 
                                isRecording = !isRecording
                                if (!isRecording) {
                                    // Simulate sending an audio message
                                    viewModel.sendMessage(chatId, activeAccount?.id ?: "", "🎤 Audio Message", "audio_path.mp3", expiresIn)
                                }
                            }
                        ) {
                            Icon(
                                if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic, 
                                contentDescription = "Record",
                                tint = if (isRecording) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (showSignDialog) {
            AlertDialog(
                onDismissRequest = { showSignDialog = false },
                title = { Text("Attach Document") },
                text = { Text("Do you want to sign this document before sending?") },
                confirmButton = {
                    TextButton(onClick = { 
                        viewModel.sendMessage(chatId, activeAccount?.id ?: "", "📄 Signed Document.pdf", null, expiresIn)
                        showSignDialog = false 
                    }) {
                        Text("Sign & Send")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        viewModel.sendMessage(chatId, activeAccount?.id ?: "", "📄 Document.pdf", null, expiresIn)
                        showSignDialog = false 
                    }) {
                        Text("Send As Is")
                    }
                }
            )
        }
        
        Column(modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding()) {
            if (!chat.isGroup && !chat.isChannel && !chat.isBot && !chat.isContact && !chat.isBlocked && !chat.isActionMenuDismissed) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { viewModel.addToContacts(chatId) }) {
                            Text("Add to Contacts")
                        }
                        TextButton(
                            onClick = { viewModel.blockUser(chatId) },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Block User")
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { viewModel.dismissActionMenu(chatId) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }
                }
            }
            if (chat.pinnedMessageId != null) {
                val pinnedMessage = messages.find { it.id == chat.pinnedMessageId }
                if (pinnedMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.8f), 
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PushPin, contentDescription = "Pinned", tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Pinned Message", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(pinnedMessage.text, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            val listState = androidx.compose.foundation.lazy.rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()
            val canScrollForward by remember { derivedStateOf { listState.canScrollForward } }
            
            // Auto scroll to bottom when new messages arrive if we were already at bottom
            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty() && !listState.canScrollForward) {
                    listState.scrollToItem(messages.size - 1)
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = false
                ) {
                    items(messages) { message ->
                        val isMe = message.senderId == activeAccount?.id
                        var senderName: String? = null
                        var senderStatus: String? = null
                        var isBot = false
                        if (!isMe) {
                            if (chat.isGroup) {
                                val member = groupMembers.find { it.userId == message.senderId }
                                senderName = member?.userName ?: message.senderId
                                isBot = message.senderId.startsWith("bot_")
                            } else {
                                if (chat.isBot) {
                                    senderName = chat.title
                                    isBot = true
                                } else {
                                    senderName = chat.title
                                }
                            }
                        } else {
                            senderName = activeAccount?.displayName
                            senderStatus = activeAccount?.customStatus
                        }
                        MessageBubble(
                            senderName = senderName,
                            senderStatus = senderStatus,
                            isBot = isBot,
                            message = message, 
                            isMe = isMe,
                            onLongClick = { showReactionDialogFor = message.id },
                            onButtonClick = { buttonText ->
                                if (buttonText.startsWith("Sandbox::")) {
                                    val botId = buttonText.substringAfter("::")
                                    navController.navigate("sandbox/$botId")
                                } else if (buttonText.startsWith("Dashboard::")) {
                                    val botId = buttonText.substringAfter("::")
                                    navController.navigate("dashboard/$botId")
                                } else {
                                    viewModel.sendMessage(chatId, activeAccount?.id ?: "", buttonText, null, expiresIn)
                                }
                            }
                        )
                    }
                }
                
                androidx.compose.animation.AnimatedVisibility(
                    visible = canScrollForward,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    enter = androidx.compose.animation.scaleIn(),
                    exit = androidx.compose.animation.scaleOut()
                ) {
                    FloatingActionButton(
                        onClick = { 
                            coroutineScope.launch { 
                                if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) 
                            } 
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Scroll to bottom")
                    }
                }
            }
        }
        
        if (showDisappearingDialog) {
            AlertDialog(
                onDismissRequest = { showDisappearingDialog = false },
                title = { Text("Disappearing Messages") },
                text = {
                    Column {
                        Text("Set a timer for new messages to disappear automatically.")
                        Spacer(Modifier.height(8.dp))
                        val options = listOf(
                            "10 Seconds" to 10_000L,
                            "1 Minute" to 60_000L,
                            "1 Hour" to 3_600_000L,
                            "1 Day" to 86_400_000L,
                            "1 Week" to 604_800_000L
                        )
                        options.forEach { (label, duration) ->
                            TextButton(onClick = { 
                                expiresIn = duration
                                showDisappearingDialog = false
                            }) { Text(label) }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { 
                        expiresIn = null
                        showDisappearingDialog = false
                    }) { Text("Off") }
                }
            )
        }
        
        if (showReactionDialogFor != null) {
            val selectedMessage = messages.find { it.id == showReactionDialogFor }
            AlertDialog(
                onDismissRequest = { showReactionDialogFor = null },
                title = { Text("Message Action") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                            val emojis = listOf("👍", "❤️", "😂", "😮", "😢", "🔥")
                            for (emoji in emojis) {
                                Text(
                                    text = emoji,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .combinedClickable(onClick = {
                                            viewModel.addReaction(showReactionDialogFor!!, emoji)
                                            showReactionDialogFor = null
                                        }),
                                    style = LocalTextStyle.current.copy(fontSize = androidx.compose.ui.unit.TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp))
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                        OutlinedButton(
                            onClick = {
                                if (selectedMessage?.isPinned == true) {
                                    viewModel.unpinMessage(chatId, showReactionDialogFor!!)
                                } else {
                                    viewModel.pinMessage(chatId, showReactionDialogFor!!)
                                }
                                showReactionDialogFor = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (selectedMessage?.isPinned == true) "Unpin Message" else "Pin Message")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                selectedMessage?.let {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(it.text))
                                }
                                showReactionDialogFor = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Copy Text")
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.deleteMessage(showReactionDialogFor!!)
                                showReactionDialogFor = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Delete Message", color = MaterialTheme.colorScheme.error)
                        }

                        if (!chat.isSecret) {
                            OutlinedButton(
                                onClick = {
                                    // Forward functionality - essentially just copies it to the draft of another chat or opens a picker
                                    // For simplicity, let's just insert it to input text and close dialog
                                    if (selectedMessage != null) {
                                        inputText = "Fwd: ${selectedMessage.text}"
                                    }
                                    showReactionDialogFor = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Forward Quote")
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun MessageBubble(message: Message, isMe: Boolean, senderName: String? = null, senderStatus: String? = null, isBot: Boolean = false, onLongClick: () -> Unit, onButtonClick: ((String) -> Unit)? = null) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        if (senderName != null) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 2.dp, start = if(isMe) 0.dp else 8.dp, end = if(isMe) 8.dp else 0.dp)) {
                if (isBot) {
                    Icon(Icons.Filled.SmartToy, contentDescription = "Bot", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                }
                Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                    Text(senderName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    if (senderStatus != null && senderStatus.isNotEmpty()) {
                        Text(senderStatus, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .background(
                    if (isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) 
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    RoundedCornerShape(
                        topStart = 16.dp, 
                        topEnd = 16.dp, 
                        bottomStart = if (isMe) 16.dp else 0.dp, 
                        bottomEnd = if (isMe) 0.dp else 16.dp
                    )
                )
                .combinedClickable(
                    onClick = { /* play audio if applicable */ },
                    onLongClick = onLongClick
                )
                .padding(12.dp)
        ) {
            Column {
                if (message.audioPath != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Play Audio", tint = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp))
                        Text(message.text, color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (message.isE2EEncrypted) {
                            Icon(Icons.Filled.Lock, contentDescription = "Encrypted", modifier = Modifier.size(12.dp), tint = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha=0.6f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f))
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(message.text, color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                if (message.reaction != null) {
                    Box(modifier = Modifier.offset(y = 8.dp).background(Color.DarkGray, CircleShape).padding(4.dp)) {
                        Text(message.reaction)
                    }
                }
            }
        }
        
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                formatTime(message.timestamp), 
                style = MaterialTheme.typography.labelSmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (message.expiresAt != null) {
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Filled.Timer, contentDescription = "Disappearing", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isMe) {
                Spacer(Modifier.width(4.dp))
                if (message.isRead) {
                    Icon(Icons.Filled.DoneAll, contentDescription = "Read", modifier = Modifier.size(16.dp), tint = Color(0xFF4CAF50))
                } else if (message.isDelivered) {
                    Icon(Icons.Filled.DoneAll, contentDescription = "Delivered", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Icon(Icons.Filled.Check, contentDescription = "Sent", modifier = Modifier.size(16.dp), tint = Color(0xFF4CAF50))
                }
            }
        }
        
        if (!message.buttonsData.isNullOrEmpty()) {
            Spacer(Modifier.height(4.dp))
            val buttons = message.buttonsData.split("||")
            FlowRow(
                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
                modifier = Modifier.padding(top = 4.dp).fillMaxWidth()
            ) {
                buttons.forEach { buttonText ->
                    OutlinedButton(
                        onClick = { onButtonClick?.invoke(buttonText) },
                        modifier = Modifier.padding(end = 4.dp, bottom = 4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        val display = if (buttonText.contains("::")) buttonText.substringBefore("::") else buttonText
                        Text(display, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return formatter.format(date)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: AppViewModel, chatId: String, navController: NavController) {
    val chats by viewModel.chats.collectAsState()
    val chat = chats.find { it.id == chatId } ?: return
    
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    
    var showAvatarViewer by remember { mutableStateOf(false) }
    val avatars = remember(chatId) {
        listOf(
            "https://picsum.photos/seed/${chatId}/400",
            "https://picsum.photos/seed/${chatId}_1/400",
            "https://picsum.photos/seed/${chatId}_2/400"
        )
    }

    if (showAvatarViewer) {
        AvatarViewerDialog(
            avatars = avatars,
            initialPage = 0,
            onDismiss = { showAvatarViewer = false }
        )
    }
    
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* more options */ }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { showAvatarViewer = true },
                    contentAlignment = Alignment.Center
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    coil.compose.AsyncImage(
                        model = coil.request.ImageRequest.Builder(context)
                            .data(avatars.first())
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(chat.title, style = MaterialTheme.typography.headlineMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                
                val statusText = if (connectionStatus == ConnectionStatus.ONLINE) "online" else "last seen recently"
                val statusColor = if (connectionStatus == ConnectionStatus.ONLINE) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                
                Text(statusText, style = MaterialTheme.typography.bodyMedium, color = statusColor)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Info", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                
                val chatUsername = chat.title.lowercase().replace(" ", "_")
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { 
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString("t.me/$chatUsername")) 
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Text("@$chatUsername", style = MaterialTheme.typography.bodyLarge)
                    Text("Username (Tap to copy link)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Hey there! I am using NodeX.", style = MaterialTheme.typography.bodyLarge)
                Text("Bio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                if (!chat.isBlocked) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.blockUser(chatId)
                            navController.popBackStack()
                        }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Block, contentDescription = "Block", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Block User", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    Text("User Blocked", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
