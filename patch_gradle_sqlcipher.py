import re

with open("gradle/libs.versions.toml", "r") as f:
    content = f.read()

content = content.replace('secretsGradlePlugin = "2.0.1"', 'secretsGradlePlugin = "2.0.1"\nsqlcipher = "4.5.4"\nsqliteKtx = "2.4.0"')
content = content.replace('tink-android = { group = "com.google.crypto.tink", name = "tink-android", version.ref = "tink" }', 'tink-android = { group = "com.google.crypto.tink", name = "tink-android", version.ref = "tink" }\nandroid-database-sqlcipher = { group = "net.zetetic", name = "android-database-sqlcipher", version.ref = "sqlcipher" }\nandroidx-sqlite-ktx = { group = "androidx.sqlite", name = "sqlite-ktx", version.ref = "sqliteKtx" }')

with open("gradle/libs.versions.toml", "w") as f:
    f.write(content)

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace('implementation(libs.androidx.room.runtime)', 'implementation(libs.androidx.room.runtime)\n  implementation(libs.android.database.sqlcipher)\n  implementation(libs.androidx.sqlite.ktx)')

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
