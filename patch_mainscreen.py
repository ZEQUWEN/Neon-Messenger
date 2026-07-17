with open("app/src/main/java/com/example/ui/MainScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    if i == 259:
        new_lines.append(line)
        new_lines.append("            ParticleOverlay(theme)\n")
    else:
        new_lines.append(line)

with open("app/src/main/java/com/example/ui/MainScreen.kt", "w") as f:
    f.writelines(new_lines)
