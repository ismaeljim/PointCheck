package com.pointcheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.pointcheck.navigation.AppNavigation
import com.pointcheck.ui.theme.PointCheckTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PointCheckApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointCheckApp() {
    val snackbar = remember { SnackbarHostState() }
    PointCheckTheme {
        Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                AppNavigation(snackbar)
            }
        }
    }
}