import re

with open("app/src/main/java/com/example/data/AppDatabase.kt", "r") as f:
    content = f.read()

imports = """import androidx.room.RoomDatabase
import net.zetetic.database.sqlcipher.SupportFactory
"""

content = content.replace("import androidx.room.RoomDatabase", imports)

db_builder = """                val factory = SupportFactory("messenger_secret_passphrase".toByteArray())
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "messenger_database"
                )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()"""

content = content.replace("""                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "messenger_database"
                )
                .fallbackToDestructiveMigration()""", db_builder)

with open("app/src/main/java/com/example/data/AppDatabase.kt", "w") as f:
    f.write(content)
