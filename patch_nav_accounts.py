import re

with open("app/src/main/java/com/example/ui/MainScreen.kt", "r") as f:
    content = f.read()

content = content.replace('composable("settings") { SettingsMenuScreen(viewModel, mainNavController) }', 'composable("settings") { SettingsMenuScreen(viewModel, mainNavController) }\n                            composable("settings/accounts") { SettingsAccountsScreen(viewModel, mainNavController) }')

with open("app/src/main/java/com/example/ui/MainScreen.kt", "w") as f:
    f.write(content)
