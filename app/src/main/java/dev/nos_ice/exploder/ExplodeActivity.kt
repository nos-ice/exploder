package dev.nos_ice.exploder

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import dev.nos_ice.exploder.ui.theme.ExploderTheme
import kotlinx.coroutines.delay

class CommandInput {
    var progress by mutableIntStateOf(0)
        private set

    fun makeProgress() {
        progress++
    }

    fun resetProgress() {
        progress = 0
    }
}

class ExplodeActivity : ComponentActivity() {
    private val commandInput = CommandInput()
    private lateinit var commandText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setShowWhenLocked(true)

        val prefs = this.getSharedPreferences("explode", MODE_PRIVATE)
        commandText = prefs.getString("command_text", "+-") ?: "+-"

        setContent {
            ExploderTheme {
                ExplodeDialog {
                    val prefs = this.getSharedPreferences("exploder", MODE_PRIVATE)
                    val audioUriString = prefs.getString("saved_audio_uri", null)
                    audioUriString?.let {
                        MediaPlayer().apply {
                            setDataSource(this@ExplodeActivity, it.toUri())
                            prepare()
                            start()
                        }
                    }
                    blackout()
                }
            }
        }
    }

    fun blackout() {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = 0f
        window.attributes = layoutParams

        val intent = Intent(this, BlackoutActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (commandInput.progress == commandText.length) {
            finish()
            return true
        }

        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (commandText[commandInput.progress] == '+') {
                    Log.d("EA", "+")
                    commandInput.makeProgress()
                    if (commandInput.progress == commandText.length) {
                        finish()
                    }
                } else {
                    commandInput.resetProgress()
                }
                return true
            }

            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (commandText[commandInput.progress] == '-') {
                    Log.d("EA", "-")
                    commandInput.makeProgress()
                    if (commandInput.progress == commandText.length) {
                        finish()
                    }
                } else {
                    commandInput.resetProgress()
                }
                return true
            }

            else -> {
                commandInput.resetProgress()
                return super.onKeyDown(keyCode, event)
            }
        }
    }
}

@Composable
fun ExplodeDialog(onExploded: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("exploder", Context.MODE_PRIVATE)
    var timeLeft by remember {
        mutableIntStateOf(prefs.getInt("time_limit", 5))
    }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        onExploded()
    }

    Popup(
        alignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text("このデバイスは${timeLeft}秒後に爆発します")
                }
            }
        }
    }
}
