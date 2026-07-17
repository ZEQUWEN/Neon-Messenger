package com.example.ui

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto.SignalProtocolManager
import com.example.data.MessengerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import com.example.utils.MessageSanitizer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

enum class AppTheme {
    DEFAULT,
    NEON_SNOWFLAKES,
    NEON_CHERRY_BLOSSOM,
    NEON_CONFETTI,
    NEON_MOON,
    NEON_ROOM_FOG
}

@Entity(tableName = "accounts")
data class UserAccount(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val profilePicUrl: String, // Or gif URL
    val is2FAEnabled: Boolean = false,
    val isActive: Boolean = false,
    val bio: String = "",
    val sessionToken: String? = null,
    val customStatus: String = ""
)

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String?,
    val isRegistered: Boolean = true
)

@Entity(    tableName = "messages",
    indices = [androidx.room.Index("chatId"), androidx.room.Index("senderId")]
)
data class Message(
    @PrimaryKey val id: String,
    val chatId: String = "",
    val senderId: String,
    val text: String,
    val audioPath: String? = null,
    val isE2EEncrypted: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val reaction: String? = null,
    val expiresAt: Long? = null,
    val isDelivered: Boolean = false,
    val isRead: Boolean = false,
    val isPinned: Boolean = false,
    val mediaPath: String? = null,
    val mediaType: String? = null,
    val documentData: String? = null,
    val locationData: String? = null,
    val buttonsData: String? = null
)

@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey val id: String,
    val title: String,
    val isChannel: Boolean = false,
    val isGroup: Boolean = false,
    val isBot: Boolean = false,
    val isSecret: Boolean = false,
    val lastMessage: String,
    val unreadCount: Int = 0,
    val pinnedMessageId: String? = null,
    val isContact: Boolean = false,
    val isBlocked: Boolean = false,
    val isActionMenuDismissed: Boolean = false,
    val isArchived: Boolean = false
)

@Entity(
    tableName = "group_members",
    primaryKeys = ["chatId", "userId"],
    indices = [androidx.room.Index("chatId"), androidx.room.Index("userId")]
)
data class GroupMember(
    val chatId: String,
    val userId: String,
    val userName: String,
    val isAdmin: Boolean = false,
    val canReadMessages: Boolean = true,
    val canSendMessages: Boolean = true
)

@Entity(
    tableName = "drafts",
    indices = [androidx.room.Index("chatId")]
)
data class Draft(
    @PrimaryKey val chatId: String,
    val text: String
)

enum class ConnectionStatus {
    ONLINE, OFFLINE, CONNECTING
}

class AppViewModel(private val repository: MessengerRepository) : ViewModel() {
    private val signalProtocolManager = SignalProtocolManager()

    private val _theme = MutableStateFlow(
        run {
            val savedTheme = repository.getTheme()
            if (savedTheme != null) {
                try {
                    AppTheme.valueOf(savedTheme)
                } catch (e: Exception) {
                    AppTheme.DEFAULT
                }
            } else {
                AppTheme.DEFAULT
            }
        }
    )
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    private val _isAutoThemeEnabled = MutableStateFlow(repository.getAutoThemeSwitcherEnabled())
    val isAutoThemeEnabled: StateFlow<Boolean> = _isAutoThemeEnabled.asStateFlow()

    private val _customPrimaryColor = MutableStateFlow<Long?>(repository.getCustomPrimaryColor())
    val customPrimaryColor: StateFlow<Long?> = _customPrimaryColor.asStateFlow()

    private val _customSecondaryColor = MutableStateFlow<Long?>(repository.getCustomSecondaryColor())
    val customSecondaryColor: StateFlow<Long?> = _customSecondaryColor.asStateFlow()

    private val _favoriteThemes = MutableStateFlow(repository.getFavoriteThemes())
    val favoriteThemes: StateFlow<Set<String>> = _favoriteThemes.asStateFlow()

