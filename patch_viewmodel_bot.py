with open("app/src/main/java/com/example/ui/AppViewModel.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if "if (chat?.isGroup == true && text.contains(\"@bot\", ignoreCase = true)) {" in line:
        skip = True
        new_lines.append("            if (chat != null) {\n")
        new_lines.append("                BotService.handleMessage(text, chat, repository, signalProtocolManager)\n")
        new_lines.append("            }\n")
    elif skip and "if (chat?.isBot == true) {" in line:
        pass
    elif skip and "}" in line and "repository.insertMessage(replyMsg)" in new_lines[-1] if len(new_lines) > 0 else False: # this condition doesn't make sense but we need to skip till the end of the second block. Let's just do it cleanly.
        pass

