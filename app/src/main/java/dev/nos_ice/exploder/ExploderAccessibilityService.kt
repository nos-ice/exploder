package dev.nos_ice.exploder

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent

class ExploderAccessibilityService : AccessibilityService() {
    private var dialogFlag = true

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> {
                    if (dialogFlag) {
                        dialogFlag = false
                        val intent =
                            Intent(this@ExploderAccessibilityService, ExplodeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                }

                Intent.ACTION_USER_PRESENT -> {
                    dialogFlag = true
                }

                "ACTION_FINISH_EXPLODING" -> {
                    dialogFlag = true
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_USER_PRESENT)

        registerReceiver(receiver, filter)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}
    override fun onInterrupt() {}
}