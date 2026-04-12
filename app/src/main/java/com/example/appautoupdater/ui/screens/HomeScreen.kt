import androidx.compose.foundation.layout.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.unit.dp;
import androidx.compose.ui.tooling.preview.Preview;
import androidx.compose.material3.Text;

@Composable
fun HomeScreen(availableUpdates: List<Update>) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Auto Updater") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Available Updates:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            for (update in availableUpdates) {
                UpdateCard(update)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun UpdateCard(update: Update) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Version: ${update.version}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Patch Notes: ${update.patchNotes}")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { updateAction(update) }) {
                Text("Update Now")
            }
        }
    }
}

fun updateAction(update: Update) {
    // Placeholder for update action logic
}

data class Update(val version: String, val patchNotes: String)  

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    HomeScreen(availableUpdates = listOf(
        Update(version = "1.0.1", patchNotes = "Fixed bugs and improved performance"),
        Update(version = "1.0.2", patchNotes = "Added new features and stability improvements")
    ))
}