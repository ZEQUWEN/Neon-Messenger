import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("            com.example.data.CryptoManager.init(applicationContext)\n", "            com.example.data.CryptoManager.init(applicationContext)\n            System.loadLibrary(\"sqlcipher\")\n")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
