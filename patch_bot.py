with open("app/src/main/java/com/example/ui/botapi/Bot.kt", "r") as f:
    content = f.read()

content = content.replace("import java.util.UUID", "import java.util.UUID\nimport com.example.utils.MessageSanitizer")

target = """    suspend fun sendReply(
        text: String,
        chatId: String,
        repository: MessengerRepository,
        signalProtocolManager: SignalProtocolManager
    ) {
        val encryptedReply = signalProtocolManager.encryptMessage(text)"""

replacement = """    suspend fun sendReply(
        text: String,
        chatId: String,
        repository: MessengerRepository,
        signalProtocolManager: SignalProtocolManager
    ) {
        val sanitizedText = MessageSanitizer.sanitize(text)
        val encryptedReply = signalProtocolManager.encryptMessage(sanitizedText)"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/ui/botapi/Bot.kt", "w") as f:
    f.write(content)
