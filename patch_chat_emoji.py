import re

with open("app/src/main/java/com/example/ui/ChatScreen.kt", "r") as f:
    content = f.read()

replacement = """
                    var showEmojiPicker by remember { mutableStateOf(false) }
                    IconButton(onClick = { showEmojiPicker = !showEmojiPicker }) {
                        Text("😀", style = MaterialTheme.typography.titleLarge)
                    }
                    if (showEmojiPicker) {
                        DropdownMenu(
                            expanded = showEmojiPicker,
                            onDismissRequest = { showEmojiPicker = false }
                        ) {
                            val emojis = listOf("😀", "😂", "🥰", "😎", "🤔", "👍", "❤️", "🔥")
                            Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                emojis.forEach { emoji ->
                                    Text(
                                        text = emoji,
                                        style = MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.clickable { 
                                            inputText += emoji
                                            showEmojiPicker = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    OutlinedTextField(
"""

content = content.replace("                    OutlinedTextField(", replacement)

with open("app/src/main/java/com/example/ui/ChatScreen.kt", "w") as f:
    f.write(content)
