package dev.nos_ice.exploder

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import dev.nos_ice.exploder.ui.theme.ExploderTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExploderTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { MainTopBar() }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CommandTextField()
                        TimeLimitField()
                        PenaltyTimeField()
                        SelectAudio { saveAudioUri(this@MainActivity, it) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar() {
    TopAppBar(
        title = {
            Text("Exploder")
        },
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun CommandTextField() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("explode", Context.MODE_PRIVATE)
    var commandText by remember {
        mutableStateOf(prefs.getString("command_text", "+-") ?: "+-")
    }

    Column {
        OutlinedTextField(
            value = commandText,
            onValueChange = {
                commandText = it.replace(Regex("[^+-]"), "")
                prefs.edit { putString("command_text", it) }
            },
            label = { Text("ダイアログ終了コマンド") }
        )
        Text(
            text = "音量ボタンを押してダイアログを閉じます\n" +
                    "音量+の場合は「+」、音量-の場合は「-」を押す順番に入力してください",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun TimeLimitField() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("exploder", Context.MODE_PRIVATE)
    var timeLimit by remember {
        mutableStateOf(prefs.getInt("time_limit", 5).toString())
    }

    Column {
        OutlinedTextField(
            value = timeLimit,
            onValueChange = {
                timeLimit = it.filter { c -> c.isDigit() }
                prefs.edit { putInt("time_limit", timeLimit.toIntOrNull() ?: 5) }
            },
            label = { Text("タイムリミット(s)") }
        )
        Text(
            text = "既定値: 5",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun PenaltyTimeField() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("exploder", Context.MODE_PRIVATE)
    var penaltyTime by remember {
        mutableStateOf(prefs.getInt("penalty_time", 10).toString())
    }

    Column {
        OutlinedTextField(
            value = penaltyTime,
            onValueChange = {
                penaltyTime = it.filter { c -> c.isDigit() }
                prefs.edit { putInt("penalty_time", penaltyTime.toIntOrNull() ?: 10) }
            },
            label = { Text("ペナルティ(s)") }
        )
        Text(
            text = "既定値: 10",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun SelectAudio(onAudioSelected: (Uri) -> Unit) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            onAudioSelected(it)
        }
    }
    Button(onClick = { launcher.launch(arrayOf("audio/*")) }) {
        Text("音声ファイルを選択")
    }
}

fun saveAudioUri(context: Context, uri: Uri) {
    val prefs = context.getSharedPreferences("exploder", Context.MODE_PRIVATE)
    prefs.edit { putString("saved_audio_uri", uri.toString()) }
}