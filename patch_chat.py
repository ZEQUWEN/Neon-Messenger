import sys

with open('app/src/main/java/com/example/ui/ChatScreen.kt', 'r') as f:
    content = f.read()

target = """                    var attachmentMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { attachmentMenuExpanded = true }) {
                            Icon(Icons.Filled.AttachFile, contentDescription = "Attach")
                        }"""

replacement = """                    var attachmentMenuExpanded by remember { mutableStateOf(false) }
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
                                    text = { Text("/mybots - edit your bots") },
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
                        }"""

if target in content:
    with open('app/src/main/java/com/example/ui/ChatScreen.kt', 'w') as f:
        f.write(content.replace(target, replacement))
    print("Patched successfully")
else:
    print("Target not found")
