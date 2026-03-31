package dev.nos_ice.exploder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import dev.nos_ice.exploder.ui.theme.ExploderTheme
import kotlinx.coroutines.delay

class BlackoutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setShowWhenLocked(true)

        setContent {
            ExploderTheme {
                BlackoutScreen { finishExploding() }
            }
        }
    }

    fun finishExploding() {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = layoutParams

        val intent = Intent("ACTION_FINISH_EXPLODING")
        sendBroadcast(intent)

        finish()
    }
}

@Composable
fun BlackoutScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("exploder", Context.MODE_PRIVATE)
    var penaltyTime by remember {
        mutableIntStateOf(prefs.getInt("penalty_time", 5))
    }
    var penaltyCleared by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (penaltyTime > 0) {
            delay(1000L)
            penaltyTime--
        }
        penaltyCleared = true
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable {
                if (penaltyCleared) {
                    onFinished()
                }
            }
    )
}