# Neon Messenger Bot API

This repository includes a comprehensive, native Bot API for the Android Messenger platform. It enables developers to easily create and integrate bots that interact with users both in direct messages and in group chats.

## Architecture

The Bot API consists of the following core components:

*   **`Bot` Interface**: The contract that all bots must implement.
*   **`BotRegistry`**: A central manager where all bots are registered.
*   **`BotService`**: The router that directs incoming messages to the appropriate registered bot.

## Creating a Bot

To create a new bot, simply implement the `Bot` interface.

```kotlin
import com.example.ui.botapi.Bot
import com.example.ui.Chat
import com.example.data.MessengerRepository
import com.example.crypto.SignalProtocolManager

class MyCustomBot : Bot {
    override val id = "my_custom_bot_id"
    override val name = "MyCustomBot"
    override val description = "Short description of what the bot does."
    override val category = "Utilities"
    override val longDescription = "A longer description explaining the bot's features in detail."

    override suspend fun onMessageReceived(
        messageText: String,
        chat: Chat,
        repository: MessengerRepository,
        signalProtocolManager: SignalProtocolManager
    ) {
        // 1. Process the incoming messageText
        val responseText = "Hello! You said: $messageText"
        
        // 2. Send a reply back to the chat using the helper method
        sendReply(responseText, chat.id, repository, signalProtocolManager)
    }
}
```

## Registering the Bot

Once you have created your bot class, register it in `BotRegistry.kt` so it becomes available in the app.

```kotlin
object BotRegistry {
    // ...
    init {
        registerBot(WeatherBot())
        registerBot(EchoBot())
        registerBot(MyCustomBot()) // Add your bot here
    }
    // ...
}
```

## How It Works

*   **Direct Messages**: When a user chats with a bot directly, the `BotService` intercepts the message and routes it to the specific bot's `onMessageReceived` method.
*   **Group Chats**: When a user mentions a bot by name (e.g., `@WeatherBot`) in a group chat, the `BotService` detects the mention and forwards the message to that bot. The bot can then reply directly in the group.
*   **Simulated Asynchrony**: Because this is a demonstration, bots may use `delay()` in their handlers to simulate network calls to external APIs.

## Included Examples

We have included several example bots demonstrating different capabilities:

1.  **`EchoBot`**: A simple bot that demonstrates receiving a message and echoing it back.
2.  **`WeatherBot`**: Demonstrates simulated fetching of external API data (weather conditions) based on keywords.
3.  **`CryptoBot`**: Demonstrates command parsing (e.g., `/price BTC`).
4.  **`ReminderBot`**: Demonstrates a simple utility interaction.

These examples can be found in `app/src/main/java/com/example/ui/botapi/`.
