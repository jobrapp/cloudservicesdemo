package com.jobrapp.cloudservices.services.googledrive

import android.app.Activity
import android.content.Intent
import android.os.Environment
import android.support.v4.app.ActivityCompat.startIntentSenderForResult
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.drive.*
import com.jobrapp.cloudservices.services.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import okio.Okio
import java.io.File
import java.util.*


/**
 * Handle Google Drive Services
 */
class GoogleDriveService(val activity: Activity, val config: GoogleDriveConfig) : BaseService() {
    /**
     * Handles high-level drive functions like sync
     */
    private var driveClient: DriveClient? = null

    /**
     * Handle access to Drive resources/files.
     */
    private var driveResourceClient: DriveResourceClient? = null

    private val googleSigninClient : GoogleSignInClient by lazy {
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        for (scope in SCOPES) {
            builder.requestScopes(scope)
        }
        val signInOptions = builder.build()
        GoogleSignIn.getClient(activity, signInOptions)
    }
    private var signInAccount : GoogleSignInAccount? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            serviceListener?.cancelled()
            return
        }
        when(requestCode) {
            REQUEST_CODE_OPEN_ITEM -> if (data != null ) openItem(data)
            REQUEST_CODE_SIGN_IN -> if (data != null ) handleSignin(data)
        }
    }

    private fun handleSignin(data : Intent) {
        val getAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
        if (getAccountTask.isSuccessful) {
            initializeDriveClient(getAccountTask.result)
        } else {
            Log.e(TAG, "Sign-in failed.")
            serviceListener?.handleError(CloudServiceException("Sign-in failed.", getAccountTask.exception))
        }
    }

    /**
     * Starts the sign-in process and initializes the Drive client.
     */
    override fun auth() {
        authState = AuthState.Authenticating()
        if (signInAccount == null) {
            val requiredScopes = HashSet<Scope>(2)
            requiredScopes.add(Drive.SCOPE_FILE)
            requiredScopes.add(Drive.SCOPE_APPFOLDER)
            signInAccount = GoogleSignIn.getLastSignedInAccount(activity)
            val containsScope = signInAccount?.grantedScopes?.containsAll(requiredScopes)
            if (signInAccount != null && containsScope != null && containsScope) {
                initializeDriveClient(signInAccount!!)
            } else {
                activity.startActivityForResult(googleSigninClient.signInIntent, REQUEST_CODE_SIGN_IN)
            }
        } else {
            authState = AuthState.Authenticated()
            pickFiles(null)
        }
    }

    override fun logout() {
        authState = AuthState.Init()
        googleSigninClient.signOut()
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private fun initializeDriveClient(signInAccount: GoogleSignInAccount) {
        authState = AuthState.Authenticated()
        driveClient = Drive.getDriveClient(activity.getApplicationContext(), signInAccount)
        driveResourceClient = Drive.getDriveResourceClient(activity.getApplicationContext(), signInAccount)
        onDriveClientReady()
    }

    /**
     * Shows a toast message.
     */
    protected fun showMessage(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

    protected fun onDriveClientReady() {
        getFiles(null)
    }

    override fun downloadFile(data: FileDataType?) {
        if (data == null || (data !is FileData)) {
            Log.e(TAG, "downloadFile data is null")
            serviceListener?.handleError(CloudServiceException(""))
            return
        }
        val drive = data.data as DriveFile
        var fileName = "test.pdf"
        driveResourceClient?.getMetadata(drive)
                ?.addOnSuccessListener {
                    fileName = it.originalFilename
                }
        val openFileTask = driveResourceClient?.openFile(drive, DriveFile.MODE_READ_ONLY)
        openFileTask
                ?.continueWithTask({ task ->
                    val contents = task.getResult()
                    contents.getInputStream().use {
                        try {
                            //This is the app's download directory, not the phones
                            val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            val tempFile = File(storageDir, fileName)
                            tempFile.createNewFile()
                            val sink = Okio.buffer(Okio.sink(tempFile))
                            sink.writeAll(Okio.source(it))
                            sink.close()
                            serviceListener?.fileDownloaded(tempFile)
                        } catch (e : Exception) {
                            Log.e(TAG, "Problems saving file", e)
                            launch(UI) {
                                serviceListener?.handleError(CloudServiceException("Problems downloading file"))
                            }
                        }
                    }
                    val discardTask = driveResourceClient?.discardContents(contents)
                    discardTask
                })
                ?.addOnFailureListener({ e ->
                    // Handle failure
                    Log.e(TAG, "Unable to read contents", e)
                    logout()
                    auth()
                })
    }

    /**
     * Prompts the user to select a folder using OpenFileActivity.
     *
     * @param openOptions Filter that should be applied to the selection
     * @return Task that resolves with the selected item's ID.
     */
    private fun pickItem(openOptions: OpenFileActivityOptions) {
        val openTask = driveClient?.newOpenFileActivityIntentSender(openOptions)
        openTask?.let {
            openTask.continueWith { task ->
                startIntentSenderForResult(activity, task.result, REQUEST_CODE_OPEN_ITEM, null, 0, 0, 0, null)
            }
        }
    }

    override fun getFiles(path: String?) {
        pickFiles(null)

    }

    /**
     * Prompts the user to select a text file using OpenFileActivity.
     *
     * @return Task that resolves with the selected item's ID.
     */
    protected fun pickFiles(driveId : DriveId?) {
        val builder = OpenFileActivityOptions.Builder()
        if (config.mimeTypes != null) {
            builder.setMimeType(config.mimeTypes)
        } else {
            builder.setMimeType(documentMimeTypes)
        }
        if (config.activityTitle != null && config.activityTitle.isNotEmpty()) {
            builder.setActivityTitle(config.activityTitle)
        }
        if (driveId != null) {
            builder.setActivityStartFolder(driveId)
        }
        val openOptions = builder.build()
        pickItem(openOptions)
    }

    fun openItem(data: Intent) {
        val driveId = data.getParcelableExtra<DriveId>(
                OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID)
        if (driveId != null && driveId.resourceId != null) {
            downloadFile(FileData(driveId.resourceId!!, driveId.resourceId!!, false, data = driveId.asDriveFile()))
        }
    }

    companion object {
        private val SCOPES = setOf<Scope>(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
        val documentMimeTypes = arrayListOf("application/pdf", "application/msword" ,"application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    }
}