import re

with open("app/src/main/java/com/example/ui/AppViewModel.kt", "r") as f:
    content = f.read()

replacement = """
    fun addAccountAction() {
        _isAddingAccount.value = true
    }

    fun toggle2FA(accountId: String, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val acc = repository.allAccounts.firstOrNull()?.find { it.id == accountId }
            if (acc != null) {
                repository.insertAccount(acc.copy(is2FAEnabled = enabled))
            }
        }
    }

    fun switchAccount(accountId: String) {"""

content = content.replace("    fun switchAccount(accountId: String) {", replacement)

with open("app/src/main/java/com/example/ui/AppViewModel.kt", "w") as f:
    f.write(content)
