import re

with open("app/src/main/java/com/example/ui/AppViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("fun switchAccount(accountId: String) {", """fun createAccount(username: String, displayName: String) {
        viewModelScope.launch {
            val newId = java.util.UUID.randomUUID().toString()
            val newAccount = UserAccount(id = newId, username = username, displayName = displayName, profilePicUrl = "", isActive = false, is2FAEnabled = false, bio = "")
            repository.insertAccount(newAccount)
            switchAccount(newId)
        }
    }
    
    fun switchAccount(accountId: String) {""")

with open("app/src/main/java/com/example/ui/AppViewModel.kt", "w") as f:
    f.write(content)
