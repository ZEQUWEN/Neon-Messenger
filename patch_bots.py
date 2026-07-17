with open("app/src/main/java/com/example/ui/MainScreen.kt", "r") as f:
    content = f.read()

import re

# Add to navigation drawer
drawer_item = """            item {
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.SmartToy, "Discover Bots") },
                    label = { Text("Discover Bots") },
                    selected = false,
                    onClick = { 
                        navController.navigate("discover_bots")
                        onCloseDrawer()
                    }
                )
            }
            item {
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Group, "Create Group") }"""
content = content.replace('            item {\n                NavigationDrawerItem(\n                    icon = { Icon(Icons.Filled.Group, "Create Group") }', drawer_item)

# Add route
nav_item = """                            composable("chat_list") { ChatListScreen(viewModel, mainNavController) }
                            composable("discover_bots") { DiscoverBotsScreen(viewModel, mainNavController) }"""
content = content.replace('                            composable("chat_list") { ChatListScreen(viewModel, mainNavController) }', nav_item)

with open("app/src/main/java/com/example/ui/MainScreen.kt", "w") as f:
    f.write(content)
