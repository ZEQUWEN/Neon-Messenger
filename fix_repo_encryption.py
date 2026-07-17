import re

with open("app/src/main/java/com/example/data/AppDatabase.kt", "r") as f:
    content = f.read()

# Add import for map
if 'import kotlinx.coroutines.flow.map' not in content:
    content = content.replace('import androidx.room.*', 'import androidx.room.*\nimport kotlinx.coroutines.flow.map')

# Replace val allAccounts
content = content.replace(
    'val allAccounts: Flow<List<UserAccount>> = userDao.getAllAccounts()',
    'val allAccounts: Flow<List<UserAccount>> = userDao.getAllAccounts().map { list -> list.map { it.copy(sessionToken = it.sessionToken?.let { token -> CryptoManager.decrypt(token) }) } }'
)

# Replace val allContacts
content = content.replace(
    'val allContacts: Flow<List<Contact>> = contactDao.getAllContacts()',
    'val allContacts: Flow<List<Contact>> = contactDao.getAllContacts().map { list -> list.map { it.copy(name = CryptoManager.decrypt(it.name), phoneNumber = it.phoneNumber?.let { phone -> CryptoManager.decrypt(phone) }) } }'
)

# Replace getMessages
content = content.replace(
    'fun getMessages(chatId: String) = messageDao.getMessagesForChat(chatId)',
    'fun getMessages(chatId: String) = messageDao.getMessagesForChat(chatId).map { list -> list.map { it.copy(text = CryptoManager.decrypt(it.text), audioPath = it.audioPath?.let { p -> CryptoManager.decrypt(p) }, mediaPath = it.mediaPath?.let { p -> CryptoManager.decrypt(p) }, documentData = it.documentData?.let { p -> CryptoManager.decrypt(p) }) } }'
)

# Replace insertAccount
content = content.replace(
    'suspend fun insertAccount(account: UserAccount) = userDao.insertAccount(account)',
    'suspend fun insertAccount(account: UserAccount) = userDao.insertAccount(account.copy(sessionToken = account.sessionToken?.let { CryptoManager.encrypt(it) }))'
)

# Replace insertMessage
content = content.replace(
    'suspend fun insertMessage(message: Message) = messageDao.insertMessage(message)',
    'suspend fun insertMessage(message: Message) = messageDao.insertMessage(message.copy(text = CryptoManager.encrypt(message.text), audioPath = message.audioPath?.let { CryptoManager.encrypt(it) }, mediaPath = message.mediaPath?.let { CryptoManager.encrypt(it) }, documentData = message.documentData?.let { CryptoManager.encrypt(it) }))'
)

# Replace insertContact
content = content.replace(
    'suspend fun insertContact(contact: Contact) = contactDao.insertContact(contact)',
    'suspend fun insertContact(contact: Contact) = contactDao.insertContact(contact.copy(name = CryptoManager.encrypt(contact.name), phoneNumber = contact.phoneNumber?.let { CryptoManager.encrypt(it) }))'
)

with open("app/src/main/java/com/example/data/AppDatabase.kt", "w") as f:
    f.write(content)
