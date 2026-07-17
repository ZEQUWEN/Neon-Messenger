with open("gradle/libs.versions.toml", "r") as f:
    content = f.read()

content = content.replace(
    'secretsGradlePlugin = "2.0.1"\n',
    'secretsGradlePlugin = "2.0.1"\ntink = "1.8.0"\n'
)

content = content.replace(
    'firebase-ai = { group = "com.google.firebase", name = "firebase-ai" }\n',
    'firebase-ai = { group = "com.google.firebase", name = "firebase-ai" }\ntink-android = { group = "com.google.crypto.tink", name = "tink-android", version.ref = "tink" }\n'
)

with open("gradle/libs.versions.toml", "w") as f:
    f.write(content)

with open("app/build.gradle.kts", "r") as f:
    build_content = f.read()

build_content = build_content.replace(
    'implementation(libs.androidx.security.crypto)',
    'implementation(libs.androidx.security.crypto)\n  implementation(libs.tink.android)'
)

with open("app/build.gradle.kts", "w") as f:
    f.write(build_content)
