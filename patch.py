with open('app/src/main/java/com/example/ui/CallScreen.kt', 'r') as f:
    content = f.read()

# Remove the import from the top
content = content.replace("import androidx.compose.material.icons.filled.ArrowBack\npackage com.example.ui\n", "package com.example.ui\nimport androidx.compose.material.icons.filled.ArrowBack\n")

with open('app/src/main/java/com/example/ui/CallScreen.kt', 'w') as f:
    f.write(content)
