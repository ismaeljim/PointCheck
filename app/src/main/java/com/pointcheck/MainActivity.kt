package com.pointcheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.pointcheck.core.navigation.AppNavigation
import com.pointcheck.core.ui.theme.PointCheckTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PointCheckApp()
        }
    }
}

@Composable
fun PointCheckApp() {
    PointCheckTheme {
        AppNavigation()
    }
}
