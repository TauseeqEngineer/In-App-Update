package com.android.inappupdate

import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class AppUpdateManagerHandler(
    private val activity: AppCompatActivity,
    private val updateCallback: UpdateCallback
) {
    interface UpdateCallback {
        fun onUpdateAccepted()
        fun onUpdateCanceled()
        fun onUpdateFailed(error: String)
        fun onNoUpdateAvailable()
        fun onUpdateDownloaded()
    }

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private val requestCode = 123

    fun checkForAppUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            val isUpdateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isImmediateUpdateAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            val isFlexibleUpdateAllowed = appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)

            when {
                isUpdateAvailable && isImmediateUpdateAllowed -> {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            activity,
                            requestCode
                        )
                    } catch (exception: Exception) {
                        exception.message?.let {
                            updateCallback.onUpdateFailed(it)
                        }
                    }
                }
                isUpdateAvailable && isFlexibleUpdateAllowed -> {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.FLEXIBLE,
                            activity,
                            requestCode
                        )
                    } catch (exception: Exception) {
                        exception.message?.let {
                            updateCallback.onUpdateFailed(it)
                        }
                    }
                    appUpdateManager.registerListener { state ->
                        if (state.installStatus() == InstallStatus.DOWNLOADED) {
                            updateCallback.onUpdateDownloaded()
                        }
                    }
                }
                else -> {
                    updateCallback.onNoUpdateAvailable()
                }
            }
        }.addOnFailureListener {exception->
            exception.message?.let {
                updateCallback.onUpdateFailed(it)
            }
        }
    }

    fun checkUpdateState() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                updateCallback.onUpdateDownloaded()
            }
        }
    }

    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }
}
