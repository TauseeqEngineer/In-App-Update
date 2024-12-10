package com.android.inappupdate

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.inappupdate.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.install.model.ActivityResult

class MainActivity : AppCompatActivity(), AppUpdateManagerHandler.UpdateCallback {
    private var updateManagerHandler: AppUpdateManagerHandler? = null
    private var updateLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupUpdateManagerHandler()
        registerLauncher()
        updateManagerHandler?.checkForAppUpdate()
    }

    override fun onResume() {
        super.onResume()
        checkUpdateState()
    }

    private fun checkUpdateState() {
        updateManagerHandler?.checkUpdateState()
    }

    private fun setupUpdateManagerHandler() {
        updateManagerHandler = AppUpdateManagerHandler(this, this)
    }

    private fun registerLauncher() {
        updateLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> onUpdateAccepted()
                Activity.RESULT_CANCELED -> onUpdateCanceled()
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> onUpdateFailed("Update Failed")
            }
        }
    }

    override fun onUpdateAccepted() {
        recordLog("Update accepted, app will be updated in the background")
    }

    override fun onUpdateCanceled() {
        recordLog("Update canceled by the user")
    }

    override fun onUpdateFailed(error: String) {
        recordLog("Update failed, handle error")
        showSnackBar(error)
    }

    override fun onNoUpdateAvailable() {
        recordLog("No update available")
        showSnackBar("No Update Available")
    }

    override fun onUpdateDownloaded() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "An update has just been downloaded. Restart to apply the update.",
            Snackbar.LENGTH_INDEFINITE
        ).setAction("Restart") {
            updateManagerHandler?.completeUpdate()
        }.show()
    }

    private fun recordLog(message: String) {
        Log.d("Update", message)
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_SHORT
        ).show()
        binding.tvUpdateState.text= message
    }
}
