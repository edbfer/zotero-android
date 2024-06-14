package org.zotero.android.screens.settings.linked_files

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.selects.select
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.zotero.android.architecture.BaseViewModel2
import org.zotero.android.architecture.Defaults
import org.zotero.android.architecture.EventBusConstants
import org.zotero.android.architecture.ViewEffect
import org.zotero.android.architecture.ViewState
import org.zotero.android.sync.SessionController
import javax.inject.Inject


@HiltViewModel
internal class SettingsLinkedFilesViewModel @Inject constructor(
    private val defaults: Defaults,
    private val sessionController: SessionController,
    private val context: Context,
) : BaseViewModel2<SettingsLinkedFilesViewState, SettingsLinkedFilesViewEffect>(SettingsLinkedFilesViewState()) {

    fun init() = initOnce {
        updateState {
            copy(root_path = defaults.getRootLinkedFilesPath().toString()!!)
        }
    }

    fun onBack() {
        triggerEffect(SettingsLinkedFilesViewEffect.OnBack)
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )

        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            if (cursor != null) cursor.close()
        }
        return null
    }

    fun getPath(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), id.toLong()
                )

                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )

                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            // Return the remote address

            if (isGooglePhotosUri(uri)) return uri.lastPathSegment

            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }

        return null
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(pathWasSelected: EventBusConstants.PathWasSelected)
    {
        val selected_uri = pathWasSelected.uri!!
        EventBus.getDefault().unregister(this)

        Log.w("TestTest", selected_uri.toString())

        //make sure it is permanent
        context.contentResolver.takePersistableUriPermission(selected_uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        val docFiles = DocumentFile.fromTreeUri(context, selected_uri)!!
        defaults.setRootLinkedFilesPath(selected_uri)

        updateState {
            copy(root_path = defaults.getRootLinkedFilesPath().toString())
        }
    }

    fun onPathSelection(onPathSelect: () -> Unit)
    {
        EventBus.getDefault().register(this)
        onPathSelect()
    }

}

internal data class SettingsLinkedFilesViewState(
    val root_path: String = "",
) : ViewState

internal sealed class SettingsLinkedFilesViewEffect : ViewEffect {
    object OnBack : SettingsLinkedFilesViewEffect()
}
