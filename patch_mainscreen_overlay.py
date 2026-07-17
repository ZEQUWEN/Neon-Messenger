with open("app/src/main/java/com/example/ui/MainScreen.kt", "r") as f:
    content = f.read()

# Remove ParticleOverlay(theme) from the end
content = content.replace("            ParticleOverlay(theme)\n        }\n    }\n}\n", "        }\n    }\n}\n")

# Add it after AnimatedContent
animated_content_end = "                    AppTheme.DEFAULT -> ElegantDarkBackground(opacity = opacity)\n                }\n            }\n"
content = content.replace(animated_content_end, animated_content_end + "            ParticleOverlay(theme)\n\n")

with open("app/src/main/java/com/example/ui/MainScreen.kt", "w") as f:
    f.write(content)
