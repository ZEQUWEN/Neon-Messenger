with open("app/src/main/java/com/example/ui/MainScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if i >= 1269 and i <= 1289:
        pass
    else:
        new_lines.append(line)

fixed_chunk = """            item {
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.SmartToy, "Discover Bots") },
                    label = { Text("Discover Bots") },
                    selected = false,
                    onClick = { 
                        navController.navigate("discover_bots")
                        onCloseDrawer()
                    }
                )
            }
            item {
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Group, "Create Group") },
                    label = { Text("Create Group") },
                    selected = false,
                    onClick = { 
                        onCreateGroupClick()
                        onCloseDrawer()
                    }
                )
            }
"""
new_lines.insert(1269, fixed_chunk)

with open("app/src/main/java/com/example/ui/MainScreen.kt", "w") as f:
    f.writelines(new_lines)

