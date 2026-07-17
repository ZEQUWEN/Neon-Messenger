with open("app/src/main/java/com/example/ui/ParticleSystem.kt", "r") as f:
    content = f.read()

content = content.replace("Canvas(modifier = Modifier.fillMaxSize()) {", "val currentFrame = lastFrameTime\n    Canvas(modifier = Modifier.fillMaxSize()) {")

with open("app/src/main/java/com/example/ui/ParticleSystem.kt", "w") as f:
    f.write(content)
