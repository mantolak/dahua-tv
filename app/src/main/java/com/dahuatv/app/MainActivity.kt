package com.dahuatv.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dahuatv.app.data.CameraRepository
import com.dahuatv.app.ui.DahuaTvApp
import com.dahuatv.app.ui.theme.DahuaTvTheme

class MainActivity : ComponentActivity() {
    private val repository by lazy { CameraRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // A camera wall should never let the TV fall asleep.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            DahuaTvTheme {
                DahuaTvApp(repository)
            }
        }
    }
}
