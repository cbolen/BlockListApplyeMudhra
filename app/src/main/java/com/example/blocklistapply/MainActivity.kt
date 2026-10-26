package com.example.blocklistapply

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView
    private lateinit var details: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        status = TextView(this).apply {
            setPadding(24, 24, 24, 24)
            textSize = 15f
            setTextColor(Color.WHITE)
        }
        details = TextView(this).apply {
            setPadding(24, 16, 24, 16)
            textSize = 13f
            setTextIsSelectable(true)
        }
        setContentView(ScrollView(this).apply {
            isFillViewport = true
            addView(LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                fitsSystemWindows = true
                addView(status, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
                addView(details, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
            })
        })
    }

    private fun refresh() {
        val missing = BlocklistApplier.missing(this)
        val lines = StringBuilder()
        when {
            missing.isEmpty() -> {
                status.text = "PROTECTED: all ${BlocklistApplier.HASHES.size} compromised eMudhra keys are blocklisted"
                status.setBackgroundColor(Color.rgb(0, 120, 0))
            }
            !BlocklistApplier.hasPermission(this) -> {
                status.text = "NOT PROTECTED: ${missing.size} keys missing and WRITE_SECURE_SETTINGS is not granted"
                status.setBackgroundColor(Color.rgb(170, 0, 0))
                lines.append("Grant once over adb:\n")
                    .append("adb shell pm grant $packageName android.permission.WRITE_SECURE_SETTINGS\n\n")
            }
            else -> {
                status.text = "NOT PROTECTED: ${missing.size} keys missing (write failed)"
                status.setBackgroundColor(Color.rgb(170, 0, 0))
            }
        }
        val have = BlocklistApplier.current(this).toSet()
        BlocklistApplier.HASHES.forEach { (h, name) ->
            lines.append(if (h in have) "[blocked] " else "[MISSING] ").append(name).append("\n  ").append(h).append("\n")
        }
        lines.append("\nTotal entries in pubkey_blacklist: ${have.size}")
        details.text = lines
    }

    override fun onResume() {
        super.onResume()
        BlocklistApplier.apply(this)
        refresh()
    }
}
