package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.MessengerRepository
import com.example.ui.AppViewModel
import com.example.ui.MainAppNavigation
import com.example.ui.theme.NeonMessengerTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.utils.CrashReporter.init(this)
        com.example.utils.CrashReporter.logLifecycleEvent("onCreate")

        val lastCrash = com.example.utils.CrashReporter.getLastCrash(this)
        if (lastCrash != null) {
            setContent {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Text(text = "PREVIOUS CRASH:\n$lastCrash", modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()))
                }
            }
            return
        }

        try {
            com.example.data.CryptoManager.init(applicationContext)
            net.sqlcipher.database.SQLiteDatabase.loadLibs(applicationContext)
            val db = AppDatabase.getDatabase(applicationContext)
            val sharedPrefs = getSharedPreferences("neon_messenger_prefs", android.content.Context.MODE_PRIVATE)
            val repository = MessengerRepository(db.userDao(), db.chatDao(), db.messageDao(), db.groupMemberDao(), db.draftDao(), db.contactDao(), sharedPrefs)

            val factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return AppViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
            
            val viewModel: AppViewModel by viewModels { factory }
            viewModel.checkAutoTheme()

            enableEdgeToEdge()
            setContent {
                val primaryColorLong by viewModel.customPrimaryColor.collectAsState()
                val secondaryColorLong by viewModel.customSecondaryColor.collectAsState()
                
                val primary = if (primaryColorLong != null && primaryColorLong != 0L) Color(primaryColorLong!!.toULong()) else null
                val secondary = if (secondaryColorLong != null && secondaryColorLong != 0L) Color(secondaryColorLong!!.toULong()) else null
                
                NeonMessengerTheme(customPrimary = primary, customSecondary = secondary) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.background
                    ) {
                        MainAppNavigation(viewModel)
                    }
                }
            }
        } catch (e: Exception) {
            setContent {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Text(text = "CRASH: \${e.stackTraceToString()}", modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()))
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        com.example.utils.CrashReporter.logLifecycleEvent("onStart")
    }

    override fun onResume() {
        super.onResume()
        com.example.utils.CrashReporter.logLifecycleEvent("onResume")
    }

    override fun onPause() {
        super.onPause()
        com.example.utils.CrashReporter.logLifecycleEvent("onPause")
    }

    override fun onStop() {
        super.onStop()
        com.example.utils.CrashReporter.logLifecycleEvent("onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        com.example.utils.CrashReporter.logLifecycleEvent("onDestroy")
    }
}
