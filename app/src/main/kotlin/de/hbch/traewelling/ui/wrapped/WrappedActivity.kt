package de.hbch.traewelling.ui.wrapped

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.hbch.traewelling.theme.MainTheme

class WrappedActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainTheme {
                var initialized by remember { mutableStateOf(false) }
                var currentStep by remember { mutableIntStateOf(0) }

                Scaffold(
                    content = { innerPadding ->
                        if (!initialized) {
                            WrappedIsBeingPrepared(
                                modifier = Modifier.fillMaxWidth().padding(innerPadding)
                            )
                        } else {

                        }
                    }
                )
            }
        }
    }
}