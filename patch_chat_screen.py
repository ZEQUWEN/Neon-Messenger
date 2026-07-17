with open("app/src/main/java/com/example/ui/ChatScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    if "val messages by viewModel.getMessages(chatId).collectAsState(initial = emptyList())" in line:
        new_lines.append(line)
        new_lines.append("    val groupMembers by if (chat.isGroup) viewModel.getGroupMembers(chatId).collectAsState(initial = emptyList()) else remember { mutableStateOf(emptyList()) }\n")
    elif "MessageBubble(" in line and "isMe = isMe," in lines[i+2]:
        new_lines.append("""                        var senderName: String? = null
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
                        }
""")
        new_lines.append(line)
        new_lines.append("                            senderName = senderName,\n")
        new_lines.append("                            isBot = isBot,\n")
    else:
        new_lines.append(line)

with open("app/src/main/java/com/example/ui/ChatScreen.kt", "w") as f:
    f.writelines(new_lines)
