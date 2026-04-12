package com.example.appautoupdater

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.appautoupdater.ui.theme.AppUpdateManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppUpdateManagerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    UpdateScreen() 
                }
            }
        }
    }
}

@Composable
fun UpdateScreen() {
    val updateStatus = remember { mutableStateOf("Checking for updates...") }
    // Simulated update management logic goes here

    Text(text = updateStatus.value)
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AppUpdateManagerTheme {
        UpdateScreen()
    }
}