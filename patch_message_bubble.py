with open("app/src/main/java/com/example/ui/ChatScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    if "fun MessageBubble(message: Message, isMe: Boolean, onLongClick: () -> Unit)" in line:
        new_lines.append("fun MessageBubble(message: Message, isMe: Boolean, senderName: String? = null, isBot: Boolean = false, onLongClick: () -> Unit) {\n")
    elif "Box(" in line and "if (isMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)" in lines[i+3]:
        # we need to inject the sender name BEFORE the Box
        new_lines.append("        if (!isMe && senderName != null) {\n")
        new_lines.append("            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 2.dp, start = 8.dp)) {\n")
        new_lines.append("                if (isBot) {\n")
        new_lines.append("                    Icon(Icons.Filled.SmartToy, contentDescription = \"Bot\", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)\n")
        new_lines.append("                    Spacer(Modifier.width(4.dp))\n")
        new_lines.append("                }\n")
        new_lines.append("                Text(senderName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)\n")
        new_lines.append("            }\n")
        new_lines.append("        }\n")
        new_lines.append(line)
    elif "MessageBubble(" in line and "isMe = isMe," in lines[i+2]:
        new_lines.append(line)
    else:
        new_lines.append(line)

with open("app/src/main/java/com/example/ui/ChatScreen.kt", "w") as f:
    f.writelines(new_lines)
