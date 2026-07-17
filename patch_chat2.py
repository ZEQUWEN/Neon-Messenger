import sys

with open('app/src/main/java/com/example/ui/ChatScreen.kt', 'r') as f:
    content = f.read()

target = """                                DropdownMenuItem(
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
                                )"""

replacement = """                                DropdownMenuItem(
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
                                )"""

if target in content:
    with open('app/src/main/java/com/example/ui/ChatScreen.kt', 'w') as f:
        f.write(content.replace(target, replacement))
    print("Patched successfully")
else:
    print("Target not found")
