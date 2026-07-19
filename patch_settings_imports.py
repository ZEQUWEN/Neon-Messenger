import re

with open("app/src/main/java/com/example/ui/SettingsScreens.kt", "r") as f:
    content = f.read()

imports = """import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.scale
import androidx.compose.material3.Switch
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
"""

if "import androidx.compose.foundation.lazy.LazyColumn" not in content:
    content = content.replace("import androidx.compose.foundation.layout.*", "import androidx.compose.foundation.layout.*\n" + imports)

with open("app/src/main/java/com/example/ui/SettingsScreens.kt", "w") as f:
    f.write(content)
