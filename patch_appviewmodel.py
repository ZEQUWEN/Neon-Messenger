with open("app/src/main/java/com/example/ui/AppViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("import kotlinx.coroutines.flow.update", "import kotlinx.coroutines.flow.update\nimport com.example.utils.MessageSanitizer")

target = """    fun sendMessage(chatId: String, senderId: String, text: String, audioPath: String? = null, expiresAt: Long? = null) {
        viewModelScope.launch {
            val encryptedMsg = signalProtocolManager.encryptMessage(text)"""

replacement = """    fun sendMessage(chatId: String, senderId: String, text: String, audioPath: String? = null, expiresAt: Long? = null) {
        viewModelScope.launch {
            val sanitizedText = MessageSanitizer.sanitize(text)
            val encryptedMsg = signalProtocolManager.encryptMessage(sanitizedText)"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/ui/AppViewModel.kt", "w") as f:
    f.write(content)
