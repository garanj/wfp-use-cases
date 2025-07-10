package com.example.surf.complication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.surf.data.updateComplications

class TapReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, p1: Intent?) {
        if (context == null) {
            return
        }
        updateComplications(context)
    }
}