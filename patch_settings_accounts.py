import re

with open("app/src/main/java/com/example/ui/SettingsScreens.kt", "r") as f:
    content = f.read()

replacement = """            SettingsListItem(
                icon = { Icon(Icons.Filled.Group, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) },
                title = "Accounts",
                subtitle = "Manage authorized accounts",
                onClick = { navController.navigate("settings/accounts") }
            )
            SettingsListItem(
                icon = { Icon(Icons.Filled.Person, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) },"""

content = content.replace("            SettingsListItem(\n                icon = { Icon(Icons.Filled.Person, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) },", replacement)

with open("app/src/main/java/com/example/ui/SettingsScreens.kt", "w") as f:
    f.write(content)
