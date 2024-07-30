package com.tailspin.niispecvuz

import android.os.Bundle
import android.os.storage.StorageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tailspin.niispecvuz.ui.theme.NiiSpecVuzTheme


class MainActivity : ComponentActivity() {

    private lateinit var storageService: StorageManager
    private val mainViewModel: MainViewModel by viewModels { MainViewModel.factory(storageService) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        storageService = getSystemService(STORAGE_SERVICE) as StorageManager
        setContent {
            NiiSpecVuzTheme {
                val state by mainViewModel.scanState.collectAsState()
                Screen(state = state) {
                    mainViewModel.scanningExternalStorage(this)
                }
            }
        }
    }

}

@Composable
fun Screen(state: ScanApkState, onStartScan: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onStartScan,
            enabled = state !is ScanApkState.ApkScanningState
        ) {
            Text("Start APK Scan")
        }
        Spacer(modifier = Modifier.height(16.dp))
        when (state) {
            is ScanApkState.ApkScanningCompleteState -> Text("APK Files Found: ${state.apkCount}")
            ScanApkState.ApkScanningEmptyState -> Text("Apk Scanner")
            is ScanApkState.ApkScanningErrorState -> Text("Error: ${state.errorMessage}")
            ScanApkState.ApkScanningState -> Text("Scanning...")
            ScanApkState.StoragePermissionDeniedState -> Text("Permission Denied")
        }
    }
}
