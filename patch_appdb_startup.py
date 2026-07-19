import re

with open("app/src/main/java/com/example/data/AppDatabase.kt", "r") as f:
    content = f.read()

replacement = """        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val dbName = "messenger_database_encrypted"
                try {
                    net.sqlcipher.database.SQLiteDatabase.loadLibs(context.applicationContext)
                    val dbFile = context.getDatabasePath(dbName)
                    if (dbFile.exists()) {
                        val db = net.sqlcipher.database.SQLiteDatabase.openDatabase(
                            dbFile.absolutePath,
                            "messenger_secret_passphrase",
                            null,
                            net.sqlcipher.database.SQLiteDatabase.OPEN_READONLY
                        )
                        db.version
                        db.close()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "Database integrity check failed, deleting database...", e)
                    context.deleteDatabase(dbName)
                }

                val factory = SupportFactory("messenger_secret_passphrase".toByteArray())
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    dbName
                )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }"""

# find the getDatabase function
start = content.find("fun getDatabase(context: Context): AppDatabase {")
end = content.find("}\n    }", start) + 1

if start != -1 and end != -1:
    content = content[:start] + replacement + content[end:]
    with open("app/src/main/java/com/example/data/AppDatabase.kt", "w") as f:
        f.write(content)
else:
    print("Could not find getDatabase function")
