package com.example.blocklistapply

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("BlocklistApply", "${intent.action}: ${BlocklistApplier.apply(context)}")
    }
}
