import re

with open("app/src/main/java/com/example/data/AppDatabase.kt", "r") as f:
    content = f.read()

content = content.replace("net.zetetic.database.sqlcipher.SupportFactory", "net.sqlcipher.database.SupportFactory")
content = content.replace("net.zetetic", "net.sqlcipher")
content = content.replace("net.sqlcipher.database.sqlcipher.SupportFactory", "net.sqlcipher.database.SupportFactory")

with open("app/src/main/java/com/example/data/AppDatabase.kt", "w") as f:
    f.write(content)
