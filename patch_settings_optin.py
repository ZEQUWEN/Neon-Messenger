with open("app/src/main/java/com/example/ui/SettingsScreens.kt", "r") as f:
    content = f.read()

content = content.replace("@Composable\nfun SettingsAccountsScreen", "@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\nfun SettingsAccountsScreen")

with open("app/src/main/java/com/example/ui/SettingsScreens.kt", "w") as f:
    f.write(content)
