with open("app/src/main/java/com/example/ui/BotService.kt", "r") as f:
    content = f.read()

content = content.replace("import com.example.ui.botapi.BotRegistry", "import com.example.ui.botapi.BotRegistry\nimport com.example.utils.MessageSanitizer")
content = content.replace("bot?.onMessageReceived(messageText, chat, repository, signalProtocolManager)", "bot?.onMessageReceived(MessageSanitizer.sanitize(messageText), chat, repository, signalProtocolManager)")
content = content.replace("bot.onMessageReceived(messageText, chat, repository, signalProtocolManager)", "bot.onMessageReceived(MessageSanitizer.sanitize(messageText), chat, repository, signalProtocolManager)")

with open("app/src/main/java/com/example/ui/BotService.kt", "w") as f:
    f.write(content)
