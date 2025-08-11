package com.example.football

import android.os.Bundle
import androidx.activity.ComponentActivity
import kotlinx.coroutines.runBlocking

/**
 * Activity that does nothing except open then close. This is launched from the watch face in the
 * case where the app has never been opened, which in turn then sets a flag to indicate that the
 * app has been opened at least once.
 */
class LaunchConfirmedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runBlocking {
            setHasOpenedOnce(this@LaunchConfirmedActivity)
        }
        finish()
    }
}