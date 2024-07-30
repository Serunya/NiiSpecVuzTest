package com.tailspin.niispecvuz

sealed interface ScanApkState {
    data object ApkScanningEmptyState : ScanApkState
    data object ApkScanningState : ScanApkState
    data object StoragePermissionDeniedState : ScanApkState
    class ApkScanningCompleteState(val apkCount: Int) : ScanApkState
    class ApkScanningErrorState(val errorMessage: String) : ScanApkState
}