with open("app/src/main/java/com/example/ui/ChatScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if line.startswith("import androidx.compose.runtime.getValue") or line.startswith("import androidx.compose.runtime.setValue"):
        continue
    new_lines.append(line)

content = "".join(new_lines)
content = content.replace("package com.example.ui\n", "package com.example.ui\nimport androidx.compose.runtime.getValue\nimport androidx.compose.runtime.setValue\n")

with open("app/src/main/java/com/example/ui/ChatScreen.kt", "w") as f:
    f.write(content)
