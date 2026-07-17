with open("app/src/main/java/com/example/ui/ParticleSystem.kt", "r") as f:
    content = f.read()

content = content.replace("    val currentFrame = lastFrameTime\n    Canvas(modifier = Modifier.fillMaxSize()) {\n", "    Canvas(modifier = Modifier.fillMaxSize()) {\n        val currentFrame = lastFrameTime\n")

with open("app/src/main/java/com/example/ui/ParticleSystem.kt", "w") as f:
    f.write(content)
