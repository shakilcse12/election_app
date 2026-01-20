package com.example.electionapp


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.electionapp.ui.main.MainScreen
import com.example.electionapp.ui.theme.ElectionAppTheme
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // --- REQUIRED FOR OSM ---
        Configuration.getInstance().load(
            applicationContext,
            run { getSharedPreferences("osm_pref", MODE_PRIVATE) }
        )
        Configuration.getInstance().userAgentValue = packageName
        setContent {
            ElectionAppTheme {
                MainScreen()
            }
        }
    }
}
