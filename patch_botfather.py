import sys

with open('app/src/main/java/com/example/ui/botapi/BotFather.kt', 'r') as f:
    content = f.read()

target = """                when (messageText.lowercase().trim()) {
                    "create bot", "/newbot" -> {
                        states[chat.id] = BotFatherState.WaitingForBotName
                        sendReplyWithButtons("Alright, a new bot. How are we going to call it? Please choose a name for your bot.", emptyList(), chat.id, repository, signalProtocolManager)
                    }
                    "manage bots", "/mybots" -> {
                        val customBots = BotRegistry.getCustomBots()
                        if (customBots.isEmpty()) {
                            sendReplyWithButtons("You don't have any bots yet. Create one with 'Create Bot'.", listOf("Create Bot"), chat.id, repository, signalProtocolManager)
                        } else {
                            val buttons = customBots.map { it.name }
                            sendReplyWithButtons("Choose a bot to manage:", buttons, chat.id, repository, signalProtocolManager)
                        }
                    }
                    else -> {
                        val customBot = BotRegistry.getCustomBots().find { it.name == messageText }
                        if (customBot != null) {
                            states[chat.id] = BotFatherState.ManagingBot(customBot.id)
                            sendBotManagementMenu(customBot as CustomBot, chat.id, repository, signalProtocolManager)
                        } else {
                            sendReplyWithButtons(
                                "I am the BotFather. You can create new bots and manage them here.",
                                listOf("Create Bot", "Manage Bots"),
                                chat.id, repository, signalProtocolManager
                            )
                        }
                    }
                }"""

replacement = """                when (messageText.lowercase().trim()) {
                    "/start" -> {
                        states[chat.id] = BotFatherState.Idle
                        sendReplyWithButtons("I can help you create and manage Telegram bots. If you're new to the Bot API, please see the manual.\\n\\nYou can control me by sending these commands:\\n\\n/newbot - create a new bot\\n/mybots - edit your bots\\n/description - bot description", emptyList(), chat.id, repository, signalProtocolManager)
                    }
                    "/description" -> {
                        sendReplyWithButtons("BotFather is the one bot to rule them all. Use it to create new bot accounts and manage your existing bots.\\n\\nAbout Telegram bots:\\nhttps://core.telegram.org/bots\\nBot API manual:\\nhttps://core.telegram.org/bots/api\\n\\nContact @BotSupport if you have questions about the Bot API.", emptyList(), chat.id, repository, signalProtocolManager)
                    }
                    "create bot", "/newbot", "/new bot" -> {
                        states[chat.id] = BotFatherState.WaitingForBotName
                        sendReplyWithButtons("Alright, a new bot. How are we going to call it? Please choose a name for your bot.", emptyList(), chat.id, repository, signalProtocolManager)
                    }
                    "manage bots", "/mybots", "/my bots" -> {
                        val customBots = BotRegistry.getCustomBots()
                        if (customBots.isEmpty()) {
                            sendReplyWithButtons("You don't have any bots yet. Create one with '/newbot'.", listOf("Create Bot"), chat.id, repository, signalProtocolManager)
                        } else {
                            val buttons = customBots.map { it.name }
                            sendReplyWithButtons("Choose a bot to manage:", buttons, chat.id, repository, signalProtocolManager)
                        }
                    }
                    else -> {
                        val customBot = BotRegistry.getCustomBots().find { it.name == messageText }
                        if (customBot != null) {
                            states[chat.id] = BotFatherState.ManagingBot(customBot.id)
                            sendBotManagementMenu(customBot as CustomBot, chat.id, repository, signalProtocolManager)
                        } else {
                            sendReplyWithButtons(
                                "I am the BotFather. You can create new bots and manage them here.",
                                listOf("Create Bot", "Manage Bots"),
                                chat.id, repository, signalProtocolManager
                            )
                        }
                    }
                }"""

if target in content:
    with open('app/src/main/java/com/example/ui/botapi/BotFather.kt', 'w') as f:
        f.write(content.replace(target, replacement))
    print("Patched successfully")
else:
    print("Target not found")
