package com.jobrapp.cloudservices.services.gmail

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.drive.Drive
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.MessagePartHeader
import com.jobrapp.cloudservices.services.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import okio.Okio
import java.io.File


/**
 * Handle Gmail Integration
 * Requires api_key.txt in assets folder to work. Get this from the developer console
 */
class GmailService(val activity: Activity) : BaseService() {
    private val googleAccountCredential: GoogleAccountCredential
    var prefs: Prefs = Prefs(activity)
    private var accountName: String? = null
    private val googleSigninClient : GoogleSignInClient by lazy {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .requestScopes(Drive.SCOPE_APPFOLDER)
                .build()
        GoogleSignIn.getClient(activity, signInOptions)
    }

    init {
        googleAccountCredential = GoogleAccountCredential.usingOAuth2(
                activity.getApplicationContext(), SCOPES)
                .setBackOff(ExponentialBackOff())
    }

    override fun auth() {
        authState = AuthState.Authenticating()
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (!hasPermissions()) {
            requestPermission()
        } else if (!hasAccount()) {
            activity.startActivityForResult(googleAccountCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER)
        } else {
            getEmails()
        }
    }

    override fun logout() {
        authState = AuthState.Init()
        googleSigninClient.signOut()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_GET_ACCOUNT_PERMISSIONS -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    auth()
                } else {
                    serviceListener?.cancelled()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            serviceListener?.cancelled()
            return
        }
        when (requestCode) {
            REQUEST_ACCOUNT_PICKER -> {
                if (data == null || data.extras == null) {
                    Log.e(TAG, "No result for Account Picker")
                    serviceListener?.cancelled()
                    return
                }
                accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                prefs.putString(ACCOUNT_NAME, accountName!!)
                googleAccountCredential.selectedAccountName = accountName
                authState = AuthState.Authenticated()
                getEmails()
            }
            REQUEST_GOOGLE_PLAY_SERVICES, REQUEST_GET_ACCOUNT_PERMISSIONS -> {
                auth()
            }
        }
    }

    fun getEmails() {
        launch(CommonPool) {
            try {
                val gmail = getGmailClient()
                // Get the current user's emails that have attachments
                var listMessagesResponse = gmail.users().messages().list("me").setQ(QUERY).execute()
                if (listMessagesResponse != null) {
                    val allMessages = ArrayList<Message>()
                    var messages = listMessagesResponse.messages
                    while (messages != null) {
                        allMessages.addAll(messages)
                        if (listMessagesResponse.nextPageToken != null) {
                            listMessagesResponse = gmail.users().messages().list("me")
                                    .setQ(QUERY)
                                    .setPageToken(listMessagesResponse.nextPageToken)
                                    .execute()
                            messages = listMessagesResponse.messages
                        } else {
                            break
                        }
                    }
                    val gMessages = ArrayList<GmailMessage>()
                    for (message in messages) {
                        val gMessage = gmail.users().messages().get("me", message.id)
                                .setFormat("metadata").execute()
                        if (gMessage != null) {
                            gMessages.add(parseMessage(gMessage))
                        }
                    }
                    launch(UI) {
                        serviceListener?.currentFiles("", gMessages)
                    }
                }
            } catch (e: UserRecoverableAuthIOException) {
                activity.startActivityForResult(e.intent, REQUEST_GET_ACCOUNT_PERMISSIONS)
            } catch (e: Exception) {
                Log.e(TAG, "Problems getting emails", e)
            }
        }
    }

    override fun getFiles(path: String?) {
        if (path == null) {
            return
        }
        launch(CommonPool) {
            try {
                val gmail = getGmailClient()
                val message = gmail.users().messages().get("me", path).execute()
                val parts = message.payload.parts
                val gmailList = ArrayList<GmailFile>()
                for (part in parts) {
                    if (part.filename != null && part.filename.length > 0) {
                        val filename = part.getFilename();
                        gmailList.add(GmailFile(path, filename))
                    }
                }
                launch(UI) {
                    serviceListener?.currentFiles("", gmailList)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Problems downloading email", e)
            }
        }
    }

    private fun getGmailClient(): Gmail {
        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        val gmail = Gmail.Builder(transport, jsonFactory, googleAccountCredential)
                .setApplicationName("CloudServicesDemo")
                .build()
        return gmail
    }

    private fun parseMessage(message: Message): GmailMessage {
        val headers = message.payload.headers
        val from = parseHeaders(headers, "From")
        val date = parseHeaders(headers, "Date")
        val subject = parseHeaders(headers, "Subject")
        val gMessage = GmailMessage(message.id, from, subject, date)
        return gMessage
    }

    private fun parseHeaders(headers: List<MessagePartHeader>, keyword: String): String {
        for (header in headers) {
            if (keyword == header.name) {
                return header.value
            }
        }
        return ""
    }

    private fun hasPermissions(): Boolean {
        return PermissionsManager.hasPermission(activity, Manifest.permission.GET_ACCOUNTS)
    }

    private fun hasAccount(): Boolean {
        if (accountName == null) {
            accountName = prefs.getString(ACCOUNT_NAME)
            googleAccountCredential.selectedAccountName = accountName
        }
        return accountName != null
    }

    private fun requestPermission() {
        PermissionsManager.requestPermissions(activity, arrayOf(Manifest.permission.GET_ACCOUNTS), REQUEST_GET_ACCOUNT_PERMISSIONS)
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(activity)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(activity)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     * Google Play Services on this device.
     */
    fun showGooglePlayServicesAvailabilityErrorDialog(
            connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                activity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }

    override fun downloadFile(data: FileDataType?) {
        if (data == null || (data !is GmailFile)) {
            return
        }
        launch(CommonPool) {
            try {
                val gmail = getGmailClient()
                val message = gmail.users().messages().get("me", data.id).execute()
                if (message == null || message.payload == null ||
                        message.payload.parts == null) {
                    launch(UI) {
                        serviceListener?.handleError(CloudServiceException("No attachment found"))
                    }
                    return@launch
                }
                val parts = message.payload.parts
                for (part in parts) {
                    if (part.filename != null && part.filename.length > 0) {
                        val filename = part.getFilename();
                        val attId = part.getBody().getAttachmentId();
                        val attachPart = gmail.users().messages().attachments().get("me", data.id, attId).execute()
                        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        val file = File(storageDir, filename)
                        file.createNewFile()
                        val sink = Okio.buffer(Okio.sink(file))
                        val fileByteArray = Base64.decodeBase64(attachPart.data)
                        sink.write(fileByteArray)
                        sink.close()
                        launch(UI) {
                            serviceListener?.fileDownloaded(file)
                        }
                        break
                    }
                }
            } catch (e: UserRecoverableAuthIOException) {
                activity.startActivityForResult(e.intent, REQUEST_GET_ACCOUNT_PERMISSIONS)
            } catch (e: Exception) {
                Log.e(TAG, "Problems downloading email", e)
                launch(UI) {
                    serviceListener?.handleError(CloudServiceException("Problems downloading file"))
                }
            }
        }
    }

    companion object {
        private val SCOPES = listOf(GmailScopes.GMAIL_READONLY)
        const val REQUEST_GET_ACCOUNT_PERMISSIONS = 300
        const val REQUEST_GOOGLE_PLAY_SERVICES = 301
        const val REQUEST_ACCOUNT_PICKER = 302
        const val ACCOUNT_NAME = "ACCOUNT_NAME"
        const val QUERY = "has:attachment AND filename:.doc OR filename:.docx OR filename:.pdf"
    }
}