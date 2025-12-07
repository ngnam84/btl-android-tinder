package com.btl.tinder.data

import android.net.Uri

/**
 * A data class to represent a media item selected from the local device,
 * before it's uploaded.
 *
 * @param uri The local content URI of the media.
 * @param type The type of the media, e.g., "image" or "video".
 */
data class LocalMediaItem(val uri: Uri, val type: String)