    private val _batterySaverEnabled = MutableStateFlow(repository.getBatterySaverEnabled())
    val batterySaverEnabled: StateFlow<Boolean> = _batterySaverEnabled.asStateFlow()

    private val _themeOpacity = MutableStateFlow(repository.getThemeOpacity())
    val themeOpacity: StateFlow<Float> = _themeOpacity.asStateFlow()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.ONLINE)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _requires2FA = MutableStateFlow<String?>(null)
    val requires2FA: StateFlow<String?> = _requires2FA.asStateFlow()

    private val _confirmationCode = MutableStateFlow<String?>(null)
    val confirmationCode: StateFlow<String?> = _confirmationCode.asStateFlow()

    private val _pendingEmail = MutableStateFlow<String?>(null)
    val pendingEmail: StateFlow<String?> = _pendingEmail.asStateFlow()

    fun requestEmailConfirmation(email: String) {
        val code = (100000..999999).random().toString()
        _confirmationCode.value = code
        _pendingEmail.value = email
    }

    fun verifyEmailConfirmation(code: String): Boolean {
        val currentCode = _confirmationCode.value
        if (currentCode != null && currentCode == code) {
            val email = _pendingEmail.value
            _confirmationCode.value = null
            _pendingEmail.value = null
            
            // Update the email in active account
            viewModelScope.launch {
                val account = repository.allAccounts.firstOrNull()?.firstOrNull { it.isActive }
                if (account != null && email != null) {
                    repository.insertAccount(account.copy(username = email))
                }
            }
            
            return true
        }
        return false
    }

    private val _isAddingAccount = MutableStateFlow(false)
    val isAddingAccount: StateFlow<Boolean> = _isAddingAccount.asStateFlow()

    fun startAddAccount() {
        _isAddingAccount.value = true
        viewModelScope.launch { repository.logoutAll() }
    }

    fun clearAddingAccount() {
        _isAddingAccount.value = false
    }
    val accounts: StateFlow<List<UserAccount>> = repository.allAccounts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val chats: StateFlow<List<Chat>> = repository.allChats
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val contacts: StateFlow<List<Contact>> = repository.allContacts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isE2EEnabled = MutableStateFlow(true)
    val isE2EEnabled: StateFlow<Boolean> = _isE2EEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            // Periodic cleanup of expired messages
            launch {
                while (true) {
                    val now = System.currentTimeMillis()
                    repository.deleteExpiredMessages(now)
                    kotlinx.coroutines.delay(1000) // check every second
                }
            }
            
            // Seed initial data if empty
            val accs = repository.allAccounts.firstOrNull(); if (accs.isNullOrEmpty()) {
                
                    repository.insertAccount(UserAccount("1", "@neo_hacker", "Neo", "https://i.pravatar.cc/150?img=11", true, true))
                    repository.insertAccount(UserAccount("2", "@synth_wave", "Synth Wave", "https://i.pravatar.cc/150?img=33", false, false))
                    repository.insertAccount(UserAccount("3", "@cyber_punk", "Cyber P.", "https://i.pravatar.cc/150?img=55", false, false))
                    
                    repository.insertChat(Chat("c1", "Neon Coders", isGroup = true, lastMessage = "Let's build in Compose! \uD83D\uDD25", unreadCount = 4))
                    repository.insertChat(Chat("botfather", "BotFather", isBot = true, lastMessage = "I am the BotFather. I can help you create and manage your bots.", unreadCount = 0))
                    repository.insertChat(Chat("c2", "Cyberpunk Daily", isChannel = true, lastMessage = "Welcome to the future.", unreadCount = 12))
                    repository.insertChat(Chat("c3", "SynthBot", isBot = true, lastMessage = "Command executed.", unreadCount = 0))
                    repository.insertChat(Chat("c4", "@trinity", isGroup = false, lastMessage = "The matrix has you.", unreadCount = 1))

                    repository.insertGroupMember(GroupMember("c1", "u1", "Sarah Connor", isAdmin = true))
                    repository.insertGroupMember(GroupMember("c1", "u2", "John Doe", isAdmin = false))
                    repository.insertGroupMember(GroupMember("c1", "u3", "Crypto Alpha", isAdmin = false))
                    repository.insertGroupMember(GroupMember("c1", "u4", "Neon Hacker", isAdmin = false))
                }
        }
    }

    fun getMessages(chatId: String) = repository.getMessages(chatId).map { messages ->
        messages.map { msg ->
            msg.copy(text = signalProtocolManager.decryptMessage(msg.text))
        }
    }
    fun getGroupMembers(chatId: String) = repository.getGroupMembers(chatId)
    
    suspend fun getDraft(chatId: String) = repository.getDraft(chatId)

    fun updateAdminStatus(chatId: String, userId: String, isAdmin: Boolean) {
        viewModelScope.launch {
            repository.updateAdminStatus(chatId, userId, isAdmin)
        }
    }

    fun blockUser(chatId: String) {
        viewModelScope.launch {
            repository.updateBlockedStatus(chatId, true)
        }
    }

    fun unblockUser(chatId: String) {
        viewModelScope.launch {
            repository.updateBlockedStatus(chatId, false)
        }
    }

    fun clearHistory(chatId: String) {
        viewModelScope.launch {
            repository.clearHistory(chatId)
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            repository.deleteChat(chatId)
        }
    }

    fun addToContacts(chatId: String) {
        viewModelScope.launch {
            repository.updateContactStatus(chatId, true)
        }
    }

    fun dismissActionMenu(chatId: String) {
        viewModelScope.launch {
            repository.updateActionMenuDismissed(chatId, true)
        }
    }

    private val _typingChats = MutableStateFlow<Set<String>>(emptySet())
    val typingChats: StateFlow<Set<String>> = _typingChats.asStateFlow()

    fun simulateTyping(chatId: String) {
        viewModelScope.launch {
            _typingChats.update { it + chatId }
            kotlinx.coroutines.delay(3000)
            _typingChats.update { it - chatId }
        }
    }

    fun exportMessageHistory(chatId: String) {
        viewModelScope.launch {
            val messages = repository.getMessages(chatId).firstOrNull() ?: emptyList()
            val text = messages.joinToString("\n") { msg ->
                val decrypted = signalProtocolManager.decryptMessage(msg.text)
                "[${java.util.Date(msg.timestamp)}] ${msg.senderId}: $decrypted"
            }
            val encryptedBackup = signalProtocolManager.encryptMessage(text)
            // Simulating saving to a file. In a real app we'd use FileOutputStream to Context.filesDir.
            println("Exported history for $chatId: \n$encryptedBackup")
        }
    }
    fun updateProfile(id: String, username: String, displayName: String, bio: String, profilePicUrl: String, customStatus: String = "") {
        viewModelScope.launch {
            repository.updateProfile(id, username, displayName, bio, profilePicUrl, customStatus)
        }
    }
    
    fun addGroupMember(chatId: String, userId: String, userName: String, isAdmin: Boolean) {
        viewModelScope.launch {
            repository.insertGroupMember(com.example.ui.GroupMember(chatId, userId, userName, isAdmin))
        }
    }

    fun updateBotPermissions(chatId: String, userId: String, canRead: Boolean, canSend: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val member = repository.getGroupMemberSync(chatId, userId)
            if (member != null) {
                repository.insertGroupMember(member.copy(canReadMessages = canRead, canSendMessages = canSend))
            }
        }
    }

    fun removeGroupMember(chatId: String, userId: String) {
        viewModelScope.launch {
            repository.removeMember(chatId, userId)
        }
    }

    fun sendMessage(chatId: String, senderId: String, text: String, audioPath: String? = null, expiresIn: Long? = null) {
        viewModelScope.launch {
            val sanitizedText = MessageSanitizer.sanitize(text)
            val encryptedMsg = signalProtocolManager.encryptMessage(sanitizedText)
            val msg = Message(
                id = java.util.UUID.randomUUID().toString(),
                chatId = chatId,
                senderId = senderId,
                text = encryptedMsg,
                audioPath = audioPath,
                timestamp = System.currentTimeMillis(),
                expiresAt = if (expiresIn != null) System.currentTimeMillis() + expiresIn else null
            )
            repository.insertMessage(msg)
            
            // Simulate the other person typing a reply after a couple of seconds if it's not a group or channel
            // (or even if it is, for demo)
            kotlinx.coroutines.delay(1000)
            simulateTyping(chatId)
            val chat = repository.allChats.firstOrNull()?.find { it.id == chatId }
            if (chat != null) {
                BotService.handleMessage(text, chat, repository, signalProtocolManager)
            }

        }
    }

    fun addReaction(messageId: String, reaction: String) {
        viewModelScope.launch {
            repository.updateReaction(messageId, reaction)
        }
    }

    fun saveDraft(chatId: String, draft: String) {
        viewModelScope.launch {
            repository.updateDraft(chatId, draft)
        }
    }

    fun pinMessage(chatId: String, messageId: String) {
        viewModelScope.launch {
            repository.updatePinnedMessage(chatId, messageId)
            repository.updatePinStatus(messageId, true)
        }
    }

    fun unpinMessage(chatId: String, messageId: String) {
        viewModelScope.launch {
            repository.updatePinnedMessage(chatId, null)
            repository.updatePinStatus(messageId, false)
        }
    }

    fun markMessagesAsRead(chatId: String, myUserId: String) {
        viewModelScope.launch {
            repository.markAsRead(chatId, myUserId)
        }
    }

    fun switchTheme(newTheme: AppTheme) {
        _theme.value = newTheme
        repository.saveTheme(newTheme.name)
    }

    fun setAutoThemeEnabled(enabled: Boolean) {
        _isAutoThemeEnabled.value = enabled
        repository.saveAutoThemeSwitcherEnabled(enabled)
        if (enabled) {
            checkAutoTheme()
        }
    }

    fun checkAutoTheme() {
        if (_isAutoThemeEnabled.value) {
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            if (hour >= 18 || hour < 6) {
                switchTheme(AppTheme.NEON_MOON)
            } else {
                switchTheme(AppTheme.NEON_CHERRY_BLOSSOM)
            }
        }
    }

    fun setCustomPrimaryColor(color: Long?) {
        _customPrimaryColor.value = color
        if (color != null) repository.saveCustomPrimaryColor(color) else repository.saveCustomPrimaryColor(0L)
    }

    fun setCustomSecondaryColor(color: Long?) {
        _customSecondaryColor.value = color
        if (color != null) repository.saveCustomSecondaryColor(color) else repository.saveCustomSecondaryColor(0L)
    }

    fun resetTheme() {
        setCustomPrimaryColor(null)
        setCustomSecondaryColor(null)
        switchTheme(AppTheme.DEFAULT)
        setAutoThemeEnabled(false)
        setThemeOpacity(1.0f)
    }

    fun toggleArchive(chatId: String, isArchived: Boolean) {
        viewModelScope.launch {
            repository.updateArchiveStatus(chatId, isArchived)
        }
    }
    
    fun toggleFavoriteTheme(themeName: String) {
        val current = _favoriteThemes.value.toMutableSet()
        if (current.contains(themeName)) current.remove(themeName) else current.add(themeName)
        _favoriteThemes.value = current
        repository.saveFavoriteThemes(current)
    }

    fun setBatterySaverEnabled(enabled: Boolean) {
        _batterySaverEnabled.value = enabled
        repository.saveBatterySaverEnabled(enabled)
    }

    fun setThemeOpacity(opacity: Float) {
        _themeOpacity.value = opacity
        repository.saveThemeOpacity(opacity)
    }

    fun importTheme(themeCode: String) {
        try {
            val parts = themeCode.substringAfter("Neon Messenger Theme Code: ").split("-")
            if (parts.size >= 3) {
                val themeName = parts[0]
                val primaryStr = parts[1]
                val secondaryStr = parts[2]
                
                switchTheme(AppTheme.valueOf(themeName))
                setCustomPrimaryColor(if (primaryStr != "def") primaryStr.toLongOrNull() else null)
                setCustomSecondaryColor(if (secondaryStr != "def") secondaryStr.toLongOrNull() else null)
                setAutoThemeEnabled(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun syncContacts() {
        viewModelScope.launch {
            // Simulated contact sync logic
            // In a real app we would use Context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, ...)
            // and compare with backend registered users.
            repository.insertChat(Chat("c5", "Trinity", isGroup = false, lastMessage = "New contact synchronized from phone.", unreadCount = 1))
            repository.insertChat(Chat("c6", "Morpheus", isGroup = false, lastMessage = "Are you ready?", unreadCount = 0))
        }
    }


    fun addBot(bot: com.example.ui.Chat) {
        viewModelScope.launch {
            val exists = repository.allChats.firstOrNull()?.any { it.id == bot.id } == true
            if (!exists) {
                repository.insertChat(bot)
            }
        }
    }

    fun createSecretChat(name: String, description: String = "Secret Chat", linkOrUsername: String = "") {
        viewModelScope.launch {
            val chat = Chat(
                id = java.util.UUID.randomUUID().toString(),
                title = name,
                isGroup = false,
                isChannel = false,
                isSecret = true,
                lastMessage = description,
                unreadCount = 0
            )
            repository.insertChat(chat)
        }
    }

    fun createChat(name: String, description: String, photoUri: String, isPrivate: Boolean, linkOrUsername: String, isGroup: Boolean, isChannel: Boolean) {
        viewModelScope.launch {
            val chat = Chat(
                id = java.util.UUID.randomUUID().toString(),
                title = name,
                isGroup = isGroup,
                isChannel = isChannel,
                lastMessage = if (isGroup) "Group created" else "Channel created",
                unreadCount = 0
            )
            repository.insertChat(chat)
        }
    }
    fun logout() {
        viewModelScope.launch {
            repository.logoutAll()
        }
    }

    
    fun insertContact(contact: Contact) {
        viewModelScope.launch {
            repository.insertContact(contact)
        }
    }

    fun deleteContact(contactId: String) {
        viewModelScope.launch {
            repository.deleteContact(contactId)
        }
    }

    fun createAccount(username: String, displayName: String) {
        viewModelScope.launch {
            val newId = java.util.UUID.randomUUID().toString()
            val newAccount = UserAccount(id = newId, username = username, displayName = displayName, profilePicUrl = "", isActive = false, is2FAEnabled = false, bio = "")
            repository.insertAccount(newAccount)
            switchAccount(newId)
        }
    }
    
    fun switchAccount(accountId: String) {
        viewModelScope.launch {
            val account = accounts.value.find { it.id == accountId }
            if (account?.is2FAEnabled == true) {
                _requires2FA.value = accountId
            } else {
                repository.switchActiveAccount(accountId)
            }
        }
    }

    fun verify2FA(code: String) {
        if (code.length == 6) {
            val accountId = _requires2FA.value
            if (accountId != null) {
                viewModelScope.launch {
                    repository.switchActiveAccount(accountId)
                    _requires2FA.value = null
                }
            }
        }
    }

    fun cancel2FA() {
        _requires2FA.value = null
    }

    fun toggle2FA(accountId: String, currentEnabled: Boolean) {
        viewModelScope.launch {
            repository.update2FA(accountId, !currentEnabled)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun deleteAccount(accountId: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAccount(accountId)
            onDeleted()
        }
    }

    // --- Search functionality context ---
}
