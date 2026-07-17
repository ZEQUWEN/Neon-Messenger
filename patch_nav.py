import re

with open("app/src/main/java/com/example/ui/MainScreen.kt", "r") as f:
    content = f.read()

# Remove them from top
content = re.sub(r'    val rootNavController = rememberNavController\(\)\n', '', content)
content = re.sub(r'    val mainNavController = rememberNavController\(\)\n', '', content)

# Add them to inside the blocks
content = content.replace('            if (!isAuthComplete) {', '            if (!isAuthComplete) {\n                val rootNavController = rememberNavController()')
content = content.replace('            } else {\n                ModalNavigationDrawer(', '            } else {\n                val mainNavController = rememberNavController()\n                ModalNavigationDrawer(')

with open("app/src/main/java/com/example/ui/MainScreen.kt", "w") as f:
    f.write(content)
