@file:OptIn(ExperimentalMaterial3Api::class)

package com.pointcheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.pointcheck.core.navigation.AppNavigation
import com.pointcheck.core.presentation.components.AppBottomBar
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
    val snackbar = remember { SnackbarHostState() }
    val navController = rememberNavController()
    
    PointCheckTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = { AppBottomBar(navController) }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                AppNavigation(snackbar, navController)
            }
        }
    }
}
