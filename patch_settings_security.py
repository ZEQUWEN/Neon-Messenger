import re

with open("app/src/main/java/com/example/ui/SettingsScreens.kt", "r") as f:
    content = f.read()

replacement = """        Text("Data and Backup", modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            val context = androidx.compose.ui.platform.LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            var isExporting by remember { mutableStateOf(false) }
            val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/zip")
            ) { uri ->
                if (uri != null) {
                    isExporting = true
                    coroutineScope.launch {
                        val success = com.example.data.BackupUtility.exportDatabase(context, uri)
                        isExporting = false
                        android.widget.Toast.makeText(context, if (success) "Backup exported successfully" else "Backup failed", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }

            SettingsListItem(
                icon = { Icon(Icons.Filled.Backup, null) },
                title = "Export Local Backup",
                subtitle = if (isExporting) "Exporting..." else "Save encrypted database backup",
                onClick = {
                    exportLauncher.launch("messenger_backup_${System.currentTimeMillis()}.zip")
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Delete My Account","""

content = content.replace('            Text("Delete My Account",', replacement)

with open("app/src/main/java/com/example/ui/SettingsScreens.kt", "w") as f:
    f.write(content)

