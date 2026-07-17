with open("app/src/main/java/com/example/ui/MainScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if "@OptIn(ExperimentalMaterial3Api::class)" in line and "fun DiscoverBotsScreen(viewModel: AppViewModel, navController: NavController) {" in lines[lines.index(line)+1]:
        skip = True
    if skip and line.startswith("}") and lines.index(line) == len(lines)-1: # naive skip
        pass
    elif not skip:
        new_lines.append(line)
        
with open("app/src/main/java/com/example/ui/MainScreen.kt", "w") as f:
    f.writelines(new_lines)
