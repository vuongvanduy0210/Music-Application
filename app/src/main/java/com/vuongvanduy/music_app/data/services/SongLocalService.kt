package com.vuongvanduy.music_app.data.services

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.vuongvanduy.music_app.common.DEVICE_SONGS_FRAGMENT_TAG
import com.vuongvanduy.music_app.data.common.sortListAscending
import com.vuongvanduy.music_app.data.models.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.Collator
import java.util.Locale
import javax.inject.Inject

class SongLocalService @Inject constructor(@ApplicationContext val context: Context) {

    @SuppressLint("Recycle")
    fun getLocalMusic(): List<Song> {
        val list = mutableListOf<Song>()
        val contentResolver: ContentResolver = context.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver.query(
            uri,
            null, null,
            null, null
        )

        if (cursor == null) {
            Toast.makeText(context, "Something Went Wrong.", Toast.LENGTH_SHORT).show()
        } else if (!cursor.moveToFirst()) {
            Log.e(DEVICE_SONGS_FRAGMENT_TAG, "No Music Found on Device")
        } else {
            //get columns
            val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

            do {
                val resourceId = cursor.getLong(idColumn)
                val name = cursor.getString(titleColumn)
                val singer = cursor.getString(artistColumn)
                val imageId = cursor.getLong(albumIdColumn)

                val resourceUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, resourceId
                ).toString()
                val albumArtUri = Uri
                    .parse("content://media/external/audio/albumart/$imageId")
                    .toString()
                val song = Song(name, singer, resourceUri, albumArtUri)
                list.add(song)
            } while (cursor.moveToNext())

            sortListAscending(list)
        }
        return list
    }
}