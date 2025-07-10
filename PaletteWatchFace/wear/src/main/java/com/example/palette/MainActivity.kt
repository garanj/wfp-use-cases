/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.palette

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.palette.data.StatusManager
import com.example.palette.data.updateComplications
import com.example.palette.ui.PaletteNavigation
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            PaletteNavigation()
        }

        lifecycleScope.launch {
            setHasOpenedOnce(this@MainActivity)
        }
    }
}

suspend fun setHasOpenedOnce(context: Context) {
    val statusManager = StatusManager(context)
    statusManager.setOpenedOnce(true)
    updateComplications(context)
}
