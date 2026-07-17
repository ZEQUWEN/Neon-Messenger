import re

with open('app/src/main/java/com/example/ui/CallScreen.kt', 'r') as f:
    content = f.read()

# remove old CallsListScreen
content = re.sub(r'@OptIn\(androidx\.compose\.material3\.ExperimentalMaterial3Api::class\)\s*@Composable\s*fun CallsListScreen.*?\}\s*\}', '', content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/CallScreen.kt', 'w') as f:
    f.write(content)
