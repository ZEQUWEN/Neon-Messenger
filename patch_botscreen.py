with open("app/src/main/java/com/example/ui/DiscoverBotsScreen.kt", "r") as f:
    content = f.read()

import re

# We will remove the availableBots and BotProfile from the top, and just import BotRegistry and Bot
content = content.replace("data class BotProfile(\n    val id: String,\n    val name: String,\n    val description: String,\n    val category: String,\n    val longDescription: String\n)\n\nval availableBots = listOf(\n    BotProfile(\"b1\", \"WeatherBot\", \"Get daily weather updates.\", \"Utilities\", \"WeatherBot provides real-time weather forecasts, severe weather alerts, and daily summaries. Simply type @WeatherBot in a group or send a direct message.\"),\n    BotProfile(\"b2\", \"ReminderBot\", \"I can remind you of things.\", \"Productivity\", \"Set timers and reminders using natural language. For example: '@ReminderBot remind me to call Mom in 2 hours'.\"),\n    BotProfile(\"b3\", \"NewsBot\", \"Top news stories.\", \"News\", \"Get personalized news feeds from around the world. Subscribe to topics and receive daily digests.\"),\n    BotProfile(\"b4\", \"GPT Assistant\", \"How can I help?\", \"AI\", \"A powerful AI assistant ready to answer questions, write code, or just chat.\"),\n    BotProfile(\"b5\", \"CryptoBot\", \"Track crypto prices.\", \"Finance\", \"Real-time cryptocurrency tracking and alerts. Monitor your favorite coins seamlessly.\")\n)", "")

# Now add imports
content = content.replace("import androidx.navigation.NavController", "import androidx.navigation.NavController\nimport com.example.ui.botapi.BotRegistry\nimport com.example.ui.botapi.Bot")

# Update DiscoverBotsScreen
content = content.replace("val categories = listOf(\"All\") + availableBots.map { it.category }.distinct()", "val availableBots = BotRegistry.getAllBots()\n    val categories = listOf(\"All\") + availableBots.map { it.category }.distinct()")

content = content.replace("var selectedBot by remember { mutableStateOf<BotProfile?>(null) }", "var selectedBot by remember { mutableStateOf<Bot?>(null) }")

with open("app/src/main/java/com/example/ui/DiscoverBotsScreen.kt", "w") as f:
    f.write(content)
