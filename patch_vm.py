import sys

with open("app/src/main/java/com/example/ui/AppViewModel.kt", "r") as f:
    content = f.read()

target = """            val chat = repository.allChats.firstOrNull()?.find { it.id == chatId }
            if (chat?.isGroup == true && text.contains("@bot", ignoreCase = true)) {
                kotlinx.coroutines.delay(2000)
                val botReply = "Group Bot: I saw you mention a bot! Your message: $text"
                val encryptedReply = signalProtocolManager.encryptMessage(botReply)
                val replyMsg = Message(
                    id = java.util.UUID.randomUUID().toString(),
                    chatId = chatId,
                    senderId = "bot_group",
                    text = encryptedReply,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertMessage(replyMsg)
            }

            if (chat?.isBot == true) {
                kotlinx.coroutines.delay(2000)
                val botReply = "Beep boop! You said: $text"
                val encryptedReply = signalProtocolManager.encryptMessage(botReply)
                val replyMsg = Message(
                    id = java.util.UUID.randomUUID().toString(),
                    chatId = chatId,
                    senderId = "bot_${chat.id}",
                    text = encryptedReply,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertMessage(replyMsg)
            }"""

replacement = """            val chat = repository.allChats.firstOrNull()?.find { it.id == chatId }
            if (chat != null) {
                BotService.handleMessage(text, chat, repository, signalProtocolManager)
            }"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/AppViewModel.kt", "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
